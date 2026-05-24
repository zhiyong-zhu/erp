package com.erp.purchase.domain.dto;

import com.erp.common.core.domain.BaseDTO;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.UUID;

public class PurchaseDraftGenerateRequest extends BaseDTO {
    @NotEmpty(message = "补货建议不能为空")
    private List<UUID> materialIds;
    private String remark;

    public List<UUID> getMaterialIds() {
        return materialIds;
    }

    public void setMaterialIds(List<UUID> materialIds) {
        this.materialIds = materialIds;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
