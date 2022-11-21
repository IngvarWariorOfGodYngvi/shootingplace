package com.shootingplace.shootingplace.member;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class MemberInfo {

    String secondName;
    String firstName;
    Integer legitimationNumber;
    boolean isActive;
    boolean isAdult;
}
