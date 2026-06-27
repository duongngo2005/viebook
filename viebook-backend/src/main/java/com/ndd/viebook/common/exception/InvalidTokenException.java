package com.ndd.viebook.common.exception;

import org.springframework.http.HttpStatus;

public class InvalidTokenException extends AppException{
    public InvalidTokenException(HttpStatus status, String message){
        super(status, message);
    }
}
