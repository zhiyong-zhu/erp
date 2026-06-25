package com.erp.inventory.service;

import com.erp.common.core.domain.PageVO;
import com.erp.inventory.domain.dto.InventoryIssueCreateRequest;
import com.erp.inventory.domain.dto.InventoryReceiptCreateRequest;
import com.erp.inventory.domain.dto.InventoryCheckCreateRequest;
import com.erp.inventory.domain.dto.InventoryTransferCreateRequest;
import com.erp.inventory.domain.vo.InventoryBalanceVO;
import com.erp.inventory.domain.vo.InventoryCheckVO;
import com.erp.inventory.domain.vo.InventoryIssueVO;
import com.erp.inventory.domain.vo.InventoryReceiptVO;
import com.erp.inventory.domain.vo.InventoryTransactionVO;
import com.erp.inventory.domain.vo.InventoryTransferVO;
import java.io.ByteArrayInputStream;
import java.util.UUID;

public interface InventoryReceiptService {
    UUID createReceipt(InventoryReceiptCreateRequest request);
    boolean receiptExistsByIdempotencyKey(String idempotencyKey);
    InventoryCheckVO createCheck(InventoryCheckCreateRequest request);
    InventoryCheckVO reviewCheck(UUID id, String remark);
    InventoryCheckVO approveCheck(UUID id, String remark);
    InventoryCheckVO rejectCheck(UUID id, String remark);
    InventoryIssueVO createIssue(InventoryIssueCreateRequest request);
    InventoryTransferVO createTransfer(InventoryTransferCreateRequest request);
    PageVO<InventoryCheckVO> listChecks(long pageNum, long pageSize);
    PageVO<InventoryIssueVO> listIssues(long pageNum, long pageSize);
    PageVO<InventoryReceiptVO> listReceipts(long pageNum, long pageSize);
    PageVO<InventoryTransferVO> listTransfers(long pageNum, long pageSize);
    PageVO<InventoryTransactionVO> listTransactions(
            long pageNum,
            long pageSize,
            UUID receiptId,
            UUID issueId,
            UUID transferId,
            UUID checkId,
            UUID sourceOrderId
    );
    PageVO<InventoryBalanceVO> listBalances(
            long pageNum,
            long pageSize,
            String materialName,
            String warehouseCode,
            String locationCode,
            String batchNo
    );
    ByteArrayInputStream exportBalances(String materialName, String warehouseCode, String locationCode, String batchNo);
    ByteArrayInputStream exportTransactions(UUID receiptId, UUID issueId, UUID transferId, UUID checkId, UUID sourceOrderId);
}
