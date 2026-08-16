package common.exception;

import common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    // 业务异常
    @ExceptionHandler(BizException.class)
    public Result<?> handleBizException(BizException e){
        log.warn("业务异常: {}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    // 参数校验异常
    @ExceptionHandler(BindException.class)
    public Result<?> handleBindException(BindException e) {
        String msg = e.getBindingResult().getAllErrors()
                .stream()
                .map(error -> error.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("参数校验失败");
        log.warn("参数校验异常: {}", msg);
        return Result.error(400, msg);
    }

    // 兜底异常
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        log.error("系统异常: ", e);
        return Result.error(500, "服务器内部错误");
    }
}


