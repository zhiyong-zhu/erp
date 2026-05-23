package com.erp.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.system.domain.entity.SysRolePermission;
import java.util.List;
import java.util.UUID;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SysRolePermissionMapper extends BaseMapper<SysRolePermission> {
    @Delete("DELETE FROM sys_role_permission WHERE role_id = CAST(#{roleId} AS uuid)")
    int deleteByRoleId(@Param("roleId") UUID roleId);

    @Select("SELECT permission_id FROM sys_role_permission WHERE role_id = CAST(#{roleId} AS uuid)")
    List<UUID> selectPermissionIdsByRoleId(@Param("roleId") UUID roleId);
}
