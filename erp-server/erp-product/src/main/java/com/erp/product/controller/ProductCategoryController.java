package com.erp.product.controller;

import com.erp.common.core.domain.R;
import com.erp.product.domain.dto.CategoryCreateRequest;
import com.erp.product.domain.dto.CategoryUpdateRequest;
import com.erp.product.domain.vo.ProductCategoryVO;
import com.erp.product.permission.ProductPermissionCodes;
import com.erp.product.service.ProductCategoryService;
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
@RequestMapping("/api/v1/product/categories")
public class ProductCategoryController {
    private final ProductCategoryService productCategoryService;

    public ProductCategoryController(ProductCategoryService productCategoryService) {
        this.productCategoryService = productCategoryService;
    }

    @GetMapping("/tree")
    @PreAuthorize("hasAuthority(T(com.erp.product.permission.ProductPermissionCodes).CATEGORY_LIST)")
    public R<List<ProductCategoryVO>> tree() {
        return R.ok(productCategoryService.tree());
    }

    @PostMapping
    @PreAuthorize("hasAuthority(T(com.erp.product.permission.ProductPermissionCodes).CATEGORY_CREATE)")
    public R<ProductCategoryVO> create(@Valid @RequestBody CategoryCreateRequest request) {
        return R.ok(productCategoryService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority(T(com.erp.product.permission.ProductPermissionCodes).CATEGORY_UPDATE)")
    public R<ProductCategoryVO> update(@PathVariable UUID id, @Valid @RequestBody CategoryUpdateRequest request) {
        return R.ok(productCategoryService.update(id, request));
    }
}
