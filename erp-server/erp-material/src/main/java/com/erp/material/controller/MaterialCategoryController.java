package com.erp.material.controller;

import com.erp.common.core.domain.R;
import com.erp.material.domain.dto.MaterialCategoryRequest;
import com.erp.material.domain.vo.MaterialCategoryVO;
import com.erp.material.permission.MaterialPermissionCodes;
import com.erp.material.service.MaterialCategoryService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/material/categories")
public class MaterialCategoryController {
    private final MaterialCategoryService materialCategoryService;

    public MaterialCategoryController(MaterialCategoryService materialCategoryService) {
        this.materialCategoryService = materialCategoryService;
    }

    @GetMapping("/tree")
    @PreAuthorize("hasAuthority(T(com.erp.material.permission.MaterialPermissionCodes).CATEGORY_LIST)")
    public R<List<MaterialCategoryVO>> tree() {
        return R.ok(materialCategoryService.tree());
    }

    @PostMapping
    @PreAuthorize("hasAuthority(T(com.erp.material.permission.MaterialPermissionCodes).CATEGORY_CREATE)")
    public R<MaterialCategoryVO> create(@Valid @RequestBody MaterialCategoryRequest request) {
        return R.ok(materialCategoryService.save(null, request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority(T(com.erp.material.permission.MaterialPermissionCodes).CATEGORY_UPDATE)")
    public R<MaterialCategoryVO> update(@PathVariable UUID id, @Valid @RequestBody MaterialCategoryRequest request) {
        return R.ok(materialCategoryService.save(id, request));
    }
}
