package com.erp.material.service;

import com.erp.material.domain.dto.MaterialCategoryRequest;
import com.erp.material.domain.vo.MaterialCategoryVO;
import java.util.List;
import java.util.UUID;

public interface MaterialCategoryService {
    List<MaterialCategoryVO> tree();
    MaterialCategoryVO save(UUID id, MaterialCategoryRequest request);
}
