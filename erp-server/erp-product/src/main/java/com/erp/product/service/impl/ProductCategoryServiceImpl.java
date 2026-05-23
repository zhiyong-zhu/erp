package com.erp.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.erp.common.core.exception.BizException;
import com.erp.product.domain.dto.CategoryCreateRequest;
import com.erp.product.domain.dto.CategoryUpdateRequest;
import com.erp.product.domain.entity.ProductCategory;
import com.erp.product.domain.vo.ProductCategoryVO;
import com.erp.product.mapper.ProductCategoryMapper;
import com.erp.product.service.ProductCategoryService;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ProductCategoryServiceImpl implements ProductCategoryService {
    private final ProductCategoryMapper productCategoryMapper;

    public ProductCategoryServiceImpl(ProductCategoryMapper productCategoryMapper) {
        this.productCategoryMapper = productCategoryMapper;
    }

    @Override
    public List<ProductCategoryVO> tree() {
        List<ProductCategory> categories = productCategoryMapper.selectList(new LambdaQueryWrapper<ProductCategory>()
                .orderByAsc(ProductCategory::getSortOrder)
                .orderByAsc(ProductCategory::getCreatedAt));
        Map<UUID, ProductCategoryVO> byId = new LinkedHashMap<>();
        for (ProductCategory category : categories) {
            byId.put(category.getId(), toVO(category));
        }
        List<ProductCategoryVO> roots = new ArrayList<>();
        for (ProductCategoryVO category : byId.values()) {
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
    public ProductCategoryVO create(CategoryCreateRequest request) {
        ProductCategory category = new ProductCategory();
        category.setId(UUID.randomUUID());
        category.setParentId(request.getParentId());
        category.setName(request.getName());
        category.setCode(request.getCode());
        category.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        category.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        category.setCreatedAt(OffsetDateTime.now());
        category.setUpdatedAt(OffsetDateTime.now());
        productCategoryMapper.insert(category);
        return toVO(category);
    }

    @Override
    public ProductCategoryVO update(UUID id, CategoryUpdateRequest request) {
        ProductCategory category = productCategoryMapper.selectById(id);
        if (category == null) {
            throw new BizException(10006, "产品分类不存在");
        }
        category.setParentId(request.getParentId());
        category.setName(request.getName());
        category.setCode(request.getCode());
        category.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        category.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        category.setUpdatedAt(OffsetDateTime.now());
        productCategoryMapper.updateById(category);
        return toVO(category);
    }

    private ProductCategoryVO toVO(ProductCategory category) {
        ProductCategoryVO vo = new ProductCategoryVO();
        vo.setId(category.getId());
        vo.setParentId(category.getParentId());
        vo.setName(category.getName());
        vo.setCode(category.getCode());
        vo.setSortOrder(category.getSortOrder());
        vo.setStatus(category.getStatus());
        vo.setCreatedAt(category.getCreatedAt());
        return vo;
    }

    private void sortTree(List<ProductCategoryVO> categories) {
        categories.sort(Comparator.comparing(ProductCategoryVO::getSortOrder, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(ProductCategoryVO::getCreatedAt, Comparator.nullsLast(OffsetDateTime::compareTo)));
        for (ProductCategoryVO category : categories) {
            sortTree(category.getChildren());
        }
    }
}
