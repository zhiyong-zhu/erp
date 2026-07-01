package com.erp.sales.controller;

import com.erp.common.core.domain.R;
import com.erp.sales.domain.dto.CustomerAddressRequest;
import com.erp.sales.domain.vo.CustomerAddressVO;
import com.erp.sales.permission.SalesPermissionCodes;
import com.erp.sales.service.CustomerAddressService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sales/customers/{customerId}/addresses")
public class CustomerAddressController {
    private final CustomerAddressService customerAddressService;

    public CustomerAddressController(CustomerAddressService customerAddressService) {
        this.customerAddressService = customerAddressService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority(T(com.erp.sales.permission.SalesPermissionCodes).CUSTOMER_LIST)")
    public R<List<CustomerAddressVO>> list(@PathVariable UUID customerId) {
        return R.ok(customerAddressService.listByCustomer(customerId));
    }

    @PostMapping
    @PreAuthorize("hasAuthority(T(com.erp.sales.permission.SalesPermissionCodes).CUSTOMER_UPDATE)")
    public R<CustomerAddressVO> create(@PathVariable UUID customerId,
                                       @Valid @RequestBody CustomerAddressRequest request) {
        request.setCustomerId(customerId);
        return R.ok(customerAddressService.save(null, request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority(T(com.erp.sales.permission.SalesPermissionCodes).CUSTOMER_UPDATE)")
    public R<CustomerAddressVO> update(@PathVariable UUID customerId,
                                       @PathVariable UUID id,
                                       @Valid @RequestBody CustomerAddressRequest request) {
        request.setCustomerId(customerId);
        return R.ok(customerAddressService.save(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority(T(com.erp.sales.permission.SalesPermissionCodes).CUSTOMER_UPDATE)")
    public R<Void> delete(@PathVariable UUID id) {
        customerAddressService.delete(id);
        return R.ok(null);
    }

    @PostMapping("/{id}/default")
    @PreAuthorize("hasAuthority(T(com.erp.sales.permission.SalesPermissionCodes).CUSTOMER_UPDATE)")
    public R<Void> setDefault(@PathVariable UUID id) {
        customerAddressService.setDefault(id);
        return R.ok(null);
    }
}
