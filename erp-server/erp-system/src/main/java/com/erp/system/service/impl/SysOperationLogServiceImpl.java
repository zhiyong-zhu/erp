package com.erp.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.system.domain.entity.SysOperationLog;
import com.erp.system.domain.vo.OperationLogVO;
import com.erp.system.domain.vo.PageVO;
import com.erp.system.mapper.SysOperationLogMapper;
import com.erp.system.service.SysOperationLogService;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class SysOperationLogServiceImpl implements SysOperationLogService {
    private final SysOperationLogMapper sysOperationLogMapper;

    public SysOperationLogServiceImpl(SysOperationLogMapper sysOperationLogMapper) {
        this.sysOperationLogMapper = sysOperationLogMapper;
    }

    @Override
    public void save(SysOperationLog operationLog) {
        sysOperationLogMapper.insert(operationLog);
    }

    @Override
    public PageVO<OperationLogVO> list(long pageNum, long pageSize, String module, String action, String username) {
        Page<SysOperationLog> page = sysOperationLogMapper.selectPage(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<SysOperationLog>()
                        .like(module != null && !module.isBlank(), SysOperationLog::getModule, module)
                        .like(action != null && !action.isBlank(), SysOperationLog::getAction, action)
                        .like(username != null && !username.isBlank(), SysOperationLog::getUsername, username)
                        .orderByDesc(SysOperationLog::getCreatedAt));
        List<OperationLogVO> records = page.getRecords().stream().map(this::toOperationLogVO).collect(Collectors.toList());
        return new PageVO<>(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    private OperationLogVO toOperationLogVO(SysOperationLog log) {
        OperationLogVO vo = new OperationLogVO();
        vo.setId(log.getId());
        vo.setUserId(log.getUserId());
        vo.setUsername(log.getUsername());
        vo.setModule(log.getModule());
        vo.setAction(log.getAction());
        vo.setDescription(log.getDescription());
        vo.setMethod(log.getMethod());
        vo.setRequestUrl(log.getRequestUrl());
        vo.setRequestParams(log.getRequestParams());
        vo.setResponseCode(log.getResponseCode());
        vo.setIp(log.getIp());
        vo.setDuration(log.getDuration());
        vo.setTraceId(log.getTraceId());
        vo.setSuccess(log.getSuccess());
        vo.setErrorMessage(log.getErrorMessage());
        vo.setDataScopeLevel(log.getDataScopeLevel());
        vo.setDataScopeSnapshot(log.getDataScopeSnapshot());
        vo.setFieldPermissionSnapshot(log.getFieldPermissionSnapshot());
        vo.setAuditTags(log.getAuditTags());
        vo.setCreatedAt(log.getCreatedAt());
        return vo;
    }
}
