package com.erp.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.common.core.domain.PageVO;
import com.erp.common.core.exception.BizException;
import com.erp.common.security.util.SecurityUtils;
import com.erp.product.domain.dto.ProductCreateRequest;
import com.erp.product.domain.dto.ProductSkuRequest;
import com.erp.product.domain.dto.ProductStatusUpdateRequest;
import com.erp.product.domain.dto.ProductUpdateRequest;
import com.erp.product.domain.entity.Product;
import com.erp.product.domain.entity.ProductCategory;
import com.erp.product.domain.entity.ProductSku;
import com.erp.product.domain.vo.ProductSkuVO;
import com.erp.product.domain.vo.ProductVO;
import com.erp.product.mapper.ProductCategoryMapper;
import com.erp.product.mapper.ProductMapper;
import com.erp.product.mapper.ProductSkuMapper;
import com.erp.product.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductServiceImpl implements ProductService {
    private final ProductMapper productMapper;
    private final ProductSkuMapper productSkuMapper;
    private final ProductCategoryMapper productCategoryMapper;
    private final ObjectMapper objectMapper;

    public ProductServiceImpl(ProductMapper productMapper,
                              ProductSkuMapper productSkuMapper,
                              ProductCategoryMapper productCategoryMapper,
                              ObjectMapper objectMapper) {
        this.productMapper = productMapper;
        this.productSkuMapper = productSkuMapper;
        this.productCategoryMapper = productCategoryMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public PageVO<ProductVO> list(long pageNum, long pageSize, String name, UUID categoryId, Integer status) {
        Page<Product> page = productMapper.selectPage(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<Product>()
                        .eq(Product::getDeleted, false)
                        .like(name != null && !name.isBlank(), Product::getName, name)
                        .eq(categoryId != null, Product::getCategoryId, categoryId)
                        .eq(status != null, Product::getStatus, status)
                        .orderByDesc(Product::getCreatedAt));
        List<ProductVO> records = page.getRecords().stream().map(this::toProductVOWithoutSkus).collect(Collectors.toList());
        return new PageVO<>(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public ProductVO detail(UUID id) {
        Product product = getProduct(id);
        return toProductVO(product, true);
    }

    @Override
    @Transactional
    public ProductVO create(ProductCreateRequest request) {
        validateUniqueProductCode(request.getCode(), null);
        Product product = new Product();
        product.setId(UUID.randomUUID());
        product.setCode(request.getCode());
        product.setName(request.getName());
        product.setCategoryId(request.getCategoryId());
        product.setBrand(normalizeText(request.getBrand()));
        product.setUnit(request.getUnit());
        product.setDescription(normalizeText(request.getDescription()));
        product.setImages(normalizeImages(request.getImages()));
        product.setSpecifications(normalizeOptionalJson(request.getSpecifications(), "规格定义"));
        product.setStatus(request.getStatus() == null ? 0 : request.getStatus());
        product.setCreatedBy(SecurityUtils.getUserId());
        product.setCreatedAt(OffsetDateTime.now());
        product.setUpdatedBy(SecurityUtils.getUserId());
        product.setUpdatedAt(OffsetDateTime.now());
        product.setDeleted(false);
        productMapper.insert(product);
        replaceSkus(product.getId(), request.getSkus());
        return toProductVO(product, true);
    }

    @Override
    @Transactional
    public ProductVO update(UUID id, ProductUpdateRequest request) {
        Product product = getProduct(id);
        Product updateEntity = new Product();
        updateEntity.setId(id);
        updateEntity.setName(request.getName());
        updateEntity.setCategoryId(request.getCategoryId());
        updateEntity.setBrand(normalizeText(request.getBrand()));
        updateEntity.setUnit(request.getUnit());
        updateEntity.setDescription(normalizeText(request.getDescription()));
        updateEntity.setImages(normalizeImages(request.getImages()));
        updateEntity.setSpecifications(normalizeOptionalJson(request.getSpecifications(), "规格定义"));
        updateEntity.setStatus(request.getStatus() == null ? product.getStatus() : request.getStatus());
        updateEntity.setUpdatedBy(SecurityUtils.getUserId());
        updateEntity.setUpdatedAt(OffsetDateTime.now());
        productMapper.updateById(updateEntity);

        product.setName(updateEntity.getName());
        product.setCategoryId(updateEntity.getCategoryId());
        product.setBrand(updateEntity.getBrand());
        product.setUnit(updateEntity.getUnit());
        product.setDescription(updateEntity.getDescription());
        product.setImages(updateEntity.getImages());
        product.setSpecifications(updateEntity.getSpecifications());
        product.setStatus(updateEntity.getStatus());
        product.setUpdatedBy(updateEntity.getUpdatedBy());
        product.setUpdatedAt(updateEntity.getUpdatedAt());
        replaceSkus(id, request.getSkus());
        return toProductVO(product, true);
    }

    @Override
    public void updateStatus(UUID id, ProductStatusUpdateRequest request) {
        Product product = getProduct(id);
        product.setStatus(request.getStatus());
        product.setUpdatedBy(SecurityUtils.getUserId());
        product.setUpdatedAt(OffsetDateTime.now());
        productMapper.updateById(product);
    }

    private Product getProduct(UUID id) {
        Product product = productMapper.selectById(id);
        if (product == null || Boolean.TRUE.equals(product.getDeleted())) {
            throw new BizException(10006, "产品不存在");
        }
        return product;
    }

    private void validateUniqueProductCode(String code, UUID excludedId) {
        Product existing = productMapper.selectOne(new LambdaQueryWrapper<Product>()
                .eq(Product::getCode, code)
                .eq(Product::getDeleted, false));
        if (existing != null && !existing.getId().equals(excludedId)) {
            throw new BizException(10007, "产品编码已存在");
        }
    }

    private void replaceSkus(UUID productId, List<ProductSkuRequest> skuRequests) {
        List<ProductSku> existing = productSkuMapper.selectByProductId(productId);
        for (ProductSku sku : existing) {
            productSkuMapper.deleteById(sku.getId());
        }
        if (skuRequests == null) {
            return;
        }
        for (ProductSkuRequest request : skuRequests) {
            ProductSku sku = new ProductSku();
            sku.setId(UUID.randomUUID());
            sku.setProductId(productId);
            sku.setSkuCode(request.getSkuCode());
            sku.setAttributes(normalizeRequiredJson(request.getAttributes(), "SKU属性"));
            sku.setBarcode(normalizeText(request.getBarcode()));
            sku.setPrice(request.getPrice());
            sku.setCostPrice(request.getCostPrice());
            sku.setWeight(request.getWeight());
            sku.setStatus(request.getStatus() == null ? 1 : request.getStatus());
            sku.setCreatedAt(OffsetDateTime.now());
            productSkuMapper.insert(sku);
        }
    }

    private String normalizeOptionalJson(String raw, String fieldLabel) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return validateJson(raw, fieldLabel);
    }

    private String normalizeRequiredJson(String raw, String fieldLabel) {
        if (raw == null || raw.isBlank()) {
            throw new BizException(10004, fieldLabel + "不能为空");
        }
        return validateJson(raw, fieldLabel);
    }

    private String validateJson(String raw, String fieldLabel) {
        try {
            objectMapper.readTree(raw);
            return raw;
        } catch (Exception ex) {
            throw new BizException(10004, fieldLabel + "必须是合法JSON");
        }
    }

    private String normalizeText(String raw) {
        return raw == null || raw.isBlank() ? null : raw;
    }

    private String[] normalizeImages(List<String> images) {
        if (images == null || images.isEmpty()) {
            return null;
        }
        List<String> normalized = images.stream()
                .map(this::normalizeText)
                .filter(value -> value != null && !value.isBlank())
                .toList();
        return normalized.isEmpty() ? null : normalized.toArray(String[]::new);
    }

    private ProductVO toProductVOWithoutSkus(Product product) {
        return toProductVO(product, false);
    }

    private ProductVO toProductVO(Product product, boolean includeSkus) {
        ProductVO vo = new ProductVO();
        vo.setId(product.getId());
        vo.setCode(product.getCode());
        vo.setName(product.getName());
        vo.setCategoryId(product.getCategoryId());
        if (product.getCategoryId() != null) {
            ProductCategory category = productCategoryMapper.selectById(product.getCategoryId());
            vo.setCategoryName(category == null ? null : category.getName());
        }
        vo.setBrand(product.getBrand());
        vo.setUnit(product.getUnit());
        vo.setDescription(product.getDescription());
        vo.setImages(product.getImages() == null ? new ArrayList<>() : java.util.Arrays.asList(product.getImages()));
        vo.setSpecifications(product.getSpecifications());
        vo.setStatus(product.getStatus());
        List<ProductSku> skus = productSkuMapper.selectByProductId(product.getId());
        vo.setSkuCount(skus.size());
        if (includeSkus) {
            vo.setSkus(skus.stream().map(this::toSkuVO).collect(Collectors.toList()));
        }
        vo.setCreatedAt(product.getCreatedAt());
        return vo;
    }

    private ProductSkuVO toSkuVO(ProductSku sku) {
        ProductSkuVO vo = new ProductSkuVO();
        vo.setId(sku.getId());
        vo.setSkuCode(sku.getSkuCode());
        vo.setAttributes(sku.getAttributes());
        vo.setBarcode(sku.getBarcode());
        vo.setPrice(sku.getPrice());
        vo.setCostPrice(sku.getCostPrice());
        vo.setWeight(sku.getWeight());
        vo.setStatus(sku.getStatus());
        vo.setCreatedAt(sku.getCreatedAt());
        return vo;
    }
}
