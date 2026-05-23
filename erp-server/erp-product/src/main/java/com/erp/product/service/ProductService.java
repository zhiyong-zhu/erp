package com.erp.product.service;

import com.erp.common.core.domain.PageVO;
import com.erp.product.domain.dto.ProductCreateRequest;
import com.erp.product.domain.dto.ProductStatusUpdateRequest;
import com.erp.product.domain.dto.ProductUpdateRequest;
import com.erp.product.domain.vo.ProductVO;
import java.util.UUID;

public interface ProductService {
    PageVO<ProductVO> list(long pageNum, long pageSize, String name, UUID categoryId, Integer status);
    ProductVO detail(UUID id);
    ProductVO create(ProductCreateRequest request);
    ProductVO update(UUID id, ProductUpdateRequest request);
    void updateStatus(UUID id, ProductStatusUpdateRequest request);
}
