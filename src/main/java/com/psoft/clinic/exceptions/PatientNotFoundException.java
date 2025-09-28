package com.psoft.clinic.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class PatientNotFoundException extends ResponseStatusException {

    public PatientNotFoundException(String fullName) {
        super(HttpStatus.NOT_FOUND, "Paciente não encontrado com o nome: " + fullName);
    }
}