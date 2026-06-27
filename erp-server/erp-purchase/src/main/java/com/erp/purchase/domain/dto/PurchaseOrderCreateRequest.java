package com.erp.purchase.domain.dto;

import com.erp.common.core.domain.BaseDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public class PurchaseOrderCreateRequest extends BaseDTO {
    @NotNull(message = "供应商不能为空")
    private UUID supplierId;
    private String supplierName;
    private String remark;
    @Valid
    @NotEmpty(message = "采购明细不能为空")
    private List<PurchaseOrderItemUpdateRequest> items;

    public UUID getSupplierId() { return supplierId; }
    public void setSupplierId(UUID supplierId) { this.supplierId = supplierId; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public List<PurchaseOrderItemUpdateRequest> getItems() { return items; }
    public void setItems(List<PurchaseOrderItemUpdateRequest> items) { this.items = items; }
}
