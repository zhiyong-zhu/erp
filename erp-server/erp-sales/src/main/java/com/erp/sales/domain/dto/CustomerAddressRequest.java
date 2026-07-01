package com.erp.sales.domain.dto;

import com.erp.common.core.domain.BaseDTO;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public class CustomerAddressRequest extends BaseDTO {
    private UUID customerId;
    private String recipient;
    private String phone;
    @NotBlank(message = "详细地址不能为空")
    private String address;
    private Boolean isDefault;
    private String remark;

    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }
    public String getRecipient() { return recipient; }
    public void setRecipient(String recipient) { this.recipient = recipient; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public Boolean getIsDefault() { return isDefault; }
    public void setIsDefault(Boolean isDefault) { this.isDefault = isDefault; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
