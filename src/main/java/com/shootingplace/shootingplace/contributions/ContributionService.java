package com.shootingplace.shootingplace.contributions;


import com.shootingplace.shootingplace.configurations.ProfilesEnum;
import com.shootingplace.shootingplace.history.ChangeHistoryService;
import com.shootingplace.shootingplace.history.HistoryService;
import com.shootingplace.shootingplace.member.MemberEntity;
import com.shootingplace.shootingplace.member.MemberRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ContributionService {

    private final ContributionRepository contributionRepository;
    private final MemberRepository memberRepository;
    private final HistoryService historyService;
    private final ChangeHistoryService changeHistoryService;
    private final Environment environment;
    private final Logger LOG = LogManager.getLogger(getClass());


    public ContributionService(ContributionRepository contributionRepository, MemberRepository memberRepository, HistoryService historyService, ChangeHistoryService changeHistoryService, Environment environment) {
        this.contributionRepository = contributionRepository;
        this.memberRepository = memberRepository;
        this.historyService = historyService;
        this.changeHistoryService = changeHistoryService;
        this.environment = environment;
    }

    public ResponseEntity<?> addContribution(String memberUUID, LocalDate contributionPaymentDay, String pinCode) {
        if (!memberRepository.existsById(memberUUID)) {
            return ResponseEntity.badRequest().body("Nie znaleziono Klubowicza");
        }
        MemberEntity memberEntity = memberRepository.findById(memberUUID).orElseThrow(EntityNotFoundException::new);
        List<ContributionEntity> contributionEntityList = memberEntity.getHistory().getContributionList();

        LocalDate validThru = getDate(contributionPaymentDay);

        ContributionEntity contributionEntity = ContributionEntity.builder()
                .paymentDay(null)
                .validThru(null)
                .build();
        if (environment.getActiveProfiles()[0].equals("prod")) {
            if (memberEntity.getAdult()) {
                if (contributionEntityList.size() < 1) {
                    contributionEntity.setPaymentDay(contributionPaymentDay);
                    if (contributionPaymentDay.isBefore(LocalDate.of(validThru.getYear(), 6, 30))) {
                        contributionEntity.setValidThru(LocalDate.of(validThru.getYear(), 6, 30));
                    } else {
                        contributionEntity.setValidThru(LocalDate.of(validThru.getYear(), 12, 31));
                    }
                } else {
                    contributionEntity.setPaymentDay(contributionPaymentDay);
                    if (contributionEntityList.get(0).getValidThru().equals(LocalDate.of(validThru.getYear(), 6, 30))) {
                        contributionEntity.setValidThru(LocalDate.of(contributionEntityList.get(0).getValidThru().getYear(), 12, 31));
                    } else {
                        contributionEntity.setValidThru(contributionEntityList.get(0).getValidThru().plusMonths(6));
                    }
                }
            }
            if (!memberEntity.getAdult()) {
                if (contributionEntityList.size() < 1) {
                    contributionEntity.setPaymentDay(contributionPaymentDay);
                    if (contributionPaymentDay.isBefore(LocalDate.of(validThru.getYear(), 2, 28))) {
                        contributionEntity.setValidThru(LocalDate.of(validThru.getYear(), 2, 28));
                    } else {
                        contributionEntity.setValidThru(LocalDate.of(validThru.getYear(), 8, 31));
                    }
                } else {
                    contributionEntity.setPaymentDay(contributionPaymentDay);
                    if (contributionEntityList.get(0).getValidThru().equals(LocalDate.of(validThru.getYear(), 2, 28))) {
                        contributionEntity.setValidThru(LocalDate.of(contributionEntityList.get(0).getValidThru().getYear(), 8, 31));
                        System.out.println(1);
                    } else {
                        System.out.println(2);
                        LocalDate c;
                        if (contributionEntityList.get(0).getValidThru().getMonth().getValue() == 2 && contributionEntityList.get(0).getValidThru().getDayOfMonth() == 28) {
                            c = contributionEntityList.get(0).getValidThru().plusMonths(6).plusDays(3);
                            contributionEntity.setValidThru(c);
                        } else {
                            contributionEntity.setValidThru(contributionEntityList.get(0).getValidThru().plusMonths(6));
                        }
                    }
                }
            }
        }
        if (environment.getActiveProfiles()[0].equals("rcs")){
            if (contributionEntityList.size() < 1) {
                contributionEntity.setPaymentDay(contributionPaymentDay);
                contributionEntity.setValidThru(contributionPaymentDay.plusYears(1));
            } else {
                contributionEntity.setPaymentDay(contributionPaymentDay);
                contributionEntity.setValidThru(contributionEntityList.get(0).getValidThru().plusYears(1));
            }
        }
        ResponseEntity<?> response = getStringResponseEntity(pinCode, memberEntity, HttpStatus.OK, "addContribution", "Przedłużono składkę " + memberEntity.getSecondName() + " " + memberEntity.getFirstName());
        if (response.getStatusCode().equals(HttpStatus.OK)) {
            contributionRepository.save(contributionEntity);
            historyService.addContribution(memberUUID, contributionEntity);
            memberEntity.setActive(!memberEntity.getHistory().getContributionList().get(0).getValidThru().plusMonths(3).isBefore(LocalDate.now()));
            LOG.info("zmieniono " + memberEntity.getSecondName());
            memberRepository.save(memberEntity);
        }
        return response;
    }

    @NotNull
    private LocalDate getDate(LocalDate contributionPaymentDay) {
        LocalDate validThru;
        if (contributionPaymentDay.isBefore(LocalDate.of(contributionPaymentDay.getYear(), 6, 30))) {
            validThru = LocalDate.of(contributionPaymentDay.getYear(), 6, 30);
        } else {
            validThru = LocalDate.of(contributionPaymentDay.getYear(), 12, 31);
        }
        return validThru;
    }


    public ResponseEntity<?> addContributionRecord(String memberUUID, LocalDate paymentDate, String pinCode) {
        if (!memberRepository.existsById(memberUUID)) {
            return ResponseEntity.badRequest().body("Nie znaleziono Klubowicza");
        }

        MemberEntity memberEntity = memberRepository.findById(memberUUID).orElseThrow(EntityNotFoundException::new);

        LocalDate validThru = getDate(paymentDate);

        ContributionEntity contributionEntity = ContributionEntity.builder()
                .paymentDay(paymentDate)
                .validThru(validThru)
                .build();
        ResponseEntity<?> response = getStringResponseEntity(pinCode, memberEntity, HttpStatus.OK, "addContribution", "Dodano składkę " + memberEntity.getSecondName() + " " + memberEntity.getFirstName());
        if (response.getStatusCode().equals(HttpStatus.OK)) {
            memberEntity.setActive(!memberEntity.getHistory().getContributionList().get(0).getValidThru().plusMonths(3).isBefore(LocalDate.now()));
            contributionRepository.save(contributionEntity);
            historyService.addContribution(memberUUID, contributionEntity);
            LOG.info("zmieniono " + memberEntity.getSecondName());
            memberRepository.save(memberEntity);
        }
        return response;
    }


    public ContributionEntity addFirstContribution(String memberUUID, LocalDate contributionPaymentDay) {

        ContributionEntity contributionEntity = getContributionEntity(memberUUID, contributionPaymentDay);
        LOG.info("utworzono pierwszą składkę");
        return contributionRepository.save(contributionEntity);
    }

    private ContributionEntity getContributionEntity(String memberUUID, LocalDate contributionPaymentDay) {
        MemberEntity memberEntity = memberRepository.getOne(memberUUID);

        LocalDate validThru = contributionPaymentDay;
        if(environment.getActiveProfiles()[0].equals(ProfilesEnum.DZIESIATKA.getName())){
        if (memberEntity.getAdult()) {
            if (validThru.isBefore(LocalDate.of(contributionPaymentDay.getYear(), 6, 30))) {
                validThru = LocalDate.of(contributionPaymentDay.getYear(), 6, 30);
            } else {
                validThru = LocalDate.of(contributionPaymentDay.getYear(), 12, 31);
            }
            if (memberEntity.getHistory().getContributionList() != null) {
                if (!memberEntity.getHistory().getContributionList().isEmpty()) {
                    validThru = memberEntity.getHistory().getContributionList().get(0).getValidThru();
                }
            }
        }
        if (!memberEntity.getAdult()) {
            if (validThru.isBefore(LocalDate.of(contributionPaymentDay.getYear(), 2, 28))) {
                validThru = LocalDate.of(contributionPaymentDay.getYear(), 2, 28);
            } else {
                validThru = LocalDate.of(contributionPaymentDay.getYear(), 8, 31);
            }
            if (memberEntity.getHistory().getContributionList() != null) {
                if (!memberEntity.getHistory().getContributionList().isEmpty()) {
                    validThru = memberEntity.getHistory().getContributionList().get(0).getValidThru();
                }
            }
        }}
        if (environment.getActiveProfiles()[0].equals(ProfilesEnum.PANASZEW.getName())) {
            validThru = contributionPaymentDay.plusYears(1);
        }
        return ContributionEntity.builder()
                .paymentDay(contributionPaymentDay)
                .validThru(validThru)
                .build();


    }

    public ResponseEntity<?> removeContribution(String memberUUID, String contributionUUID, String pinCode) {
        if (!memberRepository.existsById(memberUUID)) {
            return ResponseEntity.badRequest().body("Nie znaleziono Klubowicza");
        }
        MemberEntity memberEntity = memberRepository.findById(memberUUID).orElseThrow(EntityNotFoundException::new);

        ContributionEntity contributionEntity = memberRepository
                .findById(memberUUID)
                .orElseThrow(EntityNotFoundException::new)
                .getHistory()
                .getContributionList()
                .stream()
                .filter(f -> f.getUuid().equals(contributionUUID))
                .collect(Collectors.toList()).get(0);

        ResponseEntity<?> response = getStringResponseEntity(pinCode, memberEntity, HttpStatus.OK, "addContribution", "Usunięto składkę " + memberEntity.getSecondName() + " " + memberEntity.getFirstName());
        if (response.getStatusCode().equals(HttpStatus.OK)) {
            memberEntity.setActive(!memberEntity.getHistory().getContributionList().get(0).getValidThru().plusMonths(3).isBefore(LocalDate.now()));
            historyService.removeContribution(memberUUID, contributionEntity);
            contributionRepository.delete(contributionEntity);
            LOG.info("zmieniono " + memberEntity.getSecondName());
            memberRepository.save(memberEntity);
        }
        return response;
    }

    public ResponseEntity<?> updateContribution(String memberUUID, String contributionUUID, LocalDate paymentDay, LocalDate validThru, String pinCode) {
        if (!memberRepository.existsById(memberUUID)) {
            return ResponseEntity.badRequest().body("Nie znaleziono Klubowicza");
        }
        MemberEntity memberEntity = memberRepository.findById(memberUUID).orElseThrow(EntityNotFoundException::new);

        ContributionEntity contributionEntity = contributionRepository.findById(contributionUUID).orElseThrow(EntityNotFoundException::new);

        if (paymentDay != null) {
            contributionEntity.setPaymentDay(paymentDay);
        }

        if (validThru != null) {
            contributionEntity.setValidThru(validThru);
        }

        ResponseEntity<?> response = getStringResponseEntity(pinCode, memberEntity, HttpStatus.OK, "addContribution", "Edytowano składkę " + memberEntity.getSecondName() + " " + memberEntity.getFirstName());
        if (response.getStatusCode().equals(HttpStatus.OK)) {
            contributionRepository.save(contributionEntity);
        }
        return response;
    }

    ResponseEntity<?> getStringResponseEntity(String pinCode, MemberEntity memberEntity, HttpStatus status, String methodName, Object body) {
        ResponseEntity<?> response = ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON).body(body);
        ResponseEntity<?> stringResponseEntity = changeHistoryService.addRecordToChangeHistory(pinCode, memberEntity.getClass().getSimpleName() + " " + methodName + " ", memberEntity.getUuid());
        if (stringResponseEntity != null) {
            response = stringResponseEntity;
        }
        return response;
    }
}