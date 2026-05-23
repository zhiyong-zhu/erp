package com.erp.product.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public class LabelTemplateRequest {
    private UUID id;
    @NotBlank(message = "模板名称不能为空")
    private String name;
    @NotNull(message = "标签宽度不能为空")
    private BigDecimal widthMm;
    @NotNull(message = "标签高度不能为空")
    private BigDecimal heightMm;
    @NotBlank(message = "模板配置不能为空")
    private String templateConfig;
    private String previewImage;
    private Integer status;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getWidthMm() {
        return widthMm;
    }

    public void setWidthMm(BigDecimal widthMm) {
        this.widthMm = widthMm;
    }

    public BigDecimal getHeightMm() {
        return heightMm;
    }

    public void setHeightMm(BigDecimal heightMm) {
        this.heightMm = heightMm;
    }

    public String getTemplateConfig() {
        return templateConfig;
    }

    public void setTemplateConfig(String templateConfig) {
        this.templateConfig = templateConfig;
    }

    public String getPreviewImage() {
        return previewImage;
    }

    public void setPreviewImage(String previewImage) {
        this.previewImage = previewImage;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
