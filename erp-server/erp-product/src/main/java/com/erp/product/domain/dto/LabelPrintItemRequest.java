package com.erp.product.domain.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class LabelPrintItemRequest {
    @NotNull(message = "SKU不能为空")
    private UUID skuId;
    @NotNull(message = "包装层级不能为空")
    private Integer packageLevel;
    @NotNull(message = "标签模板不能为空")
    private UUID labelTemplateId;
    @NotNull(message = "打印数量不能为空")
    @Min(value = 1, message = "打印数量最小为1")
    private Integer quantity;

    public UUID getSkuId() {
        return skuId;
    }

    public void setSkuId(UUID skuId) {
        this.skuId = skuId;
    }

    public Integer getPackageLevel() {
        return packageLevel;
    }

    public void setPackageLevel(Integer packageLevel) {
        this.packageLevel = packageLevel;
    }

    public UUID getLabelTemplateId() {
        return labelTemplateId;
    }

    public void setLabelTemplateId(UUID labelTemplateId) {
        this.labelTemplateId = labelTemplateId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
