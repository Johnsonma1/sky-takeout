package com.sky.controller.admin;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import com.sky.service.DishFlavorService;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 菜品管理
 */
@RestController
@RequestMapping("/admin/dish")
@Api(tags = "菜品相关接口")
@Slf4j
public class DishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private DishFlavorService dishFlavorService;
    @Autowired
    private CategoryService categoryService;


    /**
     * 菜品分页查询接口
     * 该接口允许用户根据页码和页面大小以及可选的查询条件来查询菜品信息
     *
     * @param dto 包含分页和查询条件的传输对象
     * @return 返回一个包含菜品分页数据的结果对象
     */
    @ApiOperation("菜品分页查询")
    @GetMapping("/page")
    public Result<Page<DishVO>> page(DishPageQueryDTO dto) {
        // 创建分页对象并设置页码和页面大小
        Page<Dish> page = new Page<>(dto.getPage(), dto.getPageSize());
        // 根据查询条件进行分页查询
        dishService.query()
                .like(dto.getName() != null, "name", dto.getName())
                .eq(dto.getCategoryId() != null, "category_id", dto.getCategoryId())
                .eq(dto.getStatus() != null, "status", dto.getStatus())
                .page(page);

        // 将查询结果转换为DishVO对象，并设置分类名称
        // 将查询结果转换为DishVO对象，并设置分类名称
        Page<DishVO> convert = (Page<DishVO>) page.convert(dish -> {
            DishVO dishVO = BeanUtil.toBean(dish, DishVO.class);
            dishVO.setCategoryName(categoryService.getById(dishVO.getCategoryId()).getName());
            return dishVO;
        });

        // 返回成功结果
        return Result.success(convert);
    }



    /**
     * 根据菜品ID查询菜品及其关联的口味信息
     *
     * @param id 菜品ID（路径变量）
     * @return 包含菜品详细信息的Result对象，数据封装为DishVO结构（含口味列表）
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询菜品")
    public Result<DishVO> getById(@PathVariable Long id) {
        // 记录查询日志
        log.info("根据id查询菜品:{}", id);

        // 获取菜品基本信息
        Dish dish = dishService.getById(id);

        // 查询关联的口味数据
        List<DishFlavor> flavors = dishFlavorService.query()
                .eq("dish_id", id)
                .list();

        // 转换对象并组装数据
        DishVO dishVO = BeanUtil.toBean(dish, DishVO.class);
        dishVO.setFlavors(flavors);

        return Result.success(dishVO);
    }


    /**
     * 根据分类id查询启用的菜品及其口味数据
     *
     * @param categoryId 菜品分类ID（可为空，为空时不作为查询条件）
     * @return 包含菜品VO列表的通用响应结果，VO中携带菜品口味数据
     *
     * 处理流程：
     * 1. 构造查询条件：当categoryId非空时按分类查询，同时过滤启用状态的菜品
     * 2. 转换领域对象为视图对象，并查询关联的口味数据
     * 3. 将完整的菜品数据（包含口味）封装返回
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<DishVO>> listByCategory(Integer categoryId) {
        log.info("根据分类id查询菜品:{}", categoryId);

        // 查询启用的菜品列表（根据分类条件）
        List<Dish> list = dishService.query()
                .eq(categoryId != null, "category_id", categoryId)
                .eq("status", StatusConstant.ENABLE)
                .list();

        // 转换对象并填充关联的口味数据
        List<DishVO> dishVOS = list.stream().map(dish -> {
            DishVO dishVO = BeanUtil.toBean(dish, DishVO.class);
            List<DishFlavor> dishFlavors = dishFlavorService.query()
                    .eq("dish_id", dish.getId())
                    .list();
            dishVO.setFlavors(dishFlavors);
            return dishVO;
        }).collect(Collectors.toList());

        return Result.success(dishVOS);
    }



    /**
     * 新增菜品
     * 实现菜品基础信息与口味信息的同步新增，包含以下主要步骤：
     * 1. 转换DTO对象并初始化菜品基础信息
     * 2. 批量插入菜品关联的口味数据
     *
     * @param dto 菜品数据传输对象，包含菜品基础信息及其关联的口味列表
     * @return 统一响应结果对象，成功时无具体数据返回
     *
     * @apiNote 操作需要保证菜品基础信息与口味数据的原子性存储
     */
    @PostMapping
    @ApiOperation("新增菜品")
    public Result<Void> save(@RequestBody DishDTO dto) {
        log.info("新增菜品:{}", dto);

        // 初始化并持久化菜品基础信息
        Dish dish = BeanUtil.toBean(dto, Dish.class);
        dish.setCreateTime(LocalDateTime.now());
        dish.setUpdateTime(LocalDateTime.now());
        dish.setStatus(StatusConstant.ENABLE);
        dish.setCreateUser(BaseContext.getCurrentId());
        dish.setUpdateUser(BaseContext.getCurrentId());
        dishService.save(dish);

        // 处理菜品口味关联数据
        List<DishFlavor> flavors = dto.getFlavors();
        flavors.forEach(flavor -> {
            // 建立口味与菜品的关联关系
            flavor.setDishId(dish.getId());
            dishFlavorService.save(flavor);
        });

        return Result.success();
    }

    /**
     * 修改菜品
     *
     * @param dto 菜品数据传输对象，包含待修改菜品的所有属性及关联的口味数据
     * @return 操作结果，成功时无具体返回数据
     */
    @PutMapping
    @ApiOperation("修改菜品")
    public Result<Void> update(@RequestBody DishDTO dto) {
        log.info("修改菜品:{}", dto);

        // 将DTO转换为实体对象，并设置审计字段
        Dish dish = BeanUtil.toBean(dto, Dish.class);
        dish.setUpdateTime(LocalDateTime.now());
        dish.setUpdateUser(BaseContext.getCurrentId());

        // 更新菜品基础信息
        dishService.updateById(dish);

        // 清理旧的口味数据（根据菜品ID删除所有关联口味）
        dishFlavorService.update()
                .eq("dish_id", dish.getId())
                .remove();

        // 批量保存新的口味数据（建立口味与菜品的关联关系）
        dto.getFlavors().forEach(dishFlavor -> {
            dishFlavor.setDishId(dish.getId());
            dishFlavorService.save(dishFlavor);
        });

        return Result.success();
    }

    /**
     * 批量删除菜品*/
    @DeleteMapping
    @ApiOperation("批量删除菜品")
    public Result<Void> delete(@RequestParam List<Long> ids) {
        log.info("批量删除菜品:{}", ids);
        dishService.removeByIds(ids);
        dishFlavorService.update()
                .eq("dish_id", ids)
                .remove();
        return Result.success();
    }

    /**
     * 菜品起售、停售
    * */
    @PostMapping("/status/{status}")
    @ApiOperation("菜品起售、停售")
    public Result<Void> startOrStop(@PathVariable Integer status, Long id) {
        log.info("菜品起售、停售:{},{}", status, id);
        dishService.update()
                .eq("id", id)
                .set("status", status)
                .set("update_time", LocalDateTime.now())
                .update();
        return Result.success();
    }



}
