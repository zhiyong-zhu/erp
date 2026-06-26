package com.erp.production.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.common.core.domain.PageVO;
import com.erp.common.core.exception.BizException;
import com.erp.common.report.excel.ExcelExportUtils;
import com.erp.common.security.util.SecurityUtils;
import com.erp.inventory.domain.entity.InventoryTransaction;
import com.erp.inventory.mapper.InventoryTransactionMapper;
import com.erp.material.domain.entity.Material;
import com.erp.material.mapper.MaterialMapper;
import com.erp.product.domain.entity.Product;
import com.erp.product.domain.entity.ProductPackage;
import com.erp.product.mapper.ProductMapper;
import com.erp.product.mapper.ProductPackageMapper;
import com.erp.production.domain.ProductionBatchStatusMachine;
import com.erp.production.domain.dto.ProductionBatchRequest;
import com.erp.production.domain.dto.ProductionBoxRequest;
import com.erp.production.domain.dto.ProductionBomItemRequest;
import com.erp.production.domain.dto.ProductionBomRequest;
import com.erp.production.domain.dto.ProductionProcessRequest;
import com.erp.production.domain.dto.ProductionProcessStepRequest;
import com.erp.production.domain.dto.ProductionReportRequest;
import com.erp.production.domain.dto.SerialNumberGenerateRequest;
import com.erp.production.domain.dto.SerialNumberRequest;
import com.erp.production.domain.entity.ProductionBatch;
import com.erp.production.domain.entity.ProductionBox;
import com.erp.production.domain.entity.ProductionBom;
import com.erp.production.domain.entity.ProductionBomItem;
import com.erp.production.domain.entity.ProductionProductStock;
import com.erp.production.domain.entity.ProductionProcess;
import com.erp.production.domain.entity.ProductionProcessStep;
import com.erp.production.domain.entity.ProductionReport;
import com.erp.production.domain.entity.SerialNumber;
import com.erp.production.domain.vo.ProductionBatchVO;
import com.erp.production.domain.vo.ProductionBoxVO;
import com.erp.production.domain.vo.ProductionBomItemVO;
import com.erp.production.domain.vo.ProductionBomVO;
import com.erp.production.domain.vo.ProductionProductStockVO;
import com.erp.production.domain.vo.ProductionProcessStepVO;
import com.erp.production.domain.vo.ProductionProcessVO;
import com.erp.production.domain.vo.ProductionReportVO;
import com.erp.production.domain.vo.SerialNumberVO;
import com.erp.production.mapper.ProductionBatchMapper;
import com.erp.production.mapper.ProductionBoxMapper;
import com.erp.production.mapper.ProductionBomItemMapper;
import com.erp.production.mapper.ProductionBomMapper;
import com.erp.production.mapper.ProductionProductStockMapper;
import com.erp.production.mapper.ProductionProcessMapper;
import com.erp.production.mapper.ProductionProcessStepMapper;
import com.erp.production.mapper.ProductionReportMapper;
import com.erp.production.mapper.SerialNumberMapper;
import com.erp.production.service.ProductionSerialNumberService;
import com.erp.production.service.ProductionService;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductionServiceImpl implements ProductionService {
    private static final DateTimeFormatter SPACE_DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ProductMapper productMapper;
    private final ProductPackageMapper productPackageMapper;
    private final MaterialMapper materialMapper;
    private final ProductionProcessMapper processMapper;
    private final ProductionProcessStepMapper processStepMapper;
    private final ProductionBomMapper bomMapper;
    private final ProductionBomItemMapper bomItemMapper;
    private final ProductionBatchMapper batchMapper;
    private final ProductionBoxMapper boxMapper;
    private final ProductionProductStockMapper productStockMapper;
    private final ProductionReportMapper reportMapper;
    private final SerialNumberMapper serialNumberMapper;
    private final InventoryTransactionMapper inventoryTransactionMapper;
    private final ProductionSerialNumberService serialNumberService;

    public ProductionServiceImpl(ProductMapper productMapper,
                                 ProductPackageMapper productPackageMapper,
                                 MaterialMapper materialMapper,
                                 ProductionProcessMapper processMapper,
                                 ProductionProcessStepMapper processStepMapper,
                                 ProductionBomMapper bomMapper,
                                 ProductionBomItemMapper bomItemMapper,
                                 ProductionBatchMapper batchMapper,
                                 ProductionBoxMapper boxMapper,
                                 ProductionProductStockMapper productStockMapper,
                                 ProductionReportMapper reportMapper,
                                 SerialNumberMapper serialNumberMapper,
                                 InventoryTransactionMapper inventoryTransactionMapper,
                                 ProductionSerialNumberService serialNumberService) {
        this.productMapper = productMapper;
        this.productPackageMapper = productPackageMapper;
        this.materialMapper = materialMapper;
        this.processMapper = processMapper;
        this.processStepMapper = processStepMapper;
        this.bomMapper = bomMapper;
        this.bomItemMapper = bomItemMapper;
        this.batchMapper = batchMapper;
        this.boxMapper = boxMapper;
        this.productStockMapper = productStockMapper;
        this.reportMapper = reportMapper;
        this.serialNumberMapper = serialNumberMapper;
        this.inventoryTransactionMapper = inventoryTransactionMapper;
        this.serialNumberService = serialNumberService;
    }

    @Override
    public PageVO<ProductionProcessVO> listProcesses(long pageNum, long pageSize, String name, UUID productId, Integer status) {
        Page<ProductionProcess> page = processMapper.selectPage(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<ProductionProcess>()
                        .like(hasText(name), ProductionProcess::getName, name)
                        .eq(productId != null, ProductionProcess::getProductId, productId)
                        .eq(status != null, ProductionProcess::getStatus, status)
                        .orderByDesc(ProductionProcess::getCreatedAt));
        List<ProductionProcessVO> records = page.getRecords().stream().map(this::toProcessVO).collect(Collectors.toList());
        return new PageVO<>(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    @Transactional
    public ProductionProcessVO saveProcess(UUID id, ProductionProcessRequest request) {
        if (request.getProductId() != null) {
            ensureProductExists(request.getProductId());
        }
        ensureUniqueProcessCode(id, request.getCode());
        validateProcessSteps(request.getSteps());

        OffsetDateTime now = OffsetDateTime.now();
        ProductionProcess process = id == null ? new ProductionProcess() : getProcess(id);
        if (id == null) {
            process.setId(UUID.randomUUID());
            process.setCreatedBy(SecurityUtils.getUserId());
            process.setCreatedAt(now);
        }
        process.setCode(request.getCode());
        process.setName(request.getName());
        process.setProductId(request.getProductId());
        process.setVersion(defaultString(request.getVersion(), "V1.0"));
        process.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        process.setRemark(request.getRemark());
        process.setUpdatedBy(SecurityUtils.getUserId());
        process.setUpdatedAt(now);
        if (id == null) {
            processMapper.insert(process);
        } else {
            processMapper.updateById(process);
        }
        replaceProcessSteps(process.getId(), request.getSteps());
        return toProcessVO(process);
    }

    @Override
    public PageVO<ProductionBomVO> listBoms(long pageNum, long pageSize, UUID productId, Integer status) {
        Page<ProductionBom> page = bomMapper.selectPage(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<ProductionBom>()
                        .eq(productId != null, ProductionBom::getProductId, productId)
                        .eq(status != null, ProductionBom::getStatus, status)
                        .orderByDesc(ProductionBom::getCreatedAt));
        List<ProductionBomVO> records = page.getRecords().stream().map(this::toBomVO).collect(Collectors.toList());
        return new PageVO<>(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    @Transactional
    public ProductionBomVO saveBom(UUID id, ProductionBomRequest request) {
        ensureProductExists(request.getProductId());
        ensureUniqueBomCode(id, request.getCode());
        validateBomItems(request.getItems());

        OffsetDateTime now = OffsetDateTime.now();
        ProductionBom bom = id == null ? new ProductionBom() : getBom(id);
        if (id == null) {
            bom.setId(UUID.randomUUID());
            bom.setCreatedBy(SecurityUtils.getUserId());
            bom.setCreatedAt(now);
        }
        bom.setCode(request.getCode());
        bom.setProductId(request.getProductId());
        bom.setVersion(defaultString(request.getVersion(), "V1.0"));
        bom.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        bom.setEffectiveDate(request.getEffectiveDate());
        bom.setRemark(request.getRemark());
        bom.setUpdatedBy(SecurityUtils.getUserId());
        bom.setUpdatedAt(now);
        if (id == null) {
            bomMapper.insert(bom);
        } else {
            bomMapper.updateById(bom);
        }
        replaceBomItems(bom.getId(), request.getItems());
        return toBomVO(bom);
    }

    @Override
    public PageVO<ProductionBatchVO> listBatches(long pageNum, long pageSize, String batchNo, UUID productId, String status) {
        Page<ProductionBatch> page = batchMapper.selectPage(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<ProductionBatch>()
                        .like(hasText(batchNo), ProductionBatch::getBatchNo, batchNo)
                        .eq(productId != null, ProductionBatch::getProductId, productId)
                        .eq(hasText(status), ProductionBatch::getStatus, status)
                        .orderByDesc(ProductionBatch::getCreatedAt));
        List<ProductionBatchVO> records = page.getRecords().stream().map(this::toBatchVO).collect(Collectors.toList());
        return new PageVO<>(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    @Transactional
    public ProductionBatchVO saveBatch(UUID id, ProductionBatchRequest request) {
        Product product = ensureProductExists(request.getProductId());
        ensureUniqueBatchNo(id, request.getBatchNo());
        if (request.getProcessId() != null) {
            ProductionProcess process = getProcess(request.getProcessId());
            if (process.getProductId() != null && !process.getProductId().equals(request.getProductId())) {
                throw new BizException(10004, "Process product does not match batch product");
            }
        }
        if (request.getBomId() != null) {
            ProductionBom bom = getBom(request.getBomId());
            if (!bom.getProductId().equals(request.getProductId())) {
                throw new BizException(10004, "BOM product does not match batch product");
            }
        }

        OffsetDateTime now = OffsetDateTime.now();
        ProductionBatch batch = id == null ? new ProductionBatch() : getBatch(id);
        if (id == null) {
            batch.setId(UUID.randomUUID());
            batch.setCreatedBy(SecurityUtils.getUserId());
            batch.setCreatedAt(now);
        }
        batch.setBatchNo(request.getBatchNo());
        batch.setProductId(request.getProductId());
        batch.setPlannedQuantity(request.getPlannedQuantity());
        batch.setCompletedQuantity(request.getCompletedQuantity() == null ? BigDecimal.ZERO : request.getCompletedQuantity());
        batch.setUnit(hasText(request.getUnit()) ? request.getUnit() : product.getUnit());
        batch.setProcessId(request.getProcessId());
        batch.setBomId(request.getBomId());
        batch.setStatus(defaultString(request.getStatus(), "DRAFT"));
        batch.setPlannedStartDate(request.getPlannedStartDate());
        batch.setPlannedEndDate(request.getPlannedEndDate());
        batch.setRemark(request.getRemark());
        batch.setUpdatedBy(SecurityUtils.getUserId());
        batch.setUpdatedAt(now);
        if (id == null) {
            batchMapper.insert(batch);
        } else {
            batchMapper.updateById(batch);
        }
        return toBatchVO(batch);
    }

    @Override
    @Transactional
    public ProductionBatchVO startBatch(UUID id) {
        ProductionBatch batch = getBatch(id);
        ProductionBatchStatusMachine.ensureCanStart(batch.getStatus());
        ensureBomMaterialsAvailable(batch);
        if (!ProductionBatchStatusMachine.IN_PROGRESS.equals(batch.getStatus())) {
            batch.setStatus(ProductionBatchStatusMachine.IN_PROGRESS);
            if (batch.getStartedAt() == null) {
                batch.setStartedAt(OffsetDateTime.now());
            }
        }
        batch.setUpdatedBy(SecurityUtils.getUserId());
        batch.setUpdatedAt(OffsetDateTime.now());
        batchMapper.updateById(batch);
        return toBatchVO(batch);
    }

    private void ensureBomMaterialsAvailable(ProductionBatch batch) {
        if (batch.getBomId() == null) {
            return;
        }
        List<ProductionBomItem> items = bomItemMapper.selectByBomId(batch.getBomId());
        if (items == null || items.isEmpty()) {
            throw new BizException(10004, "生产 BOM 未配置原料，不能投产");
        }
        for (ProductionBomItem item : items) {
            Material material = ensureMaterialExists(item.getMaterialId());
            BigDecimal requiredQuantity = safe(item.getQuantity()).multiply(safe(batch.getPlannedQuantity()));
            BigDecimal availableQuantity = safe(material.getCurrentStock());
            if (availableQuantity.compareTo(requiredQuantity) < 0) {
                throw new BizException(10004, "原料库存不足，不能投产：" + material.getCode() + "，需 " + requiredQuantity + "，可用 " + availableQuantity);
            }
        }
    }

    @Override
    @Transactional
    public ProductionBatchVO receiveBatch(UUID id) {
        ProductionBatch batch = getBatch(id);
        ProductionBatchStatusMachine.ensureCanReceive(batch.getStatus());
        BigDecimal receiptQuantity = safe(batch.getCompletedQuantity());
        if (receiptQuantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BizException(10004, "完工数量必须大于0才能入库");
        }
        Product product = ensureProductExists(batch.getProductId());
        ProductionProductStock stock = productStockMapper.selectOne(
                new LambdaQueryWrapper<ProductionProductStock>().eq(ProductionProductStock::getProductId, batch.getProductId())
        );
        OffsetDateTime now = OffsetDateTime.now();
        if (stock == null) {
            stock = new ProductionProductStock();
            stock.setId(UUID.randomUUID());
            stock.setProductId(product.getId());
            stock.setProductCode(product.getCode());
            stock.setProductName(product.getName());
            stock.setCurrentStock(BigDecimal.ZERO);
            stock.setCreatedBy(SecurityUtils.getUserId());
            stock.setCreatedAt(now);
        }
        stock.setCurrentStock(safe(stock.getCurrentStock()).add(receiptQuantity));
        stock.setUpdatedBy(SecurityUtils.getUserId());
        stock.setUpdatedAt(now);
        if (stock.getCreatedAt() == null) {
            productStockMapper.updateById(stock);
        } else {
            ProductionProductStock existing = productStockMapper.selectById(stock.getId());
            if (existing == null) {
                productStockMapper.insert(stock);
            } else {
                productStockMapper.updateById(stock);
            }
        }
        batch.setStatus(ProductionBatchStatusMachine.CLOSED);
        batch.setUpdatedBy(SecurityUtils.getUserId());
        batch.setUpdatedAt(now);
        batchMapper.updateById(batch);
        createProductionInTransaction(batch, stock, receiptQuantity, now);
        serialNumberService.markBatchStocked(batch.getId(), receiptQuantity, now);
        return toBatchVO(batch);
    }

    @Override
    @Transactional
    public List<SerialNumberVO> generateSerialNumbers(UUID batchId, SerialNumberGenerateRequest request) {
        ProductionBatch batch = getBatch(batchId);
        Product product = ensureProductExists(batch.getProductId());
        int quantity = request.getQuantity() == null ? batch.getPlannedQuantity().intValue() : request.getQuantity();
        if (quantity <= 0) {
            throw new BizException(10004, "Serial quantity must be greater than 0");
        }
        long existingCount = serialNumberMapper.selectCount(new LambdaQueryWrapper<SerialNumber>().eq(SerialNumber::getBatchId, batchId));
        String prefix = defaultString(request.getPrefix(), batch.getBatchNo());
        List<SerialNumberVO> result = new java.util.ArrayList<>();
        for (int index = 1; index <= quantity; index++) {
            String serialNo = "%s-%04d".formatted(prefix, existingCount + index);
            SerialNumber serialNumber = new SerialNumber();
            serialNumber.setId(UUID.randomUUID());
            serialNumber.setSerialNo(serialNo);
            serialNumber.setBatchId(batchId);
            serialNumber.setProductId(product.getId());
            serialNumber.setStatus("GENERATED");
            serialNumber.setCreatedBy(SecurityUtils.getUserId());
            serialNumber.setCreatedAt(OffsetDateTime.now());
            serialNumber.setUpdatedBy(SecurityUtils.getUserId());
            serialNumber.setUpdatedAt(OffsetDateTime.now());
            serialNumberMapper.insert(serialNumber);
            result.add(toSerialNumberVO(serialNumber));
        }
        return result;
    }

    @Override
    public PageVO<ProductionReportVO> listReports(long pageNum, long pageSize, String batchNo, UUID productId, String status) {
        Page<ProductionReport> page = reportMapper.selectPage(
                new Page<>(pageNum, pageSize),
                reportWrapper(batchNo, productId, status));
        List<ProductionReportVO> records = page.getRecords().stream().map(this::toReportVO).collect(Collectors.toList());
        return new PageVO<>(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public ByteArrayInputStream exportReports(String batchNo, UUID productId, String status) {
        List<ProductionReportVO> records = reportMapper.selectList(reportWrapper(batchNo, productId, status))
                .stream()
                .map(this::toReportVO)
                .collect(Collectors.toList());
        return ExcelExportUtils.export("production-reports", List.of(
                ExcelExportUtils.column("报工单号", ProductionReportVO::getReportNo),
                ExcelExportUtils.column("生产批次", ProductionReportVO::getBatchNo),
                ExcelExportUtils.column("产品编码", ProductionReportVO::getProductCode),
                ExcelExportUtils.column("产品名称", ProductionReportVO::getProductName),
                ExcelExportUtils.column("报工数量", ProductionReportVO::getReportQuantity),
                ExcelExportUtils.column("良品数量", ProductionReportVO::getGoodQuantity),
                ExcelExportUtils.column("不良数量", ProductionReportVO::getDefectQuantity),
                ExcelExportUtils.column("报工时间", ProductionReportVO::getReportAt),
                ExcelExportUtils.column("操作人", ProductionReportVO::getOperatorName),
                ExcelExportUtils.column("状态", ProductionReportVO::getStatus),
                ExcelExportUtils.column("备注", ProductionReportVO::getRemark)
        ), records, "生产报工导出失败");
    }

    @Override
    @Transactional
    public ProductionReportVO createReport(ProductionReportRequest request) {
        ProductionBatch batch = getBatch(request.getBatchId());
        ProductionBatchStatusMachine.ensureCanReport(batch.getStatus());

        BigDecimal reportQuantity = request.getReportQuantity();
        BigDecimal defectQuantity = safe(request.getDefectQuantity());
        BigDecimal goodQuantity = request.getGoodQuantity() == null ? reportQuantity.subtract(defectQuantity) : request.getGoodQuantity();
        if (goodQuantity.compareTo(BigDecimal.ZERO) < 0 || defectQuantity.compareTo(BigDecimal.ZERO) < 0) {
            throw new BizException(10004, "Report quantity cannot be negative");
        }
        if (goodQuantity.add(defectQuantity).compareTo(reportQuantity) != 0) {
            throw new BizException(10004, "Good quantity plus defect quantity must equal report quantity");
        }
        if (safe(batch.getCompletedQuantity()).add(goodQuantity).compareTo(safe(batch.getPlannedQuantity())) > 0) {
            throw new BizException(10004, "累计良品数量不能超过计划数量");
        }

        Product product = ensureProductExists(batch.getProductId());
        OffsetDateTime now = OffsetDateTime.now();
        ProductionReport report = new ProductionReport();
        report.setId(UUID.randomUUID());
        report.setReportNo(defaultString(request.getReportNo(), generateReportNo()));
        report.setBatchId(batch.getId());
        report.setBatchNo(batch.getBatchNo());
        report.setProductId(batch.getProductId());
        report.setProductCode(product.getCode());
        report.setProductName(product.getName());
        report.setReportQuantity(reportQuantity);
        report.setGoodQuantity(goodQuantity);
        report.setDefectQuantity(defectQuantity);
        report.setReportAt(parseDateTimeOrNow(request.getReportAt()));
        report.setOperatorName(request.getOperatorName());
        report.setStatus("SUBMITTED");
        report.setRemark(request.getRemark());
        report.setCreatedBy(SecurityUtils.getUserId());
        report.setCreatedAt(now);
        report.setUpdatedBy(SecurityUtils.getUserId());
        report.setUpdatedAt(now);
        reportMapper.insert(report);

        BigDecimal completedQuantity = safe(batch.getCompletedQuantity()).add(goodQuantity);
        batch.setCompletedQuantity(completedQuantity);
        if (batch.getStartedAt() == null) {
            batch.setStartedAt(now);
        }
        batch.setStatus(completedQuantity.compareTo(batch.getPlannedQuantity()) >= 0
                ? ProductionBatchStatusMachine.COMPLETED
                : ProductionBatchStatusMachine.IN_PROGRESS);
        if (ProductionBatchStatusMachine.COMPLETED.equals(batch.getStatus())) {
            batch.setCompletedAt(now);
        }
        batch.setUpdatedBy(SecurityUtils.getUserId());
        batch.setUpdatedAt(now);
        batchMapper.updateById(batch);
        return toReportVO(report);
    }

    @Override
    public PageVO<ProductionBoxVO> listBoxes(long pageNum, long pageSize, String batchNo, UUID productId, String status) {
        Page<ProductionBox> page = boxMapper.selectPage(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<ProductionBox>()
                        .like(hasText(batchNo), ProductionBox::getBatchNo, batchNo)
                        .eq(productId != null, ProductionBox::getProductId, productId)
                        .eq(hasText(status), ProductionBox::getStatus, status)
                        .orderByDesc(ProductionBox::getCreatedAt));
        List<ProductionBoxVO> records = page.getRecords().stream().map(this::toBoxVO).collect(Collectors.toList());
        return new PageVO<>(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    @Transactional
    public ProductionBoxVO packBox(ProductionBoxRequest request) {
        ProductionBatch batch = getBatch(request.getBatchId());
        ProductionBatchStatusMachine.ensureCanPack(batch.getStatus());
        Product product = ensureProductExists(batch.getProductId());
        ProductPackage productPackage = productPackageMapper.selectById(request.getPackageId());
        if (productPackage == null || !productPackage.getProductId().equals(batch.getProductId())) {
            throw new BizException(10006, "Package specification does not exist");
        }
        BigDecimal quantity = request.getQuantity();
        if (quantity == null && request.getSerialNos() != null && !request.getSerialNos().isEmpty()) {
            quantity = BigDecimal.valueOf(request.getSerialNos().size());
        }
        if (quantity == null) {
            quantity = BigDecimal.valueOf(productPackage.getQuantity() == null ? 1 : productPackage.getQuantity());
        }
        if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BizException(10004, "装箱数量必须大于0");
        }
        BigDecimal packedQuantity = sumPackedQuantity(batch.getId());
        if (packedQuantity.add(quantity).compareTo(safe(batch.getCompletedQuantity())) > 0) {
            throw new BizException(10004, "累计装箱数量不能超过已完成良品数量");
        }
        OffsetDateTime now = OffsetDateTime.now();
        String boxCode = "BOX-" + batch.getBatchNo() + "-" + now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        ProductionBox box = new ProductionBox();
        box.setId(UUID.randomUUID());
        box.setBoxCode(boxCode);
        box.setBatchId(batch.getId());
        box.setBatchNo(batch.getBatchNo());
        box.setProductId(product.getId());
        box.setProductCode(product.getCode());
        box.setProductName(product.getName());
        box.setPackageId(productPackage.getId());
        box.setPackageName(productPackage.getName());
        box.setPackageLevel(productPackage.getLevel());
        box.setQuantity(quantity);
        box.setSerialNos(request.getSerialNos() == null ? null : String.join(",", request.getSerialNos()));
        box.setLabelHtml(buildBoxLabelHtml(box));
        box.setStatus("PACKED");
        box.setRemark(request.getRemark());
        box.setCreatedBy(SecurityUtils.getUserId());
        box.setCreatedAt(now);
        box.setUpdatedBy(SecurityUtils.getUserId());
        box.setUpdatedAt(now);
        serialNumberService.markPacked(batch.getId(), product.getId(), request.getSerialNos(), now);
        boxMapper.insert(box);
        return toBoxVO(box);
    }

    @Override
    public PageVO<ProductionProductStockVO> listProductStock(long pageNum, long pageSize, String productName) {
        Page<ProductionProductStock> page = productStockMapper.selectPage(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<ProductionProductStock>()
                        .like(hasText(productName), ProductionProductStock::getProductName, productName)
                        .orderByDesc(ProductionProductStock::getUpdatedAt));
        List<ProductionProductStockVO> records = page.getRecords().stream().map(this::toProductStockVO).collect(Collectors.toList());
        return new PageVO<>(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public PageVO<SerialNumberVO> listSerialNumbers(long pageNum, long pageSize, String serialNo, UUID batchId, UUID productId, String status) {
        Page<SerialNumber> page = serialNumberMapper.selectPage(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<SerialNumber>()
                        .like(hasText(serialNo), SerialNumber::getSerialNo, serialNo)
                        .eq(batchId != null, SerialNumber::getBatchId, batchId)
                        .eq(productId != null, SerialNumber::getProductId, productId)
                        .eq(hasText(status), SerialNumber::getStatus, status)
                        .orderByDesc(SerialNumber::getCreatedAt));
        List<SerialNumberVO> records = page.getRecords().stream().map(this::toSerialNumberVO).collect(Collectors.toList());
        return new PageVO<>(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    @Transactional
    public SerialNumberVO saveSerialNumber(UUID id, SerialNumberRequest request) {
        ensureProductExists(request.getProductId());
        ensureUniqueSerialNo(id, request.getSerialNo());
        if (request.getBatchId() != null) {
            ProductionBatch batch = getBatch(request.getBatchId());
            if (!batch.getProductId().equals(request.getProductId())) {
                throw new BizException(10004, "Batch product does not match serial product");
            }
        }

        OffsetDateTime now = OffsetDateTime.now();
        SerialNumber serialNumber = id == null ? new SerialNumber() : getSerialNumber(id);
        if (id == null) {
            serialNumber.setId(UUID.randomUUID());
            serialNumber.setCreatedBy(SecurityUtils.getUserId());
            serialNumber.setCreatedAt(now);
        }
        serialNumber.setSerialNo(request.getSerialNo());
        serialNumber.setBatchId(request.getBatchId());
        serialNumber.setProductId(request.getProductId());
        serialNumber.setStatus(defaultString(request.getStatus(), "GENERATED"));
        serialNumber.setProducedAt(parseDateTime(request.getProducedAt()));
        serialNumber.setShippedAt(parseDateTime(request.getShippedAt()));
        serialNumber.setRemark(request.getRemark());
        serialNumber.setUpdatedBy(SecurityUtils.getUserId());
        serialNumber.setUpdatedAt(now);
        if (id == null) {
            serialNumberMapper.insert(serialNumber);
        } else {
            serialNumberMapper.updateById(serialNumber);
        }
        return toSerialNumberVO(serialNumber);
    }

    private void replaceProcessSteps(UUID processId, List<ProductionProcessStepRequest> stepRequests) {
        processStepMapper.delete(new LambdaQueryWrapper<ProductionProcessStep>().eq(ProductionProcessStep::getProcessId, processId));
        if (stepRequests == null) {
            return;
        }
        for (ProductionProcessStepRequest request : stepRequests) {
            ProductionProcessStep step = new ProductionProcessStep();
            step.setId(request.getId() == null ? UUID.randomUUID() : request.getId());
            step.setProcessId(processId);
            step.setStepNo(request.getStepNo());
            step.setName(request.getName());
            step.setWorkstation(request.getWorkstation());
            step.setStandardMinutes(request.getStandardMinutes());
            step.setQualityRequirement(request.getQualityRequirement());
            step.setRemark(request.getRemark());
            processStepMapper.insert(step);
        }
    }

    private void replaceBomItems(UUID bomId, List<ProductionBomItemRequest> itemRequests) {
        bomItemMapper.delete(new LambdaQueryWrapper<ProductionBomItem>().eq(ProductionBomItem::getBomId, bomId));
        if (itemRequests == null) {
            return;
        }
        for (ProductionBomItemRequest request : itemRequests) {
            int itemType = request.getItemType() == null || request.getItemType() == 1 ? 1 : 2;
            String defaultUnit;
            if (itemType == 2) {
                Product product = ensureProductExists(request.getMaterialId());
                defaultUnit = product.getUnit();
            } else {
                Material material = ensureMaterialExists(request.getMaterialId());
                defaultUnit = material.getUnit();
            }
            ProductionBomItem item = new ProductionBomItem();
            item.setId(request.getId() == null ? UUID.randomUUID() : request.getId());
            item.setBomId(bomId);
            item.setMaterialId(request.getMaterialId());
            item.setItemType(itemType);
            item.setQuantity(request.getQuantity());
            item.setUnit(hasText(request.getUnit()) ? request.getUnit() : defaultUnit);
            item.setLossRate(request.getLossRate() == null ? BigDecimal.ZERO : request.getLossRate());
            item.setProcessStepNo(request.getProcessStepNo());
            item.setRemark(request.getRemark());
            bomItemMapper.insert(item);
        }
    }

    private void createProductionInTransaction(ProductionBatch batch, ProductionProductStock stock, BigDecimal quantity, OffsetDateTime now) {
        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setId(UUID.randomUUID());
        transaction.setMaterialId(stock.getProductId());
        transaction.setMaterialCode(stock.getProductCode());
        transaction.setMaterialName(stock.getProductName());
        transaction.setTransactionType("PRODUCTION_IN");
        transaction.setQuantity(quantity);
        transaction.setBalanceAfter(stock.getCurrentStock());
        transaction.setSourceType("PRODUCTION_BATCH");
        transaction.setSourceOrderId(batch.getId());
        transaction.setSourceOrderNo(batch.getBatchNo());
        transaction.setRemark("生产完工入库: " + batch.getBatchNo());
        transaction.setCreatedBy(SecurityUtils.getUserId());
        transaction.setCreatedAt(now);
        inventoryTransactionMapper.insert(transaction);
    }

    private void validateProcessSteps(List<ProductionProcessStepRequest> steps) {
        if (steps == null || steps.isEmpty()) {
            throw new BizException(10004, "Process must contain at least one step");
        }
        Set<Integer> stepNos = new HashSet<>();
        for (ProductionProcessStepRequest step : steps) {
            if (!stepNos.add(step.getStepNo())) {
                throw new BizException(10004, "Process contains duplicated step numbers");
            }
        }
    }

    private void validateBomItems(List<ProductionBomItemRequest> items) {
        if (items == null || items.isEmpty()) {
            throw new BizException(10004, "BOM must contain at least one item");
        }
        Set<String> seenKeys = new HashSet<>();
        for (ProductionBomItemRequest item : items) {
            int itemType = item.getItemType() == null || item.getItemType() == 1 ? 1 : 2;
            if (itemType == 2) {
                ensureProductExists(item.getMaterialId());
            } else {
                ensureMaterialExists(item.getMaterialId());
            }
            if (!seenKeys.add(itemType + ":" + item.getMaterialId())) {
                throw new BizException(10004, "BOM contains duplicated materials");
            }
        }
    }

    private void ensureUniqueProcessCode(UUID id, String code) {
        ProductionProcess existing = processMapper.selectOne(new LambdaQueryWrapper<ProductionProcess>().eq(ProductionProcess::getCode, code));
        if (existing != null && (id == null || !existing.getId().equals(id))) {
            throw new BizException(10004, "Process code already exists");
        }
    }

    private void ensureUniqueBomCode(UUID id, String code) {
        ProductionBom existing = bomMapper.selectOne(new LambdaQueryWrapper<ProductionBom>().eq(ProductionBom::getCode, code));
        if (existing != null && (id == null || !existing.getId().equals(id))) {
            throw new BizException(10004, "BOM code already exists");
        }
    }

    private void ensureUniqueBatchNo(UUID id, String batchNo) {
        ProductionBatch existing = batchMapper.selectOne(new LambdaQueryWrapper<ProductionBatch>().eq(ProductionBatch::getBatchNo, batchNo));
        if (existing != null && (id == null || !existing.getId().equals(id))) {
            throw new BizException(10004, "Batch number already exists");
        }
    }

    private void ensureUniqueSerialNo(UUID id, String serialNo) {
        SerialNumber existing = serialNumberMapper.selectOne(new LambdaQueryWrapper<SerialNumber>().eq(SerialNumber::getSerialNo, serialNo));
        if (existing != null && (id == null || !existing.getId().equals(id))) {
            throw new BizException(10004, "Serial number already exists");
        }
    }

    private Product ensureProductExists(UUID productId) {
        Product product = productMapper.selectById(productId);
        if (product == null || Boolean.TRUE.equals(product.getDeleted())) {
            throw new BizException(10006, "Product does not exist");
        }
        return product;
    }

    private Material ensureMaterialExists(UUID materialId) {
        Material material = materialMapper.selectById(materialId);
        if (material == null) {
            throw new BizException(10006, "Material does not exist");
        }
        return material;
    }

    private ProductionProcess getProcess(UUID id) {
        ProductionProcess process = processMapper.selectById(id);
        if (process == null) {
            throw new BizException(10006, "Process does not exist");
        }
        return process;
    }

    private ProductionBom getBom(UUID id) {
        ProductionBom bom = bomMapper.selectById(id);
        if (bom == null) {
            throw new BizException(10006, "BOM does not exist");
        }
        return bom;
    }

    private ProductionBatch getBatch(UUID id) {
        ProductionBatch batch = batchMapper.selectById(id);
        if (batch == null) {
            throw new BizException(10006, "Batch does not exist");
        }
        return batch;
    }

    private SerialNumber getSerialNumber(UUID id) {
        SerialNumber serialNumber = serialNumberMapper.selectById(id);
        if (serialNumber == null) {
            throw new BizException(10006, "Serial number does not exist");
        }
        return serialNumber;
    }

    private ProductionProcessVO toProcessVO(ProductionProcess process) {
        ProductionProcessVO vo = new ProductionProcessVO();
        vo.setId(process.getId());
        vo.setCode(process.getCode());
        vo.setName(process.getName());
        vo.setProductId(process.getProductId());
        Product product = process.getProductId() == null ? null : productMapper.selectById(process.getProductId());
        vo.setProductName(product == null ? null : product.getName());
        vo.setVersion(process.getVersion());
        vo.setStatus(process.getStatus());
        vo.setRemark(process.getRemark());
        vo.setCreatedAt(process.getCreatedAt());
        vo.setUpdatedAt(process.getUpdatedAt());
        vo.setSteps(processStepMapper.selectByProcessId(process.getId()).stream().map(this::toProcessStepVO).toList());
        return vo;
    }

    private ProductionProcessStepVO toProcessStepVO(ProductionProcessStep step) {
        ProductionProcessStepVO vo = new ProductionProcessStepVO();
        vo.setId(step.getId());
        vo.setStepNo(step.getStepNo());
        vo.setName(step.getName());
        vo.setWorkstation(step.getWorkstation());
        vo.setStandardMinutes(step.getStandardMinutes());
        vo.setQualityRequirement(step.getQualityRequirement());
        vo.setRemark(step.getRemark());
        return vo;
    }

    private ProductionBomVO toBomVO(ProductionBom bom) {
        ProductionBomVO vo = new ProductionBomVO();
        vo.setId(bom.getId());
        vo.setCode(bom.getCode());
        vo.setProductId(bom.getProductId());
        Product product = productMapper.selectById(bom.getProductId());
        if (product != null) {
            vo.setProductCode(product.getCode());
            vo.setProductName(product.getName());
        }
        vo.setVersion(bom.getVersion());
        vo.setStatus(bom.getStatus());
        vo.setEffectiveDate(bom.getEffectiveDate());
        vo.setRemark(bom.getRemark());
        vo.setCreatedAt(bom.getCreatedAt());
        vo.setUpdatedAt(bom.getUpdatedAt());
        vo.setItems(bomItemMapper.selectByBomId(bom.getId()).stream().map(this::toBomItemVO).toList());
        return vo;
    }

    private ProductionBomItemVO toBomItemVO(ProductionBomItem item) {
        ProductionBomItemVO vo = new ProductionBomItemVO();
        vo.setId(item.getId());
        vo.setMaterialId(item.getMaterialId());
        int itemType = item.getItemType() == null || item.getItemType() == 1 ? 1 : 2;
        vo.setItemType(itemType);
        if (itemType == 2) {
            Product product = productMapper.selectById(item.getMaterialId());
            if (product != null) {
                vo.setMaterialCode(product.getCode());
                vo.setMaterialName(product.getName());
            }
        } else {
            Material material = materialMapper.selectById(item.getMaterialId());
            if (material != null) {
                vo.setMaterialCode(material.getCode());
                vo.setMaterialName(material.getName());
            }
        }
        vo.setQuantity(item.getQuantity());
        vo.setUnit(item.getUnit());
        vo.setLossRate(item.getLossRate());
        vo.setProcessStepNo(item.getProcessStepNo());
        vo.setRemark(item.getRemark());
        return vo;
    }

    private ProductionBatchVO toBatchVO(ProductionBatch batch) {
        ProductionBatchVO vo = new ProductionBatchVO();
        vo.setId(batch.getId());
        vo.setBatchNo(batch.getBatchNo());
        vo.setProductId(batch.getProductId());
        Product product = productMapper.selectById(batch.getProductId());
        if (product != null) {
            vo.setProductCode(product.getCode());
            vo.setProductName(product.getName());
        }
        vo.setPlannedQuantity(batch.getPlannedQuantity());
        vo.setCompletedQuantity(batch.getCompletedQuantity());
        vo.setUnit(batch.getUnit());
        vo.setProcessId(batch.getProcessId());
        ProductionProcess process = batch.getProcessId() == null ? null : processMapper.selectById(batch.getProcessId());
        vo.setProcessName(process == null ? null : process.getName());
        vo.setBomId(batch.getBomId());
        ProductionBom bom = batch.getBomId() == null ? null : bomMapper.selectById(batch.getBomId());
        vo.setBomCode(bom == null ? null : bom.getCode());
        vo.setStatus(batch.getStatus());
        vo.setPlannedStartDate(batch.getPlannedStartDate());
        vo.setPlannedEndDate(batch.getPlannedEndDate());
        vo.setStartedAt(batch.getStartedAt());
        vo.setCompletedAt(batch.getCompletedAt());
        vo.setRemark(batch.getRemark());
        vo.setCreatedAt(batch.getCreatedAt());
        vo.setUpdatedAt(batch.getUpdatedAt());
        return vo;
    }

    private ProductionReportVO toReportVO(ProductionReport report) {
        ProductionReportVO vo = new ProductionReportVO();
        vo.setId(report.getId());
        vo.setReportNo(report.getReportNo());
        vo.setBatchId(report.getBatchId());
        vo.setBatchNo(report.getBatchNo());
        vo.setProductId(report.getProductId());
        vo.setProductCode(report.getProductCode());
        vo.setProductName(report.getProductName());
        vo.setReportQuantity(report.getReportQuantity());
        vo.setGoodQuantity(report.getGoodQuantity());
        vo.setDefectQuantity(report.getDefectQuantity());
        vo.setReportAt(report.getReportAt());
        vo.setOperatorName(report.getOperatorName());
        vo.setStatus(report.getStatus());
        vo.setRemark(report.getRemark());
        vo.setCreatedAt(report.getCreatedAt());
        vo.setUpdatedAt(report.getUpdatedAt());
        return vo;
    }

    private LambdaQueryWrapper<ProductionReport> reportWrapper(String batchNo, UUID productId, String status) {
        return new LambdaQueryWrapper<ProductionReport>()
                .like(hasText(batchNo), ProductionReport::getBatchNo, batchNo)
                .eq(productId != null, ProductionReport::getProductId, productId)
                .eq(hasText(status), ProductionReport::getStatus, status)
                .orderByDesc(ProductionReport::getReportAt)
                .orderByDesc(ProductionReport::getCreatedAt);
    }

    private ProductionBoxVO toBoxVO(ProductionBox box) {
        ProductionBoxVO vo = new ProductionBoxVO();
        vo.setId(box.getId());
        vo.setBoxCode(box.getBoxCode());
        vo.setBatchId(box.getBatchId());
        vo.setBatchNo(box.getBatchNo());
        vo.setProductId(box.getProductId());
        vo.setProductCode(box.getProductCode());
        vo.setProductName(box.getProductName());
        vo.setPackageId(box.getPackageId());
        vo.setPackageName(box.getPackageName());
        vo.setPackageLevel(box.getPackageLevel());
        vo.setQuantity(box.getQuantity());
        vo.setSerialNos(box.getSerialNos() == null || box.getSerialNos().isBlank() ? List.of() : List.of(box.getSerialNos().split(",")));
        vo.setLabelHtml(box.getLabelHtml());
        vo.setStatus(box.getStatus());
        vo.setRemark(box.getRemark());
        vo.setCreatedAt(box.getCreatedAt());
        vo.setUpdatedAt(box.getUpdatedAt());
        return vo;
    }

    private ProductionProductStockVO toProductStockVO(ProductionProductStock stock) {
        ProductionProductStockVO vo = new ProductionProductStockVO();
        vo.setId(stock.getId());
        vo.setProductId(stock.getProductId());
        vo.setProductCode(stock.getProductCode());
        vo.setProductName(stock.getProductName());
        vo.setCurrentStock(stock.getCurrentStock());
        vo.setCreatedAt(stock.getCreatedAt());
        vo.setUpdatedAt(stock.getUpdatedAt());
        return vo;
    }

    private SerialNumberVO toSerialNumberVO(SerialNumber serialNumber) {
        SerialNumberVO vo = new SerialNumberVO();
        vo.setId(serialNumber.getId());
        vo.setSerialNo(serialNumber.getSerialNo());
        vo.setBatchId(serialNumber.getBatchId());
        ProductionBatch batch = serialNumber.getBatchId() == null ? null : batchMapper.selectById(serialNumber.getBatchId());
        vo.setBatchNo(batch == null ? null : batch.getBatchNo());
        vo.setProductId(serialNumber.getProductId());
        Product product = productMapper.selectById(serialNumber.getProductId());
        if (product != null) {
            vo.setProductCode(product.getCode());
            vo.setProductName(product.getName());
        }
        vo.setStatus(serialNumber.getStatus());
        vo.setProducedAt(serialNumber.getProducedAt());
        vo.setShippedAt(serialNumber.getShippedAt());
        vo.setRemark(serialNumber.getRemark());
        vo.setCreatedAt(serialNumber.getCreatedAt());
        vo.setUpdatedAt(serialNumber.getUpdatedAt());
        return vo;
    }

    private OffsetDateTime parseDateTime(String value) {
        if (!hasText(value)) {
            return null;
        }
        try {
            return OffsetDateTime.parse(value);
        } catch (Exception ignored) {
            try {
                return LocalDateTime.parse(value, SPACE_DATE_TIME).atZone(ZoneId.systemDefault()).toOffsetDateTime();
            } catch (Exception ex) {
                throw new BizException(10004, "Invalid date time format");
            }
        }
    }

    private OffsetDateTime parseDateTimeOrNow(String value) {
        OffsetDateTime parsed = parseDateTime(value);
        return parsed == null ? OffsetDateTime.now() : parsed;
    }

    private String generateReportNo() {
        return "PR-" + OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    private String buildBoxLabelHtml(ProductionBox box) {
        return """
                <div style='width:320px;border:1px solid #111827;padding:14px;font-family:Arial,sans-serif;'>
                  <div style='font-size:18px;font-weight:700;'>%s</div>
                  <div style='margin-top:6px;'>产品：%s</div>
                  <div>批次：%s</div>
                  <div>包装：%s / 数量：%s</div>
                  <div style='margin-top:12px;padding:10px;border:1px dashed #111827;text-align:center;font-family:monospace;'>%s</div>
                </div>
                """.formatted(box.getBoxCode(), box.getProductName(), box.getBatchNo(), box.getPackageName(), box.getQuantity(), box.getBoxCode());
    }

    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private BigDecimal sumPackedQuantity(UUID batchId) {
        List<ProductionBox> boxes = boxMapper.selectList(new LambdaQueryWrapper<ProductionBox>()
                .eq(ProductionBox::getBatchId, batchId)
                .ne(ProductionBox::getStatus, "CANCELLED"));
        return boxes.stream()
                .map(ProductionBox::getQuantity)
                .map(this::safe)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String defaultString(String value, String defaultValue) {
        return hasText(value) ? value : defaultValue;
    }
}
