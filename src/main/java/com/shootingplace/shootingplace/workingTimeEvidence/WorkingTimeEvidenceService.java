package com.shootingplace.shootingplace.workingTimeEvidence;

import com.shootingplace.shootingplace.barCodeCards.BarCodeCardEntity;
import com.shootingplace.shootingplace.barCodeCards.BarCodeCardRepository;
import com.shootingplace.shootingplace.domain.enums.UserSubType;
import com.shootingplace.shootingplace.file.FilesEntity;
import com.shootingplace.shootingplace.file.FilesRepository;
import com.shootingplace.shootingplace.services.Mapping;
import com.shootingplace.shootingplace.users.UserEntity;
import com.shootingplace.shootingplace.users.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
public class WorkingTimeEvidenceService {
    private final UserRepository userRepository;
    private final WorkingTimeEvidenceRepository workRepo;
    private final BarCodeCardRepository barCodeCardRepo;
    private final FilesRepository filesRepo;

    private static final Logger log = LoggerFactory.getLogger(WorkingTimeEvidenceService.class);

    public WorkingTimeEvidenceService(UserRepository userRepository, WorkingTimeEvidenceRepository workRepo, BarCodeCardRepository barCodeCardRepo, FilesRepository filesRepo) {
        this.userRepository = userRepository;
        this.workRepo = workRepo;
        this.barCodeCardRepo = barCodeCardRepo;
        this.filesRepo = filesRepo;
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

        return String.format("%02d:%02d:%02d",
                hours, minutes, seconds);

    }


    public List<String> getAllUsersInWork() {

        List<WorkingTimeEvidenceEntity> list = workRepo.findAll().stream().filter(f -> !f.isClose()).collect(Collectors.toList());
        List<String> list1 = new ArrayList<>();
        list.forEach(e -> list1.add(e.getUser().getFirstName() + " " + e.getUser().getSecondName()));

        return list1;
    }

