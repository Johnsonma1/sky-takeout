package com.sky.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 订单
 */
@Service
@Slf4j
public class OrderServiceImpl
        extends ServiceImpl<OrderMapper, Orders>
        implements OrderService {

}