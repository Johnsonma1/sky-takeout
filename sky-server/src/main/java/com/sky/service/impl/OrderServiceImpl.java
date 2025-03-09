package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sky.constant.MessageConstant;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.*;
import com.sky.exception.OrderBusinessException;
import com.sky.mapper.OrderMapper;
import com.sky.service.*;
import com.sky.utils.JwtUtil;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.webSocket.WebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

/**
 * 订单
 */
@Service
@Slf4j
public class OrderServiceImpl
        extends ServiceImpl<OrderMapper, Orders>
        implements OrderService {

    @Autowired
    HttpServletRequest request;
    @Autowired
    UserService userService;
    @Autowired
    AddressBookService addressBookService;
    @Autowired
    ShoppingCartService shoppingCartService;
    @Autowired
    OrderDetailService orderDetailService;
    @Autowired
    private WeChatPayUtil weChatPayUtil;
    @Autowired
    private OrderService orderService;
    @Autowired
    private WebSocketServer webSocketServer;
    @Autowired
    private OrderMapper orderMapper;

    @Override
    public OrderSubmitVO submitOrder(OrdersSubmitDTO dto) {
        /////////////////////// 1. 基础数据准备：用户信息、地址信息、购物车列表
        //用户信息
        String authentication = request.getHeader("authentication");
        Long userId = JwtUtil.parseJWT(authentication).get("userId", Long.class);
        User user = userService.getById(userId);
        //地址信息
        Long addressBookId = dto.getAddressBookId();
        AddressBook address = addressBookService.getById(addressBookId);
        //购物车列表
        List<ShoppingCart> carts = shoppingCartService.query().eq("user_id", userId).list();


        /////////////////////// 2. 构建订单对象 Orders
        Orders orders = Orders.builder()
                .number(String.valueOf(System.currentTimeMillis()))
                .status(1)
                .userId(userId)
                .addressBookId(addressBookId)
                .orderTime(LocalDateTime.now())
                .payMethod(dto.getPayMethod())
                .payStatus(0)
                .amount(dto.getAmount())
                .remark(dto.getRemark())
                .userName(user.getName())
                .phone(address.getPhone())
                .address(address.getDetail())
                .consignee(address.getConsignee())
                .estimatedDeliveryTime(dto.getEstimatedDeliveryTime())
                .deliveryStatus(dto.getDeliveryStatus())
                .packAmount(dto.getPackAmount())
                .tablewareNumber(dto.getTablewareNumber())
                .tablewareStatus(dto.getTablewareStatus())
                .build();

        this.save(orders);


        /////////////////////// 3. 构建订单项列表 List<OrderDetail>
        carts.forEach(cart -> {
            OrderDetail orderDetail = OrderDetail.builder()
                    .name(cart.getName())
                    .orderId(orders.getId())
                    .dishId(cart.getDishId())
                    .setmealId(cart.getSetmealId())
                    .dishFlavor(cart.getDishFlavor())
                    .number(cart.getNumber())
                    .amount(cart.getAmount())
                    .image(cart.getImage())
                    .build();

            orderDetailService.save(orderDetail);
        });

        /////////////////////// 4. 删除购物车数据
        shoppingCartService.update().eq("user_id", userId).remove();

        /////////////////////// 5. 构建OrderSubmitVO对象返回
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(orders.getId())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .orderTime(orders.getOrderTime())
                .build();

        return orderSubmitVO;
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
       /* // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userService.getById(userId);

        //调用微信支付接口，生成预支付交易单
        JSONObject jsonObject = weChatPayUtil.pay(
                ordersPaymentDTO.getOrderNumber(),  //商户订单号
                ordersPaymentDTO.getPayMethod(),
                new BigDecimal(0.01), //支付金额，单位 元
                "苍穹外卖订单", //商品描述
                user.getOpenid() //微信用户的openid
        );

        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        return vo;*/
        return null;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        //Orders ordersDB = orderMapper.getByNumber(outTradeNo);
        Orders ordersDB = orderService.query()
                        .eq("number", outTradeNo)
                        .one();
        ordersDB.setPayStatus(Orders.PAID);
        ordersDB.setCheckoutTime(LocalDateTime.now());
        this.updateById(ordersDB);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间

        /*Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        this.save(orders);*/
        HashMap<String, Object> msg = new HashMap<>();
        msg.put("type", "1");//1表示来单提醒 2表示催单
        msg.put("orderId", ordersDB.getId());
        msg.put("content", "订单号:" + outTradeNo);

        String toJSON = JSON.toJSONString(msg);
        webSocketServer.sendToAllClient(toJSON);

    }

    /**
     * 用户催单
     *
     * @param id
     */
    @Override
    public void reminder(Long id) {
        Orders orders = orderService.getById(id);
        if (orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        HashMap<String, Object> msg = new HashMap<>();
        //1表示来单提醒 2表示催单
        msg.put("type", "2");
        msg.put("orderId", orders.getId());
        msg.put("content", "订单号:" + orders.getNumber());

        String toJSON = JSON.toJSONString(msg);
        webSocketServer.sendToAllClient(toJSON);
    }

}