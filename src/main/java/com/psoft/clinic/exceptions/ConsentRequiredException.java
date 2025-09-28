package com.psoft.clinic.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ConsentRequiredException extends RuntimeException {
    public ConsentRequiredException() {
        super("Você deve aceitar o consentimento para concluir o registro.");
    }
}
