package com.sky.handler;

import com.sky.constant.MessageConstant;
import com.sky.exception.BaseException;
import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理器，处理项目中抛出的业务异常
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 捕获业务异常
     *
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result<String> baseExceptionHandler(BaseException ex) {
        log.error("异常信息：{}", ex.getMessage());
        ex.printStackTrace(); //输出到控制台
        return Result.error(ex.getMessage());
    }

    /**
     * 处理  SQLIntegrityConstraintViolationException 异常
     *
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result<String> SQLIntegrityConstraintViolationHandler(SQLIntegrityConstraintViolationException ex) {
        log.error("异常信息：{}", ex.getMessage());
        ex.printStackTrace(); //输出到控制台
        String msg = ex.getMessage().split(" ")[2]; //截取用户输入的信息
        return Result.error(msg + MessageConstant.ALREADY_EXISTS);
    }


    /**
     * 全局默认异常处理
     *
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result<String> exceptionHandler(Exception ex) {
        log.error("异常信息：{}", ex.getMessage());
        ex.printStackTrace(); //输出到控制台
        return Result.error(MessageConstant.UNKNOWN_ERROR);
    }


}
