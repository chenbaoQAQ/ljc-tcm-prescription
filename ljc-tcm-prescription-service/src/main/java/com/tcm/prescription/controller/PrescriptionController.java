package com.tcm.prescription.controller;

import com.tcm.prescription.common.Result;
import com.tcm.prescription.dto.*;
import com.tcm.prescription.service.PrescriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/prescriptions")
@RequiredArgsConstructor
@Tag(name = "Prescription Management")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    @GetMapping
    @Operation(summary = "List Prescriptions")
    public Result<Page<PrescriptionSimpleResp>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(prescriptionService.list(keyword, page, size));
    }

    @PostMapping
    @Operation(summary = "Create Prescription")
    public Result<PrescriptionDetailResp> create(@RequestBody @Validated PrescriptionCreateReq req) {
        return Result.success(prescriptionService.create(req));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Prescription Detail")
    public Result<PrescriptionDetailResp> get(@PathVariable Long id) {
        return Result.success(prescriptionService.getDetail(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update Prescription")
    public Result<PrescriptionDetailResp> update(@PathVariable Long id, @RequestBody @Validated PrescriptionCreateReq req) {
        return Result.success(prescriptionService.update(id, req));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Prescription")
    public Result<Void> delete(@PathVariable Long id) {
        prescriptionService.delete(id);
        return Result.success(null);
    }

    @PostMapping("/merge")
    @Operation(summary = "Merge Prescriptions")
    public Result<MergeResp> merge(@RequestBody @Validated MergeReq req) {
        return Result.success(prescriptionService.merge(req));
    }
}
