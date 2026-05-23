package com.erp.product.controller;

import com.erp.common.core.domain.R;
import com.erp.product.domain.dto.LabelPrintRequest;
import com.erp.product.domain.vo.LabelPrintPreviewVO;
import com.erp.product.permission.ProductPermissionCodes;
import com.erp.product.service.LabelPrintService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/product/labels")
public class LabelPrintController {
    private final LabelPrintService labelPrintService;

    public LabelPrintController(LabelPrintService labelPrintService) {
        this.labelPrintService = labelPrintService;
    }

    @PostMapping("/preview")
    @PreAuthorize("hasAuthority(T(com.erp.product.permission.ProductPermissionCodes).LABEL_PRINT)")
    public R<LabelPrintPreviewVO> preview(@Valid @RequestBody LabelPrintRequest request) {
        return R.ok(labelPrintService.preview(request));
    }

    @PostMapping("/print")
    @PreAuthorize("hasAuthority(T(com.erp.product.permission.ProductPermissionCodes).LABEL_PRINT)")
    public R<LabelPrintPreviewVO> print(@Valid @RequestBody LabelPrintRequest request) {
        return R.ok(labelPrintService.preview(request));
    }
}
