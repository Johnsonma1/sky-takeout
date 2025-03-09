package com.sky.controller.admin;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 分类管理
 */
@RestController
@RequestMapping("/admin/category")
@Api(tags = "分类相关接口")
@Slf4j
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /**分类分页查询
     * @param
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("分页查询分类")
    public Result <Page<Category>> page(CategoryPageQueryDTO dto){
        log.info("分页查询分类:{}",dto);
        Page page = new Page(dto.getPage(),dto.getPageSize());

        categoryService.query()
                .like(dto.getName() != null, "name", dto.getName())
                .eq(dto.getType() != null, "type", dto.getType())
                .orderByDesc("update_time")
                .page(page);

        return Result.success(page);
    }

    /**新增分类
     * @param
     * @return
     */
    @PostMapping
    @ApiOperation("新增分类")
    public Result saveCategory(@RequestBody CategoryDTO dto){
        log.info("新增分类:{}",dto);
        Category category = BeanUtil.toBean(dto, Category.class);
        category.setStatus(StatusConstant.ENABLE);
        category.setCreateTime(LocalDateTime.now());
        category.setUpdateTime(LocalDateTime.now());
        category.setCreateUser(BaseContext.getCurrentId());
        category.setUpdateUser(BaseContext.getCurrentId());
        categoryService.save(category);
        return Result.success();
    }


    /**修改分类
     * @param
     * @return
     */
    @PutMapping
    @ApiOperation("修改分类")
    public Result  update(@RequestBody CategoryDTO dto){
        log.info("修改分类:{}",dto);
        Category category = BeanUtil.toBean(dto, Category.class);
        category.setUpdateTime(LocalDateTime.now());
        category.setUpdateUser(BaseContext.getCurrentId());
        categoryService.updateById(category);
        return Result.success();
    }


    /**
     * 根据类型查询分类
     *
     * @param type 分类的类型，可选参数
     * @return 返回查询结果，包含符合条件的分类列表
     */
    @GetMapping("/list")
    @ApiOperation("根据类型查询分类")
    public Result<List<Category>> list(Integer type) {
        // 调用服务层方法按类型查询分类
        List<Category> list = categoryService.query()
                .eq(type != null, "type", type)
                .list();
        // 返回查询结果
        return Result.success(list);
    }

    /**
     * 启用禁用分类
     * @param status 分类状态，可选参数
     *               0：禁用 1：启用
     */
    @PostMapping("/status/{status}")
    @ApiOperation("启用或禁用分类")
    public Result<String> startOrStop(@PathVariable Integer status, Long id) {
        log.info("启用或禁用分类:{},{}", status, id);
        // 调用服务层方法启用禁用分类
        categoryService.update()
                .eq("id", id)
                .set("status", status)
                .set("update_time", LocalDateTime.now())
                .update();
        return Result.success();
    }

    /**根据id删除分类
     * @param id 分类id
     */
    @DeleteMapping
    @ApiOperation("根据id删除分类")
    public Result deleteById(Long id) {
        log.info("根据id删除分类:{}", id);
        // 调用服务层方法根据id删除分类
        categoryService.removeById(id);
        return Result.success();
    }

}
