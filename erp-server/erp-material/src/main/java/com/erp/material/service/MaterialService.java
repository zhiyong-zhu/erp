package com.erp.material.service;

import com.erp.common.core.domain.PageVO;
import com.erp.material.domain.dto.MaterialRequest;
import com.erp.material.domain.vo.MaterialVO;
import java.util.UUID;

public interface MaterialService {
    PageVO<MaterialVO> list(long pageNum, long pageSize, String name, UUID categoryId, Integer status);
    MaterialVO save(UUID id, MaterialRequest request);
}
