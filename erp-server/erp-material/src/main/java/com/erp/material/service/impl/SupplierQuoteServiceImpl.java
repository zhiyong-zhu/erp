package com.erp.material.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.common.core.domain.PageVO;
import com.erp.common.core.exception.BizException;
import com.erp.common.security.util.SecurityUtils;
import com.erp.material.domain.dto.SupplierQuoteRequest;
import com.erp.material.domain.entity.Material;
import com.erp.material.domain.entity.Supplier;
import com.erp.material.domain.entity.SupplierQuote;
import com.erp.material.domain.vo.SupplierQuoteVO;
import com.erp.material.mapper.MaterialMapper;
import com.erp.material.mapper.SupplierMapper;
import com.erp.material.mapper.SupplierQuoteMapper;
import com.erp.material.service.SupplierQuoteQueryService;
import com.erp.material.service.SupplierQuoteService;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class SupplierQuoteServiceImpl implements SupplierQuoteService, SupplierQuoteQueryService {
    private final SupplierQuoteMapper supplierQuoteMapper;
    private final SupplierMapper supplierMapper;
    private final MaterialMapper materialMapper;

    public SupplierQuoteServiceImpl(
            SupplierQuoteMapper supplierQuoteMapper,
            SupplierMapper supplierMapper,
            MaterialMapper materialMapper
    ) {
        this.supplierQuoteMapper = supplierQuoteMapper;
        this.supplierMapper = supplierMapper;
        this.materialMapper = materialMapper;
    }

    @Override
    public PageVO<SupplierQuoteVO> list(long pageNum, long pageSize, UUID supplierId, UUID materialId) {
        Page<SupplierQuote> page = supplierQuoteMapper.selectPage(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<SupplierQuote>()
                        .eq(supplierId != null, SupplierQuote::getSupplierId, supplierId)
                        .eq(materialId != null, SupplierQuote::getMaterialId, materialId)
                        .orderByDesc(SupplierQuote::getEffectiveDate)
                        .orderByDesc(SupplierQuote::getCreatedAt)
        );
        List<SupplierQuoteVO> records = page.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return new PageVO<>(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public SupplierQuoteVO save(UUID id, SupplierQuoteRequest request) {
        Supplier supplier = supplierMapper.selectById(request.getSupplierId());
        if (supplier == null || supplier.getStatus() == null || supplier.getStatus() != 1) {
            throw new BizException(10004, "供应商不存在或未启用");
        }
        Material material = materialMapper.selectById(request.getMaterialId());
        if (material == null || material.getStatus() == null || material.getStatus() != 1) {
            throw new BizException(10004, "原料不存在或未启用");
        }
        if (request.getExpiryDate() != null
                && request.getEffectiveDate() != null
                && request.getExpiryDate().isBefore(request.getEffectiveDate())) {
            throw new BizException(10004, "失效日期不能早于生效日期");
        }

        SupplierQuote quote = id == null ? new SupplierQuote() : supplierQuoteMapper.selectById(id);
        if (id != null && quote == null) {
            throw new BizException(10006, "供应商报价不存在");
        }
        if (quote.getId() == null) {
            quote.setId(UUID.randomUUID());
            quote.setCreatedBy(SecurityUtils.getUserId());
            quote.setCreatedAt(OffsetDateTime.now());
        }
        quote.setSupplierId(request.getSupplierId());
        quote.setMaterialId(request.getMaterialId());
        quote.setQuotePrice(request.getQuotePrice());
        quote.setCurrency(request.getCurrency());
        quote.setMinOrderQuantity(request.getMinOrderQuantity());
        quote.setLeadTimeDays(request.getLeadTimeDays());
        quote.setRemark(request.getRemark());
        quote.setEffectiveDate(request.getEffectiveDate() == null ? LocalDate.now() : request.getEffectiveDate());
        quote.setExpiryDate(request.getExpiryDate());
        quote.setUpdatedBy(SecurityUtils.getUserId());
        quote.setUpdatedAt(OffsetDateTime.now());

        if (id == null) {
            supplierQuoteMapper.insert(quote);
        } else {
            supplierQuoteMapper.updateById(quote);
        }
        return toVO(quote);
    }

    @Override
    public List<SupplierQuote> listEffectiveQuotesByMaterialId(UUID materialId, LocalDate referenceDate) {
        LocalDate targetDate = referenceDate == null ? LocalDate.now() : referenceDate;
        return supplierQuoteMapper.selectList(
                new LambdaQueryWrapper<SupplierQuote>()
                        .eq(SupplierQuote::getMaterialId, materialId)
                        .le(SupplierQuote::getEffectiveDate, targetDate)
                        .and(wrapper -> wrapper.isNull(SupplierQuote::getExpiryDate).or().ge(SupplierQuote::getExpiryDate, targetDate))
                        .orderByAsc(SupplierQuote::getQuotePrice)
                        .orderByAsc(SupplierQuote::getLeadTimeDays)
                        .orderByDesc(SupplierQuote::getEffectiveDate)
        );
    }

    private SupplierQuoteVO toVO(SupplierQuote quote) {
        SupplierQuoteVO vo = new SupplierQuoteVO();
        vo.setId(quote.getId());
        vo.setSupplierId(quote.getSupplierId());
        Supplier supplier = supplierMapper.selectById(quote.getSupplierId());
        vo.setSupplierName(supplier == null ? null : supplier.getName());
        vo.setMaterialId(quote.getMaterialId());
        Material material = materialMapper.selectById(quote.getMaterialId());
        vo.setMaterialName(material == null ? null : material.getName());
        vo.setQuotePrice(quote.getQuotePrice());
        vo.setCurrency(quote.getCurrency());
        vo.setMinOrderQuantity(quote.getMinOrderQuantity());
        vo.setLeadTimeDays(quote.getLeadTimeDays());
        vo.setRemark(quote.getRemark());
        vo.setEffectiveDate(quote.getEffectiveDate());
        vo.setExpiryDate(quote.getExpiryDate());
        vo.setCreatedAt(quote.getCreatedAt());
        vo.setUpdatedAt(quote.getUpdatedAt());
        return vo;
    }
}
