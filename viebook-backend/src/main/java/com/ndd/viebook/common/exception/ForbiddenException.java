package com.ndd.viebook.common.exception;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends AppException {
    public ForbiddenException(HttpStatus status, String message){
        super(status, message);
    }
}
