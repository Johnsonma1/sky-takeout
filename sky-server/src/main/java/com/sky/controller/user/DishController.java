package com.sky.controller.user;

import cn.hutool.core.bean.BeanUtil;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.result.Result;
import com.sky.service.DishFlavorService;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController("userDishController")
@RequestMapping("/user/dish")
@Slf4j
@Api(tags = "C端-菜品浏览接口")
public class DishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private DishFlavorService dishFlavorService;

    /**
     * 根据分类ID查询菜品列表
     * 此方法通过分类ID来查询符合条件的菜品列表，并将每个菜品及其对应的口味信息封装到DishVO对象中返回
     *
     * @param categoryId 分类ID，用于查询特定分类下的菜品列表
     * @return 返回一个Result对象，其中包含一个DishVO对象的列表，每个DishVO对象代表一个菜品及其口味信息
     */
    @GetMapping("/list")
    @ApiOperation("根据分类ID查询菜品列表")
    public Result<List<DishVO>> list(Long categoryId) {
        log.info("根据分类ID查询菜品列表:{}", categoryId);

        // 根据分类ID查询菜品列表
        List<Dish> dishList = dishService.query()
                .eq("category_id", categoryId)
                .eq("status", 1)
                .list();
        List<DishVO> dishVOList = dishList.stream().map(dish -> {
            DishVO dishVO = BeanUtil.toBean(dish, DishVO.class);
            List<DishFlavor> dishFlavors = dishFlavorService.query()
                    .eq("dish_id", dish.getId())
                    .list();
            dishVO.setFlavors(dishFlavors);
            return dishVO;
        }).collect(Collectors.toList());

        return Result.success(dishVOList);
    }


}
