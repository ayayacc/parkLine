package com.kl.parkLine.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.kl.parkLine.enums.RetCode;

import lombok.Getter;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
@SuppressWarnings("serial")
@Getter
public class BusinessException extends Exception
{
    private RetCode retCode;
    
    public BusinessException(String msg)
    {
        super(msg);
        retCode = RetCode.failed;
    }
    
    public BusinessException(RetCode retCode, String msg)
    {
        super(msg);
        this.retCode = retCode;
    }
}
