package com.shootingplace.shootingplace.services;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.shootingplace.shootingplace.domain.entities.*;
import com.shootingplace.shootingplace.domain.enums.CountingMethod;
import com.shootingplace.shootingplace.domain.enums.Discipline;
import com.shootingplace.shootingplace.domain.models.FilesModel;
import com.shootingplace.shootingplace.domain.models.MemberRanking;
import com.shootingplace.shootingplace.domain.models.Score;
import com.shootingplace.shootingplace.repositories.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FilesService {


    private final MemberRepository memberRepository;
    private final AmmoEvidenceRepository ammoEvidenceRepository;
    private final FilesRepository filesRepository;
    private final TournamentRepository tournamentRepository;
    private final ClubRepository clubRepository;
    private final OtherPersonRepository otherPersonRepository;
    private final GunRepository gunRepository;
    private final ContributionRepository contributionRepository;
    private final CompetitionRepository competitionRepository;
    private final GunStoreRepository gunStoreRepository;
    private final Logger LOG = LogManager.getLogger(getClass());


    public FilesService(MemberRepository memberRepository, AmmoEvidenceRepository ammoEvidenceRepository, FilesRepository filesRepository, TournamentRepository tournamentRepository, ClubRepository clubRepository, OtherPersonRepository otherPersonRepository, GunRepository gunRepository, ContributionRepository contributionRepository, CompetitionRepository competitionRepository, GunStoreRepository gunStoreRepository) {
        this.memberRepository = memberRepository;
        this.ammoEvidenceRepository = ammoEvidenceRepository;
        this.filesRepository = filesRepository;
        this.tournamentRepository = tournamentRepository;
        this.clubRepository = clubRepository;
        this.otherPersonRepository = otherPersonRepository;
        this.gunRepository = gunRepository;
        this.contributionRepository = contributionRepository;
        this.competitionRepository = competitionRepository;
        this.gunStoreRepository = gunStoreRepository;
    }

    FilesEntity createFileEntity(FilesModel filesModel) {
        filesModel.setDate(LocalDate.now());
        filesModel.setTime(LocalTime.now());
        FilesEntity filesEntity = Mapping.map(filesModel);
        LOG.info(filesModel.getName().trim() + " Encja została zapisana");
        return filesRepository.saveAndFlush(filesEntity);

    }

    public FilesEntity contributionConfirm(String memberUUID, String contributionUUID) throws DocumentException, IOException {
        ClubEntity club = clubRepository.getOne(1);
        MemberEntity memberEntity = memberRepository.findById(memberUUID).orElseThrow(EntityNotFoundException::new);
        ContributionEntity contributionEntity;
        if (contributionUUID == null || contributionUUID.isEmpty() || contributionUUID.equals("null")) {
            contributionEntity = memberEntity.getHistory().getContributionList().get(0);
        } else {
            contributionEntity = contributionRepository.findById(contributionUUID).orElseThrow(EntityNotFoundException::new);
        }
        LocalDate contribution = contributionEntity.getPaymentDay();
        LocalDate validThru = contributionEntity.getValidThru();
        String fileName = "Składka_" + memberEntity.getFirstName() + "_" + memberEntity.getSecondName() + "_" + LocalDate.now().format(dateFormat()) + ".pdf";

        String clubFullName = club.getFullName().toUpperCase();

        // tutaj powinien być text z ustawień o składki

        String contributionText = "Składki uiszczane w trybie półrocznym muszą zostać opłacone najpóźniej do końca pierwszego " +
                "kwartału za pierwsze półrocze i analogicznie za drugie półrocze do końca trzeciego kwartału. W przypadku " +
                "niedotrzymania terminu wpłaty (raty), wysokość (raty) składki ulega powiększeniu o karę w wysokości 50%" +
                " zaległości. (Regulamin Opłacania Składek Członkowskich Klubu Strzeleckiego „Dziesiątka” LOK w Łodzi)";

        LocalDate nextContribution = null;

        // tutaj musi być odpowiednie przeliczanie według ważności składek z ustawień

        if (memberEntity.getAdult()) {
            nextContribution = validThru.plusMonths(3);
        } else {
            if (validThru.equals(LocalDate.of(validThru.getYear(), 2, 28))) {
                nextContribution = validThru.plusMonths(1);
                nextContribution = nextContribution.plusDays(3);
            }
            if (validThru.equals(LocalDate.of(validThru.getYear(), 8, 31))) {
                nextContribution = validThru.plusMonths(2);
            }
        }
        Document document = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(document,
                new FileOutputStream(fileName));
        writer.setPageEvent(new PageStamper());
        document.open();
        document.addTitle(fileName);
        document.addCreationDate();

        String group;
        if (memberEntity.getAdult()) {
            group = "OGÓLNA";
        } else {
            group = "MŁODZIEŻOWA";
        }

        String status;
        if (getSex(memberEntity.getPesel()).equals("Pani")) {
            status = "opłaciła";
        } else {
            status = "opłacił";
        }
        String contributionLevel;
        if (memberEntity.getAdult()) {
            contributionLevel = "120";
        } else {
            contributionLevel = "50";
        }

        Paragraph p = new Paragraph(clubFullName + "\n", font(14, 1));
        Paragraph p1 = new Paragraph("Potwierdzenie opłacenia składki członkowskiej", font(14, 2));
        Paragraph h1 = new Paragraph("Grupa ", font(14, 0));
        Phrase h2 = new Phrase(group, font(14, 1));
        Paragraph p2 = new Paragraph("\n\nNazwisko i Imię : ", font(11, 0));
        Phrase p3 = new Phrase(memberEntity.getSecondName() + " " + memberEntity.getFirstName(), font(18, 1));
        Phrase p4 = new Phrase("Numer Legitymacji : ", font(11, 0));
        Phrase p5 = new Phrase(String.valueOf(memberEntity.getLegitimationNumber()), font(18, 1));
        Paragraph p6 = new Paragraph("\n\n\nData opłacenia składki : ", font(11, 0));
        Phrase p7 = new Phrase(String.valueOf(contribution), font(11, 1));
        Paragraph p8 = new Paragraph("\n\nSkładka ważna do : ", font(11, 0));
        Phrase p9 = new Phrase(String.valueOf(validThru), font(11, 1));
        Paragraph p10 = new Paragraph("\n\n\n" + getSex(memberEntity.getPesel()) + " ", font(11, 0));
        Phrase p11 = new Phrase(memberEntity.getSecondName() + " " + memberEntity.getFirstName() + " dnia : " + contribution + " " + status + " półroczną składkę członkowską w wysokości " + contributionLevel + " PLN.", font(11, 0));
        Paragraph p12 = new Paragraph("\n\n\n\n\nTermin opłacenia kolejnej składki : ", font(11, 0));
        Paragraph p13 = new Paragraph("\n" + (nextContribution), font(11, 1));
        Paragraph p14 = new Paragraph("", font(11, 0));

        Phrase p15 = new Phrase("\n\n" + contributionText, font(11, 2));

        Paragraph p16 = new Paragraph("\n\n\n\n\n\n\n\n\n", font(11, 0));
        Paragraph p19 = new Paragraph("pieczęć klubu", font(11, 0));
        Phrase p20 = new Phrase("                                                                 ");
        Phrase p21 = new Phrase("pieczęć i podpis osoby przyjmującej składkę");

        p.setAlignment(1);
        p1.setAlignment(1);
        h1.add(h2);
        h1.setAlignment(1);
        p2.add(p3);
        p2.add("                                    ");
        p4.add(p5);
        p2.add(p4);
        p6.add(p7);
        p8.add(p9);
        p10.add(p11);
        p14.add(p15);

        p20.add(p21);
        p19.add(p20);
        p16.setIndentationLeft(25);
        p19.setIndentationLeft(40);


        document.add(p);
        document.add(p1);
        document.add(h1);
        document.add(p2);
        document.add(p6);
        document.add(p8);
        document.add(p10);
        document.add(p12);
        document.add(p13);
        document.add(p14);
        document.add(p16);
        document.add(p19);

        document.close();

        byte[] data = convertToByteArray(fileName);
        FilesModel filesModel = FilesModel.builder()
                .name(fileName)
                .data(data)
                .type(String.valueOf(MediaType.APPLICATION_PDF))
                .size(data.length)
                .build();

        FilesEntity filesEntity =
                createFileEntity(filesModel);

        File file = new File(fileName);

        file.delete();
        return filesEntity;
    }

    public FilesEntity personalCardFile(String memberUUID) throws IOException, DocumentException {
        MemberEntity memberEntity = memberRepository.findById(memberUUID).orElseThrow(EntityNotFoundException::new);
        ClubEntity club = clubRepository.getOne(1);
        String fileName = "Karta_Członkowska_" + memberEntity.getFirstName().trim() + "_" + memberEntity.getSecondName() + ".pdf";
        LocalDate birthDate = birthDay(memberEntity.getPesel());
        Document document = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(document,
                new FileOutputStream(fileName));
        writer.setPageEvent(new PageStamper());

        document.open();
        document.addTitle(fileName);
        document.addCreationDate();

        // pobierać z ustawień

        String statement = "Oświadczenie:\n" +
                "- Zobowiązuję się do przestrzegania Regulaminu Strzelnicy, oraz Regulaminu Klubu Strzeleckiego „Dziesiątka” Ligi Obrony Kraju w Łodzi.\n" +
                "- Wyrażam zgodę na przesyłanie mi informacji przez Klub Strzelecki „Dziesiątka” za pomocą środków komunikacji elektronicznej, w szczególności pocztą elektroniczną oraz w postaci sms-ów/mms-ów.\n" +
                "Wyrażenie zgody jest dobrowolne i może być odwołane w każdym czasie w na podstawie oświadczenia skierowanego na adres siedziby Klubu, na podstawie oświadczenia przesłanego za pośrednictwem poczty elektronicznej na adres: biuro@ksdziesiatka.pl lub w inny uzgodniony sposób.\n" +
                "- Zgadzam się na przetwarzanie moich danych osobowych (w tym wizerunku) przez Administratora Danych, którym jest Stowarzyszenie Liga Obrony Kraju mające siedzibę główną w Warszawie pod adresem: \n" +
                "ul. Chocimska 14, 00-791 Warszawa w celach związanych z moim członkostwem w KS „Dziesiątka” LOK Łódź.";


        String sex = getSex(memberEntity.getPesel());
        if (sex.equals("Pani")) {
            sex = "córki";
        } else {
            sex = "syna";
        }
        String adultAcceptation = "- Wyrażam zgodę na udział i członkostwo " + sex + " w Klubie";
        Paragraph p = new Paragraph(club.getFullName() + "\n", font(14, 1));
        // setAlignment(0) = left setAlignment(1) = center setAlignment(2) = right
        p.setAlignment(1);
        String group;
        if (memberEntity.getAdult()) {
            group = "OGÓLNA";
        } else {
            group = "MŁODZIEŻOWA";
        }
        Paragraph p1 = new Paragraph("Karta Członkowska\n", font(14, 2));
        Phrase p2 = new Phrase(group, font(14, 1));
        Paragraph p3 = new Paragraph("\nNazwisko i Imię : ", font(11, 0));
        Phrase p4 = new Phrase(memberEntity.getSecondName() + " " + memberEntity.getFirstName(), font(18, 1));
        Phrase p5 = new Phrase("Numer Legitymacji : ", font(11, 0));
        Phrase p6 = new Phrase(String.valueOf(memberEntity.getLegitimationNumber()), font(18, 1));
        Paragraph p7 = new Paragraph("\nData Wstąpienia : ", font(11, 0));
        Phrase p8 = new Phrase(String.valueOf(memberEntity.getJoinDate()), font(15, 0));
        Paragraph p9 = new Paragraph("\nData Urodzenia : ", font(11, 0));
        Phrase p10 = new Phrase(String.valueOf(birthDate));
        Paragraph p11 = new Paragraph("PESEL : " + memberEntity.getPesel(), font(11, 0));
        Paragraph p12 = new Paragraph("", font(11, 0));
        Phrase p13 = new Phrase(memberEntity.getIDCard());
        String phone = memberEntity.getPhoneNumber();
        String split = phone.substring(0, 3) + " ";
        String split1 = phone.substring(3, 6) + " ";
        String split2 = phone.substring(6, 9) + " ";
        String split3 = phone.substring(9, 12) + " ";
        String phoneSplit = split + split1 + split2 + split3;
        Paragraph p14 = new Paragraph("Telefon Kontaktowy : " + phoneSplit, font(11, 0));
        Paragraph p15 = new Paragraph("", font(11, 0));
        Paragraph p16 = new Paragraph("\n\nAdres Zamieszkania", font(11, 0));
        Paragraph p17 = new Paragraph("", font(11, 0));
        Paragraph p18 = new Paragraph("\n\n" + statement, font(11, 0));
        Paragraph p19;
        if (!memberEntity.getAdult()) {
            if (LocalDate.now().minusYears(18).isBefore(birthDate)) {
                p18 = new Paragraph("\n\n" + statement + "\n" + adultAcceptation + "\n\n     Podpis Rodzica / Opiekuna Prawnego\n         ..................................................", font(11, 0));
            }
            p19 = new Paragraph("\n\n\n\n.............................................", font(11, 0));
        } else {
            p19 = new Paragraph("\n\n\n\n\n\n.............................................", font(11, 0));
        }
        Phrase p20 = new Phrase("                                                              ");
        Phrase p21 = new Phrase("............................................................");
        Paragraph p22 = new Paragraph("miejscowość, data i podpis Klubowicza", font(11, 0));
        Phrase p23 = new Phrase("                                                                 ");
        Phrase p24 = new Phrase("podpis przyjmującego");

        p1.add("Grupa ");
        p1.add(p2);
        p1.setAlignment(1);
        p3.add(p4);
        p3.add("          ");
        p5.add(p6);
        p3.add(p5);
        p7.add(p8);
        p7.add("\n");
        p9.add(p10);
        if (memberEntity.getAdult()) {
            p12.add("Numer Dowodu Osobistego : ");
        } else {
            p12.add("Numer Legitymacji Szkolnej / Numer Dowodu Osobistego : ");
        }
        p12.add(p13);
        p12.add("\n\n\n");
        if (memberEntity.getEmail() != null) {
            p15.add("Email : " + memberEntity.getEmail());
        } else {
            p15.add("Email : Nie podano");
        }
        if (memberEntity.getAddress().getPostOfficeCity() != null) {
            p17.add("Miasto : " + memberEntity.getAddress().getPostOfficeCity());
        } else {
            p17.add("Miasto : ");
        }
        p17.add("\n");
        if (memberEntity.getAddress().getZipCode() != null) {
            p17.add("Kod pocztowy : " + memberEntity.getAddress().getZipCode());
        } else {
            p17.add("Kod pocztowy : ");
        }
        p17.add("\n");
        if (memberEntity.getAddress().getStreet() != null) {
            p17.add("Ulica : " + memberEntity.getAddress().getStreet());
            if (memberEntity.getAddress().getStreetNumber() != null) {
                p17.add(" " + memberEntity.getAddress().getStreetNumber());
            } else {
                p17.add(" ");
            }
        } else {
            p17.add("Ulica : ");
        }
        p17.add("\n");
        if (memberEntity.getAddress().getFlatNumber() != null) {
            p17.add("Numer Mieszkania : " + memberEntity.getAddress().getFlatNumber());
        } else {
            p17.add("Numer Mieszkania : ");
        }
        p20.add(p21);
        p19.add(p20);
        p19.setIndentationLeft(40);
        p22.setIndentationLeft(25);
        p22.add(p23);
        p22.add(p24);

        document.add(p);
        document.add(p1);
        document.add(p3);
        document.add(p7);
        document.add(p9);
        document.add(p11);
        document.add(p12);
        document.add(p14);
        document.add(p15);
        document.add(p16);
        document.add(p17);
        document.add(p18);
        document.add(p19);
        document.add(p22);


        document.close();

        byte[] data = convertToByteArray(fileName);
        FilesModel filesModel = FilesModel.builder()
                .name(fileName)
                .data(data)
                .type(String.valueOf(MediaType.APPLICATION_PDF))
                .size(data.length)
                .build();

        FilesEntity filesEntity =
                createFileEntity(filesModel);


        filesEntity.setName(fileName);
        filesEntity.setType(String.valueOf(MediaType.APPLICATION_PDF));
        filesEntity.setData(data);
        File file = new File(fileName);

        file.delete();
        return filesEntity;

    }

    public FilesEntity createAmmunitionListDocument(String ammoEvidenceUUID) throws IOException, DocumentException {
        AmmoEvidenceEntity ammoEvidenceEntity = ammoEvidenceRepository.findById(ammoEvidenceUUID).orElseThrow(EntityNotFoundException::new);
        ClubEntity club = clubRepository.getOne(1);

        List<AmmoInEvidenceEntity> ammoInEvidenceEntityList1 = new ArrayList<>();


        List<AmmoInEvidenceEntity> a = ammoEvidenceEntity.getAmmoInEvidenceEntityList();

        String[] sort = {"5,6mm", "9x19mm", "12/76", ".357", ".38", "7,62x39mm"};

        AmmoInEvidenceEntity ammoInEvidenceEntity1 = a.stream().filter(f -> f.getCaliberName().equals(sort[0])).findFirst().orElse(null);
        AmmoInEvidenceEntity ammoInEvidenceEntity2 = a.stream().filter(f -> f.getCaliberName().equals(sort[1])).findFirst().orElse(null);
        AmmoInEvidenceEntity ammoInEvidenceEntity3 = a.stream().filter(f -> f.getCaliberName().equals(sort[2])).findFirst().orElse(null);
        AmmoInEvidenceEntity ammoInEvidenceEntity4 = a.stream().filter(f -> f.getCaliberName().equals(sort[3])).findFirst().orElse(null);
        AmmoInEvidenceEntity ammoInEvidenceEntity5 = a.stream().filter(f -> f.getCaliberName().equals(sort[4])).findFirst().orElse(null);
        AmmoInEvidenceEntity ammoInEvidenceEntity6 = a.stream().filter(f -> f.getCaliberName().equals(sort[5])).findFirst().orElse(null);

        List<AmmoInEvidenceEntity> ammoInEvidenceEntityList2 = a.stream().filter(f ->
                        !f.getCaliberName().equals(sort[0])
                                && !f.getCaliberName().equals(sort[1])
                                && !f.getCaliberName().equals(sort[2])
                                && !f.getCaliberName().equals(sort[3])
                                && !f.getCaliberName().equals(sort[4])
                                && !f.getCaliberName().equals(sort[5]))
                .collect(Collectors.toList());

        if (ammoInEvidenceEntity1 != null) {

            ammoInEvidenceEntityList1.add(ammoInEvidenceEntity1);
        }
        if (ammoInEvidenceEntity2 != null) {

            ammoInEvidenceEntityList1.add(ammoInEvidenceEntity2);
        }
        if (ammoInEvidenceEntity3 != null) {

            ammoInEvidenceEntityList1.add(ammoInEvidenceEntity3);
        }
        if (ammoInEvidenceEntity4 != null) {

            ammoInEvidenceEntityList1.add(ammoInEvidenceEntity4);
        }
        if (ammoInEvidenceEntity5 != null) {

            ammoInEvidenceEntityList1.add(ammoInEvidenceEntity5);
        }
        if (ammoInEvidenceEntity6 != null) {

            ammoInEvidenceEntityList1.add(ammoInEvidenceEntity6);
        }

        ammoInEvidenceEntityList1.addAll(ammoInEvidenceEntityList2);

        a.sort(Comparator.comparing(AmmoInEvidenceEntity::getCaliberName));

        String fileName = "Lista_Amunicyjna_" + ammoEvidenceEntity.getDate().format(dateFormat()) + ".pdf";

        Document document = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(document,
                new FileOutputStream(fileName));
        writer.setPageEvent(new PageStamper());
        document.open();
        document.addTitle(fileName);
        document.addCreationDate();


        Paragraph number = new Paragraph(ammoEvidenceEntity.getNumber(), font(10, 4));
        Paragraph p = new Paragraph(club.getFullName() + "\n", font(14, 1));
        Paragraph p1 = new Paragraph("Lista rozliczenia amunicji " + ammoEvidenceEntity.getDate().format(dateFormat()), font(12, 2));

        number.setIndentationLeft(450);
        p.setAlignment(1);
        p1.add("\n");
        p1.setAlignment(1);

        document.add(number);
        document.add(p);
        document.add(p1);
        for (AmmoInEvidenceEntity ammoInEvidenceEntity : ammoInEvidenceEntityList1) {
            Paragraph p2 = new Paragraph("Kaliber : " + ammoInEvidenceEntity.getCaliberName() + "\n", font(12, 1));
            p2.add("\n");
            p2.setIndentationLeft(230);
            document.add(p2);
            float[] pointColumnWidths = {20F, 255F, 25};
            PdfPTable tableLabel = new PdfPTable(pointColumnWidths);
            PdfPCell cellLabel = new PdfPCell(new Paragraph(new Paragraph("lp.", font(10, 2))));
            PdfPCell cell1Label = new PdfPCell(new Paragraph(new Paragraph("Imię i Nazwisko", font(10, 2))));
            PdfPCell cell2Label = new PdfPCell(new Paragraph(new Paragraph("ilość sztuk", font(10, 2))));


            tableLabel.addCell(cellLabel);
            tableLabel.addCell(cell1Label);
            tableLabel.addCell(cell2Label);
            document.add(tableLabel);
            for (int j = 0; j < ammoInEvidenceEntity.getAmmoUsedToEvidenceEntityList().size(); j++) {
                PdfPTable table = new PdfPTable(pointColumnWidths);
                PdfPCell cell;
                PdfPCell cell1;
                PdfPCell cell2;

                String name;
                if (ammoInEvidenceEntity.getAmmoUsedToEvidenceEntityList().get(j).getMemberEntity() == null) {
                    OtherPersonEntity otherPersonEntity = ammoInEvidenceEntity.getAmmoUsedToEvidenceEntityList().get(j).getOtherPersonEntity();
                    name = otherPersonEntity.getSecondName() + " " + otherPersonEntity.getFirstName();
                } else {
                    MemberEntity memberEntity = ammoInEvidenceEntity.getAmmoUsedToEvidenceEntityList().get(j).getMemberEntity();
                    name = memberEntity.getSecondName() + " " + memberEntity.getFirstName();
                }

                cell = new PdfPCell(new Paragraph(String.valueOf(j + 1), font(10, 2)));
                cell1 = new PdfPCell(new Paragraph(name, font(10, 2)));
                cell2 = new PdfPCell(new Paragraph(ammoInEvidenceEntity.getAmmoUsedToEvidenceEntityList().get(j).getCounter().toString(), font(10, 2)));
                table.addCell(cell);
                table.addCell(cell1);
                table.addCell(cell2);
                document.add(table);
            }
            PdfPTable tableSum = new PdfPTable(pointColumnWidths);
            PdfPCell cellTableSum = new PdfPCell(new Paragraph(new Paragraph("", font(10, 2))));
            PdfPCell cellTableSum1 = new PdfPCell(new Paragraph(new Paragraph("Suma", font(10, 2))));
            PdfPCell cellTableSum2 = new PdfPCell(new Paragraph(new Paragraph(ammoInEvidenceEntity.getQuantity().toString(), font(10, 2))));
            cellTableSum.setBorder(0);
            cellTableSum1.setBorder(0);
            cellTableSum1.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);

            tableSum.addCell(cellTableSum);
            tableSum.addCell(cellTableSum1);
            tableSum.addCell(cellTableSum2);
            document.add(tableSum);

        }

        document.close();


        byte[] data = convertToByteArray(fileName);
        FilesModel filesModel = FilesModel.builder()
                .name(fileName)
                .data(data)
                .type(String.valueOf(MediaType.APPLICATION_PDF))
                .size(data.length)
                .build();

        FilesEntity filesEntity =
                createFileEntity(filesModel);

        File file = new File(fileName);

        file.delete();
        return filesEntity;

    }

    public FilesEntity createApplicationForExtensionOfTheCompetitorsLicense(String memberUUID) throws IOException, DocumentException {
        MemberEntity memberEntity = memberRepository.findById(memberUUID).orElseThrow(EntityNotFoundException::new);

        String fileName = "Wniosek_" + memberEntity.getFirstName() + "_" + memberEntity.getSecondName() + ".pdf";

        Document document = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(fileName));
        document.open();
        document.addTitle(fileName);
        document.addCreationDate();
        PdfContentByte cb = writer.getDirectContent();
        String path1 = "Wniosek_o_przedluzenie_licencji_zawodniczej.pdf";
        PdfReader reader = new PdfReader(path1);
        PdfImportedPage page = writer.getImportedPage(reader, 1);

        document.newPage();
        cb.addTemplate(page, 0, 0);

        String licenseNumber = memberEntity.getLicense().getNumber();
        char[] P = memberEntity.getPesel().toCharArray();
        String name = memberEntity.getSecondName().toUpperCase() + "  " + memberEntity.getFirstName().toUpperCase();
        String phone = memberEntity.getPhoneNumber();
        String split = phone.substring(0, 3) + " ";
        String split1 = phone.substring(3, 6) + " ";
        String split2 = phone.substring(6, 9) + " ";
        String split3 = phone.substring(9, 12) + " ";
        String phoneSplit = split + split1 + split2 + split3;
        String date = memberEntity.getLicense().getValidThru().toString().substring(2, 4);

        // brać uprawnienia z patentu
        int pistol = 0;
        int licenceYear = memberEntity.getLicense().getValidThru().getYear();
        List<CompetitionHistoryEntity> collectPistol = memberEntity.getHistory().getCompetitionHistory()
                .stream()
                .filter(f -> f.getDisciplines() == null)
                .filter(CompetitionHistoryEntity::isWZSS)
                .filter(f -> f.getDate().getYear() == licenceYear)
                .filter(f -> f.getDiscipline().equals(Discipline.values()[0].getName()))
                .collect(Collectors.toList());
        List<CompetitionHistoryEntity> collectRifle = memberEntity.getHistory().getCompetitionHistory()
                .stream()
                .filter(f -> f.getDisciplines() == null)
                .filter(CompetitionHistoryEntity::isWZSS)
                .filter(f -> f.getDate().getYear() == licenceYear)
                .filter(f -> f.getDiscipline().equals(Discipline.values()[1].getName()))
                .collect(Collectors.toList());
        List<CompetitionHistoryEntity> collectShotgun = memberEntity.getHistory().getCompetitionHistory()
                .stream()
                .filter(f -> f.getDisciplines() == null)
                .filter(CompetitionHistoryEntity::isWZSS)
                .filter(f -> f.getDate().getYear() == licenceYear)
                .filter(f -> f.getDiscipline().equals(Discipline.values()[2].getName()))
                .collect(Collectors.toList());

        List<CompetitionHistoryEntity> competitionHistory = memberEntity.getHistory().getCompetitionHistory();
        for (CompetitionHistoryEntity entity : competitionHistory) {
            if (entity.getDisciplines() != null) {
                if (entity.getDate().getYear() == licenceYear) {
                    String[] disciplines = entity.getDisciplines();
                    for (String s : disciplines) {
                        if (s.equals(Discipline.PISTOL.getName())) {
                            collectPistol.add(entity);
                        }
                        if (s.equals(Discipline.RIFLE.getName())) {
                            collectRifle.add(entity);
                        }
                        if (s.equals(Discipline.SHOTGUN.getName())) {
                            collectShotgun.add(entity);
                        }
                    }
                }
            }
        }


        if (memberEntity.getShootingPatent().getPistolPermission()) {
            pistol = collectPistol.size();
        }
        int rifle = 0;
        if (memberEntity.getShootingPatent().getRiflePermission()) {
            rifle = collectRifle.size();
        }
        int shotgun = 0;
        if (memberEntity.getShootingPatent().getShotgunPermission()) {
            shotgun = collectShotgun.size();
        }

        if (pistol >= 4) {
            if (rifle > 2) {
                rifle = 2;
            }
            if (shotgun > 2) {
                shotgun = 2;
            }
            pistol = 4;

        } else if (rifle >= 4) {
            if (pistol > 2) {
                pistol = 2;
            }
            if (shotgun > 2) {
                shotgun = 2;
            }
            rifle = 4;

        } else if (shotgun >= 4) {
            if (pistol > 2) {
                pistol = 2;
            }
            if (rifle > 2) {
                rifle = 2;
            }
            shotgun = 4;

        }

        Paragraph patentNumber = new Paragraph(memberEntity.getShootingPatent().getPatentNumber() + "                                                       " + licenseNumber, font(12, 0));

        patentNumber.setIndentationLeft(160);

        Paragraph newLine = new Paragraph("\n", font(7, 0));
        Paragraph pesel = new Paragraph(P[0] + "   " + P[1] + "   " + P[2] + "   " + P[3] + "   " + P[4] + "   " + P[5] + "  " + P[6] + "   " + P[7] + "   " + P[8] + "   " + P[9] + "   " + P[10] + "                                             " + phoneSplit, font(12, 0));
        Paragraph names = new Paragraph(name, font(12, 0));
        Paragraph year = new Paragraph(date, font(12, 1));
        pesel.setIndentationLeft(72);
        names.setIndentationLeft(150);
        year.setIndentationLeft(350);

        for (int i = 0; i < 12; i++) {

            document.add(newLine);
        }

        document.add(patentNumber);

        for (int i = 0; i < 1; i++) {

            document.add(newLine);
        }
        document.add(pesel);
        document.add(new Paragraph("\n", font(4, 0)));
        for (int i = 0; i < 1; i++) {

            document.add(newLine);
        }
        document.add(names);
        for (int i = 0; i < 5; i++) {

            document.add(newLine);
        }
        document.add(year);

        for (int i = 0; i < 3; i++) {

            document.add(newLine);
        }
        ClubEntity club = clubRepository.findById(1).orElseThrow(EntityNotFoundException::new);
        int fontSize = 10;
        float fixedHeight = 27F;
        document.add(new Phrase("\n", font(7, 0)));
        int counter = 0;
        for (int i = 0; i < pistol; i++) {
            float[] pointColumnWidths = {50, 20, 20, 5, 10, 2, 28};
            PdfPTable table = new PdfPTable(pointColumnWidths);
            counter++;
            PdfPCell cell = new PdfPCell(new Paragraph(collectPistol.get(i).getName() + "\n" + club.getName(), font(fontSize, 0)));
            PdfPCell cell1 = new PdfPCell(new Paragraph(" " + collectPistol.get(i).getDate().toString(), font(fontSize, 0)));
            PdfPCell cell2 = new PdfPCell(new Paragraph("Łódź", font(fontSize, 0)));
            PdfPCell cell3 = new PdfPCell(new Paragraph("X", font(fontSize, 1)));
            PdfPCell cell4 = new PdfPCell(new Paragraph(" ", font(fontSize, 0)));
            PdfPCell cell5 = new PdfPCell(new Paragraph(" ", font(fontSize, 0)));
            PdfPCell cell6 = new PdfPCell(new Paragraph("WZSS", font(fontSize, 0)));
            cell.setBorder(0);
            cell1.setBorder(0);
            cell2.setBorder(0);
            cell3.setBorder(0);
            cell4.setBorder(0);
            cell5.setBorder(0);
            cell6.setBorder(0);
            cell.setUseDescender(true);
            cell.setFixedHeight(fixedHeight);
            cell1.setFixedHeight(fixedHeight);
            cell2.setFixedHeight(fixedHeight);
            cell3.setFixedHeight(fixedHeight);
            cell4.setFixedHeight(fixedHeight);
            cell5.setFixedHeight(fixedHeight);
            cell6.setFixedHeight(fixedHeight);
            table.addCell(cell);
            table.addCell(cell1);
            table.addCell(cell2);
            table.addCell(cell3);
            table.addCell(cell4);
            table.addCell(cell5);
            table.addCell(cell6);
            document.add(table);
            if (counter == 4) {
                document.add(new Phrase("\n", font(3, 0)));
            }
        }
        for (int i = 0; i < rifle; i++) {
            float[] pointColumnWidths = {50, 20, 20, 5, 2, 10, 28};
            PdfPTable table = new PdfPTable(pointColumnWidths);
            counter++;

            PdfPCell cell = new PdfPCell(new Paragraph(collectRifle.get(i).getName() + "\n" + club.getName(), font(fontSize, 0)));
            PdfPCell cell1 = new PdfPCell(new Paragraph(" " + collectRifle.get(i).getDate().toString(), font(fontSize, 0)));
            PdfPCell cell2 = new PdfPCell(new Paragraph("Łódź", font(fontSize, 0)));
            PdfPCell cell3 = new PdfPCell(new Paragraph(" ", font(fontSize, 0)));
            PdfPCell cell4 = new PdfPCell(new Paragraph("X", font(fontSize, 1)));
            PdfPCell cell5 = new PdfPCell(new Paragraph(" ", font(fontSize, 0)));
            PdfPCell cell6 = new PdfPCell(new Paragraph("WZSS", font(fontSize, 0)));
            cell.setBorder(0);
            cell1.setBorder(0);
            cell2.setBorder(0);
            cell3.setBorder(0);
            cell4.setBorder(0);
            cell5.setBorder(0);
            cell6.setBorder(0);
            cell.setUseDescender(true);
            cell.setFixedHeight(fixedHeight);
            cell1.setFixedHeight(fixedHeight);
            cell2.setFixedHeight(fixedHeight);
            cell3.setFixedHeight(fixedHeight);
            cell4.setFixedHeight(fixedHeight);
            cell5.setFixedHeight(fixedHeight);
            cell6.setFixedHeight(fixedHeight);
            table.addCell(cell);
            table.addCell(cell1);
            table.addCell(cell2);
            table.addCell(cell3);
            table.addCell(cell4);
            table.addCell(cell5);
            table.addCell(cell6);
            document.add(table);
            if (counter == 4) {
                document.add(new Phrase("\n", font(3, 0)));
            }
        }
        for (int i = 0; i < shotgun; i++) {
            float[] pointColumnWidths = {50, 20, 20, 8, 3, 6, 28};
            PdfPTable table = new PdfPTable(pointColumnWidths);
            counter++;

            PdfPCell cell = new PdfPCell(new Paragraph(collectShotgun.get(i).getName() + "\n" + club.getName(), font(fontSize, 0)));
            PdfPCell cell1 = new PdfPCell(new Paragraph(" " + collectShotgun.get(i).getDate().toString(), font(fontSize, 0)));
            PdfPCell cell2 = new PdfPCell(new Paragraph("Łódź", font(fontSize, 0)));
            PdfPCell cell3 = new PdfPCell(new Paragraph(" ", font(fontSize, 0)));
            PdfPCell cell4 = new PdfPCell(new Paragraph(" ", font(fontSize, 0)));
            PdfPCell cell5 = new PdfPCell(new Paragraph("X", font(fontSize, 1)));
            PdfPCell cell6 = new PdfPCell(new Paragraph("WZSS", font(fontSize, 0)));
            cell.setBorder(0);
            cell1.setBorder(0);
            cell2.setBorder(0);
            cell3.setBorder(0);
            cell4.setBorder(0);
            cell5.setBorder(0);
            cell6.setBorder(0);
            cell.setUseDescender(true);
            cell.setFixedHeight(fixedHeight);
            cell1.setFixedHeight(fixedHeight);
            cell2.setFixedHeight(fixedHeight);
            cell3.setFixedHeight(fixedHeight);
            cell4.setFixedHeight(fixedHeight);
            cell5.setFixedHeight(fixedHeight);
            cell6.setFixedHeight(fixedHeight);
            table.addCell(cell);
            table.addCell(cell1);
            table.addCell(cell2);
            table.addCell(cell3);
            table.addCell(cell4);
            table.addCell(cell5);
            table.addCell(cell6);
            document.add(table);
            if (counter == 4) {
                document.add(new Phrase("\n", font(3, 0)));
            }
        }
        document.close();


        byte[] data = convertToByteArray(fileName);
        FilesModel filesModel = FilesModel.builder()
                .name(fileName)
                .data(data)
                .type(String.valueOf(MediaType.APPLICATION_PDF))
                .size(data.length)
                .build();

        FilesEntity filesEntity =
                createFileEntity(filesModel);

        File file = new File(fileName);

        file.delete();
        return filesEntity;

    }

    public FilesEntity createAnnouncementFromCompetition(String tournamentUUID) throws IOException, DocumentException {
        TournamentEntity tournamentEntity = tournamentRepository.findById(tournamentUUID).orElseThrow(EntityNotFoundException::new);
        ClubEntity c = clubRepository.getOne(1);

        String fileName = tournamentEntity.getDate().format(dateFormat()) + " " + c.getName() + " " + tournamentEntity.getName() + ".pdf";

        Document document = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(document,
                new FileOutputStream(fileName));
        writer.setPageEvent(new PageStamper());
        document.open();
        document.addTitle(fileName);
        document.addCreationDate();

        String hour = String.valueOf(LocalTime.now().getHour());
        String minute = String.valueOf(LocalTime.now().getMinute());
        if (Integer.parseInt(minute) < 10) {
            minute = "0" + minute;
        }

        String now = hour + ":" + minute;


        Paragraph title = new Paragraph(tournamentEntity.getName().toUpperCase() + "\n" + c.getName(), font(13, 1));
        Paragraph date = new Paragraph("Łódź, " + monthFormat(tournamentEntity.getDate()), font(10, 2));
        Paragraph newLine = new Paragraph("\n", font(10, 0));

        document.add(title);
        document.add(date);
        document.add(newLine);

        PdfPTable mainTable = new PdfPTable(1);
        mainTable.setWidthPercentage(100);


        for (int i = 0; i < tournamentEntity.getCompetitionsList().size(); i++) {
            if (!tournamentEntity.getCompetitionsList().get(i).getScoreList().isEmpty()) {
                CompetitionMembersListEntity competitionMembersListEntity = tournamentEntity.getCompetitionsList().get(i);

                List<ScoreEntity> scoreList = competitionMembersListEntity.getScoreList().stream().filter(f -> !f.isDsq()).filter(f -> !f.isDnf()).filter(f -> !f.isPk()).sorted(Comparator.comparing(ScoreEntity::getScore).reversed()).collect(Collectors.toList());

                List<ScoreEntity> collect = competitionMembersListEntity.getScoreList().stream().filter(f -> f.isDnf() || f.isDsq() || f.isPk()).collect(Collectors.toList());
                scoreList.addAll(collect);
                Paragraph competition = new Paragraph(competitionMembersListEntity.getName(), font(14, 1));
                competition.add("\n");
                document.add(competition);
                float[] pointColumnWidths = {25F, 150F, 125F, 50F, 25F, 25F};
                PdfPTable tableLabel = new PdfPTable(pointColumnWidths);
                String p1 = "10 x", p2 = "10 /";
                if (competitionMembersListEntity.getCountingMethod() != null) {
                    if (competitionMembersListEntity.getCountingMethod().equals(CountingMethod.COMSTOCK.getName())) {
                        p1 = "";
                        p2 = "";
                    } else {
                        p1 = "10 x";
                        p2 = "10 /";
                    }
                }
                PdfPCell cellLabel = new PdfPCell(new Paragraph("M-ce", font(10, 1)));
                PdfPCell cellLabel1 = new PdfPCell(new Paragraph("Imię i Nazwisko", font(10, 1)));
                PdfPCell cellLabel2 = new PdfPCell(new Paragraph("Klub", font(10, 1)));
                PdfPCell cellLabel3 = new PdfPCell(new Paragraph("Wynik", font(10, 1)));
                PdfPCell cellLabel4 = new PdfPCell(new Paragraph(p1, font(10, 1)));
                PdfPCell cellLabel5 = new PdfPCell(new Paragraph(p2, font(10, 1)));

                document.add(newLine);

                cellLabel.setHorizontalAlignment(Element.ALIGN_CENTER);
                cellLabel1.setHorizontalAlignment(Element.ALIGN_CENTER);
                cellLabel2.setHorizontalAlignment(Element.ALIGN_CENTER);
                cellLabel3.setHorizontalAlignment(Element.ALIGN_CENTER);
                cellLabel4.setHorizontalAlignment(Element.ALIGN_CENTER);
                cellLabel5.setHorizontalAlignment(Element.ALIGN_CENTER);

                cellLabel.setBorder(0);
                cellLabel1.setBorder(0);
                cellLabel2.setBorder(0);
                cellLabel3.setBorder(0);
                cellLabel4.setBorder(0);
                cellLabel5.setBorder(0);

                tableLabel.setWidthPercentage(100F);
                tableLabel.addCell(cellLabel);
                tableLabel.addCell(cellLabel1);
                tableLabel.addCell(cellLabel2);
                tableLabel.addCell(cellLabel3);
                tableLabel.addCell(cellLabel4);
                tableLabel.addCell(cellLabel5);

                document.add(tableLabel);


                for (int j = 0; j < scoreList.size(); j++) {

                    String secondName;
                    String firstName;
                    String club;
                    if (scoreList.get(j).getMember() != null) {
                        secondName = scoreList.get(j).getMember().getSecondName();
                        firstName = scoreList.get(j).getMember().getFirstName();
                        club = scoreList.get(j).getMember().getClub().getName();

                    } else {
                        secondName = scoreList.get(j).getOtherPersonEntity().getSecondName();
                        firstName = scoreList.get(j).getOtherPersonEntity().getFirstName();
                        club = scoreList.get(j).getOtherPersonEntity().getClub().getName();

                    }
                    float score = scoreList.get(j).getScore();
                    String scoreOuterTen = String.valueOf(scoreList.get(j).getOuterTen() - scoreList.get(j).getInnerTen());
                    String scoreInnerTen = String.valueOf(scoreList.get(j).getInnerTen());
                    if (scoreOuterTen.startsWith("0")) {
                        scoreOuterTen = "";
                    }
                    if (scoreInnerTen.startsWith("0")) {
                        scoreInnerTen = "";
                    }
                    String o1 = scoreInnerTen.replace(".0", ""), o2 = scoreOuterTen.replace(".0", "");
                    if (scoreList.get(j).getInnerTen() == 0) {
                        o1 = scoreInnerTen = "";
                    }
                    if (scoreList.get(j).getOuterTen() == 0) {
                        o2 = scoreOuterTen = "";
                    }
                    DecimalFormat myFormatter = new DecimalFormat("###.####");
                    String result = myFormatter.format(score);
                    if (score == 100) {
                        result = "100,0000";
                    }
                    if (scoreList.get(j).isDnf()) {
                        result = "DNF";
                    }
                    if (scoreList.get(j).isDsq()) {
                        result = "DSQ";
                    }
                    if (scoreList.get(j).isPk()) {
                        result = "PK";
                    }
                    if (competitionMembersListEntity.getCountingMethod() != null) {

                        if (competitionMembersListEntity.getCountingMethod().equals(CountingMethod.COMSTOCK.getName())) {
                            o1 = "";
                            o2 = "";

                        } else {
                            o1 = scoreInnerTen.replace(".0", "");
                            o2 = scoreOuterTen.replace(".0", "");
//                            result = result.replace(".0", "");

                        }
                    }
                    PdfPTable playerTableLabel = new PdfPTable(pointColumnWidths);
                    PdfPCell playerCellLabel = new PdfPCell(new Paragraph(String.valueOf(j + 1), font(11, 0)));
                    PdfPCell playerCellLabel1 = new PdfPCell(new Paragraph(secondName + " " + firstName, font(11, 0)));
                    PdfPCell playerCellLabel2 = new PdfPCell(new Paragraph(club, font(11, 0)));
                    PdfPCell playerCellLabel3 = new PdfPCell(new Paragraph(result, font(11, 1)));
                    PdfPCell playerCellLabel4 = new PdfPCell(new Paragraph(o1, font(9, 2)));
                    PdfPCell playerCellLabel5 = new PdfPCell(new Paragraph(o2, font(9, 2)));


                    playerCellLabel.setBorder(0);
                    playerCellLabel1.setBorder(0);
                    playerCellLabel2.setBorder(0);
                    playerCellLabel3.setBorder(0);
                    playerCellLabel4.setBorder(0);
                    playerCellLabel5.setBorder(0);

                    playerCellLabel.setHorizontalAlignment(Element.ALIGN_CENTER);
                    playerCellLabel1.setHorizontalAlignment(Element.ALIGN_LEFT);
                    playerCellLabel2.setHorizontalAlignment(Element.ALIGN_LEFT);
                    playerCellLabel3.setHorizontalAlignment(Element.ALIGN_CENTER);
                    playerCellLabel4.setHorizontalAlignment(Element.ALIGN_CENTER);
                    playerCellLabel5.setHorizontalAlignment(Element.ALIGN_CENTER);


                    playerTableLabel.setWidthPercentage(100F);

                    playerTableLabel.addCell(playerCellLabel);
                    playerTableLabel.addCell(playerCellLabel1);
                    playerTableLabel.addCell(playerCellLabel2);
                    playerTableLabel.addCell(playerCellLabel3);
                    playerTableLabel.addCell(playerCellLabel4);
                    playerTableLabel.addCell(playerCellLabel5);

                    document.add(playerTableLabel);

                }
            }
        }
        float[] pointColumnWidths = {220F, 240F, 240F};
        String mainArbiter;
        String mainArbiterClass;
        if (tournamentEntity.getMainArbiter() != null) {
            mainArbiter = tournamentEntity.getMainArbiter().getFirstName() + " " + tournamentEntity.getMainArbiter().getSecondName();
            mainArbiterClass = tournamentEntity.getMainArbiter().getMemberPermissions().getArbiterClass();
            mainArbiterClass = getArbiterClass(mainArbiterClass);
        } else {
            if (tournamentEntity.getOtherMainArbiter() != null) {
                mainArbiter = tournamentEntity.getOtherMainArbiter().getFirstName() + " " + tournamentEntity.getOtherMainArbiter().getSecondName();
                mainArbiterClass = tournamentEntity.getOtherMainArbiter().getPermissionsEntity().getArbiterClass();
                mainArbiterClass = getArbiterClass(mainArbiterClass);
            } else {
                mainArbiter = "Nie Wskazano";
                mainArbiterClass = "";
            }
        }

        String arbiterRTS;
        String arbiterRTSClass;
        if (tournamentEntity.getCommissionRTSArbiter() != null) {
            arbiterRTS = tournamentEntity.getCommissionRTSArbiter().getFirstName() + " " + tournamentEntity.getCommissionRTSArbiter().getSecondName();
            arbiterRTSClass = tournamentEntity.getCommissionRTSArbiter().getMemberPermissions().getArbiterClass();
            arbiterRTSClass = getArbiterClass(arbiterRTSClass);
        } else {
            if (tournamentEntity.getOtherCommissionRTSArbiter() != null) {
                arbiterRTS = tournamentEntity.getOtherCommissionRTSArbiter().getFirstName() + " " + tournamentEntity.getOtherCommissionRTSArbiter().getSecondName();
                arbiterRTSClass = tournamentEntity.getOtherCommissionRTSArbiter().getPermissionsEntity().getArbiterClass();
                arbiterRTSClass = getArbiterClass(arbiterRTSClass);
            } else {
                arbiterRTS = "Nie Wskazano";
                arbiterRTSClass = "";
            }
        }


        PdfPTable arbiterTableLabel = new PdfPTable(pointColumnWidths);
        PdfPCell arbiterCellLabel1 = new PdfPCell(new Paragraph("Sędzia Główny \n" + mainArbiter + "\n" + mainArbiterClass, font(12, 0)));
        PdfPCell arbiterCellLabel2 = new PdfPCell(new Paragraph(" ", font(10, 0)));
        PdfPCell arbiterCellLabel3 = new PdfPCell(new Paragraph("Przewodniczący Komisji RTS \n" + arbiterRTS + "\n" + arbiterRTSClass, font(12, 0)));

        arbiterCellLabel1.setHorizontalAlignment(Element.ALIGN_CENTER);
        arbiterCellLabel2.setHorizontalAlignment(Element.ALIGN_CENTER);
        arbiterCellLabel3.setHorizontalAlignment(Element.ALIGN_CENTER);

        arbiterCellLabel1.setBorder(0);
        arbiterCellLabel2.setBorder(0);
        arbiterCellLabel3.setBorder(0);

        arbiterTableLabel.setWidthPercentage(100F);

        arbiterTableLabel.addCell(arbiterCellLabel1);
        arbiterTableLabel.addCell(arbiterCellLabel2);
        arbiterTableLabel.addCell(arbiterCellLabel3);


        document.add(newLine);
        document.add(newLine);

        Paragraph state = new Paragraph("Zawody odbyły się zgodnie z przepisami bezpieczeństwa i regulaminem zawodów,", font(10, 0));
        Paragraph state1 = new Paragraph("oraz liczba sklasyfikowanych zawodników", font(10, 0));
        Paragraph state2 = new Paragraph("była zgodna ze stanem faktycznym.", font(10, 0));
        state.setAlignment(0);
        state1.setAlignment(0);
        state2.setAlignment(0);
        if (!tournamentEntity.isOpen()) {
            document.add(state);
            document.add(state1);
            document.add(state2);
            document.add(newLine);
        }
        document.add(arbiterTableLabel);
        if (tournamentEntity.isOpen()) {
            document.add(new Paragraph("Sporządzono " + now, font(14, 0)));
        }
        document.close();


        byte[] data = convertToByteArray(fileName);
        FilesModel filesModel = FilesModel.builder()
                .name(fileName)
                .data(data)
                .type(String.valueOf(MediaType.APPLICATION_PDF))
                .size(data.length)
                .build();

        FilesEntity filesEntity =
                createFileEntity(filesModel);

        File file = new File(fileName);

        file.delete();
        return filesEntity;
    }

    public FilesEntity CertificateOfClubMembership(String memberUUID, String reason) throws IOException, DocumentException {
        MemberEntity memberEntity = memberRepository.findById(memberUUID).orElseThrow(EntityNotFoundException::new);
        ClubEntity club = clubRepository.getOne(1);
        String fileName = reason + " " + memberEntity.getFirstName().trim().concat(" " + memberEntity.getSecondName().trim()) + ".pdf";

        Document document = new Document(PageSize.A4);
        FileOutputStream fileOutputStream = new FileOutputStream(fileName);
        PdfWriter writer = PdfWriter.getInstance(document,
                new FileOutputStream(fileName));
        writer.setPageEvent(new PageStamper());
        document.open();
        document.addTitle(fileName);
        document.addCreationDate();

        Paragraph newLine = new Paragraph("\n", font(12, 0));

        PdfPTable mainTable = new PdfPTable(1);

        document.add(mainTable);
        String[] choice = {"ZAŚWIADCZENIE ZWYKŁE", "ZAŚWIADCZENIE DO POLICJI"};
        // pobieranie z ustawień
        String policeAddress = "";
        if (reason.equals(choice[1])) {
            policeAddress = "\nKomendant Wojewódzki Policji w Łodzi\nWydział Postępowań Administracyjnych\n 90-144 Łódź, ul. Sienkiewicza 26";
        }

        Paragraph date = new Paragraph("Łódź, " + LocalDate.now().format(dateFormat()), font(12, 0));
        date.setAlignment(2);
        Paragraph police = new Paragraph(policeAddress, font(12, 0));
        police.setAlignment(2);
        document.add(date);
        document.add(police);

        Paragraph title = new Paragraph("\n\nZaświadczenie\n\n", font(14, 1));
        title.setAlignment(1);
        String pesel = "";
        if (reason.equals(choice[1])) {
            pesel = " PESEL: " + memberEntity.getPesel();
        }
        String sex, word = "";
        if (getSex(memberEntity.getPesel()).equals("Pani")) {
            sex = "Pani";
            word = "wystąpiła";
        } else {
            sex = "Pan";
            word = "wystąpił";
        }
        Paragraph par1 = new Paragraph("" + sex + " " + memberEntity.getFirstName().concat(" " + memberEntity.getSecondName()) + pesel + " jest czynnym członkiem Klubu Strzeleckiego „Dziesiątka” LOK w Łodzi. Numer legitymacji klubowej: " + memberEntity.getLegitimationNumber() + ". " +
                "Uczestniczy w zawodach i treningach strzeleckich osiągając bardzo dobre wyniki. " +
                "Czynnie uczestniczy w życiu Klubu. ", font(12, 0));
        par1.setFirstLineIndent(40);

        Paragraph par2 = new Paragraph(getSex(memberEntity.getPesel()) + " " + memberEntity.getFirstName().concat(" " + memberEntity.getSecondName()) + " wyraża chęć pogłębiania swojej wiedzy i umiejętności w sporcie strzeleckim przez współzawodnictwo w różnych konkurencjach strzeleckich.", font(12, 0));
        par2.setFirstLineIndent(40);
        String s = "";
        if (memberEntity.getLicense().getValidThru() != null) {
            s = String.valueOf(memberEntity.getLicense().getValidThru().getYear());
        }
        Paragraph par3 = new Paragraph(getSex(memberEntity.getPesel()) + " " + memberEntity.getFirstName().concat(" " + memberEntity.getSecondName()) + " posiada Patent Strzelecki PZSS oraz ważną Licencję Zawodniczą PZSS na rok " + s, font(12, 0));
        par3.setFirstLineIndent(40);

        Paragraph par4 = new Paragraph(club.getFullName() + " jest członkiem Polskiego Związku Strzelectwa Sportowego i posiada Licencję Klubową nr LK-" + club.getLicenseNumber() + ", jest również Członkiem Łódzkiego Związku Strzelectwa Sportowego, zarejestrowany pod numerem ewidencyjnym 6.", font(12, 0));
        par4.setFirstLineIndent(40);

        Paragraph par5 = new Paragraph(getSex(memberEntity.getPesel()) + " " + memberEntity.getFirstName().concat(" " + memberEntity.getSecondName()) + " " + word + " z prośbą o wydanie niniejszego zaświadczenia, skutkiem którego będzie złożenie wniosku o pozwolenie na broń sportową do celów sportowych. ", font(12, 0));
        par5.setFirstLineIndent(40);

        Paragraph par6 = new Paragraph("Sporządzono w 2 egz.", font(12, 0));

        Paragraph par7 = new Paragraph("Egz. Nr 1 – a/a\n" +
                "Egz. Nr 2 – Adresat", font(12, 0));
        par7.setIndentationLeft(40);
        document.add(title);
        document.add(par1);
        document.add(par2);
        if (memberEntity.getLicense().getNumber() != null && memberEntity.getMemberPermissions().getArbiterNumber() != null) {
            document.add(par3);
        }
        if (reason.equals(choice[1])) {
            document.add(par4);
            document.add(par5);
        }
        document.add(newLine);
        document.add(newLine);
        document.add(newLine);
        document.add(par6);
        document.add(newLine);
        document.add(newLine);
        document.add(par7);

        document.close();


        byte[] data = convertToByteArray(fileName);
        FilesModel filesModel = FilesModel.builder()
                .name(fileName)
                .data(data)
                .type(String.valueOf(MediaType.APPLICATION_PDF))
                .size(data.length)
                .build();

        FilesEntity filesEntity =
                createFileEntity(filesModel);

        File file = new File(fileName);

        file.delete();
        return filesEntity;
    }

    public FilesEntity getMemberCSVFile(String memberUUID) throws IOException {
        MemberEntity memberEntity = memberRepository.findById(memberUUID).orElseThrow(EntityNotFoundException::new);
        String fileName = memberEntity.getFirstName() + memberEntity.getSecondName().toUpperCase().trim() + ".csv";
        File file = new File(fileName);

        String[] tab = new String[5];

        LocalDate localDate = birthDay(memberEntity.getPesel());
        String monthValue = String.valueOf(localDate.getMonthValue());
        if (Integer.parseInt(monthValue) < 10) {
            monthValue = "0" + monthValue;
        }
        String dayOfMonth = String.valueOf(localDate.getDayOfMonth());
        if (Integer.parseInt(dayOfMonth) < 10) {
            dayOfMonth = "0" + dayOfMonth;
        }
        String date = localDate.getYear() + "-" + monthValue + "-" + dayOfMonth;
        tab[0] = memberEntity.getPesel();
        tab[1] = memberEntity.getFirstName().trim();
        tab[2] = memberEntity.getSecondName().trim();
        tab[3] = date;
        tab[4] = memberEntity.getEmail();

        FileWriter fileWriter = new FileWriter(fileName);
        System.out.println(fileWriter.getEncoding());
        String coma = ";";
        for (String s : tab) {

            fileWriter.write(s + coma);
        }

        fileWriter.close();


        byte[] data = convertToByteArray(fileName);
        FilesModel filesModel = FilesModel.builder()
                .name(fileName)
                .data(data)
                .type(String.valueOf(MediaType.TEXT_XML))
                .size(data.length)
                .build();

        FilesEntity filesEntity =
                createFileEntity(filesModel);


        file.delete();
        return filesEntity;
    }

    public FilesEntity getStartsMetric(String memberUUID, String otherID, String tournamentUUID, List<String> competitions, String startNumber) throws IOException, DocumentException {
        String name;
        String club;

        if (otherID != null) {
            OtherPersonEntity otherPersonEntity = otherPersonRepository.findById(Integer.valueOf(otherID)).orElseThrow(EntityNotFoundException::new);
            name = otherPersonEntity.getSecondName().concat(otherPersonEntity.getFirstName());
            club = otherPersonEntity.getClub().getName();
        } else {
            MemberEntity memberEntity = memberRepository.findById(memberUUID).orElseThrow(EntityNotFoundException::new);
            name = memberEntity.getSecondName().concat(memberEntity.getFirstName());
            club = memberEntity.getClub().getName();
        }
        TournamentEntity tournamentEntity = tournamentRepository.findById(tournamentUUID).orElseThrow(EntityNotFoundException::new);

        ClubEntity clubEntity = clubRepository.findById(1).orElseThrow(EntityNotFoundException::new);

        String fileName = "metryki_" + name + ".pdf";

        Document document = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(document,
                new FileOutputStream(fileName));
        document.open();
        document.addTitle(fileName);
        document.addCreationDate();

        List<String> comp = competitions.stream().filter(value -> !value.contains(" pneumatyczny ") && !value.contains(" pneumatyczna ")).sorted().collect(Collectors.toList());
        competitions.stream().filter(competition -> competition.contains("pneumatyczny") || competition.contains(" pneumatyczna ")).sorted().forEach(comp::add);
        Paragraph newLine = new Paragraph("\n", font(11, 0));
        for (int j = 0; j < comp.size(); j++) {

            int d = Integer.parseInt(startNumber);

            int finalJ = j;

            ScoreEntity score = tournamentEntity.getCompetitionsList()
                    .stream()
                    .filter(f -> f.getName().equals(comp.get(finalJ)))
                    .findFirst().orElseThrow(EntityNotFoundException::new)
                    .getScoreList()
                    .stream()
                    .filter(f -> f.getMetricNumber() == d)
                    .findFirst().orElseThrow(EntityNotFoundException::new);

            CompetitionEntity competitionEntity = competitionRepository.findAll().stream().filter(f -> f.getName().equals(comp.get(finalJ))).findFirst().orElseThrow(EntityNotFoundException::new);
            int compShots = competitionEntity.getNumberOfShots();
            int numberOfShots;
            if (competitionEntity.getNumberOfShots() > 10) {
                numberOfShots = 10;
            } else {
                numberOfShots = competitionEntity.getNumberOfShots();
            }

            Paragraph par1 = new Paragraph(tournamentEntity.getName().toUpperCase() + " " + clubEntity.getName(), font(12, 1));
            par1.setAlignment(1);
            Paragraph par2 = new Paragraph(name.toUpperCase(), font(13, 1));
            par2.setAlignment(1);
            String a = "";
            String b = "";
            if (score.isAmmunition()) {
                a = "A";
                b = "";
            }
            if (!score.isAmmunition() && score.isGun()) {
                b = "B";
            }
            Chunk clubChunk = new Chunk(club, font(10, 0));
            par2.add(clubChunk);
            Chunk chunk = new Chunk("                            " + a + " " + b + "  Nr. " + startNumber, font(18, 1));
            par2.add(chunk);

            Paragraph par3 = new Paragraph(comp.get(j), font(12, 1));
            par3.setAlignment(1);

            Paragraph par4 = new Paragraph("Podpis sędziego .............................", font(12, 0));
            Chunk chunk1 = new Chunk("                                   Podpis zawodnika .............................       ", font(12, 0));
            Chunk chunk2 = new Chunk(" Nr. " + startNumber, font(18, 1));

            par4.add(chunk1);
            par4.add(chunk2);

            float[] pointColumnWidths = new float[numberOfShots + 1];
            if (competitionEntity.getCountingMethod().equals(CountingMethod.COMSTOCK.getName())) {
                pointColumnWidths = new float[6];
            }
            if (!competitionEntity.getCountingMethod().equals(CountingMethod.COMSTOCK.getName())) {
                for (int i = 0; i <= numberOfShots; i++) {
                    if (i < pointColumnWidths.length - 1) {
                        pointColumnWidths[i] = 25F;
                    } else {
                        pointColumnWidths[i] = 80F;
                    }
                }
            } else {
                Arrays.fill(pointColumnWidths, 25F);
            }
            PdfPTable table = new PdfPTable(pointColumnWidths);
            PdfPTable table1 = new PdfPTable(pointColumnWidths);
            PdfPTable table2 = new PdfPTable(pointColumnWidths);

            table.setWidthPercentage(100F);
            table1.setWidthPercentage(100F);
            table2.setWidthPercentage(100F);

            document.add(par1);
            document.add(par2);
            document.add(par3);
            document.add(newLine);
            if (!competitionEntity.getCountingMethod().equals(CountingMethod.COMSTOCK.getName())) {
                for (int i = 0; i <= numberOfShots; i++) {
                    Paragraph p;
                    if (i < numberOfShots) {
                        p = new Paragraph(String.valueOf(i + 1), font(14, 0));
                    } else {
                        if (competitionEntity.getCountingMethod() != null && competitionEntity.getCountingMethod().equals(CountingMethod.COMSTOCK.getName())) {
                            p = new Paragraph("CZAS / PROCEDURY", font(14, 1));
                        } else {
                            p = new Paragraph("SUMA", font(14, 1));
                        }
                    }
                    PdfPCell cell = new PdfPCell(p);
                    cell.setHorizontalAlignment(1);
                    table.addCell(cell);
                    if (i >= 10) {
                        document.add(table);
                        break;
                    }
                }
                if (numberOfShots < 10) {
                    document.add(table);

                }
                int loopLength = competitionEntity.getNumberOfShots() + 1,
                        secondLoopLength = 0;
                for (int i = 0; i <= loopLength; i++) {
                    String s = " ";
                    Chunk c = new Chunk(s, font(28, 0));
                    Paragraph p = new Paragraph(c);
                    PdfPCell cell = new PdfPCell(p);
                    table1.addCell(cell);
                    if (i == loopLength) {
                        for (int k = i; k > 0; k--) {
                            secondLoopLength++;
                            if (k % 10 == 1) {
                                break;
                            }
                        }
                    }
                }
                document.add(table1);
                if (compShots > 10) {
                    for (int i = 0; i <= 10; i++) {
                        String s = " ";
                        Chunk c = new Chunk(s, font(28, 0));
                        Paragraph p = new Paragraph(c);
                        PdfPCell cell = new PdfPCell(p);

                        table2.addCell(cell);
                    }
                    document.add(table2);

                }
            } else {
                for (int i = 0; i < pointColumnWidths.length; i++) {
                    Paragraph p = new Paragraph();
                    if (i == 0) {
                        p = new Paragraph("ALFA", font(12, 0));
                    }
                    if (i == 1) {
                        p = new Paragraph("CHARLIE", font(12, 0));
                    }
                    if (i == 2) {
                        p = new Paragraph("DELTA", font(12, 0));
                    }
                    if (i == 3) {
                        p = new Paragraph("PROCEDURY", font(12, 0));
                    }
                    if (i == 4) {
                        p = new Paragraph("CZAS", font(12, 0));
                    }
                    if (i == 5) {
                        p = new Paragraph("UWAGI", font(12, 0));
                    }
                    PdfPCell cell = new PdfPCell(p);
                    cell.setHorizontalAlignment(1);
                    table.addCell(cell);
                }
                document.add(table);
                for (int i = 0; i < pointColumnWidths.length; i++) {
                    Paragraph p = new Paragraph();
                    if (i == 0) {
                        p = new Paragraph(" ", font(28, 0));
                    }
                    if (i == 1) {
                        p = new Paragraph(" ", font(28, 0));
                    }
                    if (i == 2) {
                        p = new Paragraph(" ", font(28, 0));
                    }
                    if (i == 3) {
                        p = new Paragraph(" ", font(28, 0));
                    }
                    if (i == 4) {
                        p = new Paragraph(" ", font(28, 0));
                    }
                    if (i == 5) {
                        p = new Paragraph(" ", font(28, 0));
                    }
                    PdfPCell cell = new PdfPCell(p);
                    cell.setHorizontalAlignment(1);
                    table1.addCell(cell);
                }
                document.add(table1);
            }
            Paragraph par5 = new Paragraph("_______________________________________________________________________________________", font(12, 0));

            document.add(newLine);
            document.add(par4);
            if (j < 7) {
                document.add(par5);
                document.add(newLine);
            }
        }

        document.close();


        byte[] data = convertToByteArray(fileName);
        FilesModel filesModel = FilesModel.builder()
                .name(fileName)
                .data(data)
                .type(String.valueOf(MediaType.APPLICATION_PDF))
                .size(data.length)
                .build();

        FilesEntity filesEntity =
                createFileEntity(filesModel);

        File file = new File(fileName);

        file.delete();
        return filesEntity;
    }

    public FilesEntity generateMembersListWithCondition(boolean condition) throws IOException, DocumentException {

        String fileName = "Lista_klubowiczów_na_dzień " + LocalDate.now().format(dateFormat()) + ".pdf";

        Document document = new Document(PageSize.A4.rotate());
        PdfWriter writer = PdfWriter.getInstance(document,
                new FileOutputStream(fileName));
        document.open();
        document.addTitle(fileName);
        document.addCreationDate();

        List<MemberEntity> all = memberRepository.findAll().stream().filter(f -> !f.getErased()).filter(f -> f.getAdult().equals(condition)).sorted(Comparator.comparing(MemberEntity::getSecondName)).collect(Collectors.toList());

        String hour = String.valueOf(LocalTime.now().getHour());
        String minute = String.valueOf(LocalTime.now().getMinute());
        if (Integer.parseInt(minute) < 10) {
            minute = "0" + minute;
        }

        String now = hour + ":" + minute;

        Paragraph title = new Paragraph("Lista klubowiczów na dzień " + LocalDate.now().format(dateFormat()) + " " + now, font(14, 1));
        Paragraph newLine = new Paragraph("\n", font(14, 0));
        document.add(title);
        document.add(newLine);

        float[] pointColumnWidths = {4F, 58F, 10F, 12F, 12F, 12F};


        PdfPTable titleTable = new PdfPTable(pointColumnWidths);

        titleTable.setWidthPercentage(100);

        PdfPCell lp = new PdfPCell(new Paragraph("lp", font(12, 0)));
        PdfPCell name = new PdfPCell(new Paragraph("Nazwisko Imię", font(12, 0)));
        PdfPCell LegNumber = new PdfPCell(new Paragraph("legitymacja", font(12, 0)));
        PdfPCell inDate = new PdfPCell(new Paragraph("Data zapisu", font(12, 0)));
        PdfPCell contributionDate = new PdfPCell(new Paragraph("Data opłacenia składki", font(12, 0)));
        PdfPCell contributionValidThru = new PdfPCell(new Paragraph("Składka ważna do", font(12, 0)));

        titleTable.addCell(lp);
        titleTable.addCell(name);
        titleTable.addCell(LegNumber);
        titleTable.addCell(inDate);
        titleTable.addCell(contributionDate);
        titleTable.addCell(contributionValidThru);

        document.add(titleTable);
        document.add(newLine);

        for (int i = 0; i < all.size(); i++) {
            MemberEntity memberEntity = all.get(i);
            PdfPTable memberTable = new PdfPTable(pointColumnWidths);

            memberTable.setWidthPercentage(100);

            PdfPCell lpCell = new PdfPCell(new Paragraph(String.valueOf(i + 1), font(12, 0)));
            PdfPCell nameCell = new PdfPCell(new Paragraph(memberEntity.getSecondName().concat(" " + memberEntity.getFirstName()), font(12, 0)));
            PdfPCell legNumberCell = new PdfPCell(new Paragraph(String.valueOf(memberEntity.getLegitimationNumber()), font(12, 0)));
            PdfPCell inDateCell = new PdfPCell(new Paragraph(String.valueOf(memberEntity.getJoinDate()), font(12, 0)));
            PdfPCell contributionDateCell;
            PdfPCell contributionValidThruCell;
            if (memberEntity.getHistory().getContributionList().size() > 0) {

                contributionDateCell = new PdfPCell(new Paragraph(String.valueOf(memberEntity.getHistory().getContributionList().get(0).getPaymentDay()), font(12, 0)));
                contributionValidThruCell = new PdfPCell(new Paragraph(String.valueOf(memberEntity.getHistory().getContributionList().get(0).getValidThru()), font(12, 0)));

            } else {

                contributionDateCell = new PdfPCell(new Paragraph("BRAK SKŁADEK", font(12, 1)));
                contributionValidThruCell = new PdfPCell(new Paragraph("BRAK SKŁADEK", font(12, 1)));

            }

            memberTable.addCell(lpCell);
            memberTable.addCell(nameCell);
            memberTable.addCell(legNumberCell);
            memberTable.addCell(inDateCell);
            memberTable.addCell(contributionDateCell);
            memberTable.addCell(contributionValidThruCell);

            document.add(memberTable);
        }


        document.close();


        byte[] data = convertToByteArray(fileName);
        FilesModel filesModel = FilesModel.builder()
                .name(fileName)
                .data(data)
                .type(String.valueOf(MediaType.APPLICATION_PDF))
                .size(data.length)
                .build();

        FilesEntity filesEntity =
                createFileEntity(filesModel);

        File file = new File(fileName);

        file.delete();
        return filesEntity;
    }

    public FilesEntity generateAllMembersList() throws IOException, DocumentException {

        String fileName = "Lista_obecności_klubowiczów " + LocalDate.now().format(dateFormat()) + ".pdf";

        Document document = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(document,
                new FileOutputStream(fileName));
        writer.setPageEvent(new PageStamper());

        document.open();
        document.addTitle(fileName);
        document.addCreationDate();

        List<MemberEntity> all = memberRepository.findAll().stream().filter(f -> !f.getErased()).sorted(Comparator.comparing(MemberEntity::getSecondName)).collect(Collectors.toList());

        String hour = String.valueOf(LocalTime.now().getHour());
        String minute = String.valueOf(LocalTime.now().getMinute());
        if (Integer.parseInt(minute) < 10) {
            minute = "0" + minute;
        }

        String now = hour + ":" + minute;

        Paragraph title = new Paragraph("Lista obecności klubowiczów na dzień " + LocalDate.now().format(dateFormat()), font(14, 1));
        Paragraph newLine = new Paragraph("\n", font(14, 0));
        document.add(title);
        document.add(newLine);

        float[] pointColumnWidths = {3F, 25F, 15F, 20F};


        PdfPTable titleTable = new PdfPTable(pointColumnWidths);

        titleTable.setWidthPercentage(100);
        Paragraph lp1 = new Paragraph("lp", font(12, 0));
        PdfPCell lp = new PdfPCell(lp1);
        Paragraph name1 = new Paragraph("Nazwisko Imię", font(12, 0));
        PdfPCell name = new PdfPCell(name1);
        Paragraph legNumber1 = new Paragraph("Legitymacja", font(12, 0));
        PdfPCell legNumber = new PdfPCell(legNumber1);
        Paragraph signature1 = new Paragraph("Podpis", font(12, 0));
        PdfPCell signature = new PdfPCell(signature1);

        lp.setHorizontalAlignment(1);
        name.setHorizontalAlignment(1);
        legNumber.setHorizontalAlignment(1);
        signature.setHorizontalAlignment(1);

        titleTable.addCell(lp);
        titleTable.addCell(name);
        titleTable.addCell(legNumber);
        titleTable.addCell(signature);

        document.add(titleTable);
        document.add(newLine);

        for (int i = 0; i < all.size(); i++) {
            MemberEntity memberEntity = all.get(i);
            PdfPTable memberTable = new PdfPTable(pointColumnWidths);

            memberTable.setWidthPercentage(100);

            PdfPCell lpCell = new PdfPCell(new Paragraph(String.valueOf(i + 1), font(12, 0)));
            PdfPCell nameCell = new PdfPCell(new Paragraph(memberEntity.getSecondName().concat(" " + memberEntity.getFirstName()), font(12, 0)));
            PdfPCell legNumberCell = new PdfPCell(new Paragraph(String.valueOf(memberEntity.getLegitimationNumber()), font(12, 0)));
            PdfPCell signatureCell = new PdfPCell(new Paragraph(" ", font(12, 0)));

            lpCell.setHorizontalAlignment(1);
            legNumberCell.setHorizontalAlignment(1);

            memberTable.addCell(lpCell);
            memberTable.addCell(nameCell);
            memberTable.addCell(legNumberCell);
            memberTable.addCell(signatureCell);

            document.add(memberTable);
        }


        document.close();


        byte[] data = convertToByteArray(fileName);
        FilesModel filesModel = FilesModel.builder()
                .name(fileName)
                .data(data)
                .type(String.valueOf(MediaType.APPLICATION_PDF))
                .size(data.length)
                .build();

        FilesEntity filesEntity =
                createFileEntity(filesModel);

        File file = new File(fileName);

        file.delete();
        return filesEntity;
    }

    public FilesEntity getJudge(String tournamentUUID) throws IOException, DocumentException {

        TournamentEntity tournamentEntity = tournamentRepository.findById(tournamentUUID).orElseThrow(EntityNotFoundException::new);

        String fileName = "Lista_sędziów_na_zawodach_" + tournamentEntity.getName() + ".pdf";

        Document document = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(document,
                new FileOutputStream(fileName));
        writer.setPageEvent(new PageStamper());
        document.open();
        document.addTitle(fileName);
        document.addCreationDate();

        Paragraph title = new Paragraph(tournamentEntity.getName().toUpperCase() + "\n" + "„DZIESIĄTKA” ŁÓDŹ", font(13, 1));
        Paragraph date = new Paragraph("Łódź, " + monthFormat(tournamentEntity.getDate()), font(10, 2));

        Paragraph listTitle = new Paragraph("WYKAZ SĘDZIÓW", font(13, 0));
        Paragraph newLine = new Paragraph("\n", font(13, 0));

        Paragraph subTitle = new Paragraph("\nSędzia Główny", font(13, 0));
        Paragraph subTitle1 = new Paragraph("\nPrzewodniczący Komisji RTS", font(13, 0));

        String mainArbiter;
        String mainArbiterClass;

        if (tournamentEntity.getMainArbiter() != null) {
            mainArbiter = tournamentEntity.getMainArbiter().getFirstName() + " " + tournamentEntity.getMainArbiter().getSecondName();
            mainArbiterClass = tournamentEntity.getMainArbiter().getMemberPermissions().getArbiterClass();
            mainArbiterClass = getArbiterClass(mainArbiterClass);
        } else {
            if (tournamentEntity.getOtherMainArbiter() != null) {
                mainArbiter = tournamentEntity.getOtherMainArbiter().getFirstName() + " " + tournamentEntity.getOtherMainArbiter().getSecondName();
                mainArbiterClass = tournamentEntity.getOtherMainArbiter().getPermissionsEntity().getArbiterClass();
                mainArbiterClass = getArbiterClass(mainArbiterClass);
            } else {
                mainArbiter = "Nie Wskazano";
                mainArbiterClass = "";
            }
        }

        Paragraph mainArbiterOnTournament = new Paragraph(mainArbiter + " " + mainArbiterClass, font(12, 0));

        String commissionRTSArbiter;
        String commissionRTSArbiterClass;

        if (tournamentEntity.getCommissionRTSArbiter() != null) {
            commissionRTSArbiter = tournamentEntity.getCommissionRTSArbiter().getFirstName() + " " + tournamentEntity.getCommissionRTSArbiter().getSecondName();
            commissionRTSArbiterClass = tournamentEntity.getCommissionRTSArbiter().getMemberPermissions().getArbiterClass();
            commissionRTSArbiterClass = getArbiterClass(commissionRTSArbiterClass);
        } else {
            if (tournamentEntity.getOtherCommissionRTSArbiter() != null) {
                commissionRTSArbiter = tournamentEntity.getOtherCommissionRTSArbiter().getFirstName() + " " + tournamentEntity.getOtherCommissionRTSArbiter().getSecondName();
                commissionRTSArbiterClass = tournamentEntity.getOtherCommissionRTSArbiter().getPermissionsEntity().getArbiterClass();
                commissionRTSArbiterClass = getArbiterClass(commissionRTSArbiterClass);
            } else {
                commissionRTSArbiter = "Nie Wskazano";
                commissionRTSArbiterClass = "";
            }
        }

        Paragraph commissionRTSArbiterOnTournament = new Paragraph(commissionRTSArbiter + " " + commissionRTSArbiterClass, font(12, 0));

        Paragraph others = new Paragraph("\nSędziowie Stanowiskowi\n", font(12, 0));
        Paragraph others1 = new Paragraph("\nSędziowie Biura Obliczeń\n", font(12, 0));

        document.add(title);
        document.add(date);
        document.add(newLine);
        document.add(listTitle);
        document.add(subTitle);
        document.add(mainArbiterOnTournament);
        document.add(subTitle1);
        document.add(commissionRTSArbiterOnTournament);

        if (tournamentEntity.getArbitersList().size() > 0 || tournamentEntity.getOtherArbitersList().size() > 0) {
            document.add(others);
        }

        List<MemberEntity> arbitersList = tournamentEntity.getArbitersList();
        if (arbitersList.size() > 0) {
            for (MemberEntity entity : arbitersList) {
                String arbiterClass = entity.getMemberPermissions().getArbiterClass();
                arbiterClass = getArbiterClass(arbiterClass);
                Paragraph otherArbiter = new Paragraph(entity.getFirstName().concat(" " + entity.getSecondName() + " " + arbiterClass), font(12, 0));
                document.add(otherArbiter);
            }
        }

        List<OtherPersonEntity> otherArbitersList = tournamentEntity.getOtherArbitersList();
        if (otherArbitersList.size() > 0) {

            for (OtherPersonEntity personEntity : otherArbitersList) {
                String arbiterClass = personEntity.getPermissionsEntity().getArbiterClass();
                arbiterClass = getArbiterClass(arbiterClass);
                Paragraph otherArbiters = new Paragraph(personEntity.getFirstName().concat(" " + personEntity.getSecondName() + " " + arbiterClass), font(12, 0));
                document.add(otherArbiters);
            }

        }

        if (tournamentEntity.getArbitersRTSList().size() > 0 || tournamentEntity.getOtherArbitersRTSList().size() > 0) {
            document.add(others1);
        }

        List<MemberEntity> arbitersRTSList = tournamentEntity.getArbitersRTSList();
        if (arbitersRTSList.size() > 0) {
            for (MemberEntity entity : arbitersRTSList) {
                String arbiterClass = entity.getMemberPermissions().getArbiterClass();
                arbiterClass = getArbiterClass(arbiterClass);
                Paragraph otherRTSArbiter = new Paragraph(entity.getFirstName().concat(" " + entity.getSecondName() + " " + arbiterClass), font(12, 0));
                document.add(otherRTSArbiter);
            }
        }

        List<OtherPersonEntity> otherArbitersRTSList = tournamentEntity.getOtherArbitersRTSList();
        if (otherArbitersRTSList.size() > 0) {
            for (OtherPersonEntity personEntity : otherArbitersRTSList) {
                String arbiterClass = personEntity.getPermissionsEntity().getArbiterClass();
                arbiterClass = getArbiterClass(arbiterClass);
                Paragraph otherPersonRTSArbiter = new Paragraph(personEntity.getFirstName().concat(" " + personEntity.getSecondName() + " " + arbiterClass), font(12, 0));
                document.add(otherPersonRTSArbiter);
            }
        }

        document.close();


        byte[] data = convertToByteArray(fileName);
        FilesModel filesModel = FilesModel.builder()
                .name(fileName)
                .data(data)
                .type(String.valueOf(MediaType.APPLICATION_PDF))
                .size(data.length)
                .build();

        FilesEntity filesEntity =
                createFileEntity(filesModel);

        File file = new File(fileName);

        file.delete();
        return filesEntity;

    }

    public FilesEntity generateListOfMembersToReportToPolice() throws
            IOException, DocumentException {

        String fileName = "Lista_osób_do_zgłoszenia_na_Policję " + LocalDate.now().format(dateFormat()) + ".pdf";

        Document document = new Document(PageSize.A4.rotate());
        PdfWriter writer = PdfWriter.getInstance(document,
                new FileOutputStream(fileName));
        writer.setPageEvent(new PageStamper());
        document.open();
        document.addTitle(fileName);
        document.addCreationDate();
        String minute;
        if (LocalTime.now().getMinute() < 10) {
            minute = "0" + LocalTime.now().getMinute();
        } else {
            minute = String.valueOf(LocalTime.now().getMinute());
        }
        String now = LocalTime.now().getHour() + ":" + minute;
        Paragraph title = new Paragraph("Lista osób do zgłoszenia na policję " + LocalDate.now().format(dateFormat()) + " " + now, font(14, 1));
        Paragraph newLine = new Paragraph("\n", font(14, 0));


        document.add(title);
        document.add(newLine);
        LocalDate notValidLicense = LocalDate.now().minusYears(1);
        List<MemberEntity> memberEntityList = memberRepository.findAll().stream()
                .filter(f -> !f.getErased())
                .filter(f -> f.getLicense().getNumber() != null)
                .filter(f -> !f.getLicense().isValid())
                .filter(f -> f.getLicense().getValidThru().isBefore(notValidLicense))
                .sorted(Comparator.comparing(MemberEntity::getSecondName))
                .collect(Collectors.toList());
        float[] pointColumnWidths = {7F, 44F, 17F, 17F};


        PdfPTable titleTable = new PdfPTable(pointColumnWidths);

        PdfPCell lp = new PdfPCell(new Paragraph("lp", font(12, 0)));
        PdfPCell name = new PdfPCell(new Paragraph("Nazwisko Imię", font(12, 0)));
        PdfPCell pesel = new PdfPCell(new Paragraph("PESEL", font(12, 0)));
        PdfPCell licenceNumber = new PdfPCell(new Paragraph("numer licencji", font(12, 0)));

        titleTable.setWidthPercentage(100);

        titleTable.addCell(lp);
        titleTable.addCell(name);
        titleTable.addCell(pesel);
        titleTable.addCell(licenceNumber);

        document.add(titleTable);

        document.add(newLine);

        for (int i = 0; i < memberEntityList.size(); i++) {

            MemberEntity memberEntity = memberEntityList.get(i);

            String memberEntityName = memberEntity.getSecondName().concat(" " + memberEntity.getFirstName());

            PdfPTable memberTable = new PdfPTable(pointColumnWidths);

            PdfPCell lpCell = new PdfPCell(new Paragraph(String.valueOf(i + 1), font(12, 0)));
            PdfPCell nameCell = new PdfPCell(new Paragraph(memberEntityName, font(12, 0)));
            PdfPCell peselCell = new PdfPCell(new Paragraph(String.valueOf(memberEntity.getPesel()), font(12, 0)));
            PdfPCell licenceNumberCell = new PdfPCell(new Paragraph(memberEntity.getLicense().getNumber(), font(12, 0)));

            memberTable.setWidthPercentage(100);

            memberTable.addCell(lpCell);
            memberTable.addCell(nameCell);
            memberTable.addCell(peselCell);
            memberTable.addCell(licenceNumberCell);

            document.add(memberTable);

        }


        document.close();


        byte[] data = convertToByteArray(fileName);
        FilesModel filesModel = FilesModel.builder()
                .name(fileName)
                .data(data)
                .type(String.valueOf(MediaType.APPLICATION_PDF))
                .size(data.length)
                .build();

        FilesEntity filesEntity =
                createFileEntity(filesModel);

        File file = new File(fileName);

        file.delete();
        return filesEntity;
    }

    public FilesEntity generateAllMembersToErasedList() throws IOException, DocumentException {

        String fileName = "Lista_osób_do_skreślenia_na_dzień" + LocalDate.now().format(dateFormat()) + ".pdf";

        Document document = new Document(PageSize.A4.rotate());
        PdfWriter writer = PdfWriter.getInstance(document,
                new FileOutputStream(fileName));
        writer.setPageEvent(new PageStamper());
        document.open();
        document.addTitle(fileName);
        document.addCreationDate();
        String minute;
        if (LocalTime.now().getMinute() < 10) {
            minute = "0" + LocalTime.now().getMinute();
        } else {
            minute = String.valueOf(LocalTime.now().getMinute());
        }
        String now = LocalTime.now().getHour() + ":" + minute;
        Paragraph title = new Paragraph("Lista osób do skreślenia na dzień " + LocalDate.now().format(dateFormat()) + " " + now, font(14, 1));
        Paragraph newLine = new Paragraph("\n", font(14, 0));


        document.add(title);
        document.add(newLine);
        LocalDate notValidContribution = LocalDate.of(LocalDate.now().getYear(), 12, 31).minusYears(2);
        List<MemberEntity> memberEntityList = memberRepository.findAll().stream()
                .filter(f -> !f.getErased())
                .filter(f -> !f.getActive())
                .filter(f -> f.getHistory().getContributionList().isEmpty() || f.getHistory().getContributionList().get(0).getValidThru().minusDays(1).isBefore(notValidContribution))
                .sorted(Comparator.comparing(MemberEntity::getSecondName))
                .collect(Collectors.toList());
        float[] pointColumnWidths = {4F, 42F, 14F, 14F, 14F, 14F};


        PdfPTable titleTable = new PdfPTable(pointColumnWidths);

        PdfPCell lp = new PdfPCell(new Paragraph("lp", font(12, 0)));
        PdfPCell name = new PdfPCell(new Paragraph("Nazwisko Imię", font(12, 0)));
        PdfPCell legitimation = new PdfPCell(new Paragraph("legitymacja", font(12, 0)));
        PdfPCell licenceNumber = new PdfPCell(new Paragraph("numer licencji", font(12, 0)));
        PdfPCell licenceDate = new PdfPCell(new Paragraph("licencja ważna do", font(12, 0)));
        PdfPCell contributionDate = new PdfPCell(new Paragraph("Składka ważna do", font(12, 0)));

        titleTable.setWidthPercentage(100);

        titleTable.addCell(lp);
        titleTable.addCell(name);
        titleTable.addCell(legitimation);
        titleTable.addCell(licenceNumber);
        titleTable.addCell(licenceDate);
        titleTable.addCell(contributionDate);

        document.add(titleTable);

        document.add(newLine);

        for (int i = 0; i < memberEntityList.size(); i++) {

            MemberEntity memberEntity = memberEntityList.get(i);

            String memberEntityName = memberEntity.getSecondName().concat(" " + memberEntity.getFirstName());

            PdfPTable memberTable = new PdfPTable(pointColumnWidths);

            PdfPCell lpCell = new PdfPCell(new Paragraph(String.valueOf(i + 1), font(12, 0)));
            PdfPCell nameCell = new PdfPCell(new Paragraph(memberEntityName, font(12, 0)));
            PdfPCell legitimationCell = new PdfPCell(new Paragraph(String.valueOf(memberEntity.getLegitimationNumber()), font(12, 0)));
            PdfPCell licenceNumberCell;
            if (memberEntity.getLicense().getNumber() != null) {
                licenceNumberCell = new PdfPCell(new Paragraph(memberEntity.getLicense().getNumber(), font(12, 0)));
            } else {
                licenceNumberCell = new PdfPCell(new Paragraph("", font(12, 0)));
            }
            PdfPCell licenceDateCell;
            if (memberEntity.getLicense().getNumber() != null) {
                licenceDateCell = new PdfPCell(new Paragraph(String.valueOf(memberEntity.getLicense().getValidThru()), font(12, 0)));
            } else {
                licenceDateCell = new PdfPCell(new Paragraph("", font(12, 0)));
            }
            PdfPCell contributionDateCell;
            if (memberEntity.getHistory().getContributionList().size() > 0) {
                contributionDateCell = new PdfPCell(new Paragraph(String.valueOf(memberEntity.getHistory().getContributionList().get(0).getValidThru()), font(12, 0)));
            } else {
                contributionDateCell = new PdfPCell(new Paragraph("BRAK SKŁADEK", font(12, 0)));
            }
            memberTable.setWidthPercentage(100);

            memberTable.addCell(lpCell);
            memberTable.addCell(nameCell);
            memberTable.addCell(legitimationCell);
            memberTable.addCell(licenceNumberCell);
            memberTable.addCell(licenceDateCell);
            memberTable.addCell(contributionDateCell);

            document.add(memberTable);

        }


        document.close();


        byte[] data = convertToByteArray(fileName);
        FilesModel filesModel = FilesModel.builder()
                .name(fileName)
                .data(data)
                .type(String.valueOf(MediaType.APPLICATION_PDF))
                .size(data.length)
                .build();

        FilesEntity filesEntity =
                createFileEntity(filesModel);

        File file = new File(fileName);

        file.delete();
        return filesEntity;
    }

    public FilesEntity getAllMembersWithLicenceValidAndContributionNotValid() throws IOException, DocumentException {


        String fileName = "Lista_osób_bez_składek_z_ważnymi_licencjami_na_dzień" + LocalDate.now().format(dateFormat()) + ".pdf";

        Document document = new Document(PageSize.A4.rotate());
        PdfWriter writer = PdfWriter.getInstance(document,
                new FileOutputStream(fileName));
        writer.setPageEvent(new PageStamper());
        document.open();
        document.addTitle(fileName);
        document.addCreationDate();
        String minute;
        if (LocalTime.now().getMinute() < 10) {
            minute = "0" + LocalTime.now().getMinute();
        } else {
            minute = String.valueOf(LocalTime.now().getMinute());
        }
        String now = LocalTime.now().getHour() + ":" + minute;
        Paragraph title = new Paragraph("Lista osób bez składek z ważnymi licencjami na dzień " + LocalDate.now().format(dateFormat()) + " " + now, font(14, 1));
        Paragraph newLine = new Paragraph("\n", font(14, 0));


        document.add(title);
        document.add(newLine);

        List<MemberEntity> memberEntityList = memberRepository.findAll().stream()
                .filter(f -> !f.getErased())
                .filter(f -> f.getLicense().getNumber() != null)
                .filter(f -> f.getLicense().isValid())
                .filter(f -> !f.getActive())
                .sorted(Comparator.comparing(MemberEntity::getSecondName))
                .collect(Collectors.toList());
        float[] pointColumnWidths = {4F, 42F, 14F, 14F, 14F, 14F};


        PdfPTable titleTable = new PdfPTable(pointColumnWidths);

        PdfPCell lp = new PdfPCell(new Paragraph("lp", font(12, 0)));
        PdfPCell name = new PdfPCell(new Paragraph("Nazwisko Imię", font(12, 0)));
        PdfPCell legitimation = new PdfPCell(new Paragraph("legitymacja", font(12, 0)));
        PdfPCell licenceNumber = new PdfPCell(new Paragraph("numer licencji", font(12, 0)));
        PdfPCell licenceDate = new PdfPCell(new Paragraph("licencja ważna do", font(12, 0)));
        PdfPCell contributionDate = new PdfPCell(new Paragraph("Składka ważna do", font(12, 0)));

        titleTable.setWidthPercentage(100);

        titleTable.addCell(lp);
        titleTable.addCell(name);
        titleTable.addCell(legitimation);
        titleTable.addCell(licenceNumber);
        titleTable.addCell(licenceDate);
        titleTable.addCell(contributionDate);

        document.add(titleTable);

        document.add(newLine);

        for (int i = 0; i < memberEntityList.size(); i++) {

            MemberEntity memberEntity = memberEntityList.get(i);

            String memberEntityName = memberEntity.getSecondName().concat(" " + memberEntity.getFirstName());

            PdfPTable memberTable = new PdfPTable(pointColumnWidths);

            PdfPCell lpCell = new PdfPCell(new Paragraph(String.valueOf(i + 1), font(12, 0)));
            PdfPCell nameCell = new PdfPCell(new Paragraph(memberEntityName, font(12, 0)));
            PdfPCell legitimationCell = new PdfPCell(new Paragraph(String.valueOf(memberEntity.getLegitimationNumber()), font(12, 0)));
            PdfPCell licenceNumberCell = new PdfPCell(new Paragraph(memberEntity.getLicense().getNumber(), font(12, 0)));
            PdfPCell licenceDateCell = new PdfPCell(new Paragraph(String.valueOf(memberEntity.getLicense().getValidThru()), font(12, 0)));
            PdfPCell contributionDateCell;
            if (memberEntity.getHistory().getContributionList().size() > 0) {
                contributionDateCell = new PdfPCell(new Paragraph(String.valueOf(memberEntity.getHistory().getContributionList().get(0).getValidThru()), font(12, 0)));
            } else {
                contributionDateCell = new PdfPCell(new Paragraph("BRAK SKŁADEK", font(12, 0)));
            }
            memberTable.setWidthPercentage(100);

            memberTable.addCell(lpCell);
            memberTable.addCell(nameCell);
            memberTable.addCell(legitimationCell);
            memberTable.addCell(licenceNumberCell);
            memberTable.addCell(licenceDateCell);
            memberTable.addCell(contributionDateCell);

            document.add(memberTable);

        }


        document.close();


        byte[] data = convertToByteArray(fileName);
        FilesModel filesModel = FilesModel.builder()
                .name(fileName)
                .data(data)
                .type(String.valueOf(MediaType.APPLICATION_PDF))
                .size(data.length)
                .build();

        FilesEntity filesEntity =
                createFileEntity(filesModel);

        File file = new File(fileName);

        file.delete();
        return filesEntity;

    }

    public FilesEntity getGunRegistry(List<String> guns) throws IOException, DocumentException {


        String fileName = "Lista_broni_w_magazynie_na_dzień" + LocalDate.now().format(dateFormat()) + ".pdf";

        Document document = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(document,
                new FileOutputStream(fileName));
        document.open();
        document.addTitle(fileName);
        document.addCreationDate();

        Paragraph title = new Paragraph("Lista Broni w Magazynie".toUpperCase(), font(14, 1));
        Paragraph newLine = new Paragraph("\n", font(14, 0));


        document.add(title);
        document.add(newLine);

        float[] pointColumnWidths = {4F, 16F, 10F, 10F, 10F, 10F, 10F};
        int tableContentSize = 10;
        Paragraph lp = new Paragraph("Lp", font(tableContentSize, 0));
        Paragraph modelName = new Paragraph("Marka i Model", font(tableContentSize, 0));
        Paragraph caliberAndProductionYear = new Paragraph("Kaliber i rok produkcji", font(tableContentSize, 0));
        Paragraph serialNumber = new Paragraph("Numer i seria", font(tableContentSize, 0));
        Paragraph recordInEvidenceBook = new Paragraph("Poz. z książki ewidencji", font(tableContentSize, 0));
        Paragraph numberOfMagazines = new Paragraph("Magazynki", font(tableContentSize, 0));
        Paragraph gunCertificateSerialNumber = new Paragraph("Numer świadectwa", font(tableContentSize, 0));


        PdfPTable titleTable = new PdfPTable(pointColumnWidths);
        titleTable.setWidthPercentage(100);

        PdfPCell lpCell = new PdfPCell(lp);
        PdfPCell modelNameCell = new PdfPCell(modelName);
        PdfPCell caliberAndProductionYearCell = new PdfPCell(caliberAndProductionYear);
        PdfPCell serialNumberCell = new PdfPCell(serialNumber);
        PdfPCell recordInEvidenceBookCell = new PdfPCell(recordInEvidenceBook);
        PdfPCell numberOfMagazinesCell = new PdfPCell(numberOfMagazines);
        PdfPCell gunCertificateSerialNumberCell = new PdfPCell(gunCertificateSerialNumber);

        lpCell.setHorizontalAlignment(1);
        lpCell.setVerticalAlignment(1);
        modelNameCell.setHorizontalAlignment(1);
        modelNameCell.setVerticalAlignment(1);
        caliberAndProductionYearCell.setHorizontalAlignment(1);
        caliberAndProductionYearCell.setVerticalAlignment(1);
        serialNumberCell.setHorizontalAlignment(1);
        serialNumberCell.setVerticalAlignment(1);
        recordInEvidenceBookCell.setHorizontalAlignment(1);
        recordInEvidenceBookCell.setVerticalAlignment(1);
        numberOfMagazinesCell.setHorizontalAlignment(1);
        numberOfMagazinesCell.setVerticalAlignment(1);
        gunCertificateSerialNumberCell.setHorizontalAlignment(1);
        gunCertificateSerialNumberCell.setVerticalAlignment(1);


        titleTable.addCell(lpCell);
        titleTable.addCell(modelNameCell);
        titleTable.addCell(caliberAndProductionYearCell);
        titleTable.addCell(serialNumberCell);
        titleTable.addCell(recordInEvidenceBookCell);
        titleTable.addCell(numberOfMagazinesCell);
        titleTable.addCell(gunCertificateSerialNumberCell);


        List<String> list = new ArrayList<>();

        for (int i = 0; i < guns.size(); i++) {
            int finalI = i;
            GunStoreEntity gunStoreEntity = gunStoreRepository.findAll().stream().filter(f -> f.getUuid().equals(guns.get(finalI))).findFirst().orElseThrow(EntityNotFoundException::new);
            if (!gunStoreEntity.getGunEntityList().isEmpty()) {
                list.add(gunStoreEntity.getTypeName());
            }
            list.sort(String::compareTo);
        }

        for (int i = 0; i < list.size(); i++) {
            Paragraph gunTypeName = new Paragraph(list.get(i), font(12, 1));
            gunTypeName.setAlignment(1);
            document.add(gunTypeName);
            document.add(newLine);
            document.add(titleTable);

            int finalI = i;
            List<GunEntity> collect = gunRepository.findAll()
                    .stream()
                    .filter(f -> f.getGunType().equals(list.get(finalI)))
                    .filter(GunEntity::isInStock)
                    .sorted(Comparator.comparing(GunEntity::getCaliber).thenComparing(GunEntity::getModelName))
                    .collect(Collectors.toList());
            if (collect.size() > 0) {

                for (int j = 0; j < collect.size(); j++) {
                    int contentSize = 8;
                    GunEntity gun = collect.get(j);

                    PdfPTable gunTable = new PdfPTable(pointColumnWidths);
                    gunTable.setWidthPercentage(100);

                    Paragraph lpGun = new Paragraph(String.valueOf(j + 1), font(contentSize, 0));
                    Paragraph modelNameGun = new Paragraph(gun.getModelName(), font(contentSize, 0));
                    Paragraph caliberAndProductionYearGun;
                    if (gun.getProductionYear() != null && !gun.getProductionYear().isEmpty() && !gun.getProductionYear().equals("null")) {
                        caliberAndProductionYearGun = new Paragraph(gun.getCaliber() + "\nrok " + gun.getProductionYear(), font(contentSize, 0));

                    } else {
                        caliberAndProductionYearGun = new Paragraph(gun.getCaliber(), font(contentSize, 0));
                    }
                    Paragraph serialNumberGun = new Paragraph(gun.getSerialNumber(), font(contentSize, 0));
                    Paragraph recordInEvidenceBookGun = new Paragraph(gun.getRecordInEvidenceBook(), font(contentSize, 0));
                    Paragraph numberOfMagazinesGun = new Paragraph(gun.getNumberOfMagazines(), font(contentSize, 0));
                    Paragraph gunCertificateSerialNumberGun = new Paragraph(gun.getGunCertificateSerialNumber(), font(contentSize, 0));

                    PdfPCell lpGunCell = new PdfPCell(lpGun);
                    PdfPCell modelNameGunCell = new PdfPCell(modelNameGun);
                    PdfPCell caliberAndProductionYearGunCell = new PdfPCell(caliberAndProductionYearGun);
                    PdfPCell serialNumberGunCell = new PdfPCell(serialNumberGun);
                    PdfPCell recordInEvidenceBookGunCell = new PdfPCell(recordInEvidenceBookGun);
                    PdfPCell numberOfMagazinesGunCell = new PdfPCell(numberOfMagazinesGun);
                    PdfPCell gunCertificateSerialNumberGunCell = new PdfPCell(gunCertificateSerialNumberGun);

                    lpGunCell.setHorizontalAlignment(1);
                    lpGunCell.setVerticalAlignment(1);
                    modelNameGunCell.setHorizontalAlignment(1);
                    modelNameGunCell.setVerticalAlignment(1);
                    caliberAndProductionYearGunCell.setHorizontalAlignment(1);
                    caliberAndProductionYearGunCell.setVerticalAlignment(1);
                    serialNumberGunCell.setHorizontalAlignment(1);
                    serialNumberGunCell.setVerticalAlignment(1);
                    recordInEvidenceBookGunCell.setHorizontalAlignment(1);
                    recordInEvidenceBookGunCell.setVerticalAlignment(1);
                    numberOfMagazinesGunCell.setHorizontalAlignment(1);
                    numberOfMagazinesGunCell.setVerticalAlignment(1);
                    gunCertificateSerialNumberGunCell.setHorizontalAlignment(1);
                    gunCertificateSerialNumberGunCell.setVerticalAlignment(1);

                    gunTable.addCell(lpGunCell);
                    gunTable.addCell(modelNameGunCell);
                    gunTable.addCell(caliberAndProductionYearGunCell);
                    gunTable.addCell(serialNumberGunCell);
                    gunTable.addCell(recordInEvidenceBookGunCell);
                    gunTable.addCell(numberOfMagazinesGunCell);
                    gunTable.addCell(gunCertificateSerialNumberGunCell);

                    document.add(gunTable);
                }
                document.add(newLine);

            }

        }
        document.close();


        byte[] data = convertToByteArray(fileName);
        FilesModel filesModel = FilesModel.builder()
                .name(fileName)
                .data(data)
                .type(String.valueOf(MediaType.APPLICATION_PDF))
                .size(data.length)
                .build();

        FilesEntity filesEntity =
                createFileEntity(filesModel);

        File file = new File(fileName);

        file.delete();
        return filesEntity;

    }

    public FilesEntity getGunTransportCertificate(List<String> guns, LocalDate date, LocalDate date1) throws
            IOException, DocumentException {


        String fileName = "Lista_broni_do_przewozu_na_dzień" + LocalDate.now().format(dateFormat()) + ".pdf";

        Document document = new Document(PageSize.A4.rotate());
        PdfWriter writer = PdfWriter.getInstance(document,
                new FileOutputStream(fileName));
        writer.setPageEvent(new PageStamper());
        document.open();
        document.addTitle(fileName);
        document.addCreationDate();
        String minute;
        if (LocalTime.now().getMinute() < 10) {
            minute = "0" + LocalTime.now().getMinute();
        } else {
            minute = String.valueOf(LocalTime.now().getMinute());
        }
        String now = LocalTime.now().getHour() + ":" + minute;
        Paragraph title = new Paragraph("Lista jednostek broni do przewozu od dnia " + date + " do dnia " + date1, font(14, 1));
        Paragraph subtitle = new Paragraph("Wystawiono dnia " + LocalDate.now().format(dateFormat()) + " o godzinie " + now, font(14, 1));
        Paragraph newLine = new Paragraph("\n", font(14, 0));


        document.add(title);
        document.add(subtitle);
        document.add(newLine);

        float[] pointColumnWidths = {4F, 16F, 12F, 12F, 12F, 12F, 12F};

        Paragraph lp = new Paragraph("Lp", font(12, 0));
        Paragraph modelName = new Paragraph("Marka i Model", font(12, 0));
        Paragraph caliberAndProductionYear = new Paragraph("Kaliber i rok produkcji", font(12, 0));
        Paragraph serialNumber = new Paragraph("Numer i seria", font(12, 0));
        Paragraph recordInEvidenceBook = new Paragraph("Poz. z książki ewidencji", font(12, 0));
        Paragraph numberOfMagazines = new Paragraph("Magazynki", font(12, 0));
        Paragraph gunCertificateSerialNumber = new Paragraph("Numer świadectwa", font(12, 0));


        PdfPTable titleTable = new PdfPTable(pointColumnWidths);
        titleTable.setWidthPercentage(100);

        PdfPCell lpCell = new PdfPCell(lp);
        PdfPCell modelNameCell = new PdfPCell(modelName);
        PdfPCell caliberAndProductionYearCell = new PdfPCell(caliberAndProductionYear);
        PdfPCell serialNumberCell = new PdfPCell(serialNumber);
        PdfPCell recordInEvidenceBookCell = new PdfPCell(recordInEvidenceBook);
        PdfPCell numberOfMagazinesCell = new PdfPCell(numberOfMagazines);
        PdfPCell gunCertificateSerialNumberCell = new PdfPCell(gunCertificateSerialNumber);

        lpCell.setHorizontalAlignment(1);
        lpCell.setVerticalAlignment(1);
        modelNameCell.setHorizontalAlignment(1);
        modelNameCell.setVerticalAlignment(1);
        caliberAndProductionYearCell.setHorizontalAlignment(1);
        caliberAndProductionYearCell.setVerticalAlignment(1);
        serialNumberCell.setHorizontalAlignment(1);
        serialNumberCell.setVerticalAlignment(1);
        recordInEvidenceBookCell.setHorizontalAlignment(1);
        recordInEvidenceBookCell.setVerticalAlignment(1);
        numberOfMagazinesCell.setHorizontalAlignment(1);
        numberOfMagazinesCell.setVerticalAlignment(1);
        gunCertificateSerialNumberCell.setHorizontalAlignment(1);
        gunCertificateSerialNumberCell.setVerticalAlignment(1);


        titleTable.addCell(lpCell);
        titleTable.addCell(modelNameCell);
        titleTable.addCell(caliberAndProductionYearCell);
        titleTable.addCell(serialNumberCell);
        titleTable.addCell(recordInEvidenceBookCell);
        titleTable.addCell(numberOfMagazinesCell);
        titleTable.addCell(gunCertificateSerialNumberCell);

        document.add(titleTable);

        List<GunEntity> collect1 = new ArrayList<>(gunRepository.findAllById(guns));

        collect1 = collect1.stream().sorted(Comparator.comparing(GunEntity::getCaliber).thenComparing(GunEntity::getModelName)).collect(Collectors.toList());

        for (int j = 0; j < collect1.size(); j++) {

            GunEntity gun = collect1.get(j);

            PdfPTable gunTable = new PdfPTable(pointColumnWidths);
            gunTable.setWidthPercentage(100);

            Paragraph lpGun = new Paragraph(String.valueOf(j + 1), font(12, 0));
            Paragraph modelNameGun = new Paragraph(gun.getModelName(), font(12, 0));
            Paragraph caliberAndProductionYearGun;
            if (gun.getProductionYear() != null && !gun.getProductionYear().isEmpty() && !gun.getProductionYear().equals("null")) {
                caliberAndProductionYearGun = new Paragraph(gun.getCaliber() + "\nrok " + gun.getProductionYear(), font(12, 0));

            } else {
                caliberAndProductionYearGun = new Paragraph(gun.getCaliber(), font(12, 0));
            }
            Paragraph serialNumberGun = new Paragraph(gun.getSerialNumber(), font(12, 0));
            Paragraph recordInEvidenceBookGun = new Paragraph(gun.getRecordInEvidenceBook(), font(12, 0));
            Paragraph numberOfMagazinesGun = new Paragraph(gun.getNumberOfMagazines(), font(12, 0));
            Paragraph gunCertificateSerialNumberGun = new Paragraph(gun.getGunCertificateSerialNumber(), font(12, 0));

            PdfPCell lpGunCell = new PdfPCell(lpGun);
            PdfPCell modelNameGunCell = new PdfPCell(modelNameGun);
            PdfPCell caliberAndProductionYearGunCell = new PdfPCell(caliberAndProductionYearGun);
            PdfPCell serialNumberGunCell = new PdfPCell(serialNumberGun);
            PdfPCell recordInEvidenceBookGunCell = new PdfPCell(recordInEvidenceBookGun);
            PdfPCell numberOfMagazinesGunCell = new PdfPCell(numberOfMagazinesGun);
            PdfPCell gunCertificateSerialNumberGunCell = new PdfPCell(gunCertificateSerialNumberGun);

            lpGunCell.setHorizontalAlignment(1);
            lpGunCell.setVerticalAlignment(1);
            modelNameGunCell.setHorizontalAlignment(1);
            modelNameGunCell.setVerticalAlignment(1);
            caliberAndProductionYearGunCell.setHorizontalAlignment(1);
            caliberAndProductionYearGunCell.setVerticalAlignment(1);
            serialNumberGunCell.setHorizontalAlignment(1);
            serialNumberGunCell.setVerticalAlignment(1);
            recordInEvidenceBookGunCell.setHorizontalAlignment(1);
            recordInEvidenceBookGunCell.setVerticalAlignment(1);
            numberOfMagazinesGunCell.setHorizontalAlignment(1);
            numberOfMagazinesGunCell.setVerticalAlignment(1);
            gunCertificateSerialNumberGunCell.setHorizontalAlignment(1);
            gunCertificateSerialNumberGunCell.setVerticalAlignment(1);

            gunTable.addCell(lpGunCell);
            gunTable.addCell(modelNameGunCell);
            gunTable.addCell(caliberAndProductionYearGunCell);
            gunTable.addCell(serialNumberGunCell);
            gunTable.addCell(recordInEvidenceBookGunCell);
            gunTable.addCell(numberOfMagazinesGunCell);
            gunTable.addCell(gunCertificateSerialNumberGunCell);

            document.add(gunTable);


        }

        document.close();


        byte[] data = convertToByteArray(fileName);
        FilesModel filesModel = FilesModel.builder()
                .name(fileName)
                .data(data)
                .type(String.valueOf(MediaType.APPLICATION_PDF))
                .size(data.length)
                .build();

        FilesEntity filesEntity =
                createFileEntity(filesModel);

        File file = new File(fileName);

        file.delete();
        return filesEntity;
    }

    public FilesEntity getAllErasedMembers() throws IOException, DocumentException {


        String fileName = "Lista_osób_skreślonych_na_dzień" + LocalDate.now().format(dateFormat()) + ".pdf";

        Document document = new Document(PageSize.A4.rotate());
        PdfWriter writer = PdfWriter.getInstance(document,
                new FileOutputStream(fileName));
        writer.setPageEvent(new PageStamper());
        document.open();
        document.addTitle(fileName);
        document.addCreationDate();
        String minute;
        if (LocalTime.now().getMinute() < 10) {
            minute = "0" + LocalTime.now().getMinute();
        } else {
            minute = String.valueOf(LocalTime.now().getMinute());
        }
        String now = LocalTime.now().getHour() + ":" + minute;
        Paragraph title = new Paragraph("Lista osób bez składek z ważnymi licencjami na dzień " + LocalDate.now().format(dateFormat()) + " " + now, font(14, 1));
        Paragraph newLine = new Paragraph("\n", font(14, 0));


        document.add(title);
        document.add(newLine);

        List<MemberEntity> memberEntityList = memberRepository.findAll().stream()
                .filter(MemberEntity::getErased)
                .sorted(Comparator.comparing(MemberEntity::getSecondName))
                .collect(Collectors.toList());
        float[] pointColumnWidths = {4F, 28F, 10F, 10F, 14F, 14F, 36F};


        PdfPTable titleTable = new PdfPTable(pointColumnWidths);

        PdfPCell lp = new PdfPCell(new Paragraph("lp", font(12, 0)));
        PdfPCell name = new PdfPCell(new Paragraph("Nazwisko Imię", font(12, 0)));
        PdfPCell legitimation = new PdfPCell(new Paragraph("legitymacja", font(12, 0)));
        PdfPCell licenceNumber = new PdfPCell(new Paragraph("numer licencji", font(12, 0)));
        PdfPCell licenceDate = new PdfPCell(new Paragraph("licencja ważna do", font(12, 0)));
        PdfPCell contributionDate = new PdfPCell(new Paragraph("Składka ważna do", font(12, 0)));
        PdfPCell erasedReason = new PdfPCell(new Paragraph("Przyczyna skreślenia", font(12, 0)));

        titleTable.setWidthPercentage(100);

        titleTable.addCell(lp);
        titleTable.addCell(name);
        titleTable.addCell(legitimation);
        titleTable.addCell(licenceNumber);
        titleTable.addCell(licenceDate);
        titleTable.addCell(contributionDate);
        titleTable.addCell(erasedReason);

        document.add(titleTable);

        document.add(newLine);

        for (int i = 0; i < memberEntityList.size(); i++) {

            MemberEntity memberEntity = memberEntityList.get(i);

            String memberEntityName = memberEntity.getSecondName().concat(" " + memberEntity.getFirstName());

            PdfPTable memberTable = new PdfPTable(pointColumnWidths);

            PdfPCell lpCell = new PdfPCell(new Paragraph(String.valueOf(i + 1), font(12, 0)));
            PdfPCell nameCell = new PdfPCell(new Paragraph(memberEntityName, font(12, 0)));
            PdfPCell legitimationCell = new PdfPCell(new Paragraph(String.valueOf(memberEntity.getLegitimationNumber()), font(12, 0)));
            PdfPCell licenceNumberCell;
            if (memberEntity.getLicense().getNumber() != null) {
                licenceNumberCell = new PdfPCell(new Paragraph(memberEntity.getLicense().getNumber(), font(12, 0)));
            } else {
                licenceNumberCell = new PdfPCell(new Paragraph("", font(12, 0)));
            }
            PdfPCell licenceDateCell;
            if (memberEntity.getLicense().getValidThru() != null) {
                licenceDateCell = new PdfPCell(new Paragraph(String.valueOf(memberEntity.getLicense().getValidThru()), font(12, 0)));
            } else {
                licenceDateCell = new PdfPCell(new Paragraph("", font(12, 0)));
            }
            PdfPCell contributionDateCell;
            if (memberEntity.getHistory().getContributionList().size() > 0) {
                contributionDateCell = new PdfPCell(new Paragraph(String.valueOf(memberEntity.getHistory().getContributionList().get(0).getValidThru()), font(12, 0)));
            } else {
                contributionDateCell = new PdfPCell(new Paragraph("BRAK SKŁADEK", font(12, 0)));
            }
            PdfPCell erasedReasonCell = null;
            if (memberEntity.getErasedEntity() != null) {
                erasedReasonCell = new PdfPCell(new Paragraph(memberEntity.getErasedEntity().getErasedType() + " " + memberEntity.getErasedEntity().getDate().format(dateFormat()), font(12, 0)));
            }
            memberTable.setWidthPercentage(100);

            memberTable.addCell(lpCell);
            memberTable.addCell(nameCell);
            memberTable.addCell(legitimationCell);
            memberTable.addCell(licenceNumberCell);
            memberTable.addCell(licenceDateCell);
            memberTable.addCell(contributionDateCell);
            memberTable.addCell(erasedReasonCell);

            document.add(memberTable);

        }


        document.close();


        byte[] data = convertToByteArray(fileName);
        FilesModel filesModel = FilesModel.builder()
                .name(fileName)
                .data(data)
                .type(String.valueOf(MediaType.APPLICATION_PDF))
                .size(data.length)
                .build();

        FilesEntity filesEntity =
                createFileEntity(filesModel);

        File file = new File(fileName);

        file.delete();
        return filesEntity;

    }

    public FilesEntity getRankingCompetitions() throws IOException, DocumentException {
        String fileName = "Lista_rankingowa.pdf";
        ClubEntity club = clubRepository.getOne(1);
        Document document = new Document(PageSize.A4.rotate());
        PdfWriter writer = PdfWriter.getInstance(document,
                new FileOutputStream(fileName));
        document.open();
        document.addTitle(fileName);
        document.addCreationDate();

        Paragraph title = new Paragraph("Lista Rankingowa " + club.getName(), font(13, 1));
        Paragraph date = new Paragraph(String.valueOf(LocalDate.now().getYear()), font(10, 2));
        Paragraph newLine = new Paragraph("\n", font(10, 0));

        document.add(title);
        document.add(date);
        document.add(newLine);

        PdfPTable mainTable = new PdfPTable(1);
        mainTable.setWidthPercentage(100);

        List<TournamentEntity> all = tournamentRepository.findAll().stream().filter(TournamentEntity::isRanking).collect(Collectors.toList());

        List<List<MemberRanking>> ranking = new ArrayList<>();

        for (int i = 0; i < all.size(); i++) {
            List<MemberRanking> innerRanking = new ArrayList<>();

            for (int j = 0; j < all.get(i).getCompetitionsList().size(); j++) {
                List<ScoreEntity> scoreList = all.get(i).getCompetitionsList().get(j).getScoreList();
                for (ScoreEntity scoreEntity : scoreList) {

                    if (scoreEntity.getMember() != null) {

                        List<Score> scores = new ArrayList<>();
                        MemberEntity member = scoreEntity.getMember();
                        scores.add(Mapping.map(scoreEntity));
                        MemberRanking mr = MemberRanking.builder()
                                .uuid(member.getUuid())
                                .competitionName(all.get(i).getCompetitionsList().get(j).getName())
                                .firstName(member.getFirstName())
                                .secondName(member.getSecondName())
                                .scores(scores)
                                .build();
                        if (i == 0) {
                            innerRanking.add(mr);
                            ranking.add(innerRanking);
                        }
                    }
                }
            }
        }

        for (int l = 0; l < all.size(); l++) {
            List<MemberRanking> memberRankingList = ranking.get(l);
            for (MemberRanking mr : memberRankingList) {
                for (int n = 0; l < mr.getScores().size(); n++) {
                    Score score = mr.getScores().get(l);
                    System.out.println(l + " " + mr.getCompetitionName() + " " + mr.getSecondName() + " " + score.getScore());
                }
            }
        }

        float[] pointColumnWidths = {10F, 30F, 50F, 10F};
        PdfPTable tableLabel = new PdfPTable(pointColumnWidths);
        int size = all.size();
        PdfPTable innerTable = new PdfPTable(size);
        for (TournamentEntity tournamentEntity : all) {
            PdfPCell cell = new PdfPCell(new Paragraph(String.valueOf(tournamentEntity.getDate().format(dateFormat())), font(10, 1)));
            cell.setBorderWidth(0);
            cell.setHorizontalAlignment(1);
            innerTable.addCell(cell);
        }

        PdfPCell cellLabel = new PdfPCell(new Paragraph("M-ce", font(10, 1)));
        PdfPCell cellLabel1 = new PdfPCell(new Paragraph("Imię i Nazwisko", font(10, 1)));
        PdfPCell cellLabel2 = new PdfPCell(innerTable);
        PdfPCell cellLabel3 = new PdfPCell(new Paragraph("Wynik", font(10, 1)));

        document.add(newLine);
        cellLabel.setHorizontalAlignment(Element.ALIGN_CENTER);
        cellLabel1.setHorizontalAlignment(Element.ALIGN_CENTER);
        cellLabel2.setHorizontalAlignment(Element.ALIGN_CENTER);
        cellLabel3.setHorizontalAlignment(Element.ALIGN_CENTER);

        cellLabel.setBorderWidth(0);
        cellLabel1.setBorderWidth(0);
        cellLabel2.setBorderWidth(0);
        cellLabel3.setBorderWidth(0);

        tableLabel.setWidthPercentage(100F);
        tableLabel.addCell(cellLabel);
        tableLabel.addCell(cellLabel1);
        tableLabel.addCell(cellLabel2);
        tableLabel.addCell(cellLabel3);

        document.add(tableLabel);
        document.add(newLine);
        for (
                int i = 0; i < all.size(); i++) {

            for (int j = 0; j < all.get(i).getCompetitionsList().size(); j++) {
                List<ScoreEntity> scoreList = all.get(i).getCompetitionsList().get(j).getScoreList();
                for (int h = 0; h < scoreList.size(); h++) {
                    List<ScoreEntity> scores = all.get(i).getCompetitionsList().get(j).getScoreList();

                    PdfPTable memberTable = new PdfPTable(pointColumnWidths);
                    PdfPTable innerMemberTable = new PdfPTable(all.size());

                    memberTable.setWidthPercentage(100);
                    innerMemberTable.setWidthPercentage(100);

                    Paragraph place = new Paragraph(String.valueOf(j), font(10, 0));
                    Paragraph fullScore = new Paragraph("pełen wynik", font(10, 0));

                    PdfPCell cell = new PdfPCell(place);
                    PdfPCell cell1;
                    PdfPCell cell2 = new PdfPCell(innerMemberTable);
                    PdfPCell cell3 = new PdfPCell(fullScore);
                    cell.setBorderWidth(0);
                    cell2.setBorderWidth(0);
                    cell3.setBorderWidth(0);
                    MemberEntity member = scores.get(h).getMember();
                    if (member != null) {
                        Paragraph member1 = new Paragraph(member.getSecondName().concat(" " + member.getFirstName()), font(10, 0));
                        cell1 = new PdfPCell(member1);
                        cell1.setBorderWidth(0);
                        memberTable.addCell(cell);
                        memberTable.addCell(cell1);
                        PdfPCell cell4 = new PdfPCell();
                        for (int g = 0; g < all.size(); g++) {
                            if (g == i) {
                                Paragraph score = new Paragraph(String.valueOf(scoreList.get(h).getScore()), font(10, 0));
                                cell4 = new PdfPCell(score);
                            }
                            innerMemberTable.addCell(cell4);
                        }
                        memberTable.addCell(cell2);
                        memberTable.addCell(cell3);
                        document.add(memberTable);
                    }
                }
            }
        }

        document.close();


        byte[] data = convertToByteArray(fileName);
        FilesModel filesModel = FilesModel.builder()
                .name(fileName)
                .data(data)
                .type(String.valueOf(MediaType.APPLICATION_PDF))
                .size(data.length)
                .build();

        FilesEntity filesEntity =
                createFileEntity(filesModel);

        File file = new File(fileName);

        file.delete();
        return filesEntity;
    }

    public FilesEntity getJudgingReportInChosenTime(/*LocalDate from, LocalDate to*/) throws IOException, DocumentException {
// TODO wprowadzić daty do wyboru
        String fileName = "raport sędziowania.pdf";
        Document document = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(document,
                new FileOutputStream(fileName));
        writer.setPageEvent(new PageStamper());
        document.open();
        document.addTitle(fileName);
        document.addCreationDate();

        List<MemberEntity> arbiters = memberRepository.findAll()
                .stream()
                .filter(f -> !f.getErased())
                .filter(f -> f.getMemberPermissions().getArbiterNumber() != null)
                .collect(Collectors.toList());


        Paragraph title = new Paragraph("Raport sędziowania „DZIESIĄTKA” ŁÓDŹ", font(13, 1));
//        Paragraph date = new Paragraph("Łódź, od " + dateFormat(from) + " do " + dateFormat(to), font(10, 2));

        document.add(title);
//        document.add(date);
        // dla każdego sędziego
        for (int i = 0; i < arbiters.size(); i++) {
            MemberEntity arbiter = arbiters.get(i);
            if (arbiter.getHistory().getJudgingHistory().size() > 0) {
                Paragraph arbiterP = new Paragraph(arbiter.getFirstName() + arbiter.getSecondName(), font(10, 1));
                document.add(arbiterP);

                List<JudgingHistoryEntity> judgingHistory = arbiter.getHistory().getJudgingHistory();
                // dodawanie jego sędziowania
                for (int j = 0; j < judgingHistory.size(); j++) {

                    Chunk tournamentIndex = new Chunk(String.valueOf(j + 1) + " ", font(10, 0));
                    Chunk tournamentName = new Chunk(judgingHistory.get(j).getName() + " ", font(10, 0));
                    Chunk tournamentDate = new Chunk(judgingHistory.get(j).getDate().format(dateFormat()).toString() + " ", font(10, 0));
                    Chunk tournamentFunction = new Chunk(judgingHistory.get(j).getJudgingFunction() + " ", font(10, 0));
                    Paragraph tournament = new Paragraph();
                    tournament.add(tournamentIndex);
                    tournament.add(tournamentName);
                    tournament.add(tournamentDate);
                    tournament.add(tournamentFunction);
                    document.add(tournament);
                }
            }

        }


        document.close();


        byte[] data = convertToByteArray(fileName);
        FilesModel filesModel = FilesModel.builder()
                .name(fileName)
                .data(data)
                .type(String.valueOf(MediaType.APPLICATION_PDF))
                .size(data.length)
                .build();

        FilesEntity filesEntity =
                createFileEntity(filesModel);

        File file = new File(fileName);

        file.delete();
        return filesEntity;
    }

    public List<FilesModel> getAllFilesList() {

        List<FilesModel> list = new ArrayList<>();

        filesRepository.findAll().stream().filter(f -> f.getDate() != null).filter(f -> f.getTime() != null).forEach(e -> list.add(
                FilesModel.builder()
                        .uuid(e.getUuid())
                        .date(e.getDate())
                        .name(e.getName())
                        .type(e.getType())
                        .time(e.getTime())
                        .size(e.getSize())
                        .build()));
        list.sort(Comparator.comparing(FilesModel::getDate).thenComparing(FilesModel::getTime).reversed());
        filesRepository.findAll().stream().filter(f -> f.getDate() == null).filter(f -> f.getTime() != null).forEach(e -> list.add(
                FilesModel.builder()
                        .uuid(e.getUuid())
                        .date(e.getDate())
                        .name(e.getName())
                        .type(e.getType())
                        .time(e.getTime())
                        .size(e.getSize())
                        .build()));

        return list;

    }

    public FilesEntity getFile(String uuid) {
        return filesRepository.findById(uuid).orElseThrow(EntityNotFoundException::new);
    }

    public String store(MultipartFile file) throws IOException {
        String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        FilesModel build = FilesModel.builder()
                .name(fileName)
                .type(String.valueOf(file.getContentType()))
                .data(file.getBytes())
                .size(file.getSize())
                .build();
        FilesEntity fileEntity = createFileEntity(build);

        return fileEntity.getUuid();
    }

    public String store(MultipartFile file, MemberEntity member) throws IOException {
//        String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String name = member.getLegitimationNumber().toString() + member.getSecondName().toUpperCase() + member.getFirstName().toUpperCase();
        FilesModel build = FilesModel.builder()
                .name(name)
                .type(String.valueOf(file.getContentType()))
                .data(file.getBytes())
                .size(file.getSize())

                .build();
        FilesEntity fileEntity = createFileEntity(build);

        member.setImageUUID(fileEntity.getUuid());
        memberRepository.saveAndFlush(member);

        return fileEntity.getUuid();
    }

    public List<FilesModel> getAllImages() {
        List<FilesEntity> image = filesRepository.findAll().stream().filter(f -> f.getType().contains("image")).collect(Collectors.toList());

        List<FilesModel> model = new ArrayList<>();

        image.forEach(e -> {
                    FilesModel map = Mapping.map(e);
                    gunRepository.findAll().stream().filter(f -> f.getImgUUID() != null).filter(f -> f.getImgUUID().equals(e.getUuid())).findFirst().ifPresent(gunEntity -> map.setGun(Mapping.map(gunEntity)));
                    model.add(map);
                }
        );
        model.sort(Comparator.comparing(FilesModel::getDate).thenComparing(FilesModel::getTime).reversed());
        return model;
    }

    public ResponseEntity<?> delete(String uuid) {

        if (filesRepository.existsById(uuid)) {

            MemberEntity memberEntity = memberRepository.findAll()
                    .stream()
                    .filter(f -> f.getImageUUID() != null)
                    .filter(f -> f.getImageUUID().equals(uuid))
                    .findFirst()
                    .orElse(null);
            if (memberEntity != null) {
                memberEntity.setImageUUID(null);
                memberRepository.save(memberEntity);
            }

            filesRepository.deleteById(uuid);
            LOG.info("Usunięto plik");
            return ResponseEntity.ok("\"Usunięto plik\"");
        } else {
            return ResponseEntity.badRequest().body("\"Nie udało się usunąć\"");
        }

    }

    private String getSex(String pesel) {
        int i = pesel.charAt(9);
        if (i % 2 == 0) {
            return "Pani";
        } else return "Pan";
    }

    private byte[] convertToByteArray(String path) throws IOException {
        File file = new File(path);
        return Files.readAllBytes(file.toPath());

    }

    /**
     * @param pesel
     * @return date of birth in format year-month-day
     */
    private LocalDate birthDay(String pesel) {

        int year;
        int month;
        year = 10 * Integer.parseInt(pesel.substring(0, 1));
        year += Integer.parseInt(pesel.substring(1, 2));
        month = 10 * Integer.parseInt(pesel.substring(2, 3));
        month += Integer.parseInt(pesel.substring(3, 4));
        if (month > 80 && month < 93) {
            year += 1800;
        } else if (month > 0 && month < 13) {
            year += 1900;
        } else if (month > 20 && month < 33) {
            year += 2000;
        } else if (month > 40 && month < 53) {
            year += 2100;
        } else if (month > 60 && month < 73) {
            year += 2200;
        }

        if (month > 12 && month < 33) {
            month -= 20;
        }
        int day = Integer.parseInt(pesel.substring(4, 6));

        return LocalDate.of(year, month, day);
    }

    /**
     * 1 - BOLD , 2 - ITALIC, 3 - BOLDITALIC
     *
     * @param size  set font size
     * @param style set style Bold/Italic/Bolditalic
     * @return returns new font
     */
    private Font font(int size, int style) throws IOException, DocumentException {
        BaseFont czcionka = BaseFont.createFont("font/times.ttf", BaseFont.IDENTITY_H, BaseFont.CACHED);
        return new Font(czcionka, size, style);
    }

    private String monthFormat(LocalDate date) {

        String day = String.valueOf(date.getDayOfMonth());
        String month = "";

        if (date.getMonth().getValue() == 1) {
            month = "stycznia";
        }
        if (date.getMonth().getValue() == 2) {
            month = "lutego";
        }
        if (date.getMonth().getValue() == 3) {
            month = "marca";
        }
        if (date.getMonth().getValue() == 4) {
            month = "kwietnia";
        }
        if (date.getMonth().getValue() == 5) {
            month = "maja";
        }
        if (date.getMonth().getValue() == 6) {
            month = "czerwca";
        }
        if (date.getMonth().getValue() == 7) {
            month = "lipca";
        }
        if (date.getMonth().getValue() == 8) {
            month = "sierpnia";
        }
        if (date.getMonth().getValue() == 9) {
            month = "września";
        }
        if (date.getMonth().getValue() == 10) {
            month = "października";
        }
        if (date.getMonth().getValue() == 11) {
            month = "listopada";
        }
        if (date.getMonth().getValue() == 12) {
            month = "grudnia";
        }
        String year = String.valueOf(date.getYear());


        return day + " " + month + " " + year;


    }

    @NotNull
    private String getArbiterClass(String arbiterClass) {
        switch (arbiterClass) {
            case "Klasa 3":
                arbiterClass = "Sędzia Klasy Trzeciej";
                break;
            case "Klasa 2":
                arbiterClass = "Sędzia Klasy Drugiej";
                break;
            case "Klasa 1":
                arbiterClass = "Sędzia Klasy Pierwszej";
                break;
            case "Klasa Państwowa":
                arbiterClass = "Sędzia Klasy Państwowej";
                break;
            case "Klasa Międzynarodowa":
                arbiterClass = "Sędzia Klasy Międzynarodowej";
                break;
            default:
                LOG.info("Nie znaleziono Klasy Sędziowskiej");

        }
        return arbiterClass;
    }

    private DateTimeFormatter dateFormat() {
        String europeanDatePattern = "dd.MM.yyyy";
        return DateTimeFormatter.ofPattern(europeanDatePattern);
    }


    static class PageStamper extends PdfPageEventHelper {
        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            final int currentPageNumber = writer.getCurrentPageNumber();

            try {
                final Rectangle pageSize = document.getPageSize();
                final PdfContentByte directContent = writer.getDirectContent();

                directContent.setColorFill(BaseColor.BLACK);
                directContent.setFontAndSize(BaseFont.createFont(), 10);

                directContent.setTextMatrix(pageSize.getRight(40), pageSize.getBottom(25));
                directContent.showText(String.valueOf(currentPageNumber));

            } catch (DocumentException | IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onStartPage(PdfWriter writer, Document document) {
            try {
                // to działa w miejscu docelowym - nieruszać tego
                String source = "C:/Program Files/Apache Software Foundation/Tomcat 9.0/webapps/shootingplace-1.0/WEB-INF/classes/pełna-nazwa(małe).bmp";
                Image image = Image.getInstance(source);

                Rectangle pageSize = document.getPageSize();
                int multiplicity = 7;
                image.scaleAbsolute(new Rectangle(16 * multiplicity, 9 * multiplicity));
                float pw = pageSize.getWidth() / 2;
                float iw = image.getScaledWidth() / 2;
                float[] position = {pw - iw, -5};

                image.setAbsolutePosition(position[0], position[1]);

                final PdfContentByte directContent = writer.getDirectContent();

                directContent.addImage(image);
            } catch (DocumentException | IOException e) {
                e.printStackTrace();
            }
        }

    }


}
