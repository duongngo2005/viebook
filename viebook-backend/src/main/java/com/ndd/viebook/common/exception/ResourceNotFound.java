package com.ndd.viebook.common.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFound extends AppException {
    public ResourceNotFound(HttpStatus status, String message){
        super(status, message);
    }
}
