package com.shootingplace.shootingplace.repositories;

import com.shootingplace.shootingplace.domain.entities.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<MemberEntity, String> {
    Optional<MemberEntity> findByPesel(String pesel);

    Optional<MemberEntity> findByEmail(String email);

    Optional<MemberEntity> findByLegitimationNumber(Integer legitimationNumber);
    Optional<MemberEntity> findByClubCardBarCode(String barCode);

    Optional<MemberEntity> findByPhoneNumber(String phoneNumber);

    Optional<MemberEntity> findByIDCard(String IDCard);

    List<MemberEntity> findAllByErasedIsTrue();

}
