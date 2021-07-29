package com.shootingplace.shootingplace.services;

import com.shootingplace.shootingplace.domain.entities.CompetitionEntity;
import com.shootingplace.shootingplace.domain.enums.CompetitionType;
import com.shootingplace.shootingplace.domain.enums.CountingMethod;
import com.shootingplace.shootingplace.domain.enums.Discipline;
import com.shootingplace.shootingplace.domain.models.Competition;
import com.shootingplace.shootingplace.repositories.CompetitionMembersListRepository;
import com.shootingplace.shootingplace.repositories.CompetitionRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CompetitionService {

    private final CompetitionRepository competitionRepository;
    private final CompetitionMembersListRepository competitionMembersListRepository;
    private final Logger LOG = LogManager.getLogger(getClass());


    public CompetitionService(CompetitionRepository competitionRepository, CompetitionMembersListRepository competitionMembersListRepository) {
        this.competitionRepository = competitionRepository;
        this.competitionMembersListRepository = competitionMembersListRepository;
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
                        competitionRepository.saveAndFlush(competitionEntity);
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
                    competitionRepository.saveAndFlush(competitionEntity);

                }
            }
        }

        LOG.info("Wyświetlono listę Konkurencji");
        competitionEntityList.sort(Comparator.comparing(CompetitionEntity::getOrdering));
        return competitionEntityList;
    }

    private void createCompetitions() {

        competitionRepository.saveAndFlush(CompetitionEntity.builder()
                .uuid(UUID.randomUUID().toString())
                .name("25m Pistolet sportowy 10 strzałów OPEN")
                .numberOfShots(10)
                .type(CompetitionType.OPEN.getName())
                .discipline(Discipline.PISTOL.getName())
                .countingMethod(CountingMethod.NORMAL.getName())
                .ordering(3)
                .build());
        competitionRepository.saveAndFlush(CompetitionEntity.builder()
                .uuid(UUID.randomUUID().toString())
                .name("25m Pistolet centralnego zapłonu 10 strzałów OPEN")
                .numberOfShots(10)
                .type(CompetitionType.OPEN.getName())
                .discipline(Discipline.PISTOL.getName())
                .countingMethod(CountingMethod.NORMAL.getName())
                .ordering(4)
                .build());
        competitionRepository.saveAndFlush(CompetitionEntity.builder()
                .uuid(UUID.randomUUID().toString())
                .name("10m Pistolet pneumatyczny 10 strzałów OPEN")
                .numberOfShots(10)
                .type(CompetitionType.OPEN.getName())
                .discipline(Discipline.PISTOL.getName())
                .countingMethod(CountingMethod.NORMAL.getName())
                .ordering(2)
                .build());
        competitionRepository.saveAndFlush(CompetitionEntity.builder()
                .uuid(UUID.randomUUID().toString())
                .name("50m Pistolet dowolny 10 strzałów OPEN")
                .numberOfShots(10)
                .type(CompetitionType.OPEN.getName())
                .discipline(Discipline.PISTOL.getName())
                .countingMethod(CountingMethod.NORMAL.getName())
                .ordering(5)
                .build());
        competitionRepository.saveAndFlush(CompetitionEntity.builder()
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

    public boolean createNewCompetition(Competition competition) {
        List<String> list = new ArrayList<>();
        int size = competitionRepository.findAll().size() + 1;
        competitionRepository.findAll().forEach(e -> list.add(e.getName()));
        if (list.isEmpty()) {
            createCompetitions();
        }
        if (list.contains(competition.getName())) {
            LOG.info("Taka konkurencja już istnieje");
            return false;
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
                        .build();
                if (competition.getCountingMethod().equals(CountingMethod.NORMAL.getName())) {
                    competitionEntity.setCountingMethod(CountingMethod.NORMAL.getName());
                    LOG.info("Ustawiono metodę liczenia " + CountingMethod.NORMAL.getName());
                }
                if (competition.getCountingMethod().equals(CountingMethod.COMSTOCK.getName())) {
                    LOG.info("Ustawiono metodę liczenia " + CountingMethod.COMSTOCK.getName());
                    competitionEntity.setCountingMethod(CountingMethod.COMSTOCK.getName());
                }
                competitionRepository.saveAndFlush(competitionEntity);
                LOG.info("Utworzono nową konkurencję \"" + competition.getName() + "\"");
                return true;
            } else {
                LOG.info("Po prostu się nie udało");
                return false;
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
                    .build();
            if (competition.getCountingMethod().equals(CountingMethod.NORMAL.getName())) {
                competitionEntity.setCountingMethod(CountingMethod.NORMAL.getName());
                LOG.info("Ustawiono metodę liczenia " + CountingMethod.NORMAL.getName());
            }
            if (competition.getCountingMethod().equals(CountingMethod.COMSTOCK.getName())) {
                LOG.info("Ustawiono metodę liczenia " + CountingMethod.COMSTOCK.getName());
                competitionEntity.setCountingMethod(CountingMethod.COMSTOCK.getName());
            }
            competitionRepository.saveAndFlush(competitionEntity);
            LOG.info("Utworzono nową konkurencję \"" + competition.getName() + "\"");
            return true;
        }
    }

    public boolean updateOrderingNumber(String uuid, String orderNumber) {

        List<CompetitionEntity> all = competitionRepository.findAll();

        CompetitionEntity competitionEntity = all.stream().filter(f -> f.getOrdering().equals(Integer.parseInt(orderNumber))).findFirst().orElse(null);

        CompetitionEntity one = competitionRepository.getOne(uuid);
        if (competitionEntity != null) {
            competitionEntity.setOrdering(one.getOrdering());
        }
        one.setOrdering(Integer.valueOf(orderNumber));

        competitionRepository.saveAndFlush(one);
        if (competitionEntity != null) {
            competitionRepository.saveAndFlush(competitionEntity);
        }
        competitionMembersListRepository.findAll().stream().filter(f -> f.getName().equals(one.getName())).forEach(e -> {
            e.setOrdering(Integer.valueOf(orderNumber));
            competitionMembersListRepository.saveAndFlush(e);
        });
        return true;
    }
}
