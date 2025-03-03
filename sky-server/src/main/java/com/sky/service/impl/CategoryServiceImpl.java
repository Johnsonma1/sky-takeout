package com.sky.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sky.entity.Category;
import com.sky.mapper.CategoryMapper;
import com.sky.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 分类业务层
 */
@Service
@Slf4j
public class CategoryServiceImpl
        extends ServiceImpl<CategoryMapper, Category>
        implements CategoryService {

}
