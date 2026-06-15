package com.erp.production.domain.vo;

import com.erp.common.core.domain.BaseVO;
import java.math.BigDecimal;
import java.util.UUID;

public class ProductionProductStockVO extends BaseVO {
    private UUID id;
    private UUID productId;
    private String productCode;
    private String productName;
    private BigDecimal currentStock;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }
    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public BigDecimal getCurrentStock() { return currentStock; }
    public void setCurrentStock(BigDecimal currentStock) { this.currentStock = currentStock; }
}
