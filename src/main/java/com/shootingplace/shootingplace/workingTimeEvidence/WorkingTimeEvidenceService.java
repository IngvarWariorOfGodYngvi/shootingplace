package com.shootingplace.shootingplace.workingTimeEvidence;

import com.shootingplace.shootingplace.users.UserEntity;
import com.shootingplace.shootingplace.users.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class WorkingTimeEvidenceService {
    private final UserRepository userRepository;
    private final WorkingTimeEvidenceRepository workRepo;

    private static final Logger log = LoggerFactory.getLogger(WorkingTimeEvidenceService.class);

    public WorkingTimeEvidenceService(UserRepository userRepository, WorkingTimeEvidenceRepository workRepo) {
        this.userRepository = userRepository;
        this.workRepo = workRepo;
    }

    public String createNewWTE(String number) {
        String msg;
        List<WorkingTimeEvidenceEntity> all = workRepo.findAll();
        WorkingTimeEvidenceEntity entity1 = all.stream()
                .filter(Objects::nonNull)
                .filter(f -> !f.isClose())
                .filter(f -> f.getCardNumber().equals(number))
                .findFirst().orElse(null);

        if (entity1 != null) {
            return closeWTE(entity1, false);
        } else {

//        WorkingTimeEvidenceEntity entity1 = all.stream().filter(f -> f.getCardNumber().equals(number)).filter(f -> !f.isClose()).collect(Collectors.toList()).get(0);

            if (userRepository.existsByCardNumber(number)) {
                UserEntity user = userRepository.findByCardNumber(number);
                LocalDateTime start = LocalDateTime.now();

                WorkingTimeEvidenceEntity entity = WorkingTimeEvidenceEntity.builder()
                        .start(start)
                        .stop(null)
                        .cardNumber(number)
                        .isClose(false)
                        .user(user).build();

                workRepo.save(entity);
                msg = user.getName() + " " + user.getSecondName() + " Witaj w Pracy";
            } else {
                msg = "nie znaleziono osoby o tym numerze karty";
            }
            log.info(msg);
            return msg;
        }
    }


    String closeWTE(WorkingTimeEvidenceEntity entity, boolean auto) {
        String msg;

        LocalDateTime stop = LocalDateTime.now();

        if (auto) {
            stop = LocalDateTime.now().minusHours(2);
        }

        LocalDateTime temp = getTime(entity.getStart(), true);
        LocalDateTime temp1 = getTime(stop, false);

        entity.setStop(stop);
        String s = countTime(temp, temp1);
        entity.setWorkTime(s);
        entity.closeWTE();
        workRepo.save(entity);
        msg = entity.getUser().getName() + " " + entity.getUser().getSecondName() + " Opuścił Pracę";
        log.info(msg);
        return msg;
    }

    private String countTime(LocalDateTime start, LocalDateTime stop) {

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


        String format = String.format("%02d:%02d:%02d",
                hours, minutes, seconds);
        log.info("przepracowano: " + format);
        return format;

    }


    public List<String> getAllActiveUsers() {

        List<WorkingTimeEvidenceEntity> list = workRepo.findAll().stream().filter(f -> !f.isClose()).collect(Collectors.toList());
        List<String> list1 = new ArrayList<>();
        list.forEach(e -> list1.add(e.getUser().getName() + " " + e.getUser().getSecondName()));

        return list1;
    }

    public void closeAllActiveWorkTime() {
        log.info("zamykam to co jest otwarte");
        workRepo.findAll()
                .stream()
                .filter(f -> !f.isClose())
                .collect(Collectors.toList())
                .forEach(e ->
                {
                    LocalTime countTime = LocalTime.parse(countTime(e.getStart(), LocalDateTime.now()));
                    if (countTime.getHour() > 6) {
                        closeWTE(e, true);
                    }

                });
    }


    private LocalDateTime getTime(LocalDateTime time, boolean in) {
        LocalTime temp = time.toLocalTime();
        if (in) {
            if (time.getMinute() < 8) {
                temp = LocalTime.of(time.getHour(), 0);
            }
            if (time.getMinute() >= 8 && time.getMinute() < 15) {
                temp = LocalTime.of(time.getHour(), 15);
            }
            if (time.getMinute() > 15 && time.getMinute() < 23) {
                temp = LocalTime.of(time.getHour(), 15);
            }
            if (time.getMinute() > 23 && time.getMinute() < 30) {
                temp = LocalTime.of(time.getHour(), 30);
            }
            if (time.getMinute() > 30 && time.getMinute() < 38) {
                temp = LocalTime.of(time.getHour(), 30);
            }
            if (time.getMinute() > 38 && time.getMinute() < 45) {
                temp = LocalTime.of(time.getHour(), 45);
            }
            if (time.getMinute() > 45 && time.getMinute() < 53) {
                temp = LocalTime.of(time.getHour(), 45);
            }
            if (time.getMinute() > 53) {
                temp = LocalTime.of(time.getHour() + 1, 0);
            }

        } else {
            if (time.getMinute() > 8) {
                temp = LocalTime.of(time.getHour(), 15);
            }
            if (time.getMinute() >= 20 && time.getMinute() < 30) {
                temp = LocalTime.of(time.getHour(), 30);
            }
            if (time.getMinute() > 30 && time.getMinute() < 35) {
                temp = LocalTime.of(time.getHour(), 30);
            }
            if (time.getMinute() > 35 && time.getMinute() < 45) {
                temp = LocalTime.of(time.getHour(), 45);
            }
            if (time.getMinute() > 45 && time.getMinute() < 50) {
                temp = LocalTime.of(time.getHour(), 45);
            }
            if (time.getMinute() > 50) {
                temp = LocalTime.of(time.getHour() + 1, 0);
            }

        }

        return LocalDateTime.of(time.toLocalDate(), temp);
    }

    public void closeAllActiveWorkTimeAfterCloseApp() {
        closeAllActiveWorkTime();
    }
}
