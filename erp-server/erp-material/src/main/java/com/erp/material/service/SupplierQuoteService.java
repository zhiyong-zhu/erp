package com.erp.material.service;

import com.erp.common.core.domain.PageVO;
import com.erp.material.domain.dto.SupplierQuoteRequest;
import com.erp.material.domain.vo.SupplierQuoteVO;
import java.util.UUID;

public interface SupplierQuoteService {
    PageVO<SupplierQuoteVO> list(long pageNum, long pageSize, UUID supplierId, UUID materialId);
    SupplierQuoteVO save(UUID id, SupplierQuoteRequest request);
}
