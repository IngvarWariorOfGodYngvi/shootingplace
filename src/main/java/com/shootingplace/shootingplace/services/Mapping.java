package com.shootingplace.shootingplace.services;

import com.shootingplace.shootingplace.address.Address;
import com.shootingplace.shootingplace.address.AddressEntity;
import com.shootingplace.shootingplace.ammoEvidence.*;
import com.shootingplace.shootingplace.armory.Caliber;
import com.shootingplace.shootingplace.armory.CaliberEntity;
import com.shootingplace.shootingplace.armory.Gun;
import com.shootingplace.shootingplace.armory.GunEntity;
import com.shootingplace.shootingplace.contributions.Contribution;
import com.shootingplace.shootingplace.contributions.ContributionEntity;
import com.shootingplace.shootingplace.domain.entities.*;
import com.shootingplace.shootingplace.domain.models.*;
import com.shootingplace.shootingplace.file.FilesEntity;
import com.shootingplace.shootingplace.file.FilesModel;
import com.shootingplace.shootingplace.history.ChangeHistoryEntity;
import com.shootingplace.shootingplace.history.History;
import com.shootingplace.shootingplace.history.HistoryEntity;
import com.shootingplace.shootingplace.license.License;
import com.shootingplace.shootingplace.license.LicenseEntity;
import com.shootingplace.shootingplace.member.Member;
import com.shootingplace.shootingplace.member.MemberDTO;
import com.shootingplace.shootingplace.member.MemberEntity;
import com.shootingplace.shootingplace.shootingPatent.ShootingPatent;
import com.shootingplace.shootingplace.shootingPatent.ShootingPatentEntity;
import com.shootingplace.shootingplace.tournament.TournamentDTO;
import com.shootingplace.shootingplace.tournament.TournamentEntity;
import com.shootingplace.shootingplace.users.ChangeHistoryDTO;
import com.shootingplace.shootingplace.users.UserDTO;
import com.shootingplace.shootingplace.users.UserEntity;
import com.shootingplace.shootingplace.weaponPermission.WeaponPermission;
import com.shootingplace.shootingplace.weaponPermission.WeaponPermissionEntity;
import com.shootingplace.shootingplace.workingTimeEvidence.WorkingTimeEvidenceDTO;
import com.shootingplace.shootingplace.workingTimeEvidence.WorkingTimeEvidenceEntity;

import java.util.Optional;
import java.util.stream.Collectors;

public class Mapping {

    public static Member map(MemberEntity e) {
        return Member.builder()
                .joinDate(e.getJoinDate())
                .legitimationNumber(e.getLegitimationNumber())
                .firstName(e.getFirstName())
                .secondName(e.getSecondName())
                .shootingPatent(map(e.getShootingPatent()))
                .license(map(e.getLicense()))
                .email(e.getEmail())
                .pesel(e.getPesel())
                .IDCard(e.getIDCard())
                .club(e.getClub())
                .address(map(e.getAddress()))
                .phoneNumber(e.getPhoneNumber())
                .weaponPermission(map(e.getWeaponPermission()))
                .active(e.getActive())
                .adult(e.getAdult())
                .erased(e.getErased())
                .history(map(e.getHistory()))
                .memberPermissions(map(e.getMemberPermissions()))
                .personalEvidence(map(e.getPersonalEvidence()))
                .pzss(checkBool(e.getPzss()))
                .erasedEntity(e.getErasedEntity())
                .build();
    }

    private static Boolean checkBool(Boolean b) {
        if (b == null) {
            return false;
        } else return b;

    }


    public static MemberEntity map(Member e) {
        return MemberEntity.builder()
                .joinDate(e.getJoinDate())
                .legitimationNumber(e.getLegitimationNumber())
                .firstName(e.getFirstName())
                .secondName(e.getSecondName())
                .shootingPatent(map(e.getShootingPatent()))
                .license(map(e.getLicense()))
                .email(e.getEmail())
                .pesel(e.getPesel())
                .IDCard(e.getIDCard())
                .club(e.getClub())
                .address(map(e.getAddress()))
                .phoneNumber(e.getPhoneNumber())
                .weaponPermission(map(e.getWeaponPermission()))
                .active(e.getActive())
                .adult(e.getAdult())
                .erased(e.getErased())
                .memberPermissions(map(e.getMemberPermissions()))
                .personalEvidence(map(e.getPersonalEvidence()))
                .pzss(e.getPzss())
                .erasedEntity(e.getErasedEntity())
                .history(map(e.getHistory()))
                .build();
    }

