package com.shootingplace.shootingplace.statistics;

import com.shootingplace.shootingplace.BookOfRegistrationOfStayAtTheShootingPlace.RegistrationRecordRepository;
import com.shootingplace.shootingplace.Mapping;
import com.shootingplace.shootingplace.ammoEvidence.AmmoEvidenceEntity;
import com.shootingplace.shootingplace.ammoEvidence.AmmoEvidenceRepository;
import com.shootingplace.shootingplace.ammoEvidence.AmmoUsedToEvidenceEntity;
import com.shootingplace.shootingplace.ammoEvidence.AmmoUsedToEvidenceEntityRepository;
import com.shootingplace.shootingplace.armory.Caliber;
import com.shootingplace.shootingplace.contributions.ContributionRepository;
import com.shootingplace.shootingplace.history.CompetitionHistoryEntity;
import com.shootingplace.shootingplace.history.HistoryRepository;
import com.shootingplace.shootingplace.history.LicensePaymentHistoryDTO;
import com.shootingplace.shootingplace.member.MemberDTO;
import com.shootingplace.shootingplace.member.MemberEntity;
import com.shootingplace.shootingplace.member.MemberRepository;
import com.shootingplace.shootingplace.tournament.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
public class StatisticsService {

    private final MemberRepository memberRepository;
    private final ContributionRepository contributionRepository;
    private final AmmoEvidenceRepository ammoEvidenceRepository;
    private final AmmoUsedToEvidenceEntityRepository used;
    private final RegistrationRecordRepository rrrepo;
    private final TournamentRepository tournamentRepository;
    private final ScoreRepository scoreRepository;
    private final CompetitionMembersListRepository competitionMembersListRepository;

    public StatisticsService(MemberRepository memberRepository, ContributionRepository contributionRepository, AmmoEvidenceRepository ammoEvidenceRepository, AmmoUsedToEvidenceEntityRepository used, RegistrationRecordRepository rrrepo, TournamentRepository tournamentRepository, HistoryRepository historyRepository, ScoreRepository scoreRepository, CompetitionMembersListRepository competitionMembersListRepository) {
        this.memberRepository = memberRepository;
        this.contributionRepository = contributionRepository;
        this.ammoEvidenceRepository = ammoEvidenceRepository;
        this.used = used;
        this.rrrepo = rrrepo;
        this.tournamentRepository = tournamentRepository;
        this.scoreRepository = scoreRepository;
        this.competitionMembersListRepository = competitionMembersListRepository;
    }

