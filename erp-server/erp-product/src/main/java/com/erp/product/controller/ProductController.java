package com.erp.product.controller;

import com.erp.common.core.domain.R;
import com.erp.common.core.domain.PageVO;
import com.erp.product.domain.dto.ProductCreateRequest;
import com.erp.product.domain.dto.ProductStatusUpdateRequest;
import com.erp.product.domain.dto.ProductUpdateRequest;
import com.erp.product.domain.vo.ProductVO;
import com.erp.product.permission.ProductPermissionCodes;
import com.erp.product.service.ProductService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/product/products")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority(T(com.erp.product.permission.ProductPermissionCodes).PRODUCT_LIST)")
    public R<PageVO<ProductVO>> list(@RequestParam(defaultValue = "1") long pageNum,
                                     @RequestParam(defaultValue = "10") long pageSize,
                                     @RequestParam(required = false) String name,
                                     @RequestParam(required = false) UUID categoryId,
                                     @RequestParam(required = false) Integer status) {
        return R.ok(productService.list(pageNum, pageSize, name, categoryId, status));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority(T(com.erp.product.permission.ProductPermissionCodes).PRODUCT_DETAIL)")
    public R<ProductVO> detail(@PathVariable UUID id) {
        return R.ok(productService.detail(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority(T(com.erp.product.permission.ProductPermissionCodes).PRODUCT_CREATE)")
    public R<ProductVO> create(@Valid @RequestBody ProductCreateRequest request) {
        return R.ok(productService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority(T(com.erp.product.permission.ProductPermissionCodes).PRODUCT_UPDATE)")
    public R<ProductVO> update(@PathVariable UUID id, @Valid @RequestBody ProductUpdateRequest request) {
        return R.ok(productService.update(id, request));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority(T(com.erp.product.permission.ProductPermissionCodes).PRODUCT_UPDATE)")
    public R<Void> updateStatus(@PathVariable UUID id, @Valid @RequestBody ProductStatusUpdateRequest request) {
        productService.updateStatus(id, request);
        return R.ok(null);
    }
}
