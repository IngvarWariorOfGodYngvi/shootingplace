package com.shootingplace.shootingplace.services;

import com.shootingplace.shootingplace.domain.entities.*;
import com.shootingplace.shootingplace.domain.enums.ArbiterWorkClass;
import com.shootingplace.shootingplace.domain.models.CompetitionMembersList;
import com.shootingplace.shootingplace.domain.models.MemberDTO;
import com.shootingplace.shootingplace.domain.models.Tournament;
import com.shootingplace.shootingplace.domain.models.TournamentDTO;
import com.shootingplace.shootingplace.repositories.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TournamentService {

    private final TournamentRepository tournamentRepository;
    private final MemberRepository memberRepository;
    private final OtherPersonRepository otherPersonRepository;
    private final CompetitionMembersListRepository competitionMembersListRepository;
    private final CompetitionRepository competitionRepository;
    private final HistoryService historyService;
    private final ChangeHistoryService changeHistoryService;
    private final UsedHistoryRepository usedHistoryRepository;
    private final Logger LOG = LogManager.getLogger(getClass());


    public TournamentService(TournamentRepository tournamentRepository, MemberRepository memberRepository, OtherPersonRepository otherPersonRepository, CompetitionMembersListRepository competitionMembersListRepository, CompetitionRepository competitionRepository, HistoryService historyService, ChangeHistoryService changeHistoryService, UsedHistoryRepository usedHistoryRepository) {
        this.tournamentRepository = tournamentRepository;
        this.memberRepository = memberRepository;
        this.otherPersonRepository = otherPersonRepository;
        this.competitionMembersListRepository = competitionMembersListRepository;
        this.competitionRepository = competitionRepository;
        this.historyService = historyService;
        this.changeHistoryService = changeHistoryService;
        this.usedHistoryRepository = usedHistoryRepository;
    }

    public ResponseEntity<String> createNewTournament(Tournament tournament) {
        String[] s1 = tournament.getName().split(" ");
        StringBuilder name = new StringBuilder();
        for (String value : s1) {
            String splinted = value.substring(0, 1).toUpperCase() + value.substring(1).toLowerCase() + " ";
            name.append(splinted);
        }
        TournamentEntity tournamentEntity = TournamentEntity.builder()
                .name(name.toString())
                .date(tournament.getDate())
                .open(tournament.isOpen())
                .WZSS(tournament.isWzss())
                .build();

        if (tournament.getDate() == null) {
            tournamentEntity.setDate(LocalDate.now());
        }


        tournamentRepository.saveAndFlush(tournamentEntity);
        LOG.info("Stworzono nowe zawody " + tournamentEntity.getName());

        return ResponseEntity.status(201).body("\"" + tournamentEntity.getUuid() + "\"");
    }

    public boolean updateTournament(String tournamentUUID, Tournament tournament) {
        TournamentEntity tournamentEntity = tournamentRepository.findById(tournamentUUID)
                .orElseThrow(EntityNotFoundException::new);
        if (tournamentEntity.isOpen()) {
            if (tournament.getName() != null) {
                if (!tournament.getName().isEmpty()) {
                    String[] s1 = tournament.getName().split(" ");
                    StringBuilder name = new StringBuilder();
                    for (String value : s1) {
                        String splinted = value.substring(0, 1).toUpperCase() + value.substring(1).toLowerCase() + " ";
                        name.append(splinted);
                    }
                    tournamentEntity.setName(name.toString());
                    LOG.info("Zmieniono nazwę zawodów");
                }
            }

            if (tournament.getDate() != null) {
                tournamentEntity.setDate(tournament.getDate());
                LOG.info("Zmieniono datę zawodów");
            }
            tournamentRepository.saveAndFlush(tournamentEntity);
            historyService.updateTournamentEntityInCompetitionHistory(tournamentUUID);
            historyService.updateTournamentInJudgingHistory(tournamentUUID);
            return true;
        }

        LOG.warn("Zawody są już zamknięte i nie można już nic zrobić");
        return true;
    }


    public ResponseEntity<?> getOpenTournament() {

        List<TournamentEntity> collect = tournamentRepository.findAll().stream().filter(TournamentEntity::isOpen).collect(Collectors.toList());
        if (collect.size() > 1) {
            return ResponseEntity.status(409).body("Pojawił się jakiś konflikt i nie można wyświetlić zawodów");
        }
        if(collect.size() == 0){
            return ResponseEntity.status(418).body("Nie ma nic do wyświetlenia");
        }
        TournamentEntity tournamentEntity = collect.get(0);
        MemberDTO mainArbiterDTO;
        MemberDTO commissionRTSArbiter;
        if (tournamentEntity.getMainArbiter() != null) {
            mainArbiterDTO = Mapping.map2DTO(tournamentEntity.getMainArbiter());
        } else {
            mainArbiterDTO = null;
        }
        if (tournamentEntity.getCommissionRTSArbiter() != null) {
            commissionRTSArbiter = Mapping.map2DTO(tournamentEntity.getCommissionRTSArbiter());
        } else {
            commissionRTSArbiter = null;
        }
// DO NOT TOUCH THIS PART
        tournamentEntity.getCompetitionsList()
                .forEach(e->
                {if (e.getOrdering()==null){
                    CompetitionEntity competitionEntity = competitionRepository.findByNameEquals(e.getName()).orElseThrow(EntityNotFoundException::new);
                    e.setOrdering(competitionEntity.getOrdering());
                    competitionMembersListRepository.save(e);
                }});

        List<CompetitionMembersList> collect1 = tournamentEntity.getCompetitionsList()
                .stream().map(Mapping::map)
                .sorted(Comparator.comparing(CompetitionMembersList::getOrdering))
                .collect(Collectors.toList());

        Tournament tournament = Tournament.builder()
                .uuid(tournamentEntity.getUuid())
                .date(tournamentEntity.getDate())
                .name(tournamentEntity.getName())
                .open(tournamentEntity.isOpen())
                .wzss(tournamentEntity.isWZSS())
                .mainArbiter(mainArbiterDTO)
                .commissionRTSArbiter(commissionRTSArbiter)
                .otherMainArbiter(tournamentEntity.getOtherMainArbiter())
                .otherCommissionRTSArbiter(tournamentEntity.getOtherCommissionRTSArbiter())
                .arbitersList(tournamentEntity.getArbitersList().stream().map(Mapping::map2DTO).collect(Collectors.toList()))
                .otherArbitersList(tournamentEntity.getOtherArbitersList())
                .arbitersRTSList(tournamentEntity.getArbitersRTSList().stream().map(Mapping::map2DTO).collect(Collectors.toList()))
                .otherArbitersRTSList(tournamentEntity.getOtherArbitersRTSList())
                .competitionsList(collect1)
                .build();


        LOG.info("Wyświetlono listę zawodów");
        return ResponseEntity.ok(tournament);
}

    public ResponseEntity<?> closeTournament(String tournamentUUID) {
        TournamentEntity tournamentEntity = tournamentRepository.findById(tournamentUUID).orElseThrow(EntityNotFoundException::new);
        if (tournamentEntity.isOpen()) {
            LOG.info("Zawody " + tournamentEntity.getName() + " zostały zamknięte");
            tournamentEntity.setOpen(false);
            tournamentRepository.saveAndFlush(tournamentEntity);
            return ResponseEntity.ok("\"Zawody zostały zamknięte\"");
        } else {
            return ResponseEntity.badRequest().body("\"Nie można zamknąć zawodów\"");
        }
    }

    public ResponseEntity<?> removeArbiterFromTournament(String tournamentUUID, int legitimationNumber) {
        TournamentEntity tournamentEntity = tournamentRepository.findById(tournamentUUID).orElseThrow(EntityNotFoundException::new);
        if (tournamentEntity.isOpen()) {

            MemberEntity memberEntity = memberRepository
                    .findAll()
                    .stream()
                    .filter(f -> f.getLegitimationNumber()
                            .equals(legitimationNumber))
                    .findFirst().orElseThrow(EntityNotFoundException::new);

            List<MemberEntity> list = tournamentEntity.getArbitersList();

            historyService.removeJudgingRecord(memberEntity.getUuid(), tournamentUUID);
            list.remove(memberEntity);

            tournamentRepository.saveAndFlush(tournamentEntity);

            return ResponseEntity.ok("\"Usunięto Sędziego\"");
        }
        return ResponseEntity.status(418).body("\"I'm a teapot\"");
    }

    public ResponseEntity<String> removeOtherArbiterFromTournament(String tournamentUUID, int id) {

        if (!tournamentRepository.existsById(tournamentUUID)){
            return ResponseEntity.badRequest().body("\"Nie znaleziono zawodów z tym identyfikatorem\"");
        }
        TournamentEntity tournamentEntity = tournamentRepository.findById(tournamentUUID).orElseThrow(EntityNotFoundException::new);

        if (tournamentEntity.isOpen()) {

            OtherPersonEntity otherPersonEntity = otherPersonRepository
                    .findAll()
                    .stream()
                    .filter(f -> f.getId()
                            .equals(id))
                    .findFirst().orElseThrow(EntityNotFoundException::new);

            List<OtherPersonEntity> list = tournamentEntity.getOtherArbitersList();

            list.remove(otherPersonEntity);

            tournamentRepository.saveAndFlush(tournamentEntity);

            return ResponseEntity.ok("\"Usunięto Sędziego\"");
        }
        return ResponseEntity.status(418).body("\"I'm a teapot\"");
    }

    public ResponseEntity<String> addMainArbiter(String tournamentUUID, int legitimationNumber) {
        if (!tournamentRepository.existsById(tournamentUUID)){
            return ResponseEntity.badRequest().body("\"Nie znaleziono zawodów z tym identyfikatorem\"");
        }
        TournamentEntity tournamentEntity = tournamentRepository.findById(tournamentUUID).orElseThrow(EntityNotFoundException::new);
        if (tournamentEntity.isOpen()) {

            if (tournamentEntity.getOtherMainArbiter() != null) {
                tournamentEntity.setOtherMainArbiter(null);
            }
            String function = ArbiterWorkClass.MAIN_ARBITER.getName();

            MemberEntity memberEntity = memberRepository
                    .findAll()
                    .stream()
                    .filter(f -> f.getLegitimationNumber()
                            .equals(legitimationNumber))
                    .findFirst().orElseThrow(EntityNotFoundException::new);

            if (tournamentEntity.getCommissionRTSArbiter() != null) {
                if (tournamentEntity.getCommissionRTSArbiter().equals(memberEntity)) {
                    return ResponseEntity.badRequest().body("\"Sędzia już jest przypisany\"");
                }
            }
            if (tournamentEntity.getArbitersList() != null) {
                if (tournamentEntity.getArbitersList().stream().anyMatch(a -> a.equals(memberEntity))) {
                    return ResponseEntity.badRequest().body("\"Sędzia już jest przypisany\"");
                }
            }

            if (!memberEntity.getMemberPermissions().getArbiterNumber().isEmpty()) {
                if (tournamentEntity.getMainArbiter() == null || tournamentEntity.getMainArbiter() != memberEntity) {
                    if (tournamentEntity.getMainArbiter() == null) {
                        tournamentEntity.setMainArbiter(memberEntity);

                    } else {
                        historyService.removeJudgingRecord(tournamentEntity.getMainArbiter().getUuid(), tournamentUUID);
                        tournamentEntity.setMainArbiter(memberEntity);
                        tournamentEntity.setOtherMainArbiter(null);

                    }
                    tournamentRepository.saveAndFlush(tournamentEntity);
                    LOG.info("Ustawiono sędziego głównego zawodów");
                    historyService.addJudgingRecord(memberEntity.getUuid(), tournamentUUID, function);
                    return ResponseEntity.ok("\"Ustawiono sędziego głównego zawodów\"");
                } else {
                    return ResponseEntity.badRequest().body("\"Nie udało się przypisać sędziego\"");
                }
            }
        }
        return ResponseEntity.badRequest().body("\"Nie udało się przypisać sędziego\"");
    }

    public ResponseEntity<String> addOtherMainArbiter(String tournamentUUID, int id) {
        if (!tournamentRepository.existsById(tournamentUUID)){
            return ResponseEntity.badRequest().body("\"Nie znaleziono zawodów z tym identyfikatorem\"");
        }
        TournamentEntity tournamentEntity = tournamentRepository.findById(tournamentUUID).orElseThrow(EntityNotFoundException::new);
        if (tournamentEntity.isOpen()) {

            OtherPersonEntity otherPersonEntity = otherPersonRepository
                    .findAll()
                    .stream()
                    .filter(f -> f.getId()
                            .equals(id))
                    .findFirst().orElseThrow(EntityNotFoundException::new);

            if (tournamentEntity.getOtherCommissionRTSArbiter() != null) {
                if (tournamentEntity.getOtherCommissionRTSArbiter().equals(otherPersonEntity)) {
                    return ResponseEntity.badRequest().body("\"Sędzia już jest przypisany\"");
                }
            }
            if (tournamentEntity.getOtherArbitersList() != null) {
                if (tournamentEntity.getOtherArbitersList().stream().anyMatch(a -> a.equals(otherPersonEntity))) {
                    return ResponseEntity.badRequest().body("\"Sędzia już jest przypisany\"");
                }
            }

            if (!otherPersonEntity.getPermissionsEntity().getArbiterNumber().isEmpty()) {
                if (tournamentEntity.getOtherMainArbiter() == null || tournamentEntity.getOtherMainArbiter() != otherPersonEntity) {
                    if (tournamentEntity.getOtherMainArbiter() == null) {
                        if (tournamentEntity.getMainArbiter() != null) {

                            historyService.removeJudgingRecord(tournamentEntity.getMainArbiter().getUuid(), tournamentUUID);
                        }
                        tournamentEntity.setOtherMainArbiter(otherPersonEntity);
                        tournamentEntity.setMainArbiter(null);

                    } else {
                        historyService.removeJudgingRecord(tournamentEntity.getMainArbiter().getUuid(), tournamentUUID);
                        tournamentEntity.setMainArbiter(null);
                        tournamentEntity.setOtherMainArbiter(otherPersonEntity);
                    }
                    tournamentRepository.saveAndFlush(tournamentEntity);
                    LOG.info("Ustawiono sędziego głównego zawodów");
                    return ResponseEntity.ok("\"Ustawiono sędziego głównego zawodów\"");
                } else {
                    return ResponseEntity.badRequest().body("\"Nie udało się przypisać sędziego\"");
                }
            }
        }
        return ResponseEntity.badRequest().body("\"Nie udało się przypisać sędziego\"");
    }

    public ResponseEntity<?> addRTSArbiter(String tournamentUUID, int legitimationNumber) {
        if (!tournamentRepository.existsById(tournamentUUID)){
            return ResponseEntity.badRequest().body("\"Nie znaleziono zawodów z tym identyfikatorem\"");
        }
        TournamentEntity tournamentEntity = tournamentRepository.findById(tournamentUUID).orElseThrow(EntityNotFoundException::new);
        if (tournamentEntity.isOpen()) {

            if (tournamentEntity.getOtherCommissionRTSArbiter() != null) {
                tournamentEntity.setOtherCommissionRTSArbiter(null);
            }
            String function = ArbiterWorkClass.RTS_ARBITER.getName();

            MemberEntity memberEntity = memberRepository
                    .findAll()
                    .stream()
                    .filter(f -> f.getLegitimationNumber()
                            .equals(legitimationNumber))
                    .findFirst().orElseThrow(EntityNotFoundException::new);

            if (tournamentEntity.getMainArbiter() != null) {
                if (tournamentEntity.getMainArbiter().equals(memberEntity)) {
                    return ResponseEntity.badRequest().body("\"Sędzia już jest przypisany\"");
                }
            }
            if (tournamentEntity.getArbitersList() != null) {
                if (tournamentEntity.getArbitersList().stream().anyMatch(a -> a.equals(memberEntity))) {
                    return ResponseEntity.badRequest().body("\"Sędzia już jest przypisany\"");
                }
            }

            if (!memberEntity.getMemberPermissions().getArbiterNumber().isEmpty()) {
                if (tournamentEntity.getCommissionRTSArbiter() == null || tournamentEntity.getCommissionRTSArbiter() != memberEntity) {
                    if (tournamentEntity.getCommissionRTSArbiter() != null) {
                        historyService.removeJudgingRecord(tournamentEntity.getCommissionRTSArbiter().getUuid(), tournamentUUID);
                    }
                    tournamentEntity.setCommissionRTSArbiter(memberEntity);
                    tournamentRepository.saveAndFlush(tournamentEntity);
                    LOG.info("Ustawiono sędziego RTS");
                    historyService.addJudgingRecord(memberEntity.getUuid(), tournamentUUID, function);
                    return ResponseEntity.ok("\"Ustawiono sędziego RTS\"");
                } else {
                    return ResponseEntity.badRequest().body("\"Nie udało się przypisać sędziego\"");
                }
            }
        }
        return ResponseEntity.badRequest().body("\"Nie udało się przypisać sędziego\"");
    }

    public ResponseEntity<?> addOtherRTSArbiter(String tournamentUUID, int id) {
        if (!tournamentRepository.existsById(tournamentUUID)){
            return ResponseEntity.badRequest().body("\"Nie znaleziono zawodów z tym identyfikatorem\"");
        }
        TournamentEntity tournamentEntity = tournamentRepository.findById(tournamentUUID).orElseThrow(EntityNotFoundException::new);
        if (tournamentEntity.isOpen()) {

            OtherPersonEntity otherPersonEntity = otherPersonRepository
                    .findAll()
                    .stream()
                    .filter(f -> f.getId()
                            .equals(id))
                    .findFirst().orElseThrow(EntityNotFoundException::new);

            if (tournamentEntity.getOtherMainArbiter() != null) {
                if (tournamentEntity.getOtherMainArbiter().equals(otherPersonEntity)) {
                    return ResponseEntity.badRequest().body("\"Sędzia już jest przypisany\"");
                }
            }
            if (tournamentEntity.getOtherArbitersList() != null) {
                if (tournamentEntity.getOtherArbitersList().stream().anyMatch(a -> a.equals(otherPersonEntity))) {
                    return ResponseEntity.badRequest().body("\"Sędzia już jest przypisany\"");
                }
            }

            if (!otherPersonEntity.getPermissionsEntity().getArbiterNumber().isEmpty()) {
                if (tournamentEntity.getOtherCommissionRTSArbiter() == null || tournamentEntity.getOtherCommissionRTSArbiter() != otherPersonEntity) {
                    if (tournamentEntity.getOtherCommissionRTSArbiter() == null) {
                        if (tournamentEntity.getCommissionRTSArbiter() != null) {
                            historyService.removeJudgingRecord(tournamentEntity.getCommissionRTSArbiter().getUuid(), tournamentUUID);
                        }

                    } else {
                        historyService.removeJudgingRecord(tournamentEntity.getCommissionRTSArbiter().getUuid(), tournamentUUID);
                    }
                    tournamentEntity.setOtherCommissionRTSArbiter(otherPersonEntity);
                    tournamentEntity.setCommissionRTSArbiter(null);
                    tournamentRepository.saveAndFlush(tournamentEntity);
                    LOG.info("Ustawiono sędziego biura obliczeń");
                    return ResponseEntity.ok("\"Ustawiono sędziego RTSń\"");
                } else {
                    return ResponseEntity.badRequest().body("\"Nie udało się przypisać sędziego\"");
                }
            }
        }
        return ResponseEntity.badRequest().body("\"Nie udało się przypisać sędziego\"");
    }

    public ResponseEntity<?> addOthersArbiters(String tournamentUUID, int legitimationNumber) {
        if (!tournamentRepository.existsById(tournamentUUID)){
            return ResponseEntity.badRequest().body("\"Nie znaleziono zawodów z tym identyfikatorem\"");
        }
        TournamentEntity tournamentEntity = tournamentRepository.findById(tournamentUUID).orElseThrow(EntityNotFoundException::new);
        if (tournamentEntity.isOpen()) {
            String function = ArbiterWorkClass.HELP.getName();

            MemberEntity memberEntity = memberRepository
                    .findAll()
                    .stream()
                    .filter(f -> f.getLegitimationNumber()
                            .equals(legitimationNumber))
                    .findFirst().orElseThrow(EntityNotFoundException::new);

            if (tournamentEntity.getMainArbiter() != null) {
                if (tournamentEntity.getMainArbiter().equals(memberEntity)) {
                    return ResponseEntity.badRequest().body("\"Sędzia już jest przypisany\"");
                }
            }
            if (tournamentEntity.getCommissionRTSArbiter() != null) {
                if (tournamentEntity.getCommissionRTSArbiter().equals(memberEntity)) {
                    return ResponseEntity.badRequest().body("\"Sędzia już jest przypisany\"");
                }
            }
            if (tournamentEntity.getArbitersList() != null) {
                if (tournamentEntity.getArbitersList().contains(memberEntity)) {
                    return ResponseEntity.badRequest().body("\"Sędzia już jest przypisany\"");
                }
            }
            if (tournamentEntity.getArbitersRTSList() != null) {
                if (tournamentEntity.getArbitersRTSList().contains(memberEntity)) {
                    return ResponseEntity.badRequest().body("\"Sędzia już jest przypisany\"");
                }
            }
            if (!memberEntity.getMemberPermissions().getArbiterNumber().isEmpty()) {
                List<MemberEntity> list = tournamentEntity.getArbitersList();
                list.add(memberEntity);
                list.sort(Comparator.comparing(MemberEntity::getSecondName));

                tournamentEntity.setArbitersList(list);
                tournamentRepository.saveAndFlush(tournamentEntity);
                LOG.info("Dodano sędziego pomocniczego");
                historyService.addJudgingRecord(memberEntity.getUuid(), tournamentUUID, function);
                return ResponseEntity.ok("\"Ustawiono sędziego pomocniczego\"");
            }
        }
        return ResponseEntity.badRequest().body("\"Nie udało się przypisać sędziego\"");
    }

    public ResponseEntity<?> addPersonOthersArbiters(String tournamentUUID, int id) {
        if (!tournamentRepository.existsById(tournamentUUID)){
            return ResponseEntity.badRequest().body("\"Nie znaleziono zawodów z tym identyfikatorem\"");
        }
        TournamentEntity tournamentEntity = tournamentRepository.findById(tournamentUUID).orElseThrow(EntityNotFoundException::new);
        if (tournamentEntity.isOpen()) {

            OtherPersonEntity otherPersonEntity = otherPersonRepository
                    .findAll()
                    .stream()
                    .filter(f -> f.getId()
                            .equals(id))
                    .findFirst().orElseThrow(EntityNotFoundException::new);

            if (tournamentEntity.getOtherMainArbiter() != null) {
                if (tournamentEntity.getOtherMainArbiter().equals(otherPersonEntity)) {
                    return ResponseEntity.badRequest().body("\"Sędzia już jest przypisany\"");
                }
            }
            if (tournamentEntity.getOtherCommissionRTSArbiter() != null) {
                if (tournamentEntity.getOtherCommissionRTSArbiter().equals(otherPersonEntity)) {
                    return ResponseEntity.badRequest().body("\"Sędzia już jest przypisany\"");
                }
            }
            if (tournamentEntity.getOtherArbitersList() != null) {
                if (tournamentEntity.getOtherArbitersList().contains(otherPersonEntity)) {
                    return ResponseEntity.badRequest().body("\"Sędzia już jest przypisany\"");
                }
            }
            if (tournamentEntity.getOtherArbitersRTSList() != null) {
                if (tournamentEntity.getOtherArbitersRTSList().contains(otherPersonEntity)) {
                    return ResponseEntity.badRequest().body("\"Sędzia już jest przypisany\"");
                }
            }
            if (!otherPersonEntity.getPermissionsEntity().getArbiterNumber().isEmpty()) {
                List<OtherPersonEntity> list = tournamentEntity.getOtherArbitersList();
                list.add(otherPersonEntity);
                list.sort(Comparator.comparing(OtherPersonEntity::getSecondName));

                tournamentEntity.setOtherArbitersList(list);
                tournamentRepository.saveAndFlush(tournamentEntity);
                LOG.info("Dodano sędziego pomocniczego");
                return ResponseEntity.ok("\"Ustawiono sędziego pomocniczego\"");
            }
        }
        return ResponseEntity.badRequest().body("\"Nie udało się przypisać sędziego\"");
    }

    public String addNewCompetitionListToTournament(String tournamentUUID, String competitionUUID) {
        TournamentEntity tournamentEntity = tournamentRepository.findById(tournamentUUID).orElseThrow(EntityNotFoundException::new);
        if (tournamentEntity.isOpen()) {
            if (competitionUUID != null) {
                CompetitionEntity competition = competitionRepository.findById(competitionUUID).orElseThrow(EntityNotFoundException::new);
                if (!tournamentEntity.getCompetitionsList().isEmpty()) {
                    for (int i = 0; i < tournamentEntity.getCompetitionsList().size(); i++) {
                        if (tournamentEntity.getCompetitionsList().get(i).getName().equals(competition.getName())) {
                            LOG.info("konkurencja " + competition.getName() + " jest już dodana");
                            return "konkurencja " + competition.getName() + " jest już dodana";
                        }
                    }
                }
                CompetitionMembersListEntity competitionMembersList = CompetitionMembersListEntity.builder()
                        .name(competition.getName())
                        .attachedToTournament(tournamentEntity.getUuid())
                        .date(tournamentEntity.getDate())
                        .discipline(competition.getDiscipline())
                        .disciplines(competition.getDisciplines())
                        .countingMethod(competition.getCountingMethod())
                        .type(competition.getType())
                        .numberOfShots(competition.getNumberOfShots())
                        .WZSS(tournamentEntity.isWZSS())
                        .ordering(competition.getOrdering())
                        .caliberUUID(competition.getCaliberUUID())
                        .practiceShots(competition.getPracticeShots())
                        .build();
                competitionMembersListRepository.saveAndFlush(competitionMembersList);
                List<CompetitionMembersListEntity> competitionsList = tournamentEntity.getCompetitionsList();
                competitionsList.add(competitionMembersList);
                competitionsList.sort(Comparator.comparing(CompetitionMembersListEntity::getOrdering));
                tournamentRepository.saveAndFlush(tournamentEntity);
                LOG.info("Dodano konkurencję " + competition.getName() + " do zawodów");
                return "Dodano konkurencję " + competition.getName() + " do zawodów";
            }
        }
        return "Nie udało się dodać konkurencji";
    }

    public List<TournamentDTO> getClosedTournaments() {
        List<TournamentEntity> all = tournamentRepository.findAll().stream().filter(f -> !f.isOpen()).collect(Collectors.toList());
        List<TournamentDTO> allDTO = new ArrayList<>();
        all.forEach(e -> allDTO.add(Mapping.map1(e)));
        allDTO.sort(Comparator.comparing(TournamentDTO::getDate).reversed());
        return allDTO;
    }

    public ResponseEntity<?> deleteTournament(String tournamentUUID, String pinCode) {
        if (!tournamentRepository.existsById(tournamentUUID)){
            return ResponseEntity.badRequest().body("\"Nie znaleziono zawodów z tym identyfikatorem\"");
        }
        TournamentEntity tournamentEntity = tournamentRepository.findById(tournamentUUID).orElseThrow(EntityNotFoundException::new);
        if (changeHistoryService.comparePinCode(pinCode)) {
            if (tournamentEntity.isOpen()) {
                if (!tournamentEntity.getCompetitionsList().isEmpty()) {
                    tournamentEntity.getCompetitionsList()
                            .forEach(e -> e.getScoreList()
                                    .stream().filter(f -> f.getMember() != null)
                                    .forEach(a -> historyService.removeCompetitionRecord(a.getMember().getUuid(), competitionMembersListRepository.findById(a.getCompetitionMembersListEntityUUID()).orElseThrow(EntityNotFoundException::new))));
                }
                if (tournamentEntity.getMainArbiter() != null) {
                    historyService.removeJudgingRecord(tournamentEntity.getMainArbiter().getUuid(), tournamentEntity.getUuid());
                }
                if (tournamentEntity.getCommissionRTSArbiter() != null) {
                    historyService.removeJudgingRecord(tournamentEntity.getCommissionRTSArbiter().getUuid(), tournamentEntity.getUuid());
                }
                if (!tournamentEntity.getArbitersList().isEmpty()) {
                    tournamentEntity.getArbitersList().forEach(e -> historyService.removeJudgingRecord(e.getUuid(), tournamentEntity.getUuid()));
                }
                tournamentRepository.delete(tournamentEntity);
                return ResponseEntity.ok("\"Zawody zostały usunięte - nie da się już ich przywrócić\"");
            } else {
                return ResponseEntity.badRequest().body("\"Coś poszło nie tak i nie udało się usunąć zawodów\"");
            }
        } else {
            return ResponseEntity.status(403).body("\"Wprowadzono zły kod - Spróbuj ponownie\"");
        }
    }

    public List<String> getCompetitionsListInTournament(String tournamentUUID) {
        TournamentEntity tournamentEntity = tournamentRepository.findById(tournamentUUID).orElseThrow(EntityNotFoundException::new);
        List<String> list = new ArrayList<>();

        List<CompetitionMembersListEntity> competitionsList = tournamentEntity.getCompetitionsList();
        competitionsList.sort(Comparator.comparing(CompetitionMembersListEntity::getOrdering));
        competitionsList.forEach(e ->
        {
            if (e.getOrdering() == null) {
                System.out.println(e.getName());
                CompetitionEntity competitionEntity = competitionRepository.findAll().stream().filter(f -> f.getName().equals(e.getName())).findFirst().orElseThrow(EntityNotFoundException::new);
                e.setOrdering(competitionEntity.getOrdering());
                competitionMembersListRepository.saveAndFlush(e);
            }


            list.add(e.getName());
        });

        return list;
    }

    public ResponseEntity<?> addOthersRTSArbiters(String tournamentUUID, int legitimationNumber) {
        if (!tournamentRepository.existsById(tournamentUUID)){
            return ResponseEntity.badRequest().body("\"Nie znaleziono zawodów z tym identyfikatorem\"");
        }
        TournamentEntity tournamentEntity = tournamentRepository.findById(tournamentUUID).orElseThrow(EntityNotFoundException::new);
        if (tournamentEntity.isOpen()) {
            String function = ArbiterWorkClass.RTS_HELP.getName();

            MemberEntity memberEntity = memberRepository
                    .findAll()
                    .stream()
                    .filter(f -> f.getLegitimationNumber()
                            .equals(legitimationNumber))
                    .findFirst().orElseThrow(EntityNotFoundException::new);

            if (tournamentEntity.getMainArbiter() != null) {
                if (tournamentEntity.getMainArbiter().equals(memberEntity)) {
                    return ResponseEntity.badRequest().body("\"Sędzia już jest przypisany\"");
                }
            }
            if (tournamentEntity.getCommissionRTSArbiter() != null) {
                if (tournamentEntity.getCommissionRTSArbiter().equals(memberEntity)) {
                    return ResponseEntity.badRequest().body("\"Sędzia już jest przypisany\"");
                }
            }
            if (tournamentEntity.getArbitersList() != null) {
                if (tournamentEntity.getArbitersList().contains(memberEntity)) {
                    return ResponseEntity.badRequest().body("\"Sędzia już jest przypisany\"");
                }
            }
            if (tournamentEntity.getArbitersRTSList() != null) {
                if (tournamentEntity.getArbitersRTSList().contains(memberEntity)) {
                    return ResponseEntity.badRequest().body("\"Sędzia już jest przypisany\"");
                }
            }
            if (!memberEntity.getMemberPermissions().getArbiterNumber().isEmpty()) {
                List<MemberEntity> list = tournamentEntity.getArbitersRTSList();
                list.add(memberEntity);
                list.sort(Comparator.comparing(MemberEntity::getSecondName));

                tournamentEntity.setArbitersRTSList(list);
                tournamentRepository.saveAndFlush(tournamentEntity);
                LOG.info("Dodano sędziego biura obliczeń");
                historyService.addJudgingRecord(memberEntity.getUuid(), tournamentUUID, function);
                return ResponseEntity.ok("\"Ustawiono sędziego biura obliczeń\"");
            }
        }
        return ResponseEntity.badRequest().body("\"Nie udało się przypisać sędziego\"");
    }

    public ResponseEntity<?> addPersonOthersRTSArbiters(String tournamentUUID, int id) {
        if (!tournamentRepository.existsById(tournamentUUID)){
            return ResponseEntity.badRequest().body("\"Nie znaleziono zawodów z tym identyfikatorem\"");
        }
        TournamentEntity tournamentEntity = tournamentRepository.findById(tournamentUUID).orElseThrow(EntityNotFoundException::new);
        if (tournamentEntity.isOpen()) {

            OtherPersonEntity otherPersonEntity = otherPersonRepository
                    .findAll()
                    .stream()
                    .filter(f -> f.getId()
                            .equals(id))
                    .findFirst().orElseThrow(EntityNotFoundException::new);

            if (tournamentEntity.getOtherMainArbiter() != null) {
                if (tournamentEntity.getOtherMainArbiter().equals(otherPersonEntity)) {
                    return ResponseEntity.badRequest().body("\"Sędzia już jest przypisany\"");
                }
            }
            if (tournamentEntity.getOtherCommissionRTSArbiter() != null) {
                if (tournamentEntity.getOtherCommissionRTSArbiter().equals(otherPersonEntity)) {
                    return ResponseEntity.badRequest().body("\"Sędzia już jest przypisany\"");
                }
            }
            if (tournamentEntity.getOtherArbitersList() != null) {
                if (tournamentEntity.getOtherArbitersList().contains(otherPersonEntity)) {
                    return ResponseEntity.badRequest().body("\"Sędzia już jest przypisany\"");
                }
            }
            if (tournamentEntity.getOtherArbitersRTSList() != null) {
                if (tournamentEntity.getOtherArbitersRTSList().contains(otherPersonEntity)) {
                    return ResponseEntity.badRequest().body("\"Sędzia już jest przypisany\"");
                }
            }
            if (!otherPersonEntity.getPermissionsEntity().getArbiterNumber().isEmpty()) {
                List<OtherPersonEntity> list = tournamentEntity.getOtherArbitersRTSList();
                list.add(otherPersonEntity);
                list.sort(Comparator.comparing(OtherPersonEntity::getSecondName));

                tournamentEntity.setOtherArbitersRTSList(list);
                tournamentRepository.saveAndFlush(tournamentEntity);
                LOG.info("Dodano sędziego biura obliczeń");
                return ResponseEntity.ok("\"Ustawiono sędziego biura obliczeń\"");
            }
        }
        return ResponseEntity.badRequest().body("\"Nie udało się przypisać sędziego\"");
    }

    public ResponseEntity<?> removeRTSArbiterFromTournament(String tournamentUUID, int legitimationNumber) {
        if (!tournamentRepository.existsById(tournamentUUID)){
            return ResponseEntity.badRequest().body("\"Nie znaleziono zawodów z tym identyfikatorem\"");
        }
        TournamentEntity tournamentEntity = tournamentRepository.findById(tournamentUUID).orElseThrow(EntityNotFoundException::new);
        if (tournamentEntity.isOpen()) {

            MemberEntity memberEntity = memberRepository
                    .findAll()
                    .stream()
                    .filter(f -> f.getLegitimationNumber()
                            .equals(legitimationNumber))
                    .findFirst().orElseThrow(EntityNotFoundException::new);

            List<MemberEntity> list = tournamentEntity.getArbitersRTSList();

            historyService.removeJudgingRecord(memberEntity.getUuid(), tournamentUUID);
            list.remove(memberEntity);

            tournamentRepository.saveAndFlush(tournamentEntity);

            return ResponseEntity.ok("\"Usunięto sędziego\"");
        }
        return ResponseEntity.badRequest().body("\"Nie udało się usunąć sędziego\"");
    }

    public ResponseEntity<?> removeRTSOtherArbiterFromTournament(String tournamentUUID, int id) {
        if (!tournamentRepository.existsById(tournamentUUID)){
            return ResponseEntity.badRequest().body("\"Nie znaleziono zawodów z tym identyfikatorem\"");
        }
        TournamentEntity tournamentEntity = tournamentRepository.findById(tournamentUUID).orElseThrow(EntityNotFoundException::new);
        if (tournamentEntity.isOpen()) {

            OtherPersonEntity otherPersonEntity = otherPersonRepository
                    .findAll()
                    .stream()
                    .filter(f -> f.getId()
                            .equals(id))
                    .findFirst().orElseThrow(EntityNotFoundException::new);

            List<OtherPersonEntity> list = tournamentEntity.getOtherArbitersRTSList();

            list.remove(otherPersonEntity);

            tournamentRepository.saveAndFlush(tournamentEntity);

            return ResponseEntity.ok("\"Usunięto sędziego\"");
        }
        return ResponseEntity.badRequest().body("\"Nie udało się usunąć sędziego\"");
    }

    public List<String> getStatistics(String tournamentUUID) {

        TournamentEntity tournamentEntity = tournamentRepository.findById(tournamentUUID).orElseThrow(EntityNotFoundException::new);
        List<String> list = new ArrayList<>();

//        List<Integer> list1 = new ArrayList<>();
        List<String> list1 = new ArrayList<>();
        List<Integer> list2 = new ArrayList<>();
        tournamentEntity.getCompetitionsList().forEach(e -> e.getScoreList().forEach(g -> {
            if (g.getMember() != null) {
                if (!list1.contains(g.getMember().getUuid())) {
                    list1.add(g.getMember().getUuid());
                }
            }
            if (g.getOtherPersonEntity() != null) {
                if (!list1.contains(String.valueOf(g.getOtherPersonEntity().getId()))) {
                    list1.add(String.valueOf(g.getOtherPersonEntity().getId()));
                }
            }
        }));
        tournamentEntity.getCompetitionsList().forEach(e -> e.getScoreList().forEach(g -> list2.add(g.getMetricNumber())));
//        int i = list2.stream().mapToInt(v->v).max().orElseThrow(NoSuchElementException::new);


        List<ScoreEntity> list3 = new ArrayList<>();
        tournamentEntity.getCompetitionsList().forEach(e -> e.getScoreList().stream().filter(ScoreEntity::isAmmunition).forEach(list3::add));

        List<ScoreEntity> list4 = new ArrayList<>();
        tournamentEntity.getCompetitionsList().forEach(e -> e.getScoreList().stream().filter(ScoreEntity::isGun).forEach(list4::add));


        list.add(String.valueOf(tournamentEntity.getCompetitionsList().size()));
        list.add(String.valueOf(list1.size()));
        list.add(String.valueOf(list2.size()));
        list.add(String.valueOf(list3.size()));
        list.add(String.valueOf(list4.size()));
        return list;
    }

    public ResponseEntity<?> openTournament(String tournamentUUID, String pinCode) {
        if (tournamentRepository.findAll().stream().anyMatch(TournamentEntity::isOpen)) {
            System.out.println("coś");
            return ResponseEntity.badRequest().body("\"Nie można otworzyć zawodów gdy inne są otwarte\"");
        } else {
            TournamentEntity tournamentEntity = tournamentRepository.findById(tournamentUUID).orElseThrow(EntityNotFoundException::new);

            LOG.info("Zawody " + tournamentEntity.getName() + " zostały otwarte");
            tournamentEntity.setOpen(true);
            tournamentRepository.saveAndFlush(tournamentEntity);
            changeHistoryService.addRecordToChangeHistory(pinCode, tournamentEntity.getClass().getSimpleName() + " openTournament", tournamentUUID);
            return ResponseEntity.ok("\"Otwarto zawody z dnia " + tournamentEntity.getDate() + "\"");

        }
    }

    public Boolean checkAnyOpenTournament() {
        return tournamentRepository.findAll().stream().anyMatch(TournamentEntity::isOpen);
    }

    public List<UsedHistoryEntity> getListOfGunsOnTournament(String tournamentUUID) {

        return usedHistoryRepository.findAll().stream().filter(f -> f.getEvidenceUUID().equals(tournamentUUID)).collect(Collectors.toList());
    }

}
