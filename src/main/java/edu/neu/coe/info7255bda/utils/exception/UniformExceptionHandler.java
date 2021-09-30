package edu.neu.coe.info7255bda.utils.exception;


import edu.neu.coe.info7255bda.constant.StatusCode;
import edu.neu.coe.info7255bda.model.VO.ResultData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class UniformExceptionHandler {

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResultData<String> parameterBodyMissingException(HttpMessageNotReadableException e) {
        log.error("Error message: {}", e.getMessage(), e);
        return ResultData.fail(StatusCode.PARAMETER_BODY_ERROR.getCode(), StatusCode.PARAMETER_BODY_ERROR.getMessage());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ResultData<String> requestMethodException(HttpRequestMethodNotSupportedException e) {
        log.error("Error message: {}", e.getMessage(), e);
        return ResultData.fail(StatusCode.REQUEST_METHOD_ERROR.getCode(), StatusCode.REQUEST_METHOD_ERROR.getMessage());
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResultData<String> requestParamException(HttpMediaTypeNotSupportedException e) {
        log.error("Error message: {}", e.getMessage(), e);
        return ResultData.fail(StatusCode.PARAMETER_TYPE_ERROR.getCode(), StatusCode.PARAMETER_TYPE_ERROR.getMessage());
    }

    @ExceptionHandler(CustomerException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResultData<String> jsonFormatException(CustomerException e) {
        log.error("Error message: {}", e.getMessage(), e);
        return ResultData.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResultData<String> unknownException(Exception e) {
        log.error("Error message: {}", e.getMessage(), e);
        return ResultData.fail(StatusCode.UNKNOWN_ERROR.getCode(), StatusCode.UNKNOWN_ERROR.getMessage());
    }
}
