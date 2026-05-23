package com.erp.material.service;

import com.erp.common.core.domain.PageVO;
import com.erp.material.domain.dto.SupplierRequest;
import com.erp.material.domain.vo.SupplierVO;
import java.util.UUID;

public interface SupplierService {
    PageVO<SupplierVO> list(long pageNum, long pageSize, String name);
    SupplierVO save(UUID id, SupplierRequest request);
}
