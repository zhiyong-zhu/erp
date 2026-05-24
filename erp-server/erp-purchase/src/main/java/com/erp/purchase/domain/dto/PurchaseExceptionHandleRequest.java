package com.erp.purchase.domain.dto;

import com.erp.common.core.domain.BaseDTO;
import jakarta.validation.constraints.NotBlank;

public class PurchaseExceptionHandleRequest extends BaseDTO {
    @NotBlank(message = "处理动作不能为空")
    private String action;
    private String resolution;

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getResolution() { return resolution; }
    public void setResolution(String resolution) { this.resolution = resolution; }
}
