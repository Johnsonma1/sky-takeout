package com.sky.controller.admin;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sky.constant.MessageConstant;
import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.result.Result;
import com.sky.service.OrderDetailService;
import com.sky.service.OrderService;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 订单管理
 */
@RestController("adminOrderController")
@RequestMapping("/admin/order")
@Slf4j
@Api(tags = "订单管理接口")
public class OrderController {
    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderDetailService orderDetailService;

    @ApiOperation("订单搜索")
    @GetMapping("/conditionSearch")
    public Result<IPage<OrderVO>> conditionSearch(OrdersPageQueryDTO dto) {

        Page<Orders> page = new Page<>(dto.getPage(), dto.getPageSize());

        orderService.query()
                .eq(dto.getStatus() != null, "status", dto.getStatus())
                .like(dto.getNumber() != null, "number", dto.getNumber())
                .like(dto.getPhone() != null, "phone", dto.getPhone())
                .between(dto.getBeginTime() != null, "order_time", dto.getBeginTime(), dto.getEndTime())
                .orderByDesc("order_time")
                .page(page);

        //将Oreder转换成OrderVO
        IPage<OrderVO> convert = page.convert(orders -> {
            OrderVO orderVO = new OrderVO();
            //将Order中的数据封装到orderVo中
            BeanUtil.copyProperties(orders, orderVO);
            //还要将订单中所有的菜品名称以指定格式设置到ordervo中   例如：宫保鸡丁* 3；红烧带鱼* 2；农家小炒肉* 1；
            List<OrderDetail> detailList = orderDetailService.query().eq("order_id", orders.getId()).list();

            // 将订单详情列表转换为一个包含所有菜品名称和数量的字符串
            // 例如：“菜名1*数量1,菜名2*数量2”，用于显示订单中的菜品信息
            String orderDishs = detailList.stream().map(orderDetail -> {
                return orderDetail.getName() + "*" + orderDetail.getNumber();
            }).collect(Collectors.joining("; "));

            // 设置订单VO的菜品信息，并返回订单VO对象
            orderVO.setOrderDishes(orderDishs);
            return orderVO;
        });


        return Result.success(convert);
    }
    @GetMapping("/statistics")
    @ApiOperation("统计各订单状态数量")
    public Result<OrderStatisticsVO> statistics() {
        Long toBeConfirmedCount = orderService.query().eq("status", 2).count();
        Long confirmedCount = orderService.query().eq("status", 3).count();
        Long deliveryInProgressCount = orderService.query().eq("status", 4).count();

        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setConfirmed(toBeConfirmedCount.intValue());
        orderStatisticsVO.setToBeConfirmed(confirmedCount.intValue());
        orderStatisticsVO.setDeliveryInProgress(deliveryInProgressCount.intValue());

        return Result.success(orderStatisticsVO);
    }
    @ApiOperation("查询订单详情")
    @GetMapping("/details/{id}")
    public Result<OrderVO> getOrderDetailById(@PathVariable Long id){
        //1. 查询订单主体数据 Orders
        Orders orders = orderService.getById(id);

        //2. 查询订单所包含的订单项数据List<OrderDetail>
        List<OrderDetail> detailList = orderDetailService.query().eq("order_id", id).list();

        //3. 将Orders和List<OrderDetail>封装到OrderVO对象中，返回
        OrderVO orderVO = BeanUtil.toBean(orders, OrderVO.class);
        orderVO.setOrderDetailList(detailList);

        return Result.success(orderVO);
    }
    @PutMapping("/confirm")
    @ApiOperation("接单")
    public Result<Void> confirm(@RequestBody OrdersConfirmDTO ordersConfirmDTO) {
        orderService.update()
                .eq("id", ordersConfirmDTO.getId())
                .set("status", Orders.CONFIRMED)
                .update();
        return Result.success();
    }
    @PutMapping("/rejection")
    @ApiOperation("拒单")
    public Result<Void> rejection(@RequestBody OrdersRejectionDTO ordersRejectionDTO) {
        //用户已支付，需要退款
       /* String refund = weChatPayUtil.refund(
                   ordersDB.getNumber(),
                    ordersDB.getNumber(),
                    new BigDecimal(0.01),
                   new BigDecimal(0.01));*/
        //TODO 异常报错
        orderService.update()
                .eq(ordersRejectionDTO.getId()!=null,"id", ordersRejectionDTO.getId())
                .set("status", Orders.CANCELLED)
                .set("rejection_reason", ordersRejectionDTO.getRejectionReason())
                .set("cancel_time", LocalDateTime.now())
                .update();
        return Result.success();
    }
    @PutMapping("/cancel")
    @ApiOperation("取消订单")
    public Result<Void> cancel(@RequestBody OrdersCancelDTO ordersCancelDTO) {
        // 根据id查询订单
        Orders ordersDB = orderService.getById(ordersCancelDTO.getId());
        //支付状态
        Integer payStatus = ordersDB.getPayStatus();
        if (payStatus == 1) {
              //用户已支付，需要退款
//            String refund = weChatPayUtil.refund(
//                    ordersDB.getNumber(),
//                    ordersDB.getNumber(),
//                    new BigDecimal(0.01),
//                    new BigDecimal(0.01));
        }
        boolean isUpdate = orderService.update()
                .eq(ordersCancelDTO.getId()!=null,"id", ordersCancelDTO.getId())
                .set("status", Orders.CANCELLED)
                .set("cancel_reason", ordersCancelDTO.getCancelReason())
                .set("cancel_time", LocalDateTime.now())
                .update();
        if (!isUpdate) {
            return Result.error(MessageConstant.UNKNOWN_ERROR);
        }
        return Result.success();
    }
    @PutMapping("/delivery/{id}")
    @ApiOperation("派送订单")
    public Result<Void> delivery(@PathVariable("id") Long id) {
        boolean isUpdate = orderService.update()
                .eq(id !=null,"id", id)
                .set("status", Orders.DELIVERY_IN_PROGRESS)
                .update();
        if (!isUpdate) {
            return Result.error(MessageConstant.ORDER_STATUS_ERROR);
        }
        return Result.success();
    }
    @PutMapping("/complete/{id}")
    @ApiOperation("完成订单")
    public Result<Void> complete(@PathVariable("id") Long id) {
        boolean isUpdate = orderService.update()
                .eq(id !=null,"id", id)
                .eq("status", Orders.DELIVERY_IN_PROGRESS)
                .set("status", Orders.COMPLETED)
                .set("delivery_time", LocalDateTime.now())
                .update();
        if (!isUpdate) {
            return Result.error(MessageConstant.ORDER_STATUS_ERROR);
        }
        return Result.success();
    }

}