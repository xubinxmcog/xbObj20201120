package com.enuos.live.exception;

import com.enuos.live.error.BussinessException;
import com.enuos.live.feign.ExceptionFeign;
import com.enuos.live.result.Result;
import io.lettuce.core.RedisConnectionException;
import org.apache.ibatis.exceptions.TooManyResultsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.FileNotFoundException;
import javax.servlet.http.HttpServletRequest;

@ControllerAdvice
@ResponseBody
public class CommonExceptionAdvice {

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private ExceptionFeign exceptionFeign;

    private static Logger logger = LoggerFactory.getLogger(CommonExceptionAdvice.class);

    /**
     * 400 - Bad Request
     */
    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        logger.error("缺少请求参数=[{}], 接口=[{}]", e.getParameterName(), request.getRequestURL(), e);
        try {
            if (!request.getRequestURL().toString().contains("/exception")) {
                exceptionFeign.saveException(String.valueOf(request.getRequestURL()), "515", "缺少请求参数", e.getMessage());
            }
        } catch (Exception e1) {
        }
        return Result.error(500, "服务繁忙，请稍后（515）");
    }

    /**
     * 400 - Bad Request
     */
    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        logger.error("参数解析失败, 接口=[{}]", request.getRequestURL(), e);
        try {
            if (!request.getRequestURL().toString().contains("/exception")) {
                exceptionFeign.saveException(String.valueOf(request.getRequestURL()), "516", "参数解析失败", e.getMessage());
            }
        } catch (Exception e1) {
        }
        return Result.error(500, "服务繁忙，请稍后（516）");
    }

    /**
     * 400 - Bad Request
     */
    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        logger.error("参数验证失败, 接口=[{}]", request.getRequestURL(), e);
        try {
            if (!request.getRequestURL().toString().contains("/exception")) {
                exceptionFeign.saveException(String.valueOf(request.getRequestURL()), "517", "参数验证失败", e.getMessage());
            }
        } catch (Exception e1) {
        }
        return Result.error(500, "服务繁忙，请稍后（517）");
    }

    /**
     * 400 - Bad Request
     */
    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(BindException.class)
    public Result handleBindException(BindException e) {
        logger.error("参数绑定失败, 接口=[{}]", request.getRequestURL(), e);
        try {
            if (!request.getRequestURL().toString().contains("/exception")) {
                exceptionFeign.saveException(String.valueOf(request.getRequestURL()), "518", "参数绑定失败", e.getMessage());
            }
        } catch (Exception e1) {
        }
        return Result.error(500, "服务繁忙，请稍后（518）");
    }


    /**
     * 405 - Method Not Allowed
     */
    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        logger.error("不支持当前请求方法, 接口=[{}]", request.getRequestURL(), e);
        try {
            if (!request.getRequestURL().toString().contains("/exception")) {
                exceptionFeign.saveException(String.valueOf(request.getRequestURL()), "519", "不支持当前请求方法", e.getMessage());
            }
        } catch (Exception e1) {
        }
        return Result.error(500, "不支持当前请求方法（519）");
    }

    /**
     * 415 - Unsupported Media Type
     */
    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public Result handleHttpMediaTypeNotSupportedException(Exception e) {
        logger.error("不支持当前媒体类型, 接口=[{}]", request.getRequestURL(), e);
        try {
            if (!request.getRequestURL().toString().contains("/exception")) {
                exceptionFeign.saveException(String.valueOf(request.getRequestURL()), "520", "不支持当前媒体类型", e.getMessage());
            }
        } catch (Exception e1) {
        }
        return Result.error(500, "服务繁忙，请稍后（520）");
    }

    /**
     * 500 - Internal Server Error
     */
    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(BussinessException.class)
    public Result handleServiceException(BussinessException e) {
        try {
            if (!request.getRequestURL().toString().contains("/exception")) {
                exceptionFeign.saveException(String.valueOf(request.getRequestURL()), "500", e.getCode() + "_" + e.getMessage(), e.getMessage());
            }
        } catch (Exception e1) {
        }
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 500 - Internal Server Error
     */
    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(Exception.class)
    public Result handleException(Exception e) {
        logger.error("通用异常, 接口=[{}]", request.getRequestURL(), e);
        String msg = "";
        String code = "555";
        if (e instanceof NullPointerException) {
            //空指针异常
            msg = "空指针异常";
            code = "501";
//            return Result.error(500, "服务繁忙，请稍后（501）");
        } else if (e instanceof ClassCastException) {
            //类型强制转换异常
            msg = "类型强制转换异常";
            code = "502";
//            return Result.error(500, "服务繁忙，请稍后（502）");
        } else if (e instanceof ArrayIndexOutOfBoundsException) {
            //数组越界
            msg = "数组越界";
            code = "503";
//            return Result.error(500, "服务繁忙，请稍后（503）");
        } else if (e instanceof NumberFormatException) {
            //数组转换异常
            msg = "数组转换异常";
            code = "504";
//            return Result.error(500, "服务繁忙，请稍后（504）");
        } else if (e instanceof NoSuchMethodException) {
            //方法未找到异常
            msg = "方法未找到异常";
            code = "505";
//            return Result.error(500, "服务繁忙，请稍后（505）");
        } else if (e instanceof ArithmeticException) {
            //算数异常
            msg = "算数异常";
            code = "506";
//            return Result.error(500, "服务繁忙，请稍后（506）");
        } else if (e instanceof IllegalAccessException) {
            //访问权限异常
            msg = "访问权限异常";
            code = "507";
//            return Result.error(500, "服务繁忙，请稍后（507）");
        } else if (e instanceof ArrayStoreException) {
            //数组存储异常
            msg = "数组存储异常";
            code = "508";
//            return Result.error(500, "服务繁忙，请稍后（508）");
        } else if (e instanceof FileNotFoundException) {
            //文件不存在异常
            msg = "文件不存在异常";
            code = "509";
//            return Result.error(500, "服务繁忙，请稍后（509）");
        } else if (e instanceof InstantiationException) {
            //实例化异常
            msg = "实例化异常";
            code = "510";
//            return Result.error(500, "服务繁忙，请稍后（510）");
        } else if (e instanceof BadSqlGrammarException) {
            //sql错误
            msg = "SQL错误";
            code = "511";
//            return Result.error(500, "服务繁忙，请稍后（511）");
        } else if (e instanceof TooManyResultsException) {
            //sql错误
            msg = "SQL返回结果过多";
            code = "512";
//            return Result.error(500, "服务繁忙，请稍后（512）");
        }
        try {
            if (!request.getRequestURL().toString().contains("exception")) {
                exceptionFeign.saveException(String.valueOf(request.getRequestURL()), code, msg, e.getMessage());
            }
        } catch (Exception e1) {
        }
        return Result.error(500, "服务繁忙，请稍后（" + code + "）");
    }

    /**
     * 操作数据库出现异常:名称重复，外键关联
     */
    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public Result handleException(DataIntegrityViolationException e) {
        logger.error("操作数据库出现异常, 接口=[{}]", request.getRequestURL(), e);
        try {
            if (!request.getRequestURL().toString().contains("/exception")) {
                exceptionFeign.saveException(String.valueOf(request.getRequestURL()), "514", "操作数据库出现异常", e.getMessage());
            }
        } catch (Exception e1) {
        }
        return Result.error(500, "服务繁忙，请稍后（514）");
    }

    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(RedisConnectionException.class)
    public Result handleException(RedisConnectionException e) {
        logger.error("远程链接Redis服务失败, 接口=[{}]", request.getRequestURL(), e);
        try {
            if (!request.getRequestURL().toString().contains("/exception")) {
                exceptionFeign.saveException(String.valueOf(request.getRequestURL()), "530", "远程链接Redis服务失败", e.getMessage());
            }
        } catch (Exception e1) {
        }
        return Result.error(500, "服务繁忙，请稍后（530）");
    }
}
