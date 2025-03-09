package com.sky.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sky.constant.MessageConstant;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.entity.Employee;
import com.sky.mapper.EmployeeMapper;
import com.sky.service.EmployeeService;
import com.sky.utils.JwtUtil;
import com.sky.vo.EmployeeLoginVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.Map;


@Service
@Slf4j
public class EmployeeServiceImpl
        extends ServiceImpl<EmployeeMapper, Employee>
        implements EmployeeService {

    @Autowired
    EmployeeMapper employeeMapper;

    @Override
    public EmployeeLoginVO login(EmployeeLoginDTO dto){
        //1. 根据用户名查询数据库 select * from employee where username=?
        //使用Wrappers来封装查询条件
        QueryWrapper<Employee> wrapper = Wrappers.<Employee>query()
                .eq("username", dto.getUsername());
        Employee employee = employeeMapper.selectOne(wrapper);
        String password = employee.getPassword();

        if(employee == null){
            //用户不存在
            throw new RuntimeException(MessageConstant.ACCOUNT_NOT_FOUND);
        }
        //2. 将dto中的密码和数据库中的密码做对比
        //TODO MD5加密

        if(!password.equals(DigestUtils.md5DigestAsHex(dto.getPassword().getBytes()))){
            //密码错误
            throw new RuntimeException(MessageConstant.PASSWORD_ERROR);
        }
        //3.判断用户状态
        if(employee.getStatus() == 0){
            //用户被禁用
            throw new RuntimeException(MessageConstant.ACCOUNT_LOCKED);
        }

        //4.生成jwt令牌
        Map<String,Object> map = new HashMap<>();
        map.put("empId",employee.getId());
        String token = JwtUtil.createJWT(map);

        //5.返回数据
        return EmployeeLoginVO.builder()
                .id(employee.getId())
                .userName(employee.getUsername())
                .name(employee.getName())
                .token(token)
                .build();


    }


}

