package com.erp.sales.domain;

import com.erp.common.core.exception.BizException;
import java.util.List;
import java.util.Map;

public final class SaleOrderStatusMachine {
    public static final String PENDING_CONFIRM = "PENDING_CONFIRM";
    public static final String CONFIRMED = "CONFIRMED";
    public static final String PENDING_SHIP = "PENDING_SHIP";
    public static final String PARTIAL_SHIPPED = "PARTIAL_SHIPPED";
    public static final String SHIPPED = "SHIPPED";
    public static final String COMPLETED = "COMPLETED";
    public static final String CANCELLED = "CANCELLED";
    public static final String RETURN_REQUEST = "RETURN_REQUEST";
    public static final String RETURNING = "RETURNING";
    public static final String RETURNED = "RETURNED";

    public static final String ACTION_CONFIRM = "confirm";
    public static final String ACTION_CANCEL = "cancel";
    public static final String ACTION_COMPLETE = "complete";
    public static final String ACTION_REQUEST_RETURN = "requestReturn";

    private static final Map<String, Transition> TRANSITIONS = Map.of(
            ACTION_CONFIRM, new Transition(List.of(PENDING_CONFIRM), PENDING_SHIP),
            ACTION_CANCEL, new Transition(List.of(PENDING_CONFIRM, CONFIRMED, PENDING_SHIP), CANCELLED),
            ACTION_COMPLETE, new Transition(List.of(SHIPPED), COMPLETED),
            ACTION_REQUEST_RETURN, new Transition(List.of(SHIPPED, COMPLETED), RETURN_REQUEST)
    );

    private SaleOrderStatusMachine() {
    }

    public static String next(String currentStatus, String action) {
        Transition transition = TRANSITIONS.get(action);
        if (transition == null) {
            throw new BizException(10004, "不支持的销售订单操作: " + action);
        }
        if (!transition.fromStatuses().contains(currentStatus)) {
            throw new BizException(10004, "当前订单状态不允许该操作（当前: " + label(currentStatus) + "，操作: " + actionLabel(action) + "）");
        }
        return transition.toStatus();
    }

    public static void ensureCanEdit(String currentStatus) {
        if (!PENDING_CONFIRM.equals(currentStatus)) {
            throw new BizException(10004, "只有待确认订单允许编辑");
        }
    }

    public static void ensureCanShip(String currentStatus) {
        if (!CONFIRMED.equals(currentStatus) && !PENDING_SHIP.equals(currentStatus) && !PARTIAL_SHIPPED.equals(currentStatus)) {
            throw new BizException(10004, "只有已确认或待发货订单允许发货");
        }
    }

    public static void ensureCanCreateReturn(String currentStatus) {
        if (!SHIPPED.equals(currentStatus)
                && !COMPLETED.equals(currentStatus)
                && !RETURN_REQUEST.equals(currentStatus)
                && !RETURNING.equals(currentStatus)) {
            throw new BizException(10004, "只有已发货、已完成、已申请退货或退货中订单允许创建退货单");
        }
    }

    private static String label(String status) {
        return switch (status) {
            case PENDING_CONFIRM -> "待确认";
            case CONFIRMED -> "已确认";
            case PENDING_SHIP -> "待发货";
            case PARTIAL_SHIPPED -> "部分发货";
            case SHIPPED -> "已发货";
            case COMPLETED -> "已完成";
            case CANCELLED -> "已取消";
            case RETURN_REQUEST -> "退货申请";
            case RETURNING -> "退货中";
            case RETURNED -> "已退货";
            default -> status;
        };
    }

    private static String actionLabel(String action) {
        return switch (action) {
            case ACTION_CONFIRM -> "确认";
            case ACTION_CANCEL -> "取消";
            case ACTION_COMPLETE -> "完成";
            case ACTION_REQUEST_RETURN -> "申请退货";
            default -> action;
        };
    }

    private record Transition(List<String> fromStatuses, String toStatus) {
    }
}
