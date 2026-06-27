package com.erp.purchase.service;

import com.erp.common.core.domain.PageVO;
import com.erp.purchase.domain.dto.PurchaseDraftGenerateRequest;
import com.erp.purchase.domain.dto.PurchaseOrderCreateRequest;
import com.erp.purchase.domain.dto.PurchaseOrderReceiveRequest;
import com.erp.purchase.domain.dto.PurchaseOrderStatusRequest;
import com.erp.purchase.domain.dto.PurchaseOrderUpdateRequest;
import com.erp.purchase.domain.vo.PurchasePayableStatVO;
import com.erp.purchase.domain.vo.PurchaseOrderVO;
import java.util.UUID;

public interface PurchaseOrderService {
    PageVO<PurchaseOrderVO> listOrders(long pageNum, long pageSize);
    PurchaseOrderVO detail(UUID id);
    PurchaseOrderVO createDraft(PurchaseOrderCreateRequest request);
    PageVO<PurchaseOrderVO> generateDraftOrdersFromReplenishment(PurchaseDraftGenerateRequest request);
    PurchaseOrderVO updateDraft(UUID id, PurchaseOrderUpdateRequest request);
    PurchaseOrderVO changeStatus(UUID id, PurchaseOrderStatusRequest request);
    PurchaseOrderVO receiveOrder(UUID id, PurchaseOrderReceiveRequest request);
    PageVO<PurchasePayableStatVO> listPayableStats(long pageNum, long pageSize);
}
