package com.erp.material.domain.vo;

import java.math.BigDecimal;

public class MaterialAlertVO extends MaterialVO {
    private BigDecimal shortageAmount;

    public BigDecimal getShortageAmount() {
        return shortageAmount;
    }

    public void setShortageAmount(BigDecimal shortageAmount) {
        this.shortageAmount = shortageAmount;
    }
}
