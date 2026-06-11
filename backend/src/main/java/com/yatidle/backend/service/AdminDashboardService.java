package com.yatidle.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yatidle.backend.entity.Category;
import com.yatidle.backend.entity.Item;
import com.yatidle.backend.entity.Report;
import com.yatidle.backend.entity.TradeOrder;
import com.yatidle.backend.entity.User;
import com.yatidle.backend.entity.Wanted;
import com.yatidle.backend.mapper.CategoryMapper;
import com.yatidle.backend.mapper.ItemMapper;
import com.yatidle.backend.mapper.ReportMapper;
import com.yatidle.backend.mapper.TradeOrderMapper;
import com.yatidle.backend.mapper.UserMapper;
import com.yatidle.backend.mapper.WantedMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminDashboardService {

    private final UserMapper userMapper;
    private final ItemMapper itemMapper;
    private final WantedMapper wantedMapper;
    private final TradeOrderMapper tradeOrderMapper;
    private final ReportMapper reportMapper;
    private final CategoryMapper categoryMapper;

    public AdminDashboardService(UserMapper userMapper, ItemMapper itemMapper, WantedMapper wantedMapper,
                                 TradeOrderMapper tradeOrderMapper, ReportMapper reportMapper,
                                 CategoryMapper categoryMapper) {
        this.userMapper = userMapper;
        this.itemMapper = itemMapper;
        this.wantedMapper = wantedMapper;
        this.tradeOrderMapper = tradeOrderMapper;
        this.reportMapper = reportMapper;
        this.categoryMapper = categoryMapper;
    }

    public Map<String, Object> overview() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("userTotal", userMapper.selectCount(null));
        data.put("todayNewUsers", userMapper.selectCount(new LambdaQueryWrapper<User>().ge(User::getCreateTime, todayStart)));
        data.put("itemTotal", itemMapper.selectCount(new LambdaQueryWrapper<Item>().eq(Item::getIsDeleted, 0)));
        data.put("onSaleItems", itemMapper.selectCount(new LambdaQueryWrapper<Item>().eq(Item::getIsDeleted, 0).eq(Item::getStatus, "ON_SALE")));
        data.put("wantedTotal", wantedMapper.selectCount(new LambdaQueryWrapper<Wanted>().eq(Wanted::getIsDeleted, 0)));
        data.put("orderTotal", tradeOrderMapper.selectCount(new LambdaQueryWrapper<TradeOrder>().eq(TradeOrder::getIsDeleted, 0)));
        data.put("pendingReports", reportMapper.selectCount(new LambdaQueryWrapper<Report>().eq(Report::getIsDeleted, 0).eq(Report::getStatus, "PENDING")));
        data.put("completedOrders", tradeOrderMapper.selectCount(new LambdaQueryWrapper<TradeOrder>().eq(TradeOrder::getIsDeleted, 0).eq(TradeOrder::getStatus, "COMPLETED")));
        data.put("publishTrend", lastSevenDaysItemTrend());
        data.put("orderTrend", lastSevenDaysOrderTrend());
        data.put("itemStatusStats", itemStatusStats());
        data.put("reportStatusStats", reportStatusStats());
        data.put("userStatusStats", userStatusStats());
        data.put("onSaleCategoryStats", onSaleCategoryStats());
        data.put("wantedStatusStats", wantedStatusStats());
        return data;
    }

    private List<Map<String, Object>> lastSevenDaysItemTrend() {
        List<Map<String, Object>> trend = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate day = LocalDate.now().minusDays(i);
            Long count = itemMapper.selectCount(new LambdaQueryWrapper<Item>().eq(Item::getIsDeleted, 0)
                    .ge(Item::getCreateTime, day.atStartOfDay()).lt(Item::getCreateTime, day.plusDays(1).atStartOfDay()));
            trend.add(Map.of("date", day.toString(), "count", count));
        }
        return trend;
    }

    private List<Map<String, Object>> lastSevenDaysOrderTrend() {
        List<Map<String, Object>> trend = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate day = LocalDate.now().minusDays(i);
            Long count = tradeOrderMapper.selectCount(new LambdaQueryWrapper<TradeOrder>().eq(TradeOrder::getIsDeleted, 0)
                    .ge(TradeOrder::getCreateTime, day.atStartOfDay()).lt(TradeOrder::getCreateTime, day.plusDays(1).atStartOfDay()));
            trend.add(Map.of("date", day.toString(), "count", count));
        }
        return trend;
    }

    private List<Map<String, Object>> itemStatusStats() {
        return List.of(
                stat("ON_SALE", "在售", itemMapper.selectCount(new LambdaQueryWrapper<Item>().eq(Item::getIsDeleted, 0).eq(Item::getStatus, "ON_SALE"))),
                stat("SOLD", "已售", itemMapper.selectCount(new LambdaQueryWrapper<Item>().eq(Item::getIsDeleted, 0).eq(Item::getStatus, "SOLD"))),
                stat("REMOVED", "下架", itemMapper.selectCount(new LambdaQueryWrapper<Item>().eq(Item::getIsDeleted, 0).eq(Item::getStatus, "REMOVED")))
        );
    }

    private List<Map<String, Object>> reportStatusStats() {
        return List.of(
                stat("PENDING", "待处理", reportMapper.selectCount(new LambdaQueryWrapper<Report>().eq(Report::getIsDeleted, 0).eq(Report::getStatus, "PENDING"))),
                stat("HANDLED", "已处理", reportMapper.selectCount(new LambdaQueryWrapper<Report>().eq(Report::getIsDeleted, 0).eq(Report::getStatus, "HANDLED"))),
                stat("REJECTED", "已驳回", reportMapper.selectCount(new LambdaQueryWrapper<Report>().eq(Report::getIsDeleted, 0).eq(Report::getStatus, "REJECTED")))
        );
    }

    private List<Map<String, Object>> userStatusStats() {
        return List.of(
                stat("active", "正常", userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getStatus, "active").eq(User::getRole, 0))),
                stat("inactive", "封禁", userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getStatus, "inactive").eq(User::getRole, 0))),
                stat("admin", "管理员", userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getRole, 1)))
        );
    }

    private List<Map<String, Object>> onSaleCategoryStats() {
        List<Category> categories = categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                .eq(Category::getIsDeleted, 0)
                .eq(Category::getStatus, 1)
                .orderByAsc(Category::getSortOrder)
                .orderByAsc(Category::getId));
        List<Map<String, Object>> stats = new ArrayList<>();
        for (Category category : categories) {
            Long count = itemMapper.selectCount(new LambdaQueryWrapper<Item>()
                    .eq(Item::getIsDeleted, 0)
                    .eq(Item::getStatus, "ON_SALE")
                    .eq(Item::getCategoryId, category.getId()));
            if (count != null && count > 0) {
                stats.add(stat(String.valueOf(category.getId()), category.getName(), count));
            }
        }
        return stats;
    }

    private List<Map<String, Object>> wantedStatusStats() {
        return List.of(
                stat("active", "有效", wantedMapper.selectCount(new LambdaQueryWrapper<Wanted>().eq(Wanted::getIsDeleted, 0).eq(Wanted::getStatus, "active"))),
                stat("pending", "待定", wantedMapper.selectCount(new LambdaQueryWrapper<Wanted>().eq(Wanted::getIsDeleted, 0).eq(Wanted::getStatus, "pending"))),
                stat("closed", "已关闭", wantedMapper.selectCount(new LambdaQueryWrapper<Wanted>().eq(Wanted::getIsDeleted, 0).eq(Wanted::getStatus, "closed"))),
                stat("sold", "已成交", wantedMapper.selectCount(new LambdaQueryWrapper<Wanted>().eq(Wanted::getIsDeleted, 0).eq(Wanted::getStatus, "sold")))
        );
    }

    private Map<String, Object> stat(String key, String label, Long count) {
        return Map.of("key", key, "label", label, "count", count == null ? 0L : count);
    }
}
