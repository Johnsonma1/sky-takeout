package com.sky.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 处理订单相关的定时任务类
 */
@Component
@Slf4j
public class OrderTask {

    /**
     * 每分钟执行，处理超时订单
     * 用户下单超过15分钟，还未支付的订单，需要将订单的状态修改为已取消
     */
    //@Scheduled(cron = "0 * * * * ?")
    public void processTimeoutOrder() {
    }

    /**
     * 每天凌晨1点执行，处理派送中订单
     * 订单的状态是派送中，需要将订单的状态设置为已完成
     */
    //@Scheduled(cron = "0 0 2 * * ?")
    public void processDeliveryOrder() {
    }
}
