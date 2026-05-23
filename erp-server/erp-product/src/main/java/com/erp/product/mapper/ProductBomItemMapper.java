package com.erp.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.product.domain.entity.ProductBomItem;
import java.util.List;
import java.util.UUID;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ProductBomItemMapper extends BaseMapper<ProductBomItem> {
    @Select("SELECT * FROM product_bom_item WHERE bom_id = CAST(#{bomId} AS uuid) ORDER BY sort_order ASC, id ASC")
    List<ProductBomItem> selectByBomId(@Param("bomId") UUID bomId);
}
