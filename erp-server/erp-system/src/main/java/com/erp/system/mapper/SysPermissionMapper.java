package com.erp.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.system.domain.entity.SysPermission;
import java.util.List;
import java.util.UUID;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SysPermissionMapper extends BaseMapper<SysPermission> {
    @Select("""
            SELECT DISTINCT p.* FROM sys_permission p
            INNER JOIN sys_role_permission rp ON rp.permission_id = p.id
            INNER JOIN sys_user_role ur ON ur.role_id = rp.role_id
            WHERE ur.user_id = CAST(#{userId} AS uuid) AND p.status = 1
            """)
    List<SysPermission> selectByUserId(@Param("userId") UUID userId);

    @Select("""
            SELECT DISTINCT p.* FROM sys_permission p
            INNER JOIN sys_role_permission rp ON rp.permission_id = p.id
            WHERE rp.role_id = CAST(#{roleId} AS uuid)
            ORDER BY p.sort_order ASC, p.name ASC
            """)
    List<SysPermission> selectByRoleId(@Param("roleId") UUID roleId);
}
