package com.shootingplace.shootingplace.member;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
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
    List<MemberEntity> findAllByErasedFalse();

    @Query(nativeQuery = true, value = "SELECT * FROM shootingplace.member_entity join shootingplace.license_entity on shootingplace.member_entity.license_uuid=shootingplace.license_entity.uuid where club_id = '1' and !member_entity.erased and member_entity.pzss and license_entity.number > 0 and license_entity.valid;")
    List<MemberEntity> findAllWhereCLubEquals1ErasedFalsePzssTrueLicenseValidTrue();

    @Query(nativeQuery = true, value = "SELECT * FROM shootingplace.member_entity join shootingplace.license_entity on shootingplace.member_entity.license_uuid=shootingplace.license_entity.uuid where club_id = '1' and !member_entity.erased and member_entity.pzss and license_entity.number > 0 and !license_entity.valid;")
    List<MemberEntity> findAllWhereCLubEquals1ErasedFalsePzssTrueLicenseValidFalse();

    List<MemberEntity> findAllByErasedFalseAndActiveFalse();

    Page<MemberEntity> findAllByErasedFalse(Pageable pageable);

    List<MemberEntity> findAllByErasedTrue();

    @Query(nativeQuery = true, value = "Select count(*) from shootingplace.member_entity WHERE (join_date BETWEEN (:start) AND (:stop)) ")
    int countActualYearMemberCounts(@Param("start") LocalDate start, @Param("stop") LocalDate stop);

    @Query(nativeQuery = true, value = "SELECT uuid, first_name, second_name, imageuuid, active, adult, erased, pzss, legitimation_number, join_date from shootingplace.member_entity WHERE (join_date BETWEEN (:firstDate) AND (:secondDate)) order by join_date")
    List<IMemberDTO> getMemberBetweenJoinDate(@Param("firstDate") LocalDate firstDate, @Param("secondDate") LocalDate secondDate);

    void delete(MemberEntity one);

    @Query(nativeQuery = true, value = "SELECT uuid, first_name, second_name, imageuuid, active, adult, erased, pzss, legitimation_number, join_date from shootingplace.member_entity WHERE history_uuid = (:historyUUID)")
    IMemberDTO getByHistoryUUID(@Param("historyUUID") String historyUUID);

    MemberEntity findByHistoryUuid(String historyUUID);

    @Query(nativeQuery = true, value = "SELECT max(legitimation_number) from shootingplace.member_entity")
    int getMaxLegitimationNumber();
    @Query(nativeQuery = true, value = "SELECT * from shootingplace.member_entity where adult = false and erased = false")
    List<MemberEntity> findAllByAdultFalseAndErasedFalse();
    @Query(nativeQuery = true, value = "SELECT * from shootingplace.member_entity where club_id = '1' and !member_entity.erased and member_entity.pzss")
    List<MemberEntity> findAllWhereClubEquals1ErasedFalsePzssTrue();

    MemberEntity findByLicenseUuid(String licenseUUID);
}
