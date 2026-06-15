package com.erp.production.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.production.domain.entity.ProductionBomItem;
import java.util.List;
import java.util.UUID;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ProductionBomItemMapper extends BaseMapper<ProductionBomItem> {
    @Select("SELECT * FROM production_bom_item WHERE bom_id = CAST(#{bomId} AS uuid) ORDER BY id ASC")
    List<ProductionBomItem> selectByBomId(@Param("bomId") UUID bomId);
}
