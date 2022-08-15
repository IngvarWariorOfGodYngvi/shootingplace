package com.shootingplace.shootingplace.member;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface MemberRepository {
    Optional<MemberEntity> findByPesel(String pesel);

    Optional<MemberEntity> findByEmail(String email);

    Optional<MemberEntity> findByLegitimationNumber(Integer legitimationNumber);
    Optional<MemberEntity> findByClubCardBarCode(String barCode);

    Optional<MemberEntity> findByPhoneNumber(String phoneNumber);

    Optional<MemberEntity> findByIDCard(String IDCard);

    boolean existsByLegitimationNumber(Integer legitimationNumber);

    List<MemberEntity> findAll();

    MemberEntity save(MemberEntity entity);

    boolean existsById(String uuid);

    Optional<MemberEntity> findById(String uuid);

    MemberEntity getOne(String uuid);

    Page<MemberEntity> findAll(Pageable page);
}
