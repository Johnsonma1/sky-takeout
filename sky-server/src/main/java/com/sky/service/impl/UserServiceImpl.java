package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.*;
import com.sky.mapper.UserMapper;
import com.sky.service.AddressBookService;
import com.sky.service.OrderDetailService;
import com.sky.service.ShoppingCartService;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import com.sky.utils.JwtUtil;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.UserLoginVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class UserServiceImpl
        extends ServiceImpl<UserMapper, User>
        implements UserService {
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
    @Override
    public UserLoginVO wxLogin(UserLoginDTO userLoginDTO) {
        //1. openid，通过微信官方接口获取
        Map<String, String> param = new HashMap<>();
        param.put("appid", "wx0746e11b759884e0"); //小程序id
        param.put("secret", "19c90719a3410f139a6f1b4230ebd601"); //小程序密钥
        param.put("js_code", userLoginDTO.getCode()); //授权码
        param.put("grant_type", "authorization_code"); //授权类型，固定值
        //发送请求
        String body = HttpClientUtil.doGet("https://api.weixin.qq.com/sns/jscode2session", param);
        String openid = (String) JSONObject.parseObject(body).get("openid");

        if (openid == null) {
            throw new RuntimeException("登录失败");
        }

        //2. id，用户id
        //向user表中添加一条数据，主要就是保存openid
        User user = this.query()
                .eq("openid", openid)
                .one();
        if (user == null) {
            //说明表中没有openid，是新用户，添加操作，其实就是注册
            user = User.builder()
                    .openid(openid)
                    .createTime(LocalDateTime.now())
                    .build();
            //添加操作
            this.save(user);
        }

        //3. token
        //登录成功后，生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        String token = JwtUtil.createJWT(claims);


        return UserLoginVO.builder()
                .openid(openid)
                .id(user.getId())
                .token(token)
                .build();
    }

}