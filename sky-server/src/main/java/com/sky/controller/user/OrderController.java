package com.sky.controller.user;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.ShoppingCart;
import com.sky.result.Result;
import com.sky.service.OrderDetailService;
import com.sky.service.OrderService;
import com.sky.service.ShoppingCartService;
import com.sky.utils.JwtUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
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
 * 订单
 */
@RestController("userOrderController")
@RequestMapping("/user/order")
@Slf4j
@Api(tags = "C端-订单接口")
public class OrderController {
    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderDetailService orderDetailService;
    @Autowired
    private ShoppingCartService shoppingCartService;

    @ApiOperation("用户提交订单")
    @PostMapping("/submit")
    public Result<OrderSubmitVO> submitOrder(@RequestBody OrdersSubmitDTO ordersSubmitDTO) {
        OrderSubmitVO orderSubmitVO = orderService.submitOrder(ordersSubmitDTO);
        return Result.success(orderSubmitVO);
    }


    @ApiOperation("查询历史订单")
    @GetMapping("/historyOrders")
    public Result<IPage<OrderVO>> historyOrders(OrdersPageQueryDTO ordersPageQueryDTO,@RequestHeader String authentication){
        Long userId = JwtUtil.parseJWT(authentication).get("userId", Long.class);
        Page<Orders> page = new Page<>(ordersPageQueryDTO.getPage(),ordersPageQueryDTO.getPageSize());
        orderService.query()
                .eq("user_id",userId)
                .eq(ordersPageQueryDTO.getStatus()!=null ,"status",ordersPageQueryDTO.getStatus())
                .orderByDesc("order_time")
                .page(page);

        IPage<OrderVO> convert = (IPage<OrderVO>) page.convert(order -> {
            OrderVO orderVO = BeanUtil.toBean(order, OrderVO.class);
            List<OrderDetail> orderDetailList = orderDetailService.query()
                    .eq("order_id", order.getId())
                    .list();
            orderVO.setOrderDetailList(orderDetailList);
            return orderVO;
        });
        return Result.success(convert);
    }
    @ApiOperation("查询订单详情")
    @GetMapping("/orderDetail/{id}")
    public Result<OrderVO> orderDetail(@PathVariable Long id){
        Orders orders = orderService.getById(id);
        OrderVO orderVO = BeanUtil.toBean(orders, OrderVO.class);
        List<OrderDetail> orderDetailList = orderDetailService.query()
                .eq("order_id", orders.getId())
                .list();
        orderVO.setOrderDetailList(orderDetailList);
        return Result.success(orderVO);
    }
    @ApiOperation("取消订单")
    @PutMapping("/cancel/{id}")
    public Result cancel(@PathVariable Long id){
        orderService.update()
                .eq("id",id)
                .set("status",Orders.CANCELLED)
                .update();
        return Result.success();
    }

    @ApiOperation("再来一单")
    @PostMapping("/repetition/{id}")
    public Result repetition(@PathVariable Long id,@RequestHeader String authentication){
        Long userId = JwtUtil.parseJWT(authentication).get("userId", Long.class);

        List<OrderDetail> orderDetailList = orderDetailService.query()
                .eq("order_id", id)
                .list();

        List<ShoppingCart> shoppingCartList = orderDetailList.stream().map(orderDetail -> {
            ShoppingCart shoppingCart = new ShoppingCart();
            BeanUtil.copyProperties(orderDetail,shoppingCart,"id");
            shoppingCart.setUserId(userId);
            shoppingCart.setCreateTime(LocalDateTime.now());
            return shoppingCart;
        }).collect(Collectors.toList());
        shoppingCartService.saveBatch(shoppingCartList);
        return Result.success();

    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    @PutMapping("/payment")
    @ApiOperation("订单支付")
    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        log.info("订单支付：{}", ordersPaymentDTO);
//        OrderPaymentVO orderPaymentVO = orderService.payment(ordersPaymentDTO);
//        log.info("生成预支付交易单：{}", orderPaymentVO);
        orderService.paySuccess(ordersPaymentDTO.getOrderNumber());
        return Result.success();
    }

    /**
     * 催单
     *
     * @param id
     * @return
     */
    @GetMapping("/reminder/{id}")
    @ApiOperation("催单")
    public Result reminder(@PathVariable Long id) {
        log.info("用户催单orderId:{}", id);
        orderService.reminder(id);

        return Result.success();
    }
}