package me.ezzi.myimagehostbackend.handler;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotRoleException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import me.ezzi.myimagehostbackend.common.constant.MessageConstant;
import me.ezzi.myimagehostbackend.exception.BaseException;
import me.ezzi.myimagehostbackend.pojo.entity.Result;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.stream.Collectors;

/**
 * 全局异常处理器，处理项目中抛出的业务异常
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 捕获业务异常
     */
    @ExceptionHandler(BaseException.class)
    public Result<?> exceptionHandler(BaseException ex) {
        log.error("业务异常：{}", ex.getMessage());
        return Result.error(ex.getMessage());
    }

    /**
     * 捕获 Sa-Token 未登录异常
     */
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(NotLoginException.class)
    public Result<?> exceptionHandler(NotLoginException ex) {
        log.warn("用户未登录或 Token 无效：{}", ex.getMessage());
        return Result.error(MessageConstant.USER_NOT_LOGIN);
    }

    /**
     * 捕获 Sa-Token 无权限异常
     */
    @ExceptionHandler(NotRoleException.class)
    public Result<?> exceptionHandler(NotRoleException ex) {
        log.warn("无权限访问：{}", ex.getMessage());
        return Result.error(MessageConstant.NO_PERMISSION);
    }

    /**
     * 捕获参数校验异常 (MethodArgumentNotValidException)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> exceptionHandler(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("参数校验失败：{}", message);
        return Result.error(message);
    }

    /**
     * 捕获参数校验异常 (BindException)
     */
    @ExceptionHandler(BindException.class)
    public Result<?> exceptionHandler(BindException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("参数绑定失败：{}", message);
        return Result.error(message);
    }

    /**
     * 捕获参数校验异常 (ConstraintViolationException)
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<?> exceptionHandler(ConstraintViolationException ex) {
        String message = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
        log.warn("参数约束校验失败：{}", message);
        return Result.error(message);
    }

    /**
     * 捕获 SQL 完整性约束异常
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public Result<?> exceptionHandler(SQLIntegrityConstraintViolationException ex) {
        String message = ex.getMessage();
        if (message.contains("Duplicate entry")) {
            String[] split = message.split(" ");
            String repeatedContent = split.length > 2 ? split[2] : "数据";
            String msg = repeatedContent + MessageConstant.ALREADY_EXISTS;
            log.error("数据库唯一约束冲突：{}", msg);
            return Result.error(msg);
        } else {
            log.error("数据库异常：{}", message);
            return Result.error(MessageConstant.UNKNOWN_ERROR);
        }
    }

    /**
     * 捕获其他未知异常
     */
    @ExceptionHandler(Exception.class)
    public Result<?> exceptionHandler(Exception ex) {
        log.error("系统未知异常", ex);
        return Result.error(MessageConstant.UNKNOWN_ERROR);
    }
}
