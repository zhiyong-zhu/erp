package com.erp.product.controller;

import com.erp.common.core.domain.R;
import com.erp.product.domain.dto.ProductPackageRequest;
import com.erp.product.domain.vo.ProductPackageVO;
import com.erp.product.permission.ProductPermissionCodes;
import com.erp.product.service.ProductPackageService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/product/products/{productId}/packages")
public class ProductPackageController {
    private final ProductPackageService productPackageService;

    public ProductPackageController(ProductPackageService productPackageService) {
        this.productPackageService = productPackageService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority(T(com.erp.product.permission.ProductPermissionCodes).PACKAGE_LIST)")
    public R<List<ProductPackageVO>> list(@PathVariable UUID productId) {
        return R.ok(productPackageService.list(productId));
    }

    @PostMapping
    @PreAuthorize("hasAuthority(T(com.erp.product.permission.ProductPermissionCodes).PACKAGE_UPDATE)")
    public R<List<ProductPackageVO>> save(@PathVariable UUID productId, @Valid @RequestBody List<@Valid ProductPackageRequest> requests) {
        return R.ok(productPackageService.save(productId, requests));
    }
}
