package com.erp.system.service;

import com.erp.system.domain.entity.SysOperationLog;
import com.erp.system.domain.vo.OperationLogVO;
import com.erp.system.domain.vo.PageVO;

public interface SysOperationLogService {
    void save(SysOperationLog operationLog);
    PageVO<OperationLogVO> list(long pageNum, long pageSize, String module, String action, String username);
}
