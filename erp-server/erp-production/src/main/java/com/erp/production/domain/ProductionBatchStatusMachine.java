package com.erp.production.domain;

import com.erp.common.core.exception.BizException;

public final class ProductionBatchStatusMachine {
    public static final String DRAFT = "DRAFT";
    public static final String RELEASED = "RELEASED";
    public static final String IN_PROGRESS = "IN_PROGRESS";
    public static final String COMPLETED = "COMPLETED";
    public static final String CLOSED = "CLOSED";

    private ProductionBatchStatusMachine() {
    }

    public static void ensureCanStart(String status) {
        if (!DRAFT.equals(status) && !RELEASED.equals(status) && !IN_PROGRESS.equals(status)) {
            throw new BizException(10004, "只有草稿、已下达或生产中批次允许投产");
        }
    }

    public static void ensureCanReport(String status) {
        if (!IN_PROGRESS.equals(status)) {
            throw new BizException(10004, "只有生产中批次允许报工");
        }
    }

    public static void ensureCanPack(String status) {
        if (!IN_PROGRESS.equals(status) && !COMPLETED.equals(status)) {
            throw new BizException(10004, "只有生产中或已完工批次允许装箱");
        }
    }

    public static void ensureCanReceive(String status) {
        if (!COMPLETED.equals(status)) {
            throw new BizException(10004, "只有已完工批次允许完工入库");
        }
    }
}
