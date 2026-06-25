package com.erp.production.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.production.domain.entity.ProductionMaterialMovementItem;
import java.util.List;
import java.util.UUID;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ProductionMaterialMovementItemMapper extends BaseMapper<ProductionMaterialMovementItem> {
    @Select("SELECT * FROM production_material_movement_item WHERE movement_id = #{movementId} ORDER BY id ASC")
    List<ProductionMaterialMovementItem> selectByMovementId(UUID movementId);
}
