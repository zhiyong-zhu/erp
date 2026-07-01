package com.erp.sales.domain.vo;

import com.erp.common.core.domain.BaseVO;
import java.time.OffsetDateTime;
import java.util.UUID;

public class CustomerVO extends BaseVO {
    private UUID id;
    private String code;
    private String name;
    private String shortName;
    private Integer customerType;
    private String contactPerson;
    private String phone;
    private String email;
    private String address;
    /** 客户等级：A=核心客户 B=潜力客户 C=普通客户 */
    private String grade;
    private Integer paymentTerms;
    private UUID salesRepId;
    private String taxNumber;
    private Integer status;
    private String remark;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
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
    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }
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
