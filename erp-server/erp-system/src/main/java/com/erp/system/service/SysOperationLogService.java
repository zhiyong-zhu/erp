package com.erp.system.service;

import com.erp.common.core.domain.PageVO;
import com.erp.system.domain.entity.SysOperationLog;
import com.erp.system.domain.vo.OperationLogVO;

public interface SysOperationLogService {
    void save(SysOperationLog operationLog);
    PageVO<OperationLogVO> list(long pageNum, long pageSize, String module, String action, String username);
}
