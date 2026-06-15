package com.erp.sales.domain.dto;

import com.erp.common.core.domain.BaseDTO;
import jakarta.validation.constraints.NotBlank;

public class SaleOrderStatusRequest extends BaseDTO {
    @NotBlank(message = "操作类型不能为空")
    private String action;
    private String remark;

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
