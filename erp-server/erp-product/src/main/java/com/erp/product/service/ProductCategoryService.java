package com.erp.product.service;

import com.erp.product.domain.dto.CategoryCreateRequest;
import com.erp.product.domain.dto.CategoryUpdateRequest;
import com.erp.product.domain.vo.ProductCategoryVO;
import java.util.List;
import java.util.UUID;

public interface ProductCategoryService {
    List<ProductCategoryVO> tree();
    ProductCategoryVO create(CategoryCreateRequest request);
    ProductCategoryVO update(UUID id, CategoryUpdateRequest request);
}
