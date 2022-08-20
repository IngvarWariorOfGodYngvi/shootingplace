package com.shootingplace.shootingplace.workingTimeEvidence;

import com.shootingplace.shootingplace.barCodeCards.BarCodeCardEntity;
import com.shootingplace.shootingplace.barCodeCards.BarCodeCardRepository;
import com.shootingplace.shootingplace.domain.enums.UserSubType;
import com.shootingplace.shootingplace.services.Mapping;
import com.shootingplace.shootingplace.users.UserEntity;
import com.shootingplace.shootingplace.users.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class WorkingTimeEvidenceService {
    private final UserRepository userRepository;
    private final WorkingTimeEvidenceRepository workRepo;
    private final BarCodeCardRepository barCodeCardRepo;

    private static final Logger log = LoggerFactory.getLogger(WorkingTimeEvidenceService.class);

    public WorkingTimeEvidenceService(UserRepository userRepository, WorkingTimeEvidenceRepository workRepo, BarCodeCardRepository barCodeCardRepo) {
        this.userRepository = userRepository;
        this.workRepo = workRepo;
        this.barCodeCardRepo = barCodeCardRepo;
    }

    public String createNewWTE(String number) {
        String msg;
        BarCodeCardEntity barCode = barCodeCardRepo.findByBarCode(number);
        if (!barCode.isActive()) {
            msg = "Karta jest nieaktywna i nie można jej użyć ponownie";
            log.info(msg);
            return msg;
        }
        String belongsTo = barCode.getBelongsTo();

        // szukam osoby do której należy karta
        UserEntity user = userRepository.findById(belongsTo).orElse(null);

        // biorę wszystkie niezamknięte wiersze z obecnego miesiąca gdzie występuje osoba
        WorkingTimeEvidenceEntity entity1 = workRepo.findAll()
                .stream()
                .filter(f -> !f.isClose())
                .filter(f -> f.getStart().getMonth().equals(LocalDateTime.now().getMonth()))
                .filter(f -> f.getUser().equals(user))
                .findFirst().orElse(null);
//
//        WorkingTimeEvidenceEntity entity1 = all.stream()
//                .filter(Objects::nonNull)
//                .findFirst().orElse(null);

        // jeżeli jedna osoba jest zalogowana jako pracownik a odbije się jako zarząd to pracownika ma zamykać a otwierać zarząd


        if (entity1 != null) {
            if (!barCode.getSubType().equals(entity1.getWorkType())) {
                msg = closeWTE(entity1, false, barCode);
                String msg1 = openWTE(barCode, number);
                return msg + " " + msg1;
            } else {

                return closeWTE(entity1, false, barCode);
            }

        } else {
            msg = openWTE(barCode, number);
            return msg;
        }
    }

    String openWTE(BarCodeCardEntity barCode, String number) {
        String msg;
        String belongsTo = barCode.getBelongsTo();
        UserEntity user = userRepository.findById(belongsTo).orElse(null);
        if (user == null) {
            msg = "nie znaleziono osoby o tym numerze karty";
        } else {
            LocalDateTime start = LocalDateTime.now();

            String subType = barCode.getSubType();

            WorkingTimeEvidenceEntity entity = WorkingTimeEvidenceEntity.builder()
                    .start(start)
                    .stop(null)
                    .cardNumber(number)
                    .isClose(false)
                    .user(user)
                    .workType(subType).build();

            workRepo.save(entity);
            msg = user.getFirstName() + " " + user.getSecondName() + " Witaj w Pracy";
        }
        barCode.addCountUse();
        barCodeCardRepo.save(barCode);
        log.info(msg);
        return msg;
    }


    String closeWTE(WorkingTimeEvidenceEntity entity, boolean auto, BarCodeCardEntity barCode) {
        String msg;

        LocalDateTime stop = LocalDateTime.now();
        entity.setAutomatedClosed(false);
        if (auto) {
            stop = LocalDateTime.now().minusHours(2);
            entity.setAutomatedClosed(true);
        }
        if (!barCode.isMaster()) {
            barCode.setActive(false);
            barCodeCardRepo.save(barCode);
        }

        LocalDateTime temp = getTime(entity.getStart(), true);
        LocalDateTime temp1 = getTime(stop, false);

        entity.setStop(stop);
        String s = countTime(temp, temp1);
        int i = Integer.parseInt(s.substring(0, 2));

        if (i > 8) {
            entity.setToClarify(true);
        }
        if (i > 24) {
            i = i % 24;
            int j = Integer.parseInt(s.substring(3, 5));
            int k = Integer.parseInt(s.substring(6));

            s = String.format("%02d:%02d:%02d", i, j, k);
        }

        entity.setWorkTime(s);
        entity.closeWTE();
        workRepo.save(entity);
        msg = entity.getUser().getFirstName() + " " + entity.getUser().getSecondName() + " Opuścił Pracę";
        log.info(msg);
        return msg;
    }

    public String countTime(LocalDateTime start, LocalDateTime stop) {

        LocalDateTime tempDateTime = LocalDateTime.from(start);

        long years = tempDateTime.until(stop, ChronoUnit.YEARS);
        tempDateTime = tempDateTime.plusYears(years);

        long months = tempDateTime.until(stop, ChronoUnit.MONTHS);
        tempDateTime = tempDateTime.plusMonths(months);

        long days = tempDateTime.until(stop, ChronoUnit.DAYS);
        tempDateTime = tempDateTime.plusDays(days);

        long hours = tempDateTime.until(stop, ChronoUnit.HOURS);
        tempDateTime = tempDateTime.plusHours(hours);

        long minutes = tempDateTime.until(stop, ChronoUnit.MINUTES);
        tempDateTime = tempDateTime.plusMinutes(minutes);

        long seconds = tempDateTime.until(stop, ChronoUnit.SECONDS);


        if (days > 0) {
            hours = hours + (24 * days);
        }

        String format = String.format("%02d:%02d:%02d",
                hours, minutes, seconds);
        log.info("przepracowano: " + format);
        return format;

    }


    public List<String> getAllUsersInWork() {

        List<WorkingTimeEvidenceEntity> list = workRepo.findAll().stream().filter(f -> !f.isClose()).collect(Collectors.toList());
        List<String> list1 = new ArrayList<>();
        list.forEach(e -> list1.add(e.getUser().getFirstName() + " " + e.getUser().getSecondName()));

        return list1;
    }

    public void closeAllActiveWorkTime(String workType) {
        if (workType.equals(UserSubType.WORKER.getName())) {
            log.info("zamykam to co jest otwarte");
            workRepo.findAll()
                    .stream()
                    .filter(f -> !f.isClose())
                    .filter(f -> f.getWorkType().equals(workType))
                    .collect(Collectors.toList())
                    .forEach(e ->
                    {
                        String s = countTime(e.getStart(), LocalDateTime.now());
                        BarCodeCardEntity barCode = barCodeCardRepo.findByBarCode(e.getCardNumber());
                        if (Integer.parseInt(s.substring(0, 2)) > 6) {
                            closeWTE(e, true, barCode);
                        }

                    });
        }
    }


    public LocalDateTime getTime(LocalDateTime time, boolean in) {
        LocalTime temp = time.toLocalTime();
        if (in) {
            if (time.getMinute() < 8) {
                temp = LocalTime.of(time.getHour(), 0);
            }
            if (time.getMinute() >= 8 && time.getMinute() <= 15) {
                temp = LocalTime.of(time.getHour(), 15);
            }
            if (time.getMinute() > 15 && time.getMinute() <= 23) {
                temp = LocalTime.of(time.getHour(), 15);
            }
            if (time.getMinute() > 23 && time.getMinute() <= 30) {
                temp = LocalTime.of(time.getHour(), 30);
            }
            if (time.getMinute() > 30 && time.getMinute() <= 38) {
                temp = LocalTime.of(time.getHour(), 30);
            }
            if (time.getMinute() > 38 && time.getMinute() <= 45) {
                temp = LocalTime.of(time.getHour(), 45);
            }
            if (time.getMinute() > 45 && time.getMinute() <= 53) {
                temp = LocalTime.of(time.getHour(), 45);
            }
            if (time.getMinute() > 53) {
                temp = LocalTime.of(time.getHour() + 1, 0);
            }

        } else {
            if (time.getMinute() < 8) {
                temp = LocalTime.of(time.getHour(), 0);
            }
            if (time.getMinute() >= 8) {
                temp = LocalTime.of(time.getHour(), 15);
            }
            if (time.getMinute() >= 20 && time.getMinute() <= 30) {
                temp = LocalTime.of(time.getHour(), 30);
            }
            if (time.getMinute() > 30 && time.getMinute() <= 38) {
                temp = LocalTime.of(time.getHour(), 30);
            }
            if (time.getMinute() > 38 && time.getMinute() <= 45) {
                temp = LocalTime.of(time.getHour(), 45);
            }
            if (time.getMinute() > 45 && time.getMinute() <= 50) {
                temp = LocalTime.of(time.getHour(), 45);
            }
            if (time.getMinute() > 50) {
                temp = LocalTime.of(time.getHour() + 1, 0);
            }

        }

        return LocalDateTime.of(time.toLocalDate(), temp);
    }

    public List<WorkingTimeEvidenceDTO> getAllWorkingTimeEvidenceInMonth(String month) {
        List<WorkingTimeEvidenceDTO> WTEDTO = new ArrayList<>();
        List<WorkingTimeEvidenceEntity> pl = workRepo.findAll().stream().filter(f -> f.getStart().getMonth().getDisplayName(TextStyle.FULL_STANDALONE, Locale.forLanguageTag("pl")).equals(month)).collect(Collectors.toList());
        pl.forEach(e -> {
            WorkingTimeEvidenceDTO map = Mapping.map(e);
            UserEntity userEntity = userRepository.findById(map.getUser()).orElseThrow(EntityNotFoundException::new);
            String name = userEntity.getSecondName() + " " + userEntity.getFirstName();
            map.setUser(name);
            WTEDTO.add(map);
        });
        return WTEDTO;


    }
}
