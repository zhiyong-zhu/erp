package com.erp.product.domain.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

public class LabelPrintRequest {
    @Valid
    private List<LabelPrintItemRequest> items;
    private String printerId;
    @NotBlank(message = "打印模式不能为空")
    private String printMode;

    public List<LabelPrintItemRequest> getItems() {
        return items;
    }

    public void setItems(List<LabelPrintItemRequest> items) {
        this.items = items;
    }

    public String getPrinterId() {
        return printerId;
    }

    public void setPrinterId(String printerId) {
        this.printerId = printerId;
    }

    public String getPrintMode() {
        return printMode;
    }

    public void setPrintMode(String printMode) {
        this.printMode = printMode;
    }
}
