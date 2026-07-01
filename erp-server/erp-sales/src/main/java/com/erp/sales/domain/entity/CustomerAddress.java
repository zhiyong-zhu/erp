package com.erp.sales.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.erp.common.core.domain.BaseEntity;
import java.util.UUID;

@TableName("customer_address")
public class CustomerAddress extends BaseEntity {
    @TableId
    private UUID id;
    private UUID customerId;
    private String recipient;
    private String phone;
    private String address;
    private Boolean isDefault;
    private String remark;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
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
