package com.erp.sales.service;

import com.erp.common.core.domain.PageVO;
import com.erp.sales.domain.dto.SaleOrderCreateRequest;
import com.erp.sales.domain.dto.SaleOrderStatusRequest;
import com.erp.sales.domain.dto.ShippingOrderRequest;
import com.erp.sales.domain.vo.SaleOrderVO;
import com.erp.sales.domain.vo.SaleReceivableStatVO;
import com.erp.sales.domain.vo.ShippingVO;
import java.util.UUID;

public interface SaleOrderService {
    PageVO<SaleOrderVO> listOrders(long pageNum, long pageSize, String status, String customerName);
    SaleOrderVO detail(UUID id);
    SaleOrderVO create(SaleOrderCreateRequest request);
    SaleOrderVO updateDraft(UUID id, SaleOrderCreateRequest request);
    SaleOrderVO changeStatus(UUID id, SaleOrderStatusRequest request);
    SaleOrderVO ship(UUID id, ShippingOrderRequest request);
    PageVO<ShippingVO> listShipping(long pageNum, long pageSize);
    ShippingVO shippingDetail(UUID id);
    PageVO<SaleReceivableStatVO> listReceivableStats(long pageNum, long pageSize);
}
