package com.gcloud.demo.uploaddemo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice
public class ExceptionHandlerAdvice
{
//    @ExceptionHandler(value = GcException.class)
//    public ResponseEntity<ResponseResult<ErrorInfo>> handleServiceException(GcException ex, HttpServletRequest request)
//    {
//        ErrorInfo err = ExceptionUtil.errExceptionHandle(ex, request);
//        return new ResponseEntity.ok;
//    }
}
