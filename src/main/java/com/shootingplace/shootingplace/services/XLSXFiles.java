package com.shootingplace.shootingplace.services;

import com.shootingplace.shootingplace.domain.entities.ClubEntity;
import com.shootingplace.shootingplace.domain.entities.FilesEntity;
import com.shootingplace.shootingplace.domain.entities.TournamentEntity;
import com.shootingplace.shootingplace.domain.models.FilesModel;
import com.shootingplace.shootingplace.repositories.ClubRepository;
import com.shootingplace.shootingplace.repositories.FilesRepository;
import com.shootingplace.shootingplace.repositories.TournamentRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalTime;
@Service
public class XLSXFiles {


    private final TournamentRepository tournamentRepository;
    private final ClubRepository clubRepository;
    private final FilesRepository filesRepository;


    public XLSXFiles(TournamentRepository tournamentRepository, ClubRepository clubRepository, FilesRepository filesRepository) {
        this.tournamentRepository = tournamentRepository;
        this.clubRepository = clubRepository;
        this.filesRepository = filesRepository;
    }


    public FilesEntity createAnnouncementInXLSXType(String tournamentUUID) throws IOException {
        TournamentEntity tournamentEntity = tournamentRepository.findById(tournamentUUID).orElseThrow(EntityNotFoundException::new);
        ClubEntity c = clubRepository.getOne(1);

        String fileName = tournamentEntity.getDate() + " " + c.getName() + " " + tournamentEntity.getName() + ".pdf";

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Arkusz1");
//
//        Font headerFont = workbook.createFont();
//        headerFont.setBold(true);
//        headerFont.setFontHeightInPoints((short) 14);
//        headerFont.setColor(IndexedColors.RED.getIndex());

        CellStyle headerCellStyle = workbook.createCellStyle();
//        headerCellStyle.setFont(headerFont);

        String[] columns = { "First Name", "Last Name", "Email",
                "Date Of Birth" };
        // Create a Row
        Row headerRow = sheet.createRow(0);

        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerCellStyle);
        }

        // Create Other rows and cells with contacts data
        int rowNum = 1;

        for (String col : columns) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(col);
            row.createCell(1).setCellValue(col);
            row.createCell(2).setCellValue(col);
            row.createCell(3).setCellValue(col);
        }

        // Resize all columns to fit the content size
//        for (int i = 0; i < columns.length; i++) {
//            sheet.autoSizeColumn(i);
//        }

        // Write the output to a file
        try (FileOutputStream outputStream = new FileOutputStream(fileName)) {
            workbook.write(outputStream);
        }

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
        System.out.println("coś");
        return filesRepository.saveAndFlush(filesEntity);

    }


}
