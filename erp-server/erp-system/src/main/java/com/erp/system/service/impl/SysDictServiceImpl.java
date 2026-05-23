package com.erp.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.common.core.exception.BizException;
import com.erp.system.domain.dto.DictDataCreateRequest;
import com.erp.system.domain.dto.DictDataUpdateRequest;
import com.erp.system.domain.dto.DictTypeCreateRequest;
import com.erp.system.domain.dto.DictTypeUpdateRequest;
import com.erp.system.domain.entity.SysDictData;
import com.erp.system.domain.entity.SysDictType;
import com.erp.system.domain.vo.DictDataVO;
import com.erp.system.domain.vo.DictTypeVO;
import com.erp.system.domain.vo.PageVO;
import com.erp.system.mapper.SysDictDataMapper;
import com.erp.system.mapper.SysDictTypeMapper;
import com.erp.system.service.SysDictService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SysDictServiceImpl implements SysDictService {
    private static final String DICT_CACHE_PREFIX = "erp:dict:";

    private final SysDictTypeMapper sysDictTypeMapper;
    private final SysDictDataMapper sysDictDataMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    public SysDictServiceImpl(SysDictTypeMapper sysDictTypeMapper,
                              SysDictDataMapper sysDictDataMapper,
                              StringRedisTemplate stringRedisTemplate,
                              ObjectMapper objectMapper) {
        this.sysDictTypeMapper = sysDictTypeMapper;
        this.sysDictDataMapper = sysDictDataMapper;
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public PageVO<DictTypeVO> listDictTypes(long pageNum, long pageSize) {
        Page<SysDictType> page = sysDictTypeMapper.selectPage(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<SysDictType>().orderByAsc(SysDictType::getCreatedAt));
        List<DictTypeVO> records = page.getRecords().stream().map(this::toDictTypeVO).collect(Collectors.toList());
        return new PageVO<>(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    @Transactional
    public DictTypeVO createDictType(DictTypeCreateRequest request) {
        validateTypeCode(request.getCode(), null);
        SysDictType type = new SysDictType();
        type.setId(UUID.randomUUID());
        type.setName(request.getName());
        type.setCode(request.getCode());
        type.setDescription(request.getDescription());
        type.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        type.setCreatedAt(OffsetDateTime.now());
        type.setUpdatedAt(OffsetDateTime.now());
        sysDictTypeMapper.insert(type);
        invalidateDictCache(type.getCode());
        return toDictTypeVO(type);
    }

    @Override
    @Transactional
    public DictTypeVO updateDictType(UUID id, DictTypeUpdateRequest request) {
        SysDictType type = getDictType(id);
        String oldCode = type.getCode();
        validateTypeCode(request.getCode(), id);
        type.setName(request.getName());
        type.setCode(request.getCode());
        type.setDescription(request.getDescription());
        type.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        type.setUpdatedAt(OffsetDateTime.now());
        sysDictTypeMapper.updateById(type);
        invalidateDictCache(oldCode);
        invalidateDictCache(type.getCode());
        return toDictTypeVO(type);
    }

    @Override
    public List<DictDataVO> listDictData(String code) {
        ensureDictTypeExists(code);
        String cacheKey = dictCacheKey(code);
        String cached = stringRedisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            try {
                return objectMapper.readValue(cached, new TypeReference<List<DictDataVO>>() {
                });
            } catch (Exception ignored) {
                stringRedisTemplate.delete(cacheKey);
            }
        }
        List<DictDataVO> items = sysDictDataMapper.selectByDictTypeCode(code).stream()
                .map(this::toDictDataVO)
                .collect(Collectors.toList());
        try {
            stringRedisTemplate.opsForValue().set(cacheKey, objectMapper.writeValueAsString(items), Duration.ofHours(24));
        } catch (Exception ignored) {
            // Ignore cache serialization failures to keep dictionary queries available.
        }
        return items;
    }

    @Override
    @Transactional
    public DictDataVO createDictData(String code, DictDataCreateRequest request) {
        ensureDictTypeExists(code);
        validateDictItemValue(code, request.getValue(), null);
        SysDictData data = new SysDictData();
        data.setId(UUID.randomUUID());
        data.setDictTypeCode(code);
        data.setLabel(request.getLabel());
        data.setValue(request.getValue());
        data.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        data.setCssClass(request.getCssClass());
        data.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        data.setCreatedAt(OffsetDateTime.now());
        data.setUpdatedAt(OffsetDateTime.now());
        sysDictDataMapper.insert(data);
        invalidateDictCache(code);
        return toDictDataVO(data);
    }

    @Override
    @Transactional
    public DictDataVO updateDictData(UUID id, DictDataUpdateRequest request) {
        SysDictData data = getDictData(id);
        validateDictItemValue(data.getDictTypeCode(), request.getValue(), id);
        data.setLabel(request.getLabel());
        data.setValue(request.getValue());
        data.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        data.setCssClass(request.getCssClass());
        data.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        data.setUpdatedAt(OffsetDateTime.now());
        sysDictDataMapper.updateById(data);
        invalidateDictCache(data.getDictTypeCode());
        return toDictDataVO(data);
    }

    private SysDictType getDictType(UUID id) {
        SysDictType type = sysDictTypeMapper.selectById(id);
        if (type == null) {
            throw new BizException(10006, "字典类型不存在");
        }
        return type;
    }

    private SysDictData getDictData(UUID id) {
        SysDictData data = sysDictDataMapper.selectById(id);
        if (data == null) {
            throw new BizException(10006, "字典项不存在");
        }
        return data;
    }

    private void ensureDictTypeExists(String code) {
        SysDictType type = sysDictTypeMapper.selectOne(new LambdaQueryWrapper<SysDictType>().eq(SysDictType::getCode, code));
        if (type == null) {
            throw new BizException(10006, "字典类型不存在");
        }
    }

    private void validateTypeCode(String code, UUID excludedId) {
        SysDictType existing = sysDictTypeMapper.selectOne(new LambdaQueryWrapper<SysDictType>().eq(SysDictType::getCode, code));
        if (existing != null && !existing.getId().equals(excludedId)) {
            throw new BizException(10007, "字典编码已存在");
        }
    }

    private void validateDictItemValue(String dictTypeCode, String value, UUID excludedId) {
        SysDictData existing = sysDictDataMapper.selectOne(new LambdaQueryWrapper<SysDictData>()
                .eq(SysDictData::getDictTypeCode, dictTypeCode)
                .eq(SysDictData::getValue, value));
        if (existing != null && !existing.getId().equals(excludedId)) {
            throw new BizException(10007, "字典值已存在");
        }
    }

    private void invalidateDictCache(String code) {
        stringRedisTemplate.delete(dictCacheKey(code));
    }

    private String dictCacheKey(String code) {
        return DICT_CACHE_PREFIX + code;
    }

    private DictTypeVO toDictTypeVO(SysDictType type) {
        DictTypeVO vo = new DictTypeVO();
        vo.setId(type.getId());
        vo.setName(type.getName());
        vo.setCode(type.getCode());
        vo.setDescription(type.getDescription());
        vo.setStatus(type.getStatus());
        vo.setCreatedAt(type.getCreatedAt());
        return vo;
    }

    private DictDataVO toDictDataVO(SysDictData data) {
        DictDataVO vo = new DictDataVO();
        vo.setId(data.getId());
        vo.setDictTypeCode(data.getDictTypeCode());
        vo.setLabel(data.getLabel());
        vo.setValue(data.getValue());
        vo.setSortOrder(data.getSortOrder());
        vo.setCssClass(data.getCssClass());
        vo.setStatus(data.getStatus());
        vo.setCreatedAt(data.getCreatedAt());
        return vo;
    }
}
