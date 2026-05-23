package com.erp.product.service;

import com.erp.product.domain.dto.ProductPackageRequest;
import com.erp.product.domain.vo.ProductPackageVO;
import java.util.List;
import java.util.UUID;

public interface ProductPackageService {
    List<ProductPackageVO> list(UUID productId);
    List<ProductPackageVO> save(UUID productId, List<ProductPackageRequest> requests);
}
