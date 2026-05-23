package com.erp.product.service;

import com.erp.product.domain.dto.ProductBomRequest;
import com.erp.product.domain.vo.ProductBomVO;
import java.util.UUID;

public interface ProductBomService {
    ProductBomVO get(UUID productId);
    ProductBomVO save(UUID productId, ProductBomRequest request);
}
