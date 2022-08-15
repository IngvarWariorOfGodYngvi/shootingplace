package com.shootingplace.shootingplace.users;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {

    private String uuid;

    private String firstName;

    private String secondName;

    private String subType;
}
