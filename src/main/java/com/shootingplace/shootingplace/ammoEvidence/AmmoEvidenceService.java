package com.shootingplace.shootingplace.ammoEvidence;

import com.shootingplace.shootingplace.Mapping;
import com.shootingplace.shootingplace.armory.ArmoryService;
import com.shootingplace.shootingplace.armory.GunRepository;
import com.shootingplace.shootingplace.history.ChangeHistoryService;
import com.shootingplace.shootingplace.history.UsedHistoryRepository;
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
import java.util.List;

@Service
public class AmmoEvidenceService {


    private final AmmoEvidenceRepository ammoEvidenceRepository;
    private final ChangeHistoryService changeHistoryService;
    private final ArmoryService armoryService;
    private final UsedHistoryRepository usedHistoryRepository;
    private final GunRepository gunRepository;
    private final Logger LOG = LogManager.getLogger(getClass());


    public AmmoEvidenceService(AmmoEvidenceRepository ammoEvidenceRepository, ChangeHistoryService changeHistoryService, ArmoryService armoryService, UsedHistoryRepository usedHistoryRepository, GunRepository gunRepository) {
        this.ammoEvidenceRepository = ammoEvidenceRepository;
        this.changeHistoryService = changeHistoryService;
        this.armoryService = armoryService;
        this.usedHistoryRepository = usedHistoryRepository;
        this.gunRepository = gunRepository;
    }

    public ResponseEntity<?> getOpenEvidence() {
        return ammoEvidenceRepository.findAllByOpenTrue().size()!=0 ? ResponseEntity.ok(Mapping.map(ammoEvidenceRepository.findAllByOpenTrue().get(0))) : ResponseEntity.ok().body(null);
    }

    public AmmoEvidenceEntity getEvidence(String uuid) {
        return ammoEvidenceRepository.getOne(uuid);
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
        LOG.info("zamknięto");
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

    public ResponseEntity<?> addGunToList(String evidenceUUID, String barcode) {

        String s = armoryService.addUseToGun(barcode, evidenceUUID);
        return ResponseEntity.ok("\"" + s + "\"");
    }
//
//    public List<GunEntity> getGunInAmmoEvidenceList(String evidenceUUID) {
//        List<UsedHistoryEntity> collect = usedHistoryRepository.findAll().stream().filter(f -> f.getEvidenceUUID() != null).filter(f -> f.getEvidenceUUID().equals(evidenceUUID)).collect(Collectors.toList());
//        List<GunEntity> gunEntityList = new ArrayList<>();
//        collect.forEach(e -> gunEntityList.add(gunRepository.findById(e.getGunUUID()).orElseThrow(EntityNotFoundException::new)));
//        return gunEntityList;
//    }

    ResponseEntity<?> getStringResponseEntity(String pinCode, AmmoEvidenceEntity ammoEvidenceEntity, HttpStatus status, String methodName, Object body) {
        ResponseEntity<?> response = ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON).body(body);
        ResponseEntity<?> stringResponseEntity = changeHistoryService.addRecordToChangeHistory(pinCode, ammoEvidenceEntity.getClass().getSimpleName() + " " + methodName + " ", ammoEvidenceEntity.getUuid());
        if (stringResponseEntity != null) {
            response = stringResponseEntity;
        }
        return response;
    }

}
