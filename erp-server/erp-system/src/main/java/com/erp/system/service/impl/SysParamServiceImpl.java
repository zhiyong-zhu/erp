package com.erp.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.common.core.domain.PageVO;
import com.erp.common.core.exception.BizException;
import com.erp.system.domain.dto.SysParamUpdateRequest;
import com.erp.system.domain.entity.SysParam;
import com.erp.system.domain.vo.SysParamVO;
import com.erp.system.mapper.SysParamMapper;
import com.erp.system.service.SysParamService;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SysParamServiceImpl implements SysParamService {
    private static final String PARAM_CACHE_PREFIX = "erp:param:";
    private static final Duration CACHE_TTL = Duration.ofHours(24);

    private final SysParamMapper sysParamMapper;
    private final StringRedisTemplate stringRedisTemplate;

    public SysParamServiceImpl(SysParamMapper sysParamMapper, StringRedisTemplate stringRedisTemplate) {
        this.sysParamMapper = sysParamMapper;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public PageVO<SysParamVO> list(long pageNum, long pageSize) {
        Page<SysParam> page = sysParamMapper.selectPage(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<SysParam>().orderByAsc(SysParam::getCode));
        return new PageVO<>(
                page.getRecords().stream().map(this::toVO).collect(Collectors.toList()),
                page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public String getValue(String code) {
        String cacheKey = paramCacheKey(code);
        try {
            String cached = stringRedisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                return cached;
            }
        } catch (Exception ignored) {
            // 缓存不可用时回退到数据库查询
        }
        SysParam param = findByCode(code);
        if (param == null) {
            return null;
        }
        try {
            stringRedisTemplate.opsForValue().set(cacheKey, param.getValue(), CACHE_TTL);
        } catch (Exception ignored) {
            // 忽略缓存写入失败，保证参数读取可用
        }
        return param.getValue();
    }

    @Override
    public boolean getBoolean(String code, boolean defaultValue) {
        String raw = getValue(code);
        if (raw == null || raw.isBlank()) {
            return defaultValue;
        }
        return Boolean.parseBoolean(raw.trim());
    }

    @Override
    @Transactional
    public SysParamVO update(UUID id, SysParamUpdateRequest request) {
        SysParam param = sysParamMapper.selectById(id);
        if (param == null) {
            throw new BizException(10006, "系统参数不存在");
        }
        param.setValue(request.getValue());
        if (request.getDescription() != null) {
            param.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            param.setStatus(request.getStatus());
        }
        param.setUpdatedAt(OffsetDateTime.now());
        sysParamMapper.updateById(param);
        invalidateCache(param.getCode());
        return toVO(param);
    }

    private SysParam findByCode(String code) {
        return sysParamMapper.selectOne(new LambdaQueryWrapper<SysParam>().eq(SysParam::getCode, code));
    }

    private void invalidateCache(String code) {
        try {
            stringRedisTemplate.delete(paramCacheKey(code));
        } catch (Exception ignored) {
            // 忽略缓存失效失败
        }
    }

    private String paramCacheKey(String code) {
        return PARAM_CACHE_PREFIX + code;
    }

    private SysParamVO toVO(SysParam param) {
        SysParamVO vo = new SysParamVO();
        vo.setId(param.getId());
        vo.setCode(param.getCode());
        vo.setName(param.getName());
        vo.setValue(param.getValue());
        vo.setValueType(param.getValueType());
        vo.setDescription(param.getDescription());
        vo.setStatus(param.getStatus());
        vo.setCreatedAt(param.getCreatedAt());
        vo.setUpdatedAt(param.getUpdatedAt());
        return vo;
    }
}
