package com.erp.inventory.service;

import com.erp.common.core.domain.PageVO;
import com.erp.inventory.domain.dto.InventoryIssueCreateRequest;
import com.erp.inventory.domain.dto.InventoryReceiptCreateRequest;
import com.erp.inventory.domain.dto.InventoryCheckCreateRequest;
import com.erp.inventory.domain.dto.InventoryTransferCreateRequest;
import com.erp.inventory.domain.vo.InventoryCheckVO;
import com.erp.inventory.domain.vo.InventoryIssueVO;
import com.erp.inventory.domain.vo.InventoryReceiptVO;
import com.erp.inventory.domain.vo.InventoryTransactionVO;
import com.erp.inventory.domain.vo.InventoryTransferVO;
import java.util.UUID;

public interface InventoryReceiptService {
    UUID createReceipt(InventoryReceiptCreateRequest request);
    InventoryCheckVO createCheck(InventoryCheckCreateRequest request);
    InventoryIssueVO createIssue(InventoryIssueCreateRequest request);
    InventoryTransferVO createTransfer(InventoryTransferCreateRequest request);
    PageVO<InventoryCheckVO> listChecks(long pageNum, long pageSize);
    PageVO<InventoryIssueVO> listIssues(long pageNum, long pageSize);
    PageVO<InventoryReceiptVO> listReceipts(long pageNum, long pageSize);
    PageVO<InventoryTransferVO> listTransfers(long pageNum, long pageSize);
    PageVO<InventoryTransactionVO> listTransactions(long pageNum, long pageSize);
}