    public static MemberDTO map2DTO(MemberEntity e) {
        if (e.getErasedEntity() != null) {
            return Optional.of(e).map(o ->
                    MemberDTO.builder()
                            .uuid(e.getUuid())
                            .firstName(e.getFirstName())
                            .secondName(e.getSecondName())
                            .image(e.getImageUUID())
                            .adult(e.getAdult())
                            .active(e.getActive())
                            .erased(e.getErased())
                            .erasedEntity(map(e.getErasedEntity()))
                            .pzss(e.getPzss())
                            .legitimationNumber(e.getLegitimationNumber())
                            .license(map(e.getLicense()))
                            .club(Mapping.map(e.getClub()))
                            .joinDate(e.getJoinDate())
                            .memberPermissions(map(e.getMemberPermissions()))
                            .build()).orElse(null);
        } else {
            return Optional.of(e).map(o ->
                    MemberDTO.builder()
                            .uuid(e.getUuid())
                            .firstName(e.getFirstName())
                            .secondName(e.getSecondName())
                            .image(e.getImageUUID())
                            .adult(e.getAdult())
                            .active(e.getActive())
                            .erased(e.getErased())
                            .pzss(e.getPzss())
                            .legitimationNumber(e.getLegitimationNumber())
                            .license(map(e.getLicense()))
                            .club(Mapping.map(e.getClub()))
                            .joinDate(e.getJoinDate())
                            .memberPermissions(map(e.getMemberPermissions()))
                            .build()).orElse(null);
        }

    }


    static MemberAmmo map3(MemberEntity e) {
        return MemberAmmo.builder()
                .uuid(e.getUuid())
                .firstName(e.getFirstName())
                .secondName(e.getSecondName())
                .legitimationNumber(e.getLegitimationNumber())
                .build();
    }

    static Erased map(ErasedEntity a) {
        return Optional.ofNullable(a).map(e -> Erased.builder()
                .additionalDescription(a.getAdditionalDescription())
                .erasedType(a.getErasedType())
                .date(a.getDate())
                .uuid(a.getUuid())
                .build()).orElse(null);
    }

    static Address map(AddressEntity a) {
        return Optional.ofNullable(a)
                .map(e -> Address.builder()
                        .zipCode(e.getZipCode())
                        .postOfficeCity(e.getPostOfficeCity())
                        .street(e.getStreet())
                        .streetNumber(e.getStreetNumber())
                        .flatNumber(e.getFlatNumber())
                        .build()).orElse(null);

    }

    static AddressEntity map(Address a) {
        return Optional.ofNullable(a)
                .map(e -> AddressEntity.builder()
                        .zipCode(e.getZipCode())
                        .postOfficeCity(e.getPostOfficeCity())
                        .street(e.getStreet())
                        .streetNumber(e.getStreetNumber())
                        .flatNumber(e.getFlatNumber())
                        .build()).orElse(null);
    }

    public static License map(LicenseEntity l) {
        return Optional.ofNullable(l)
                .map(e -> License.builder()
                        .number(e.getNumber())
                        .pistolPermission(e.isPistolPermission())
                        .riflePermission(e.isRiflePermission())
                        .shotgunPermission(e.isShotgunPermission())
                        .validThru(e.getValidThru())
                        .isValid(e.isValid())
                        .canProlong(e.isCanProlong())
                        .isPaid(e.isPaid())
                        .build()).orElse(null);
    }

    static LicenseEntity map(License l) {
        return Optional.ofNullable(l)
                .map(e -> LicenseEntity.builder()
                        .number(e.getNumber())
                        .pistolPermission(e.getPistolPermission())
                        .riflePermission(e.getRiflePermission())
                        .shotgunPermission(e.getShotgunPermission())
                        .validThru(e.getValidThru())
                        .valid(e.getValid())
                        .canProlong(e.getCanProlong())
                        .paid(e.getPaid())
                        .build()).orElse(null);
    }

    static ShootingPatentEntity map(ShootingPatent s) {
        return Optional.ofNullable(s)
                .map(e -> ShootingPatentEntity.builder()
                        .patentNumber(e.getPatentNumber())
                        .dateOfPosting(e.getDateOfPosting())
                        .pistolPermission(e.getPistolPermission())
                        .riflePermission(e.getRiflePermission())
                        .shotgunPermission(e.getShotgunPermission())
                        .build()).orElse(null);
    }

