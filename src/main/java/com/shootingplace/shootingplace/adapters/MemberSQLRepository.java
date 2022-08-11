package com.shootingplace.shootingplace.adapters;

import com.shootingplace.shootingplace.member.MemberEntity;
import com.shootingplace.shootingplace.member.MemberRepository;
import org.springframework.data.jpa.repository.JpaRepository;

interface MemberSQLRepository extends MemberRepository, JpaRepository<MemberEntity,String> {
}
