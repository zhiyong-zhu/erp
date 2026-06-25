package com.erp.sales.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.sales.domain.entity.ShippingOrderItem;
import java.util.List;
import java.util.UUID;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ShippingOrderItemMapper extends BaseMapper<ShippingOrderItem> {
    @Select("SELECT * FROM shipping_order_item WHERE shipping_order_id = #{shippingOrderId} ORDER BY created_at ASC")
    List<ShippingOrderItem> selectByShippingOrderId(UUID shippingOrderId);
}
