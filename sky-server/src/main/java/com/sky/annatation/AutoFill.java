package com.sky.annatation;

import com.sky.enumeration.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义注解：自动填充属性
 */

@Retention(RetentionPolicy.RUNTIME) //注解存在的范围：运行期
@Target({ElementType.METHOD}) //注解添加的位置：方法上
public @interface AutoFill {


    OperationType value(); //操作的类型，枚举类

}
