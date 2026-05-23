package com.erp.product.controller;

import com.erp.common.core.domain.R;
import com.erp.product.domain.dto.ProductBomRequest;
import com.erp.product.domain.vo.ProductBomVO;
import com.erp.product.permission.ProductPermissionCodes;
import com.erp.product.service.ProductBomService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/product/products/{productId}/bom")
public class ProductBomController {
    private final ProductBomService productBomService;

    public ProductBomController(ProductBomService productBomService) {
        this.productBomService = productBomService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority(T(com.erp.product.permission.ProductPermissionCodes).BOM_LIST)")
    public R<ProductBomVO> get(@PathVariable UUID productId) {
        return R.ok(productBomService.get(productId));
    }

    @PostMapping
    @PreAuthorize("hasAuthority(T(com.erp.product.permission.ProductPermissionCodes).BOM_UPDATE)")
    public R<ProductBomVO> save(@PathVariable UUID productId, @Valid @RequestBody ProductBomRequest request) {
        return R.ok(productBomService.save(productId, request));
    }
}
