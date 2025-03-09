package com.sky.controller.admin;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import com.sky.service.SetmealDishService;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 套餐管理
 */
@RestController
@RequestMapping("/admin/setmeal")
@Api(tags = "套餐相关接口")
@Slf4j
public class SetmealController {
    @Autowired
    private SetmealService setmealService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private SetmealDishService setmealDishService;

    @GetMapping("/page")
    @ApiOperation("套餐分页查询")
    public Result <Page<SetmealVO>> page(SetmealPageQueryDTO dto) {
        log.info("套餐分页查询：{}", dto);
        // 创建分页对象并设置页码和页面大小
        Page<Setmeal> page = new Page<>(dto.getPage(), dto.getPageSize());

        //根据查询条件进行分页查询
        setmealService.query()
                .like(dto.getName() != null, "name", dto.getName())
                .eq(dto.getCategoryId() != null, "category_id", dto.getCategoryId())
                .eq(dto.getStatus() != null, "status", dto.getStatus())
                .page(page);
        // 将查询结果转换为SetmealVO对象，并设置分类名称
        Page<SetmealVO> convert = (Page<SetmealVO>) page.convert(setmeal -> {
            SetmealVO setmealVO = BeanUtil.toBean(setmeal, SetmealVO.class);
            setmealVO.setCategoryName(categoryService.getById(setmealVO.getCategoryId()).getName());
            return setmealVO;
        });

        return Result.success(convert);
    }

    /**根据id查询套餐
     *
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询套餐")
    public Result<SetmealVO> getById(@PathVariable Long id) {
        log.info("根据id查询套餐：{}", id);
        Setmeal setmeal = setmealService.getById(id);
        SetmealVO setmealVO = BeanUtil.toBean(setmeal, SetmealVO.class);
        setmealVO.setCategoryName(categoryService.getById(setmealVO.getCategoryId()).getName());
        return Result.success(setmealVO);
    }

    /**
     * 新增套餐
     * @param dto
     * @return
     */
    @PostMapping
    @ApiOperation("新增套餐")
    public Result<Void> save(@RequestBody SetmealDTO dto) {
        log.info("新增套餐：{}", dto);
       Setmeal setmeal = BeanUtil.toBean(dto, Setmeal.class);
       setmeal.setCreateTime(LocalDateTime.now());
       setmeal.setUpdateTime(LocalDateTime.now());
       setmeal.setCreateUser(BaseContext.getCurrentId());
       setmeal.setUpdateUser(BaseContext.getCurrentId());
       setmeal.setStatus(StatusConstant.ENABLE);
       setmealService.save(setmeal);

        List<SetmealDish> setmealDishes = dto.getSetmealDishes();
        setmealDishes.forEach(setmealDish -> {
            setmealDish.setSetmealId(setmeal.getId());
            setmealDishService.save(setmealDish);

        });
        return Result.success();
    }

    /**
     * 修改套餐
     * @param dto
     * @return
     */
    @PutMapping
    @ApiOperation("修改套餐")
    public Result<Void> update(@RequestBody SetmealDTO dto) {
        log.info("修改套餐：{}", dto);
        Setmeal setmeal = BeanUtil.toBean(dto, Setmeal.class);
        setmeal.setUpdateTime(LocalDateTime.now());
        setmeal.setUpdateUser(BaseContext.getCurrentId());
        setmealService.updateById(setmeal);

        setmealDishService.update()
                .eq("dish_id", dto.getId())
                .remove();
        dto.getSetmealDishes().forEach(setmealDish -> {
            setmealDish.setSetmealId(setmeal.getId());
            setmealDishService.save(setmealDish);
        });
        return Result.success();
    }

    /**
     * 批量删除套餐
     *
     */
    @DeleteMapping
    @ApiOperation("批量删除套餐")
    public Result<Void> delete(@RequestParam List<Long> ids) {
        log.info("批量删除套餐：{}", ids);
        setmealService.update()
                .in("id", ids)
                .remove();
        return Result.success();
    }

    /**
     * 套餐起售停售
     * @param status
     * @param id
     * @return
     */
    @PostMapping("/status/{status}")
    @ApiOperation("套餐起售停售")
    public Result<Void> startOrStop(@PathVariable Integer status, Long id) {
        log.info("套餐起售停售：{}", status);
        setmealService.update()
                .eq("id", id)
                .set("status", status)
                .set("update_time", LocalDateTime.now())
                .update();
        return Result.success();
    }





}