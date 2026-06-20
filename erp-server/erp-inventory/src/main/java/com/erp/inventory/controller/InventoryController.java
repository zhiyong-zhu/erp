package com.erp.inventory.controller;

import com.erp.common.core.domain.PageVO;
import com.erp.common.core.domain.R;
import com.erp.inventory.domain.dto.InventoryCheckCreateRequest;
import com.erp.inventory.domain.dto.InventoryIssueCreateRequest;
import com.erp.inventory.domain.dto.InventoryTransferCreateRequest;
import com.erp.inventory.domain.vo.InventoryCheckVO;
import com.erp.inventory.domain.vo.InventoryIssueVO;
import com.erp.inventory.domain.vo.InventoryReceiptVO;
import com.erp.inventory.domain.vo.InventoryTransactionVO;
import com.erp.inventory.domain.vo.InventoryTransferVO;
import com.erp.inventory.service.InventoryReceiptService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryController {
    private final InventoryReceiptService inventoryReceiptService;

    public InventoryController(InventoryReceiptService inventoryReceiptService) {
        this.inventoryReceiptService = inventoryReceiptService;
    }

    @GetMapping("/receipts")
    public R<PageVO<InventoryReceiptVO>> listReceipts(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize
    ) {
        return R.ok(inventoryReceiptService.listReceipts(pageNum, pageSize));
    }

    @GetMapping("/issues")
    public R<PageVO<InventoryIssueVO>> listIssues(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize
    ) {
        return R.ok(inventoryReceiptService.listIssues(pageNum, pageSize));
    }

    @PostMapping("/issues")
    public R<InventoryIssueVO> createIssue(@RequestBody InventoryIssueCreateRequest request) {
        return R.ok(inventoryReceiptService.createIssue(request));
    }

    @GetMapping("/checks")
    public R<PageVO<InventoryCheckVO>> listChecks(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize
    ) {
        return R.ok(inventoryReceiptService.listChecks(pageNum, pageSize));
    }

    @PostMapping("/checks")
    public R<InventoryCheckVO> createCheck(@RequestBody InventoryCheckCreateRequest request) {
        return R.ok(inventoryReceiptService.createCheck(request));
    }

    @GetMapping("/transfers")
    public R<PageVO<InventoryTransferVO>> listTransfers(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize
    ) {
        return R.ok(inventoryReceiptService.listTransfers(pageNum, pageSize));
    }

    @PostMapping("/transfers")
    public R<InventoryTransferVO> createTransfer(@RequestBody InventoryTransferCreateRequest request) {
        return R.ok(inventoryReceiptService.createTransfer(request));
    }

    @GetMapping("/transactions")
    public R<PageVO<InventoryTransactionVO>> listTransactions(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize
    ) {
        return R.ok(inventoryReceiptService.listTransactions(pageNum, pageSize));
    }
}
