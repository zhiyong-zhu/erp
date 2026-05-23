package com.erp.product.domain.dto;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;

public class ProductBomRequest {
    private String version;
    private Integer status;
    private LocalDate effectiveDate;
    @Valid
    private List<ProductBomItemRequest> items;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public LocalDate getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(LocalDate effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public List<ProductBomItemRequest> getItems() {
        return items;
    }

    public void setItems(List<ProductBomItemRequest> items) {
        this.items = items;
    }
}
