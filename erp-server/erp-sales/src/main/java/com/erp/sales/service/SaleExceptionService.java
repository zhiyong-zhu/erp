package com.erp.sales.service;

import com.erp.common.core.domain.PageVO;
import com.erp.sales.domain.dto.SaleExceptionHandleRequest;
import com.erp.sales.domain.entity.SaleOrder;
import com.erp.sales.domain.entity.SaleOrderItem;
import com.erp.sales.domain.entity.SaleReturn;
import com.erp.sales.domain.entity.SaleReturnItem;
import com.erp.sales.domain.vo.SaleExceptionVO;
import java.util.UUID;

public interface SaleExceptionService {
    void createOrderException(SaleOrder order, SaleOrderItem item, String exceptionType, String description);
    void createReturnException(SaleReturn saleReturn, SaleReturnItem item, String exceptionType, String description);
    PageVO<SaleExceptionVO> list(long pageNum, long pageSize);
    SaleExceptionVO handle(UUID id, SaleExceptionHandleRequest request);
}
