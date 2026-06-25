package com.erp.inventory.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@TableName("inventory_check")
public class InventoryCheck {
    @TableId
    private UUID id;
    private String checkNo;
    private String checkType;
    private String status;
    private BigDecimal totalDifference;
    private String remark;
    private UUID createdBy;
    private OffsetDateTime createdAt;
    private UUID reviewedBy;
    private OffsetDateTime reviewedAt;
    private String reviewRemark;
    private UUID approvedBy;
    private OffsetDateTime approvedAt;
    private String approvalRemark;
    private UUID rejectedBy;
    private OffsetDateTime rejectedAt;
    private String rejectRemark;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getCheckNo() { return checkNo; }
    public void setCheckNo(String checkNo) { this.checkNo = checkNo; }
    public String getCheckType() { return checkType; }
    public void setCheckType(String checkType) { this.checkType = checkType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public BigDecimal getTotalDifference() { return totalDifference; }
    public void setTotalDifference(BigDecimal totalDifference) { this.totalDifference = totalDifference; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public UUID getCreatedBy() { return createdBy; }
    public void setCreatedBy(UUID createdBy) { this.createdBy = createdBy; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public UUID getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(UUID reviewedBy) { this.reviewedBy = reviewedBy; }
    public OffsetDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(OffsetDateTime reviewedAt) { this.reviewedAt = reviewedAt; }
    public String getReviewRemark() { return reviewRemark; }
    public void setReviewRemark(String reviewRemark) { this.reviewRemark = reviewRemark; }
    public UUID getApprovedBy() { return approvedBy; }
    public void setApprovedBy(UUID approvedBy) { this.approvedBy = approvedBy; }
    public OffsetDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(OffsetDateTime approvedAt) { this.approvedAt = approvedAt; }
    public String getApprovalRemark() { return approvalRemark; }
    public void setApprovalRemark(String approvalRemark) { this.approvalRemark = approvalRemark; }
    public UUID getRejectedBy() { return rejectedBy; }
    public void setRejectedBy(UUID rejectedBy) { this.rejectedBy = rejectedBy; }
    public OffsetDateTime getRejectedAt() { return rejectedAt; }
    public void setRejectedAt(OffsetDateTime rejectedAt) { this.rejectedAt = rejectedAt; }
    public String getRejectRemark() { return rejectRemark; }
    public void setRejectRemark(String rejectRemark) { this.rejectRemark = rejectRemark; }
}
