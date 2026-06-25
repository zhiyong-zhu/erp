package com.erp.inventory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.inventory.domain.entity.InventoryLocation;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface InventoryLocationMapper extends BaseMapper<InventoryLocation> {
}
