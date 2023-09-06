package com.shootingplace.shootingplace.file;

import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.core.annotation.Order;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@AllArgsConstructor
@Builder
public class FilesEntity {

    @Id
    @GeneratedValue(generator = "id")
    @GenericGenerator(name = "id", strategy = "org.hibernate.id.UUIDGenerator")
    private String uuid;

    private String belongToMemberUUID;

    private String name;
    private String type;
    @Lob
    private byte[] data;
    @Order
    private LocalDate date;
    private LocalTime time;
    private long size;
    private int version = 0;

    public FilesEntity() {
    }

    public FilesEntity(String name, String type, byte[] data, int version) {
        this.name = name;
        this.type = type;
        this.data = data;
        this.version = version;
    }

    public String getBelongToMemberUUID() {
        return belongToMemberUUID;
    }

    public void setBelongToMemberUUID(String belongToMemberUUID) {
        this.belongToMemberUUID = belongToMemberUUID;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }
    public void incrementVersion() {
        this.version += 1;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }
}
