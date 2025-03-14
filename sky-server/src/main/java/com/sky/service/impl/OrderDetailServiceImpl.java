package com.sky.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sky.entity.OrderDetail;
import com.sky.mapper.OrderDetailMapper;
import com.sky.service.OrderDetailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 订单详情
 */
@Service
@Slf4j
public class OrderDetailServiceImpl
        extends ServiceImpl<OrderDetailMapper, OrderDetail>
        implements OrderDetailService {

}