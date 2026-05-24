package com.erp.material.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.common.core.domain.PageVO;
import com.erp.common.core.exception.BizException;
import com.erp.common.security.util.SecurityUtils;
import com.erp.material.domain.dto.MaterialRequest;
import com.erp.material.domain.entity.Material;
import com.erp.material.domain.entity.MaterialCategory;
import com.erp.material.domain.entity.Supplier;
import com.erp.material.domain.entity.SupplierQuote;
import com.erp.material.domain.vo.MaterialAlertVO;
import com.erp.material.domain.vo.MaterialReplenishmentVO;
import com.erp.material.domain.vo.MaterialVO;
import com.erp.material.mapper.MaterialCategoryMapper;
import com.erp.material.mapper.MaterialMapper;
import com.erp.material.mapper.SupplierMapper;
import com.erp.material.service.MaterialService;
import com.erp.material.service.SupplierQuoteQueryService;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class MaterialServiceImpl implements MaterialService {
    private final MaterialMapper materialMapper;
    private final MaterialCategoryMapper materialCategoryMapper;
    private final SupplierMapper supplierMapper;
    private final SupplierQuoteQueryService supplierQuoteQueryService;

    public MaterialServiceImpl(
            MaterialMapper materialMapper,
            MaterialCategoryMapper materialCategoryMapper,
            SupplierMapper supplierMapper,
            SupplierQuoteQueryService supplierQuoteQueryService
    ) {
        this.materialMapper = materialMapper;
        this.materialCategoryMapper = materialCategoryMapper;
        this.supplierMapper = supplierMapper;
        this.supplierQuoteQueryService = supplierQuoteQueryService;
    }

    @Override
    public PageVO<MaterialVO> list(long pageNum, long pageSize, String name, UUID categoryId, Integer status) {
        Page<Material> page = materialMapper.selectPage(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<Material>()
                        .like(name != null && !name.isBlank(), Material::getName, name)
                        .eq(categoryId != null, Material::getCategoryId, categoryId)
                        .eq(status != null, Material::getStatus, status)
                        .orderByDesc(Material::getCreatedAt)
        );
        List<MaterialVO> records = page.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return new PageVO<>(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public MaterialVO save(UUID id, MaterialRequest request) {
        Material material = id == null ? new Material() : materialMapper.selectById(id);
        if (id != null && material == null) {
            throw new BizException(10006, "原料不存在");
        }
        if (request.getDefaultSupplierId() != null) {
            Supplier supplier = supplierMapper.selectById(request.getDefaultSupplierId());
            if (supplier == null || supplier.getStatus() == null || supplier.getStatus() != 1) {
                throw new BizException(10004, "默认供应商不存在或未启用");
            }
        }
        if (request.getCurrentStock() != null
                && request.getSafetyStock() != null
                && request.getCurrentStock().compareTo(request.getSafetyStock()) < 0
                && request.getLeadTimeDays() == null) {
            throw new BizException(10004, "低于安全库存时必须维护采购周期");
        }
        if (material.getId() == null) {
            material.setId(UUID.randomUUID());
            material.setCreatedBy(SecurityUtils.getUserId());
            material.setCreatedAt(OffsetDateTime.now());
        }
        material.setCode(request.getCode());
        material.setName(request.getName());
        material.setCategoryId(request.getCategoryId());
        material.setUnit(request.getUnit());
        material.setSpecifications(normalizeText(request.getSpecifications()));
        material.setDefaultSupplierId(request.getDefaultSupplierId());
        material.setSafetyStock(zero(request.getSafetyStock()));
        material.setCurrentStock(zero(request.getCurrentStock()));
        material.setLeadTimeDays(request.getLeadTimeDays());
        material.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        material.setUpdatedBy(SecurityUtils.getUserId());
        material.setUpdatedAt(OffsetDateTime.now());
        if (id == null) {
            materialMapper.insert(material);
        } else {
            materialMapper.updateById(material);
        }
        return toVO(material);
    }

    @Override
    public PageVO<MaterialAlertVO> listAlerts(long pageNum, long pageSize, String name) {
        List<MaterialAlertVO> allAlerts = materialMapper.selectList(
                        new LambdaQueryWrapper<Material>()
                                .like(name != null && !name.isBlank(), Material::getName, name)
                                .eq(Material::getStatus, 1)
                                .orderByAsc(Material::getCurrentStock)
                                .orderByAsc(Material::getSafetyStock)
                ).stream()
                .filter(this::isAlertMaterial)
                .map(this::toAlertVO)
                .collect(Collectors.toList());

        return pageSlice(allAlerts, pageNum, pageSize);
    }

    @Override
    public PageVO<MaterialReplenishmentVO> listReplenishmentSuggestions(long pageNum, long pageSize, String name) {
        List<MaterialReplenishmentVO> allSuggestions = materialMapper.selectList(
                        new LambdaQueryWrapper<Material>()
                                .like(name != null && !name.isBlank(), Material::getName, name)
                                .eq(Material::getStatus, 1)
                                .orderByAsc(Material::getCurrentStock)
                                .orderByAsc(Material::getSafetyStock)
                ).stream()
                .filter(this::isAlertMaterial)
                .map(this::toReplenishmentVO)
                .collect(Collectors.toList());

        return pageSlice(allSuggestions, pageNum, pageSize);
    }

    @Override
    public ByteArrayInputStream exportMaterials() {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("materials");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("原料编码");
            header.createCell(1).setCellValue("原料名称");
            header.createCell(2).setCellValue("单位");
            header.createCell(3).setCellValue("安全库存");
            header.createCell(4).setCellValue("当前库存");
            header.createCell(5).setCellValue("采购周期(天)");

            List<Material> materials = materialMapper.selectList(
                    new LambdaQueryWrapper<Material>().orderByDesc(Material::getCreatedAt)
            );
            int rowIndex = 1;
            for (Material material : materials) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(defaultString(material.getCode()));
                row.createCell(1).setCellValue(defaultString(material.getName()));
                row.createCell(2).setCellValue(defaultString(material.getUnit()));
                row.createCell(3).setCellValue(zero(material.getSafetyStock()).doubleValue());
                row.createCell(4).setCellValue(zero(material.getCurrentStock()).doubleValue());
                row.createCell(5).setCellValue(material.getLeadTimeDays() == null ? 0 : material.getLeadTimeDays());
            }
            workbook.write(outputStream);
            return new ByteArrayInputStream(outputStream.toByteArray());
        } catch (Exception ex) {
            throw new BizException(10006, "原料导出失败");
        }
    }

    @Override
    public void importMaterials(MultipartFile file) {
        try (XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || row.getCell(0) == null) {
                    continue;
                }
                String code = stringCellValue(row, 0);
                String name = stringCellValue(row, 1);
                String unit = stringCellValue(row, 2);
                BigDecimal safetyStock = decimalCellValue(row, 3);
                BigDecimal currentStock = decimalCellValue(row, 4);
                Integer leadTimeDays = integerCellValue(row, 5);
                if (code.isBlank() || name.isBlank()) {
                    continue;
                }

                Material material = materialMapper.selectOne(new LambdaQueryWrapper<Material>().eq(Material::getCode, code));
                if (material == null) {
                    material = new Material();
                    material.setId(UUID.randomUUID());
                    material.setCode(code);
                    material.setCreatedBy(SecurityUtils.getUserId());
                    material.setCreatedAt(OffsetDateTime.now());
                    material.setStatus(1);
                }
                material.setName(name);
                material.setUnit(unit.isBlank() ? "个" : unit);
                material.setSafetyStock(safetyStock);
                material.setCurrentStock(currentStock);
                material.setLeadTimeDays(leadTimeDays);
                material.setUpdatedBy(SecurityUtils.getUserId());
                material.setUpdatedAt(OffsetDateTime.now());
                if (materialMapper.selectById(material.getId()) == null) {
                    materialMapper.insert(material);
                } else {
                    materialMapper.updateById(material);
                }
            }
        } catch (Exception ex) {
            throw new BizException(10006, "原料导入失败");
        }
    }

    private MaterialVO toVO(Material material) {
        MaterialVO vo = new MaterialVO();
        vo.setId(material.getId());
        vo.setCode(material.getCode());
        vo.setName(material.getName());
        vo.setCategoryId(material.getCategoryId());
        if (material.getCategoryId() != null) {
            MaterialCategory category = materialCategoryMapper.selectById(material.getCategoryId());
            vo.setCategoryName(category == null ? null : category.getName());
        }
        vo.setUnit(material.getUnit());
        vo.setSpecifications(material.getSpecifications());
        vo.setDefaultSupplierId(material.getDefaultSupplierId());
        if (material.getDefaultSupplierId() != null) {
            Supplier supplier = supplierMapper.selectById(material.getDefaultSupplierId());
            vo.setDefaultSupplierName(supplier == null ? null : supplier.getName());
        }
        vo.setSafetyStock(material.getSafetyStock());
        vo.setCurrentStock(material.getCurrentStock());
        vo.setLeadTimeDays(material.getLeadTimeDays());
        vo.setStatus(material.getStatus());
        vo.setCreatedAt(material.getCreatedAt());
        vo.setUpdatedAt(material.getUpdatedAt());
        return vo;
    }

    private MaterialAlertVO toAlertVO(Material material) {
        MaterialVO base = toVO(material);
        MaterialAlertVO vo = new MaterialAlertVO();
        vo.setId(base.getId());
        vo.setCode(base.getCode());
        vo.setName(base.getName());
        vo.setCategoryId(base.getCategoryId());
        vo.setCategoryName(base.getCategoryName());
        vo.setUnit(base.getUnit());
        vo.setSpecifications(base.getSpecifications());
        vo.setDefaultSupplierId(base.getDefaultSupplierId());
        vo.setDefaultSupplierName(base.getDefaultSupplierName());
        vo.setSafetyStock(base.getSafetyStock());
        vo.setCurrentStock(base.getCurrentStock());
        vo.setLeadTimeDays(base.getLeadTimeDays());
        vo.setStatus(base.getStatus());
        vo.setCreatedAt(base.getCreatedAt());
        vo.setUpdatedAt(base.getUpdatedAt());
        vo.setShortageAmount(zero(base.getSafetyStock()).subtract(zero(base.getCurrentStock())));
        return vo;
    }

    private MaterialReplenishmentVO toReplenishmentVO(Material material) {
        MaterialReplenishmentVO vo = new MaterialReplenishmentVO();
        vo.setMaterialId(material.getId());
        vo.setMaterialCode(material.getCode());
        vo.setMaterialName(material.getName());
        vo.setUnit(material.getUnit());
        vo.setCurrentStock(zero(material.getCurrentStock()));
        vo.setSafetyStock(zero(material.getSafetyStock()));
        BigDecimal shortage = zero(material.getSafetyStock()).subtract(zero(material.getCurrentStock()));
        vo.setShortageAmount(shortage);

        List<SupplierQuote> quotes = supplierQuoteQueryService.listEffectiveQuotesByMaterialId(material.getId(), LocalDate.now());
        SupplierQuote selectedQuote = selectBestQuote(material, quotes);
        BigDecimal suggestedQuantity = shortage.max(BigDecimal.ZERO);
        String reason = "按安全库存缺口补货";

        if (selectedQuote != null) {
          BigDecimal moq = zero(selectedQuote.getMinOrderQuantity());
          if (moq.compareTo(BigDecimal.ZERO) > 0 && suggestedQuantity.compareTo(moq) < 0) {
              suggestedQuantity = moq;
              reason = "按安全库存缺口补货，并对齐供应商最小起订量";
          }
          Supplier supplier = supplierMapper.selectById(selectedQuote.getSupplierId());
          vo.setSupplierId(selectedQuote.getSupplierId());
          vo.setSupplierName(supplier == null ? null : supplier.getName());
          vo.setQuotePrice(selectedQuote.getQuotePrice());
          vo.setCurrency(selectedQuote.getCurrency());
          vo.setLeadTimeDays(selectedQuote.getLeadTimeDays());
          vo.setEstimatedAmount(selectedQuote.getQuotePrice() == null ? null : selectedQuote.getQuotePrice().multiply(suggestedQuantity).setScale(2, RoundingMode.HALF_UP));
        } else {
          if (material.getDefaultSupplierId() != null) {
              Supplier supplier = supplierMapper.selectById(material.getDefaultSupplierId());
              vo.setSupplierId(material.getDefaultSupplierId());
              vo.setSupplierName(supplier == null ? null : supplier.getName());
          }
          vo.setLeadTimeDays(material.getLeadTimeDays());
          reason = "按安全库存缺口补货，当前无有效报价";
        }

        vo.setSuggestedQuantity(suggestedQuantity);
        vo.setRecommendationReason(reason);
        return vo;
    }

    private SupplierQuote selectBestQuote(Material material, List<SupplierQuote> quotes) {
        if (quotes.isEmpty()) {
            return null;
        }
        if (material.getDefaultSupplierId() != null) {
            for (SupplierQuote quote : quotes) {
                if (material.getDefaultSupplierId().equals(quote.getSupplierId())) {
                    return quote;
                }
            }
        }
        return quotes.get(0);
    }

    private boolean isAlertMaterial(Material material) {
        return zero(material.getSafetyStock()).compareTo(BigDecimal.ZERO) > 0
                && zero(material.getCurrentStock()).compareTo(zero(material.getSafetyStock())) <= 0;
    }

    private String normalizeText(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private BigDecimal zero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }

    private <T> PageVO<T> pageSlice(List<T> source, long pageNum, long pageSize) {
        int fromIndex = (int) Math.max((pageNum - 1) * pageSize, 0);
        int toIndex = (int) Math.min(fromIndex + pageSize, source.size());
        List<T> records = fromIndex >= source.size() ? List.of() : source.subList(fromIndex, toIndex);
        return new PageVO<>(records, (long) source.size(), pageNum, pageSize);
    }

    private String stringCellValue(Row row, int cellIndex) {
        if (row.getCell(cellIndex) == null) {
            return "";
        }
        return row.getCell(cellIndex).toString().trim();
    }

    private BigDecimal decimalCellValue(Row row, int cellIndex) {
        String raw = stringCellValue(row, cellIndex);
        if (raw.isBlank()) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(raw);
    }

    private Integer integerCellValue(Row row, int cellIndex) {
        String raw = stringCellValue(row, cellIndex);
        if (raw.isBlank()) {
            return 0;
        }
        return (int) Double.parseDouble(raw);
    }
}
