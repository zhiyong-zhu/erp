package com.erp.sales.service;

import com.erp.sales.domain.dto.CustomerAddressRequest;
import com.erp.sales.domain.vo.CustomerAddressVO;
import java.util.List;
import java.util.UUID;

public interface CustomerAddressService {
    List<CustomerAddressVO> listByCustomer(UUID customerId);
    CustomerAddressVO save(UUID id, CustomerAddressRequest request);
    void delete(UUID id);
    void setDefault(UUID id);
}
