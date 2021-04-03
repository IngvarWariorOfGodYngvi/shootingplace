package com.shootingplace.shootingplace.services;

import com.shootingplace.shootingplace.domain.entities.*;
import com.shootingplace.shootingplace.domain.enums.ArbiterWorkClass;
import com.shootingplace.shootingplace.domain.models.MemberDTO;
import com.shootingplace.shootingplace.domain.models.Tournament;
import com.shootingplace.shootingplace.domain.models.TournamentDTO;
import com.shootingplace.shootingplace.repositories.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.*;
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
    private final Logger LOG = LogManager.getLogger(getClass());


    public TournamentService(TournamentRepository tournamentRepository, MemberRepository memberRepository, OtherPersonRepository otherPersonRepository, CompetitionMembersListRepository competitionMembersListRepository, CompetitionRepository competitionRepository, HistoryService historyService, ChangeHistoryService changeHistoryService) {
        this.tournamentRepository = tournamentRepository;
        this.memberRepository = memberRepository;
        this.otherPersonRepository = otherPersonRepository;
        this.competitionMembersListRepository = competitionMembersListRepository;
        this.competitionRepository = competitionRepository;
        this.historyService = historyService;
        this.changeHistoryService = changeHistoryService;
    }

    public String createNewTournament(Tournament tournament) {
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

        return tournamentEntity.getUuid();
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


    public List<Tournament> getListOfTournaments() {
        LOG.info("Wyświetlono listę zawodów");

        List<Tournament> list = new ArrayList<>();

        List<TournamentEntity> collect = tournamentRepository
                .findAll()
                .stream()
                .filter(TournamentEntity::isOpen)
                .collect(Collectors.toList());
        for (TournamentEntity tournamentEntity : collect) {
            MemberDTO mainArbiterDTO;
            MemberDTO commissionRTSArbiter;
            if (tournamentEntity.getMainArbiter() != null) {
                mainArbiterDTO = Mapping.map2(tournamentEntity.getMainArbiter());
            } else {
                mainArbiterDTO = null;
            }
            if (tournamentEntity.getCommissionRTSArbiter() != null) {
                commissionRTSArbiter = Mapping.map2(tournamentEntity.getCommissionRTSArbiter());
            } else {
                commissionRTSArbiter = null;
            }

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

                    .arbitersList(tournamentEntity.getArbitersList().stream().map(Mapping::map2).collect(Collectors.toList()))

                    .otherArbitersList(tournamentEntity.getOtherArbitersList())

                    .arbitersRTSList(tournamentEntity.getArbitersRTSList().stream().map(Mapping::map2).collect(Collectors.toList()))

                    .otherArbitersRTSList(tournamentEntity.getOtherArbitersRTSList())

                    .competitionsList(tournamentEntity.getCompetitionsList().stream().map(Mapping::map).collect(Collectors.toList()))

                    .build();

            list.add(tournament);
        }
        return list;
    }

    public boolean closeTournament(String tournamentUUID) {
        TournamentEntity tournamentEntity = tournamentRepository.findById(tournamentUUID).orElseThrow(EntityNotFoundException::new);
        if (tournamentEntity.isOpen()) {
            LOG.info("Zawody " + tournamentEntity.getName() + " zostały zamknięte");
            tournamentEntity.setOpen(false);
            tournamentRepository.saveAndFlush(tournamentEntity);
            return true;
        } else {
            return false;
        }
    }

    public boolean removeArbiterFromTournament(String tournamentUUID, int legitimationNumber) {
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

            return true;
        }
        return false;
    }

    public boolean removeOtherArbiterFromTournament(String tournamentUUID, int id) {
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

            return true;
        }
        return false;
    }

    public boolean addMainArbiter(String tournamentUUID, int legitimationNumber) {
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
                    return false;
                }
            }
            if (tournamentEntity.getArbitersList() != null) {
                if (tournamentEntity.getArbitersList().stream().anyMatch(a -> a.equals(memberEntity))) {
                    return false;
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
                } else {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public boolean addOtherMainArbiter(String tournamentUUID, int id) {
        TournamentEntity tournamentEntity = tournamentRepository.findById(tournamentUUID).orElseThrow(EntityNotFoundException::new);
        if (tournamentEntity.isOpen()) {
            String function = ArbiterWorkClass.MAIN_ARBITER.getName();

            OtherPersonEntity otherPersonEntity = otherPersonRepository
                    .findAll()
                    .stream()
                    .filter(f -> f.getId()
                            .equals(id))
                    .findFirst().orElseThrow(EntityNotFoundException::new);

            if (tournamentEntity.getOtherCommissionRTSArbiter() != null) {
                if (tournamentEntity.getOtherCommissionRTSArbiter().equals(otherPersonEntity)) {
                    return false;
                }
            }
            if (tournamentEntity.getOtherArbitersList() != null) {
                if (tournamentEntity.getOtherArbitersList().stream().anyMatch(a -> a.equals(otherPersonEntity))) {
                    return false;
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
                } else {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public boolean addRTSArbiter(String tournamentUUID, int legitimationNumber) {
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
                    return false;
                }
            }
            if (tournamentEntity.getArbitersList() != null) {
                if (tournamentEntity.getArbitersList().stream().anyMatch(a -> a.equals(memberEntity))) {
                    return false;
                }
            }

            if (!memberEntity.getMemberPermissions().getArbiterNumber().isEmpty()) {
                if (tournamentEntity.getCommissionRTSArbiter() == null || tournamentEntity.getCommissionRTSArbiter() != memberEntity) {
                    if (tournamentEntity.getCommissionRTSArbiter() != null) {
                        historyService.removeJudgingRecord(tournamentEntity.getCommissionRTSArbiter().getUuid(), tournamentUUID);
                    }
                    tournamentEntity.setCommissionRTSArbiter(memberEntity);
                    tournamentRepository.saveAndFlush(tournamentEntity);
                    LOG.info("Ustawiono sędziego biura obliczeń");
                    historyService.addJudgingRecord(memberEntity.getUuid(), tournamentUUID, function);
                } else {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public boolean addOtherRTSArbiter(String tournamentUUID, int id) {
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
                    return false;
                }
            }
            if (tournamentEntity.getOtherArbitersList() != null) {
                if (tournamentEntity.getOtherArbitersList().stream().anyMatch(a -> a.equals(otherPersonEntity))) {
                    return false;
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
                } else {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public boolean addOthersArbiters(String tournamentUUID, int legitimationNumber) {
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
                    return false;
                }
            }
            if (tournamentEntity.getCommissionRTSArbiter() != null) {
                if (tournamentEntity.getCommissionRTSArbiter().equals(memberEntity)) {
                    return false;
                }
            }
            if (tournamentEntity.getArbitersList() != null) {
                if (tournamentEntity.getArbitersList().contains(memberEntity)) {
                    return false;
                }
            }
            if (tournamentEntity.getArbitersRTSList() != null) {
                if (tournamentEntity.getArbitersRTSList().contains(memberEntity)) {
                    return false;
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
            }
            return true;
        }
        return false;
    }

    public boolean addPersonOthersArbiters(String tournamentUUID, int id) {
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
                    return false;
                }
            }
            if (tournamentEntity.getOtherCommissionRTSArbiter() != null) {
                if (tournamentEntity.getOtherCommissionRTSArbiter().equals(otherPersonEntity)) {
                    return false;
                }
            }
            if (tournamentEntity.getOtherArbitersList() != null) {
                if (tournamentEntity.getOtherArbitersList().contains(otherPersonEntity)) {
                    return false;
                }
            }
            if (tournamentEntity.getOtherArbitersRTSList() != null) {
                if (tournamentEntity.getOtherArbitersRTSList().contains(otherPersonEntity)) {
                    return false;
                }
            }
            if (!otherPersonEntity.getPermissionsEntity().getArbiterNumber().isEmpty()) {
                List<OtherPersonEntity> list = tournamentEntity.getOtherArbitersList();
                list.add(otherPersonEntity);
                list.sort(Comparator.comparing(OtherPersonEntity::getSecondName));

                tournamentEntity.setOtherArbitersList(list);
                tournamentRepository.saveAndFlush(tournamentEntity);
                LOG.info("Dodano sędziego pomocniczego");
            }
            return true;
        }
        return false;
    }

    public boolean addNewCompetitionListToTournament(String tournamentUUID, String competitionUUID) {
        TournamentEntity tournamentEntity = tournamentRepository.findById(tournamentUUID).orElseThrow(EntityNotFoundException::new);
        if (tournamentEntity.isOpen()) {
            if (competitionUUID != null) {
                CompetitionEntity competition = competitionRepository.findById(competitionUUID).orElseThrow(EntityNotFoundException::new);
                if (!tournamentEntity.getCompetitionsList().isEmpty()) {
                    for (int i = 0; i < tournamentEntity.getCompetitionsList().size(); i++) {
                        if (tournamentEntity.getCompetitionsList().get(i).getName().equals(competition.getName())) {
                            LOG.info("Nie można dodać konkurencji bo taka już istnieje w zawodach");
                            return false;
                        }
                    }
                }
                CompetitionMembersListEntity competitionMembersList = CompetitionMembersListEntity.builder()
                        .name(competition.getName())
                        .attachedToTournament(tournamentEntity.getUuid())
                        .date(tournamentEntity.getDate())
                        .discipline(competition.getDiscipline())
                        .build();
                competitionMembersListRepository.saveAndFlush(competitionMembersList);
                List<CompetitionMembersListEntity> competitionsList = tournamentEntity.getCompetitionsList();
                competitionsList.add(competitionMembersList);
                tournamentRepository.saveAndFlush(tournamentEntity);
                LOG.info("Dodano konkurencję do zawodów");

            }
            return true;
        }
        return false;
    }

    public List<TournamentDTO> getClosedTournaments() {
        List<TournamentEntity> all = tournamentRepository.findAll().stream().filter(f -> !f.isOpen()).collect(Collectors.toList());
        List<TournamentDTO> allDTO = new ArrayList<>();
        all.forEach(e -> allDTO.add(Mapping.map1(e)));
        allDTO.sort(Comparator.comparing(TournamentDTO::getDate).reversed());
        return allDTO;
    }

    public boolean deleteTournament(String tournamentUUID) {
        TournamentEntity tournamentEntity = tournamentRepository.findById(tournamentUUID).orElseThrow(EntityNotFoundException::new);
        if (tournamentEntity.isOpen()) {
            if (!tournamentEntity.getCompetitionsList().isEmpty()) {
                tournamentEntity.getCompetitionsList().forEach(e -> e.getScoreList()
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
            return true;
        } else {
            return false;
        }
    }

    public List<String> getCompetitionsListInTournament(String tournamentUUID) {
        TournamentEntity tournamentEntity = tournamentRepository.findById(tournamentUUID).orElseThrow(EntityNotFoundException::new);
        List<String> list = new ArrayList<>();

        tournamentEntity.getCompetitionsList().forEach(e -> list.add(e.getName()));

        return list;
    }

    public boolean addOthersRTSArbiters(String tournamentUUID, int legitimationNumber) {
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
                    return false;
                }
            }
            if (tournamentEntity.getCommissionRTSArbiter() != null) {
                if (tournamentEntity.getCommissionRTSArbiter().equals(memberEntity)) {
                    return false;
                }
            }
            if (tournamentEntity.getArbitersList() != null) {
                if (tournamentEntity.getArbitersList().contains(memberEntity)) {
                    return false;
                }
            }
            if (tournamentEntity.getArbitersRTSList() != null) {
                if (tournamentEntity.getArbitersRTSList().contains(memberEntity)) {
                    return false;
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
            }
            return true;
        }
        return false;
    }

    public boolean addPersonOthersRTSArbiters(String tournamentUUID, int id) {
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
                    return false;
                }
            }
            if (tournamentEntity.getOtherCommissionRTSArbiter() != null) {
                if (tournamentEntity.getOtherCommissionRTSArbiter().equals(otherPersonEntity)) {
                    return false;
                }
            }
            if (tournamentEntity.getOtherArbitersList() != null) {
                if (tournamentEntity.getOtherArbitersList().contains(otherPersonEntity)) {
                    return false;
                }
            }
            if (tournamentEntity.getOtherArbitersRTSList() != null) {
                if (tournamentEntity.getOtherArbitersRTSList().contains(otherPersonEntity)) {
                    return false;
                }
            }
            if (!otherPersonEntity.getPermissionsEntity().getArbiterNumber().isEmpty()) {
                List<OtherPersonEntity> list = tournamentEntity.getOtherArbitersRTSList();
                list.add(otherPersonEntity);
                list.sort(Comparator.comparing(OtherPersonEntity::getSecondName));

                tournamentEntity.setOtherArbitersRTSList(list);
                tournamentRepository.saveAndFlush(tournamentEntity);
                LOG.info("Dodano sędziego biura obliczeń");
            }
            return true;
        }
        return false;
    }

    public boolean removeRTSArbiterFromTournament(String tournamentUUID, int legitimationNumber) {
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

            return true;
        }
        return false;
    }

    public boolean removeRTSOtherArbiterFromTournament(String tournamentUUID, int id) {
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

            return true;
        }
        return false;
    }

    public List<String> getStatistics(String tournamentUUID) {

        TournamentEntity tournamentEntity = tournamentRepository.findById(tournamentUUID).orElseThrow(EntityNotFoundException::new);
        List<String> list = new ArrayList<>();

        List<Integer> list1 = new ArrayList<>();
        tournamentEntity.getCompetitionsList().forEach(e -> e.getScoreList().forEach(g -> list1.add(g.getMetricNumber())));
        int i = list1.stream().mapToInt(v -> v).max().orElseThrow(NoSuchElementException::new);


        List<ScoreEntity> list2 = new ArrayList<>();
        tournamentEntity.getCompetitionsList().forEach(e -> e.getScoreList().stream().filter(ScoreEntity::isAmmunition).forEach(list2::add));

        List<ScoreEntity> list3 = new ArrayList<>();
        tournamentEntity.getCompetitionsList().forEach(e -> e.getScoreList().stream().filter(ScoreEntity::isGun).forEach(list3::add));


        list.add(String.valueOf(tournamentEntity.getCompetitionsList().size()));
        list.add(String.valueOf(i));
        list.add(String.valueOf(list1.size()));
        list.add(String.valueOf(list2.size()));
        list.add(String.valueOf(list3.size()));
        return list;
    }

    public boolean openTournament(String tournamentUUID, String pinCode) {
        if (tournamentRepository.findAll().stream().anyMatch(TournamentEntity::isOpen)) {
            return false;
        } else {
            TournamentEntity tournamentEntity = tournamentRepository.findById(tournamentUUID).orElseThrow(EntityNotFoundException::new);

            LOG.info("Zawody " + tournamentEntity.getName() + " zostały otwarte");
            tournamentEntity.setOpen(true);
            tournamentRepository.saveAndFlush(tournamentEntity);
            changeHistoryService.addRecordToChangeHistory(pinCode, tournamentEntity.getClass().getSimpleName() + " openTournament", tournamentUUID);
            return true;

        }
    }
}
