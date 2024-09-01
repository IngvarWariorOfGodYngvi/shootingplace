package com.shootingplace.shootingplace.history;

import com.shootingplace.shootingplace.contributions.ContributionEntity;
import com.shootingplace.shootingplace.contributions.ContributionRepository;
import com.shootingplace.shootingplace.enums.Discipline;
import com.shootingplace.shootingplace.exceptions.NoUserPermissionException;
import com.shootingplace.shootingplace.license.LicenseEntity;
import com.shootingplace.shootingplace.license.LicenseRepository;
import com.shootingplace.shootingplace.member.MemberEntity;
import com.shootingplace.shootingplace.member.MemberRepository;
import com.shootingplace.shootingplace.shootingPatent.ShootingPatent;
import com.shootingplace.shootingplace.shootingPatent.ShootingPatentEntity;
import com.shootingplace.shootingplace.tournament.CompetitionMembersListEntity;
import com.shootingplace.shootingplace.tournament.TournamentEntity;
import com.shootingplace.shootingplace.tournament.TournamentRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class HistoryService {

    private final HistoryRepository historyRepository;
    private final MemberRepository memberRepository;
    private final LicenseRepository licenseRepository;
    private final CompetitionHistoryRepository competitionHistoryRepository;
    private final TournamentRepository tournamentRepository;
    private final JudgingHistoryRepository judgingHistoryRepository;
    private final ContributionRepository contributionRepository;
    private final LicensePaymentHistoryRepository licensePaymentHistoryRepository;
    private final ChangeHistoryService changeHistoryService;
    private final Logger LOG = LogManager.getLogger(getClass());


    public HistoryService(HistoryRepository historyRepository, MemberRepository memberRepository, LicenseRepository licenseRepository, CompetitionHistoryRepository competitionHistoryRepository, TournamentRepository tournamentRepository, JudgingHistoryRepository judgingHistoryRepository, ContributionRepository contributionRepository, LicensePaymentHistoryRepository licensePaymentHistoryRepository, ChangeHistoryService changeHistoryService) {
        this.historyRepository = historyRepository;
        this.memberRepository = memberRepository;
        this.licenseRepository = licenseRepository;
        this.competitionHistoryRepository = competitionHistoryRepository;
        this.tournamentRepository = tournamentRepository;
        this.judgingHistoryRepository = judgingHistoryRepository;
        this.contributionRepository = contributionRepository;
        this.licensePaymentHistoryRepository = licensePaymentHistoryRepository;
        this.changeHistoryService = changeHistoryService;
    }

    //  Basic
    public History getHistory() {
        return History.builder()
                .licenseHistory(new String[3])
                .patentDay(new LocalDate[3])
                .licensePaymentHistory(new ArrayList<>())
                .contributionList(new ArrayList<>())
                .judgingHistory(new ArrayList<>())
                .competitionHistory(new ArrayList<>())
                .pistolCounter(0)
                .rifleCounter(0)
                .shotgunCounter(0)
                .patentFirstRecord(false)
                .build();

    }

    // Contribution
    public void addContribution(String memberUUID, ContributionEntity contribution) {
        HistoryEntity historyEntity = memberRepository
                .getOne(memberUUID)
                .getHistory();
        contribution.setHistoryUUID(historyEntity.getUuid());
        List<ContributionEntity> contributionList = historyEntity
                .getContributionList();
        if (contributionList != null) {
            contributionList
                    .sort(Comparator.comparing(ContributionEntity::getPaymentDay).thenComparing(ContributionEntity::getValidThru));
        } else {
            contributionList = new ArrayList<>();
        }
        contributionList.add(contribution);
        contributionList
                .sort(Comparator.comparing(ContributionEntity::getPaymentDay).thenComparing(ContributionEntity::getValidThru).reversed());
        historyEntity.setContributionsList(contributionList);

        LOG.info("Dodano rekord w historii składek");
        historyRepository.save(historyEntity);
    }

    public void removeContribution(String memberUUID, ContributionEntity contribution) {
        HistoryEntity historyEntity = memberRepository
                .getOne(memberUUID)
                .getHistory();
        historyEntity
                .getContributionList()
                .remove(contribution);
        contribution.setHistoryUUID(null);
        contributionRepository.save(contribution);
        LOG.info("Usunięto składkę");
        historyRepository.save(historyEntity);
    }

    // license
    public void addLicenseHistoryRecord(String memberUUID, int index) {
        HistoryEntity historyEntity = memberRepository
                .getOne(memberUUID)
                .getHistory();

        String[] licenseTab = historyEntity.getLicenseHistory().clone();
        if (licenseTab == null) {
            licenseTab = new String[3];
        } else {
            if (index == 0) {
                licenseTab[0] = "Pistolet";
            }
            if (index == 1) {
                licenseTab[1] = "Karabin";
            }
            if (index == 2) {
                licenseTab[2] = "Strzelba";
            }
        }
        historyEntity.setLicenseHistory(licenseTab);
        historyRepository.save(historyEntity);
    }

    void addDateToPatentPermissions(String memberUUID, LocalDate date, int index) {
        MemberEntity memberEntity = memberRepository.getOne(memberUUID);
        HistoryEntity historyEntity = memberEntity.getHistory();
        LocalDate[] dateTab = historyEntity.getPatentDay().clone();
        if (index == 0) {
            if (memberEntity.getShootingPatent().getDateOfPosting() != null) {
                if (!memberEntity.getHistory().getPatentFirstRecord()) {
                    dateTab[0] = memberEntity.getShootingPatent().getDateOfPosting();
                    LOG.info("Pobrano datę patentu dla Pistoletu");
                }
                if (memberEntity.getHistory().getPatentFirstRecord() && historyEntity.getPatentDay()[0] == null) {
                    dateTab[0] = date;
                    LOG.info("Ustawiono datę patentu Karabinu na domyślną");
                }
            }
        }
        if (index == 1) {
            if (memberEntity.getShootingPatent().getDateOfPosting() != null) {
                if (!memberEntity.getHistory().getPatentFirstRecord()) {
                    dateTab[1] = memberEntity.getShootingPatent().getDateOfPosting();
                    LOG.info("Pobrano datę patentu dla Karabinu");
                }
                if (memberEntity.getHistory().getPatentFirstRecord() && historyEntity.getPatentDay()[1] == null) {
                    dateTab[1] = date;
                    LOG.info("Ustawiono datę patentu Karabinu na domyślną");
                }
            }
        }
        if (index == 2) {
            if (memberEntity.getShootingPatent().getDateOfPosting() != null) {
                if (!memberEntity.getHistory().getPatentFirstRecord()) {
                    dateTab[2] = memberEntity.getShootingPatent().getDateOfPosting();
                    LOG.info("Pobrano datę patentu dla Strzelby");
                }
                if (memberEntity.getHistory().getPatentFirstRecord() && historyEntity.getPatentDay()[2] == null) {
                    dateTab[2] = date;
                    LOG.info("Ustawiono datę patentu Strzelby na domyślną");
                }
            }
        }
        if (!historyEntity.getPatentFirstRecord()) {
            LOG.info("Już wpisano datę pierwszego nadania patentu");
        }
        historyEntity.setPatentDay(dateTab);
        historyRepository.save(historyEntity);

    }

    public ResponseEntity<?> addLicenseHistoryPayment(String memberUUID, String pinCode) throws NoUserPermissionException {

        if (!memberRepository.existsById(memberUUID)) {
            return ResponseEntity.badRequest().body("Nie znaleziono Klubowicza");
        }
        MemberEntity memberEntity = memberRepository.getOne(memberUUID);
        LicenseEntity licenseEntity = memberEntity.getLicense();

        HistoryEntity historyEntity = memberEntity.getHistory();
        if (!licenseEntity.isPaid()) {
            if (historyEntity.getLicensePaymentHistory() == null) {
                historyEntity.setLicensePaymentHistory(new ArrayList<>());
            }
            int dateYear = memberEntity.getLicense().getValidThru() != null ? memberEntity.getLicense().getValidThru().getYear() + 1 : LocalDate.now().getYear();
            List<LicensePaymentHistoryEntity> licensePaymentHistory = historyEntity.getLicensePaymentHistory();
            LicensePaymentHistoryEntity build = LicensePaymentHistoryEntity.builder()
                    .date(LocalDate.now())
                    .validForYear(dateYear)
                    .memberUUID(memberUUID)
                    .isPayInPZSSPortal(false)
                    .isNew(licenseEntity.getNumber() == null)
                    .build();
            licensePaymentHistoryRepository.save(build);
            licensePaymentHistory.add(build);

            LOG.info("Dodano wpis o nowej płatności za licencję " + LocalDate.now());
            historyRepository.save(historyEntity);

        } else {
            return ResponseEntity.badRequest().body("Licencja na ten moment jest opłacona");
        }

        ResponseEntity<?> response = getStringResponseEntity(pinCode, memberEntity, HttpStatus.OK, "addLicenseHistoryPayment", "Dodano płatność za Licencję");
        if (response.getStatusCode().equals(HttpStatus.OK)) {
            licenseEntity.setPaid(true);
            licenseRepository.save(licenseEntity);
        }
        return response;


    }

    public ResponseEntity<?> toggleLicencePaymentInPZSS(String paymentUUID, boolean condition, String pinCode) {

        if (!licensePaymentHistoryRepository.existsById(paymentUUID)) {
            return ResponseEntity.badRequest().body("Nie znaleziono płatności");
        }

        LicensePaymentHistoryEntity licensePaymentHistoryEntity = licensePaymentHistoryRepository.findById(paymentUUID).orElseThrow(EntityNotFoundException::new);
        licensePaymentHistoryEntity.setPayInPZSSPortal(condition);
        licensePaymentHistoryRepository.save(licensePaymentHistoryEntity);
        return getStringResponseEntityMenagement(pinCode, memberRepository.getOne(licensePaymentHistoryEntity.getMemberUUID()), HttpStatus.OK, "toggleLicencePaymentInPZSS", "Oznaczono jako " + (condition ? "" : "nie") + "opłacone w Portalu PZSS");
//        return ResponseEntity.ok("Oznaczono jako " + (condition ? "" : "nie") + "opłacone w Portalu PZSS");
    }

    //  Tournament
    private CompetitionHistoryEntity createCompetitionHistoryEntity(String tournamentUUID, LocalDate date, String discipline, List<String> disciplineList, String attachedTo) {
        TournamentEntity tournamentEntity = tournamentRepository.getOne(tournamentUUID);
        String name = tournamentEntity.getName();
        CompetitionHistoryEntity build = CompetitionHistoryEntity.builder()
                .name(name)
                .WZSS(tournamentEntity.isWZSS())
                .date(date)
                .discipline(discipline)
                .attachedToList(attachedTo)
                .build();
        build.setDisciplineList(disciplineList);
        return build;
    }

    public void addCompetitionRecord(String memberUUID, CompetitionMembersListEntity list) {
        MemberEntity memberEntity = memberRepository.getOne(memberUUID);

        CompetitionHistoryEntity competitionHistoryEntity = createCompetitionHistoryEntity(list.getAttachedToTournament(), list.getDate(), list.getDiscipline(), list.getDisciplineList(), list.getUuid());
        competitionHistoryRepository.save(competitionHistoryEntity);

        List<CompetitionHistoryEntity> competitionHistoryEntityList = memberEntity
                .getHistory()
                .getCompetitionHistory();

        competitionHistoryEntityList.add(competitionHistoryEntity);
        competitionHistoryEntityList.sort(Comparator.comparing(CompetitionHistoryEntity::getDate).reversed());

        HistoryEntity historyEntity = memberEntity
                .getHistory();
        historyEntity.setCompetitionHistory(competitionHistoryEntityList);
        historyRepository.save(historyEntity);
        LOG.info("Dodano wpis w historii startów.");
        checkStarts(memberUUID);
        checkProlongLicense(memberUUID);
    }

    private void checkProlongLicense(String memberUUID) {
        MemberEntity memberEntity = memberRepository.getOne(memberUUID);
        HistoryEntity historyEntity = memberEntity.getHistory();
        Integer[] arr = {
                memberEntity.getLicense().isPistolPermission() ? historyEntity.getPistolCounter() : -1,
                memberEntity.getLicense().isRiflePermission() ? historyEntity.getRifleCounter() : -1,
                memberEntity.getLicense().isShotgunPermission() ? historyEntity.getShotgunCounter() : -1
        };
        Arrays.sort(arr, Collections.reverseOrder());
        memberEntity.getLicense().setCanProlong(arr[0] != -1 && arr[0] >= 4 && arr[1] == -1 && arr[2] == -1 || arr[0] != -1 && arr[0] >= 4 && arr[1] != -1 && arr[1] >= 2 && arr[2] == -1 || arr[0] != -1 && arr[0] >= 4 && arr[1] != -1 && arr[1] >= 2 && arr[2] >= 2);
        licenseRepository.save(memberEntity.getLicense());
    }

    public void rewriteDisciplinesToDisciplineList() {
        List<MemberEntity> all = memberRepository.findAll();
        for (MemberEntity m : all) {
            HistoryEntity history = m.getHistory();
            if (history != null) {
                if (history.getCompetitionHistory() != null && history.getCompetitionHistory().size() > 0) {
                    List<CompetitionHistoryEntity> competitionHistory = history.getCompetitionHistory();
                    competitionHistory.forEach(c -> {
                        List<String> disciplines = c.getDisciplines() != null ? List.of(c.getDisciplines()) : new ArrayList<>();
                        List<String> disciplineList = c.getDisciplineList();
                        if (disciplines.size() > 0)
                            c.setDisciplineList(disciplines);
                        else c.setDisciplineList(disciplineList);
                        System.out.println("przepisuję zawody " + c.getName());
                        System.out.println(disciplines.toString());
                        System.out.println(disciplineList.toString());
                        competitionHistoryRepository.save(c);
                    });
                }
            }
        }
    }

    public void checkStarts() {
//        rewriteDisciplinesToDisciplineList();
        List<MemberEntity> collect = memberRepository.findAllByErasedFalse();
        collect.forEach(e -> checkStarts(e.getUuid()));
    }

    public void checkStarts(String memberUUID) {
        MemberEntity e = memberRepository.getOne(memberUUID);
        System.out.println("Sprawdzam" + e.getFullName());
        int year = e.getLicense().getNumber() != null ? e.getLicense().getValidThru().getYear() : LocalDate.now().getYear();
        List<CompetitionHistoryEntity> collect1 = e.getHistory()
                .getCompetitionHistory()
                .stream()
                .filter(f -> f.getDate().getYear() == year)
                .collect(Collectors.toList());
        if (!collect1.isEmpty()) {
            long countPistol = collect1.stream()
                    .filter(f -> f.getDiscipline() != null && f.getDiscipline().equals(Discipline.PISTOL.getName()))
                    .count();
            long countRifle = collect1.stream()
                    .filter(f -> f.getDiscipline() != null && f.getDiscipline().equals(Discipline.RIFLE.getName()))
                    .count();
            long countShotgun = collect1.stream()
                    .filter(f -> f.getDiscipline() != null && f.getDiscipline().equals(Discipline.SHOTGUN.getName()))
                    .count();

            int pistolC = 0, rifleC = 0, shotgunC = 0;
            List<CompetitionHistoryEntity> collect2 = collect1.stream()
                    .filter(f -> f.getDisciplineList() != null)
                    .collect(Collectors.toList());
            for (CompetitionHistoryEntity entity : collect2) {
                List<String> disciplineList = entity.getDisciplineList();
                for (String s : disciplineList) {
                    if (s.contains(Discipline.PISTOL.getName())) {
                        pistolC++;
                    }
                    if (s.contains(Discipline.RIFLE.getName())) {
                        rifleC++;
                    }
                    if (s.contains(Discipline.SHOTGUN.getName())) {
                        shotgunC++;
                    }
                }
            }
            HistoryEntity history = e.getHistory();
            System.out.println((int) (countPistol + pistolC));
            System.out.println((int) countRifle + rifleC);
            System.out.println((int) countShotgun + shotgunC);
            history.setPistolCounter((int) (countPistol + pistolC));
            history.setRifleCounter((int) countRifle + rifleC);
            history.setShotgunCounter((int) countShotgun + shotgunC);
            historyRepository.save(history);
        }
        else {
            HistoryEntity history = e.getHistory();
            history.setPistolCounter(0);
            history.setRifleCounter(0);
            history.setShotgunCounter(0);
            historyRepository.save(history);
        }
    }

    public void removeCompetitionRecord(String memberUUID, CompetitionMembersListEntity list) {
        HistoryEntity historyEntity = memberRepository.getOne(memberUUID)
                .getHistory();
        CompetitionHistoryEntity competitionHistoryEntity = new CompetitionHistoryEntity();
        for (CompetitionHistoryEntity e : historyEntity.getCompetitionHistory()) {
            if (e.getAttachedToList().equals(list.getUuid())) {
                competitionHistoryEntity = competitionHistoryRepository.findById(e.getUuid()).orElseThrow(EntityNotFoundException::new);
                break;
            }

        }
        historyEntity.getCompetitionHistory().remove(competitionHistoryEntity);

        LOG.info("Zaktualizowano wpis w historii startów");
        historyRepository.save(historyEntity);
        checkStarts(memberUUID);
        competitionHistoryRepository.delete(competitionHistoryEntity);
        checkProlongLicense(memberUUID);
    }

    public void addJudgingRecord(String memberUUID, String tournamentUUID, String function) {

        TournamentEntity tournamentEntity = tournamentRepository.getOne(tournamentUUID);

        HistoryEntity historyEntity = memberRepository
                .getOne(memberUUID)
                .getHistory();

        JudgingHistoryEntity judgingHistoryEntity = createJudgingHistoryEntity(tournamentEntity.getDate(), tournamentEntity.getName(), tournamentEntity.getUuid(), function);

        List<JudgingHistoryEntity> judgingHistory = historyEntity.getJudgingHistory();

        judgingHistory.add(judgingHistoryEntity);
        judgingHistoryRepository.save(judgingHistoryEntity);
        historyEntity.setJudgingHistory(judgingHistory);

        historyRepository.save(historyEntity);
    }

    public void removeJudgingRecord(String memberUUID, String tournamentUUID) {

        List<JudgingHistoryEntity> judgingHistoryEntityList = memberRepository.getOne(memberUUID)
                .getHistory().getJudgingHistory();

        JudgingHistoryEntity any = judgingHistoryEntityList
                .stream()
                .filter(e -> e.getTournamentUUID().equals(tournamentUUID))
                .findFirst().orElseThrow(EntityNotFoundException::new);
        judgingHistoryEntityList.remove(any);
        HistoryEntity historyEntity = memberRepository
                .getOne(memberUUID)
                .getHistory();
        historyEntity.setJudgingHistory(judgingHistoryEntityList);
        historyRepository.save(historyEntity);
        judgingHistoryRepository.delete(any);

    }

    private JudgingHistoryEntity createJudgingHistoryEntity(LocalDate date, String name, String tournamentUUID, String function) {
        return JudgingHistoryEntity.builder()
                .date(date)
                .time(LocalTime.now())
                .name(name)
                .judgingFunction(function)
                .tournamentUUID(tournamentUUID)
                .build();
    }

    public void updateTournamentEntityInCompetitionHistory(String tournamentUUID) {

        TournamentEntity tournamentEntity = tournamentRepository.findById(tournamentUUID).orElseThrow(EntityNotFoundException::new);
        tournamentEntity.getCompetitionsList().forEach(competitionList -> competitionList
                .getScoreList()
                .stream().filter(f -> f.getMember() != null)
                .forEach(scoreEntity -> scoreEntity.getMember()
                        .getHistory()
                        .getCompetitionHistory()
                        .stream()
                        .filter(f -> f.getAttachedToList().equals(competitionList.getUuid()))
                        .forEach(f -> {
                            f.setName(tournamentEntity.getName());
                            f.setDate(tournamentEntity.getDate());
                            competitionHistoryRepository.save(f);
                        })));
    }

    public void updateTournamentInJudgingHistory(String tournamentUUID) {
        TournamentEntity tournamentEntity = tournamentRepository.getOne(tournamentUUID);
        if (tournamentEntity.getArbitersList() != null) {
            tournamentEntity
                    .getArbitersList()
                    .forEach(member -> member
                            .getHistory()
                            .getJudgingHistory()
                            .stream()
                            .filter(f -> f.getTournamentUUID().equals(tournamentUUID))
                            .forEach(f -> {
                                f.setName(tournamentEntity.getName());
                                f.setDate(tournamentEntity.getDate());
                                judgingHistoryRepository.save(f);
                            })
                    );
        }
        if (tournamentEntity.getMainArbiter() != null) {
            tournamentEntity.getMainArbiter()
                    .getHistory()
                    .getJudgingHistory()
                    .stream()
                    .filter(f -> f.getTournamentUUID().equals(tournamentUUID))
                    .forEach(f -> {
                        f.setName(tournamentEntity.getName());
                        f.setDate(tournamentEntity.getDate());
                        judgingHistoryRepository.save(f);
                    });
        }
        if (tournamentEntity.getCommissionRTSArbiter() != null) {
            tournamentEntity.getCommissionRTSArbiter()
                    .getHistory()
                    .getJudgingHistory()
                    .stream()
                    .filter(f -> f.getTournamentUUID().equals(tournamentUUID))
                    .forEach(f -> {
                        f.setName(tournamentEntity.getName());
                        f.setDate(tournamentEntity.getDate());
                        judgingHistoryRepository.save(f);
                    });
        }

    }

    public void updateShootingPatentHistory(String memberUUID, ShootingPatent shootingPatent) {
        MemberEntity memberEntity = memberRepository.getOne(memberUUID);
        ShootingPatentEntity shootingPatentEntity = memberEntity.getShootingPatent();
        HistoryEntity historyEntity =
                memberEntity.getHistory();
        if (shootingPatentEntity.getDateOfPosting() != null) {
            if (shootingPatentEntity.getPistolPermission()) {
                addDateToPatentPermissions(memberUUID, shootingPatent.getDateOfPosting(), 0);
            }
            if (shootingPatentEntity.getRiflePermission()) {
                addDateToPatentPermissions(memberUUID, shootingPatent.getDateOfPosting(), 1);
            }
            if (shootingPatentEntity.getShotgunPermission()) {
                addDateToPatentPermissions(memberUUID, shootingPatent.getDateOfPosting(), 2);
            }
            if (shootingPatentEntity.getDateOfPosting() != null) {
                historyEntity.setPatentFirstRecord(true);
            }
            historyRepository.save(historyEntity);
        }
    }

    public ResponseEntity<?> getStringResponseEntity(String pinCode, MemberEntity memberEntity, HttpStatus status, String methodName, Object body) throws NoUserPermissionException {
        ResponseEntity<?> response = ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON).body(body);
        ResponseEntity<String> stringResponseEntity = changeHistoryService.addRecordToChangeHistory(pinCode, memberEntity != null ? memberEntity.getClass().getSimpleName() + " " + methodName + " " : methodName, memberEntity != null ? memberEntity.getUuid() : "nie dotyczy");
        if (stringResponseEntity != null) {
            response = stringResponseEntity;
        }
        return response;
    }

    public ResponseEntity<?> getStringResponseEntityMenagement(String pinCode, MemberEntity memberEntity, HttpStatus status, String methodName, Object body) {
        ResponseEntity<?> response = ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON).body(body);
        ResponseEntity<String> stringResponseEntity = changeHistoryService.addRecordToChangeHistoryMenagement(pinCode, memberEntity != null ? memberEntity.getClass().getSimpleName() + " " + methodName + " " : methodName, memberEntity != null ? memberEntity.getUuid() : "nie dotyczy");
        if (stringResponseEntity != null) {
            response = stringResponseEntity;
        }
        return response;
    }


}
