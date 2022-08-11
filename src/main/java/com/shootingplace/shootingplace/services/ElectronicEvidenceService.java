//package com.shootingplace.shootingplace.services;
//
//import com.shootingplace.shootingplace.domain.entities.ElectronicEvidenceEntity;
//import com.shootingplace.shootingplace.member.MemberEntity;
//import com.shootingplace.shootingplace.repositories.ElectronicEvidenceRepository;
//import com.shootingplace.shootingplace.member.MemberRepository;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//import org.springframework.stereotype.Service;
//
//import javax.persistence.EntityNotFoundException;
//import java.time.LocalDate;
//import java.util.*;
//
//@Service
//public class ElectronicEvidenceService {
//
//    private final ElectronicEvidenceRepository evidenceRepository;
//    private final MemberRepository memberRepository;
//    private final Logger LOG = LogManager.getLogger(getClass());
//
//
//    public ElectronicEvidenceService(ElectronicEvidenceRepository evidenceRepository, MemberRepository memberRepository) {
//        this.evidenceRepository = evidenceRepository;
//        this.memberRepository = memberRepository;
//    }
//
//    public List<MemberEntity> getMembersInEvidence() {
//        if (!evidenceRepository.findById(1).isPresent()) {
//            ElectronicEvidenceEntity evidenceEntity = ElectronicEvidenceEntity.builder()
//                    .id(1)
//                    .date(LocalDate.now())
//                    .members(null)
//                    .others("Brak")
//                    .build();
//            evidenceRepository.saveAndFlush(evidenceEntity);
//            LOG.info("utworzono encję ewidencji");
//
//        }
//        LOG.info("Wywołano Ewidencję");
//        ElectronicEvidenceEntity evidenceEntity = evidenceRepository.findById(1).orElseThrow(EntityNotFoundException::new);
//
//        List<MemberEntity> list = new ArrayList<>(evidenceEntity.getMembers());
//        list.sort(Comparator.comparing(MemberEntity::getSecondName));
//
//        return list;
//
//    }
//
//    public Boolean addMemberToEvidence(String memberUUID) {
//
//        MemberEntity memberEntity = memberRepository.findById(memberUUID).orElseThrow(EntityNotFoundException::new);
//
//        if (!evidenceRepository.findById(1).isPresent()) {
//            Set<MemberEntity> set = new HashSet<>();
//            ElectronicEvidenceEntity evidenceEntity = ElectronicEvidenceEntity.builder()
//                    .id(1)
//                    .date(LocalDate.now())
//                    .members(set)
//                    .others("Brak")
//                    .build();
//            evidenceRepository.saveAndFlush(evidenceEntity);
//            LOG.info("utworzono encję ewidencji");
//
//        }
//        ElectronicEvidenceEntity evidenceEntity = evidenceRepository.findById(1).orElseThrow(EntityNotFoundException::new);
//        Set<MemberEntity> set = evidenceEntity.getMembers();
//        set.add(memberEntity);
//        evidenceEntity.setMembers(set);
//        evidenceEntity.setOthers("Brak");
//        if (!evidenceEntity.getDate().equals(LocalDate.now())) {
//            evidenceEntity.setDate(LocalDate.now());
//        }
//        evidenceRepository.saveAndFlush(evidenceEntity);
//        LOG.info("Dodano do listy : " + memberEntity.getFirstName().concat(" " + memberEntity.getSecondName()));
//        return true;
//    }
//
//    public ElectronicEvidenceEntity getEvidence() {
//
//        if (!evidenceRepository.findById(1).isPresent()) {
//            ElectronicEvidenceEntity evidenceEntity = ElectronicEvidenceEntity.builder()
//                    .id(1)
//                    .date(LocalDate.now())
//                    .members(null)
//                    .others("Brak")
//                    .build();
//            evidenceRepository.saveAndFlush(evidenceEntity);
//            LOG.info("utworzono encję ewidencji");
//
//        }
//        return evidenceRepository.findById(1).orElseThrow(EntityNotFoundException::new);
//
//    }
//
//    public Boolean clearEvidence() {
//        ElectronicEvidenceEntity evidenceEntity = evidenceRepository.findById(1).orElseThrow(EntityNotFoundException::new);
//        evidenceEntity.setMembers(null);
//        evidenceEntity.setDate(LocalDate.now());
//        LOG.info("Wyczyszczono ewidencję");
//        evidenceRepository.saveAndFlush(evidenceEntity);
//        return true;
//    }
//
//    public Boolean setEvidenceDate(String date) {
//        ElectronicEvidenceEntity evidenceEntity = evidenceRepository.findById(1).orElseThrow(EntityNotFoundException::new);
//        evidenceEntity.setDate(LocalDate.parse(date));
//        evidenceRepository.saveAndFlush(evidenceEntity);
//        LOG.info("Zmieniono datę listy");
//        return true;
//    }
//}