    public void closeAllActiveWorkTime(String workType) {
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

    public List<UserWithWorkingTimeList> getAllWorkingTimeEvidenceInMonth(String month, String workType) {

//        List<WorkingTimeEvidenceDTO> WTEDTO = new ArrayList<>();
        month = month == null || month.toLowerCase(Locale.ROOT).equals("null") ? LocalDate.now().getMonth().getDisplayName(TextStyle.FULL_STANDALONE, Locale.forLanguageTag("pl")).toLowerCase(Locale.ROOT) : month.toLowerCase(Locale.ROOT);
        workType = workType == null || workType.equals("null") ? null : workType;
        List<UserWithWorkingTimeList> userWithWorkingTimeListList = new ArrayList<>();
        String finalMonth = month;
        String finalWorkType = workType;
        List<WorkingTimeEvidenceEntity> pl1;
        if (workType != null) {
            pl1 = workRepo.findAll()
                    .stream()
                    .filter(WorkingTimeEvidenceEntity::isClose)
                    .filter(f -> f.getStart() != null && f.getStop() != null)
                    .filter(f -> f.getWorkType().contains(finalWorkType))
                    .filter(f -> f.getStart().getMonth().getDisplayName(TextStyle.FULL_STANDALONE, Locale.forLanguageTag("pl")).equals(finalMonth.toLowerCase(Locale.ROOT)))
                    .collect(Collectors.toList());
        } else {
            pl1 = workRepo.findAll()
                    .stream()
                    .filter(WorkingTimeEvidenceEntity::isClose)
                    .filter(f -> f.getStart() != null && f.getStop() != null)
                    .filter(f -> f.getStart().getMonth().getDisplayName(TextStyle.FULL_STANDALONE, Locale.forLanguageTag("pl")).equals(finalMonth.toLowerCase(Locale.ROOT)))
                    .collect(Collectors.toList());
        }
        AtomicInteger workSumHours = new AtomicInteger();
        AtomicInteger workSumMinutes = new AtomicInteger();
        AtomicReference<String> format = new AtomicReference<>("");
        Set<UserEntity> user = new HashSet<>();

        pl1.forEach(e -> user.add(e.getUser()));

        user.forEach(e -> {
            List<WorkingTimeEvidenceDTO> pl2 = pl1.stream()
                    .filter(f -> f.getUser().equals(e))
                    .map(Mapping::map)
                    .sorted(Comparator.comparing(WorkingTimeEvidenceDTO::getWorkType).thenComparing(WorkingTimeEvidenceDTO::getStart).reversed())
                    .collect(Collectors.toList());

            for (int i = 0; i < pl2.size(); i++) {

//            pl2.forEach(g -> {
                WorkingTimeEvidenceDTO g = pl2.get(i);
                LocalDateTime start = getTime(g.getStart(), true);
                LocalDateTime stop = getTime(g.getStop(), false);
                String workTime = countTime(start, stop);
                System.out.println(workTime);


                int workTimeSumHours;
                int workTimeSumMinutes;


                workTimeSumHours = sumIntFromString(workTime, 0, 2);
                workTimeSumMinutes = sumIntFromString(workTime, 3, 5);
                workSumHours.getAndAdd(workTimeSumHours);
                workSumMinutes.getAndAdd(workTimeSumMinutes);

            }
            int acquire = workSumMinutes.getAcquire() % 60;
            int acquire1 = workSumMinutes.getAcquire() / 60;
            workSumHours.getAndAdd(acquire1);
            format.set(String.format("%02d:%02d",
                    workSumHours.getAcquire(), acquire));
            System.out.println(format);


            UserWithWorkingTimeList build = UserWithWorkingTimeList.builder()
                    .uuid(e.getUuid())
                    .firstName(e.getFirstName())
                    .secondName(e.getSecondName())
                    .subType(e.getSubType())
                    .WTEdtoList(pl2)
                    .workTime(String.valueOf(format))
                    .build();
            userWithWorkingTimeListList.add(build);
            workSumHours.set(0);
            workSumMinutes.set(0);
            format.set(String.format("0%2d:%02d", 0, 0));
        });
        userWithWorkingTimeListList.sort(Comparator.comparing(UserWithWorkingTimeList::getSecondName)
                .thenComparing(UserWithWorkingTimeList::getFirstName)
                .reversed());
        return userWithWorkingTimeListList;

    }

    private Integer sumIntFromString(String sequence, int substringStart, int substringEnd) {
        return Integer.parseInt(sequence.substring(substringStart, substringEnd));

    }

    public ResponseEntity<?> acceptWorkingTime(List<String> uuidList, String pinCode) {

        if (uuidList.isEmpty()) {
            return ResponseEntity.badRequest().body("Lista jest pusta - wybierz elementy do zmiany");
        }
        UserEntity userEntity = userRepository.findAll()
                .stream()
                .filter(f -> f.getSubType().contains(UserSubType.MANAGEMENT_CEO.getName()))
                .filter(f -> f.getPinCode().equals(pinCode))
                .findFirst()
                .orElse(null);

        if (userEntity != null) {
            if (userEntity.getPinCode().equals(pinCode)) {
                List<WorkingTimeEvidenceEntity> list = new ArrayList<>();
                uuidList.forEach(e -> list.add(workRepo.findById(e).orElseThrow(EntityNotFoundException::new)));
                list.forEach(e -> e.setAccepted(true));

                list.forEach(workRepo::save);
                return ResponseEntity.ok("Zatwierdzono czas pracy");
            } else {
                return ResponseEntity.badRequest().body("Pin jest niezgodny - tylko Prezes");
            }
        }
        return ResponseEntity.badRequest().body("Coś się nie udało");
    }

    public ResponseEntity<?> inputChangesToWorkTime(List<WorkingTimeEvidenceDTO> list, String pinCode) {

        if (list.isEmpty()) {
            return ResponseEntity.badRequest().body("Lista jest pusta - wybierz elementy do zmiany");
        }
        UserEntity userEntity = userRepository.findAll()
                .stream()
                .filter(f -> f.getSubType().contains(UserSubType.MANAGEMENT_CEO.getName()))
                .filter(f -> f.getPinCode().equals(pinCode))
                .findFirst()
                .orElse(null);
        if (userEntity != null) {
            if (userEntity.getPinCode().equals(pinCode)) {
                String month = list.get(0).getStart().getMonth().getDisplayName(TextStyle.FULL_STANDALONE, Locale.forLanguageTag("pl"));
                month = month.substring(0, 1).toUpperCase() + month.substring(1);
                String finalMonth = month;
                List<FilesEntity> collect = filesRepo.findAll().stream().filter(f -> f.getName().contains("raport_pracy_" + finalMonth)).sorted(Comparator.comparing(FilesEntity::getVersion).thenComparing(FilesEntity::getDate).thenComparing(FilesEntity::getTime).reversed()).collect(Collectors.toList());
                if (!collect.isEmpty()) {
                    System.out.println(collect.size());
                    System.out.println("sprawdzam");
                    FilesEntity file = collect.get(0);
                    System.out.println(file.getVersion());
                    file.setVersion(file.getVersion() + 1);
                    filesRepo.save(file);
                    System.out.println(file.getVersion());
                }

                list.forEach(e -> {
                    String s = countTime(e.getStart(), e.getStop());
                    int i = Integer.parseInt(s.substring(0, 2));

                    WorkingTimeEvidenceEntity entity = workRepo.findById(e.getUuid()).orElseThrow(EntityNotFoundException::new);
                    if (i > 8) {
                        entity.setToClarify(true);
                    }

                    entity.setStart(e.getStart());
                    entity.setStop(e.getStop());
                    entity.setWorkTime(countTime(getTime(e.getStart(), true), getTime(e.getStop(), false)));
                    workRepo.save(entity);
                });
                return ResponseEntity.ok("Zatwierdzono zmiany w czasie pracy");
            } else {
                return ResponseEntity.badRequest().body("Pin jest niezgodny - tylko Prezes");
            }
        }
        return ResponseEntity.badRequest().body("Coś się nie udało");
    }

    public ResponseEntity<?> getAllWorkingType() {


        List<String> list = new ArrayList<>();
        list.add(UserSubType.MANAGEMENT.getName());
        list.add(UserSubType.WORKER.getName());
        list.add(UserSubType.REVISION_COMMITTEE.getName());
        list.add(UserSubType.VISITOR.getName());


        return ResponseEntity.ok(list);
    }

    public boolean isInWork(UserEntity userEntity) {
        return workRepo.findAll().stream().filter(f -> !f.isClose()).anyMatch(e -> e.getUser().equals(userEntity));

    }
}
