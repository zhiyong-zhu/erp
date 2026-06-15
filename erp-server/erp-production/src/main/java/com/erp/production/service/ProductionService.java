package com.erp.production.service;

import com.erp.common.core.domain.PageVO;
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
import java.util.List;
import java.util.UUID;

public interface ProductionService {
    PageVO<ProductionProcessVO> listProcesses(long pageNum, long pageSize, String name, UUID productId, Integer status);
    ProductionProcessVO saveProcess(UUID id, ProductionProcessRequest request);
    PageVO<ProductionBomVO> listBoms(long pageNum, long pageSize, UUID productId, Integer status);
    ProductionBomVO saveBom(UUID id, ProductionBomRequest request);
    PageVO<ProductionBatchVO> listBatches(long pageNum, long pageSize, String batchNo, UUID productId, String status);
    ProductionBatchVO saveBatch(UUID id, ProductionBatchRequest request);
    ProductionBatchVO startBatch(UUID id);
    ProductionBatchVO receiveBatch(UUID id);
    List<SerialNumberVO> generateSerialNumbers(UUID batchId, SerialNumberGenerateRequest request);
    PageVO<ProductionReportVO> listReports(long pageNum, long pageSize, String batchNo, UUID productId, String status);
    ProductionReportVO createReport(ProductionReportRequest request);
    PageVO<ProductionBoxVO> listBoxes(long pageNum, long pageSize, String batchNo, UUID productId, String status);
    ProductionBoxVO packBox(ProductionBoxRequest request);
    PageVO<ProductionProductStockVO> listProductStock(long pageNum, long pageSize, String productName);
    PageVO<SerialNumberVO> listSerialNumbers(long pageNum, long pageSize, String serialNo, UUID batchId, UUID productId, String status);
    SerialNumberVO saveSerialNumber(UUID id, SerialNumberRequest request);
}
