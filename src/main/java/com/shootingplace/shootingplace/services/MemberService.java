package com.shootingplace.shootingplace.services;

import com.shootingplace.shootingplace.domain.entities.MemberEntity;
import com.shootingplace.shootingplace.domain.models.Member;
import com.shootingplace.shootingplace.repositories.MemberRepository;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }


    //--------------------------------------------------------------------------
    public Map<UUID, Member> getMembers() {
        Map<UUID, Member> map = new HashMap<>();
        memberRepository.findAll().forEach(e -> map.put(e.getUuid(), map(e)));
        System.out.println("Wyświetlono listę członków klubu");
        System.out.println("Ilość klubowiczów aktywnych : " + memberRepository.findAllByActive(true).size());
        System.out.println("Ilość klubowiczów nieaktywnych : " + memberRepository.findAllByActive(false).size());
        System.out.println("liczba wpisów do rejestru : " + map.size());
        return map;
    }

    public Map<UUID, Member> getActiveMembers() {
        Map<UUID, Member> map = new HashMap<>();
        memberRepository.findAllByActive(true).forEach(e -> map.put(e.getUuid(), map(e)));
        System.out.println("Ilość klubowiczów aktywnych : " + map.size());

        return map;
    }

    public Map<UUID, Member> getNonActiveMembers() {
        Map<UUID, Member> map = new HashMap<>();
        memberRepository.findAllByActive(false).forEach(e -> map.put(e.getUuid(), map(e)));
        System.out.println("Ilość klubowiczów aktywnych : " + map.size());

        return map;
    }

    //--------------------------------------------------------------------------
    public void addMember(Member member) {
        if (memberRepository.findByPesel(member.getPesel()).isPresent()) {
            System.out.println("Ktoś już na taki numer PESEL");
        } else if (memberRepository.findByEmail(member.getEmail()).isPresent()) {
            System.out.println("Ktoś już ma taki adres e-mail");
        } else if (memberRepository.findByLicenseNumber(member.getLicenseNumber()).isPresent()) {
            System.out.println("Ktoś już ma taki numer licencji");
        } else if (memberRepository.findByShootingPatentNumber(member.getShootingPatentNumber()).isPresent()) {
            System.out.println("Ktoś już ma taki numer patentu");
        } else if (memberRepository.findByLegitimationNumber(member.getLegitimationNumber()).isPresent()) {
            System.out.println("Ktoś już ma taki numer legitymacji");
        } else if (memberRepository.findByPhoneNumber(member.getPhoneNumber()).isPresent()) {
            System.out.println("Ktoś już ma taki numer telefonu");
        } else {
            if (member.getJoinDate() == null) {
                System.out.println("ustawiono domyślną datę zapisu");
                member.setJoinDate(LocalDate.now());
            }
            if (member.getLegitimationNumber() == null) {
                System.out.println("ustawiono domyślny numer legitymacji");
                member.setLegitimationNumber(memberRepository.findAll().size() + 1);
            }
            if (member.getLicenseNumber() == null) {
                System.out.println("Nie ma numeru licencji");
                member.setLicenseNumber(member.getFirstName() + " " + member.getSecondName() + " nie posiada licencji");
            }
            if (member.getShootingPatentNumber() == null) {
                System.out.println("Nie ma numeru patentu");
                member.setShootingPatentNumber(member.getFirstName() + " " + member.getSecondName() + " nie posiada patentu");
            }
            if (member.getAddress() == null) {
                System.out.println("Adres nie został wskazany");
                member.setAddress("nie wskazano adresu");
            }
            if (member.getActive().equals(false) || member.getActive() == null || member.getActive().equals(true)) {
                System.out.println("Klubowicz nie jest jeszcze aktywny");
                member.setActive(false);
            }
            if (member.getWeaponPermission() == null) {
                System.out.println("Klubowicz nie posiada pozwolenia na broń");
                member.setWeaponPermission(false);
            }
            String s = "+48";
            if (member.getPhoneNumber() == null) {
                System.out.println("Nie podano numeru telefonu");
                member.setPhoneNumber(s + "000000000");
            }
            if (member.getPhoneNumber() != null) {
                member.setPhoneNumber(s + member.getPhoneNumber().replaceAll("\\s", ""));
            }
            System.out.println("Dodano nowego członka Klubu");
            memberRepository.saveAndFlush(map(member));
        }
    }

    //--------------------------------------------------------------------------
    public boolean deleteMember(UUID uuid) {
        MemberEntity memberEntity = memberRepository.findById(uuid).orElseThrow(EntityNotFoundException::new);
        if (memberRepository.existsById(uuid) && !memberEntity.getActive()) {
            memberRepository.deleteById(uuid);
            System.out.println("Usunięto członka klubu");
            return true;
        } else if (memberRepository.existsById(uuid) && memberEntity.getActive()) {
            System.out.println("Klubowicz jest aktywny");
            return false;
        } else
            System.out.println("Nie znaleziono takiego klubowicza");
        return false;
    }

    //--------------------------------------------------------------------------
    public boolean activateOrDeactivateMember(UUID uuid) {
        if (memberRepository.existsById(uuid)) {
            MemberEntity memberEntity = memberRepository.findById(uuid).orElseThrow(EntityNotFoundException::new);
            if (!memberEntity.getActive()) {
                System.out.println(memberEntity.getFirstName() + "jest już aktywny");
                memberEntity.setActive(true);
            } else {
                System.out.println(memberEntity.getFirstName() + " Jest nieaktywny");
                memberEntity.setActive(false);
            }

            memberRepository.saveAndFlush(memberEntity);
            return true;
        } else
            System.out.println("Nie znaleziono takiego klubowicza");
        return false;
    }


    //--------------------------------------------------------------------------
    public boolean updateMember(UUID uuid, Member member) {
        try {
            MemberEntity memberEntity = memberRepository.findById(uuid).orElseThrow(EntityNotFoundException::new);
            if (memberEntity.getActive().equals(false)) {
                System.out.println("Klubowicz nie aktywny");
                return false;
            } else {
                if (member.getFirstName() != null) {
                    memberEntity.setFirstName(member.getFirstName());
                    System.out.println(goodMessage() + "Imię");
                }
                if (member.getSecondName() != null) {
                    memberEntity.setSecondName(member.getSecondName());
                    System.out.println(goodMessage() + "Nazwisko");

                }
                if (member.getJoinDate() != null) {
                    memberEntity.setJoinDate(member.getJoinDate());
                    System.out.println(goodMessage() + "Data przystąpienia do klubu");
                }
                if (member.getLegitimationNumber() != null) {
                    if (memberRepository.findByLegitimationNumber(member.getLegitimationNumber()).isPresent()) {
                        System.out.println("Już ktoś ma taki numer legitymacji");
                        return false;
                    } else {
                        memberEntity.setLegitimationNumber(member.getLegitimationNumber());
                    }
                }
                if ((member.getLicenseNumber() != null)) {
                    if (memberRepository.findByLicenseNumber(member.getLicenseNumber()).isPresent()) {
                        System.out.println("Już ktoś ma ten numer licencji");
                        return false;
                    } else {
                        memberEntity.setLicenseNumber(member.getLicenseNumber());
                        System.out.println(goodMessage() + "Numer Licencji");
                    }
                }
                if (member.getShootingPatentNumber() != null) {
                    if (memberRepository.findByShootingPatentNumber(member.getShootingPatentNumber()).isPresent()) {
                        System.out.println("Już ktoś ma ten numer patentu");
                        return false;
                    } else {
                        memberEntity.setShootingPatentNumber(member.getShootingPatentNumber());
                        System.out.println(goodMessage() + "Numer Patentu");
                    }
                }
                if (member.getEmail() != null) {
                    if (memberRepository.findByEmail(member.getEmail()).isPresent()) {
                        System.out.println("Już ktoś ma taki sam e-mail");
                        return false;
                    } else {
                        memberEntity.setEmail(member.getEmail());
                        System.out.println(goodMessage() + "Email");
                    }
                }
                if (member.getPesel() != null) {
                    if (memberRepository.findByPesel(member.getPesel()).isPresent()) {
                        System.out.println("Już ktoś ma taki sam numer PESEL");
                        return false;
                    } else {
                        memberEntity.setPesel(member.getPesel());
                        System.out.println(goodMessage() + "Numer PESEL");
                    }
                }
                if (member.getAddress() != null) {
                    memberEntity.setAddress(member.getAddress());
                    System.out.println(goodMessage() + "Adres");
                }
                if (member.getPhoneNumber() != null) {
                    String s = "+48";
                    memberEntity.setPhoneNumber((s + member.getPhoneNumber()).replaceAll("\\s", ""));
                    System.out.println(goodMessage() + "Numer Telefonu");
                }
            }
            memberRepository.saveAndFlush(memberEntity);
            return true;
        } catch (
                EntityNotFoundException ex) {
            badMessage();
            return false;
        }
    }

    private String goodMessage() {
        return "Zaktualizowano pomyślnie : ";
    }

    private void badMessage() {
        System.out.println("Nie znaleziono klubowicza");
    }

    //--------------------------------------------------------------------------

    // Mapping
    private Member map(MemberEntity e) {
        return Member.builder()
                .joinDate(e.getJoinDate())
                .legitimationNumber(e.getLegitimationNumber())
                .firstName(e.getFirstName())
                .secondName(e.getSecondName())
                .licenseNumber(e.getLicenseNumber())
                .shootingPatentNumber(e.getShootingPatentNumber())
                .email(e.getEmail())
                .pesel(e.getPesel())
                .address(e.getAddress())
                .phoneNumber(e.getPhoneNumber())
                .weaponPermission(e.getWeaponPermission())
                .active(e.getActive())
                .build();
    }

    private MemberEntity map(Member e) {
        return MemberEntity.builder()
                .joinDate(e.getJoinDate())
                .legitimationNumber(e.getLegitimationNumber())
                .firstName(e.getFirstName())
                .secondName(e.getSecondName())
                .licenseNumber(e.getLicenseNumber())
                .shootingPatentNumber(e.getShootingPatentNumber())
                .email(e.getEmail())
                .pesel(e.getPesel())
                .address(e.getAddress())
                .phoneNumber(e.getPhoneNumber())
                .weaponPermission(e.getWeaponPermission())
                .active(e.getActive())
                .build();
    }

}
