package com.sky.controller.user;

import cn.hutool.core.bean.BeanUtil;
import com.sky.constant.StatusConstant;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.service.SetmealDishService;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController("userSetmealController")
@RequestMapping("/user/setmeal")
@Api(tags = "C端-套餐浏览接口")
public class SetmealController {

    @Autowired
    private SetmealService setmealService;
    @Autowired
    private SetmealDishService setmealDishService;
    @Autowired
    private DishService dishService;
    /**根据分类id查询套餐
     *
     *
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询套餐")
    public Result<List<SetmealVO>> list(Long categoryId) {
        log.info("根据分类id查询套餐:{}", categoryId);
        List<Setmeal> list = setmealService.query()
                .eq(categoryId != null, "categoryId", categoryId)
                .eq("status", StatusConstant.ENABLE)
                .list();

        List<SetmealVO> setmealVOS = list.stream().map(setmeal -> {
            SetmealVO setmealVO = BeanUtil.toBean(setmeal,SetmealVO.class);
            List<SetmealDish> setmealDishes = setmealDishService.query()
                    .eq("setmeal_id", setmeal.getId())
                    .list();
            setmealVO.setSetmealDishes(setmealDishes);
            return setmealVO;
        }).collect(Collectors.toList());
        return Result.success(setmealVOS);

    }
    /**
     * 根据套餐id查询套餐详情
     * 此方法通过发送GET请求到/dish/{id}来获取特定套餐的详细信息
     * 它主要做了以下工作：
     * 1. 根据提供的套餐id查询关联的菜品信息
     * 2. 将查询到的数据转换为DishItemVO对象列表
     * 3. 返回包含菜品详情列表的Result对象
     *
     * @param id 套餐的唯一标识符
     * @return 包含套餐详情列表的Result对象
     */
    @GetMapping("/dish/{id}")
    @ApiOperation("根据套餐id查询套餐详情")
    public Result<List<DishItemVO>> getDishItemById(@PathVariable Long id) {
        log.info("根据套餐id查询套餐详情:{}", id);
        List<SetmealDish> setmealDishes = setmealDishService.query()
                .eq("setmeal_id", id)
                .list();

        List<DishItemVO> dishItemVOList = setmealDishes.stream().map(setmealDish -> {
            DishItemVO dishItemVO = BeanUtil.toBean(setmealDish, DishItemVO.class);
            Dish dish = dishService.getById(setmealDish.getDishId());
            BeanUtil.copyProperties(dish, dishItemVO);
            return dishItemVO;
        }).collect(Collectors.toList());
        return Result.success(dishItemVOList);
    }

}
