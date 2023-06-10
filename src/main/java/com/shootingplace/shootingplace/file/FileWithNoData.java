package com.shootingplace.shootingplace.file;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
@Data
public class FileWithNoData {
    private String uuid;
    private String belongToMemberUUID;

    private String name;
    private String type;

    private LocalDate date;
    private LocalTime time;
    private long size;
    private int version;
}