    static ShootingPatent map(ShootingPatentEntity s) {
        return Optional.ofNullable(s)
                .map(e -> ShootingPatent.builder()
                        .patentNumber(e.getPatentNumber())
                        .dateOfPosting(e.getDateOfPosting())
                        .pistolPermission(e.getPistolPermission())
                        .riflePermission(e.getRiflePermission())
                        .shotgunPermission(e.getShotgunPermission())
                        .build()).orElse(null);
    }

    static History map(HistoryEntity e) {
        return History.builder()
                .contributionList(e.getContributionList().stream().map(Mapping::map).collect(Collectors.toList()))
                .licenseHistory(e.getLicenseHistory())
                .competitionHistory(e.getCompetitionHistory().stream().map(Mapping::map).collect(Collectors.toList()))
                .patentDay(e.getPatentDay())
                .patentFirstRecord(e.getPatentFirstRecord())
                .licensePaymentHistory(e.getLicensePaymentHistory().stream().map(Mapping::map).collect(Collectors.toList()))
                .pistolCounter(e.getPistolCounter())
                .rifleCounter(e.getRifleCounter())
                .shotgunCounter(e.getShotgunCounter())
                .build();
    }

    private static CompetitionHistory map(CompetitionHistoryEntity e) {
        return CompetitionHistory.builder()
                .date(e.getDate())
                .discipline(e.getDiscipline())
                .name(e.getName())
                .build();
    }

    private static Contribution map(ContributionEntity e) {
        return Contribution.builder()
                .historyUUID(e.getHistoryUUID())
                .paymentDay(e.getPaymentDay())
                .validThru(e.getValidThru())
                .build();
    }

    static HistoryEntity map(History e) {

        return Optional.ofNullable(e).map(h -> HistoryEntity.builder()
                .licenseHistory(h.getLicenseHistory())
                .patentDay(h.getPatentDay())
                .patentFirstRecord(h.getPatentFirstRecord())
                .licensePaymentHistory(null)
                .pistolCounter(h.getPistolCounter())
                .rifleCounter(h.getRifleCounter())
                .shotgunCounter(h.getShotgunCounter())
                .build()).orElse(null);

    }

    static LicensePaymentHistory map(LicensePaymentHistoryEntity l) {
        return LicensePaymentHistory.builder()
                .date(l.getDate())
                .validForYear(l.getValidForYear())
                .memberUUID(l.getMemberUUID())
                .build();
    }

    static WeaponPermission map(WeaponPermissionEntity w) {
        return Optional.ofNullable(w).map(e -> WeaponPermission.builder()
                .number(e.getNumber())
                .isExist(e.getExist())
                .admissionToPossessAWeapon(e.getAdmissionToPossessAWeapon())
                .admissionToPossessAWeaponIsExist(e.getAdmissionToPossessAWeaponIsExist())
                .build()).orElse(null);
    }

    static WeaponPermissionEntity map(WeaponPermission w) {
        return Optional.ofNullable(w).map(e -> WeaponPermissionEntity.builder()
                .number(e.getNumber())
                .isExist(e.getExist())
                .admissionToPossessAWeapon(e.getAdmissionToPossessAWeapon())
                .admissionToPossessAWeaponIsExist(e.getAdmissionToPossessAWeaponIsExist())
                .build()).orElse(null);
    }

    static MemberPermissions map(MemberPermissionsEntity m) {
        return Optional.ofNullable(m).map(e -> MemberPermissions.builder()
                .instructorNumber(e.getInstructorNumber())
                .arbiterNumber(e.getArbiterNumber())
                .arbiterClass(e.getArbiterClass())
                .arbiterPermissionValidThru(e.getArbiterPermissionValidThru())
                .shootingLeaderNumber(e.getShootingLeaderNumber())
                .build()).orElse(null);
    }

    static MemberPermissionsEntity map(MemberPermissions m) {
        return Optional.ofNullable(m).map(e -> MemberPermissionsEntity.builder()
                .instructorNumber(e.getInstructorNumber())
                .arbiterNumber(e.getArbiterNumber())
                .arbiterClass(e.getArbiterClass())
                .arbiterPermissionValidThru(e.getArbiterPermissionValidThru())
                .shootingLeaderNumber(e.getShootingLeaderNumber())
                .build()).orElse(null);
    }

    public static CompetitionMembersList map(CompetitionMembersListEntity c) {
        return CompetitionMembersList.builder()
                .uuid(c.getUuid())
                .name(c.getName())
                .date(c.getDate())
                .WZSS(c.isWZSS())
                .attachedToTournament(c.getAttachedToTournament())
                .scoreList(c.getScoreList().stream().map(Mapping::map).collect(Collectors.toList()))
                .discipline(c.getDiscipline())
                .disciplines(c.getDisciplines())
                .countingMethod(c.getCountingMethod())
                .type(c.getType())
                .numberOfShots(c.getNumberOfShots())
                .ordering(c.getOrdering())
                .caliberUUID(c.getCaliberUUID())
                .practiceShots(c.getPracticeShots())
                .build();
    }

