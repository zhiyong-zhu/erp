package com.erp.sales.service;

import com.erp.common.core.domain.PageVO;
import com.erp.sales.domain.dto.CustomerRequest;
import com.erp.sales.domain.vo.CustomerVO;
import java.util.UUID;

public interface CustomerService {
    PageVO<CustomerVO> list(long pageNum, long pageSize, String name);
    CustomerVO save(UUID id, CustomerRequest request);
}
