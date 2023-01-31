package com.shootingplace.shootingplace.file;

import com.shootingplace.shootingplace.Mapping;
import com.shootingplace.shootingplace.club.ClubEntity;
import com.shootingplace.shootingplace.club.ClubRepository;
import com.shootingplace.shootingplace.enums.CountingMethod;
import com.shootingplace.shootingplace.member.MemberDTO;
import com.shootingplace.shootingplace.member.MemberEntity;
import com.shootingplace.shootingplace.member.MemberRepository;
import com.shootingplace.shootingplace.tournament.CompetitionMembersListEntity;
import com.shootingplace.shootingplace.tournament.ScoreEntity;
import com.shootingplace.shootingplace.tournament.TournamentEntity;
import com.shootingplace.shootingplace.tournament.TournamentRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.text.Collator;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class XLSXFilesService {


    private final TournamentRepository tournamentRepository;
    private final ClubRepository clubRepository;
    private final FilesRepository filesRepository;
    private final MemberRepository memberRepository;

    private final Logger LOG = LogManager.getLogger(getClass());


    public XLSXFilesService(TournamentRepository tournamentRepository, ClubRepository clubRepository, FilesRepository filesRepository, MemberRepository memberRepository) {
        this.tournamentRepository = tournamentRepository;
        this.clubRepository = clubRepository;
        this.filesRepository = filesRepository;
        this.memberRepository = memberRepository;
    }


    public FilesEntity createAnnouncementInXLSXType(String tournamentUUID) throws IOException {
        TournamentEntity tournamentEntity = tournamentRepository.findById(tournamentUUID).orElseThrow(EntityNotFoundException::new);
        ClubEntity c = clubRepository.getOne(1);

        int rc = 0;

        String fileName = "rezultaty" + c.getName().toUpperCase() + ".xlsx";

        XSSFWorkbook workbook = new XSSFWorkbook();

        //style
        XSSFCellStyle cellStyleTitle = workbook.createCellStyle();
        XSSFCellStyle cellStyleDate = workbook.createCellStyle();
        XSSFCellStyle cellStyleCompetitionTitle = workbook.createCellStyle();
        XSSFCellStyle cellStyleCompetitionSubTitle = workbook.createCellStyle();
        XSSFCellStyle cellStyleNormalCenterAlignment = workbook.createCellStyle();

        XSSFFont fontTitle = workbook.createFont();
        fontTitle.setBold(true);
        fontTitle.setFontHeightInPoints((short) 14);
        fontTitle.setFontName("Calibri");

        XSSFFont fontDate = workbook.createFont();
        fontDate.setItalic(true);
        fontDate.setFontHeightInPoints((short) 11);
        fontDate.setFontName("Calibri");

        XSSFFont fontCompetitionTitle = workbook.createFont();
        fontCompetitionTitle.setBold(true);
        fontCompetitionTitle.setFontHeightInPoints((short) 12);
        fontCompetitionTitle.setFontName("Calibri");

        XSSFFont fontCompetitionSubTitle = workbook.createFont();
        fontCompetitionSubTitle.setBold(true);
        fontCompetitionSubTitle.setFontHeightInPoints((short) 10);
        fontCompetitionSubTitle.setFontName("Calibri");

        XSSFFont fontNormalCenterAlignment = workbook.createFont();
        fontNormalCenterAlignment.setFontHeightInPoints((short) 10);
        fontNormalCenterAlignment.setFontName("Calibri");

        cellStyleTitle.setFont(fontTitle);

        cellStyleDate.setFont(fontDate);

        cellStyleCompetitionTitle.setFont(fontCompetitionTitle);

        cellStyleCompetitionSubTitle.setFont(fontCompetitionSubTitle);
        cellStyleCompetitionSubTitle.setAlignment(HorizontalAlignment.CENTER);

        cellStyleNormalCenterAlignment.setAlignment(HorizontalAlignment.CENTER);
        cellStyleNormalCenterAlignment.setFont(fontNormalCenterAlignment);

        XSSFSheet sheet = workbook.createSheet("rezultaty-" + tournamentEntity.getDate());

        XSSFRow row = sheet.createRow(rc++);

        XSSFRow row1 = sheet.createRow(rc++);

        XSSFCell cell = row.createCell(0);
        cell.setCellStyle(cellStyleTitle);

        XSSFCell cell1 = row1.createCell(0);
        cell1.setCellStyle(cellStyleDate);

        sheet.setColumnWidth(0, 11 * 128); //M-ce
        sheet.setColumnWidth(1, 30 * 256); //Imię i nazwisko
        sheet.setColumnWidth(3, 25 * 256); //Klub
        sheet.setColumnWidth(4, 18 * 128); //Wynik
        sheet.setColumnWidth(5, 10 * 128); //10x
        sheet.setColumnWidth(6, 10 * 128); //10/
        cell.setCellValue(tournamentEntity.getName().toUpperCase() + c.getName());
        cell1.setCellValue("Łódź, " + dateFormat(tournamentEntity.getDate()));
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 6));
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 6));

        for (int i = 0; i < tournamentEntity.getCompetitionsList().size(); i++) {
            int cc = 0;
            if (!tournamentEntity.getCompetitionsList().get(i).getScoreList().isEmpty()) {
                CompetitionMembersListEntity competitionMembersListEntity = tournamentEntity.getCompetitionsList().get(i);

                List<ScoreEntity> scoreList = competitionMembersListEntity.getScoreList().stream().filter(f -> !f.isDsq()).filter(f -> !f.isDnf()).filter(f -> !f.isPk()).sorted(Comparator.comparing(ScoreEntity::getScore).reversed()).collect(Collectors.toList());

                List<ScoreEntity> collect = competitionMembersListEntity.getScoreList().stream().filter(f -> f.isDnf() || f.isDsq() || f.isPk()).collect(Collectors.toList());
                scoreList.addAll(collect);
                XSSFRow row2 = sheet.createRow(rc);
                XSSFCell cell2 = row2.createCell(cc);

                cell2.setCellStyle(cellStyleCompetitionTitle);

                cell2.setCellValue(competitionMembersListEntity.getName());
                sheet.addMergedRegion(new CellRangeAddress(rc, rc++, 0, 6));
                XSSFRow row3 = sheet.createRow(rc);
                XSSFCell cell31 = row3.createCell(cc++);
                XSSFCell cell32 = row3.createCell(cc++);
                XSSFCell cell33 = row3.createCell(cc++);
                XSSFCell cell34 = row3.createCell(cc++);
                XSSFCell cell35 = row3.createCell(cc++);
                XSSFCell cell36 = row3.createCell(cc++);
                XSSFCell cell37 = row3.createCell(cc);

                cell31.setCellValue("M-ce");
                cell32.setCellValue("Nazwisko i Imię");
                cell33.setCellValue("");
                cell34.setCellValue("Klub");
                cell35.setCellValue("Wynik");

                cell31.setCellStyle(cellStyleCompetitionSubTitle);
                cell32.setCellStyle(cellStyleCompetitionSubTitle);
                cell33.setCellStyle(cellStyleCompetitionSubTitle);
                cell34.setCellStyle(cellStyleCompetitionSubTitle);
                cell35.setCellStyle(cellStyleCompetitionSubTitle);
                cell36.setCellStyle(cellStyleCompetitionSubTitle);
                cell37.setCellStyle(cellStyleCompetitionSubTitle);

                sheet.addMergedRegion(new CellRangeAddress(rc, rc++, 1, 2));

                if (competitionMembersListEntity.getCountingMethod() != null) {
                    if (competitionMembersListEntity.getCountingMethod().equals(CountingMethod.COMSTOCK.getName())) {
                        cell36.setCellValue("");
                        cell37.setCellValue("");
                    } else {
                        cell36.setCellValue("10 x");
                        cell37.setCellValue("10 /");
                    }
                }

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
                        result = result.concat("(PK)");
                    }
                    if (competitionMembersListEntity.getCountingMethod() != null) {

                        if (competitionMembersListEntity.getCountingMethod().equals(CountingMethod.COMSTOCK.getName())) {
                            o1 = "";
                            o2 = "";

                        } else {
                            o1 = scoreInnerTen.replace(".0", "");
                            o2 = scoreOuterTen.replace(".0", "");

                        }
                    }

                    XSSFRow row4 = sheet.createRow(rc);
                    cc = 0;
                    XSSFCell cell41 = row4.createCell(cc++); //M-ce
                    XSSFCell cell42 = row4.createCell(cc++); //Imię i nazwisko
                    XSSFCell cell43 = row4.createCell(cc++); //Imię i nazwisko
                    XSSFCell cell44 = row4.createCell(cc++); //Klub
                    XSSFCell cell45 = row4.createCell(cc++); //Wynik
                    XSSFCell cell46 = row4.createCell(cc++); //10x
                    XSSFCell cell47 = row4.createCell(cc); //10/

                    cell41.setCellValue(String.valueOf(j + 1));
                    cell42.setCellValue(secondName + " " + firstName);
                    cell43.setCellValue("");
                    cell44.setCellValue(club);
                    cell45.setCellValue(result);
                    cell46.setCellValue(o1);
                    cell47.setCellValue(o2);

                    cell41.setCellStyle(cellStyleNormalCenterAlignment);
                    cell45.setCellStyle(cellStyleCompetitionSubTitle);
                    cell46.setCellStyle(cellStyleNormalCenterAlignment);
                    cell47.setCellStyle(cellStyleNormalCenterAlignment);

                    sheet.addMergedRegion(new CellRangeAddress(rc, rc, 1, 2));
                    if (j < scoreList.size() - 1) {
                        rc++;
                    }
                }
                rc++;
            }


        }

        rc++;

        XSSFRow row2 = sheet.createRow(rc);
        sheet.addMergedRegion(new CellRangeAddress(rc, rc++, 1, 3));
        XSSFRow row3 = sheet.createRow(rc);
        sheet.addMergedRegion(new CellRangeAddress(rc, rc++, 1, 3));
        XSSFRow row4 = sheet.createRow(rc);
        sheet.addMergedRegion(new CellRangeAddress(rc, rc, 1, 3));

        XSSFCell cell2 = row2.createCell(1);
        XSSFCell cell3 = row3.createCell(1);
        XSSFCell cell4 = row4.createCell(1);

        cell2.setCellValue("Zawody odbyły się zgodnie z przepisami bezpieczeństwa");
        cell3.setCellValue("i regulaminem zawodów, oraz liczba sklasyfikowanych zawodników");
        cell4.setCellValue("była zgodna ze stanem faktycznym.");
        rc++;
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

        rc++;
        XSSFRow row5 = sheet.createRow(rc++);
        XSSFRow row6 = sheet.createRow(rc++);
        XSSFRow row7 = sheet.createRow(rc);

        XSSFCell cell5 = row5.createCell(1);
        XSSFCell cell6 = row6.createCell(1);
        XSSFCell cell7 = row7.createCell(1);

        XSSFCell cell51 = row5.createCell(3);
        XSSFCell cell61 = row6.createCell(3);
        XSSFCell cell71 = row7.createCell(3);


        cell5.setCellValue("Sędzia Główny");
        cell6.setCellValue(mainArbiter);
        cell7.setCellValue(mainArbiterClass);

        cell51.setCellValue("Przewodniczący Komisji RTS");
        cell61.setCellValue(arbiterRTS);
        cell71.setCellValue(arbiterRTSClass);

        cell5.setCellStyle(cellStyleNormalCenterAlignment);
        cell51.setCellStyle(cellStyleNormalCenterAlignment);

        cell6.setCellStyle(cellStyleCompetitionSubTitle);
        cell61.setCellStyle(cellStyleCompetitionSubTitle);

        cell7.setCellStyle(cellStyleNormalCenterAlignment);
        cell71.setCellStyle(cellStyleNormalCenterAlignment);

        try (FileOutputStream outputStream = new FileOutputStream(fileName)) {
            workbook.write(outputStream);
        }
        workbook.close();

        byte[] data = convertToByteArray(fileName);
        FilesModel filesModel = FilesModel.builder()
                .name(fileName)
                .data(data)
                .type(String.valueOf(MediaType.TEXT_XML))
                .size(data.length)
                .build();

        FilesEntity filesEntity =
                createFileEntity(filesModel);
        File file = new File(fileName);
        file.delete();
        LOG.info("Pobrano plik " + fileName);

        return filesEntity;
    }

    public FilesEntity getJoinDateSum(LocalDate firstDate, LocalDate secondDate) throws IOException {

        String fileName = "lista klubowiczów zapisanych od "+firstDate.toString()+" do "+ secondDate.toString()+".xlsx";

        List<MemberDTO> collect = memberRepository.findAll().stream()
                .filter(f -> f.getJoinDate().isAfter(firstDate.minusDays(1)))
                .filter(f -> f.getJoinDate().isBefore(secondDate.plusDays(1)))
                .map(Mapping::map2DTO)
                .sorted(Comparator.comparing(MemberDTO::getJoinDate).thenComparing(MemberDTO::getSecondName).thenComparing(MemberDTO::getFirstName))
                .collect(Collectors.toList());

        int rc = 0;

        XSSFWorkbook workbook = new XSSFWorkbook();

        XSSFSheet sheet = workbook.createSheet("lista");

        XSSFFont fontNormalCenterAlignment = workbook.createFont();
        fontNormalCenterAlignment.setFontHeightInPoints((short) 10);
        fontNormalCenterAlignment.setFontName("Calibri");

        XSSFCellStyle cellStyleNormalCenterAlignment = workbook.createCellStyle();
        cellStyleNormalCenterAlignment.setAlignment(HorizontalAlignment.CENTER);
        cellStyleNormalCenterAlignment.setFont(fontNormalCenterAlignment);

        XSSFRow row = sheet.createRow(rc++);

        sheet.setColumnWidth(0, 5000);
        sheet.setColumnWidth(1, 5000);
        sheet.setColumnWidth(2, 5000);

        XSSFCell cell = row.createCell(0);
        cell.setCellStyle(cellStyleNormalCenterAlignment);
        cell.setCellValue("Nazwisko i Imię");
        XSSFCell cell1 = row.createCell(1);
        cell1.setCellValue("Numer Legitymacji");
        cell1.setCellStyle(cellStyleNormalCenterAlignment);
        XSSFCell cell2 = row.createCell(2);
        cell2.setCellValue("Data Zapisu");
        cell2.setCellStyle(cellStyleNormalCenterAlignment);


        for (MemberDTO memberDTO : collect) {
            int cc = 0;
            sheet.setColumnWidth(0, 5000);
            sheet.setColumnWidth(1, 5000);
            sheet.setColumnWidth(2, 5000);
            XSSFRow row1 = sheet.createRow(rc++);
            XSSFCell cell3 = row1.createCell(cc++);
            XSSFCell cell4 = row1.createCell(cc++);
            XSSFCell cell5 = row1.createCell(cc);

            cell3.setCellStyle(cellStyleNormalCenterAlignment);
            cell4.setCellStyle(cellStyleNormalCenterAlignment);
            cell5.setCellStyle(cellStyleNormalCenterAlignment);

            cell3.setCellValue(memberDTO.getMemberName());
            cell4.setCellValue(memberDTO.getLegitimationNumber());
            cell5.setCellValue(memberDTO.getJoinDate().toString());

        }
        try (FileOutputStream outputStream = new FileOutputStream(fileName)) {
            workbook.write(outputStream);
        }
        workbook.close();

        byte[] data = convertToByteArray(fileName);
        FilesModel filesModel = FilesModel.builder()
                .name(fileName)
                .data(data)
                .type(String.valueOf(MediaType.TEXT_XML))
                .size(data.length)
                .build();

        FilesEntity filesEntity =
                createFileEntity(filesModel);
        File file = new File(fileName);
        file.delete();
        LOG.info("Pobrano plik " + fileName);

        return filesEntity;
    }

    public FilesEntity getErasedSum(LocalDate firstDate, LocalDate secondDate) throws IOException {
        String fileName = "lista klubowiczów usuniętych od "+firstDate.toString()+" do "+ secondDate.toString()+".xlsx";

        List<MemberDTO> collect = memberRepository.findAllByErasedTrue().stream()
                .filter(f->f.getErasedEntity()!=null)
                .filter(f -> f.getErasedEntity().getDate().isAfter(firstDate.minusDays(1)))
                .filter(f -> f.getErasedEntity().getDate().isBefore(secondDate.plusDays(1)))
                .map(Mapping::map2DTO)
                .sorted(Comparator.comparing(MemberDTO::getSecondName,pl()).thenComparing(MemberDTO::getFirstName,pl()).reversed())
                .collect(Collectors.toList());

        int rc = 0;

        XSSFWorkbook workbook = new XSSFWorkbook();

        XSSFSheet sheet = workbook.createSheet("lista usuniętych");

        XSSFFont fontNormalCenterAlignment = workbook.createFont();
        fontNormalCenterAlignment.setFontHeightInPoints((short) 10);
        fontNormalCenterAlignment.setFontName("Calibri");

        XSSFCellStyle cellStyleNormalCenterAlignment = workbook.createCellStyle();
        cellStyleNormalCenterAlignment.setAlignment(HorizontalAlignment.CENTER);
        cellStyleNormalCenterAlignment.setFont(fontNormalCenterAlignment);

        XSSFRow row = sheet.createRow(rc++);

        sheet.setColumnWidth(0, 5000);
        sheet.setColumnWidth(1, 5000);
        sheet.setColumnWidth(2, 5000);

        XSSFCell cell = row.createCell(0);
        cell.setCellStyle(cellStyleNormalCenterAlignment);
        cell.setCellValue("Nazwisko i Imię");
        XSSFCell cell1 = row.createCell(1);
        cell1.setCellValue("Numer Legitymacji");
        cell1.setCellStyle(cellStyleNormalCenterAlignment);
        XSSFCell cell2 = row.createCell(2);
        cell2.setCellValue("Data Skreślenia");
        cell2.setCellStyle(cellStyleNormalCenterAlignment);


        for (MemberDTO memberDTO : collect) {
            int cc = 0;
            sheet.setColumnWidth(0, 5000);
            sheet.setColumnWidth(1, 5000);
            sheet.setColumnWidth(2, 5000);
            XSSFRow row1 = sheet.createRow(rc++);
            XSSFCell cell3 = row1.createCell(cc++);
            XSSFCell cell4 = row1.createCell(cc++);
            XSSFCell cell5 = row1.createCell(cc);

            cell3.setCellStyle(cellStyleNormalCenterAlignment);
            cell4.setCellStyle(cellStyleNormalCenterAlignment);
            cell5.setCellStyle(cellStyleNormalCenterAlignment);

            cell3.setCellValue(memberDTO.getMemberName());
            cell4.setCellValue(memberDTO.getLegitimationNumber());
            cell5.setCellValue(memberDTO.getErasedEntity().getDate().toString());

        }
        try (FileOutputStream outputStream = new FileOutputStream(fileName)) {
            workbook.write(outputStream);
        }
        workbook.close();

        byte[] data = convertToByteArray(fileName);
        FilesModel filesModel = FilesModel.builder()
                .name(fileName)
                .data(data)
                .type(String.valueOf(MediaType.TEXT_XML))
                .size(data.length)
                .build();

        FilesEntity filesEntity =
                createFileEntity(filesModel);
        File file = new File(fileName);
        file.delete();
        LOG.info("Pobrano plik " + fileName);

        return filesEntity;
    }

    public FilesEntity getAllMembersToTableXLSXFile(boolean condition) throws IOException {

        String fileName = "Lista_klubowiczów_na_dzień " + LocalDate.now().format(dateFormat()) + ".xlsx";

        List<MemberEntity> collect = memberRepository.findAllByErasedFalse()
                .stream()
                .filter(f -> f.getAdult().equals(condition))
                .sorted(Comparator.comparing(MemberEntity::getSecondName, pl()))
                .collect(Collectors.toList());


        int rc = 0;

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Lista klubowiczów");

        XSSFRow row = sheet.createRow(rc++);

        sheet.setColumnWidth(0, 11 * 128); //lp
        sheet.setColumnWidth(1, 30 * 256); //Imię i nazwisko
        sheet.setColumnWidth(2, 25 * 256); //legitymacja
        sheet.setColumnWidth(3, 18 * 128); //data zapisu
        sheet.setColumnWidth(4, 10 * 128); //data opłacenia składki
        sheet.setColumnWidth(5, 10 * 128); //Składka ważna do

        XSSFCell cell = row.createCell(0);
        XSSFCell cell1 = row.createCell(1);
        XSSFCell cell2 = row.createCell(2);
        XSSFCell cell3 = row.createCell(3);
        XSSFCell cell4 = row.createCell(4);
        XSSFCell cell5 = row.createCell(5);

        cell.setCellValue("lp");
        cell1.setCellValue("Nazwisko i Imię");
        cell2.setCellValue("nr. legitymacji");
        cell3.setCellValue("data zapisu");
        cell4.setCellValue("data opłacenia składki");
        cell5.setCellValue("składka ważna do");
        sheet.createRow(rc++);

        for (int i=0;i< collect.size();i++){
            XSSFRow row1 = sheet.createRow(rc++);
            cell = row1.createCell(0);
            cell1 = row1.createCell(1);
            cell2 = row1.createCell(2);
            cell3 = row1.createCell(3);
            cell4 = row1.createCell(4);
            cell5 = row1.createCell(5);

            cell.setCellValue(i+1);
            cell1.setCellValue(collect.get(i).getMemberName());
            cell2.setCellValue(collect.get(i).getLegitimationNumber());
            cell3.setCellValue(collect.get(i).getJoinDate().toString());

            if (collect.get(i).getHistory().getContributionList().size() > 0) {

                cell4.setCellValue(collect.get(i).getHistory().getContributionList().get(0).getPaymentDay().toString());
                cell5.setCellValue(collect.get(i).getHistory().getContributionList().get(0).getValidThru().toString());

            } else {

                cell4.setCellValue("BRAK SKŁADEK");
                cell5.setCellValue("BRAK SKŁADEK");

            }
        }

        try (FileOutputStream outputStream = new FileOutputStream(fileName)) {
            workbook.write(outputStream);
        }
        workbook.close();

        byte[] data = convertToByteArray(fileName);
        FilesModel filesModel = FilesModel.builder()
                .name(fileName)
                .data(data)
                .type(String.valueOf(MediaType.TEXT_XML))
                .size(data.length)
                .build();

        FilesEntity filesEntity =
                createFileEntity(filesModel);
        File file = new File(fileName);
        file.delete();
        LOG.info("Pobrano plik " + fileName);

        return filesEntity;
    }

    private byte[] convertToByteArray(String path) throws IOException {
        File file = new File(path);
        return Files.readAllBytes(file.toPath());

    }

    FilesEntity createFileEntity(FilesModel filesModel) {
        filesModel.setDate(LocalDate.now());
        filesModel.setTime(LocalTime.now());
        FilesEntity filesEntity = Mapping.map(filesModel);
        return filesRepository.save(filesEntity);

    }

    private String dateFormat(LocalDate date) {

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
//                LOG.info("Nie znaleziono Klasy Sędziowskiej");

        }
        return arbiterClass;
    }

    private Collator pl() {
        return Collator.getInstance(Locale.forLanguageTag("pl"));
    }

    private DateTimeFormatter dateFormat() {
        String europeanDatePattern = "dd.MM.yyyy";
        return DateTimeFormatter.ofPattern(europeanDatePattern);
    }

}
