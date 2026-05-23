package com.erp.product.service.impl;

import com.erp.common.core.exception.BizException;
import com.erp.product.domain.dto.ProductPackageRequest;
import com.erp.product.domain.entity.Product;
import com.erp.product.domain.entity.ProductPackage;
import com.erp.product.domain.vo.ProductPackageVO;
import com.erp.product.mapper.ProductMapper;
import com.erp.product.mapper.ProductPackageMapper;
import com.erp.product.service.ProductPackageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductPackageServiceImpl implements ProductPackageService {
    private final ProductMapper productMapper;
    private final ProductPackageMapper productPackageMapper;
    private final ObjectMapper objectMapper;

    public ProductPackageServiceImpl(ProductMapper productMapper,
                                     ProductPackageMapper productPackageMapper,
                                     ObjectMapper objectMapper) {
        this.productMapper = productMapper;
        this.productPackageMapper = productPackageMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<ProductPackageVO> list(UUID productId) {
        ensureProductExists(productId);
        return productPackageMapper.selectByProductId(productId).stream().map(this::toVO).toList();
    }

    @Override
    @Transactional
    public List<ProductPackageVO> save(UUID productId, List<ProductPackageRequest> requests) {
        ensureProductExists(productId);
        validateRequests(requests);
        List<ProductPackage> existing = productPackageMapper.selectByProductId(productId);
        for (ProductPackage pack : existing) {
            productPackageMapper.deleteById(pack.getId());
        }
        if (requests == null) {
            return List.of();
        }
        List<ProductPackageVO> result = new ArrayList<>();
        for (ProductPackageRequest request : requests) {
            ProductPackage pack = new ProductPackage();
            pack.setId(request.getId() == null ? UUID.randomUUID() : request.getId());
            pack.setProductId(productId);
            pack.setLevel(request.getLevel());
            pack.setName(request.getName());
            pack.setQuantity(request.getQuantity());
            pack.setWeight(request.getWeight());
            pack.setDimensions(normalizeOptionalJson(request.getDimensions(), "包装尺寸"));
            pack.setBarcode(normalizeText(request.getBarcode()));
            pack.setLabelTemplateId(request.getLabelTemplateId());
            pack.setCreatedAt(OffsetDateTime.now());
            productPackageMapper.insert(pack);
            result.add(toVO(pack));
        }
        return result;
    }

    private void validateRequests(List<ProductPackageRequest> requests) {
        if (requests == null) {
            return;
        }
        Set<Integer> levels = new HashSet<>();
        for (ProductPackageRequest request : requests) {
            Integer level = request.getLevel();
            if (level == null || level < 1 || level > 3) {
                throw new BizException(10004, "包装层级只允许1-3");
            }
            if (!levels.add(level)) {
                throw new BizException(10004, "同一产品的包装层级不能重复");
            }
        }
    }

    private void ensureProductExists(UUID productId) {
        Product product = productMapper.selectById(productId);
        if (product == null || Boolean.TRUE.equals(product.getDeleted())) {
            throw new BizException(10006, "产品不存在");
        }
    }

    private ProductPackageVO toVO(ProductPackage pack) {
        ProductPackageVO vo = new ProductPackageVO();
        vo.setId(pack.getId());
        vo.setLevel(pack.getLevel());
        vo.setName(pack.getName());
        vo.setQuantity(pack.getQuantity());
        vo.setWeight(pack.getWeight());
        vo.setDimensions(pack.getDimensions());
        vo.setBarcode(pack.getBarcode());
        vo.setLabelTemplateId(pack.getLabelTemplateId());
        vo.setCreatedAt(pack.getCreatedAt());
        return vo;
    }

    private String normalizeOptionalJson(String raw, String fieldLabel) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
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
}
