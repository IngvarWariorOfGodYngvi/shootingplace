package com.shootingplace.shootingplace.adapters;

import com.shootingplace.shootingplace.member.MemberPermissionsEntity;
import com.shootingplace.shootingplace.member.MemberPermissionsRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberPermissionSQLRepository extends MemberPermissionsRepository, JpaRepository<MemberPermissionsEntity,String> {
}
