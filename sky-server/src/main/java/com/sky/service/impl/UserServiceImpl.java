package com.sky.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sky.entity.User;
import com.sky.mapper.UserMapper;
import com.sky.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserServiceImpl
        extends ServiceImpl<UserMapper, User>
        implements UserService {


}
