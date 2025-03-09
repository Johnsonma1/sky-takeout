package com.sky.task;

import com.sky.entity.Orders;
import com.sky.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 处理订单相关的定时任务类
 */
@Component
@Slf4j
public class OrderTask {
    @Autowired
    private OrderService orderService;

    /**
     * 每分钟执行，处理超时订单
     * 用户下单超过15分钟，还未支付的订单，需要将订单的状态修改为已取消
     */
    @Scheduled(cron = "0 * * * * ?")
    public void processTimeoutOrder() {
        log.info("处理超时订单任务开始执行...");
        LocalDateTime time = LocalDateTime.now().plusMinutes(-15);
        //select * from orders where status = 1 and order_time < (now-15)

        orderService.update()
                .eq("status", Orders.PENDING_PAYMENT)
                .lt("order_time", time)
                .set("status", Orders.CANCELLED)
                .set("cancel_reason", "订单超时，自动取消！")
                .set("cancel_time", LocalDateTime.now())
                .update();
    }

    /**
     * 每天凌晨1点执行，处理派送中订单
     * 订单的状态是派送中，需要将订单的状态设置为已完成
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void processDeliveryOrder() {
        log.info("处理派送中订单任务开始执行...");
        LocalDateTime time = LocalDateTime.now().plusMinutes(-60);
        orderService.update()
                .eq("status", Orders.DELIVERY_IN_PROGRESS)
                .lt("delivery_time", time)
                .set("status", Orders.COMPLETED)

                .set("delivery_time", LocalDateTime.now())
                .update();
    }
}
