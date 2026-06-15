package com.erp.sales.service;

import com.erp.common.core.domain.PageVO;
import com.erp.sales.domain.dto.SaleReturnRequest;
import com.erp.sales.domain.vo.SaleReturnVO;
import java.util.UUID;

public interface SaleReturnService {
    PageVO<SaleReturnVO> listReturns(long pageNum, long pageSize);
    SaleReturnVO detail(UUID id);
    SaleReturnVO create(SaleReturnRequest request);
    SaleReturnVO changeStatus(UUID id, String action, String remark);
}
