package com.erp.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.system.domain.entity.SysRole;
import java.util.List;
import java.util.UUID;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {
    @Select("""
            SELECT r.* FROM sys_role r
            INNER JOIN sys_user_role ur ON ur.role_id = r.id
            WHERE ur.user_id = CAST(#{userId} AS uuid)
            """)
    List<SysRole> selectByUserId(@Param("userId") UUID userId);
}
