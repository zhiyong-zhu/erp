package com.erp.material.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.erp.common.core.exception.BizException;
import com.erp.common.security.util.SecurityUtils;
import com.erp.material.domain.dto.MaterialCategoryRequest;
import com.erp.material.domain.entity.MaterialCategory;
import com.erp.material.domain.vo.MaterialCategoryVO;
import com.erp.material.mapper.MaterialCategoryMapper;
import com.erp.material.service.MaterialCategoryService;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class MaterialCategoryServiceImpl implements MaterialCategoryService {
    private final MaterialCategoryMapper materialCategoryMapper;

    public MaterialCategoryServiceImpl(MaterialCategoryMapper materialCategoryMapper) {
        this.materialCategoryMapper = materialCategoryMapper;
    }

    @Override
    public List<MaterialCategoryVO> tree() {
        List<MaterialCategory> categories = materialCategoryMapper.selectList(new LambdaQueryWrapper<MaterialCategory>()
                .orderByAsc(MaterialCategory::getSortOrder)
                .orderByAsc(MaterialCategory::getCreatedAt));
        Map<UUID, MaterialCategoryVO> byId = new LinkedHashMap<>();
        for (MaterialCategory category : categories) {
            byId.put(category.getId(), toVO(category));
        }
        List<MaterialCategoryVO> roots = new ArrayList<>();
        for (MaterialCategoryVO category : byId.values()) {
            if (category.getParentId() != null && byId.containsKey(category.getParentId())) {
                byId.get(category.getParentId()).getChildren().add(category);
            } else {
                roots.add(category);
            }
        }
        sortTree(roots);
        return roots;
    }

    @Override
    public MaterialCategoryVO save(UUID id, MaterialCategoryRequest request) {
        MaterialCategory category = id == null ? new MaterialCategory() : materialCategoryMapper.selectById(id);
        if (id != null && category == null) {
            throw new BizException(10006, "原料分类不存在");
        }
        if (category.getId() == null) {
            category.setId(UUID.randomUUID());
            category.setCreatedBy(SecurityUtils.getUserId());
            category.setCreatedAt(OffsetDateTime.now());
        }
        category.setParentId(request.getParentId());
        category.setName(request.getName());
        category.setCode(request.getCode());
        category.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        category.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        category.setUpdatedBy(SecurityUtils.getUserId());
        category.setUpdatedAt(OffsetDateTime.now());
        if (id == null) {
            materialCategoryMapper.insert(category);
        } else {
            materialCategoryMapper.updateById(category);
        }
        return toVO(category);
    }

    private MaterialCategoryVO toVO(MaterialCategory category) {
        MaterialCategoryVO vo = new MaterialCategoryVO();
        vo.setId(category.getId());
        vo.setParentId(category.getParentId());
        vo.setName(category.getName());
        vo.setCode(category.getCode());
        vo.setSortOrder(category.getSortOrder());
        vo.setStatus(category.getStatus());
        vo.setCreatedAt(category.getCreatedAt());
        vo.setUpdatedAt(category.getUpdatedAt());
        return vo;
    }

    private void sortTree(List<MaterialCategoryVO> categories) {
        categories.sort(Comparator.comparing(MaterialCategoryVO::getSortOrder, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(MaterialCategoryVO::getCreatedAt, Comparator.nullsLast(OffsetDateTime::compareTo)));
        for (MaterialCategoryVO category : categories) {
            sortTree(category.getChildren());
        }
    }
}
