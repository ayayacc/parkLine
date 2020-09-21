package com.kl.parkLine.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
@SuppressWarnings("serial")
public class BusinessException extends Exception
{
    public BusinessException(String msg)
    {
        super(msg);
    }
}
