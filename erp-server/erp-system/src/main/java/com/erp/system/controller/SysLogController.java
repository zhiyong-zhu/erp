package com.erp.system.controller;

import com.erp.common.core.domain.R;
import com.erp.system.domain.vo.OperationLogVO;
import com.erp.system.domain.vo.PageVO;
import com.erp.system.permission.SystemPermissionCodes;
import com.erp.system.service.SysOperationLogService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/system/logs")
public class SysLogController {
    private final SysOperationLogService sysOperationLogService;

    public SysLogController(SysOperationLogService sysOperationLogService) {
        this.sysOperationLogService = sysOperationLogService;
    }

    @GetMapping("/operations")
    @PreAuthorize("hasAuthority(T(com.erp.system.permission.SystemPermissionCodes).LOG_LIST)")
    public R<PageVO<OperationLogVO>> listOperations(@RequestParam(defaultValue = "1") long pageNum,
                                                    @RequestParam(defaultValue = "10") long pageSize,
                                                    @RequestParam(required = false) String module,
                                                    @RequestParam(required = false) String action,
                                                    @RequestParam(required = false) String username) {
        return R.ok(sysOperationLogService.list(pageNum, pageSize, module, action, username));
    }
}
