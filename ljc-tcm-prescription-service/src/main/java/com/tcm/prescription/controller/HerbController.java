package com.tcm.prescription.controller;

import com.tcm.prescription.common.Result;
import com.tcm.prescription.dto.HerbCreateReq;
import com.tcm.prescription.dto.HerbQuery;
import com.tcm.prescription.dto.HerbUpdateReq;
import com.tcm.prescription.entity.Herb;
import com.tcm.prescription.service.HerbService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/herbs")
@RequiredArgsConstructor
@Tag(name = "Herb Management")
public class HerbController {

    private final HerbService herbService;

    @GetMapping
    @Operation(summary = "List Herbs")
    public Result<Page<Herb>> list(HerbQuery query) {
        return Result.success(herbService.list(query));
    }

    @PostMapping
    @Operation(summary = "Create Herb")
    public Result<Herb> create(@RequestBody @Validated HerbCreateReq req) {
        return Result.success(herbService.create(req));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Herb Detail")
    public Result<Herb> get(@PathVariable Long id) {
        return Result.success(herbService.getById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update Herb")
    public Result<Herb> update(@PathVariable Long id, @RequestBody @Validated HerbUpdateReq req) {
        return Result.success(herbService.update(id, req));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Herb (Soft)")
    public Result<Void> delete(@PathVariable Long id) {
        herbService.delete(id);
        return Result.success(null);
    }
}
