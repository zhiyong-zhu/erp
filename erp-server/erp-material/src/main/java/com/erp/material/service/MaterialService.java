package com.erp.material.service;

import com.erp.common.core.domain.PageVO;
import com.erp.material.domain.dto.MaterialRequest;
import com.erp.material.domain.vo.MaterialAlertVO;
import com.erp.material.domain.vo.MaterialReplenishmentVO;
import com.erp.material.domain.vo.MaterialVO;
import java.io.ByteArrayInputStream;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

public interface MaterialService {
    PageVO<MaterialVO> list(long pageNum, long pageSize, String name, UUID categoryId, Integer status);

    MaterialVO save(UUID id, MaterialRequest request);

    PageVO<MaterialAlertVO> listAlerts(long pageNum, long pageSize, String name);

    PageVO<MaterialReplenishmentVO> listReplenishmentSuggestions(long pageNum, long pageSize, String name);

    ByteArrayInputStream exportMaterials();

    void importMaterials(MultipartFile file);
}
