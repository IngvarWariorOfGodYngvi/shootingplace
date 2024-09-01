package com.shootingplace.shootingplace.competition;

import com.shootingplace.shootingplace.enums.CompetitionType;
import com.shootingplace.shootingplace.enums.CountingMethod;
import com.shootingplace.shootingplace.enums.Discipline;
import com.shootingplace.shootingplace.exceptions.NoUserPermissionException;
import com.shootingplace.shootingplace.history.ChangeHistoryService;
import com.shootingplace.shootingplace.tournament.CompetitionMembersListRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CompetitionService {

    private final CompetitionRepository competitionRepository;
    private final CompetitionMembersListRepository competitionMembersListRepository;
    private final ChangeHistoryService changeHistoryService;
    private final Logger LOG = LogManager.getLogger(getClass());


    public CompetitionService(CompetitionRepository competitionRepository, CompetitionMembersListRepository competitionMembersListRepository, ChangeHistoryService changeHistoryService) {
        this.competitionRepository = competitionRepository;
        this.competitionMembersListRepository = competitionMembersListRepository;
        this.changeHistoryService = changeHistoryService;
    }

    public List<CompetitionEntity> getAllCompetitions() {
        List<CompetitionEntity> competitionEntityList = competitionRepository.findAll().stream().sorted(Comparator.comparing(CompetitionEntity::getOrdering)).collect(Collectors.toList());

        if (competitionEntityList.isEmpty()) {
            createCompetitions();
            LOG.info("Zostały utworzone domyślne encje Konkurencji");
        }
//        for (CompetitionEntity competitionEntity : competitionEntityList) {
//            if (competitionEntity.getNumberOfShots() == null) {
//                String[] split = competitionEntity.getName().split(" ");
//                for (String value : split) {
//                    String s = value.replaceAll("[a-zA-ZżźćńółęąśŻŹĆĄŚĘŁÓŃ]", "");
//                    if (!s.equals("")) {
//                        competitionEntity.setNumberOfShots(Integer.parseInt(s));
//                        competitionRepository.save(competitionEntity);
//                    }
//                }
//            }
//        }
//        for (CompetitionEntity competitionEntity : competitionEntityList) {
//            if (competitionEntity.getType() == null) {
//                String[] split = competitionEntity.getName().split(" ");
//                for (String s : split) {
//                    if (s.equals(CompetitionType.OPEN.getName())) {
//                        competitionEntity.setType(CompetitionType.OPEN.getName());
//                    } else {
//                        competitionEntity.setType(CompetitionType.YOUTH.getName());
//                    }
//                    competitionRepository.save(competitionEntity);
//
//                }
//            }
//        }

//        competitionEntityList.sort(Comparator.comparing(CompetitionEntity::getOrdering));
        return competitionEntityList;
    }

    private void createCompetitions() {

        competitionRepository.save(CompetitionEntity.builder()
                .uuid(UUID.randomUUID().toString())
                .name("25m Pistolet sportowy 10 strzałów OPEN")
                .numberOfShots(10)
                .type(CompetitionType.OPEN.getName())
                .discipline(Discipline.PISTOL.getName())
                .countingMethod(CountingMethod.NORMAL.getName())
                .ordering(3)
                .build());
        competitionRepository.save(CompetitionEntity.builder()
                .uuid(UUID.randomUUID().toString())
                .name("25m Pistolet centralnego zapłonu 10 strzałów OPEN")
                .numberOfShots(10)
                .type(CompetitionType.OPEN.getName())
                .discipline(Discipline.PISTOL.getName())
                .countingMethod(CountingMethod.NORMAL.getName())
                .ordering(4)
                .build());
        competitionRepository.save(CompetitionEntity.builder()
                .uuid(UUID.randomUUID().toString())
                .name("10m Pistolet pneumatyczny 10 strzałów OPEN")
                .numberOfShots(10)
                .type(CompetitionType.OPEN.getName())
                .discipline(Discipline.PISTOL.getName())
                .countingMethod(CountingMethod.NORMAL.getName())
                .ordering(2)
                .build());
        competitionRepository.save(CompetitionEntity.builder()
                .uuid(UUID.randomUUID().toString())
                .name("50m Pistolet dowolny 10 strzałów OPEN")
                .numberOfShots(10)
                .type(CompetitionType.OPEN.getName())
                .discipline(Discipline.PISTOL.getName())
                .countingMethod(CountingMethod.NORMAL.getName())
                .ordering(5)
                .build());
        competitionRepository.save(CompetitionEntity.builder()
                .uuid(UUID.randomUUID().toString())
                .name("10m Karabin pneumatyczny 10 strzałów OPEN")
                .numberOfShots(10)
                .type(CompetitionType.OPEN.getName())
                .discipline(Discipline.RIFLE.getName())
                .countingMethod(CountingMethod.NORMAL.getName())
                .ordering(1)
                .build());
        LOG.info("Stworzono encje konkurencji");
    }

    public ResponseEntity<?> createNewCompetition(Competition competition) {
        List<String> list = competitionRepository.findAll().stream().map(CompetitionEntity::getName).collect(Collectors.toList());
        int size = (int) competitionRepository.count() + 1;
        LOG.info(competition.getName().trim().toLowerCase(Locale.ROOT));
        if (list.stream().anyMatch(a -> a.trim().toLowerCase(Locale.ROOT).equals(competition.getName().trim().toLowerCase(Locale.ROOT)))) {
            LOG.info("Taka konkurencja już istnieje");
            return ResponseEntity.badRequest().body("Taka konkurencja już istnieje");
        }
        List<String> disciplines = List.of(competition.getDisciplines());
        CompetitionEntity c = CompetitionEntity.builder()
                .name(competition.getName())
                .abbreviation(competition.getAbbreviation())
                .discipline(competition.getDiscipline())
                .ordering(size)
                .type(competition.getType())
                .countingMethod(competition.getCountingMethod())
                .caliberUUID(competition.getCaliberUUID())
                .numberOfShots(competition.getNumberOfShots())
                .numberOfManyShotsList(null)
                .build();
        c.setDisciplineList(disciplines);
        competitionRepository.save(c);
        return ResponseEntity.status(201).body("utworzono konkurencję " + c.getName());
    }

    public ResponseEntity<?> updateCompetition(String uuid, Competition competition, String pinCode) throws NoUserPermissionException {

        CompetitionEntity one = competitionRepository.getOne(uuid);
        one.setOrdering(competition.getOrdering() != null ? competition.getOrdering() : one.getOrdering());
        one.setPracticeShots(competition.getPracticeShots() != null ? competition.getPracticeShots() : one.getPracticeShots());
        one.setCaliberUUID(competition.getCaliberUUID() != null ? competition.getCaliberUUID() : one.getCaliberUUID());
        if (competition.getName() != null && !competition.getName().equals("null")) {
            if (competitionRepository.findByNameEquals(competition.getName()).isEmpty()) {
                one.setName(competition.getName());
            } else {
                return ResponseEntity.badRequest().body("taka nazwa już istnieje i nie można zaktualizować konkurencji");
            }
        }
        one.setNumberOfShots(competition.getNumberOfShots() != null ? competition.getNumberOfShots() : one.getNumberOfShots());
        one.setCountingMethod(competition.getCountingMethod() != null ? competition.getCountingMethod() : one.getCountingMethod());
        one.setType(competition.getType() != null ? competition.getType() : one.getType());

        if (competition.getDiscipline() != null) {
            competition.setDiscipline(competition.getDiscipline().equals("") ? null : competition.getDiscipline());
            one.setDiscipline(competition.getDiscipline());
        }
        competition.setDisciplineList(competition.getDisciplineList().size() > 1 ? competition.getDisciplineList() : null);
        one.setDisciplineList(competition.getDisciplineList() != null ? competition.getDisciplineList() : one.getDisciplineList());
        competitionRepository.save(one);
        ResponseEntity<?> response = getStringResponseEntity(pinCode, one, HttpStatus.OK, "update Competition", "Zaktualizowano konkurencję");
        if (response.getStatusCode().equals(HttpStatus.OK)) {
            competitionRepository.save(one);
            competitionMembersListRepository.findAll()
                    .stream()
                    .filter(f -> f.getCompetitionUUID() != null && f.getCompetitionUUID().equals(one.getUuid()))
                    .forEach(e -> {
                        e.setOrdering(competition.getOrdering());
                        e.setPracticeShots(competition.getPracticeShots() != null ? competition.getPracticeShots() : e.getPracticeShots());
                        e.setCaliberUUID(competition.getCaliberUUID() != null ? competition.getCaliberUUID() : e.getCaliberUUID());
                        e.setName(competition.getName() != null ? competition.getName() : e.getName());
                        e.setNumberOfShots(competition.getNumberOfShots() != null ? competition.getNumberOfShots() : e.getNumberOfShots());
                        e.setCountingMethod(competition.getCountingMethod() != null ? competition.getCountingMethod() : e.getCountingMethod());
                        e.setType(competition.getType() != null ? competition.getType() : e.getType());
                        e.setDiscipline(competition.getDiscipline() != null ? competition.getDiscipline() : e.getDiscipline());
                        e.setDisciplineList(competition.getDisciplineList() != null ? competition.getDisciplineList() : e.getDisciplineList());
                        competitionMembersListRepository.save(e);
                    });
        }
        return response;
    }

    public ResponseEntity<?> getCompetitionMemberList(String competitionMembersListUUID) {
        if (competitionMembersListRepository.existsById(competitionMembersListUUID)) {
            return ResponseEntity.ok(competitionMembersListRepository.findById(competitionMembersListUUID));

        } else {
            return ResponseEntity.badRequest().body("brak takiej konkurencji");
        }
    }

    public ResponseEntity<?> getStringResponseEntity(String pinCode, CompetitionEntity entity, HttpStatus status, String methodName, Object body) throws NoUserPermissionException {
        ResponseEntity<?> response = ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON).body(body);
        ResponseEntity<String> stringResponseEntity = changeHistoryService.addRecordToChangeHistory(pinCode, entity != null ? entity.getClass().getSimpleName() + " " + methodName + " " : methodName, entity != null ? entity.getUuid() : "nie dotyczy");
        if (stringResponseEntity != null) {
            response = stringResponseEntity;
        }
        return response;
    }

    public List<String> getCountingMethods() {
        return Arrays.stream(CountingMethod.values()).map(CountingMethod::getName).collect(Collectors.toList());
    }

    public List<String> getDisciplines() {
        return Arrays.stream(Discipline.values()).map(Discipline::getName).collect(Collectors.toList());
    }

    public List<String> getCompetitionTypes() {
        return Arrays.stream(CompetitionType.values()).map(CompetitionType::getName).collect(Collectors.toList());
    }
}
