package com.kl.parkLine.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
@SuppressWarnings("serial")
public class EventException extends Exception
{
    public EventException(String msg)
    {
        super(msg);
    }
}
