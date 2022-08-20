package com.shootingplace.shootingplace.workingTimeEvidence;

import lombok.Data;

import java.util.List;

@Data
public class UserWithWorkingTimeList {

    private String uuid;

    private String firstName;

    private String secondName;

    private String subType;

    private List<WorkingTimeEvidenceDTO> WTEdtoList;


}