    public static Score map(ScoreEntity s) {
        if (s.getMember() != null) {
            return Score.builder()
                    .name(s.getName())
                    .member(map2DTO(s.getMember()))
                    .ammunition(s.isAmmunition())
                    .gun(s.isGun())
                    .metricNumber(s.getMetricNumber())
                    .innerTen(s.getInnerTen())
                    .outerTen(s.getOuterTen())
                    .alfa(s.getAlfa())
                    .charlie(s.getCharlie())
                    .delta(s.getDelta())
                    .procedures(s.getProcedures())
                    .hf(s.getHf())
                    .dnf(s.isDnf())
                    .dsq(s.isDsq())
                    .pk(s.isPk())
                    .otherPersonEntity(s.getOtherPersonEntity())
                    .score(s.getScore())
                    .competitionMembersListEntityUUID(s.getCompetitionMembersListEntityUUID())
                    .uuid(s.getUuid())
                    .build();
        } else {
            return Score.builder()
                    .name(s.getName())
                    .member(null)
                    .ammunition(s.isAmmunition())
                    .gun(s.isGun())
                    .metricNumber(s.getMetricNumber())
                    .innerTen(s.getInnerTen())
                    .outerTen(s.getOuterTen())
                    .alfa(s.getAlfa())
                    .charlie(s.getCharlie())
                    .delta(s.getDelta())
                    .dnf(s.isDnf())
                    .dsq(s.isDsq())
                    .pk(s.isPk())
                    .hf(s.getHf())
                    .procedures(s.getProcedures())
                    .otherPersonEntity(s.getOtherPersonEntity())
                    .score(s.getScore())
                    .competitionMembersListEntityUUID(s.getCompetitionMembersListEntityUUID())
                    .uuid(s.getUuid())
                    .build();
        }
    }


    public static FilesEntity map(FilesModel f) {
        return Optional.ofNullable(f).map(e -> FilesEntity.builder()
                .uuid(e.getUuid())
                .name(e.getName())
                .data(e.getData())
                .type(e.getType())
                .date(e.getDate())
                .time(e.getTime())
                .size(e.getSize())
                .version(e.getVersion())
                .build()).orElse(null);
    }

    public static FilesModel map(FilesEntity f) {
        return Optional.ofNullable(f).map(e -> FilesModel.builder()
                .uuid(e.getUuid())
                .name(e.getName())
                .data(e.getData())
                .type(e.getType())
                .date(e.getDate())
                .time(e.getTime())
                .size(e.getSize())
                .version(e.getVersion())
                .build()).orElse(null);
    }
//
//    public static AmmoEvidenceEntity map(AmmoEvidence a) {
//        return Optional.ofNullable(a).map(e -> AmmoEvidenceEntity.builder()
//                .number(e.getNumber())
//                .date(e.getDate())
//                .build()).orElse(null);
//    }
//
//    public static AmmoEvidence map(AmmoEvidenceEntity a) {
//        return Optional.ofNullable(a).map(e -> AmmoEvidence.builder()
//                .number(e.getNumber())
//                .date(e.getDate())
//                .build()).orElse(null);
//    }

    public static Caliber map(CaliberEntity c) {
        return Optional.ofNullable(c).map(e -> Caliber.builder()
                .name(e.getName())
                .quantity(e.getQuantity())
                .build()).orElse(null);
    }

    public static CaliberEntity map(Caliber c) {
        return Optional.ofNullable(c).map(e -> CaliberEntity.builder()
                .name(e.getName())
                .quantity(e.getQuantity())
                .build()).orElse(null);
    }

    public static PersonalEvidenceEntity map(PersonalEvidence p) {
        return Optional.ofNullable(p).map(e -> PersonalEvidenceEntity.builder()
                .build()).orElse(null);
    }

    public static PersonalEvidence map(PersonalEvidenceEntity p) {
        return Optional.ofNullable(p).map(e -> PersonalEvidence.builder()
                .build()).orElse(null);
    }


    public static CompetitionMembersListEntity map(CompetitionMembersList c) {
        return Optional.ofNullable(c).map(e -> CompetitionMembersListEntity.builder()
                .name(e.getName())
                .build()).orElse(null);
    }

