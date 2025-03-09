package com.sky.controller.user;

import com.sky.entity.AddressBook;
import com.sky.result.Result;
import com.sky.service.AddressBookService;
import com.sky.utils.JwtUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/addressBook")
@Api(tags = "C端地址簿接口")
public class AddressBookController {

    @Autowired
    private AddressBookService addressBookService;

    @ApiOperation("查询默认地址")
    @GetMapping("/default")
    public Result<AddressBook> getDefault(@RequestHeader String authentication) {
        Long userId = JwtUtil.parseJWT(authentication).get("userId", Long.class);
        AddressBook addressBook = addressBookService.query()
                .eq("user_id", userId)
                .eq("is_default", 1)
                .one();
        return Result.success(addressBook);
    }

    @ApiOperation("查询当前登录用户的所有地址信息")
    @GetMapping("/list")
    public Result<List<AddressBook>> getAddressList(@RequestHeader String authentication) {
        Long userId = JwtUtil.parseJWT(authentication).get("userId", Long.class);
        List<AddressBook> list = addressBookService.query()
                .eq("user_id", userId)
                .list();
        return Result.success(list);
    }


    @PostMapping
    @ApiOperation("新增地址")
    public Result<Void> saveAddress(@RequestBody AddressBook address, @RequestHeader String authentication){
        // 从认证信息中解析用户ID
        Long userId = JwtUtil.parseJWT(authentication).get("userId", Long.class);
        address.setUserId(userId);
        address.setIsDefault(0);
        addressBookService.save(address);
        return Result.success();
    }

    @PutMapping
    @ApiOperation("根据id修改地址")
    public Result<Void> update(@RequestBody AddressBook addressBook) {
        addressBookService.updateById(addressBook);
        return Result.success();
    }

    @DeleteMapping
    @ApiOperation("根据id删除地址")
    public Result<Void> deleteById(Long id) {
        addressBookService.removeById(id);
        return Result.success();
    }

    @PutMapping("/default")
    @ApiOperation("设置用户默认地址")
    public Result<Void> setDefaultAddress(@RequestBody AddressBook address, @RequestHeader String authentication) {
        // 从认证信息中解析用户ID
        Long userId = JwtUtil.parseJWT(authentication).get("userId", Long.class);

        //1. 设置用户的所有地址为非默认地址
        addressBookService.update()
                .eq("user_id", userId)
                .set("is_default", 0)
                .update();
        //2. 修改当前地址为默认地址
        addressBookService.update()
                .eq("id", address.getId())
                .set("is_default", 1)
                .update();

        return Result.success();
    }

    @GetMapping("/{id}")
    @ApiOperation("根据id查询地址")
    public Result<AddressBook> getById(@PathVariable Long id) {
        AddressBook addressBook = addressBookService.getById(id);
        return Result.success(addressBook);
    }



}
