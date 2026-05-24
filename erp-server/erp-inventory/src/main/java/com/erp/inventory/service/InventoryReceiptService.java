package com.erp.inventory.service;

import com.erp.common.core.domain.PageVO;
import com.erp.inventory.domain.dto.InventoryReceiptCreateRequest;
import com.erp.inventory.domain.vo.InventoryReceiptVO;
import com.erp.inventory.domain.vo.InventoryTransactionVO;
import java.util.UUID;

public interface InventoryReceiptService {
    UUID createReceipt(InventoryReceiptCreateRequest request);
    PageVO<InventoryReceiptVO> listReceipts(long pageNum, long pageSize);
    PageVO<InventoryTransactionVO> listTransactions(long pageNum, long pageSize);
}
