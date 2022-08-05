//package com.shootingplace.shootingplace.reports;
//
//import java.time.Clock;
//import java.time.Instant;
//
//public abstract class ChangeEvent {
//
//    private String uuid;
//
//    private String memberId;
//    private Instant occurrence;
//    private String className;
//    private String userName;
//
//
//    public ChangeEvent(String uuid, Clock clock) {
//        this.uuid = uuid;
//        this.occurrence = Instant.now(clock);
//    }
//
//    public String getUuid() {
//        return uuid;
//    }
//
//    public Instant getOccurrence() {
//        return occurrence;
//    }
//
//    public String getMemberId() {
//        return memberId;
//    }
//
//    @Override
//    public String toString() {
//        return getClass().getSimpleName() + "{" +
//                "memberId='" + memberId + '\'' +
//                ", occurrence=" + occurrence +
//                ", className='" + className + '\'' +
//                ", userName='" + userName + '\'' +
//                '}';
//    }
//}
