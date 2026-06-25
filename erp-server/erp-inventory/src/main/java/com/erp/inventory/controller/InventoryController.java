package com.erp.inventory.controller;

import com.erp.common.core.domain.PageVO;
import com.erp.common.core.domain.R;
import com.erp.inventory.domain.dto.InventoryCheckActionRequest;
import com.erp.inventory.domain.dto.InventoryCheckCreateRequest;
import com.erp.inventory.domain.dto.InventoryIssueCreateRequest;
import com.erp.inventory.domain.dto.InventoryLocationRequest;
import com.erp.inventory.domain.dto.InventoryReceiptCreateRequest;
import com.erp.inventory.domain.dto.InventoryTransferCreateRequest;
import com.erp.inventory.domain.dto.InventoryWarehouseRequest;
import com.erp.inventory.domain.vo.InventoryBalanceVO;
import com.erp.inventory.domain.vo.InventoryCheckVO;
import com.erp.inventory.domain.vo.InventoryIssueVO;
import com.erp.inventory.domain.vo.InventoryLocationVO;
import com.erp.inventory.domain.vo.InventoryReceiptVO;
import com.erp.inventory.domain.vo.InventoryTransactionVO;
import com.erp.inventory.domain.vo.InventoryTransferVO;
import com.erp.inventory.domain.vo.InventoryWarehouseVO;
import com.erp.inventory.service.InventoryReceiptService;
import com.erp.inventory.service.InventoryWarehouseService;
import java.io.IOException;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryController {
    private final InventoryReceiptService inventoryReceiptService;
    private final InventoryWarehouseService inventoryWarehouseService;

    public InventoryController(InventoryReceiptService inventoryReceiptService, InventoryWarehouseService inventoryWarehouseService) {
        this.inventoryReceiptService = inventoryReceiptService;
        this.inventoryWarehouseService = inventoryWarehouseService;
    }

    @GetMapping("/warehouses")
    @PreAuthorize("hasAuthority(T(com.erp.inventory.permission.InventoryPermissionCodes).WAREHOUSE_LIST)")
    public R<PageVO<InventoryWarehouseVO>> listWarehouses(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status
    ) {
        return R.ok(inventoryWarehouseService.listWarehouses(pageNum, pageSize, keyword, status));
    }

    @PostMapping("/warehouses")
    @PreAuthorize("hasAuthority(T(com.erp.inventory.permission.InventoryPermissionCodes).WAREHOUSE_CREATE)")
    public R<InventoryWarehouseVO> createWarehouse(@RequestBody InventoryWarehouseRequest request) {
        return R.ok(inventoryWarehouseService.saveWarehouse(null, request));
    }

    @PutMapping("/warehouses/{id}")
    @PreAuthorize("hasAuthority(T(com.erp.inventory.permission.InventoryPermissionCodes).WAREHOUSE_UPDATE)")
    public R<InventoryWarehouseVO> updateWarehouse(@PathVariable UUID id, @RequestBody InventoryWarehouseRequest request) {
        return R.ok(inventoryWarehouseService.saveWarehouse(id, request));
    }

    @GetMapping("/locations")
    @PreAuthorize("hasAuthority(T(com.erp.inventory.permission.InventoryPermissionCodes).LOCATION_LIST)")
    public R<PageVO<InventoryLocationVO>> listLocations(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) UUID warehouseId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status
    ) {
        return R.ok(inventoryWarehouseService.listLocations(pageNum, pageSize, warehouseId, keyword, status));
    }

    @PostMapping("/locations")
    @PreAuthorize("hasAuthority(T(com.erp.inventory.permission.InventoryPermissionCodes).LOCATION_CREATE)")
    public R<InventoryLocationVO> createLocation(@RequestBody InventoryLocationRequest request) {
        return R.ok(inventoryWarehouseService.saveLocation(null, request));
    }

    @PutMapping("/locations/{id}")
    @PreAuthorize("hasAuthority(T(com.erp.inventory.permission.InventoryPermissionCodes).LOCATION_UPDATE)")
    public R<InventoryLocationVO> updateLocation(@PathVariable UUID id, @RequestBody InventoryLocationRequest request) {
        return R.ok(inventoryWarehouseService.saveLocation(id, request));
    }

    @GetMapping("/receipts")
    @PreAuthorize("hasAuthority(T(com.erp.inventory.permission.InventoryPermissionCodes).RECEIPT_LIST)")
    public R<PageVO<InventoryReceiptVO>> listReceipts(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize
    ) {
        return R.ok(inventoryReceiptService.listReceipts(pageNum, pageSize));
    }

    @PostMapping("/receipts")
    @PreAuthorize("hasAuthority(T(com.erp.inventory.permission.InventoryPermissionCodes).RECEIPT_LIST)")
    public R<UUID> createReceipt(@RequestBody InventoryReceiptCreateRequest request) {
        return R.ok(inventoryReceiptService.createReceipt(request));
    }

    @GetMapping("/issues")
    @PreAuthorize("hasAuthority(T(com.erp.inventory.permission.InventoryPermissionCodes).ISSUE_LIST)")
    public R<PageVO<InventoryIssueVO>> listIssues(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize
    ) {
        return R.ok(inventoryReceiptService.listIssues(pageNum, pageSize));
    }

    @PostMapping("/issues")
    @PreAuthorize("hasAuthority(T(com.erp.inventory.permission.InventoryPermissionCodes).ISSUE_CREATE)")
    public R<InventoryIssueVO> createIssue(@RequestBody InventoryIssueCreateRequest request) {
        return R.ok(inventoryReceiptService.createIssue(request));
    }

    @GetMapping("/checks")
    @PreAuthorize("hasAuthority(T(com.erp.inventory.permission.InventoryPermissionCodes).CHECK_LIST)")
    public R<PageVO<InventoryCheckVO>> listChecks(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize
    ) {
        return R.ok(inventoryReceiptService.listChecks(pageNum, pageSize));
    }

    @PostMapping("/checks")
    @PreAuthorize("hasAuthority(T(com.erp.inventory.permission.InventoryPermissionCodes).CHECK_CREATE)")
    public R<InventoryCheckVO> createCheck(@RequestBody InventoryCheckCreateRequest request) {
        return R.ok(inventoryReceiptService.createCheck(request));
    }

    @PostMapping("/checks/{id}/review")
    @PreAuthorize("hasAuthority(T(com.erp.inventory.permission.InventoryPermissionCodes).CHECK_REVIEW)")
    public R<InventoryCheckVO> reviewCheck(@PathVariable UUID id, @RequestBody(required = false) InventoryCheckActionRequest request) {
        return R.ok(inventoryReceiptService.reviewCheck(id, request == null ? null : request.getRemark()));
    }

    @PostMapping("/checks/{id}/approve")
    @PreAuthorize("hasAuthority(T(com.erp.inventory.permission.InventoryPermissionCodes).CHECK_APPROVE)")
    public R<InventoryCheckVO> approveCheck(@PathVariable UUID id, @RequestBody(required = false) InventoryCheckActionRequest request) {
        return R.ok(inventoryReceiptService.approveCheck(id, request == null ? null : request.getRemark()));
    }

    @PostMapping("/checks/{id}/reject")
    @PreAuthorize("hasAuthority(T(com.erp.inventory.permission.InventoryPermissionCodes).CHECK_REJECT)")
    public R<InventoryCheckVO> rejectCheck(@PathVariable UUID id, @RequestBody(required = false) InventoryCheckActionRequest request) {
        return R.ok(inventoryReceiptService.rejectCheck(id, request == null ? null : request.getRemark()));
    }

    @GetMapping("/transfers")
    @PreAuthorize("hasAuthority(T(com.erp.inventory.permission.InventoryPermissionCodes).TRANSFER_LIST)")
    public R<PageVO<InventoryTransferVO>> listTransfers(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize
    ) {
        return R.ok(inventoryReceiptService.listTransfers(pageNum, pageSize));
    }

    @PostMapping("/transfers")
    @PreAuthorize("hasAuthority(T(com.erp.inventory.permission.InventoryPermissionCodes).TRANSFER_CREATE)")
    public R<InventoryTransferVO> createTransfer(@RequestBody InventoryTransferCreateRequest request) {
        return R.ok(inventoryReceiptService.createTransfer(request));
    }

    @GetMapping("/transactions")
    @PreAuthorize("hasAuthority(T(com.erp.inventory.permission.InventoryPermissionCodes).TRANSACTION_LIST)")
    public R<PageVO<InventoryTransactionVO>> listTransactions(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) UUID receiptId,
            @RequestParam(required = false) UUID issueId,
            @RequestParam(required = false) UUID transferId,
            @RequestParam(required = false) UUID checkId,
            @RequestParam(required = false) UUID sourceOrderId
    ) {
        return R.ok(inventoryReceiptService.listTransactions(pageNum, pageSize, receiptId, issueId, transferId, checkId, sourceOrderId));
    }

    @GetMapping("/transactions/export")
    @PreAuthorize("hasAuthority(T(com.erp.inventory.permission.InventoryPermissionCodes).TRANSACTION_LIST)")
    public ResponseEntity<InputStreamResource> exportTransactions(
            @RequestParam(required = false) UUID receiptId,
            @RequestParam(required = false) UUID issueId,
            @RequestParam(required = false) UUID transferId,
            @RequestParam(required = false) UUID checkId,
            @RequestParam(required = false) UUID sourceOrderId
    ) throws IOException {
        InputStreamResource resource = new InputStreamResource(inventoryReceiptService.exportTransactions(receiptId, issueId, transferId, checkId, sourceOrderId));
        return excelResponse(resource, "inventory-transactions.xlsx");
    }

    @GetMapping("/balances")
    @PreAuthorize("hasAuthority(T(com.erp.inventory.permission.InventoryPermissionCodes).TRANSACTION_LIST)")
    public R<PageVO<InventoryBalanceVO>> listBalances(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) String materialName,
            @RequestParam(required = false) String warehouseCode,
            @RequestParam(required = false) String locationCode,
            @RequestParam(required = false) String batchNo
    ) {
        return R.ok(inventoryReceiptService.listBalances(pageNum, pageSize, materialName, warehouseCode, locationCode, batchNo));
    }

    @GetMapping("/balances/export")
    @PreAuthorize("hasAuthority(T(com.erp.inventory.permission.InventoryPermissionCodes).TRANSACTION_LIST)")
    public ResponseEntity<InputStreamResource> exportBalances(
            @RequestParam(required = false) String materialName,
            @RequestParam(required = false) String warehouseCode,
            @RequestParam(required = false) String locationCode,
            @RequestParam(required = false) String batchNo
    ) throws IOException {
        InputStreamResource resource = new InputStreamResource(inventoryReceiptService.exportBalances(materialName, warehouseCode, locationCode, batchNo));
        return excelResponse(resource, "inventory-balances.xlsx");
    }

    private ResponseEntity<InputStreamResource> excelResponse(InputStreamResource resource, String filename) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}
