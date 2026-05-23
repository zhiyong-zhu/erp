package com.erp.product.controller;

import com.erp.common.core.domain.R;
import com.erp.product.domain.dto.LabelTemplateRequest;
import com.erp.product.domain.vo.LabelTemplateVO;
import com.erp.product.permission.ProductPermissionCodes;
import com.erp.product.service.LabelTemplateService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/product/label-templates")
public class LabelTemplateController {
    private final LabelTemplateService labelTemplateService;

    public LabelTemplateController(LabelTemplateService labelTemplateService) {
        this.labelTemplateService = labelTemplateService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority(T(com.erp.product.permission.ProductPermissionCodes).LABEL_LIST)")
    public R<List<LabelTemplateVO>> list() {
        return R.ok(labelTemplateService.list());
    }

    @PostMapping
    @PreAuthorize("hasAuthority(T(com.erp.product.permission.ProductPermissionCodes).LABEL_UPDATE)")
    public R<LabelTemplateVO> save(@Valid @RequestBody LabelTemplateRequest request) {
        return R.ok(labelTemplateService.save(request));
    }
}
