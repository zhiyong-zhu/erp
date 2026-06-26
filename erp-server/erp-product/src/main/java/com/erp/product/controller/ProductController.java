package com.erp.product.controller;

import com.erp.common.core.domain.R;
import com.erp.common.core.domain.PageVO;
import com.erp.product.domain.dto.ProductCreateRequest;
import com.erp.product.domain.dto.ProductStatusFlowRequest;
import com.erp.product.domain.dto.ProductStatusUpdateRequest;
import com.erp.product.domain.dto.ProductUpdateRequest;
import com.erp.product.domain.vo.FileUploadVO;
import com.erp.product.domain.vo.ProductVO;
import com.erp.product.permission.ProductPermissionCodes;
import com.erp.product.service.ProductService;
import java.io.IOException;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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
                                     @RequestParam(required = false) Integer status,
                                     @RequestParam(required = false) Boolean isSemifinished) {
        return R.ok(productService.list(pageNum, pageSize, name, categoryId, status, isSemifinished));
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

    @PostMapping("/{id}/flow")
    @PreAuthorize("hasAuthority(T(com.erp.product.permission.ProductPermissionCodes).PRODUCT_UPDATE)")
    public R<ProductVO> changeFlow(@PathVariable UUID id, @Valid @RequestBody ProductStatusFlowRequest request) {
        return R.ok(productService.changeStatusFlow(id, request));
    }

    @PostMapping(value = "/upload-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority(T(com.erp.product.permission.ProductPermissionCodes).PRODUCT_CREATE)")
    public R<FileUploadVO> uploadImage(@RequestPart("file") MultipartFile file) {
        return R.ok(productService.uploadImage(file));
    }

    @GetMapping("/export")
    @PreAuthorize("hasAuthority(T(com.erp.product.permission.ProductPermissionCodes).PRODUCT_DETAIL)")
    public ResponseEntity<InputStreamResource> exportProducts() throws IOException {
        InputStreamResource resource = new InputStreamResource(productService.exportProducts());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=products.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority(T(com.erp.product.permission.ProductPermissionCodes).PRODUCT_CREATE)")
    public R<Void> importProducts(@RequestPart("file") MultipartFile file) {
        productService.importProducts(file);
        return R.ok(null);
    }
}
