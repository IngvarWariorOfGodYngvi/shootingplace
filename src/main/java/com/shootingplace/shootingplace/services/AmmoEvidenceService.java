package com.shootingplace.shootingplace.services;

import com.shootingplace.shootingplace.domain.entities.AmmoEvidenceEntity;
import com.shootingplace.shootingplace.domain.models.AmmoDTO;
import com.shootingplace.shootingplace.repositories.AmmoEvidenceRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AmmoEvidenceService {


    private final AmmoEvidenceRepository ammoEvidenceRepository;
    private final ChangeHistoryService changeHistoryService;
    private final Logger LOG = LogManager.getLogger(getClass());


    public AmmoEvidenceService(AmmoEvidenceRepository ammoEvidenceRepository, ChangeHistoryService changeHistoryService) {
        this.ammoEvidenceRepository = ammoEvidenceRepository;
        this.changeHistoryService = changeHistoryService;
    }

    public List<AmmoEvidenceEntity> getAllEvidences(boolean state) {
        return ammoEvidenceRepository.findAll()
                .stream()
                .filter(f -> f.isOpen() == state)
                .sorted(Comparator.comparing(AmmoEvidenceEntity::getDate)
                        .reversed())
                .collect(Collectors.toList());
    }

    public AmmoEvidenceEntity getEvidence(String uuid) {
        return ammoEvidenceRepository.getOne(uuid);
    }

    public boolean closeEvidence(String evidenceUUID) {
        if (!ammoEvidenceRepository.existsById(evidenceUUID)) {
            return false;
        }
        AmmoEvidenceEntity ammoEvidenceEntity = ammoEvidenceRepository
                .findById(evidenceUUID)
                .orElseThrow(EntityNotFoundException::new);
        ammoEvidenceEntity.setOpen(false);
        ammoEvidenceEntity.setForceOpen(false);
        ammoEvidenceRepository.saveAndFlush(ammoEvidenceEntity);
        LOG.info("zamknięto");
        return true;
    }

    public List<AmmoDTO> getClosedEvidences() {
        List<AmmoEvidenceEntity> all = ammoEvidenceRepository.findAll().stream().filter(f -> !f.isOpen()).collect(Collectors.toList());
        List<AmmoDTO> allDTO = new ArrayList<>();
        all.forEach(e -> allDTO.add(Mapping.map1(e)));
        allDTO.sort(Comparator.comparing(AmmoDTO::getDate).thenComparing(AmmoDTO::getNumber).reversed());
        return allDTO;
    }

    public boolean openEvidence(String evidenceUUID, String pinCode) {
        if (ammoEvidenceRepository.findAll().stream().anyMatch(AmmoEvidenceEntity::isOpen)) {
            return false;

        } else {
            AmmoEvidenceEntity ammoEvidenceEntity = ammoEvidenceRepository
                    .findById(evidenceUUID)
                    .orElseThrow(EntityNotFoundException::new);
            ammoEvidenceEntity.setOpen(true);
            ammoEvidenceEntity.setForceOpen(true);
            ammoEvidenceRepository.saveAndFlush(ammoEvidenceEntity);
            LOG.info("otworzono");
            changeHistoryService.addRecordToChangeHistory(pinCode, ammoEvidenceEntity.getClass().getSimpleName() + " openAmmoEvidenceList", evidenceUUID);
            return true;
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
}
