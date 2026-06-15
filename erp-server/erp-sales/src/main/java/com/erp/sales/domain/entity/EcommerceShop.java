package com.erp.sales.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.erp.common.core.domain.BaseEntity;
import com.erp.common.mybatis.type.JsonbStringTypeHandler;
import java.time.OffsetDateTime;
import java.util.UUID;

@TableName(value = "ecommerce_shop", autoResultMap = true)
public class EcommerceShop extends BaseEntity {
    @TableId
    private UUID id;
    private String platform;
    private String shopName;
    private String shopIdOnPlatform;
    private String accessToken;
    private String refreshToken;
    private OffsetDateTime tokenExpiresAt;
    @TableField(typeHandler = JsonbStringTypeHandler.class)
    private String syncConfig;
    private Integer status;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }
    public String getShopName() { return shopName; }
    public void setShopName(String shopName) { this.shopName = shopName; }
    public String getShopIdOnPlatform() { return shopIdOnPlatform; }
    public void setShopIdOnPlatform(String shopIdOnPlatform) { this.shopIdOnPlatform = shopIdOnPlatform; }
    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    public OffsetDateTime getTokenExpiresAt() { return tokenExpiresAt; }
    public void setTokenExpiresAt(OffsetDateTime tokenExpiresAt) { this.tokenExpiresAt = tokenExpiresAt; }
    public String getSyncConfig() { return syncConfig; }
    public void setSyncConfig(String syncConfig) { this.syncConfig = syncConfig; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
