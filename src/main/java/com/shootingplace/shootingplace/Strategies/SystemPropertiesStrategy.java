package com.shootingplace.shootingplace.Strategies;

import java.time.LocalDateTime;

public interface SystemPropertiesStrategy {
    default void setDateTimeProperty() {
        System.setProperty("dateTime", LocalDateTime.now().toString());
    }

}
