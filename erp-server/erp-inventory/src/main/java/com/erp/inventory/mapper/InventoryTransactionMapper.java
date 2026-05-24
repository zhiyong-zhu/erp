package com.erp.inventory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.inventory.domain.entity.InventoryTransaction;
import java.util.List;
import java.util.UUID;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface InventoryTransactionMapper extends BaseMapper<InventoryTransaction> {
    @Select("SELECT * FROM inventory_transaction WHERE source_order_id = #{sourceOrderId} ORDER BY created_at ASC")
    List<InventoryTransaction> selectBySourceOrderId(UUID sourceOrderId);
}
