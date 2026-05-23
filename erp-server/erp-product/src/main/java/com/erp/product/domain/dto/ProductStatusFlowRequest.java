package com.erp.product.domain.dto;

import jakarta.validation.constraints.NotBlank;

public class ProductStatusFlowRequest {
    @NotBlank(message = "操作不能为空")
    private String action;

    private String comment;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
