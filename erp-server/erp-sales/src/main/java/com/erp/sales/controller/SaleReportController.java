package com.erp.sales.controller;

import com.erp.common.core.domain.R;
import com.erp.sales.domain.vo.SaleReportVO;
import com.erp.sales.service.SaleReportService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sales/reports")
public class SaleReportController {
    private final SaleReportService saleReportService;

    public SaleReportController(SaleReportService saleReportService) {
        this.saleReportService = saleReportService;
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAuthority(T(com.erp.sales.permission.SalesPermissionCodes).REPORT_LIST)")
    public R<SaleReportVO> summary() {
        return R.ok(saleReportService.summary());
    }
}
