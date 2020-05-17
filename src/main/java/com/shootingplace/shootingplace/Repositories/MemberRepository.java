package com.shootingplace.shootingplace.Repositories;

import com.shootingplace.shootingplace.domain.Entities.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MemberRepository extends JpaRepository<MemberEntity, UUID> {
}
