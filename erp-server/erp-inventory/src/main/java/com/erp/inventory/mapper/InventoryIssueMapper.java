package com.erp.inventory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.inventory.domain.entity.InventoryIssue;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface InventoryIssueMapper extends BaseMapper<InventoryIssue> {
    @Select("SELECT * FROM inventory_issue WHERE idempotency_key = #{idempotencyKey} LIMIT 1")
    InventoryIssue selectByIdempotencyKey(String idempotencyKey);
}
