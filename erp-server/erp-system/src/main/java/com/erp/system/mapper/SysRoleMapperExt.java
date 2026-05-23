package com.erp.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.system.domain.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SysRoleMapperExt extends BaseMapper<SysRole> {
    @Select("SELECT * FROM sys_role WHERE code = #{code} LIMIT 1")
    SysRole selectByCode(@Param("code") String code);
}
