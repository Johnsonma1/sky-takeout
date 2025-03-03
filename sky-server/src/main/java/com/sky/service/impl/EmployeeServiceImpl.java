package com.sky.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sky.entity.Employee;
import com.sky.mapper.EmployeeMapper;
import com.sky.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmployeeServiceImpl
        extends ServiceImpl<EmployeeMapper, Employee>
        implements EmployeeService {

}
