package com.shootingplace.shootingplace.services;

import com.shootingplace.shootingplace.domain.entities.*;
import com.shootingplace.shootingplace.domain.enums.CountingMethod;
import com.shootingplace.shootingplace.repositories.CompetitionMembersListRepository;
import com.shootingplace.shootingplace.repositories.ScoreRepository;
import com.shootingplace.shootingplace.repositories.TournamentRepository;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ScoreService {

    private final ScoreRepository scoreRepository;
    private final CompetitionMembersListRepository competitionMembersListRepository;
    private final TournamentRepository tournamentRepository;

    public ScoreService(ScoreRepository scoreRepository, CompetitionMembersListRepository competitionMembersListRepository, TournamentRepository tournamentRepository) {
        this.scoreRepository = scoreRepository;
        this.competitionMembersListRepository = competitionMembersListRepository;
        this.tournamentRepository = tournamentRepository;
    }

    ScoreEntity createScore(float score, float innerTen, float outerTen, int procedures, String competitionMembersListEntityUUID, MemberEntity memberEntity, OtherPersonEntity otherPersonEntity) {
        String name;
        if (memberEntity != null) {
            name = memberEntity.getSecondName() + " " + memberEntity.getFirstName();
        } else {
            name = otherPersonEntity.getSecondName() + " " + otherPersonEntity.getFirstName();
        }
        int number = 0;


        CompetitionMembersListEntity competitionMembersListEntity = competitionMembersListRepository.findById(competitionMembersListEntityUUID)
                .orElseThrow(EntityNotFoundException::new);

        TournamentEntity tournamentEntity = tournamentRepository.findById(competitionMembersListEntity.getAttachedToTournament()).orElseThrow(EntityNotFoundException::new);


        List<ScoreEntity> scoreEntityList = new ArrayList<>();

        tournamentEntity.getCompetitionsList().forEach(e -> scoreEntityList.addAll(e.getScoreList()));

        ScoreEntity scoreEntity = scoreEntityList.stream().max(Comparator.comparing(ScoreEntity::getMetricNumber)).orElse(null);

        boolean match = scoreEntityList.stream().anyMatch(a -> a.getMember() == (memberEntity));

        boolean match1 = scoreEntityList.stream().anyMatch(a -> a.getOtherPersonEntity() == (otherPersonEntity));

        if (memberEntity != null) {
            if (match) {
                number = scoreEntityList.stream().filter(f -> f.getMember() == (memberEntity)).findFirst().orElseThrow(EntityNotFoundException::new).getMetricNumber();
            } else {
                if (scoreEntity != null) {
                    number = scoreEntity.getMetricNumber() + 1;

                } else {
                    number = 1;
                }
            }
        }
        if (otherPersonEntity != null) {
            if (match1) {
                number = scoreEntityList.stream().filter(f -> f.getOtherPersonEntity() == (otherPersonEntity)).findFirst().orElseThrow(EntityNotFoundException::new).getMetricNumber();
            } else {
                if (scoreEntity != null) {
                    number = scoreEntity.getMetricNumber() + 1;

                } else {
                    number = 1;
                }
            }
        }

        return scoreRepository.saveAndFlush(ScoreEntity.builder()
                .competitionMembersListEntityUUID(competitionMembersListEntityUUID)
                .member(memberEntity)
                .otherPersonEntity(otherPersonEntity)
                .score(score)
                .innerTen(innerTen)
                .alfa(0)
                .charlie(0)
                .delta(0)
                .outerTen(outerTen)
                .procedures(procedures)
                .ammunition(false)
                .gun(false)
                .name(name)
                .metricNumber(number)
                .build());

    }

    public boolean setScore(String scoreUUID, float score, float innerTen, float outerTen, Float alfa, Float charlie, Float delta, int procedures) {
        ScoreEntity scoreEntity = scoreRepository.findById(scoreUUID).orElseThrow(EntityNotFoundException::new);
        String competitionMembersListEntityUUID = scoreEntity.getCompetitionMembersListEntityUUID();
        CompetitionMembersListEntity competitionMembersListEntity = competitionMembersListRepository.findById(competitionMembersListEntityUUID).orElseThrow(EntityNotFoundException::new);
        if (competitionMembersListEntity.getCountingMethod() != null && competitionMembersListEntity.getCountingMethod().equals(CountingMethod.COMSTOCK.getName())) {
            // Metoda COMSTOCK
            if (innerTen == -1) {
                //czas
                innerTen = scoreEntity.getInnerTen();
            }
            if (outerTen == -1) {
                //trafienia
                outerTen = scoreEntity.getOuterTen();
            }
            if (procedures == -1) {
                //procedury
                procedures = scoreEntity.getProcedures();
            }
            if (alfa == -1) {
                alfa = (float) 0;
            }
            if (charlie == -1) {
                charlie = (float) 0;
            }
            if (delta == -1) {
                delta = (float) 0;
            }

            int numberOfShots = competitionMembersListEntity.getNumberOfShots();
            int points;
            float penalties = numberOfShots - outerTen/*shots*/;

            if (alfa > 0 || charlie > 0 || delta > 0) {
                outerTen = alfa + charlie + delta;
                points = (int) ((alfa/*shots*/ * 5) + (charlie * 3) + (delta * 1) + (penalties * -10));
            } else {
                points = (int) ((outerTen * 5) + (penalties * -10));
            }
            if (outerTen > numberOfShots) {
                return false;
            }
            scoreEntity.setOuterTen(outerTen);
            if (points < 0) {
                points = 0;
            }
            float hf = points / (innerTen + (procedures * 3)) /*time*/;
            scoreEntity.setInnerTen(innerTen);
            List<ScoreEntity> scoreList = competitionMembersListEntity.getScoreList().stream().filter(f -> !f.isDsq()).filter(f -> !f.isDnf()).sorted(Comparator.comparing(ScoreEntity::getScore).reversed()).collect(Collectors.toList());
            float hf1;
            if (scoreList.size() > 1) {
                hf1 = scoreList.stream()
                        .max(Comparator.comparing(ScoreEntity::getHf))
                        .orElseThrow(EntityNotFoundException::new)
                        .getHf();
            } else {
                hf1 = hf;
            }
            scoreEntity.setHf(hf);
            scoreEntity.setProcedures(procedures);
            scoreEntity.setAlfa(alfa);
            scoreEntity.setCharlie(charlie);
            scoreEntity.setDelta(delta);
            if (hf < hf1) {
                scoreEntity.setScore((hf / hf1) * 100);
            } else {
                scoreEntity.setScore(100);
            }
            if (innerTen <= 0) {
                scoreEntity.setScore(0);
                scoreEntity.setHf(0);
            }
            scoreRepository.saveAndFlush(scoreEntity);
            scoreList.forEach(e -> {
                if (e.getHf() > 0) {
                    float hf2 = scoreList.stream()
                            .max(Comparator.comparing(ScoreEntity::getHf))
                            .orElseThrow(EntityNotFoundException::new).getHf();
                    e.setScore((e.getHf() / hf2) * 100);
                    scoreRepository.saveAndFlush(e);
                }
            });
            scoreList.sort(Comparator.comparing(ScoreEntity::getScore)
                    .reversed());
            List<ScoreEntity> collect = competitionMembersListEntity.getScoreList().stream().filter(f -> f.isDsq() || f.isDnf()).collect(Collectors.toList());
            scoreList.addAll(collect);
            competitionMembersListEntity.setScoreList(scoreList);


        } else {
            // Liczenie normalne
            if (score == -1) {
                score = scoreEntity.getScore();
            }
            if (innerTen == -1) {
                innerTen = scoreEntity.getInnerTen();
            }
            if (outerTen == -1) {
                outerTen = scoreEntity.getOuterTen();
            }
            scoreEntity.setScore(score);
            scoreEntity.setInnerTen(innerTen);
            scoreEntity.setOuterTen(outerTen);
            scoreRepository.saveAndFlush(scoreEntity);

            List<ScoreEntity> scoreList = competitionMembersListEntity.getScoreList()
                    .stream()
                    .filter(f -> !f.isDsq())
                    .filter(f -> !f.isDnf())
                    .sorted(Comparator.comparing(ScoreEntity::getScore).reversed()
                            .thenComparing(ScoreEntity::getInnerTen).reversed()
                            .thenComparing(ScoreEntity::getOuterTen).reversed())
                    .collect(Collectors.toList());

            List<ScoreEntity> collect = competitionMembersListEntity.getScoreList().stream().filter(f -> f.isDnf() || f.isDsq()).collect(Collectors.toList());
            scoreList.addAll(collect);

            competitionMembersListEntity.setScoreList(scoreList);
        }
        competitionMembersListRepository.saveAndFlush(competitionMembersListEntity);
        return true;
    }

    public boolean toggleAmmunitionInScore(String scoreUUID) {
        ScoreEntity scoreEntity = scoreRepository.findById(scoreUUID).orElseThrow(EntityNotFoundException::new);
        scoreEntity.toggleAmmunition();
        scoreRepository.saveAndFlush(scoreEntity);
        return true;
    }

    public boolean toggleGunInScore(String scoreUUID) {
        ScoreEntity scoreEntity = scoreRepository.findById(scoreUUID).orElseThrow(EntityNotFoundException::new);
        scoreEntity.toggleGun();
        scoreRepository.saveAndFlush(scoreEntity);
        return true;
    }

    public boolean toggleDnfScore(String scoreUUID) {
        ScoreEntity scoreEntity = scoreRepository.findById(scoreUUID).orElseThrow(EntityNotFoundException::new);
        scoreEntity.toggleDnf();
        scoreRepository.saveAndFlush(scoreEntity);
        CompetitionMembersListEntity competitionMembersListEntity = competitionMembersListRepository.findById(scoreEntity.getCompetitionMembersListEntityUUID()).orElseThrow(EntityNotFoundException::new);

        List<ScoreEntity> scoreList;
        if (competitionMembersListEntity.getCountingMethod().equals(CountingMethod.COMSTOCK.getName())) {
            scoreList = competitionMembersListEntity.getScoreList().stream().filter(f -> !f.isDsq()).filter(f -> !f.isDnf()).sorted(Comparator.comparing(ScoreEntity::getScore).reversed()).collect(Collectors.toList());
            scoreList.forEach(e -> {
                if (e.getHf() > 0) {
                    float hf2 = scoreList.stream()
                            .max(Comparator.comparing(ScoreEntity::getHf))
                            .orElseThrow(EntityNotFoundException::new).getHf();
                    e.setScore((e.getHf() / hf2) * 100);
                    scoreRepository.saveAndFlush(e);
                }
            });
            scoreList.sort(Comparator.comparing(ScoreEntity::getScore)
                    .reversed());
            List<ScoreEntity> collect = competitionMembersListEntity.getScoreList().stream().filter(f -> f.isDsq() || f.isDnf()).collect(Collectors.toList());
            scoreList.addAll(collect);
        } else {
            scoreList = competitionMembersListEntity.getScoreList()
                    .stream()
                    .filter(f -> !f.isDsq())
                    .filter(f -> !f.isDnf())
                    .sorted(Comparator.comparing(ScoreEntity::getScore).reversed()
                            .thenComparing(ScoreEntity::getInnerTen).reversed()
                            .thenComparing(ScoreEntity::getOuterTen).reversed())
                    .collect(Collectors.toList());

            List<ScoreEntity> collect = competitionMembersListEntity.getScoreList().stream().filter(f -> f.isDnf() || f.isDsq()).collect(Collectors.toList());
            scoreList.addAll(collect);

        }
        competitionMembersListEntity.setScoreList(scoreList);
        competitionMembersListRepository.saveAndFlush(competitionMembersListEntity);

        return true;
    }

    public boolean toggleDsqScore(String scoreUUID) {
        ScoreEntity scoreEntity = scoreRepository.findById(scoreUUID).orElseThrow(EntityNotFoundException::new);
        scoreEntity.toggleDsq();
        scoreRepository.saveAndFlush(scoreEntity);
        CompetitionMembersListEntity competitionMembersListEntity = competitionMembersListRepository.findById(scoreEntity.getCompetitionMembersListEntityUUID()).orElseThrow(EntityNotFoundException::new);

        List<ScoreEntity> scoreList;
        if (competitionMembersListEntity.getCountingMethod().equals(CountingMethod.COMSTOCK.getName())) {
            scoreList = competitionMembersListEntity.getScoreList().stream().filter(f -> !f.isDsq()).filter(f -> !f.isDnf()).sorted(Comparator.comparing(ScoreEntity::getScore).reversed()).collect(Collectors.toList());
            scoreList.forEach(e -> {
                if (e.getHf() > 0) {
                    float hf2 = scoreList.stream()
                            .max(Comparator.comparing(ScoreEntity::getHf))
                            .orElseThrow(EntityNotFoundException::new).getHf();
                    e.setScore((e.getHf() / hf2) * 100);
                    scoreRepository.saveAndFlush(e);
                }
            });
            scoreList.sort(Comparator.comparing(ScoreEntity::getScore)
                    .reversed());
            List<ScoreEntity> collect = competitionMembersListEntity.getScoreList().stream().filter(f -> f.isDsq() || f.isDnf()).collect(Collectors.toList());
            scoreList.addAll(collect);
        } else {
            scoreList = competitionMembersListEntity.getScoreList()
                    .stream()
                    .filter(f -> !f.isDsq())
                    .filter(f -> !f.isDnf())
                    .sorted(Comparator.comparing(ScoreEntity::getScore).reversed()
                            .thenComparing(ScoreEntity::getInnerTen).reversed()
                            .thenComparing(ScoreEntity::getOuterTen).reversed())
                    .collect(Collectors.toList());

            List<ScoreEntity> collect = competitionMembersListEntity.getScoreList().stream().filter(f -> f.isDnf() || f.isDsq()).collect(Collectors.toList());
            scoreList.addAll(collect);

        }
        competitionMembersListEntity.setScoreList(scoreList);
        competitionMembersListRepository.saveAndFlush(competitionMembersListEntity);
        return true;
    }
}
