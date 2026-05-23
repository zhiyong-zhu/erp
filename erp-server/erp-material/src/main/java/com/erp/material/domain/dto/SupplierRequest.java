package com.erp.material.domain.dto;

import com.erp.common.core.domain.BaseDTO;
import jakarta.validation.constraints.NotBlank;

public class SupplierRequest extends BaseDTO {
    @NotBlank(message = "供应商编码不能为空")
    private String code;
    @NotBlank(message = "供应商名称不能为空")
    private String name;
    private String shortName;
    private String contactPerson;
    private String phone;
    private String email;
    private String address;
    private String bankName;
    private String bankAccount;
    private String taxNumber;
    private Integer creditRating;
    private Integer status;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getShortName() { return shortName; }
    public void setShortName(String shortName) { this.shortName = shortName; }
    public String getContactPerson() { return contactPerson; }
    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }
    public String getBankAccount() { return bankAccount; }
    public void setBankAccount(String bankAccount) { this.bankAccount = bankAccount; }
    public String getTaxNumber() { return taxNumber; }
    public void setTaxNumber(String taxNumber) { this.taxNumber = taxNumber; }
    public Integer getCreditRating() { return creditRating; }
    public void setCreditRating(Integer creditRating) { this.creditRating = creditRating; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
