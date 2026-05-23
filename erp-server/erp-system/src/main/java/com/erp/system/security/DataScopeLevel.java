package com.erp.system.security;

public enum DataScopeLevel {
    SELF(3),
    DEPARTMENT(2),
    ALL(1);

    private final int code;

    DataScopeLevel(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static DataScopeLevel fromCode(Integer code) {
        if (code == null) {
            return SELF;
        }
        for (DataScopeLevel value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        return SELF;
    }
}
