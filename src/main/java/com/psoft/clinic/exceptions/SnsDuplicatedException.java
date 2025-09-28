package com.psoft.clinic.exceptions;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;





@ResponseStatus(HttpStatus.CONFLICT)
public class SnsDuplicatedException extends RuntimeException {
    public SnsDuplicatedException(String sns) {
        super( "SNS_NUMBER já existe: " + sns);
    }
}




