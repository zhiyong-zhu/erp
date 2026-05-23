package com.erp.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.erp.common.core.exception.BizException;
import com.erp.system.domain.dto.DepartmentCreateRequest;
import com.erp.system.domain.dto.DepartmentUpdateRequest;
import com.erp.system.domain.dto.StatusUpdateRequest;
import com.erp.system.domain.entity.SysDepartment;
import com.erp.system.domain.vo.DepartmentVO;
import com.erp.system.mapper.SysDepartmentMapper;
import com.erp.system.service.SysDepartmentService;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class SysDepartmentServiceImpl implements SysDepartmentService {
    private final SysDepartmentMapper sysDepartmentMapper;

    public SysDepartmentServiceImpl(SysDepartmentMapper sysDepartmentMapper) {
        this.sysDepartmentMapper = sysDepartmentMapper;
    }

    @Override
    public List<DepartmentVO> listDepartments() {
        List<SysDepartment> departments = sysDepartmentMapper.selectList(new LambdaQueryWrapper<SysDepartment>()
                .orderByAsc(SysDepartment::getSortOrder)
                .orderByAsc(SysDepartment::getCreatedAt));
        Map<UUID, DepartmentVO> byId = new LinkedHashMap<>();
        for (SysDepartment department : departments) {
            byId.put(department.getId(), toDepartmentVO(department));
        }
        List<DepartmentVO> roots = new ArrayList<>();
        for (DepartmentVO department : byId.values()) {
            if (department.getParentId() != null && byId.containsKey(department.getParentId())) {
                byId.get(department.getParentId()).getChildren().add(department);
            } else {
                roots.add(department);
            }
        }
        sortDepartments(roots);
        return roots;
    }

    @Override
    public DepartmentVO createDepartment(DepartmentCreateRequest request) {
        validateUniqueCode(request.getCode(), null);
        SysDepartment department = new SysDepartment();
        department.setId(UUID.randomUUID());
        department.setParentId(request.getParentId());
        department.setName(request.getName());
        department.setCode(request.getCode());
        department.setLeader(request.getLeader());
        department.setPhone(request.getPhone());
        department.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        department.setStatus(1);
        department.setCreatedAt(OffsetDateTime.now());
        department.setUpdatedAt(OffsetDateTime.now());
        sysDepartmentMapper.insert(department);
        return toDepartmentVO(department);
    }

    @Override
    public DepartmentVO updateDepartment(UUID id, DepartmentUpdateRequest request) {
        SysDepartment department = getDepartment(id);
        if (request.getCode() != null) {
            validateUniqueCode(request.getCode(), id);
            department.setCode(request.getCode());
        }
        department.setParentId(request.getParentId());
        if (request.getName() != null) {
            department.setName(request.getName());
        }
        department.setLeader(request.getLeader());
        department.setPhone(request.getPhone());
        department.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        department.setUpdatedAt(OffsetDateTime.now());
        sysDepartmentMapper.updateById(department);
        return toDepartmentVO(department);
    }

    @Override
    public void updateStatus(UUID id, StatusUpdateRequest request) {
        SysDepartment department = getDepartment(id);
        department.setStatus(request.getStatus());
        department.setUpdatedAt(OffsetDateTime.now());
        sysDepartmentMapper.updateById(department);
    }

    private SysDepartment getDepartment(UUID id) {
        SysDepartment department = sysDepartmentMapper.selectById(id);
        if (department == null) {
            throw new BizException(10006, "部门不存在");
        }
        return department;
    }

    private void validateUniqueCode(String code, UUID excludedId) {
        SysDepartment existing = sysDepartmentMapper.selectOne(new LambdaQueryWrapper<SysDepartment>().eq(SysDepartment::getCode, code));
        if (existing != null && !existing.getId().equals(excludedId)) {
            throw new BizException(10007, "部门编码已存在");
        }
    }

    private DepartmentVO toDepartmentVO(SysDepartment department) {
        DepartmentVO vo = new DepartmentVO();
        vo.setId(department.getId());
        vo.setParentId(department.getParentId());
        vo.setName(department.getName());
        vo.setCode(department.getCode());
        vo.setLeader(department.getLeader());
        vo.setPhone(department.getPhone());
        vo.setSortOrder(department.getSortOrder());
        vo.setStatus(department.getStatus());
        vo.setCreatedAt(department.getCreatedAt());
        return vo;
    }

    private void sortDepartments(List<DepartmentVO> departments) {
        departments.sort(Comparator.comparing(DepartmentVO::getSortOrder, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(DepartmentVO::getCreatedAt, Comparator.nullsLast(OffsetDateTime::compareTo)));
        for (DepartmentVO department : departments) {
            sortDepartments(department.getChildren());
        }
    }
}
