package com.shootingplace.shootingplace.armory.gunRepresentation;

import com.shootingplace.shootingplace.armory.GunEntity;
import com.shootingplace.shootingplace.armory.GunRepository;
import com.shootingplace.shootingplace.armory.GunUsedEntity;
import com.shootingplace.shootingplace.armory.GunUsedRepository;
import com.shootingplace.shootingplace.utils.Mapping;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

@Service
public class GunRepresentationService {

    private final GunRepresentationRepository gunRepresentationRepository;
    private final GunRepository gunRepository;
    private final GunUsedRepository gunUsedRepository;
    private final Logger LOG = LogManager.getLogger(getClass());

    public GunRepresentationService(GunRepresentationRepository gunRepresentationRepository, GunRepository gunRepository, GunUsedRepository gunUsedRepository) {
        this.gunRepresentationRepository = gunRepresentationRepository;
        this.gunRepository = gunRepository;
        this.gunUsedRepository = gunUsedRepository;
    }

    public void function(GunUsedEntity gunUsedEntity) {
        if (gunUsedEntity.getGunRepresentationEntity() == null) {
            GunEntity one = gunRepository.getOne(gunUsedEntity.getGunUUID());

            GunRepresentationEntity representation = Mapping.mapToRepresentation(one);

            GunRepresentationEntity gunRepresentation = gunRepresentationRepository.save(representation);

            gunUsedEntity.setGunRepresentationEntity(gunRepresentation);

            gunUsedRepository.save(gunUsedEntity);
            LOG.info("Zmieniam");
        }
    }
}
