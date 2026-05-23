package com.erp.product.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public class ProductPackageRequest {
    private UUID id;
    @NotNull(message = "包装层级不能为空")
    @Min(value = 1, message = "包装层级最小为1")
    private Integer level;
    @NotBlank(message = "包装名称不能为空")
    private String name;
    @NotNull(message = "装入数量不能为空")
    @Min(value = 1, message = "装入数量最小为1")
    private Integer quantity;
    private BigDecimal weight;
    private String dimensions;
    private String barcode;
    private UUID labelTemplateId;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }

    public String getDimensions() {
        return dimensions;
    }

    public void setDimensions(String dimensions) {
        this.dimensions = dimensions;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public UUID getLabelTemplateId() {
        return labelTemplateId;
    }

    public void setLabelTemplateId(UUID labelTemplateId) {
        this.labelTemplateId = labelTemplateId;
    }
}
