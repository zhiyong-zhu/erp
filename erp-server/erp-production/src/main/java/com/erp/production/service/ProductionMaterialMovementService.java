package com.erp.production.service;

import com.erp.common.core.domain.PageVO;
import com.erp.production.domain.dto.ProductionMaterialMovementRequest;
import com.erp.production.domain.vo.ProductionMaterialMovementVO;
import java.util.UUID;

public interface ProductionMaterialMovementService {
    ProductionMaterialMovementVO pickMaterials(ProductionMaterialMovementRequest request);
    ProductionMaterialMovementVO returnMaterials(ProductionMaterialMovementRequest request);
    PageVO<ProductionMaterialMovementVO> listMovements(long pageNum, long pageSize, UUID batchId, String movementType);
}
