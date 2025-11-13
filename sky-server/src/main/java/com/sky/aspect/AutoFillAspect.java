package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 自动填充切面
 */
@Aspect
@Component
@Slf4j
public class AutoFillAspect {
    /**
     * 自动填充切入点
     */
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut(){}

    /**
     * 自动填充通知
     */
    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint){
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        AutoFill autoFill = methodSignature.getMethod().getAnnotation(AutoFill.class);
        OperationType operationType = autoFill.value();

        Object[] args = joinPoint.getArgs();
        if(args == null || args.length == 0){
            return;
        }
        Object entity = args[0];

        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();

        if(operationType == OperationType.INSERT){
            try {
                entity.getClass().getMethod("setCreateTime", LocalDateTime.class).invoke(entity, now);
                entity.getClass().getMethod("setUpdateTime", LocalDateTime.class).invoke(entity, now);
                entity.getClass().getMethod("setCreateUser", Long.class).invoke(entity, currentId);
                entity.getClass().getMethod("setUpdateUser", Long.class).invoke(entity, currentId);
            } catch (Exception e) {
                log.error("自动填充插入操作失败", e);
            }
        } else if(operationType == OperationType.UPDATE){
            try {
                entity.getClass().getMethod("setUpdateTime", LocalDateTime.class).invoke(entity, now);
                entity.getClass().getMethod("setUpdateUser", Long.class).invoke(entity, currentId);
            } catch (Exception e) {
                log.error("自动填充更新操作失败", e);
            }
        }


    }
}
