package com.shootingplace.shootingplace.exceptions;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NoUserPermissionException extends Exception {
    private final Logger LOG = LogManager.getLogger(getClass());

    public NoUserPermissionException(String errorMessage, Throwable err) {
        super(errorMessage, err);
    }

    public NoUserPermissionException() {
        super();
    }

    public NoUserPermissionException(String message) {
        super(message);
    }
}
