package com.shootingplace.shootingplace.contributions;


import com.google.common.hash.Hashing;
import com.shootingplace.shootingplace.enums.ProfilesEnum;
import com.shootingplace.shootingplace.exceptions.NoUserPermissionException;
import com.shootingplace.shootingplace.history.HistoryService;
import com.shootingplace.shootingplace.member.MemberEntity;
import com.shootingplace.shootingplace.member.MemberRepository;
import com.shootingplace.shootingplace.users.UserRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import javax.validation.constraints.NotNull;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ContributionService {

    private final ContributionRepository contributionRepository;
    private final MemberRepository memberRepository;
    private final HistoryService historyService;
    private final Environment environment;
    private final UserRepository userRepository;
    private final Logger LOG = LogManager.getLogger(getClass());


    public ContributionService(ContributionRepository contributionRepository, MemberRepository memberRepository, HistoryService historyService, Environment environment, UserRepository userRepository) {
        this.contributionRepository = contributionRepository;
        this.memberRepository = memberRepository;
        this.historyService = historyService;
        this.environment = environment;
        this.userRepository = userRepository;
    }

    public ResponseEntity<?> addContribution(String memberUUID, LocalDate contributionPaymentDay, String pinCode, Integer contributionCount) throws NoUserPermissionException {
        if (!memberRepository.existsById(memberUUID)) {
            return ResponseEntity.badRequest().body("Nie znaleziono Klubowicza");
        }

        String pin = Hashing.sha256().hashString(pinCode, StandardCharsets.UTF_8).toString();
        ResponseEntity<?> response = null;
        contributionCount = environment.getActiveProfiles()[0].equals(ProfilesEnum.DZIESIATKA.getName()) ? contributionCount : 1;
        for (int i = 0; i < contributionCount; i++) {
            MemberEntity memberEntity = memberRepository.getOne(memberUUID);
            List<ContributionEntity> contributionEntityList = memberEntity.getHistory().getContributionList();
            ContributionEntity contributionEntity = ContributionEntity.builder()
                    .paymentDay(null)
                    .validThru(null)
                    .acceptedBy(userRepository.findByPinCode(pin).getFullName())
                    .build();
            if (environment.getActiveProfiles()[0].equals(ProfilesEnum.TEST.getName())) {
                contributionEntity.setPaymentDay(contributionPaymentDay);
                if (contributionEntityList.size() < 1) {
                    contributionEntity.setValidThru(contributionPaymentDay.plusMonths(3));
                } else {
                    contributionEntity.setValidThru(contributionEntityList.get(0).getValidThru().plusMonths(3));
                }
            }
            if (environment.getActiveProfiles()[0].equals(ProfilesEnum.DZIESIATKA.getName())) {
                contributionEntity.setPaymentDay(contributionPaymentDay);
                if (contributionEntityList.size() < 1) {
                    contributionEntity.setValidThru(contributionPaymentDay.plusMonths(3));
                } else {
                    contributionEntity.setValidThru(contributionEntityList.get(0).getValidThru().plusMonths(3));
                }
            }
            if (environment.getActiveProfiles()[0].equals(ProfilesEnum.PANASZEW.getName())) {
                contributionEntity.setPaymentDay(contributionPaymentDay);
                if (contributionEntityList.size() < 1) {
                    contributionEntity.setValidThru(contributionPaymentDay.plusYears(1));
                } else {
                    contributionEntity.setValidThru(contributionEntityList.get(0).getValidThru().plusYears(1));
                }
            }
            contributionRepository.save(contributionEntity);
            response = historyService.getStringResponseEntity(pinCode, contributionEntity, HttpStatus.OK, "Dodaj Składkę", "Przedłużono składkę " + memberEntity.getFullName());
            if (response.getStatusCode().equals(HttpStatus.OK)) {
                historyService.addContribution(memberUUID, contributionEntity);
                LOG.info("zmieniono " + memberEntity.getSecondName());
                memberEntity.setActive(contributionEntity.getValidThru().isAfter(LocalDate.now()));
                memberRepository.save(memberEntity);
            }
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


    public ResponseEntity<?> addContributionRecord(String memberUUID, LocalDate paymentDate, String pinCode) throws NoUserPermissionException {
        if (!memberRepository.existsById(memberUUID)) {
            return ResponseEntity.badRequest().body("Nie znaleziono Klubowicza");
        }

        MemberEntity memberEntity = memberRepository.findById(memberUUID).orElseThrow(EntityNotFoundException::new);
        String pin = Hashing.sha256().hashString(pinCode, StandardCharsets.UTF_8).toString();

        LocalDate validThru = getDate(paymentDate);

        ContributionEntity contributionEntity = ContributionEntity.builder()
                .paymentDay(paymentDate)
                .validThru(validThru)
                .acceptedBy(userRepository.findByPinCode(pin).getFullName())
                .build();
        ResponseEntity<?> response = historyService.getStringResponseEntity(pinCode, contributionEntity, HttpStatus.OK, "Dodaj Składkę ręcznie do listy ", "Dodano składkę " + memberEntity.getFullName());
        if (response.getStatusCode().equals(HttpStatus.OK)) {
            contributionRepository.save(contributionEntity);
            historyService.addContribution(memberUUID, contributionEntity);
            LOG.info("Dodano składkę " + memberEntity.getFullName());
            memberEntity.setActive(memberEntity.getHistory().getContributionList().get(0).getValidThru().isAfter(LocalDate.now()));
            memberRepository.save(memberEntity);
        }
        return response;
    }


    public ContributionEntity addFirstContribution(LocalDate contributionPaymentDay, String pinCode) {

        ContributionEntity contributionEntity = getContributionEntity(contributionPaymentDay, pinCode);
        LOG.info("utworzono pierwszą składkę");
        return contributionRepository.save(contributionEntity);
    }

    private ContributionEntity getContributionEntity(LocalDate contributionPaymentDay, String pinCode) {

        LocalDate validThru = contributionPaymentDay;
        if (environment.getActiveProfiles()[0].equals(ProfilesEnum.DZIESIATKA.getName())) {
            validThru = contributionPaymentDay.plusMonths(3);
        }
        if (environment.getActiveProfiles()[0].equals(ProfilesEnum.PANASZEW.getName())) {
            validThru = contributionPaymentDay.plusYears(1);
        }
        String pin = Hashing.sha256().hashString(pinCode, StandardCharsets.UTF_8).toString();

        return ContributionEntity.builder()
                .paymentDay(contributionPaymentDay)
                .validThru(validThru)
                .acceptedBy(userRepository.findByPinCode(pin).getFullName())
                .build();


    }

    public ResponseEntity<?> removeContribution(String memberUUID, String contributionUUID, String pinCode) throws NoUserPermissionException {
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

        ResponseEntity<?> response = historyService.getStringResponseEntity(pinCode, contributionEntity, HttpStatus.OK, "Usuń ręcznie składkę", "Usunięto składkę " + memberEntity.getSecondName() + " " + memberEntity.getFirstName());
        if (response.getStatusCode().equals(HttpStatus.OK)) {
            historyService.removeContribution(memberUUID, contributionEntity);
            LOG.info("zmieniono " + memberEntity.getSecondName());
            memberEntity.setActive(memberEntity.getHistory().getContributionList().get(0).getValidThru().isAfter(LocalDate.now()));
            memberRepository.save(memberEntity);
        }
        return response;
    }

    public ResponseEntity<?> updateContribution(String memberUUID, String contributionUUID, LocalDate paymentDay, LocalDate validThru, String pinCode) throws NoUserPermissionException {
        if (!memberRepository.existsById(memberUUID)) {
            return ResponseEntity.badRequest().body("Nie znaleziono Klubowicza");
        }
        MemberEntity memberEntity = memberRepository.getOne(memberUUID);
        String pin = Hashing.sha256().hashString(pinCode, StandardCharsets.UTF_8).toString();

        ContributionEntity contributionEntity = contributionRepository.getOne(contributionUUID);

        if (paymentDay != null) {
            contributionEntity.setPaymentDay(paymentDay);
        }

        if (validThru != null) {
            contributionEntity.setValidThru(validThru);
        }
        contributionEntity.setAcceptedBy(userRepository.findByPinCode(pin).getFullName());
        contributionEntity.setEdited(true);
        ResponseEntity<?> response = historyService.getStringResponseEntity(pinCode, contributionEntity, HttpStatus.OK, "Edytuj składkę", "Edytowano składkę " + memberEntity.getFullName());
        if (response.getStatusCode().equals(HttpStatus.OK)) {
            contributionRepository.save(contributionEntity);
            memberEntity.setActive(memberEntity.getHistory().getContributionList().get(0).getValidThru().isAfter(LocalDate.now()));
            memberRepository.save(memberEntity);
        }
        return response;
    }

}
