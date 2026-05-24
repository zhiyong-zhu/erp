package com.erp.purchase.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.purchase.domain.entity.PurchaseOrderItem;
import java.util.List;
import java.util.UUID;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PurchaseOrderItemMapper extends BaseMapper<PurchaseOrderItem> {
    @Select("SELECT * FROM purchase_order_item WHERE purchase_order_id = #{purchaseOrderId} ORDER BY created_at ASC")
    List<PurchaseOrderItem> selectByPurchaseOrderId(UUID purchaseOrderId);
}
