package com.tcm.prescription.service;

import com.tcm.prescription.common.ErrorCode;
import com.tcm.prescription.dto.MergeReq;
import com.tcm.prescription.dto.MergeResp;
import com.tcm.prescription.entity.Herb;
import com.tcm.prescription.entity.Prescription;
import com.tcm.prescription.entity.PrescriptionItem;
import com.tcm.prescription.exception.ServiceException;
import com.tcm.prescription.repository.HerbRepository;
import com.tcm.prescription.repository.PrescriptionItemRepository;
import com.tcm.prescription.repository.PrescriptionRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PrescriptionServiceTest {

    @Mock
    private PrescriptionRepository prescriptionRepository;
    @Mock
    private HerbRepository herbRepository;
    @Mock
    private PrescriptionItemRepository prescriptionItemRepository;

    @InjectMocks
    private PrescriptionService prescriptionService;

    private Herb herbA;
    private Herb herbB;
    private Herb herbC;

    @BeforeEach
    void setUp() {
        herbA = new Herb(); herbA.setId(1L); herbA.setNameCn("HerbA");
        herbB = new Herb(); herbB.setId(2L); herbB.setNameCn("HerbB");
        herbC = new Herb(); herbC.setId(3L); herbC.setNameCn("HerbC");
    }

    @Test
    void testMerge_Case1_NormalMerge() {
        // P1: A 10g, B 20g
        Prescription p1 = new Prescription(); p1.setId(10L); p1.setName("P1");
        PrescriptionItem p1_A = createItem(p1, herbA, "10.00");
        PrescriptionItem p1_B = createItem(p1, herbB, "20.00");

        // P2: A 5g, C 10g
        Prescription p2 = new Prescription(); p2.setId(20L); p2.setName("P2");
        PrescriptionItem p2_A = createItem(p2, herbA, "5.00");
        PrescriptionItem p2_C = createItem(p2, herbC, "10.00");

        List<Long> ids = Arrays.asList(10L, 20L);

        when(prescriptionRepository.findAllByIds(ids)).thenReturn(Arrays.asList(p1, p2));
        when(prescriptionItemRepository.findByPrescriptionIds(ids))
                .thenReturn(Arrays.asList(p1_A, p1_B, p2_A, p2_C));

        MergeReq req = new MergeReq();
        req.setPrescriptionIds(ids);
        
        MergeResp resp = prescriptionService.merge(req);

        // Expect: A 10 (max of 10,5), B 20, C 10
        Assertions.assertEquals(3, resp.getItems().size());
        
        MergeResp.MergedItem itemA = findItem(resp, 1L);
        Assertions.assertNotNull(itemA);
        Assertions.assertEquals(new BigDecimal("10.00"), itemA.getDoseG());
        
        MergeResp.MergedItem itemB = findItem(resp, 2L);
        Assertions.assertNotNull(itemB);
        Assertions.assertEquals(new BigDecimal("20.00"), itemB.getDoseG());

        MergeResp.MergedItem itemC = findItem(resp, 3L);
        Assertions.assertNotNull(itemC);
        Assertions.assertEquals(new BigDecimal("10.00"), itemC.getDoseG());
    }

    @Test
    void testMerge_Case2_SameDose() {
         // P1: A 10g
         // P2: A 10g
         // Result: A 10g
         Prescription p1 = new Prescription(); p1.setId(10L);
         PrescriptionItem p1_A = createItem(p1, herbA, "10.00");
         
         Prescription p2 = new Prescription(); p2.setId(20L);
         PrescriptionItem p2_A = createItem(p2, herbA, "10.00");
         
         List<Long> ids = Arrays.asList(10L, 20L);
         when(prescriptionRepository.findAllByIds(ids)).thenReturn(Arrays.asList(p1, p2));
         when(prescriptionItemRepository.findByPrescriptionIds(ids)).thenReturn(Arrays.asList(p1_A, p2_A));
         
         MergeReq req = new MergeReq();
         req.setPrescriptionIds(ids);
         MergeResp resp = prescriptionService.merge(req);
         
         Assertions.assertEquals(1, resp.getItems().size());
         Assertions.assertEquals(new BigDecimal("10.00"), resp.getItems().get(0).getDoseG());
         Assertions.assertEquals(2, resp.getItems().get(0).getSources().size());
    }

    @Test
    void testMerge_Case3_NotFound() {
        List<Long> ids = Arrays.asList(10L, 99L);
        // Only returns P1 (id 10)
        Prescription p1 = new Prescription(); p1.setId(10L);
        when(prescriptionRepository.findAllByIds(ids)).thenReturn(Collections.singletonList(p1));
        
        MergeReq req = new MergeReq();
        req.setPrescriptionIds(ids);

        ServiceException ex = Assertions.assertThrows(ServiceException.class, () -> prescriptionService.merge(req));
        Assertions.assertEquals(ErrorCode.NOT_FOUND.getCode(), ex.getCode());
    }

    private PrescriptionItem createItem(Prescription p, Herb h, String dose) {
        PrescriptionItem item = new PrescriptionItem();
        item.setPrescription(p);
        item.setHerb(h);
        item.setHerbNameSnapshot(h.getNameCn());
        item.setDoseG(new BigDecimal(dose));
        return item;
    }
    
    private MergeResp.MergedItem findItem(MergeResp resp, Long herbId) {
        return resp.getItems().stream().filter(i -> i.getHerbId().equals(herbId)).findFirst().orElse(null);
    }
}
