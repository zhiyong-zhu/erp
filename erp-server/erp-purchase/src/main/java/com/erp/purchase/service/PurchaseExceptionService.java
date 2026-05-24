package com.erp.purchase.service;

import com.erp.common.core.domain.PageVO;
import com.erp.purchase.domain.dto.PurchaseExceptionHandleRequest;
import com.erp.purchase.domain.entity.PurchaseOrder;
import com.erp.purchase.domain.entity.PurchaseOrderItem;
import com.erp.purchase.domain.vo.PurchaseExceptionVO;
import java.util.UUID;

public interface PurchaseExceptionService {
    void createInspectionException(PurchaseOrder order, PurchaseOrderItem item, String description);
    PageVO<PurchaseExceptionVO> list(long pageNum, long pageSize);
    PurchaseExceptionVO handle(UUID id, PurchaseExceptionHandleRequest request);
}
