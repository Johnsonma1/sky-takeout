package com.sky.controller.admin;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EditPasswordDTO;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.result.Result;
import com.sky.service.EmployeeService;
import com.sky.utils.JwtUtil;
import com.sky.vo.EmployeeLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

/**
 * 员工管理
 */
@RestController
@RequestMapping("/admin/employee")
@Api(tags = "员工相关接口")
@Slf4j
public class EmployeeController {
    @Autowired
    EmployeeService employeeService;

    /**
     * 处理员工登录请求
     *
     * @param dto 员工登录数据传输对象，包含登录凭证（如用户名和密码）
     * @return 包含登录结果的Result对象，数据部分为员工登录后的视图对象
     */
    @PostMapping("/login")
    @ApiOperation("员工登录")
    public Result<EmployeeLoginVO> login(@RequestBody EmployeeLoginDTO dto) {
        // 调用业务层执行登录验证，返回员工登录信息视图对象
        EmployeeLoginVO vo = employeeService.login(dto);
        return Result.success(vo);
    }

    /**
     * 处理员工信息分页查询请求
     *
     * @param dto 员工分页查询的数据传输对象，包含页码、页面大小和查询条件
     * @return 包含员工分页查询结果的响应对象
     */
    @GetMapping("/page")
    @ApiOperation("员工分页条件查询")
    public Result<Page<Employee>> page(EmployeePageQueryDTO dto) {
        // 创建分页对象，传入页码和每页大小
        Page<Employee> page = new Page<>(dto.getPage(), dto.getPageSize());

        // 调用员工服务的查询方法，并根据条件进行分页查询
        employeeService.query()
                .like(dto.getName() != null, "name", dto.getName())
                .orderByDesc("update_time")
                .page(page);

        // 返回成功结果，包含分页查询结果
        return Result.success(page);
    }

    /**
     * 保存员工信息
     *
     * @param dto 员工数据传输对象，包含需要保存的员工信息（通过请求体传递）
     * @return 操作结果，包含成功状态和响应数据
     *
     */
    @PostMapping
    @ApiOperation("保存员工")
    public Result saveEmployee(@RequestBody EmployeeDTO dto) {

        log.info("员工信息：{}", dto);
        Employee employee = BeanUtil.toBean(dto, Employee.class);

        // 设置账号状态（假设1为正常状态）
        employee.setStatus(StatusConstant.ENABLE);

        // 设置初始密码（示例：123456的MD5加密）
        employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));

        // 设置创建/修改时间
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());

        // 设置操作用户ID（需要从登录信息中获取，这里暂设为固定值）
        //TODO 从登录信息中获取
        employee.setCreateUser(BaseContext.getCurrentId());
        employee.setUpdateUser(BaseContext.getCurrentId());

        employeeService.save(employee);
        return Result.success();
    }

    /**
     * 根据员工id查询员工信息
     *
     * @param id 员工id
     * @return 包含员工信息的响应对象
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询员工")
    public Result<Employee> getById(@PathVariable Long id) {
        log.info("根据id查询员工：{}", id);
        Employee employee = employeeService.getById(id);
        return Result.success(employee);
    }

    /**
     * 修改员工信息
     *
     * @param dto 员工数据传输对象，包含需要修改的员工信息（通过请求体传递）
     * @return 操作结果，包含成功状态和响应数据
     */
    @PutMapping
    @ApiOperation("修改员工")
    public Result update(@RequestBody EmployeeDTO dto) {
        log.info("修改员工信息：{}", dto);
        Employee employee = BeanUtil.toBean(dto, Employee.class);
        employee.setUpdateTime(LocalDateTime.now());
        employee.setUpdateUser(BaseContext.getCurrentId());
        employeeService.updateById(employee);
        return Result.success();
    }

    /**启用或禁用员工
     * @param status 状态
     *               0：禁用  1：启用
     */

    @PostMapping("/status/{status}")
    @ApiOperation("启用或禁用员工")
    public Result<String> startOrStop(@PathVariable Integer status, Long id) {
        log.info("启用或禁用员工：{}，{}",status, id);
        employeeService.update()
                .eq("id", id)
                .set("status", status)
                .set("update_time", LocalDateTime.now())
                .update();
        return Result.success();
    }

    /**员工退出
     *
     */
    @PostMapping("/logout")
    @ApiOperation("员工退出")
    public Result<String> logout() {
        log.info("员工退出");
        return Result.success();
    }

      /**
     * 修改员工登录密码
     *
     * @param dto 密码修改请求参数（通过请求体JSON传递），包含：
     *            - empId 必须，员工ID（用于定位员工记录）
     *            - oldPassword 必须，原密码（用于身份验证）
     *            - newPassword 必须，新密码（需符合密码策略要求）
     * @return 统一响应结果对象：
     *         - 成功时返回空数据集
     *         - 失败时包含错误信息（如原密码错误/新密码不符合要求等）
     */
    @PutMapping("/editPassword")
    @ApiOperation("修改密码")
    public Result<String> editPassword(@RequestBody EditPasswordDTO dto, HttpServletRequest request) {
        String token = request.getHeader("token");

        Long empId = JwtUtil.parseJWT(token).get("empId",Long.class);
        boolean update = employeeService.update()
                .eq("id", empId)
                .eq("password", DigestUtils.md5DigestAsHex(dto.getOldPassword().getBytes()))
                .set("password", DigestUtils.md5DigestAsHex(dto.getNewPassword().getBytes()))
                .set("update_time", LocalDateTime.now())
                .update();
        //如果密码修改失败，说明旧密码错误
        if (!update){

            throw new RuntimeException(MessageConstant.PASSWORD_ERROR);
        }

        return Result.success();
    }


}
