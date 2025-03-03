package com.sky.aspect;

import com.sky.annatation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * AOP类：公共字段的自动填充（解析@AutoFill注解）
 */
@Slf4j
@Component
@Aspect
public class AutoFillAspect {


    /**
     * 切入点表达式：寻找所有Mapper中带@AutoFill注解的方法
     *
     * @param joinPoint
     */
    @Before("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annatation.AutoFill)")
    @SneakyThrows //抑制注解
    public void before(JoinPoint joinPoint) {
        //1. 获取mapper方法上的注解的值类型
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class); //获取方法上的指定注解
        OperationType operationType = autoFill.value(); //操作类型

        //2. 获取mapper方法上的实际参数
        Object entity = joinPoint.getArgs()[0]; //直接获取mapper方法上的第一个参数

        //3. 通过反射对实际参数赋值（添加4个值，修改2个值）
        if (operationType == OperationType.INSERT) {
            //通过反射获取指定的返回
            Method method1 = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
            Method method2 = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
            Method method3 = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
            Method method4 = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

            //运行方法
            method1.invoke(entity, LocalDateTime.now());
            method2.invoke(entity, LocalDateTime.now());
            method3.invoke(entity, BaseContext.getCurrentId());
            method4.invoke(entity, BaseContext.getCurrentId());
        }

        if (operationType == OperationType.UPDATE) {
            //通过反射获取指定的返回
            Method method2 = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
            Method method4 = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

            //运行方法
            method2.invoke(entity, LocalDateTime.now());
            method4.invoke(entity, BaseContext.getCurrentId());
        }

    }


}
