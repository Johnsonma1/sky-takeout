package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sky.constant.StatusConstant;
import com.sky.entity.Dish;
import com.sky.entity.Orders;
import com.sky.entity.Setmeal;
import com.sky.entity.User;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.DishService;
import com.sky.service.OrderService;
import com.sky.service.SetmealService;
import com.sky.service.WorkspaceService;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@Slf4j
public class WorkspaceServiceImpl implements WorkspaceService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private DishService dishService;
    @Autowired
    private SetmealService setmealService;
    @Autowired
    private OrderService orderService;

    /**
     * 根据时间段统计营业数据
     * @param begin 开始时间
     * @param end 结束时间
     * @return 营业数据VO
     */
    public BusinessDataVO getBusinessData(LocalDateTime begin, LocalDateTime end) {
        // 查询条件
        QueryWrapper<Orders> queryWrapper = new QueryWrapper<>();
        queryWrapper.between("create_time", begin, end);

        // 查询总订单数
        Integer totalOrderCount = Math.toIntExact(orderMapper.selectCount(queryWrapper));

        // 查询已完成订单的条件
        queryWrapper.eq("status", Orders.COMPLETED);

        // 营业额：已完成订单的总金额
        Double turnover = orderMapper.selectList(queryWrapper).stream()
                .mapToDouble(order -> order.getAmount().doubleValue())
                .sum();

        // 有效订单数：已完成订单的数量
        Integer validOrderCount = Math.toIntExact(orderMapper.selectCount(queryWrapper));

        // 计算订单完成率和平均客单价
        Double orderCompletionRate = 0.0;
        Double unitPrice = 0.0;
        if (totalOrderCount != 0 && validOrderCount != 0) {
            orderCompletionRate = validOrderCount.doubleValue() / totalOrderCount;
            unitPrice = turnover / validOrderCount;
        }

        // 新增用户数
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.between("create_time", begin, end);
        Integer newUsers = Math.toIntExact(userMapper.selectCount(userQueryWrapper));

        // 构建并返回营业数据VO
        return BusinessDataVO.builder()
                .turnover(turnover)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .unitPrice(unitPrice)
                .newUsers(newUsers)
                .build();
    }

    /**
     * 查询订单管理数据
     *
     * @return 订单概览VO
     */
    public OrderOverViewVO getOrderOverView() {
        // 获取当天的时间范围
        LocalDateTime startOfDay = LocalDateTime.now().with(LocalTime.MIN); // 当天开始时间
        LocalDateTime endOfDay = LocalDateTime.now().with(LocalTime.MAX);   // 当天结束时间

        // 查询条件
        QueryWrapper<Orders> queryWrapper = new QueryWrapper<>();
        queryWrapper.between("create_time", startOfDay, endOfDay);

        // 待接单
        queryWrapper.eq("status", Orders.TO_BE_CONFIRMED);
        Integer waitingOrders = Math.toIntExact(orderService.count(queryWrapper));

        // 待派送
        queryWrapper.eq("status", Orders.CONFIRMED);
        Integer deliveredOrders = Math.toIntExact(orderService.count(queryWrapper));

        // 已完成
        queryWrapper.eq("status", Orders.COMPLETED);
        Integer completedOrders = Math.toIntExact(orderService.count(queryWrapper));

        // 已取消
        queryWrapper.eq("status", Orders.CANCELLED);
        Integer cancelledOrders = Math.toIntExact(orderService.count(queryWrapper));

        // 全部订单
        queryWrapper.clear(); // 清空之前的条件
        queryWrapper.between("create_time", startOfDay, endOfDay);
        Integer allOrders = Math.toIntExact(orderService.count(queryWrapper));

        // 构建并返回订单概览VO
        return OrderOverViewVO.builder()
                .waitingOrders(waitingOrders)
                .deliveredOrders(deliveredOrders)
                .completedOrders(completedOrders)
                .cancelledOrders(cancelledOrders)
                .allOrders(allOrders)
                .build();
    }
    /**
     * 查询菜品总览
     *
     * @return 菜品概览VO
     */
    public DishOverViewVO getDishOverView() {
        // 查询条件
        QueryWrapper<Dish> queryWrapper = new QueryWrapper<>();

        // 查询已启用的菜品数量
        queryWrapper.eq("status", StatusConstant.ENABLE);
        Integer sold = Math.toIntExact(dishService.count(queryWrapper));

        // 查询已停用的菜品数量
        queryWrapper.clear(); // 清空之前的条件
        queryWrapper.eq("status", StatusConstant.DISABLE);
        Integer discontinued = Math.toIntExact(dishService.count(queryWrapper));

        // 构建并返回菜品概览VO
        return DishOverViewVO.builder()
                .sold(sold)
                .discontinued(discontinued)
                .build();
    }

    /**
     * 查询套餐总览
     *
     * @return 套餐概览VO
     */
    public SetmealOverViewVO getSetmealOverView() {
        // 查询条件
        QueryWrapper<Setmeal> queryWrapper = new QueryWrapper<>();

        // 查询已启用的套餐数量
        queryWrapper.eq("status", StatusConstant.ENABLE);
        Integer sold = Math.toIntExact(setmealService.count(queryWrapper));

        // 查询已停用的套餐数量
        queryWrapper.clear(); // 清空之前的条件
        queryWrapper.eq("status", StatusConstant.DISABLE);
        Integer discontinued = Math.toIntExact(setmealService.count(queryWrapper));

        // 构建并返回套餐概览VO
        return SetmealOverViewVO.builder()
                .sold(sold)
                .discontinued(discontinued)
                .build();
    }
}