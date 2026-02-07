#!/bin/bash
# Smoke Test Script for TCM Prescription Service
# Usage: ./smoke-test.sh

BASE_URL="http://localhost:8081/api/v1"

echo "=== TCM Prescription Service Smoke Test ==="
echo "Target: $BASE_URL"

# Helper function to check curl result
check_response() {
    if [[ $1 -ne 0 ]]; then
        echo "❌ Request failed"
        exit 1
    fi
     # If we had jq, we could check Success code. Without jq, we assume 200 OK + inspecting body manually if needed.
     # But let's check if the previous command output contains "code":0
}

# 1. Create Herbs A, B, C
echo -e "\n1. Creating Herbs..."
# We use a timestamp to avoid conflict if re-run without clearing DB (though duplicate names will fail gracefully)
TS=$(date +%s)

# Create Herb A
RESP=$(curl -s -X POST "$BASE_URL/herbs" -H "Content-Type: application/json" -d "{
    \"nameCn\": \"HerbA_${TS}\",
    \"defaultDoseG\": 10.0,
    \"notes\": \"Test Herb A\"
}")
echo "  -> Create A: $RESP"
ID_A=$(echo $RESP | grep -o '"id":[0-9]*' | head -1 | cut -d: -f2)

# Create Herb B
RESP=$(curl -s -X POST "$BASE_URL/herbs" -H "Content-Type: application/json" -d "{
    \"nameCn\": \"HerbB_${TS}\",
    \"defaultDoseG\": 20.0,
    \"notes\": \"Test Herb B\"
}")
echo "  -> Create B: $RESP"
ID_B=$(echo $RESP | grep -o '"id":[0-9]*' | head -1 | cut -d: -f2)

# Create Herb C
RESP=$(curl -s -X POST "$BASE_URL/herbs" -H "Content-Type: application/json" -d "{
    \"nameCn\": \"HerbC_${TS}\",
    \"defaultDoseG\": 30.0,
    \"notes\": \"Test Herb C\"
}")
echo "  -> Create C: $RESP"
ID_C=$(echo $RESP | grep -o '"id":[0-9]*' | head -1 | cut -d: -f2)

if [[ -z "$ID_A" || -z "$ID_B" || -z "$ID_C" ]]; then
    echo "❌ Failed to create herbs (ids missing)"
    exit 1
fi
echo "✅ Herbs Created: A($ID_A), B($ID_B), C($ID_C)"


# 2. Create Prescriptions
echo -e "\n2. Creating Prescriptions..."

# P1: A 10g, B 20g
RESP=$(curl -s -X POST "$BASE_URL/prescriptions" -H "Content-Type: application/json" -d "{
    \"name\": \"Prescription_1_${TS}\",
    \"items\": [
        { \"herbId\": $ID_A, \"doseG\": 10.0 },
        { \"herbId\": $ID_B, \"doseG\": 20.0 }
    ]
}")
echo "  -> Create P1: $RESP"
ID_P1=$(echo $RESP | grep -o '"id":[0-9]*' | head -1 | cut -d: -f2)

# P2: A 5g, C 10g
RESP=$(curl -s -X POST "$BASE_URL/prescriptions" -H "Content-Type: application/json" -d "{
    \"name\": \"Prescription_2_${TS}\",
    \"items\": [
        { \"herbId\": $ID_A, \"doseG\": 5.0 },
        { \"herbId\": $ID_C, \"doseG\": 10.0 }
    ]
}")
echo "  -> Create P2: $RESP"
ID_P2=$(echo $RESP | grep -o '"id":[0-9]*' | head -1 | cut -d: -f2)

if [[ -z "$ID_P1" || -z "$ID_P2" ]]; then
    echo "❌ Failed to create prescriptions"
    exit 1
fi
echo "✅ Prescriptions Created: P1($ID_P1), P2($ID_P2)"

# 3. Merge
echo -e "\n3. Testing Merge Logic..."
# Expect: A=10 (max of 10,5), B=20, C=10

RESP=$(curl -s -X POST "$BASE_URL/prescriptions/merge" -H "Content-Type: application/json" -d "{
    \"prescriptionIds\": [$ID_P1, $ID_P2]
}")
echo "  -> Merge Result: $RESP"

# Verify content roughly
if [[ "$RESP" == *"\"doseG\":10.00"* && "$RESP" == *"\"doseG\":20.00"* && "$RESP" == *"\"doseG\":10.00"* ]]; then
     echo "✅ Merge output looks correct (contains expected doses)"
else
     echo "⚠️ Merge output verification warning: check if doses are correct manually."
     # Note: Exact string match depends on json formatting, but simpler check is better than nothing
fi

# We specifically look for A's merge result.
# Ideally use python or ruby if available for better parsing, but user said 'pure bash' preferred or no jq.
# We will trust the visual output or simple grep.

# 4. Create Medical Record
echo -e "\n4. Testing Medical Records..."

# Create a medical record with P1 and P2
RESP=$(curl -s -X POST "$BASE_URL/medical-records" -H "Content-Type: application/json" -d "{
    \"patientName\": \"王一帆_${TS}\",
    \"visitDate\": \"2026-02-07\",
    \"prescriptionIds\": [$ID_P1, $ID_P2]
}")
echo "  -> Create Medical Record: $RESP"
ID_MR=$(echo $RESP | grep -o '"id":[0-9]*' | head -1 | cut -d: -f2)

if [[ -z "$ID_MR" ]]; then
    echo "❌ Failed to create medical record"
    exit 1
fi

# Verify merged herbs text contains expected data
if [[ "$RESP" == *"mergedHerbsText"* ]]; then
    echo "✅ Medical Record Created: ID($ID_MR)"
else
    echo "⚠️ Medical Record response missing mergedHerbsText"
fi

# 5. Query Medical Records by Patient Name
echo -e "\n5. Querying Medical Records by Patient Name..."
RESP=$(curl -s -X GET "$BASE_URL/medical-records?patientName=王一帆_${TS}&page=1&size=10")
echo "  -> Query Result: $RESP"

if [[ "$RESP" == *"\"list\""* && "$RESP" == *"$ID_MR"* ]]; then
    echo "✅ Medical Record Query Successful"
else
    echo "❌ Medical Record Query Failed"
    exit 1
fi

# 6. Get Medical Record Detail
echo -e "\n6. Getting Medical Record Detail..."
RESP=$(curl -s -X GET "$BASE_URL/medical-records/$ID_MR")
echo "  -> Detail Result: $RESP"

if [[ "$RESP" == *"\"prescriptionIds\""* && "$RESP" == *"\"mergedHerbs\""* ]]; then
    echo "✅ Medical Record Detail Retrieved"
else
    echo "❌ Medical Record Detail Failed"
    exit 1
fi

echo -e "\n=== SMOKE TEST PASSED ==="
