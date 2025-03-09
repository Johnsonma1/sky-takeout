package com.sky.controller.user;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.service.SetmealService;
import com.sky.service.ShoppingCartService;
import com.sky.utils.JwtUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 购物车
 */
@RestController
@RequestMapping("/user/shoppingCart")
@Slf4j
@Api(tags = "C端-购物车接口")
public class ShoppingCartController {
    @Autowired
    private ShoppingCartService shoppingCartService;
    @Autowired
    private DishService dishService;
    @Autowired
    private SetmealService setmealService;
    /**
     * 添加购物车项
     *
     * @param dto 购物车数据传输对象，包含菜品/套餐ID、口味等信息
     * @param request HTTP请求对象，用于获取用户凭证
     * @return 操作结果（成功/失败）
     */
    @ApiOperation("添加购物车")
    @PostMapping("/add")
    public Result<Void> add(@RequestBody ShoppingCartDTO dto, HttpServletRequest request) {
        String token = request.getHeader("authentication");
        Long userId = JwtUtil.parseJWT(token).get("userId", Long.class);

        ShoppingCart shoppingCart = BeanUtil.toBean(dto, ShoppingCart.class);
        shoppingCart.setUserId(userId);

        //1. 判断用户的购物车中是否包含商品
        // select * from shopping_cart where user_id=? and dish_id=? and setmeal_id=? and dish_flavor=?
        List<ShoppingCart> list = shoppingCartService.query()
                .eq(userId != null, "user_id", userId)
                .eq(dto.getDishId() != null, "dish_id", dto.getDishId())
                .eq(dto.getSetmealId() != null, "setmeal_id", dto.getSetmealId())
                .eq(dto.getDishFlavor() != null, "dish_flavor", dto.getDishFlavor())
                .list();

        if (CollUtil.isNotEmpty(list)) {
            //2. 如果包含，更新操作，数量+1
            ShoppingCart dbCart = list.get(0); //dbCart表示数据库中的一条数据
            dbCart.setNumber(dbCart.getNumber() + 1);
            shoppingCartService.updateById(dbCart);
        } else {
            //3. 如果不包含，添加操作，数量默认1
            //补全信息
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCart.setNumber(1); //默认数量1
            shoppingCart.setCreateTime(LocalDateTime.now());
            if (shoppingCart.getDishId() != null) {
                //根据菜品id查询菜品信息
                Dish dish = dishService.getById(shoppingCart.getDishId());
                shoppingCart.setName(dish.getName());
                shoppingCart.setImage(dish.getImage());
                shoppingCart.setAmount(dish.getPrice());
            } else {
                //根据套餐id查询套餐信息
                Setmeal setmeal = setmealService.getById(shoppingCart.getSetmealId());
                shoppingCart.setName(setmeal.getName());
                shoppingCart.setImage(setmeal.getImage());
                shoppingCart.setAmount(setmeal.getPrice());
            }
            //添加购物项
            shoppingCartService.save(shoppingCart);
        }

        return Result.success();
    }

    @ApiOperation("查看购物车")
    @GetMapping("/list")
    public Result<List<ShoppingCart>> list(HttpServletRequest request){
        log.info("查看购物车");

        /* 解析用户身份 */
        String token = request.getHeader("authentication");
        log.info("token:{}",token);
        Long userId = JwtUtil.parseJWT(token).get("userId",Long.class);

        /* 查询购物车项 */
        List<ShoppingCart> list = shoppingCartService.query()
                .eq("user_id", userId)
                .list();

        return Result.success(list);
    }

    @ApiOperation("清空购物车")
    @DeleteMapping("/clean")
    public Result<Void> clean(HttpServletRequest request){
        log.info("清空购物车");

        /* 解析用户身份 */
        String token = request.getHeader("authentication");
        Long userId = JwtUtil.parseJWT(token).get("userId",Long.class);

        /* 删除购物车项 */
        shoppingCartService.update()
                .eq("user_id", userId)
                .remove();
        return Result.success();
    }
      /**
     * 减少购物车商品数量
     *
     * @param dto 购物车数据传输对象，包含要减少的菜品/套餐信息
     * @param request HTTP请求对象，用于获取用户凭证
     * @return 操作结果（成功/失败）
     */
    @ApiOperation("删除购物车中一个商品")
    @PostMapping("/sub")
    public Result<Void> sub(@RequestBody ShoppingCartDTO dto, HttpServletRequest request){
        log.info("删除购物车中一个商品:{}",dto);

        /* 解析用户身份 */
        String token = request.getHeader("authentication");
        Long userId = JwtUtil.parseJWT(token).get("userId",Long.class);

        /* 构建查询条件（相同商品+相同用户+相同口味） */
        List<ShoppingCart> list = shoppingCartService.query()
                .eq("user_id", userId)
                .eq(dto.getDishId() != null, "dish_id", dto.getDishId())
                .eq(dto.getSetmealId() != null, "setmeal_id", dto.getSetmealId())
                .eq(dto.getDishFlavor() != null, "dish_flavor", dto.getDishFlavor())
                .list();

        /* 处理存在的购物车项 */
        if(CollUtil.isNotEmpty(list)){
            ShoppingCart cart = list.get(0);
            if(cart.getNumber() > 1){
                // 数量减一
                cart.setNumber(cart.getNumber() - 1);
                shoppingCartService.updateById(cart);
            }else{
                // 数量为1时直接删除
                shoppingCartService.removeById(cart.getId());
            }
        }
        return Result.success();
    }

}
