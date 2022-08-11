package com.shootingplace.shootingplace.users;

import com.shootingplace.shootingplace.services.ChangeHistoryService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.persistence.EntityExistsException;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final ChangeHistoryService changeHistoryService;
    private final Logger LOG = LogManager.getLogger(getClass());


    public UserService(UserRepository userRepository, ChangeHistoryService changeHistoryService) {
        this.userRepository = userRepository;
        this.changeHistoryService = changeHistoryService;
    }

    public List<UserDTO> getListOfSuperUser() {

        List<UserDTO> list = new ArrayList<>();
        userRepository.findAll().stream().filter(UserEntity::isSuperUser).forEach(e -> {
            list.add(
                    UserDTO.builder()
                            .firstName(e.getFirstName())
                            .secondName(e.getSecondName())
                            .uuid(e.getUuid()).build()
            )
            ;
        });
        return list;
    }

    public List<UserDTO> getListOfUser() {
        List<UserDTO> list = new ArrayList<>();
        userRepository.findAll().stream().filter(f -> !f.isSuperUser()).forEach(e -> {
            list.add(
                    UserDTO.builder()
                            .firstName(e.getFirstName())
                            .secondName(e.getSecondName())
                            .uuid(e.getUuid()).build()
            )
            ;
        });
        return list;
    }

    public ResponseEntity<?> createSuperUser(String firstName, String secondName, String pinCode) {
        if (userRepository.findAll().stream().noneMatch(UserEntity::isSuperUser)) {
            if ((firstName.trim().isEmpty() || firstName.equals("null")) || secondName.trim().isEmpty() || secondName.equals("null") || (pinCode.trim().isEmpty() || pinCode.equals("null"))) {
                return ResponseEntity.badRequest().body("Musisz podać jakieś informacje.");
            }
            String[] s1 = firstName.split(" ");
            StringBuilder trim = new StringBuilder();
            for (String value : s1) {
                String splinted = value.substring(0, 1).toUpperCase() + value.substring(1).toLowerCase() + " ";
                trim.append(splinted);
            }
            String[] s2 = firstName.split(" ");
            StringBuilder trim1 = new StringBuilder();
            for (String value : s2) {
                String splinted = value.substring(0, 1).toUpperCase() + value.substring(1).toLowerCase() + " ";
                trim1.append(splinted);
            }
            boolean anyMatch = userRepository.findAll().stream().anyMatch(a -> a.getFirstName().equals(trim.toString()) && a.getSecondName().equals(trim1.toString()));
            if (anyMatch) {
                return ResponseEntity.status(406).body("Taki użytkownik już istnieje.");
            }
            String trim2 = pinCode.trim();
            if (trim2.toCharArray().length < 4) {
                LOG.info("Kod jest za krótki. Musi posiadać 4 cyfry.");
                return ResponseEntity.status(409).body("Kod jest za krótki. Musi posiadać 4 cyfry.");
            }
            String[] failCode = {"0000", "1111", "2222", "3333", "4444", "5555", "6666", "7777", "8888", "9999"};
            for (int i = 0; i < trim2.toCharArray().length; i++) {
                if (trim2.equals(failCode[i])) {
                    LOG.info("Kod jest zbyt prosty - wymyśl coś trudniejszego.");
                    return ResponseEntity.status(409).body("Kod jest zbyt prosty - wymyśl coś trudniejszego.");
                }
            }
            boolean anyMatch1 = userRepository.findAll().stream().anyMatch(a -> a.getPinCode().equals(trim2));
            if (anyMatch1) {
                return ResponseEntity.status(403).body("Ze względów bezpieczeństwa wymyśl inny kod");
            }

            UserEntity userEntity = UserEntity.builder()
                    .superUser(true)
                    .firstName(trim.toString())
                    .secondName(trim1.toString())
                    .pinCode(trim2)
                    .active(true)
                    .build();
            userRepository.save(userEntity);
            return ResponseEntity.status(201).body("Utworzono użytkownika " + userEntity.getFirstName() + ".");
        }
        return ResponseEntity.status(400).body("Istnieje już jeden super-użytkownik : " + userRepository.findAll().stream().filter(UserEntity::isSuperUser).findFirst().orElseThrow(EntityExistsException::new).getFirstName() + ".");

    }

    public ResponseEntity<?> createUser(String firstName, String secondName, String pinCode, String superPinCode) {

        if (userRepository.findAll().stream().filter(f -> f.getPinCode().equals(superPinCode)).anyMatch(UserEntity::isSuperUser)) {

            if ((firstName.trim().isEmpty() || firstName.equals("null")) || secondName.trim().isEmpty() || secondName.equals("null") || (pinCode.trim().isEmpty() || pinCode.equals("null"))) {
                return ResponseEntity.badRequest().body("Musisz podać jakieś informacje.");
            }
            String[] s1 = firstName.split(" ");
            StringBuilder trim = new StringBuilder();
            for (String value : s1) {
                String splinted = value.substring(0, 1).toUpperCase() + value.substring(1).toLowerCase() + " ";
                trim.append(splinted);
            }
            String[] s2 = secondName.split(" ");
            StringBuilder trim1 = new StringBuilder();
            for (String value : s2) {
                String splinted = value.substring(0, 1).toUpperCase() + value.substring(1).toLowerCase() + " ";
                trim1.append(splinted);
            }
            boolean anyMatch = userRepository.findAll().stream().anyMatch(a -> a.getFirstName().equals(trim.toString()) && a.getSecondName().equals(trim1.toString()));
            if (anyMatch) {
                return ResponseEntity.status(406).body("Taki użytkownik już istnieje.");
            }

            String trim2 = pinCode.trim();
            if (trim2.toCharArray().length < 4) {
                ResponseEntity.status(409).body("Kod jest za krótki. Musi posiadać 4 cyfry.");
            }
            String[] failCode = {"0000", "1111", "2222", "3333", "4444", "5555", "6666", "7777", "8888", "9999"};
            for (int i = 0; i < trim2.toCharArray().length; i++) {
                if (trim2.equals(failCode[i])) {
                    LOG.info("Kod jest zbyt prosty - wymyśl coś trudniejszego.");
                    return ResponseEntity.status(409).body("Kod jest zbyt prosty - wymyśl coś trudniejszego.");
                }
            }
            UserEntity userEntity = UserEntity.builder()
                    .superUser(false)
                    .firstName(trim.toString())
                    .secondName(trim1.toString())
                    .subType(UserSubType.MANAGEMENT.getName())
                    .pinCode(trim2)
                    .active(true)
                    .build();
            userRepository.save(userEntity);
            changeHistoryService.addRecordToChangeHistory(superPinCode, userEntity.getClass().getSimpleName() + " " + "createUser", userEntity.getUuid());
            return ResponseEntity.status(201).body("Utworzono użytkownika " + userEntity.getFirstName() + ".");


        }
        return null;
    }

    public ResponseEntity<?> deactivateUser(String name) {
        return null;
    }
}
