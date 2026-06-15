package com.erp.production.controller;

import com.erp.common.core.domain.PageVO;
import com.erp.common.core.domain.R;
import com.erp.common.core.exception.BizException;
import com.erp.production.domain.dto.ProductionBatchRequest;
import com.erp.production.domain.dto.ProductionBoxRequest;
import com.erp.production.domain.dto.ProductionBomRequest;
import com.erp.production.domain.dto.ProductionProcessRequest;
import com.erp.production.domain.dto.ProductionReportRequest;
import com.erp.production.domain.dto.SerialNumberGenerateRequest;
import com.erp.production.domain.dto.SerialNumberRequest;
import com.erp.production.domain.vo.ProductionBatchVO;
import com.erp.production.domain.vo.ProductionBoxVO;
import com.erp.production.domain.vo.ProductionBomVO;
import com.erp.production.domain.vo.ProductionProductStockVO;
import com.erp.production.domain.vo.ProductionProcessVO;
import com.erp.production.domain.vo.ProductionReportVO;
import com.erp.production.domain.vo.SerialNumberVO;
import com.erp.production.service.ProductionService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/production")
public class ProductionController {
    private final ProductionService productionService;

    public ProductionController(ProductionService productionService) {
        this.productionService = productionService;
    }

    @GetMapping("/processes")
    @PreAuthorize("hasAuthority(T(com.erp.production.permission.ProductionPermissionCodes).PROCESS_LIST)")
    public R<PageVO<ProductionProcessVO>> listProcesses(@RequestParam(defaultValue = "1") long pageNum,
                                                         @RequestParam(defaultValue = "10") long pageSize,
                                                         @RequestParam(required = false) String name,
                                                         @RequestParam(required = false) String productId,
                                                         @RequestParam(required = false) Integer status) {
        return R.ok(productionService.listProcesses(pageNum, pageSize, name, parseUuid(productId), status));
    }

    @PostMapping("/processes")
    @PreAuthorize("hasAuthority(T(com.erp.production.permission.ProductionPermissionCodes).PROCESS_CREATE)")
    public R<ProductionProcessVO> createProcess(@Valid @RequestBody ProductionProcessRequest request) {
        return R.ok(productionService.saveProcess(null, request));
    }

    @PutMapping("/processes/{id}")
    @PreAuthorize("hasAuthority(T(com.erp.production.permission.ProductionPermissionCodes).PROCESS_UPDATE)")
    public R<ProductionProcessVO> updateProcess(@PathVariable UUID id, @Valid @RequestBody ProductionProcessRequest request) {
        return R.ok(productionService.saveProcess(id, request));
    }

    @GetMapping("/boms")
    @PreAuthorize("hasAuthority(T(com.erp.production.permission.ProductionPermissionCodes).BOM_LIST)")
    public R<PageVO<ProductionBomVO>> listBoms(@RequestParam(defaultValue = "1") long pageNum,
                                                @RequestParam(defaultValue = "10") long pageSize,
                                                @RequestParam(required = false) String productId,
                                                @RequestParam(required = false) Integer status) {
        return R.ok(productionService.listBoms(pageNum, pageSize, parseUuid(productId), status));
    }

    @PostMapping("/boms")
    @PreAuthorize("hasAuthority(T(com.erp.production.permission.ProductionPermissionCodes).BOM_CREATE)")
    public R<ProductionBomVO> createBom(@Valid @RequestBody ProductionBomRequest request) {
        return R.ok(productionService.saveBom(null, request));
    }

    @PutMapping("/boms/{id}")
    @PreAuthorize("hasAuthority(T(com.erp.production.permission.ProductionPermissionCodes).BOM_UPDATE)")
    public R<ProductionBomVO> updateBom(@PathVariable UUID id, @Valid @RequestBody ProductionBomRequest request) {
        return R.ok(productionService.saveBom(id, request));
    }

    @GetMapping("/batches")
    @PreAuthorize("hasAuthority(T(com.erp.production.permission.ProductionPermissionCodes).BATCH_LIST)")
    public R<PageVO<ProductionBatchVO>> listBatches(@RequestParam(defaultValue = "1") long pageNum,
                                                     @RequestParam(defaultValue = "10") long pageSize,
                                                     @RequestParam(required = false) String batchNo,
                                                     @RequestParam(required = false) String productId,
                                                     @RequestParam(required = false) String status) {
        return R.ok(productionService.listBatches(pageNum, pageSize, batchNo, parseUuid(productId), status));
    }

    @PostMapping("/batches")
    @PreAuthorize("hasAuthority(T(com.erp.production.permission.ProductionPermissionCodes).BATCH_CREATE)")
    public R<ProductionBatchVO> createBatch(@Valid @RequestBody ProductionBatchRequest request) {
        return R.ok(productionService.saveBatch(null, request));
    }

    @PutMapping("/batches/{id}")
    @PreAuthorize("hasAuthority(T(com.erp.production.permission.ProductionPermissionCodes).BATCH_UPDATE)")
    public R<ProductionBatchVO> updateBatch(@PathVariable UUID id, @Valid @RequestBody ProductionBatchRequest request) {
        return R.ok(productionService.saveBatch(id, request));
    }

