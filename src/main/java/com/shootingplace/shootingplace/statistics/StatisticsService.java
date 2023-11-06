package com.shootingplace.shootingplace.statistics;

import com.shootingplace.shootingplace.BookOfRegistrationOfStayAtTheShootingPlace.RegistrationRecordRepository;
import com.shootingplace.shootingplace.Mapping;
import com.shootingplace.shootingplace.ammoEvidence.AmmoEvidenceEntity;
import com.shootingplace.shootingplace.ammoEvidence.AmmoEvidenceRepository;
import com.shootingplace.shootingplace.ammoEvidence.AmmoUsedToEvidenceEntity;
import com.shootingplace.shootingplace.ammoEvidence.AmmoUsedToEvidenceEntityRepository;
import com.shootingplace.shootingplace.armory.Caliber;
import com.shootingplace.shootingplace.contributions.ContributionEntity;
import com.shootingplace.shootingplace.contributions.ContributionRepository;
import com.shootingplace.shootingplace.history.CompetitionHistoryEntity;
import com.shootingplace.shootingplace.history.LicensePaymentHistoryDTO;
import com.shootingplace.shootingplace.history.LicensePaymentHistoryRepository;
import com.shootingplace.shootingplace.member.IMemberDTO;
import com.shootingplace.shootingplace.member.MemberDTO;
import com.shootingplace.shootingplace.member.MemberEntity;
import com.shootingplace.shootingplace.member.MemberRepository;
import com.shootingplace.shootingplace.tournament.*;
import com.shootingplace.shootingplace.wrappers.MemberWithContributionWrapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.text.Collator;
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
    private final CompetitionMembersListRepository competitionMembersListRepository;
    private final LicensePaymentHistoryRepository licensePaymentHistoryRepository;

    public StatisticsService(MemberRepository memberRepository, ContributionRepository contributionRepository, AmmoEvidenceRepository ammoEvidenceRepository, AmmoUsedToEvidenceEntityRepository used, RegistrationRecordRepository rrrepo, TournamentRepository tournamentRepository, ScoreRepository scoreRepository, CompetitionMembersListRepository competitionMembersListRepository, LicensePaymentHistoryRepository licensePaymentHistoryRepository) {
        this.memberRepository = memberRepository;
        this.contributionRepository = contributionRepository;
        this.ammoEvidenceRepository = ammoEvidenceRepository;
        this.used = used;
        this.rrrepo = rrrepo;
        this.tournamentRepository = tournamentRepository;
        this.competitionMembersListRepository = competitionMembersListRepository;
        this.licensePaymentHistoryRepository = licensePaymentHistoryRepository;
    }

    public List<LicensePaymentHistoryDTO> getLicenseSum(LocalDate firstDate, LocalDate secondDate) {
        List<LicensePaymentHistoryDTO> list1 = new ArrayList<>();
        licensePaymentHistoryRepository.findAllByPayInPZSSPortalBetweenDate(firstDate, secondDate).forEach(e -> {
            MemberEntity member = memberRepository.getOne(e.getMemberUUID());
            list1.add(LicensePaymentHistoryDTO.builder()
                    .paymentUuid(e.getUuid())
                    .firstName(member.getFirstName())
                    .secondName(member.getSecondName())
                    .active(member.getActive())
                    .adult(member.getAdult())
                    .legitimationNumber(member.getLegitimationNumber())
                    .memberUUID(member.getUuid())
                    .isPayInPZSSPortal(e.isPayInPZSSPortal())
                    .date(e.getDate())
                    .licenseUUID(e.getUuid())
                    .validForYear(e.getValidForYear())
                    .isNew(e.isNew())
                    .build());
        });

        list1.sort(Comparator.comparing(LicensePaymentHistoryDTO::getDate).thenComparing(LicensePaymentHistoryDTO::getSecondName, Collator.getInstance(Locale.forLanguageTag("pl"))).thenComparing(LicensePaymentHistoryDTO::getFirstName, Collator.getInstance(Locale.forLanguageTag("pl"))));
        return list1;
    }

    public List<?> getJoinDateSum(LocalDate firstDate, LocalDate secondDate) {
        return memberRepository.getMemberBetweenJoinDate(firstDate, secondDate);
    }

    public List<MemberDTO> getErasedMembersSum(LocalDate firstDate, LocalDate secondDate) {

        return memberRepository.findAllByErasedTrue().stream()
                .filter(f -> f.getErasedEntity() != null)
                .filter(f -> f.getErasedEntity().getDate().isAfter(firstDate.minusDays(1)))
                .filter(f -> f.getErasedEntity().getDate().isBefore(secondDate.plusDays(1)))
                .map(Mapping::map2DTO)
                .sorted(Comparator.comparing(MemberDTO::getSecondName, Collator.getInstance(Locale.forLanguageTag("pl"))).thenComparing(MemberDTO::getFirstName, Collator.getInstance(Locale.forLanguageTag("pl"))))
                .collect(Collectors.toList());

    }

    public List<?> getContributionSum(LocalDate firstDate, LocalDate secondDate) {
        List<MemberWithContributionWrapper> collect1 = new ArrayList<>();
        List<ContributionEntity> allPaymentDayBetween = contributionRepository.getAllPaymentDayBetween(firstDate, secondDate);
        allPaymentDayBetween.forEach(e -> {
            IMemberDTO byHistoryUUID = memberRepository.getByHistoryUUID(e.getHistoryUUID());
            if (byHistoryUUID != null)
                collect1.add(MemberWithContributionWrapper.builder()
                        .member(byHistoryUUID)
                        .contribution(Mapping.map(e))
                        .build());

        });
        return collect1;

    }

    public String getMaxLegNumber() {
        return String.valueOf(memberRepository.getMaxLegitimationNumber());
    }

    public String getActualYearMemberCounts() {
        int count = memberRepository.countActualYearMemberCounts(LocalDate.of(LocalDate.now().getYear(), 1, 1), LocalDate.of(LocalDate.now().getYear(), 12, 31));

        return String.valueOf(count);
    }

    public List<MemberAmmo> getMembersAmmoTakesInTime(LocalDate firstDate, LocalDate secondDate) {
        List<AmmoEvidenceEntity> collect = ammoEvidenceRepository.getAllDateBetween(firstDate, secondDate);

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
        ammoList.sort(Comparator.comparing(MemberAmmo::getSecondName, Collator.getInstance(Locale.forLanguageTag("pl"))));
        return ammoList;
    }

    public List<MemberAmmo> getOthersAmmoTakesInTime(LocalDate firstDate, LocalDate secondDate) {
        List<AmmoEvidenceEntity> collect = ammoEvidenceRepository.getAllDateBetween(firstDate, secondDate);

        List<MemberAmmo> ammoList = new ArrayList<>();

        collect.forEach(e -> e.getAmmoInEvidenceEntityList()
                .forEach(g -> g.getAmmoUsedToEvidenceEntityList()
                        .forEach(h -> {
                                    if (h.getOtherPersonEntity() != null) {
                                        //znalazło pierwszy raz osobę
                                        if (ammoList.stream().noneMatch(a -> a.getLegitimationNumber().equals(h.getOtherPersonEntity().getId()))) {
                                            MemberAmmo memberAmmo = Mapping.map4(h.getOtherPersonEntity());
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
                                                    .filter(f -> f.getUuid().equals(String.valueOf(h.getOtherPersonEntity().getId())))
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
        ammoList.sort(Comparator.comparing(MemberAmmo::getSecondName, Collator.getInstance(Locale.forLanguageTag("pl"))));
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
        List<MemberEntity> all = memberRepository.findAllByErasedFalse()
                .stream()
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
        list1.forEach(e -> map.put(e.getFullName(), (int) e.getHistory().getCompetitionHistory().stream().filter(f -> f.getDate().getYear() == LocalDate.now().getYear()).count()));
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
        list1.forEach(e -> map.put(e.getFullName(), (int) e.getHistory().getContributionList().stream().filter(f -> f.getPaymentDay().getYear() == LocalDate.now().getYear()).count()));
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
                                map1.put(e.getFullName(), Float.valueOf(score.toString()));
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
