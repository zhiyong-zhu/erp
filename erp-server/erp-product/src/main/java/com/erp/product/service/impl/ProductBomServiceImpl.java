package com.erp.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.erp.common.core.exception.BizException;
import com.erp.product.domain.dto.ProductBomItemRequest;
import com.erp.product.domain.dto.ProductBomRequest;
import com.erp.product.domain.entity.Product;
import com.erp.product.domain.entity.ProductBom;
import com.erp.product.domain.entity.ProductBomItem;
import com.erp.product.domain.vo.ProductBomItemVO;
import com.erp.product.domain.vo.ProductBomVO;
import com.erp.product.mapper.ProductBomItemMapper;
import com.erp.product.mapper.ProductBomMapper;
import com.erp.product.mapper.ProductMapper;
import com.erp.product.service.ProductBomService;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductBomServiceImpl implements ProductBomService {
    private final ProductMapper productMapper;
    private final ProductBomMapper productBomMapper;
    private final ProductBomItemMapper productBomItemMapper;

    public ProductBomServiceImpl(ProductMapper productMapper,
                                 ProductBomMapper productBomMapper,
                                 ProductBomItemMapper productBomItemMapper) {
        this.productMapper = productMapper;
        this.productBomMapper = productBomMapper;
        this.productBomItemMapper = productBomItemMapper;
    }

    @Override
    public ProductBomVO get(UUID productId) {
        ensureProductExists(productId);
        ProductBom bom = productBomMapper.selectOne(new LambdaQueryWrapper<ProductBom>()
                .eq(ProductBom::getProductId, productId)
                .orderByDesc(ProductBom::getCreatedAt)
                .last("LIMIT 1"));
        if (bom == null) {
            return new ProductBomVO();
        }
        return toVO(bom);
    }

    @Override
    @Transactional
    public ProductBomVO save(UUID productId, ProductBomRequest request) {
        ensureProductExists(productId);
        validateBomItems(request.getItems());
        ProductBom bom = productBomMapper.selectOne(new LambdaQueryWrapper<ProductBom>()
                .eq(ProductBom::getProductId, productId)
                .orderByDesc(ProductBom::getCreatedAt)
                .last("LIMIT 1"));
        if (bom == null) {
            bom = new ProductBom();
            bom.setId(UUID.randomUUID());
            bom.setProductId(productId);
            bom.setCreatedAt(OffsetDateTime.now());
            productBomMapper.insert(bom);
        }
        bom.setVersion(request.getVersion() == null || request.getVersion().isBlank() ? "V1.0" : request.getVersion());
        bom.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        bom.setEffectiveDate(request.getEffectiveDate());
        productBomMapper.updateById(bom);

        List<ProductBomItem> existing = productBomItemMapper.selectByBomId(bom.getId());
        for (ProductBomItem item : existing) {
          productBomItemMapper.deleteById(item.getId());
        }
        if (request.getItems() != null) {
            for (ProductBomItemRequest itemRequest : request.getItems()) {
                ProductBomItem item = new ProductBomItem();
                item.setId(itemRequest.getId() == null ? UUID.randomUUID() : itemRequest.getId());
                item.setBomId(bom.getId());
                item.setMaterialId(itemRequest.getMaterialId());
                item.setMaterialType(itemRequest.getMaterialType());
                item.setQuantity(itemRequest.getQuantity());
                item.setUnit(itemRequest.getUnit());
                item.setLossRate(itemRequest.getLossRate());
                item.setRemark(itemRequest.getRemark());
                item.setSortOrder(itemRequest.getSortOrder() == null ? 0 : itemRequest.getSortOrder());
                productBomItemMapper.insert(item);
            }
        }
        return toVO(bom);
    }

    private void validateBomItems(List<ProductBomItemRequest> items) {
        if (items == null) {
            return;
        }
        if (items.size() > 10) {
            throw new BizException(10004, "BOM项数量不能超过10");
        }
        Set<UUID> materialIds = new HashSet<>();
        for (ProductBomItemRequest item : items) {
            if (!materialIds.add(item.getMaterialId())) {
                throw new BizException(10004, "BOM中存在重复物料");
            }
        }
    }

    private void ensureProductExists(UUID productId) {
        Product product = productMapper.selectById(productId);
        if (product == null || Boolean.TRUE.equals(product.getDeleted())) {
            throw new BizException(10006, "产品不存在");
        }
    }

    private ProductBomVO toVO(ProductBom bom) {
        ProductBomVO vo = new ProductBomVO();
        vo.setId(bom.getId());
        vo.setVersion(bom.getVersion());
        vo.setStatus(bom.getStatus());
        vo.setEffectiveDate(bom.getEffectiveDate());
        vo.setCreatedAt(bom.getCreatedAt());
        vo.setItems(productBomItemMapper.selectByBomId(bom.getId()).stream().map(this::toItemVO).toList());
        return vo;
    }

    private ProductBomItemVO toItemVO(ProductBomItem item) {
        ProductBomItemVO vo = new ProductBomItemVO();
        vo.setId(item.getId());
        vo.setMaterialId(item.getMaterialId());
        vo.setMaterialType(item.getMaterialType());
        vo.setQuantity(item.getQuantity());
        vo.setUnit(item.getUnit());
        vo.setLossRate(item.getLossRate());
        vo.setRemark(item.getRemark());
        vo.setSortOrder(item.getSortOrder());
        return vo;
    }
}