    public List<List<MemberDTO>> joinMonthSum(int year) {
        List<List<MemberDTO>> list = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            int finalI = i;
            List<MemberDTO> collect = memberRepository.findAll().stream()
                    .filter(f -> f.getJoinDate().getYear() == year)
                    .filter(f -> f.getJoinDate().getMonth().getValue() == finalI + 1)
                    .map(Mapping::map2DTO)
                    .sorted(Comparator.comparing(MemberDTO::getJoinDate).thenComparing(MemberDTO::getSecondName).thenComparing(MemberDTO::getFirstName))
                    .collect(Collectors.toList());

            list.add(collect);
        }
        return list;
    }

    public List<LicensePaymentHistoryDTO> getLicenseSum(LocalDate firstDate, LocalDate secondDate) {
        List<LicensePaymentHistoryDTO> list1 = new ArrayList<>();
        memberRepository.findAll()
                .forEach(member -> member.getHistory().getLicensePaymentHistory()
                        .stream()
                        .filter(lp -> lp.getDate().isAfter(firstDate.minusDays(1)))
                        .filter(lp -> lp.getDate().isBefore(secondDate.plusDays(1)))
                        .forEach(g -> list1.add(LicensePaymentHistoryDTO.builder()
                                .paymentUuid(g.getUuid())
                                .firstName(member.getFirstName())
                                .secondName(member.getSecondName())
                                .active(member.getActive())
                                .adult(member.getAdult())
                                .legitimationNumber(member.getLegitimationNumber())
                                .memberUUID(member.getUuid())
                                .isPayInPZSSPortal(g.isPayInPZSSPortal())
                                .date(g.getDate())
                                .licenseUUID(g.getUuid())
                                .validForYear(g.getValidForYear())
                                .isNew(g.isNew())
                                .build())));

        list1.sort(Comparator.comparing(LicensePaymentHistoryDTO::getDate).thenComparing(LicensePaymentHistoryDTO::getSecondName).thenComparing(LicensePaymentHistoryDTO::getFirstName));
        return list1;
    }

    public List<MemberDTO> getJoinDateSum(LocalDate firstDate, LocalDate secondDate) {

        return memberRepository.findAll().stream()
                .filter(f -> f.getJoinDate().isAfter(firstDate.minusDays(1)))
                .filter(f -> f.getJoinDate().isBefore(secondDate.plusDays(1)))
                .map(Mapping::map2DTO)
                .sorted(Comparator.comparing(MemberDTO::getJoinDate).thenComparing(MemberDTO::getSecondName).thenComparing(MemberDTO::getFirstName))
                .collect(Collectors.toList());
    }

    public List<MemberDTO> getErasedMembersSum(LocalDate firstDate, LocalDate secondDate) {

        return memberRepository.findAll().stream()
                .filter(f -> f.getErasedEntity() != null)
                .filter(f -> f.getErasedEntity().getDate().isAfter(firstDate.minusDays(1)))
                .filter(f -> f.getErasedEntity().getDate().isBefore(secondDate.plusDays(1)))
                .map(Mapping::map2DTO)
                .sorted(Comparator.comparing(MemberDTO::getSecondName).thenComparing(MemberDTO::getFirstName))
                .collect(Collectors.toList());

    }

    public List<MemberDTO> getContributionSum(LocalDate firstDate, LocalDate secondDate) {

        List<MemberEntity> memberEntities = memberRepository.findAll();

        memberEntities.forEach(t -> t.getHistory().getContributionList().forEach(g -> {
            if (g.getHistoryUUID() == null) {
                g.setHistoryUUID(t.getUuid());
                contributionRepository.save(g);
            }
        }));

        contributionRepository.findAll().forEach(e -> {
            if (e.getHistoryUUID() == null) {
                contributionRepository.delete(e);
            }
        });

        List<MemberDTO> collect1 = new ArrayList<>();

        memberRepository.findAll()
                .forEach(e -> e.getHistory().getContributionList()
                        .stream()
                        .filter(f -> f.getHistoryUUID() != null)
                        .filter(f -> f.getPaymentDay().isAfter(firstDate.minusDays(1)))
                        .filter(f -> f.getPaymentDay().isBefore(secondDate.plusDays(1)))
                        .forEach(d -> collect1.add(Mapping.map2DTO(e))));

        collect1.sort(Comparator.comparing(MemberDTO::getSecondName).thenComparing(MemberDTO::getFirstName));

        return collect1;

    }

    public String getMaxLegNumber() {
        var value = 0;
        if (!memberRepository.findAll().isEmpty()) {
            MemberEntity memberEntity = memberRepository.findAll().stream().max(Comparator.comparing(MemberEntity::getLegitimationNumber)).orElseThrow();
            value = memberEntity.getLegitimationNumber();
        }
        return String.valueOf(value);
    }

    public String getActualYearMemberCounts() {
        var count = 0;
        if (!memberRepository.findAll().isEmpty()) {
            int year = LocalDate.now().getYear();

            List<List<MemberDTO>> list = new ArrayList<>();

            for (int i = 0; i < 12; i++) {
                int finalI = i;
                List<MemberDTO> members = memberRepository.findAll().stream()
                        .filter(f -> f.getJoinDate().getYear() == year)
                        .filter(f -> f.getJoinDate().getMonth().getValue() == finalI + 1)
                        .map(Mapping::map2DTO)
                        .sorted(Comparator.comparing(MemberDTO::getJoinDate).thenComparing(MemberDTO::getSecondName).thenComparing(MemberDTO::getFirstName))
                        .collect(Collectors.toList());
                if (!members.isEmpty()) {
                    list.add(members);
                }
            }
            for (List<MemberDTO> memberDTOS : list) {
                count += memberDTOS.size();
            }
        }
        return String.valueOf(count);
    }

    public List<MemberAmmo> getMembersAmmoTakesInTime(LocalDate firstDate, LocalDate secondDate) {

        List<AmmoEvidenceEntity> collect = ammoEvidenceRepository.findAll()
                .stream()
                .filter(f -> f.getDate().isAfter(firstDate.minusDays(1)))
                .filter(f -> f.getDate().isBefore(secondDate.plusDays(1)))
                .collect(Collectors.toList());
        List<MemberAmmo> ammoList = new ArrayList<>();

        collect.forEach(e -> e.getAmmoInEvidenceEntityList()
                .forEach(g -> g.getAmmoUsedToEvidenceEntityList()
                        .forEach(h -> {
                                    if (h.getMemberEntity() != null) {
                                        //znalazło pierwszy raz osobę
                                        if (ammoList.stream().noneMatch(a -> a.getUuid().equals(h.getMemberEntity().getUuid()))) {
                                            MemberAmmo memberAmmo = Mapping.map3(h.getMemberEntity());
                                            List<Caliber> list = new ArrayList<>();
                                            Caliber caliber = Caliber.builder()
                                                    .name(g.getCaliberName())
                                                    .quantity(h.getCounter())
                                                    .build();
                                            list.add(caliber);
                                            memberAmmo.setCaliber(list);
                                            ammoList.add(memberAmmo);
                                        } else {
                                            // tutaj znalazło drugi i więcej raz osobę
                                            MemberAmmo memberAmmo = ammoList.stream()
                                                    .filter(f -> f.getUuid().equals(h.getMemberEntity().getUuid()))
                                                    .findFirst().orElseThrow(EntityNotFoundException::new);
                                            if (memberAmmo.getCaliber().stream().anyMatch(a -> a.getName().equals(g.getCaliberName()))) {
                                                Caliber caliber = memberAmmo.getCaliber().stream().filter(f -> f.getName().equals(g.getCaliberName())).findFirst().orElseThrow(EntityNotFoundException::new);
                                                Integer quantity = caliber.getQuantity();
                                                caliber.setQuantity(quantity + h.getCounter());

                                            } else {
                                                Caliber caliber = Caliber.builder()
                                                        .name(g.getCaliberName())
                                                        .quantity(h.getCounter())
                                                        .build();
                                                List<Caliber> list = memberAmmo.getCaliber();
                                                list.add(caliber);
                                                memberAmmo.setCaliber(list);

                                            }
                                        }
                                    }

                                }
                        )));
        ammoList.sort(Comparator.comparing(MemberAmmo::getSecondName));
        return ammoList;
    }

    private MemberAmmo getMemberAmmoTakes(String uuid) {

        List<AmmoUsedToEvidenceEntity> collect = used.findAll()
                .stream()
                .filter(f -> f.getMemberEntity() != null)
                .filter(f -> f.getMemberEntity().getUuid().equals(uuid))
                .collect(Collectors.toList());
        MemberAmmo memberAmmo = Mapping.map3(memberRepository.findById(uuid).orElseThrow(EntityNotFoundException::new));
        List<Caliber> list = new ArrayList<>();
        memberAmmo.setCaliber(list);
        collect.forEach(e -> {

            if (memberAmmo.getCaliber().stream().anyMatch(a -> a.getName().equals(e.getCaliberName()))) {
                Caliber caliber = memberAmmo.getCaliber().stream().filter(f -> f.getName().equals(e.getCaliberName())).findFirst().orElseThrow(EntityNotFoundException::new);
                Integer quantity = caliber.getQuantity();
                caliber.setQuantity(quantity + e.getCounter());
            } else {
                Caliber caliber1 = Caliber.builder()
                        .name(e.getCaliberName())
                        .quantity(e.getCounter())
                        .build();
                List<Caliber> clist = memberAmmo.getCaliber();
                clist.add(caliber1);
                memberAmmo.setCaliber(clist);

            }
        });
        return memberAmmo;
    }

    public ResponseEntity<?> getPersonalStatistics(String uuid) {

        MemberEntity member = memberRepository.findById(uuid).orElseThrow(EntityNotFoundException::new);

        int cc = member.getHistory().getContributionList().size();
        long vc = rrrepo.findAll().stream().filter(f -> f.getPeselOrID().equals(member.getPesel())).count();
        long mffv = member.getJoinDate().until(LocalDate.now(), ChronoUnit.MONTHS);
        MemberAmmo a = getMemberAmmoTakes(uuid);
        Set<CompetitionHistoryEntity> ch = new HashSet<>(member.getHistory().getCompetitionHistory());
        int chc = ch.size();

        PersonalStatistics ps = PersonalStatistics.builder()
                .contributionCounter(cc)
                .visitCounter((int) vc)
                .monthsFromFirstVisit((int) mffv)
                .ammo(a)
                .competitionHistoryCounter(chc).build();

        return ResponseEntity.ok(ps);
    }

    public ResponseEntity<?> getHighStatisticsCompetitions() {
        Map<String, Integer> map = new HashMap<>();
        List<TournamentEntity> all = tournamentRepository.findAll();
        final int[] i = {0};
        all.forEach(t -> {
            i[0] = 0;
            t.getCompetitionsList().forEach(c -> i[0] += c.getScoreList().size());
            map.put(t.getUuid(), i[0]);
        });
        int size = map.size();
        for (int j = 0; j < size; j++) {
            Optional<Map.Entry<String, Integer>> min = map.entrySet().stream().min(Map.Entry.comparingByValue());
            map.remove(min.get().getKey());
            if (map.size() == 10) {
                break;
            }
        }
        List<Map.Entry<String, Integer>> list = new ArrayList<>(map.entrySet());

        list.sort(Map.Entry.<String, Integer>comparingByValue().reversed());
        Map<String, Integer> map1 = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : list) {
            TournamentEntity te = tournamentRepository.findById(entry.getKey()).get();
            map1.put(te.getName() + " " + te.getDate(), entry.getValue());
        }


        return ResponseEntity.ok(map1);
    }

    public ResponseEntity<?> getTop10Competitors() {
//  top 10 zawodników
        List<MemberEntity> all = memberRepository.findAll()
                .stream()
                .filter(f -> !f.getErased())
                .filter(f -> f.getHistory() != null)
                .filter(f -> f.getHistory().getCompetitionHistory().size() > 0)
                .collect(Collectors.toList());
        List<MemberEntity> list1 = new ArrayList<>();

        for (int i = 0; i < all.size(); i++) {
            all.removeAll(list1);

            MemberEntity memberEntity = all
                    .stream()
                    .max(Comparator.comparingInt(m -> (int) m.getHistory().getCompetitionHistory()
                            .stream().filter(f -> f.getDate().getYear() == LocalDate.now().getYear()).count())).orElseThrow();
            list1.add(memberEntity);
            if (list1.size() == 10) {
                break;
            }
        }

        Map<String, Integer> map = new LinkedHashMap<>();
        list1.forEach(e -> map.put(e.getMemberName(), (int) e.getHistory().getCompetitionHistory().stream().filter(f -> f.getDate().getYear() == LocalDate.now().getYear()).count()));
        List<Map.Entry<String, Integer>> list = new ArrayList<>(map.entrySet());
        list.sort(Map.Entry.<String, Integer>comparingByValue().reversed());

        Map<String, Integer> result = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return ResponseEntity.ok(result);

    }

    public ResponseEntity<?> getTop10MembersWithTheMostMembershipContributions() {

        List<MemberEntity> all = memberRepository.findAll()
                .stream()
                .filter(f -> !f.getErased())
                .filter(f -> f.getHistory() != null)
                .filter(f -> f.getHistory().getContributionList().size() > 0)
                .collect(Collectors.toList());
        List<MemberEntity> list1 = new ArrayList<>();

        for (int i = 0; i < all.size(); i++) {
            all.removeAll(list1);

            MemberEntity memberEntity = all
                    .stream()
                    .max(Comparator.comparingInt(m -> (int) m.getHistory().getContributionList()
                            .stream().filter(f -> f.getPaymentDay().getYear() == LocalDate.now().getYear()).count())).orElseThrow();
            list1.add(memberEntity);
            if (list1.size() == 10) {
                break;
            }
        }
        Map<String, Integer> map = new LinkedHashMap<>();
        list1.forEach(e -> map.put(e.getMemberName(), (int) e.getHistory().getContributionList().stream().filter(f -> f.getPaymentDay().getYear() == LocalDate.now().getYear()).count()));
        List<Map.Entry<String, Integer>> list = new ArrayList<>(map.entrySet());
        list.sort(Map.Entry.<String, Integer>comparingByValue().reversed());

        Map<String, Integer> result = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return ResponseEntity.ok(result);
    }

    public ResponseEntity<?> getTop10CompetitionPoints() {
        List<MemberEntity> all = memberRepository.findAll()
                .stream()
                .filter(f -> !f.getErased())
                .filter(f -> f.getHistory() != null)
                .filter(f -> f.getHistory().getCompetitionHistory().size() > 0)
                .collect(Collectors.toList());
        Map<String, Float> map1 = new HashMap<>();
        all.forEach(e -> {
                    AtomicReference<Float> score = new AtomicReference<>((float) 0);
                    e.getHistory().getCompetitionHistory()
                            .stream()
                            .filter(f -> f.getDate().getYear() == LocalDate.now().getYear())
                            .forEach(g -> {
                                CompetitionMembersListEntity competitionMembersListEntity = competitionMembersListRepository.findById(g.getAttachedToList()).orElseThrow();

                                score.updateAndGet(v -> v + competitionMembersListEntity.getScoreList().stream().filter(f -> f.getMember() != null).filter(f -> f.getMember().getUuid().equals(e.getUuid())).findFirst().get().getScore());
                                map1.put(e.getMemberName(), Float.valueOf(score.toString()));
                            });
                }
        );

        List<Map.Entry<String, Float>> list = new ArrayList<>(map1.entrySet());
        list.sort(Map.Entry.<String, Float>comparingByValue().reversed());

        Map<String, Float> result = new LinkedHashMap<>();
        for (Map.Entry<String, Float> entry : list) {
            result.put(entry.getKey(), entry.getValue());
            if (result.size() == 10) {
                break;
            }
        }

        return ResponseEntity.ok(result);
    }
}
