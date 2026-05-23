package com.erp.system.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.erp.common.security.domain.LoginUser;
import com.erp.common.security.util.SecurityUtils;
import com.erp.system.domain.entity.SysDepartment;
import com.erp.system.domain.entity.SysRole;
import com.erp.system.domain.entity.SysUser;
import com.erp.system.mapper.SysDepartmentMapper;
import com.erp.system.mapper.SysRoleMapper;
import com.erp.system.mapper.SysUserMapper;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class DataScopeServiceImpl implements DataScopeService {
    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysDepartmentMapper sysDepartmentMapper;

    public DataScopeServiceImpl(SysUserMapper sysUserMapper,
                                SysRoleMapper sysRoleMapper,
                                SysDepartmentMapper sysDepartmentMapper) {
        this.sysUserMapper = sysUserMapper;
        this.sysRoleMapper = sysRoleMapper;
        this.sysDepartmentMapper = sysDepartmentMapper;
    }

    @Override
    public DataScopeContext resolveCurrentScope() {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser == null) {
            return new DataScopeContext(DataScopeLevel.SELF, null, Set.of());
        }
        List<SysRole> roles = sysRoleMapper.selectByUserId(loginUser.getUserId());
        DataScopeLevel level = resolveRoleScope(roles);
        if (level == DataScopeLevel.ALL) {
            return new DataScopeContext(level, loginUser.getUserId(), Set.of());
        }
        if (level == DataScopeLevel.SELF) {
            return new DataScopeContext(level, loginUser.getUserId(), Set.of());
        }
        SysUser user = sysUserMapper.selectById(loginUser.getUserId());
        Set<UUID> departmentIds = user == null || user.getDepartmentId() == null
                ? Set.of()
                : collectDepartmentIds(user.getDepartmentId());
        return new DataScopeContext(level, loginUser.getUserId(), departmentIds);
    }

    private DataScopeLevel resolveRoleScope(List<SysRole> roles) {
        boolean hasDepartment = false;
        for (SysRole role : roles) {
            DataScopeLevel current = DataScopeLevel.fromCode(role.getDataScope());
            if (current == DataScopeLevel.ALL) {
                return DataScopeLevel.ALL;
            }
            if (current == DataScopeLevel.DEPARTMENT) {
                hasDepartment = true;
            }
        }
        return hasDepartment ? DataScopeLevel.DEPARTMENT : DataScopeLevel.SELF;
    }

    private Set<UUID> collectDepartmentIds(UUID rootDepartmentId) {
        List<SysDepartment> departments = sysDepartmentMapper.selectList(new LambdaQueryWrapper<SysDepartment>()
                .eq(SysDepartment::getStatus, 1));
        Set<UUID> result = new HashSet<>();
        Queue<UUID> queue = new ArrayDeque<>();
        queue.add(rootDepartmentId);
        while (!queue.isEmpty()) {
            UUID current = queue.poll();
            if (!result.add(current)) {
                continue;
            }
            for (SysDepartment department : departments) {
                if (current.equals(department.getParentId())) {
                    queue.add(department.getId());
                }
            }
        }
        return result;
    }
}
