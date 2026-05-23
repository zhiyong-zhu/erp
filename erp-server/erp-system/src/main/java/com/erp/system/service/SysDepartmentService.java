package com.erp.system.service;

import com.erp.system.domain.dto.DepartmentCreateRequest;
import com.erp.system.domain.dto.DepartmentUpdateRequest;
import com.erp.system.domain.dto.StatusUpdateRequest;
import com.erp.system.domain.vo.DepartmentVO;
import java.util.List;
import java.util.UUID;

public interface SysDepartmentService {
    List<DepartmentVO> listDepartments();
    DepartmentVO createDepartment(DepartmentCreateRequest request);
    DepartmentVO updateDepartment(UUID id, DepartmentUpdateRequest request);
    void updateStatus(UUID id, StatusUpdateRequest request);
}
