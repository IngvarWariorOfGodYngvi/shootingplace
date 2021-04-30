package com.shootingplace.shootingplace.services;

import com.shootingplace.shootingplace.domain.entities.CompetitionEntity;
import com.shootingplace.shootingplace.domain.enums.CompetitionType;
import com.shootingplace.shootingplace.domain.enums.CountingMethod;
import com.shootingplace.shootingplace.domain.enums.Discipline;
import com.shootingplace.shootingplace.domain.models.Competition;
import com.shootingplace.shootingplace.repositories.CompetitionRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class CompetitionService {

    private final CompetitionRepository competitionRepository;
    private final Logger LOG = LogManager.getLogger(getClass());


    public CompetitionService(CompetitionRepository competitionRepository) {
        this.competitionRepository = competitionRepository;
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
        competitionEntityList.sort(Comparator.comparing(CompetitionEntity::getName));
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
                .build());
        competitionRepository.saveAndFlush(CompetitionEntity.builder()
                .uuid(UUID.randomUUID().toString())
                .name("25m Pistolet centralnego zapłonu 10 strzałów OPEN")
                .numberOfShots(10)
                .type(CompetitionType.OPEN.getName())
                .discipline(Discipline.PISTOL.getName())
                .countingMethod(CountingMethod.NORMAL.getName())
                .build());
        competitionRepository.saveAndFlush(CompetitionEntity.builder()
                .uuid(UUID.randomUUID().toString())
                .name("10m Pistolet pneumatyczny 10 strzałów OPEN")
                .numberOfShots(10)
                .type(CompetitionType.OPEN.getName())
                .discipline(Discipline.PISTOL.getName())
                .countingMethod(CountingMethod.NORMAL.getName())
                .build());
        competitionRepository.saveAndFlush(CompetitionEntity.builder()
                .uuid(UUID.randomUUID().toString())
                .name("50m Pistolet dowolny 10 strzałów OPEN")
                .numberOfShots(10)
                .type(CompetitionType.OPEN.getName())
                .discipline(Discipline.PISTOL.getName())
                .countingMethod(CountingMethod.NORMAL.getName())
                .build());
        competitionRepository.saveAndFlush(CompetitionEntity.builder()
                .uuid(UUID.randomUUID().toString())
                .name("10m Karabin pneumatyczny 10 strzałów OPEN")
                .numberOfShots(10)
                .type(CompetitionType.OPEN.getName())
                .discipline(Discipline.RIFLE.getName())
                .countingMethod(CountingMethod.NORMAL.getName())
                .build());
        LOG.info("Stworzono encje konkurencji");
    }

    public boolean createNewCompetition(Competition competition) {
        List<String> list = new ArrayList<>();
        competitionRepository.findAll().forEach(e -> list.add(e.getName()));
        if (list.isEmpty()) {
            createCompetitions();
        }
        if (list.contains(competition.getName())) {
            LOG.info("Taka konkurencja już istnieje");
            return false;
        }
        if (competition.getDiscipline().equals(Discipline.PISTOL.getName()) || competition.getDiscipline().equals(Discipline.RIFLE.getName()) || competition.getDiscipline().equals(Discipline.SHOTGUN.getName())) {
            CompetitionEntity competitionEntity = CompetitionEntity.builder()
                    .name(competition.getName())
                    .numberOfShots(competition.getNumberOfShots())
                    .discipline(competition.getDiscipline())
                    .type(competition.getType())
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
    }

}
