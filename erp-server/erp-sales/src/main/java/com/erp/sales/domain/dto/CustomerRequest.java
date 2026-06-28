package com.erp.sales.domain.dto;

import com.erp.common.core.domain.BaseDTO;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.UUID;

public class CustomerRequest extends BaseDTO {
    // 编码可为空：留空时由后端自动生成（C0001 格式）
    private String code;
    @NotBlank(message = "客户名称不能为空")
    private String name;
    private String shortName;
    private Integer customerType;
    private String contactPerson;
    private String phone;
    private String email;
    private String address;
    private BigDecimal creditLimit;
    private Integer paymentTerms;
    private UUID salesRepId;
    private String taxNumber;
    private Integer status;
    private String remark;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getShortName() { return shortName; }
    public void setShortName(String shortName) { this.shortName = shortName; }
    public Integer getCustomerType() { return customerType; }
    public void setCustomerType(Integer customerType) { this.customerType = customerType; }
    public String getContactPerson() { return contactPerson; }
    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public BigDecimal getCreditLimit() { return creditLimit; }
    public void setCreditLimit(BigDecimal creditLimit) { this.creditLimit = creditLimit; }
    public Integer getPaymentTerms() { return paymentTerms; }
    public void setPaymentTerms(Integer paymentTerms) { this.paymentTerms = paymentTerms; }
    public UUID getSalesRepId() { return salesRepId; }
    public void setSalesRepId(UUID salesRepId) { this.salesRepId = salesRepId; }
    public String getTaxNumber() { return taxNumber; }
    public void setTaxNumber(String taxNumber) { this.taxNumber = taxNumber; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