    @PostMapping("/batches/{id}/start")
    @PreAuthorize("hasAuthority(T(com.erp.production.permission.ProductionPermissionCodes).REPORT_CREATE)")
    public R<ProductionBatchVO> startBatch(@PathVariable UUID id) {
        return R.ok(productionService.startBatch(id));
    }

    @PostMapping("/batches/{id}/serial-numbers/generate")
    @PreAuthorize("hasAuthority(T(com.erp.production.permission.ProductionPermissionCodes).SERIAL_CREATE)")
    public R<List<SerialNumberVO>> generateSerialNumbers(@PathVariable UUID id,
                                                          @Valid @RequestBody SerialNumberGenerateRequest request) {
        return R.ok(productionService.generateSerialNumbers(id, request));
    }

    @PostMapping("/batches/{id}/receipt")
    @PreAuthorize("hasAuthority(T(com.erp.production.permission.ProductionPermissionCodes).REPORT_CREATE)")
    public R<ProductionBatchVO> receiveBatch(@PathVariable UUID id) {
        return R.ok(productionService.receiveBatch(id));
    }

    @GetMapping("/reports")
    @PreAuthorize("hasAuthority(T(com.erp.production.permission.ProductionPermissionCodes).REPORT_LIST)")
    public R<PageVO<ProductionReportVO>> listReports(@RequestParam(defaultValue = "1") long pageNum,
                                                      @RequestParam(defaultValue = "10") long pageSize,
                                                      @RequestParam(required = false) String batchNo,
                                                      @RequestParam(required = false) String productId,
                                                      @RequestParam(required = false) String status) {
        return R.ok(productionService.listReports(pageNum, pageSize, batchNo, parseUuid(productId), status));
    }

    @PostMapping("/reports")
    @PreAuthorize("hasAuthority(T(com.erp.production.permission.ProductionPermissionCodes).REPORT_CREATE)")
    public R<ProductionReportVO> createReport(@Valid @RequestBody ProductionReportRequest request) {
        return R.ok(productionService.createReport(request));
    }

    @GetMapping("/boxes")
    @PreAuthorize("hasAuthority(T(com.erp.production.permission.ProductionPermissionCodes).REPORT_LIST)")
    public R<PageVO<ProductionBoxVO>> listBoxes(@RequestParam(defaultValue = "1") long pageNum,
                                                 @RequestParam(defaultValue = "10") long pageSize,
                                                 @RequestParam(required = false) String batchNo,
                                                 @RequestParam(required = false) String productId,
                                                 @RequestParam(required = false) String status) {
        return R.ok(productionService.listBoxes(pageNum, pageSize, batchNo, parseUuid(productId), status));
    }

    @PostMapping("/boxes")
    @PreAuthorize("hasAuthority(T(com.erp.production.permission.ProductionPermissionCodes).REPORT_CREATE)")
    public R<ProductionBoxVO> packBox(@Valid @RequestBody ProductionBoxRequest request) {
        return R.ok(productionService.packBox(request));
    }

    @GetMapping("/product-stock")
    @PreAuthorize("hasAuthority(T(com.erp.production.permission.ProductionPermissionCodes).REPORT_LIST)")
    public R<PageVO<ProductionProductStockVO>> listProductStock(@RequestParam(defaultValue = "1") long pageNum,
                                                                 @RequestParam(defaultValue = "10") long pageSize,
                                                                 @RequestParam(required = false) String productName) {
        return R.ok(productionService.listProductStock(pageNum, pageSize, productName));
    }

    @GetMapping("/serial-numbers")
    @PreAuthorize("hasAuthority(T(com.erp.production.permission.ProductionPermissionCodes).SERIAL_LIST)")
    public R<PageVO<SerialNumberVO>> listSerialNumbers(@RequestParam(defaultValue = "1") long pageNum,
                                                        @RequestParam(defaultValue = "10") long pageSize,
                                                        @RequestParam(required = false) String serialNo,
                                                        @RequestParam(required = false) String batchId,
                                                        @RequestParam(required = false) String productId,
                                                        @RequestParam(required = false) String status) {
        return R.ok(productionService.listSerialNumbers(pageNum, pageSize, serialNo, parseUuid(batchId), parseUuid(productId), status));
    }

    @PostMapping("/serial-numbers")
    @PreAuthorize("hasAuthority(T(com.erp.production.permission.ProductionPermissionCodes).SERIAL_CREATE)")
    public R<SerialNumberVO> createSerialNumber(@Valid @RequestBody SerialNumberRequest request) {
        return R.ok(productionService.saveSerialNumber(null, request));
    }

    @PutMapping("/serial-numbers/{id}")
    @PreAuthorize("hasAuthority(T(com.erp.production.permission.ProductionPermissionCodes).SERIAL_UPDATE)")
    public R<SerialNumberVO> updateSerialNumber(@PathVariable UUID id, @Valid @RequestBody SerialNumberRequest request) {
        return R.ok(productionService.saveSerialNumber(id, request));
    }

    private UUID parseUuid(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException ex) {
            throw new BizException(10004, "Invalid UUID");
        }
    }
}
