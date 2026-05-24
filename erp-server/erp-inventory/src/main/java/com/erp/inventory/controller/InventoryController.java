package com.erp.inventory.controller;

import com.erp.common.core.domain.PageVO;
import com.erp.common.core.domain.R;
import com.erp.inventory.domain.vo.InventoryReceiptVO;
import com.erp.inventory.domain.vo.InventoryTransactionVO;
import com.erp.inventory.service.InventoryReceiptService;
import org.springframework.web.bind.annotation.GetMapping;
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

    @GetMapping("/transactions")
    public R<PageVO<InventoryTransactionVO>> listTransactions(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize
    ) {
        return R.ok(inventoryReceiptService.listTransactions(pageNum, pageSize));
    }
}
