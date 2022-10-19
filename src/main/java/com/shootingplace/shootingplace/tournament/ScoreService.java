package com.shootingplace.shootingplace.tournament;

import com.shootingplace.shootingplace.enums.CountingMethod;
import com.shootingplace.shootingplace.member.MemberEntity;
import com.shootingplace.shootingplace.otherPerson.OtherPersonEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.ResponseEntity;
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

    private final Logger LOG = LogManager.getLogger(getClass());

    public ScoreService(ScoreRepository scoreRepository, CompetitionMembersListRepository competitionMembersListRepository, TournamentRepository tournamentRepository) {
        this.scoreRepository = scoreRepository;
        this.competitionMembersListRepository = competitionMembersListRepository;
        this.tournamentRepository = tournamentRepository;
    }

    public ScoreEntity createScore(float score, float innerTen, float outerTen, int procedures, String competitionMembersListEntityUUID, MemberEntity memberEntity, OtherPersonEntity otherPersonEntity) {
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

        scoreEntity = scoreRepository.save(ScoreEntity.builder()
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
        getNameFromScore(scoreEntity);

        LOG.info("Utworzono wynik dla " + name);

        return scoreEntity;

    }

    public ResponseEntity<?> setScore(String scoreUUID, float score, float innerTen, float outerTen, Float alfa, Float charlie, Float delta, int procedures) {

        if (!scoreRepository.existsById(scoreUUID)) {
            return ResponseEntity.badRequest().body("Nie znaleziono wyniku. Sprawdź identyfikator rekordu");
        }
        ScoreEntity scoreEntity = scoreRepository.getOne(scoreUUID);
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
            List<ScoreEntity> scoreList = competitionMembersListEntity
                    .getScoreList()
                    .stream()
                    .filter(f -> !f.isDsq())
                    .filter(f -> !f.isDnf())
                    .filter(f -> !f.isPk())
                    .sorted(Comparator.comparing(ScoreEntity::getScore).reversed())
                    .collect(Collectors.toList());
            if (competitionMembersListEntity.getNumberOfShots() == null) {
                scoreEntity.setScore(score);
                scoreRepository.save(scoreEntity);
            } else {
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
                    return ResponseEntity.badRequest().body("Coś poszło nie tak. Sprawdź poprawność danych");
                }
                scoreEntity.setOuterTen(outerTen);
                if (points < 0) {
                    points = 0;
                }
                float hf = points / (innerTen + (procedures * 3)) /*time*/;
                scoreEntity.setInnerTen(innerTen);

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

                scoreRepository.save(scoreEntity);
                scoreList.forEach(e -> {
                    if (e.getHf() > 0) {
                        float hf2 = scoreList.stream()
                                .max(Comparator.comparing(ScoreEntity::getHf))
                                .orElseThrow(EntityNotFoundException::new).getHf();
                        e.setScore((e.getHf() / hf2) * 100);
                        scoreRepository.save(e);
                    }
                });
            }
            scoreList.sort(Comparator.comparing(ScoreEntity::getScore)
                    .reversed());
            List<ScoreEntity> collect = competitionMembersListEntity
                    .getScoreList()
                    .stream()
                    .filter(f -> f.isDsq() || f.isDnf() || f.isPk())
                    .collect(Collectors.toList());
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
            scoreRepository.save(scoreEntity);

            List<ScoreEntity> scoreList = competitionMembersListEntity.getScoreList()
                    .stream()
                    .filter(f -> !f.isDsq())
                    .filter(f -> !f.isDnf())
                    .filter(f -> !f.isPk())
                    .sorted(Comparator.comparing(ScoreEntity::getScore)
                            .thenComparing(ScoreEntity::getInnerTen)
                            .thenComparing(ScoreEntity::getOuterTen).reversed())
                    .collect(Collectors.toList());

            List<ScoreEntity> collect = competitionMembersListEntity
                    .getScoreList()
                    .stream()
                    .filter(f -> f.isDnf() || f.isDsq() || f.isPk())
                    .collect(Collectors.toList());
            scoreList.addAll(collect);

            competitionMembersListEntity.setScoreList(scoreList);
        }
        competitionMembersListRepository.save(competitionMembersListEntity);
        String name = getNameFromScore(scoreEntity);
        return ResponseEntity.ok("\"Ustawiono wynik " + name + "\"");
    }

    public ResponseEntity<?> toggleAmmunitionInScore(String scoreUUID) {

        if (!scoreRepository.existsById(scoreUUID)) {
            return ResponseEntity.badRequest().body("Nie znaleziono wyniku. Sprawdź identyfikator rekordu");
        }
        ScoreEntity scoreEntity = scoreRepository.findById(scoreUUID).orElseThrow(EntityNotFoundException::new);
        scoreEntity.toggleAmmunition();

        String name = getNameFromScore(scoreEntity);

        scoreRepository.save(scoreEntity);
        LOG.info("wydaję amunicję " + name);
        return ResponseEntity.ok("\"Wydano amunicję " + name + "\"");
    }

    public ResponseEntity<?> toggleGunInScore(String scoreUUID) {

        if (!scoreRepository.existsById(scoreUUID)) {
            return ResponseEntity.badRequest().body("Nie znaleziono wyniku. Sprawdź identyfikator rekordu");
        }
        ScoreEntity scoreEntity = scoreRepository.findById(scoreUUID).orElseThrow(EntityNotFoundException::new);
        scoreEntity.toggleGun();
        scoreRepository.save(scoreEntity);

        String name = getNameFromScore(scoreEntity);
        LOG.info("wydaję broń " + name);
        return ResponseEntity.ok("\"Wydano broń " + name + "\"");
    }

    public ResponseEntity<?> toggleDnfScore(String scoreUUID) {

        if (!scoreRepository.existsById(scoreUUID)) {
            return ResponseEntity.badRequest().body("Nie znaleziono wyniku. Sprawdź identyfikator rekordu");
        }
        ScoreEntity scoreEntity = scoreRepository.findById(scoreUUID).orElseThrow(EntityNotFoundException::new);
        scoreEntity.toggleDnf();
        scoreRepository.save(scoreEntity);
        reorganizeCompetitionMembersList(scoreEntity);

        String name = getNameFromScore(scoreEntity);
        LOG.info("Ustawiam DNF " + name);
        return ResponseEntity.ok("\"Ustawiono DNF " + name + "\"");
    }

    public ResponseEntity<?> toggleDsqScore(String scoreUUID) {

        if (!scoreRepository.existsById(scoreUUID)) {
            return ResponseEntity.badRequest().body("Nie znaleziono wyniku. Sprawdź identyfikator rekordu");
        }
        String name;

        ScoreEntity scoreEntity = scoreRepository.findById(scoreUUID).orElseThrow(EntityNotFoundException::new);
        scoreEntity.toggleDsq();
        scoreRepository.save(scoreEntity);
        reorganizeCompetitionMembersList(scoreEntity);

        if (scoreEntity.getMember() != null) {
            name = scoreEntity.getMember().getFirstName() + " " + scoreEntity.getMember().getSecondName();
        } else {
            name = scoreEntity.getOtherPersonEntity().getFirstName() + " " + scoreEntity.getOtherPersonEntity().getSecondName();
        }
        LOG.info("Ustawiam DSQ " + name);
        return ResponseEntity.ok("\"Ustawiono DSQ " + name + "\"");
    }

    public ResponseEntity<?> togglePkScore(String scoreUUID) {

        if (!scoreRepository.existsById(scoreUUID)) {
            return ResponseEntity.badRequest().body("Nie znaleziono wyniku. Sprawdź identyfikator rekordu");
        }
        ScoreEntity scoreEntity = scoreRepository.findById(scoreUUID).orElseThrow(EntityNotFoundException::new);
        scoreEntity.togglePk();
        scoreRepository.save(scoreEntity);
        reorganizeCompetitionMembersList(scoreEntity);

        String name = getNameFromScore(scoreEntity);
        LOG.info("Ustawiono PK " + name);
        return ResponseEntity.ok("\"Ustawiono PK " + name + "\"");
    }

    public ResponseEntity<?> forceSetScore(String scoreUUID, float score) {
        ScoreEntity scoreEntity = scoreRepository.findById(scoreUUID).orElseThrow(EntityNotFoundException::new);

        String competitionMembersListEntityUUID = scoreEntity.getCompetitionMembersListEntityUUID();
        CompetitionMembersListEntity competitionMembersListEntity = competitionMembersListRepository.findById(competitionMembersListEntityUUID).orElseThrow(EntityNotFoundException::new);

        scoreEntity.setScore(score);
        scoreRepository.save(scoreEntity);
        List<ScoreEntity> scoreList = competitionMembersListEntity.getScoreList().stream().filter(f -> !f.isDsq()).filter(f -> !f.isDnf()).filter(f -> !f.isPk()).sorted(Comparator.comparing(ScoreEntity::getScore).reversed()).collect(Collectors.toList());

        scoreList.sort(Comparator.comparing(ScoreEntity::getScore)
                .reversed());
        List<ScoreEntity> collect = competitionMembersListEntity.getScoreList().stream().filter(f -> f.isDsq() || f.isDnf() || f.isPk()).collect(Collectors.toList());
        scoreList.addAll(collect);
        competitionMembersListEntity.setScoreList(scoreList);
        competitionMembersListRepository.save(competitionMembersListEntity);
        String name = getNameFromScore(scoreEntity);
        LOG.info("Ustawiono wynik na twardo " + name);
        return ResponseEntity.ok("\"Ustawiono wynik " + name + "\"");
    }

    private void reorganizeCompetitionMembersList(ScoreEntity scoreEntity) {
        CompetitionMembersListEntity competitionMembersListEntity = competitionMembersListRepository.findById(scoreEntity.getCompetitionMembersListEntityUUID()).orElseThrow(EntityNotFoundException::new);

        List<ScoreEntity> scoreList;
        if (competitionMembersListEntity.getCountingMethod().equals(CountingMethod.COMSTOCK.getName())) {
            scoreList = competitionMembersListEntity.getScoreList()
                    .stream()
                    .filter(f -> !f.isDsq())
                    .filter(f -> !f.isDnf())
                    .filter(f -> !f.isPk())
                    .sorted(Comparator.comparing(ScoreEntity::getScore).reversed())
                    .collect(Collectors.toList());
            scoreList.forEach(e -> {
                if (e.getHf() > 0) {
                    float hf2 = scoreList.stream()
                            .max(Comparator.comparing(ScoreEntity::getHf))
                            .orElseThrow(EntityNotFoundException::new).getHf();
                    e.setScore((e.getHf() / hf2) * 100);
                    scoreRepository.save(e);
                }
            });
            scoreList.sort(Comparator.comparing(ScoreEntity::getScore)
                    .reversed());
            List<ScoreEntity> collect = competitionMembersListEntity.getScoreList()
                    .stream()
                    .filter(f -> f.isDsq() || f.isDnf() || f.isPk())
                    .collect(Collectors.toList());
            scoreList.addAll(collect);
        } else {
            scoreList = competitionMembersListEntity.getScoreList()
                    .stream()
                    .filter(f -> !f.isDsq())
                    .filter(f -> !f.isDnf())
                    .filter(f -> !f.isPk())
                    .sorted(Comparator.comparing(ScoreEntity::getScore).reversed()
                            .thenComparing(ScoreEntity::getInnerTen).reversed()
                            .thenComparing(ScoreEntity::getOuterTen).reversed())
                    .collect(Collectors.toList());

            List<ScoreEntity> collect = competitionMembersListEntity.getScoreList().stream().filter(f -> f.isDnf() || f.isDsq()|| f.isPk()).collect(Collectors.toList());
            scoreList.addAll(collect);

        }
        competitionMembersListEntity.setScoreList(scoreList);
        competitionMembersListRepository.save(competitionMembersListEntity);
    }

    @NotNull
    private String getNameFromScore(ScoreEntity scoreEntity) {
        String name;
        if (scoreEntity.getMember() != null) {
            name = scoreEntity.getMember().getFirstName() + scoreEntity.getMember().getSecondName();
        } else {
            name = scoreEntity.getOtherPersonEntity().getFirstName() + scoreEntity.getOtherPersonEntity().getSecondName();
        }
        return name;
    }
}
