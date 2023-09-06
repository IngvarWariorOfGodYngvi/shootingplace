package com.shootingplace.shootingplace.wrappers;

import com.shootingplace.shootingplace.license.LicenseEntity;
import com.shootingplace.shootingplace.member.IMemberDTO;
import lombok.Builder;

@Builder
public class MemberWithLicenseWarpper {
    IMemberDTO imemberDTO;
    LicenseEntity licenseEntity;
}