    public static AmmoUsedToEvidenceEntity map(AmmoUsedEvidence a) {
        return Optional.ofNullable(a).map(e -> AmmoUsedToEvidenceEntity.builder()
                .caliberName(a.getCaliberName())
                .caliberUUID(a.getCaliberUUID())
                .counter(a.getCounter())
                .memberEntity(a.getMemberEntity())
                .otherPersonEntity(a.getOtherPersonEntity())
                .name(a.getUserName())
                .date(a.getDate())
                .build()).orElse(null);
    }

    public static AmmoUsedEntity map(AmmoUsedPersonal a) {
        return Optional.ofNullable(a).map(e -> AmmoUsedEntity.builder()
                .caliberName(a.getCaliberName())
                .counter(a.getCounter())
                .memberUUID(a.getMemberUUID())
                .caliberUUID(a.getCaliberUUID())
                .date(a.getDate())
                .userName(a.getMemberName())
                .otherPersonEntityID(null)
                .build()).orElse(null);

    }

    public static AmmoDTO map1(AmmoEvidenceEntity a) {
        return AmmoDTO.builder()
                .evidenceUUID(a.getUuid())
                .date(a.getDate())
                .number(a.getNumber())
                .build();

    }

    public static TournamentDTO map1(TournamentEntity a) {
        return TournamentDTO.builder()
                .tournamentUUID(a.getUuid())
                .date(a.getDate())
                .name(a.getName())
                .build();

    }

    static Club map(ClubEntity c) {
        return Club.builder()
                .name(c.getName())
                .build();
    }


    public static Gun map(GunEntity c) {
        return Gun.builder()
                .additionalEquipment(c.getAdditionalEquipment())
                .basisForPurchaseOrAssignment(c.getBasisForPurchaseOrAssignment())
                .caliber(c.getCaliber())
                .comment(c.getComment())
                .gunCertificateSerialNumber(c.getGunCertificateSerialNumber())
                .gunType(c.getGunType())
                .modelName(c.getModelName())
                .numberOfMagazines(c.getNumberOfMagazines())
                .productionYear(c.getProductionYear())
                .recordInEvidenceBook(c.getRecordInEvidenceBook())
                .serialNumber(c.getSerialNumber())
                .uuid(c.getUuid())
                .build();
    }

    public static WorkingTimeEvidenceDTO map(WorkingTimeEvidenceEntity e) {
        return WorkingTimeEvidenceDTO.builder()
                .cardNumber(e.getCardNumber())
                .workTime(e.getWorkTime())
                .workType(e.getWorkType())
                .isAutomatedClosed(e.isAutomatedClosed())
                .start(e.getStart())
                .stop(e.getStop())
                .isAccepted(e.isAccepted())
                .toClarify(e.isToClarify())
                .user(e.getUser().getUuid())
                .uuid(e.getUuid()).build();
    }

    public static UserDTO map(UserEntity u) {
        return UserDTO.builder().firstName(u.getFirstName())
                .secondName(u.getSecondName())
                .uuid(u.getUuid())
                .subType(u.getSubType())
                .build();

    }

    public static ChangeHistoryDTO map(ChangeHistoryEntity c) {
        return ChangeHistoryDTO.builder()
                .belongsTo(c.getBelongsTo())
                .timeNow(c.getTimeNow())
                .dayNow(c.getDayNow())
                .classNamePlusMethod(c.getClassNamePlusMethod())
                .build();
    }

    public static AmmoEvidenceDTO map(AmmoEvidenceEntity a) {
        return AmmoEvidenceDTO.builder()
                .uuid(a.getUuid())
                .number(a.getNumber())
                .date(a.getDate())
                .open(a.isOpen())
                .forceOpen(a.isForceOpen())
                .ammoInEvidenceDTOList(a.getAmmoInEvidenceEntityList().stream().map(Mapping::map).collect(Collectors.toList()))
                .build();
    }

    public static AmmoInEvidenceDTO map(AmmoInEvidenceEntity a) {
        return AmmoInEvidenceDTO.builder()
                .caliberName(a.getCaliberName())
                .ammoUsedToEvidenceDTOList(a.getAmmoUsedToEvidenceEntityList().stream().map(Mapping::map).collect(Collectors.toList()))
                .quantity(a.getQuantity())
                .build();
    }

    private static AmmoUsedToEvidenceDTO map(AmmoUsedToEvidenceEntity a) {
        return AmmoUsedToEvidenceDTO.builder()
                .caliberName(a.getCaliberName())
                .counter(a.getCounter())
                .date(a.getDate())
                .name(a.getName())
                .build();
    }
}
