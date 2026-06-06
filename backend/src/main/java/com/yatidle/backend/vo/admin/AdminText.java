package com.yatidle.backend.vo.admin;

public final class AdminText {
    private AdminText() {
    }

    public static String actionText(String action) {
        return switch (nullToEmpty(action)) {
            case "UPDATE_USER_STATUS" -> "修改用户状态";
            case "UPDATE_USER_ROLE" -> "修改用户角色";
            case "UPDATE_ITEM_STATUS" -> "修改商品状态";
            case "DELETE_ITEM" -> "删除商品";
            case "HANDLE_REPORT" -> "处理举报";
            case "UPDATE_WANTED_STATUS" -> "修改求购状态";
            case "DELETE_WANTED" -> "删除求购";
            case "CANCEL_ORDER" -> "取消订单";
            case "DELETE_REVIEW" -> "删除评价";
            case "CREATE_CATEGORY" -> "新增分类";
            case "UPDATE_CATEGORY" -> "修改分类";
            case "DELETE_CATEGORY" -> "删除分类";
            case "CREATE" -> "创建订单";
            case "CANCEL" -> "取消订单";
            case "COMPLETE" -> "完成订单";
            default -> action == null || action.isBlank() ? "-" : action;
        };
    }

    public static String targetTypeText(String targetType) {
        return switch (nullToEmpty(targetType)) {
            case "USER" -> "用户";
            case "ITEM" -> "商品";
            case "REPORT" -> "举报";
            case "WANTED" -> "求购";
            case "ORDER" -> "订单";
            case "REVIEW" -> "评价";
            case "CATEGORY" -> "分类";
            default -> targetType == null || targetType.isBlank() ? "-" : targetType;
        };
    }

    public static String statusText(String status) {
        return statusText(status, null, null);
    }

    public static String statusText(String status, String action, String targetType) {
        String normalizedAction = nullToEmpty(action);
        String normalizedTargetType = nullToEmpty(targetType);
        String normalizedStatus = nullToEmpty(status);
        if ("UPDATE_USER_ROLE".equals(normalizedAction)) {
            return switch (normalizedStatus) {
                case "0" -> "普通用户";
                case "1" -> "管理员";
                default -> fallbackStatus(status);
            };
        }
        if ("UPDATE_CATEGORY".equals(normalizedAction) || "CATEGORY".equals(normalizedTargetType)) {
            return switch (normalizedStatus) {
                case "0" -> "禁用";
                case "1" -> "启用";
                case "DELETED" -> "已删除";
                default -> fallbackStatus(status);
            };
        }
        if ("UPDATE_USER_STATUS".equals(normalizedAction)) {
            return switch (normalizedStatus) {
                case "active" -> "正常";
                case "inactive" -> "封禁";
                default -> fallbackStatus(status);
            };
        }
        if ("UPDATE_ITEM_STATUS".equals(normalizedAction) || "ITEM".equals(normalizedTargetType)) {
            return switch (normalizedStatus) {
                case "ON_SALE" -> "在售";
                case "REMOVED" -> "已下架";
                case "SOLD" -> "已售";
                case "DELETED" -> "已删除";
                default -> fallbackStatus(status);
            };
        }
        if ("UPDATE_WANTED_STATUS".equals(normalizedAction) || "WANTED".equals(normalizedTargetType)) {
            return switch (normalizedStatus) {
                case "pending" -> "待定";
                case "active" -> "有效";
                case "closed" -> "已关闭";
                case "sold" -> "已成交";
                case "DELETED" -> "已删除";
                default -> fallbackStatus(status);
            };
        }
        return switch (nullToEmpty(status)) {
            case "active" -> "正常";
            case "inactive" -> "已封禁";
            case "0" -> "普通用户";
            case "1" -> "管理员";
            case "ON_SALE" -> "在售";
            case "SOLD" -> "已售出";
            case "REMOVED" -> "已下架";
            case "PENDING" -> "待交易";
            case "COMPLETED" -> "已完成";
            case "CANCELLED" -> "已取消";
            case "HANDLED" -> "已处理";
            case "REJECTED" -> "已驳回";
            case "pending" -> "待审核";
            case "active_wanted", "activeWanted" -> "展示中";
            case "closed" -> "已关闭";
            case "DELETED" -> "已删除";
            case "ACTIVE" -> "有效";
            default -> status == null || status.isBlank() ? "-" : status;
        };
    }

    private static String fallbackStatus(String status) {
        return status == null || status.isBlank() ? "-" : status;
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
