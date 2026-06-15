package com.erp.production.domain.dto;

import jakarta.validation.constraints.Min;

public class SerialNumberGenerateRequest {
    @Min(1)
    private Integer quantity;
    private String prefix;

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public String getPrefix() { return prefix; }
    public void setPrefix(String prefix) { this.prefix = prefix; }
}
