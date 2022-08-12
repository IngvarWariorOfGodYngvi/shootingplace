package com.shootingplace.shootingplace.barCodeCards;

import com.shootingplace.shootingplace.domain.models.Person;
import com.shootingplace.shootingplace.member.MemberEntity;
import com.shootingplace.shootingplace.member.MemberRepository;
import com.shootingplace.shootingplace.users.UserEntity;
import com.shootingplace.shootingplace.users.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BarCodeCardService {

    private final BarCodeCardRepository barCodeCardRepo;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MemberRepository memberRepository;

    public BarCodeCardService(BarCodeCardRepository barCodeCardRepo) {
        this.barCodeCardRepo = barCodeCardRepo;
    }

    public ResponseEntity<?> createNewCard(BarCodeCardDTO dto) {
        String adminBarCode = findAdminBarCode(dto.getBarCode());
        if(!adminBarCode.equals("false")){
            String s = dto.getBarCode().replaceAll(adminBarCode, "");
            dto.setBarCode(s);
            dto.setMaster(true);
        }


        if (barCodeCardRepo.existsByBarCode(dto.getBarCode())) {
            return ResponseEntity.badRequest().body("Taki numer jest już do kogoś przypisany - użyj innej karty");
        }
        MemberEntity memberEntity = null;
        UserEntity userEntity = userRepository.findById(dto.getBelongsTo()).orElse(null);
        if (userEntity == null) {
            memberEntity = memberRepository.findById(dto.getBelongsTo()).orElse(null);
        }

        Person person = null;
        BarCodeCardEntity build;
        List<BarCodeCardEntity> barCodeCardList;
        if (memberEntity != null) {
            build = BarCodeCardEntity.builder()
                    .barCode(dto.getBarCode())
                    .belongsTo(memberEntity.getUuid())
                    .isActive(true)
                    .type("Member")
                    .build();
            BarCodeCardEntity save = barCodeCardRepo.save(build);
            barCodeCardList = memberEntity.getBarCodeCardList();
            barCodeCardList.add(save);
            memberEntity.setBarCodeCardList(barCodeCardList);
            memberRepository.save(memberEntity);
            person = memberEntity;

        } else if (userEntity != null) {
            build = BarCodeCardEntity.builder()
                    .barCode(dto.getBarCode())
                    .belongsTo(userEntity.getUuid())
                    .isActive(true)
                    .type("User")
                    .build();
            BarCodeCardEntity save = barCodeCardRepo.save(build);
            barCodeCardList = userEntity.getBarCodeCardList();
            barCodeCardList.add(save);
            userEntity.setBarCodeCardList(barCodeCardList);
            userRepository.save(userEntity);
            person = userEntity;
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
        for (int i = 0; i < adminList.size(); i++) {
            System.out.println("i: " +i);
            System.out.println("z: " +z);
            UserEntity userEntity = adminList.get(i);
            List<BarCodeCardEntity> barCodeCardList = userEntity.getBarCodeCardList();
            for (int j = 0; j < barCodeCardList.size(); j++) {
                System.out.println(j);
                System.out.println(barCodeCardList.get(j));
                BarCodeCardEntity barCodeCardEntity = barCodeCardList.get(j);
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
    public String findAdminBarCode(String code) {
        List<UserEntity> adminList = userRepository.findAll()
                .stream()
                .filter(f -> f.getSubType().equals("Admin"))
                .collect(Collectors.toList());

        String[] keyList = new String[adminList.size()];
        int z = 0;
        for (UserEntity userEntity : adminList) {
            List<BarCodeCardEntity> barCodeCardList = userEntity.getBarCodeCardList();
            for (BarCodeCardEntity barCodeCardEntity : barCodeCardList) {
                keyList[z] = barCodeCardEntity.getBarCode();
                z++;
            }
        }
        boolean r = false;
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
                    r = b;
                    res = s;
                } else {
                    r = false;
                    res = "false";
                    break;
                }
            }
            break;
        }

        return res;
    }
}
