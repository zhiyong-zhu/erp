package com.erp.sales.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.sales.domain.entity.ShippingOrder;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ShippingOrderMapper extends BaseMapper<ShippingOrder> {
    @Select("SELECT * FROM shipping_order WHERE sale_order_id = #{saleOrderId}")
    List<ShippingOrder> selectBySaleOrderId(java.util.UUID saleOrderId);
}
