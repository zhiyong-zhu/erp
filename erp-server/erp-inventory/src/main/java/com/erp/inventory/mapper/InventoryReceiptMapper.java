package com.erp.inventory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.inventory.domain.entity.InventoryReceipt;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface InventoryReceiptMapper extends BaseMapper<InventoryReceipt> {
    @Select("SELECT * FROM inventory_receipt WHERE idempotency_key = #{idempotencyKey} LIMIT 1")
    InventoryReceipt selectByIdempotencyKey(String idempotencyKey);
}
