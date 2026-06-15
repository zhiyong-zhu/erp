package com.erp.sales.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.UUID;

@TableName("ecommerce_sku_mapping")
public class EcommerceSkuMapping {
    @TableId
    private UUID id;
    private UUID shopId;
    private String platformSkuId;
    private String platformProductName;
    private UUID skuId;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getShopId() { return shopId; }
    public void setShopId(UUID shopId) { this.shopId = shopId; }
    public String getPlatformSkuId() { return platformSkuId; }
    public void setPlatformSkuId(String platformSkuId) { this.platformSkuId = platformSkuId; }
    public String getPlatformProductName() { return platformProductName; }
    public void setPlatformProductName(String platformProductName) { this.platformProductName = platformProductName; }
    public UUID getSkuId() { return skuId; }
    public void setSkuId(UUID skuId) { this.skuId = skuId; }
}
