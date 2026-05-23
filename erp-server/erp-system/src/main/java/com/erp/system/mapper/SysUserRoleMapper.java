package com.erp.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.system.domain.entity.SysUserRole;

import java.util.UUID;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {
    @Delete("DELETE FROM sys_user_role WHERE user_id = CAST(#{userId} AS uuid)")
    int deleteByUserId(@Param("userId") UUID userId);

    @Select("SELECT role_id FROM sys_user_role WHERE user_id = CAST(#{userId} AS uuid)")
    java.util.List<UUID> selectRoleIdsByUserId(@Param("userId") UUID userId);
}
