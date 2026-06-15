package com.erp.sales.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.sales.domain.entity.SaleOrderItem;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SaleOrderItemMapper extends BaseMapper<SaleOrderItem> {
    @Select("SELECT * FROM sale_order_item WHERE sale_order_id = #{saleOrderId} ORDER BY created_at ASC")
    List<SaleOrderItem> selectBySaleOrderId(java.util.UUID saleOrderId);
}
