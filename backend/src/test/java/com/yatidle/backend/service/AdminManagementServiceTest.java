package com.yatidle.backend.service;

import com.yatidle.backend.entity.AdminActionLog;
import com.yatidle.backend.entity.Item;
import com.yatidle.backend.entity.Report;
import com.yatidle.backend.entity.User;
import com.yatidle.backend.common.exception.BusinessException;
import com.yatidle.backend.mapper.AdminActionLogMapper;
import com.yatidle.backend.mapper.ItemMapper;
import com.yatidle.backend.mapper.ReportMapper;
import com.yatidle.backend.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    private AdminActionLogMapper adminActionLogMapper;

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
    void updateUserStatusRequiresReason() {
        AdminLogService logService = new AdminLogService(adminActionLogMapper);
        AdminUserService service = new AdminUserService(userMapper, logService);

        assertThatThrownBy(() -> service.updateStatus(1L, 3L, "inactive", " "))
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
}
