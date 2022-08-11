package com.shootingplace.shootingplace.services;

import com.shootingplace.shootingplace.domain.entities.AmmoEvidenceEntity;
import com.shootingplace.shootingplace.member.MemberEntity;
import com.shootingplace.shootingplace.domain.models.*;
import com.shootingplace.shootingplace.member.MemberDTO;
import com.shootingplace.shootingplace.repositories.AmmoEvidenceRepository;
import com.shootingplace.shootingplace.repositories.ContributionRepository;
import com.shootingplace.shootingplace.member.MemberRepository;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StatisticsService {

    private final MemberRepository memberRepository;
    private final ContributionRepository contributionRepository;
    private final AmmoEvidenceRepository ammoEvidenceRepository;

    public StatisticsService(MemberRepository memberRepository, ContributionRepository contributionRepository, AmmoEvidenceRepository ammoEvidenceRepository) {
        this.memberRepository = memberRepository;
        this.contributionRepository = contributionRepository;
        this.ammoEvidenceRepository = ammoEvidenceRepository;
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
        memberRepository.findAll().stream()
                .filter(f -> !f.getErased())
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
                contributionRepository.saveAndFlush(g);
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
        return "\"" + value + "\"";
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
}
