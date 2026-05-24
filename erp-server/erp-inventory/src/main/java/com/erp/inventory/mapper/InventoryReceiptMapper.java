package com.erp.inventory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.inventory.domain.entity.InventoryReceipt;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface InventoryReceiptMapper extends BaseMapper<InventoryReceipt> {
}
