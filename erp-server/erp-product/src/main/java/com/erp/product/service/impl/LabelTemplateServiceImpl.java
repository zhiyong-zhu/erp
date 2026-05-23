package com.erp.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.erp.common.core.exception.BizException;
import com.erp.common.security.util.SecurityUtils;
import com.erp.product.domain.dto.LabelTemplateRequest;
import com.erp.product.domain.entity.LabelTemplate;
import com.erp.product.domain.vo.LabelTemplateVO;
import com.erp.product.mapper.LabelTemplateMapper;
import com.erp.product.service.LabelTemplateService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LabelTemplateServiceImpl implements LabelTemplateService {
    private final LabelTemplateMapper labelTemplateMapper;
    private final ObjectMapper objectMapper;

    public LabelTemplateServiceImpl(LabelTemplateMapper labelTemplateMapper,
                                    ObjectMapper objectMapper) {
        this.labelTemplateMapper = labelTemplateMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<LabelTemplateVO> list() {
        return labelTemplateMapper.selectList(new LambdaQueryWrapper<LabelTemplate>()
                .orderByDesc(LabelTemplate::getCreatedAt))
                .stream()
                .map(this::toVO)
                .toList();
    }

    @Override
    @Transactional
    public LabelTemplateVO save(LabelTemplateRequest request) {
        LabelTemplate template = request.getId() == null ? new LabelTemplate() : labelTemplateMapper.selectById(request.getId());
        if (request.getId() != null && template == null) {
            throw new BizException(10006, "标签模板不存在");
        }
        if (template.getId() == null) {
            template.setId(UUID.randomUUID());
            template.setCreatedAt(OffsetDateTime.now());
            template.setCreatedBy(SecurityUtils.getUserId());
        }
        template.setName(request.getName());
        template.setWidthMm(request.getWidthMm());
        template.setHeightMm(request.getHeightMm());
        template.setTemplateConfig(normalizeRequiredJson(request.getTemplateConfig()));
        template.setPreviewImage(normalizeText(request.getPreviewImage()));
        template.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        template.setUpdatedAt(OffsetDateTime.now());

        if (request.getId() == null) {
            labelTemplateMapper.insert(template);
        } else {
            labelTemplateMapper.updateById(template);
        }
        return toVO(template);
    }

    private String normalizeRequiredJson(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new BizException(10004, "模板配置不能为空");
        }
        try {
            objectMapper.readTree(raw);
            return raw;
        } catch (Exception ex) {
            throw new BizException(10004, "模板配置必须是合法JSON");
        }
    }

    private String normalizeText(String raw) {
        return raw == null || raw.isBlank() ? null : raw;
    }

    private LabelTemplateVO toVO(LabelTemplate template) {
        LabelTemplateVO vo = new LabelTemplateVO();
        vo.setId(template.getId());
        vo.setName(template.getName());
        vo.setWidthMm(template.getWidthMm());
        vo.setHeightMm(template.getHeightMm());
        vo.setTemplateConfig(template.getTemplateConfig());
        vo.setPreviewImage(template.getPreviewImage());
        vo.setStatus(template.getStatus());
        vo.setCreatedAt(template.getCreatedAt());
        return vo;
    }
}
