package com.erp.purchase.controller;

import com.erp.common.core.domain.PageVO;
import com.erp.common.core.domain.R;
import com.erp.purchase.domain.dto.PurchaseExceptionHandleRequest;
import com.erp.purchase.domain.vo.PurchaseExceptionVO;
import com.erp.purchase.service.PurchaseExceptionService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/purchase/exceptions")
public class PurchaseExceptionController {
    private final PurchaseExceptionService purchaseExceptionService;

    public PurchaseExceptionController(PurchaseExceptionService purchaseExceptionService) {
        this.purchaseExceptionService = purchaseExceptionService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority(T(com.erp.purchase.permission.PurchasePermissionCodes).EXCEPTION_LIST)")
    public R<PageVO<PurchaseExceptionVO>> list(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize
    ) {
        return R.ok(purchaseExceptionService.list(pageNum, pageSize));
    }

    @PostMapping("/{id}/handle")
    @PreAuthorize("hasAuthority(T(com.erp.purchase.permission.PurchasePermissionCodes).EXCEPTION_UPDATE)")
    public R<PurchaseExceptionVO> handle(
            @PathVariable UUID id,
            @Valid @RequestBody PurchaseExceptionHandleRequest request
    ) {
        return R.ok(purchaseExceptionService.handle(id, request));
    }
}
