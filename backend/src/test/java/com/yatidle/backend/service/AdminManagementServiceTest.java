package com.yatidle.backend.service;

import com.yatidle.backend.entity.AdminActionLog;
import com.yatidle.backend.entity.Category;
import com.yatidle.backend.entity.Item;
import com.yatidle.backend.entity.Report;
import com.yatidle.backend.entity.TradeOrder;
import com.yatidle.backend.entity.User;
import com.yatidle.backend.entity.Wanted;
import com.yatidle.backend.common.exception.BusinessException;
import com.yatidle.backend.mapper.AdminActionLogMapper;
import com.yatidle.backend.mapper.CategoryMapper;
import com.yatidle.backend.mapper.ItemMapper;
import com.yatidle.backend.mapper.ReportMapper;
import com.yatidle.backend.mapper.TradeOrderLogMapper;
import com.yatidle.backend.mapper.TradeOrderMapper;
import com.yatidle.backend.mapper.UserMapper;
import com.yatidle.backend.mapper.WantedMapper;
import com.yatidle.backend.vo.admin.AdminActionLogVO;
import com.yatidle.backend.vo.admin.AdminOrderVO;
import com.yatidle.backend.vo.admin.AdminReportVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminManagementServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private ItemMapper itemMapper;

    @Mock
    private ReportMapper reportMapper;

    @Mock
    private WantedMapper wantedMapper;

    @Mock
    private TradeOrderMapper tradeOrderMapper;

    @Mock
    private TradeOrderLogMapper tradeOrderLogMapper;

    @Mock
    private AdminActionLogMapper adminActionLogMapper;

    @Mock
    private CategoryMapper categoryMapper;

    @Test
    void updateUserStatusChangesStatusAndWritesLog() {
        AdminLogService logService = new AdminLogService(adminActionLogMapper);
        AdminUserService service = new AdminUserService(userMapper, logService);
        User user = new User();
        user.setId(3L);
        user.setStatus("active");
        when(userMapper.selectById(3L)).thenReturn(user);

        service.updateStatus(1L, 3L, "inactive", "违规发布虚假商品");

        assertThat(user.getStatus()).isEqualTo("inactive");
        verify(userMapper).updateById(user);
        ArgumentCaptor<AdminActionLog> logCaptor = ArgumentCaptor.forClass(AdminActionLog.class);
        verify(adminActionLogMapper).insert(logCaptor.capture());
        assertThat(logCaptor.getValue().getAdminId()).isEqualTo(1L);
        assertThat(logCaptor.getValue().getAction()).isEqualTo("UPDATE_USER_STATUS");
        assertThat(logCaptor.getValue().getTargetType()).isEqualTo("USER");
        assertThat(logCaptor.getValue().getBeforeStatus()).isEqualTo("active");
        assertThat(logCaptor.getValue().getAfterStatus()).isEqualTo("inactive");
    }

    @Test
    void banningUserRemovesVisibleItemsAndClosesVisibleWantedPosts() {
        AdminLogService logService = new AdminLogService(adminActionLogMapper);
        AdminUserService service = new AdminUserService(userMapper, itemMapper, wantedMapper, logService);
        User user = new User();
        user.setId(3L);
        user.setStatus("active");
        when(userMapper.selectById(3L)).thenReturn(user);

        Item onSaleItem = new Item();
        onSaleItem.setId(9L);
        onSaleItem.setUserId(3L);
        onSaleItem.setStatus("ON_SALE");
        onSaleItem.setIsDeleted(0);
        when(itemMapper.selectList(any())).thenReturn(List.of(onSaleItem));

        Wanted activeWanted = new Wanted();
        activeWanted.setId(15L);
        activeWanted.setUserId(3L);
        activeWanted.setStatus("active");
        activeWanted.setIsDeleted(0);
        when(wantedMapper.selectList(any())).thenReturn(List.of(activeWanted));

        service.updateStatus(1L, 3L, "inactive", "违规封禁");

        assertThat(user.getStatus()).isEqualTo("inactive");
        assertThat(onSaleItem.getStatus()).isEqualTo("REMOVED");
        assertThat(activeWanted.getStatus()).isEqualTo("closed");
        verify(itemMapper).updateById(onSaleItem);
        verify(wantedMapper).updateById(activeWanted);
        ArgumentCaptor<AdminActionLog> logCaptor = ArgumentCaptor.forClass(AdminActionLog.class);
        verify(adminActionLogMapper, org.mockito.Mockito.times(3)).insert(logCaptor.capture());
        assertThat(logCaptor.getAllValues()).extracting(AdminActionLog::getAction)
                .contains("UPDATE_USER_STATUS", "UPDATE_ITEM_STATUS", "UPDATE_WANTED_STATUS");
    }

    @Test
    void updateUserStatusRequiresReason() {
        AdminLogService logService = new AdminLogService(adminActionLogMapper);
        AdminUserService service = new AdminUserService(userMapper, logService);

        assertThatThrownBy(() -> service.updateStatus(1L, 3L, "inactive", " "))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("原因");
    }

    @Test
    void updateUserStatusRejectsSelfBan() {
        AdminLogService logService = new AdminLogService(adminActionLogMapper);
        AdminUserService service = new AdminUserService(userMapper, logService);

        assertThatThrownBy(() -> service.updateStatus(1L, 1L, "inactive", "误操作保护"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("不能封禁自己");
    }

    @Test
    void updateUserRoleRejectsRemovingOwnAdminRole() {
        AdminLogService logService = new AdminLogService(adminActionLogMapper);
        AdminUserService service = new AdminUserService(userMapper, logService);

        assertThatThrownBy(() -> service.updateRole(1L, 1L, 0, "误操作保护"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("不能取消自己的管理员权限");
    }

    @Test
    void updateUserRoleRequiresReason() {
        AdminLogService logService = new AdminLogService(adminActionLogMapper);
        AdminUserService service = new AdminUserService(userMapper, logService);

        assertThatThrownBy(() -> service.updateRole(1L, 3L, 1, " "))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("原因");
    }

    @Test
    void updateWantedStatusClosesAndWritesLog() {
        AdminLogService logService = new AdminLogService(adminActionLogMapper);
        AdminWantedService service = new AdminWantedService(wantedMapper, logService);
        Wanted wanted = new Wanted();
        wanted.setId(15L);
        wanted.setStatus("active");
        wanted.setIsDeleted(0);
        when(wantedMapper.selectById(15L)).thenReturn(wanted);

        service.updateStatus(1L, 15L, "closed", "违规求购");

        assertThat(wanted.getStatus()).isEqualTo("closed");
        verify(wantedMapper).updateById(wanted);
        ArgumentCaptor<AdminActionLog> logCaptor = ArgumentCaptor.forClass(AdminActionLog.class);
        verify(adminActionLogMapper).insert(logCaptor.capture());
        assertThat(logCaptor.getValue().getAction()).isEqualTo("UPDATE_WANTED_STATUS");
        assertThat(logCaptor.getValue().getTargetType()).isEqualTo("WANTED");
        assertThat(logCaptor.getValue().getBeforeStatus()).isEqualTo("active");
        assertThat(logCaptor.getValue().getAfterStatus()).isEqualTo("closed");
        assertThat(logCaptor.getValue().getRemark()).isEqualTo("违规求购");
    }

    @Test
    void updateWantedStatusRestoresAndWritesLog() {
        AdminLogService logService = new AdminLogService(adminActionLogMapper);
        AdminWantedService service = new AdminWantedService(wantedMapper, logService);
        Wanted wanted = new Wanted();
        wanted.setId(15L);
        wanted.setStatus("closed");
        wanted.setIsDeleted(0);
        when(wantedMapper.selectById(15L)).thenReturn(wanted);

        service.updateStatus(1L, 15L, "active", "恢复正常求购");

        assertThat(wanted.getStatus()).isEqualTo("active");
        verify(wantedMapper).updateById(wanted);
        ArgumentCaptor<AdminActionLog> logCaptor = ArgumentCaptor.forClass(AdminActionLog.class);
        verify(adminActionLogMapper).insert(logCaptor.capture());
        assertThat(logCaptor.getValue().getBeforeStatus()).isEqualTo("closed");
        assertThat(logCaptor.getValue().getAfterStatus()).isEqualTo("active");
        assertThat(logCaptor.getValue().getRemark()).isEqualTo("恢复正常求购");
    }

    @Test
    void updateWantedStatusRequiresReason() {
        AdminLogService logService = new AdminLogService(adminActionLogMapper);
        AdminWantedService service = new AdminWantedService(wantedMapper, logService);

        assertThatThrownBy(() -> service.updateStatus(1L, 15L, "closed", " "))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("原因");
    }

    @Test
    void updateWantedStatusRejectsInvalidStatus() {
        AdminLogService logService = new AdminLogService(adminActionLogMapper);
        AdminWantedService service = new AdminWantedService(wantedMapper, logService);

        assertThatThrownBy(() -> service.updateStatus(1L, 15L, "hidden", "状态不合法"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("状态不合法");
    }

    @Test
    void deleteWantedRequiresReasonAndWritesLog() {
        AdminLogService logService = new AdminLogService(adminActionLogMapper);
        AdminWantedService service = new AdminWantedService(wantedMapper, logService);
        Wanted wanted = new Wanted();
        wanted.setId(15L);
        wanted.setStatus("active");
        wanted.setIsDeleted(0);
        when(wantedMapper.selectById(15L)).thenReturn(wanted);

        service.delete(1L, 15L, "违规删除");

        assertThat(wanted.getIsDeleted()).isEqualTo(1);
        verify(wantedMapper).updateById(wanted);
        ArgumentCaptor<AdminActionLog> logCaptor = ArgumentCaptor.forClass(AdminActionLog.class);
        verify(adminActionLogMapper).insert(logCaptor.capture());
        assertThat(logCaptor.getValue().getAction()).isEqualTo("DELETE_WANTED");
        assertThat(logCaptor.getValue().getTargetType()).isEqualTo("WANTED");
        assertThat(logCaptor.getValue().getBeforeStatus()).isEqualTo("active");
        assertThat(logCaptor.getValue().getAfterStatus()).isEqualTo("DELETED");
        assertThat(logCaptor.getValue().getRemark()).isEqualTo("违规删除");
    }

    @Test
    void deleteWantedRejectsBlankReason() {
        AdminLogService logService = new AdminLogService(adminActionLogMapper);
        AdminWantedService service = new AdminWantedService(wantedMapper, logService);

        assertThatThrownBy(() -> service.delete(1L, 15L, " "))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("原因");
    }

    @Test
    void updateItemStatusChangesStatusAndWritesLog() {
        AdminLogService logService = new AdminLogService(adminActionLogMapper);
        AdminItemService service = new AdminItemService(itemMapper, null, null, logService, "http://127.0.0.1:8080");
        Item item = new Item();
        item.setId(9L);
        item.setStatus("ON_SALE");
        item.setIsDeleted(0);
        when(itemMapper.selectById(9L)).thenReturn(item);

        service.updateStatus(1L, 9L, "REMOVED", "违规商品");

        assertThat(item.getStatus()).isEqualTo("REMOVED");
        verify(itemMapper).updateById(item);
        verify(adminActionLogMapper).insert(any(AdminActionLog.class));
    }

    @Test
    void updateItemStatusRequiresReason() {
        AdminLogService logService = new AdminLogService(adminActionLogMapper);
        AdminItemService service = new AdminItemService(itemMapper, null, null, logService, "http://127.0.0.1:8080");

        assertThatThrownBy(() -> service.updateStatus(1L, 9L, "REMOVED", null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("原因");
    }

    @Test
    void handleReportChangesStatusAndWritesLog() {
        AdminLogService logService = new AdminLogService(adminActionLogMapper);
        AdminReportService service = new AdminReportService(reportMapper, null, null, null, null, null, logService, "http://127.0.0.1:8080");
        Report report = new Report();
        report.setId(5L);
        report.setStatus("PENDING");
        when(reportMapper.selectById(5L)).thenReturn(report);

        service.handle(1L, 5L, "HANDLED", "商品已下架", "OFFLINE_ITEM");

        assertThat(report.getStatus()).isEqualTo("HANDLED");
        assertThat(report.getHandlerId()).isEqualTo(1L);
        assertThat(report.getHandleResult()).isEqualTo("商品已下架");
        verify(reportMapper).updateById(report);
        verify(adminActionLogMapper).insert(any(AdminActionLog.class));
    }

    @Test
    void handleReportRequiresResult() {
        AdminLogService logService = new AdminLogService(adminActionLogMapper);
        AdminReportService service = new AdminReportService(reportMapper, null, null, null, null, null, logService, "http://127.0.0.1:8080");

        assertThatThrownBy(() -> service.handle(1L, 5L, "HANDLED", "", "OFFLINE_ITEM"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("结果");
    }

    @Test
    void listReportsReturnsReadableAdminReportVO() {
        AdminLogService logService = new AdminLogService(adminActionLogMapper);
        AdminReportService service = new AdminReportService(reportMapper, userMapper, itemMapper, wantedMapper, tradeOrderMapper, null, logService, "http://127.0.0.1:8080");
        Report report = new Report();
        report.setId(5L);
        report.setReporterId(2L);
        report.setTargetUserId(3L);
        report.setItemId(9L);
        report.setWantedId(10L);
        report.setOrderId(11L);
        report.setHandlerId(1L);
        report.setReason("fraud");
        report.setStatus("HANDLED");
        Page<Report> page = new Page<>(1, 10);
        page.setRecords(List.of(report));
        page.setTotal(1);
        when(reportMapper.selectPage(any(Page.class), any())).thenReturn(page);
        when(userMapper.selectBatchIds(List.of(2L, 3L, 1L))).thenReturn(List.of(user(2L, "reporter"), user(3L, "target"), user(1L, "admin")));
        Item item = new Item();
        item.setId(9L);
        item.setTitle("二手显示器");
        when(itemMapper.selectBatchIds(List.of(9L))).thenReturn(List.of(item));
        Wanted wanted = new Wanted();
        wanted.setId(10L);
        wanted.setTitle("求购键盘");
        when(wantedMapper.selectBatchIds(List.of(10L))).thenReturn(List.of(wanted));
        TradeOrder order = new TradeOrder();
        order.setId(11L);
        order.setOrderNo("NO20260601");
        when(tradeOrderMapper.selectBatchIds(List.of(11L))).thenReturn(List.of(order));

        Page<AdminReportVO> result = service.list("HANDLED", null, 1, 10);

        AdminReportVO vo = result.getRecords().get(0);
        assertThat(vo.getReporterUsername()).isEqualTo("reporter");
        assertThat(vo.getTargetUserUsername()).isEqualTo("target");
        assertThat(vo.getItemTitle()).isEqualTo("二手显示器");
        assertThat(vo.getWantedTitle()).isEqualTo("求购键盘");
        assertThat(vo.getOrderNo()).isEqualTo("NO20260601");
        assertThat(vo.getHandlerUsername()).isEqualTo("admin");
    }

    @Test
    void listOrdersReturnsReadableAdminOrderVO() {
        AdminLogService logService = new AdminLogService(adminActionLogMapper);
        AdminOrderService service = new AdminOrderService(tradeOrderMapper, tradeOrderLogMapper, itemMapper, userMapper, logService);
        TradeOrder order = new TradeOrder();
        order.setId(11L);
        order.setOrderNo("NO20260601");
        order.setItemId(9L);
        order.setBuyerId(2L);
        order.setSellerId(3L);
        order.setPrice(new BigDecimal("88.00"));
        order.setStatus("PENDING");
        order.setTradeLocation("图书馆门口");
        Page<TradeOrder> page = new Page<>(1, 10);
        page.setRecords(List.of(order));
        page.setTotal(1);
        when(tradeOrderMapper.selectPage(any(Page.class), any())).thenReturn(page);
        Item item = new Item();
        item.setId(9L);
        item.setTitle("二手显示器");
        when(itemMapper.selectBatchIds(List.of(9L))).thenReturn(List.of(item));
        when(userMapper.selectBatchIds(List.of(2L, 3L))).thenReturn(List.of(user(2L, "buyer"), user(3L, "seller")));

        Page<AdminOrderVO> result = service.list("PENDING", null, null, 1, 10);

        AdminOrderVO vo = result.getRecords().get(0);
        assertThat(vo.getOrderNo()).isEqualTo("NO20260601");
        assertThat(vo.getItemTitle()).isEqualTo("二手显示器");
        assertThat(vo.getBuyerUsername()).isEqualTo("buyer");
        assertThat(vo.getSellerUsername()).isEqualTo("seller");
        assertThat(vo.getTradeLocation()).isEqualTo("图书馆门口");
    }

    @Test
    void listLogsReturnsReadableAdminActionLogVO() {
        AdminLogService service = new AdminLogService(adminActionLogMapper, userMapper, itemMapper, reportMapper, wantedMapper, tradeOrderMapper);
        AdminActionLog log = new AdminActionLog();
        log.setId(7L);
        log.setAdminId(1L);
        log.setAction("CANCEL_ORDER");
        log.setTargetType("ORDER");
        log.setTargetId(11L);
        log.setBeforeStatus("PENDING");
        log.setAfterStatus("CANCELLED");
        log.setRemark("买卖双方纠纷");
        Page<AdminActionLog> page = new Page<>(1, 10);
        page.setRecords(List.of(log));
        page.setTotal(1);
        when(adminActionLogMapper.selectPage(any(Page.class), any())).thenReturn(page);
        when(userMapper.selectBatchIds(List.of(1L))).thenReturn(List.of(user(1L, "admin")));
        TradeOrder order = new TradeOrder();
        order.setId(11L);
        order.setOrderNo("NO20260601");
        when(tradeOrderMapper.selectBatchIds(List.of(11L))).thenReturn(List.of(order));

        Page<AdminActionLogVO> result = service.list(null, "ORDER", 1, 10);

        AdminActionLogVO vo = result.getRecords().get(0);
        assertThat(vo.getAdminUsername()).isEqualTo("admin");
        assertThat(vo.getActionText()).isEqualTo("取消订单");
        assertThat(vo.getTargetName()).isEqualTo("NO20260601");
        assertThat(vo.getBeforeStatusText()).isEqualTo("待交易");
        assertThat(vo.getAfterStatusText()).isEqualTo("已取消");
        assertThat(vo.getRemark()).isEqualTo("买卖双方纠纷");
    }

    @Test
    void actionLogStatusTextUsesActionAndTargetType() {
        AdminActionLog categoryLog = new AdminActionLog();
        categoryLog.setAction("UPDATE_CATEGORY");
        categoryLog.setTargetType("CATEGORY");
        categoryLog.setBeforeStatus("0");
        categoryLog.setAfterStatus("1");

        AdminActionLogVO categoryVO = AdminActionLogVO.from(categoryLog);

        assertThat(categoryVO.getBeforeStatusText()).isEqualTo("禁用");
        assertThat(categoryVO.getAfterStatusText()).isEqualTo("启用");

        AdminActionLog wantedLog = new AdminActionLog();
        wantedLog.setAction("UPDATE_WANTED_STATUS");
        wantedLog.setTargetType("WANTED");
        wantedLog.setBeforeStatus("pending");
        wantedLog.setAfterStatus("active");

        AdminActionLogVO wantedVO = AdminActionLogVO.from(wantedLog);

        assertThat(wantedVO.getBeforeStatusText()).isEqualTo("待定");
        assertThat(wantedVO.getAfterStatusText()).isEqualTo("有效");
    }

    @Test
    void listLogsReturnsCategoryTargetName() {
        AdminLogService service = new AdminLogService(adminActionLogMapper, userMapper, itemMapper, reportMapper, wantedMapper, tradeOrderMapper, categoryMapper);
        AdminActionLog log = new AdminActionLog();
        log.setId(8L);
        log.setAdminId(1L);
        log.setAction("UPDATE_CATEGORY");
        log.setTargetType("CATEGORY");
        log.setTargetId(6L);
        log.setBeforeStatus("0");
        log.setAfterStatus("1");
        Page<AdminActionLog> page = new Page<>(1, 10);
        page.setRecords(List.of(log));
        page.setTotal(1);
        when(adminActionLogMapper.selectPage(any(Page.class), any())).thenReturn(page);
        when(userMapper.selectBatchIds(List.of(1L))).thenReturn(List.of(user(1L, "admin")));
        Category category = new Category();
        category.setId(6L);
        category.setName("数码产品");
        when(categoryMapper.selectBatchIds(List.of(6L))).thenReturn(List.of(category));

        Page<AdminActionLogVO> result = service.list(null, "CATEGORY", 1, 10);

        AdminActionLogVO vo = result.getRecords().get(0);
        assertThat(vo.getTargetName()).isEqualTo("数码产品");
        assertThat(vo.getBeforeStatusText()).isEqualTo("禁用");
        assertThat(vo.getAfterStatusText()).isEqualTo("启用");
    }

    private User user(Long id, String username) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        return user;
    }
}
