package com.shootingplace.shootingplace.barCodeCards;

import com.google.common.hash.Hashing;
import com.shootingplace.shootingplace.domain.Person;
import com.shootingplace.shootingplace.exceptions.NoUserPermissionException;
import com.shootingplace.shootingplace.member.MemberEntity;
import com.shootingplace.shootingplace.member.MemberRepository;
import com.shootingplace.shootingplace.users.UserEntity;
import com.shootingplace.shootingplace.users.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BarCodeCardService {

    private final BarCodeCardRepository barCodeCardRepo;
    private final UserRepository userRepository;
    private final MemberRepository memberRepository;

    private static final Logger log = LoggerFactory.getLogger(BarCodeCardService.class);

    public BarCodeCardService(BarCodeCardRepository barCodeCardRepo, UserRepository userRepository, MemberRepository memberRepository) {
        this.barCodeCardRepo = barCodeCardRepo;
        this.userRepository = userRepository;
        this.memberRepository = memberRepository;
    }

    public ResponseEntity<?> createNewCard(String barCode, String uuid, String pinCode) throws NoUserPermissionException {
        String pin = Hashing.sha256().hashString(pinCode, StandardCharsets.UTF_8).toString();
        UserEntity userEntity = userRepository.findByPinCode(pin);
        if (barCodeCardRepo.existsByBarCode(barCode)) {
            return ResponseEntity.badRequest().body("Taki numer jest już do kogoś przypisany - użyj innej karty");
        }
        if (barCode.isEmpty() || barCode.isBlank()) {
            return ResponseEntity.badRequest().body("Nie podano numeru Karty");
        }
        if (!userEntity.isSuperUser()) {
            throw new NoUserPermissionException();
        }
        Person person = null;
        if (userRepository.existsById(uuid)) {
            userEntity = userRepository.getOne(uuid);
            List<BarCodeCardEntity> barCodeCardList;
            List<BarCodeCardEntity> allByBelongsTo = barCodeCardRepo.findAllByBelongsTo(userEntity.getUuid());
            int size = (int) allByBelongsTo.stream().filter(f -> f.getActivatedDay().getMonthValue() == LocalDate.now().getMonthValue()).count();
            if (size > 2) {
                return ResponseEntity.badRequest().body("Nie można dodać więcej kart do " + userEntity.getFirstName());
            }
            BarCodeCardEntity build = BarCodeCardEntity.builder()
                    .barCode(barCode)
                    .belongsTo(userEntity.getUuid())
                    .isActive(true)
                    .isMaster(true)
                    .activatedDay(LocalDate.now())
                    .type("User")
                    .subType(userEntity.getSubType())
                    .build();
            BarCodeCardEntity save = barCodeCardRepo.save(build);
            barCodeCardList = userEntity.getBarCodeCardList();
            barCodeCardList.add(save);
            userEntity.setBarCodeCardList(barCodeCardList);
            userRepository.save(userEntity);
            person = userEntity;
            return ResponseEntity.ok("Zapisano numer i przypisano do: " + person.getFirstName() + " " + person.getSecondName());
        }
        if (memberRepository.existsById(uuid)) {
            MemberEntity memberEntity = memberRepository.getOne(uuid);
            List<BarCodeCardEntity> barCodeCardList;
            List<BarCodeCardEntity> allByBelongsTo = barCodeCardRepo.findAllByBelongsTo(userEntity.getUuid());
            int size = (int) allByBelongsTo.stream().filter(f -> f.getActivatedDay().getMonthValue() == LocalDate.now().getMonthValue()).count();
            if (size > 2) {
                return ResponseEntity.badRequest().body("Nie można dodać więcej kart do " + userEntity.getFirstName());
            }
            BarCodeCardEntity build = BarCodeCardEntity.builder()
                    .barCode(barCode)
                    .belongsTo(memberEntity.getUuid())
                    .isActive(true)
                    .isMaster(true)
                    .activatedDay(LocalDate.now())
                    .type("Member")
                    .build();
            BarCodeCardEntity save = barCodeCardRepo.save(build);
            barCodeCardList = memberEntity.getBarCodeCardList();
            barCodeCardList.add(save);
            memberEntity.setBarCodeCardList(barCodeCardList);
            memberRepository.save(memberEntity);
            person = memberEntity;
        }
        if (person != null) {
            return ResponseEntity.ok("Zapisano numer i przypisano do: " + person.getFirstName() + " " + person.getSecondName());
        } else {
            return ResponseEntity.badRequest().body("coś się nie udało");
        }
    }

    public ResponseEntity<?> findAdminCode(String code) {
        List<UserEntity> adminList = userRepository.findAll()
                .stream()
                .filter(f -> f.getSubType().equals("Admin"))
                .collect(Collectors.toList());

        String[] keyList = new String[adminList.size()];
        int z = 0;
        for (UserEntity userEntity : adminList) {
            List<BarCodeCardEntity> barCodeCardList = userEntity.getBarCodeCardList();
            if (barCodeCardList.size() < 1) {
                break;
            }
            for (BarCodeCardEntity barCodeCardEntity : barCodeCardList) {
                keyList[z] = barCodeCardEntity.getBarCode();
                //tu coś nie działa
                z++;
            }
        }
        boolean r = false;

        char[] codeChars = code.toCharArray();

        // idę po długości klucza
        for (String s : keyList) {
            char[] key = s.toCharArray();
            boolean[] ok = new boolean[key.length];

            if (codeChars.length < key.length) {
                break;
            }
            int y = 0;
            for (char q : codeChars) {
                char k = key[y];
                if (q != k) {
                    ok[y] = false;
                    if (y > 0) {
                        y -= 1;
                    }
                } else {
                    ok[y] = true;
                    y++;
                }
                if (y >= ok.length) {
                    break;
                }
            }
            for (boolean b : ok) {
                if (b) {
                    r = b;
                } else {
                    r = false;
                    break;
                }
            }
            break;
        }
        return ResponseEntity.ok(r);
    }

    public void deactivateNotMasterCard() {
        log.info("dezaktywuję karty");
        List<BarCodeCardEntity> all = barCodeCardRepo.findAll();
        all.stream()
                .filter(f -> !f.isMaster())
                .filter(f -> f.getActivatedDay().getMonthValue() < LocalDate.now().getMonthValue())
                .forEach(e -> {
                    e.setActive(false);
                    barCodeCardRepo.save(e);
                });
    }

    public String findAdminBarCode(String code) {
        List<UserEntity> adminList = userRepository.findAll()
                .stream()
                .filter(f -> f.getSubType().equals("Admin"))
                .collect(Collectors.toList());

        String[] keyList = new String[adminList.size()];
        int z = 0;
        for (UserEntity userEntity : adminList) {
            List<BarCodeCardEntity> barCodeCardList = userEntity.getBarCodeCardList();
            if (barCodeCardList.size() < 1) {
                break;
            }
            for (BarCodeCardEntity barCodeCardEntity : barCodeCardList) {
                keyList[z] = barCodeCardEntity.getBarCode();
                z++;
            }
        }
        String res = "";

        char[] codeChars = code.toCharArray();

        // idę po długości klucza
        for (String s : keyList) {
            char[] key = s.toCharArray();
            boolean[] ok = new boolean[key.length];

            if (codeChars.length < key.length) {
                break;
            }
            int y = 0;
            for (char q : codeChars) {
                char k = key[y];
                if (q != k) {
                    ok[y] = false;
                    y = 0;
                } else {
                    ok[y] = true;
                    y++;
                }
                if (y >= ok.length) {
                    break;
                }
            }
            for (boolean b : ok) {
                if (b) {
                    res = s;
                } else {
                    res = "false";
                    break;
                }
            }
            break;
        }

        return res;
    }
}
