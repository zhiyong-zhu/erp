package com.erp.system.service;

import com.erp.common.core.domain.PageVO;
import com.erp.system.domain.dto.SysParamUpdateRequest;
import com.erp.system.domain.vo.SysParamVO;
import java.util.UUID;

public interface SysParamService {
    PageVO<SysParamVO> list(long pageNum, long pageSize);

    /**
     * 按编码读取参数原始值（带 Redis 缓存）。
     */
    String getValue(String code);

    /**
     * 按编码读取布尔参数，缺失或解析失败时返回 defaultValue。
     */
    boolean getBoolean(String code, boolean defaultValue);

    SysParamVO update(UUID id, SysParamUpdateRequest request);
}
