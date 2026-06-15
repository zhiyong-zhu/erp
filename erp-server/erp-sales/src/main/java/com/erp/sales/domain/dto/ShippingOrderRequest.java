package com.erp.sales.domain.dto;

import com.erp.common.core.domain.BaseDTO;

public class ShippingOrderRequest extends BaseDTO {
    private String carrierCode;
    private String carrierName;
    private String trackingNumber;
    private String remark;

    public String getCarrierCode() { return carrierCode; }
    public void setCarrierCode(String carrierCode) { this.carrierCode = carrierCode; }
    public String getCarrierName() { return carrierName; }
    public void setCarrierName(String carrierName) { this.carrierName = carrierName; }
    public String getTrackingNumber() { return trackingNumber; }
    public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
