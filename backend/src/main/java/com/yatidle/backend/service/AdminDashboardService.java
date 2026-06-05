package com.yatidle.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yatidle.backend.entity.Item;
import com.yatidle.backend.entity.Report;
import com.yatidle.backend.entity.TradeOrder;
import com.yatidle.backend.entity.User;
import com.yatidle.backend.entity.Wanted;
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

    public AdminDashboardService(UserMapper userMapper, ItemMapper itemMapper, WantedMapper wantedMapper,
                                 TradeOrderMapper tradeOrderMapper, ReportMapper reportMapper) {
        this.userMapper = userMapper;
        this.itemMapper = itemMapper;
        this.wantedMapper = wantedMapper;
        this.tradeOrderMapper = tradeOrderMapper;
        this.reportMapper = reportMapper;
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
}
