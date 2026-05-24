package com.erp.purchase.service;

import com.erp.common.core.domain.PageVO;
import com.erp.purchase.domain.dto.PurchaseReturnRequest;
import com.erp.purchase.domain.vo.PurchaseReturnVO;
import java.util.UUID;

public interface PurchaseReturnService {
    PageVO<PurchaseReturnVO> list(long pageNum, long pageSize);
    PurchaseReturnVO create(UUID purchaseOrderId, PurchaseReturnRequest request);
}
