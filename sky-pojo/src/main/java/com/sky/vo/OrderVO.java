package com.sky.vo;

import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderVO extends Orders implements Serializable {

    private String orderDishes;   //订单菜品信息，给管理端使用的    牛蛙*1,米饭*2
    private List<OrderDetail> orderDetailList;    //订单详情，   给用户端使用的
}
