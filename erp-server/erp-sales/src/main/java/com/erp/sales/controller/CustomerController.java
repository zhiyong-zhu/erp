package com.erp.sales.controller;

import com.erp.common.core.domain.PageVO;
import com.erp.common.core.domain.R;
import com.erp.sales.domain.dto.CustomerRequest;
import com.erp.sales.domain.vo.CustomerVO;
import com.erp.sales.permission.SalesPermissionCodes;
import com.erp.sales.service.CustomerService;
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
@RequestMapping("/api/v1/sales/customers")
public class CustomerController {
    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority(T(com.erp.sales.permission.SalesPermissionCodes).CUSTOMER_LIST)")
    public R<PageVO<CustomerVO>> list(@RequestParam(defaultValue = "1") long pageNum,
                                       @RequestParam(defaultValue = "10") long pageSize,
                                       @RequestParam(required = false) String name) {
        return R.ok(customerService.list(pageNum, pageSize, name));
    }

    @PostMapping
    @PreAuthorize("hasAuthority(T(com.erp.sales.permission.SalesPermissionCodes).CUSTOMER_CREATE)")
    public R<CustomerVO> create(@Valid @RequestBody CustomerRequest request) {
        return R.ok(customerService.save(null, request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority(T(com.erp.sales.permission.SalesPermissionCodes).CUSTOMER_UPDATE)")
    public R<CustomerVO> update(@PathVariable UUID id, @Valid @RequestBody CustomerRequest request) {
        return R.ok(customerService.save(id, request));
    }
}
