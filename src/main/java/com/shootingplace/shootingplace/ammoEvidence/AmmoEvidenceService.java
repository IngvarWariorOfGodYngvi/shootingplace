package com.shootingplace.shootingplace.ammoEvidence;

import com.shootingplace.shootingplace.Mapping;
import com.shootingplace.shootingplace.armory.ArmoryService;
import com.shootingplace.shootingplace.armory.GunRepository;
import com.shootingplace.shootingplace.enums.UsedType;
import com.shootingplace.shootingplace.history.ChangeHistoryService;
import com.shootingplace.shootingplace.history.UsedHistoryRepository;
import com.shootingplace.shootingplace.member.MemberEntity;
import com.shootingplace.shootingplace.member.MemberRepository;
import com.shootingplace.shootingplace.otherPerson.OtherPersonEntity;
import com.shootingplace.shootingplace.otherPerson.OtherPersonRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;

@Service
public class AmmoEvidenceService {


    private final AmmoEvidenceRepository ammoEvidenceRepository;
    private final ChangeHistoryService changeHistoryService;
    private final UsedHistoryRepository usedHistoryRepository;
    private final MemberRepository memberRepository;
    private final OtherPersonRepository otherPersonRepository;
    private final GunRepository gunRepository;
    private final ArmoryService armoryService;
    private final Logger LOG = LogManager.getLogger(getClass());


    public AmmoEvidenceService(AmmoEvidenceRepository ammoEvidenceRepository, ChangeHistoryService changeHistoryService, UsedHistoryRepository usedHistoryRepository, MemberRepository memberRepository, OtherPersonRepository otherPersonRepository, GunRepository gunRepository, ArmoryService armoryService) {
        this.ammoEvidenceRepository = ammoEvidenceRepository;
        this.changeHistoryService = changeHistoryService;
        this.usedHistoryRepository = usedHistoryRepository;
        this.memberRepository = memberRepository;
        this.otherPersonRepository = otherPersonRepository;
        this.gunRepository = gunRepository;
        this.armoryService = armoryService;
    }

    public ResponseEntity<?> getOpenEvidence() {
        return ammoEvidenceRepository.findAllByOpenTrue().size() > 0 ? ResponseEntity.ok(Mapping.map(ammoEvidenceRepository.findAllByOpenTrue().get(0))) : ResponseEntity.ok(new ArrayList<>());
    }

    public AmmoEvidenceEntity getEvidence(String uuid) {
        return ammoEvidenceRepository.getOne(uuid);
    }

    public void automationCloseEvidence() {
        List<AmmoEvidenceEntity> allByOpenTrue = ammoEvidenceRepository.findAllByOpenTrue();
        allByOpenTrue.forEach(e -> {
            closeEvidence(e.getUuid());
            LOG.info("Lista zamknięta automatycznie");
        });
    }

    public ResponseEntity<?> closeEvidence(String evidenceUUID) {
        if (!ammoEvidenceRepository.existsById(evidenceUUID)) {
            return ResponseEntity.badRequest().body("Nie znaleziono listy");
        }
        AmmoEvidenceEntity ammoEvidenceEntity = ammoEvidenceRepository
                .findById(evidenceUUID)
                .orElseThrow(EntityNotFoundException::new);
        ammoEvidenceEntity.setOpen(false);
        ammoEvidenceEntity.setForceOpen(false);
        ammoEvidenceRepository.save(ammoEvidenceEntity);
        LOG.info("zamknięto listę " + ammoEvidenceEntity.getNumber() + " z dnia " + ammoEvidenceEntity.getDate());
        return ResponseEntity.ok("Lista została zamknięta");
    }

    public List<AmmoDTO> getClosedEvidences(Pageable page) {
        page = PageRequest.of(page.getPageNumber(), page.getPageSize(), Sort.by("date").and(Sort.by("number")).descending());
        return ammoEvidenceRepository.findAllByOpenFalse(page).map(Mapping::map1).toList();
    }

    public ResponseEntity<?> openEvidence(String evidenceUUID, String pinCode) {
        if (ammoEvidenceRepository.findAll().stream().anyMatch(AmmoEvidenceEntity::isOpen)) {
            return ResponseEntity.badRequest().body("Nie można otworzyć listy bo inna jest otwarta");

        } else {
            AmmoEvidenceEntity ammoEvidenceEntity = ammoEvidenceRepository
                    .findById(evidenceUUID)
                    .orElseThrow(EntityNotFoundException::new);
            ResponseEntity<?> response = getStringResponseEntity(pinCode, ammoEvidenceEntity, HttpStatus.OK, "openEvidence", "Ręcznie otworzono listę - Pamiętaj by ją zamknąć!");
            if (response.getStatusCode().equals(HttpStatus.OK)) {
                ammoEvidenceEntity.setOpen(true);
                ammoEvidenceEntity.setForceOpen(true);
                ammoEvidenceRepository.save(ammoEvidenceEntity);
                LOG.info("otworzono");
            }
            return response;
        }
    }

    public String checkAnyOpenEvidence() {
        // send name of colours with -> \" \" <-
        // primary
        // secondary
        // accent
        // warning
        // negative
        // rest of colours
        String message = "\"primary\"";

        if (ammoEvidenceRepository.findAll().stream().anyMatch(f -> f.isOpen() && !f.isForceOpen())) {
            message = "\"negative\"";
        }
        if (ammoEvidenceRepository.findAll().stream().anyMatch(f -> f.isOpen() && f.isForceOpen())) {
            message = "\"red\"";
        }
        return message;
    }

    ResponseEntity<?> getStringResponseEntity(String pinCode, AmmoEvidenceEntity ammoEvidenceEntity, HttpStatus status, String methodName, Object body) {
        ResponseEntity<?> response = ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON).body(body);
        ResponseEntity<?> stringResponseEntity = changeHistoryService.addRecordToChangeHistory(pinCode, ammoEvidenceEntity.getClass().getSimpleName() + " " + methodName + " ", ammoEvidenceEntity.getUuid());
        if (stringResponseEntity != null) {
            response = stringResponseEntity;
        }
        return response;
    }

}
