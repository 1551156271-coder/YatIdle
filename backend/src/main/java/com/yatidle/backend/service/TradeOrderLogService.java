package com.yatidle.backend.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yatidle.backend.entity.TradeOrderLog;
import com.yatidle.backend.mapper.TradeOrderLogMapper;
import org.springframework.stereotype.Service;

@Service
public class TradeOrderLogService extends ServiceImpl<TradeOrderLogMapper, TradeOrderLog> {
    public void recordLog(Long orderId,
                          String action,
                          String beforeStatus,
                          String afterStatus,
                          Long operatorId,
                          String remark){
        TradeOrderLog log = new TradeOrderLog();
        log.setOrderId(orderId);
        log.setAction(action);
        log.setBeforeStatus(beforeStatus);
        log.setAfterStatus(afterStatus);
        log.setOperatorId(operatorId);
        log.setRemark(remark);
        this.save(log);
    }
}
