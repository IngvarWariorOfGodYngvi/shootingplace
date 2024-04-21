package com.shootingplace.shootingplace.exceptionHandlers;

import com.shootingplace.shootingplace.exceptionHandlers.Exceptions.NoPersonToAmmunitionException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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
    @ExceptionHandler(value = NoPersonToAmmunitionException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleNoPersonToAmmunitionException(NoPersonToAmmunitionException ex) {
        LOG.error(ex.getMessage());
        return "Wprowadź osobę by wydać amunicję.";
    }
//    @ExceptionHandler(value = IllegalArgumentException.class)
//    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
//    public ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException ex) {
//        LOG.info(ex.getMessage() + " " + ex.getCause());
//        return ResponseEntity.badRequest().body("Sprawdź wysyłane warunki");
//    }

}