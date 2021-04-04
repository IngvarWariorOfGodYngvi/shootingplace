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

    ScoreEntity createScore(float score, float innerTen, float outerTen, String competitionMembersListEntityUUID, MemberEntity memberEntity, OtherPersonEntity otherPersonEntity) {
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
                number = scoreEntityList.stream().filter(f -> f.getMember() == (memberEntity)).findFirst().get().getMetricNumber();
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
                number = scoreEntityList.stream().filter(f -> f.getOtherPersonEntity() == (otherPersonEntity)).findFirst().get().getMetricNumber();
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
                .outerTen(outerTen)
                .ammunition(false)
                .gun(false)
                .name(name)
                .metricNumber(number)
                .build());

    }

    public boolean setScore(String scoreUUID, float score, float innerTen, float outerTen) {
        ScoreEntity scoreEntity = scoreRepository.findById(scoreUUID).orElseThrow(EntityNotFoundException::new);
        String competitionMembersListEntityUUID = scoreEntity.getCompetitionMembersListEntityUUID();
        CompetitionMembersListEntity competitionMembersListEntity = competitionMembersListRepository.findById(competitionMembersListEntityUUID).orElseThrow(EntityNotFoundException::new);
        if (competitionMembersListEntity.getCountingMethod() != null) {
            if (competitionMembersListEntity.getCountingMethod().equals(CountingMethod.COMSTOCK.getName())) {
                // Metoda COMSTOCK
                int numberOfShots = competitionMembersListEntity.getNumberOfShots();
                float penalties = numberOfShots - outerTen/*shots*/;
                scoreEntity.setOuterTen(outerTen);
                int points = (int) ((outerTen/*shots*/ * 5) + (penalties * -10));
                if (points < 0) {
                    points = 0;
                }
                float hf = points / innerTen /*time*/;
                scoreEntity.setInnerTen(innerTen);
                List<ScoreEntity> scoreList = competitionMembersListEntity.getScoreList();
                float hf1;
                if (scoreList.size() > 1) {
                    hf1 = scoreList.stream().max(Comparator.comparing(ScoreEntity::getHf)).orElseThrow(EntityNotFoundException::new).getHf();
                } else {
                    hf1 = hf;
                }
                scoreEntity.setHf(hf);
                if (hf < hf1) {
                    scoreEntity.setScore((hf / hf1) * 100);
                } else {
                    scoreEntity.setScore(100);
                }
                scoreRepository.saveAndFlush(scoreEntity);
                scoreList.forEach(e -> {
                    if (e.getHf() > 0) {
                        float hf2 = scoreList.stream().max(Comparator.comparing(ScoreEntity::getHf)).orElseThrow(EntityNotFoundException::new).getHf();
                        e.setScore((e.getHf() / hf2) * 100);
                        scoreRepository.saveAndFlush(e);
                    }
                });
                scoreList.sort(Comparator.comparing(ScoreEntity::getScore)
                        .reversed()
                        .thenComparing(Comparator.comparing(ScoreEntity::getInnerTen)
                                .reversed()));

            }
        } else {
            if (score < 0) {
                score = scoreEntity.getScore();
            }
            if (innerTen < 0) {
                innerTen = scoreEntity.getInnerTen();
            }
            if (outerTen < 0) {
                outerTen = scoreEntity.getOuterTen();
            }
            scoreEntity.setScore(score);
            scoreEntity.setInnerTen(innerTen);
            scoreEntity.setOuterTen(outerTen);
            scoreRepository.saveAndFlush(scoreEntity);
            List<ScoreEntity> scoreList = competitionMembersListEntity.getScoreList();
            scoreList.sort(Comparator.comparing(ScoreEntity::getScore)
                    .reversed()
                    .thenComparing(Comparator.comparing(ScoreEntity::getInnerTen)
                            .reversed()
                            .thenComparing(Comparator.comparing(ScoreEntity::getOuterTen)
                                    .reversed())
                    ));
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
}
