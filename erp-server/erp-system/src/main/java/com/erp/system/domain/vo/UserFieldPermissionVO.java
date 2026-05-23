package com.erp.system.domain.vo;

public class UserFieldPermissionVO {
    private boolean canViewSensitiveFields;
    private boolean canEditSensitiveFields;

    public boolean isCanViewSensitiveFields() {
        return canViewSensitiveFields;
    }

    public void setCanViewSensitiveFields(boolean canViewSensitiveFields) {
        this.canViewSensitiveFields = canViewSensitiveFields;
    }

    public boolean isCanEditSensitiveFields() {
        return canEditSensitiveFields;
    }

    public void setCanEditSensitiveFields(boolean canEditSensitiveFields) {
        this.canEditSensitiveFields = canEditSensitiveFields;
    }
}
