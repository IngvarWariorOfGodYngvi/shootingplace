package com.shootingplace.shootingplace.exceptionHandlers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.persistence.EntityNotFoundException;
import javax.validation.ConstraintViolationException;

@RestControllerAdvice
public class ExceptionsHandler {
    private final Logger LOG = LogManager.getLogger(getClass());


    @ExceptionHandler(value = MissingPathVariableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleMissingPathVariableException(Exception ex) {
        return ex.getMessage();
    }

    @ExceptionHandler(value = ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleConstraintViolationException(Exception ex) {
        LOG.error(ex.getMessage() + " Wprowadzono nieprawidłowe dane");
        return " Wprowadzono nieprawidłowe dane";
    }

    @ExceptionHandler(value = IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    public String handleIllegalArgumentException(IllegalArgumentException ex) {
        LOG.info(ex.getMessage() + " " + ex.getCause());
        return "Wprowadzono błędne dane";
    }

    @ExceptionHandler(value = EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<?> handleEntityNotFoundException(EntityNotFoundException ex) {
        LOG.error(ex.getMessage() + " Nie znaleziono encji więc nie można wykonać żądania");
        return ResponseEntity.badRequest().body(" Wprowadzono błędne dane");
    }

}