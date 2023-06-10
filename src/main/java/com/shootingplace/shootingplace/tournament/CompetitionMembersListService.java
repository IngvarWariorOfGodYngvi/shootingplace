package com.shootingplace.shootingplace.tournament;

import com.shootingplace.shootingplace.Mapping;
import com.shootingplace.shootingplace.competition.CompetitionEntity;
import com.shootingplace.shootingplace.competition.CompetitionRepository;
import com.shootingplace.shootingplace.history.HistoryService;
import com.shootingplace.shootingplace.member.MemberEntity;
import com.shootingplace.shootingplace.member.MemberRepository;
import com.shootingplace.shootingplace.otherPerson.OtherPersonEntity;
import com.shootingplace.shootingplace.otherPerson.OtherPersonRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class CompetitionMembersListService {

    private final MemberRepository memberRepository;
    private final CompetitionMembersListRepository competitionMembersListRepository;
    private final HistoryService historyService;
    private final OtherPersonRepository otherPersonRepository;
    private final ScoreService scoreService;
    private final TournamentRepository tournamentRepository;
    private final CompetitionRepository competitionRepository;
    private final Logger LOG = LogManager.getLogger();


    public CompetitionMembersListService(MemberRepository memberRepository, CompetitionMembersListRepository competitionMembersListRepository, HistoryService historyService, OtherPersonRepository otherPersonRepository, ScoreService scoreService, TournamentRepository tournamentRepository, CompetitionRepository competitionRepository) {
        this.memberRepository = memberRepository;
        this.competitionMembersListRepository = competitionMembersListRepository;
        this.historyService = historyService;
        this.otherPersonRepository = otherPersonRepository;
        this.scoreService = scoreService;
        this.tournamentRepository = tournamentRepository;
        this.competitionRepository = competitionRepository;
    }

    public ResponseEntity<?> setCompetitionUUIDToCompetitionMemberList() {
        int sizeCML = competitionMembersListRepository.findAll().size();
        AtomicInteger counter = new AtomicInteger();
        competitionMembersListRepository.findAll().forEach(e -> {
            CompetitionEntity competitionEntity = competitionRepository.findByNameEquals(e.getName()).orElse(null);
            if (competitionEntity != null) {
                e.setCompetitionUUID(competitionEntity.getUuid());
                counter.getAndIncrement();
                String[] disciplines = e.getDisciplines();
                if (disciplines == null) {
                    e.setDisciplineList(new ArrayList<>());
                } else {
                    e.setDisciplineList(List.of(disciplines));
                }
                Integer[] numberOfManyShots1 = e.getNumberOfManyShots();
                if (numberOfManyShots1 == null) {
                    e.setNumberOfManyShotsList(new ArrayList<>());
                } else {
                    List<String> numberOfManyShotsList = Stream.of(numberOfManyShots1).map(String::valueOf).collect(Collectors.toList());
                    e.setNumberOfManyShotsList(numberOfManyShotsList);

                }
                competitionMembersListRepository.save(e);
            }
        });
        competitionRepository.findAll().forEach(e -> {
            String[] disciplines = e.getDisciplines();
            if (disciplines == null) {
                e.setDisciplineList(new ArrayList<>());
            } else {
                e.setDisciplineList(List.of(disciplines));
            }
            Integer[] numberOfManyShots1 = e.getNumberOfManyShots();
            if (numberOfManyShots1 == null) {
                e.setNumberOfManyShotsList(new ArrayList<>());
            } else {
                List<String> numberOfManyShotsList = Stream.of(numberOfManyShots1).map(String::valueOf).collect(Collectors.toList());
                e.setNumberOfManyShotsList(numberOfManyShotsList);

            }
            competitionRepository.save(e);
        });
        return ResponseEntity.ok("ilość list: " + sizeCML + ". ilość nadanych id: " + counter);
    }

    public String addScoreToCompetitionList(String competitionUUID, int legitimationNumber, int otherPerson) {
//        String competitionUUID = getCompetitionIDByName(competitionName, tournamentUUID);
        CompetitionMembersListEntity list = competitionMembersListRepository.findById(competitionUUID).orElseThrow(EntityNotFoundException::new);
        List<ScoreEntity> scoreList = list.getScoreList();

        String failed = "Nie można dodać bo osoba już się znajduje na liście";
        String success = "Dodano Osobę do";
        if (legitimationNumber > 0) {
            MemberEntity member = memberRepository.findAll().stream().filter(f -> f.getLegitimationNumber().equals(legitimationNumber)).findFirst().orElse(null);
            boolean match = scoreList.stream().anyMatch(f -> f.getMember() == member);
            if (match) {
                LOG.info("Nie można dodać bo osoba już się znajduje na liście");
                return failed.concat(" " + list.getName());
            } else {
                ScoreEntity score = scoreService.createScore(0, 0, 0, 0, competitionUUID, member, null);
                scoreList.add(score);
                scoreList.sort(Comparator.comparing(ScoreEntity::getScore)
                        .reversed().thenComparing(ScoreEntity::getInnerTen)
                        .reversed().thenComparing(ScoreEntity::getOuterTen)
                        .reversed().thenComparing(ScoreEntity::isDnf)
                        .thenComparing(ScoreEntity::isDsq)
                        .thenComparing(ScoreEntity::isPk));
                competitionMembersListRepository.save(list);
                LOG.info("Dodano Klubowicza do Listy");
                if (member != null) {
                    historyService.addCompetitionRecord(member.getUuid(), list);
                }
                return success.concat(" " + list.getName());
            }
        }
        if (otherPerson > 0) {
            OtherPersonEntity otherPersonEntity = otherPersonRepository.findAll().stream().filter(f -> f.getId().equals(otherPerson)).findFirst().orElse(null);
            if (otherPersonEntity != null) {
                boolean match1 = scoreList.stream().anyMatch(a -> a.getOtherPersonEntity() == otherPersonEntity);
                if (match1) {
                    LOG.info("Nie można dodać bo osoba już się znajduje na liście");
                    return failed.concat(" " + list.getName());
                } else {
                    ScoreEntity score = scoreService.createScore(0, 0, 0, 0, competitionUUID, null, otherPersonEntity);
                    scoreList.add(score);
                    scoreList.sort(Comparator.comparing(ScoreEntity::getScore)
                            .reversed().thenComparing(ScoreEntity::getInnerTen)
                            .reversed().thenComparing(ScoreEntity::getOuterTen)
                            .reversed().thenComparing(ScoreEntity::isDnf)
                            .thenComparing(ScoreEntity::isDsq)
                            .thenComparing(ScoreEntity::isPk));
                    LOG.info("Dodano Obcego Zawodnika do Listy");
                    competitionMembersListRepository.save(list);
                    return success.concat(" " + list.getName());
                }
            }
        } else {
            return "coś poszło nie tak";
        }
        return "coś poszło nie tak";
    }


    public String removeScoreFromList(String competitionUUID, int legitimationNumber, int otherPerson) {

//        String competitionUUID = getCompetitionIDByName(competitionName, tournamentUUID);
        CompetitionMembersListEntity list = competitionMembersListRepository.findById(competitionUUID).orElseThrow(EntityNotFoundException::new);
        List<ScoreEntity> scoreList = list.getScoreList();
        ScoreEntity score;
        if (legitimationNumber > 0) {
            MemberEntity member = memberRepository.findAll().stream().filter(f -> f.getLegitimationNumber().equals(legitimationNumber)).findFirst().orElseThrow(EntityNotFoundException::new);
            score = scoreList.stream().filter(f -> f.getMember() == member).findFirst().orElseThrow(EntityNotFoundException::new);
        } else {
            OtherPersonEntity otherPersonEntity = otherPersonRepository.findAll().stream().filter(f -> f.getId().equals(otherPerson)).findFirst().orElseThrow(EntityNotFoundException::new);
            score = scoreList.stream().filter(f -> f.getOtherPersonEntity() == otherPersonEntity).findFirst().orElseThrow(EntityNotFoundException::new);
        }
        scoreList.remove(score);
        competitionMembersListRepository.save(list);
        LOG.info("Usunięto osobę do Listy");
        if (score.getMember() != null) {
            historyService.removeCompetitionRecord(score.getMember().getUuid(), list);
        }
        return "Usunięto osobę do Listy ";
    }

    public String getCompetitionIDByName(String competitionName, String tournamentUUID) {
        TournamentEntity tournamentEntity = tournamentRepository.findById(tournamentUUID).orElseThrow(EntityNotFoundException::new);
        return tournamentEntity.getCompetitionsList().stream().filter(f -> f.getName().equals(competitionName)).findFirst().orElseThrow(EntityNotFoundException::new).getUuid();
    }

    public ResponseEntity<?> removeListFromTournament(String tournamentUUID, String competitionUUID) {
        CompetitionMembersListEntity competitionMembersListEntity = competitionMembersListRepository.findById(competitionUUID).orElseThrow(EntityNotFoundException::new);
        TournamentEntity tournamentEntity = tournamentRepository.findById(tournamentUUID).orElseThrow(EntityNotFoundException::new);

        if (competitionMembersListEntity.getScoreList().isEmpty()) {
            tournamentEntity.getCompetitionsList().remove(competitionMembersListEntity);
            tournamentRepository.save(tournamentEntity);
            competitionMembersListRepository.delete(competitionMembersListEntity);
            return ResponseEntity.ok("Usunięto konkurencję");
        } else {
            return ResponseEntity.badRequest().body("Coś poszło nie tak");
        }

    }

    public List<String> getMemberStartsInTournament(String memberUUID, int otherID, String tournamentUUID) {

        TournamentEntity tournamentEntity = tournamentRepository.findById(tournamentUUID).orElseThrow(EntityNotFoundException::new);
        List<String> list = new ArrayList<>();

        if (otherID > 0) {
            OtherPersonEntity otherPersonEntity = otherPersonRepository.findById(otherID).orElseThrow(EntityNotFoundException::new);

            tournamentEntity.getCompetitionsList().forEach(e -> e.getScoreList().stream().filter(f -> f.getOtherPersonEntity() != null).forEach(g -> {
                if (g.getOtherPersonEntity().getId().equals(otherPersonEntity.getId())) {
                    list.add(e.getName());
                }
            }));
        } else {
            MemberEntity memberEntity = memberRepository.findById(memberUUID).orElseThrow(EntityNotFoundException::new);

            tournamentEntity.getCompetitionsList().forEach(e -> e.getScoreList().stream().filter(f -> f.getMember() != null).forEach(g -> {
                if (g.getMember().getUuid().equals(memberEntity.getUuid())) {
                    list.add(e.getName());
                }
            }));
        }
        return list;

    }

    public ResponseEntity<?> getMetricNumber(String legNumber, int otherID, String tournamentUUID) {

        TournamentEntity tournamentEntity = tournamentRepository.findById(tournamentUUID).orElseThrow(EntityNotFoundException::new);
        List<Integer> metricNumber = new ArrayList<>();
        if (otherID > 0) {
            tournamentEntity.getCompetitionsList()
                    .forEach(e -> e.getScoreList().stream()
                            .filter(f -> f.getOtherPersonEntity() != null)
                            .filter(f -> f.getOtherPersonEntity().getId().equals(otherID))
                            .forEach(g -> {
                                metricNumber.add(g.getMetricNumber());
                            }));
        } else {
            tournamentEntity.getCompetitionsList()
                    .forEach(e -> e.getScoreList().stream()
                            .filter(f -> f.getMember() != null)
                            .filter(f -> f.getMember().getLegitimationNumber().equals(Integer.valueOf(legNumber)))
                            .forEach(g -> {
                                metricNumber.add(g.getMetricNumber());
                            }));
        }
        if (metricNumber.isEmpty()) {
            return ResponseEntity.badRequest().body("Taka osoba nie znajduje się na żadnej liście");
        }
        return ResponseEntity.ok(metricNumber.get(0));

    }

    public Map<String,String> getMemberStartsInTournament(int legNumber, int otherID, String tournamentUUID) {

        TournamentEntity tournamentEntity = tournamentRepository.findById(tournamentUUID).orElseThrow(EntityNotFoundException::new);
        Map<String,String> map = new HashMap<>();

        if (otherID > 0) {
            OtherPersonEntity otherPersonEntity = otherPersonRepository.findById(otherID).orElseThrow(EntityNotFoundException::new);

            tournamentEntity.getCompetitionsList().forEach(e -> e.getScoreList().stream().filter(f -> f.getOtherPersonEntity() != null).forEach(g -> {
                if (g.getOtherPersonEntity().getId().equals(otherPersonEntity.getId())) {
                    map.put(e.getName(),e.getCompetitionUUID());
                }
            }));
        } else {
            MemberEntity memberEntity = memberRepository.findByLegitimationNumber(legNumber).orElseThrow(EntityNotFoundException::new);

            tournamentEntity.getCompetitionsList().forEach(e -> e.getScoreList().stream().filter(f -> f.getMember() != null).forEach(g -> {
                if (g.getMember().getUuid().equals(memberEntity.getUuid())) {
                    map.put(e.getName(),e.getCompetitionUUID());
                }
            }));
        }
        return map;

    }

    public ScoreEntity getScoreID(int legNumber, int otherID, String tournamentUUID, String competitionName) {
        TournamentEntity tournamentEntity = tournamentRepository.findById(tournamentUUID).orElseThrow(EntityNotFoundException::new);
        ScoreEntity score;
        if (otherID > 0) {
            score = tournamentEntity.getCompetitionsList()
                    .stream()
                    .filter(f -> f.getName().equals(competitionName))
                    .findFirst().orElseThrow(EntityNotFoundException::new)
                    .getScoreList()
                    .stream().filter(f -> f.getOtherPersonEntity() != null)
                    .filter(f -> f.getOtherPersonEntity().getId().equals(otherID))
                    .findFirst().orElseThrow(EntityNotFoundException::new);
        } else {
            score = tournamentEntity.getCompetitionsList()
                    .stream()
                    .filter(f -> f.getName().equals(competitionName))
                    .findFirst().orElseThrow(EntityNotFoundException::new)
                    .getScoreList()
                    .stream()
                    .filter(f -> f.getMember() != null)
                    .filter(f -> f.getMember().getLegitimationNumber().equals(legNumber))
                    .findFirst().orElseThrow(EntityNotFoundException::new);
        }
        return score;
    }

    public CompetitionMembersList getCompetitionListByID(String uuid) {
        return Mapping.map(competitionMembersListRepository.findById(uuid).orElseThrow(EntityNotFoundException::new));
    }
}
