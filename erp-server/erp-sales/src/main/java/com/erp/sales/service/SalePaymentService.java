package com.erp.sales.service;

import com.erp.common.core.domain.PageVO;
import com.erp.sales.domain.dto.SalePaymentRequest;
import com.erp.sales.domain.vo.SalePaymentVO;
import java.util.UUID;

public interface SalePaymentService {
    SalePaymentVO createPayment(UUID saleOrderId, SalePaymentRequest request);
    PageVO<SalePaymentVO> listPayments(long pageNum, long pageSize, UUID saleOrderId, UUID customerId);
}
