package com.erp.material.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.common.core.domain.PageVO;
import com.erp.common.core.exception.BizException;
import com.erp.common.security.util.SecurityUtils;
import com.erp.material.domain.dto.SupplierRequest;
import com.erp.material.domain.entity.Supplier;
import com.erp.material.domain.vo.SupplierVO;
import com.erp.material.mapper.SupplierMapper;
import com.erp.material.service.SupplierService;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class SupplierServiceImpl implements SupplierService {
    private final SupplierMapper supplierMapper;

    public SupplierServiceImpl(SupplierMapper supplierMapper) {
        this.supplierMapper = supplierMapper;
    }

    @Override
    public PageVO<SupplierVO> list(long pageNum, long pageSize, String name) {
        Page<Supplier> page = supplierMapper.selectPage(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<Supplier>()
                        .like(name != null && !name.isBlank(), Supplier::getName, name)
                        .orderByDesc(Supplier::getCreatedAt));
        List<SupplierVO> records = page.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return new PageVO<>(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public SupplierVO save(UUID id, SupplierRequest request) {
        Supplier supplier = id == null ? new Supplier() : supplierMapper.selectById(id);
        if (id != null && supplier == null) {
            throw new BizException(10006, "供应商不存在");
        }
        if (supplier.getId() == null) {
            supplier.setId(UUID.randomUUID());
            supplier.setCreatedBy(SecurityUtils.getUserId());
            supplier.setCreatedAt(OffsetDateTime.now());
        }
        supplier.setCode(request.getCode());
        supplier.setName(request.getName());
        supplier.setShortName(request.getShortName());
        supplier.setContactPerson(request.getContactPerson());
        supplier.setPhone(request.getPhone());
        supplier.setEmail(request.getEmail());
        supplier.setAddress(request.getAddress());
        supplier.setBankName(request.getBankName());
        supplier.setBankAccount(request.getBankAccount());
        supplier.setTaxNumber(request.getTaxNumber());
        supplier.setCreditRating(request.getCreditRating());
        supplier.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        supplier.setUpdatedBy(SecurityUtils.getUserId());
        supplier.setUpdatedAt(OffsetDateTime.now());
        if (id == null) {
            supplierMapper.insert(supplier);
        } else {
            supplierMapper.updateById(supplier);
        }
        return toVO(supplier);
    }

    private SupplierVO toVO(Supplier supplier) {
        SupplierVO vo = new SupplierVO();
        vo.setId(supplier.getId());
        vo.setCode(supplier.getCode());
        vo.setName(supplier.getName());
        vo.setShortName(supplier.getShortName());
        vo.setContactPerson(supplier.getContactPerson());
        vo.setPhone(supplier.getPhone());
        vo.setEmail(supplier.getEmail());
        vo.setAddress(supplier.getAddress());
        vo.setBankName(supplier.getBankName());
        vo.setBankAccount(supplier.getBankAccount());
        vo.setTaxNumber(supplier.getTaxNumber());
        vo.setCreditRating(supplier.getCreditRating());
        vo.setStatus(supplier.getStatus());
        vo.setCreatedAt(supplier.getCreatedAt());
        vo.setUpdatedAt(supplier.getUpdatedAt());
        return vo;
    }
}
