package com.erp.system.domain.vo;

import java.time.OffsetDateTime;
import java.util.UUID;

public class OperationLogVO {
    private Long id;
    private UUID userId;
    private String username;
    private String module;
    private String action;
    private String description;
    private String method;
    private String requestUrl;
    private String requestParams;
    private Integer responseCode;
    private String ip;
    private Integer duration;
    private String traceId;
    private Boolean success;
    private String errorMessage;
    private String dataScopeLevel;
    private String dataScopeSnapshot;
    private String fieldPermissionSnapshot;
    private String auditTags;
    private OffsetDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    public String getRequestParams() {
        return requestParams;
    }

    public void setRequestParams(String requestParams) {
        this.requestParams = requestParams;
    }

    public Integer getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(Integer responseCode) {
        this.responseCode = responseCode;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getDataScopeLevel() {
        return dataScopeLevel;
    }

    public void setDataScopeLevel(String dataScopeLevel) {
        this.dataScopeLevel = dataScopeLevel;
    }

    public String getDataScopeSnapshot() {
        return dataScopeSnapshot;
    }

    public void setDataScopeSnapshot(String dataScopeSnapshot) {
        this.dataScopeSnapshot = dataScopeSnapshot;
    }

    public String getFieldPermissionSnapshot() {
        return fieldPermissionSnapshot;
    }

    public void setFieldPermissionSnapshot(String fieldPermissionSnapshot) {
        this.fieldPermissionSnapshot = fieldPermissionSnapshot;
    }

    public String getAuditTags() {
        return auditTags;
    }

    public void setAuditTags(String auditTags) {
        this.auditTags = auditTags;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
