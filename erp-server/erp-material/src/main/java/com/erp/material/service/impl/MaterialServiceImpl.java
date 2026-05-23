package com.erp.material.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.common.core.domain.PageVO;
import com.erp.common.core.exception.BizException;
import com.erp.material.domain.dto.MaterialRequest;
import com.erp.material.domain.entity.Material;
import com.erp.material.domain.entity.MaterialCategory;
import com.erp.material.domain.entity.Supplier;
import com.erp.material.domain.vo.MaterialVO;
import com.erp.material.mapper.MaterialCategoryMapper;
import com.erp.material.mapper.MaterialMapper;
import com.erp.material.mapper.SupplierMapper;
import com.erp.material.service.MaterialService;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class MaterialServiceImpl implements MaterialService {
    private final MaterialMapper materialMapper;
    private final MaterialCategoryMapper materialCategoryMapper;
    private final SupplierMapper supplierMapper;

    public MaterialServiceImpl(MaterialMapper materialMapper,
                               MaterialCategoryMapper materialCategoryMapper,
                               SupplierMapper supplierMapper) {
        this.materialMapper = materialMapper;
        this.materialCategoryMapper = materialCategoryMapper;
        this.supplierMapper = supplierMapper;
    }

    @Override
    public PageVO<MaterialVO> list(long pageNum, long pageSize, String name, UUID categoryId, Integer status) {
        Page<Material> page = materialMapper.selectPage(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<Material>()
                        .like(name != null && !name.isBlank(), Material::getName, name)
                        .eq(categoryId != null, Material::getCategoryId, categoryId)
                        .eq(status != null, Material::getStatus, status)
                        .orderByDesc(Material::getCreatedAt));
        List<MaterialVO> records = page.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return new PageVO<>(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public MaterialVO save(UUID id, MaterialRequest request) {
        Material material = id == null ? new Material() : materialMapper.selectById(id);
        if (id != null && material == null) {
            throw new BizException(10006, "原料不存在");
        }
        if (material.getId() == null) {
            material.setId(UUID.randomUUID());
            material.setCreatedAt(OffsetDateTime.now());
        }
        material.setCode(request.getCode());
        material.setName(request.getName());
        material.setCategoryId(request.getCategoryId());
        material.setUnit(request.getUnit());
        material.setSpecifications(request.getSpecifications());
        material.setDefaultSupplierId(request.getDefaultSupplierId());
        material.setSafetyStock(request.getSafetyStock());
        material.setLeadTimeDays(request.getLeadTimeDays());
        material.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        material.setUpdatedAt(OffsetDateTime.now());
        if (id == null) {
            materialMapper.insert(material);
        } else {
            materialMapper.updateById(material);
        }
        return toVO(material);
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
        vo.setLeadTimeDays(material.getLeadTimeDays());
        vo.setStatus(material.getStatus());
        vo.setCreatedAt(material.getCreatedAt());
        vo.setUpdatedAt(material.getUpdatedAt());
        return vo;
    }
}
