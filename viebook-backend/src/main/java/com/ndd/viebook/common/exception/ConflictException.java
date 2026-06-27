package com.ndd.viebook.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
@Getter
public class ConflictException extends AppException {
    public ConflictException(HttpStatus status, String message){
        super(status, message);
    }
}
