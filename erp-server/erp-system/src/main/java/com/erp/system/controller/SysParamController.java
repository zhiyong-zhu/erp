package com.erp.system.controller;

import com.erp.common.core.domain.PageVO;
import com.erp.common.core.domain.R;
import com.erp.system.domain.dto.SysParamUpdateRequest;
import com.erp.system.domain.vo.SysParamVO;
import com.erp.system.logging.OperationLog;
import com.erp.system.permission.SystemPermissionCodes;
import com.erp.system.service.SysParamService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/system/params")
public class SysParamController {
    private final SysParamService sysParamService;

    public SysParamController(SysParamService sysParamService) {
        this.sysParamService = sysParamService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority(T(com.erp.system.permission.SystemPermissionCodes).PARAM_LIST)")
    public R<PageVO<SysParamVO>> list(@RequestParam(defaultValue = "1") long pageNum,
                                      @RequestParam(defaultValue = "10") long pageSize) {
        return R.ok(sysParamService.list(pageNum, pageSize));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority(T(com.erp.system.permission.SystemPermissionCodes).PARAM_UPDATE)")
    @OperationLog(module = "system:param", action = "update", description = "更新系统参数")
    public R<SysParamVO> update(@PathVariable UUID id, @Valid @RequestBody SysParamUpdateRequest request) {
        return R.ok(sysParamService.update(id, request));
    }
}
