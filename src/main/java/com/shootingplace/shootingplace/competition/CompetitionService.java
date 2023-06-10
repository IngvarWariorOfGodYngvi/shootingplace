package com.shootingplace.shootingplace.competition;

import com.shootingplace.shootingplace.enums.CompetitionType;
import com.shootingplace.shootingplace.enums.CountingMethod;
import com.shootingplace.shootingplace.enums.Discipline;
import com.shootingplace.shootingplace.history.ChangeHistoryService;
import com.shootingplace.shootingplace.tournament.CompetitionMembersListRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

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
        List<CompetitionEntity> competitionEntityList = competitionRepository.findAll();

        if (competitionEntityList.isEmpty()) {
            createCompetitions();
            LOG.info("Zostały utworzone domyślne encje Konkurencji");
        }
        for (CompetitionEntity competitionEntity : competitionEntityList) {
            if (competitionEntity.getNumberOfShots() == null) {
                String[] split = competitionEntity.getName().split(" ");
                for (String value : split) {
                    String s = value.replaceAll("[a-zA-ZżźćńółęąśŻŹĆĄŚĘŁÓŃ]", "");
                    if (!s.equals("")) {
                        competitionEntity.setNumberOfShots(Integer.parseInt(s));
                        competitionRepository.save(competitionEntity);
                    }
                }
            }
        }
        for (CompetitionEntity competitionEntity : competitionEntityList) {
            if (competitionEntity.getType() == null) {
                String[] split = competitionEntity.getName().split(" ");
                for (String s : split) {
                    if (s.equals(CompetitionType.OPEN.getName())) {
                        competitionEntity.setType(CompetitionType.OPEN.getName());
                    } else {
                        competitionEntity.setType(CompetitionType.YOUTH.getName());
                    }
                    competitionRepository.save(competitionEntity);

                }
            }
        }

        competitionEntityList.sort(Comparator.comparing(CompetitionEntity::getOrdering));
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
        List<String> list = new ArrayList<>();
        int size = competitionRepository.findAll().size() + 1;
        competitionRepository.findAll().forEach(e -> list.add(e.getName()));
        if (list.isEmpty()) {
            createCompetitions();
        }
        if (list.contains(competition.getName())) {
            LOG.info("Taka konkurencja już istnieje");
            return ResponseEntity.badRequest().body("Taka konkurencja już istnieje");
        }
        if (!competition.getDiscipline().equals("")) {
            if (competition.getDiscipline().equals(Discipline.PISTOL.getName()) || competition.getDiscipline().equals(Discipline.RIFLE.getName()) || competition.getDiscipline().equals(Discipline.SHOTGUN.getName())) {
                CompetitionEntity competitionEntity = CompetitionEntity.builder()
                        .name(competition.getName())
                        .numberOfShots(competition.getNumberOfShots())
                        .numberOfManyShots(competition.getNumberOfManyShots())
                        .discipline(competition.getDiscipline())
                        .type(competition.getType())
                        .ordering(size)
                        .caliberUUID(competition.getCaliberUUID())
                        .practiceShots(competition.getPracticeShots())
                        .build();
                if (competition.getCountingMethod().equals(CountingMethod.NORMAL.getName())) {
                    competitionEntity.setCountingMethod(CountingMethod.NORMAL.getName());
                    LOG.info("Ustawiono metodę liczenia " + CountingMethod.NORMAL.getName());
                }
                if (competition.getCountingMethod().equals(CountingMethod.COMSTOCK.getName())) {
                    LOG.info("Ustawiono metodę liczenia " + CountingMethod.COMSTOCK.getName());
                    competitionEntity.setCountingMethod(CountingMethod.COMSTOCK.getName());
                }
                competitionRepository.save(competitionEntity);
                LOG.info("Utworzono nową konkurencję " + competition.getName());
                return ResponseEntity.status(201).body("Utworzono nową konkurencję " + competition.getName());
            } else {
                LOG.info("Po prostu się nie udało");
                return ResponseEntity.badRequest().body("Po prostu się nie udało");
            }
        } else {
            CompetitionEntity competitionEntity = CompetitionEntity.builder()
                    .name(competition.getName())
                    .numberOfShots(competition.getNumberOfShots())
                    .discipline(null)
                    .disciplines(competition.getDisciplines())
                    .numberOfManyShots(competition.getNumberOfManyShots())
                    .type(competition.getType())
                    .ordering(size)
                    .caliberUUID(competition.getCaliberUUID())
                    .practiceShots(competition.getPracticeShots())
                    .build();
            if (competition.getCountingMethod().equals(CountingMethod.NORMAL.getName())) {
                competitionEntity.setCountingMethod(CountingMethod.NORMAL.getName());
            }
            if (competition.getCountingMethod().equals(CountingMethod.COMSTOCK.getName())) {
                LOG.info("Ustawiono metodę liczenia " + CountingMethod.COMSTOCK.getName());
            }
            competitionRepository.save(competitionEntity);
            LOG.info("Utworzono nową konkurencję " + competition.getName());
            return ResponseEntity.status(201).body("Utworzono nową konkurencję " + competition.getName());

        }
    }

    public ResponseEntity<?> updateCompetition(String uuid, Competition competition, String pinCode) {

        CompetitionEntity one = competitionRepository.getOne(uuid);
        if (competition.getOrdering() != null) {
            one.setOrdering(competition.getOrdering());
        }
        if (competition.getPracticeShots() != null) {
            one.setPracticeShots(competition.getPracticeShots());
        }
        if (competition.getCaliberUUID() != null) {
            one.setCaliberUUID(competition.getCaliberUUID());
        }
        if (competition.getName() != null) {
            if (competitionRepository.findByNameEquals(competition.getName()).isEmpty()) {
                one.setName(competition.getName());
            } else {
                return ResponseEntity.badRequest().body("taka nazwa już istnieje i nie można zaktualizować konkurencji");
            }
        }
        if (competition.getNumberOfShots() != null) {
            one.setNumberOfShots(competition.getNumberOfShots());
        }
        System.out.println(competition.getCountingMethod());
        if (competition.getCountingMethod() != null) {
            one.setCountingMethod(competition.getCountingMethod());
        }
        ResponseEntity<?> response = getStringResponseEntity(pinCode, one, HttpStatus.OK, "update Competition", "Zaktualizowano konkurencję");
        if (response.getStatusCode().equals(HttpStatus.OK)) {
            competitionRepository.save(one);
            competitionMembersListRepository.findAll()
                    .stream()
                    .filter(f-> f.getCompetitionUUID().equals(one.getUuid()))
                    .forEach(e -> {
                        e.setOrdering(competition.getOrdering());
                        e.setPracticeShots(competition.getPracticeShots()!=null?competition.getPracticeShots():e.getPracticeShots());
                        e.setCaliberUUID(competition.getCaliberUUID()!=null?competition.getCaliberUUID():e.getCaliberUUID());
                        e.setName(competition.getName()!=null?competition.getName():e.getName());
                        e.setNumberOfShots(competition.getNumberOfShots()!=null?competition.getNumberOfShots():e.getNumberOfShots());
                        e.setCountingMethod(competition.getCountingMethod()!=null?competition.getCountingMethod():e.getCountingMethod());
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

    public ResponseEntity<?> getStringResponseEntity(String pinCode, CompetitionEntity entity, HttpStatus status, String methodName, Object body) {
        ResponseEntity<?> response = ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON).body(body);
        ResponseEntity<String> stringResponseEntity = changeHistoryService.addRecordToChangeHistory(pinCode, entity != null ? entity.getClass().getSimpleName() + " " + methodName + " " : methodName, entity != null ? entity.getUuid() : "nie dotyczy");
        if (stringResponseEntity != null) {
            response = stringResponseEntity;
        }
        return response;
    }
}
