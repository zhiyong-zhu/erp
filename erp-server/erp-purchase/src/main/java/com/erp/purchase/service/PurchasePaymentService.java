package com.erp.purchase.service;

import com.erp.common.core.domain.PageVO;
import com.erp.purchase.domain.dto.PurchasePaymentRequest;
import com.erp.purchase.domain.vo.PurchasePaymentVO;
import java.util.UUID;

public interface PurchasePaymentService {
    PurchasePaymentVO createPayment(UUID purchaseOrderId, PurchasePaymentRequest request);
    PageVO<PurchasePaymentVO> listPayments(long pageNum, long pageSize, UUID purchaseOrderId, UUID supplierId);
}
