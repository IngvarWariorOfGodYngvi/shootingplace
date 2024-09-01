package com.shootingplace.shootingplace.armory;

import com.shootingplace.shootingplace.Mapping;
import com.shootingplace.shootingplace.exceptions.NoUserPermissionException;
import com.shootingplace.shootingplace.history.ChangeHistoryService;
import com.shootingplace.shootingplace.workingTimeEvidence.WorkingTimeEvidenceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.naming.NoPermissionException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ShootingPacketService {

    private final ShootingPacketRepository shootingPacketRepository;
    private final CaliberForShootingPacketRepository caliberForShootingPacketRepository;
    private final WorkingTimeEvidenceRepository workingTimeEvidenceRepository;
    private final ChangeHistoryService changeHistoryService;
    private final CaliberRepository caliberRepository;

    public ShootingPacketService(ShootingPacketRepository shootingPacketRepository, CaliberForShootingPacketRepository caliberForShootingPacketRepository, WorkingTimeEvidenceRepository workingTimeEvidenceRepository, ChangeHistoryService changeHistoryService, CaliberRepository caliberRepository) {
        this.shootingPacketRepository = shootingPacketRepository;
        this.caliberForShootingPacketRepository = caliberForShootingPacketRepository;
        this.workingTimeEvidenceRepository = workingTimeEvidenceRepository;
        this.changeHistoryService = changeHistoryService;
        this.caliberRepository = caliberRepository;
    }

    public List<ShootingPacketDTO> getAllShootingPacket() {
        return shootingPacketRepository.findAll().stream().map(Mapping::map).sorted(Comparator.comparing(ShootingPacketDTO::getPrice)).collect(Collectors.toList());
    }

    public ResponseEntity<?> addShootingPacket(String name, float price, Map<String, Integer> map,String pinCode) throws NoPermissionException, NoUserPermissionException {
        if (!workingTimeEvidenceRepository.existsByIsCloseFalse()) {
            return ResponseEntity.badRequest().body("Najpierw zarejestruj pobyt");
        }
        List<CaliberForShootingPacketEntity> entities = new ArrayList<>();
        map.forEach((k, v) -> entities.add(caliberForShootingPacketRepository.save(CaliberForShootingPacketEntity.builder().caliberUUID(k).caliberName(caliberRepository.getOne(k).getName()).quantity(v).build())));
        return getStringResponseEntity(pinCode,shootingPacketRepository.save(ShootingPacketEntity.builder().name(name.toUpperCase()).price(price).calibers(entities).build()),HttpStatus.OK,"addShootingPacket","utworzono pakiet strzelecki " + name.toUpperCase());
    }

    public ResponseEntity<?> getStringResponseEntity(String pinCode, ShootingPacketEntity shootingPacketEntity, HttpStatus status, String methodName, Object body) throws NoPermissionException, NoUserPermissionException {
        ResponseEntity<?> response = ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON).body(body);
        ResponseEntity<String> stringResponseEntity;
        stringResponseEntity = changeHistoryService.addRecordToChangeHistory(pinCode, methodName, shootingPacketEntity.getUuid() + " " + shootingPacketEntity.getName());

        if (stringResponseEntity != null) {
            response = stringResponseEntity;
        }
        return response;
    }
}
