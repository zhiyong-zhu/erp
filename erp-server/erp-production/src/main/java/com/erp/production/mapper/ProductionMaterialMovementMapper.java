package com.erp.production.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.production.domain.entity.ProductionMaterialMovement;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ProductionMaterialMovementMapper extends BaseMapper<ProductionMaterialMovement> {
    @Select("SELECT * FROM production_material_movement WHERE idempotency_key = #{idempotencyKey} LIMIT 1")
    ProductionMaterialMovement selectByIdempotencyKey(String idempotencyKey);
}
