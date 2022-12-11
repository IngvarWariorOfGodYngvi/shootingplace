package com.shootingplace.shootingplace.file;

import com.shootingplace.shootingplace.Mapping;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Service
public class DocxFiles {

    //    private final Logger LOG = LogManager.getLogger(getClass());
    private final FilesRepository filesRepository;

    public DocxFiles(FilesRepository filesRepository) {
        this.filesRepository = filesRepository;
    }

    FilesEntity createFileEntity(FilesModel filesModel) {
        filesModel.setDate(LocalDate.now());
        filesModel.setTime(LocalTime.now());
        FilesEntity filesEntity = Mapping.map(filesModel);
//        LOG.info(filesModel.getName().trim() + " Encja została zapisana");
        return filesRepository.save(filesEntity);

    }

    public FilesEntity simpleDocxFile() throws Docx4JException, IOException {
        String fileName = " welcome.docx";
        String policeAddress = "";
        String policeCity = "w Łodzi";
        String policeZipCode = "91-048";
        String policeStreet = "Lutomierska";
        String policeStreetNumber = "108/112";

        policeAddress = "\nKomendant Wojewódzki Policji " + policeCity + "\nWydział Postępowań Administracyjnych\n " + policeZipCode + " " + policeCity + ", " + policeStreet + " " + policeStreetNumber;

        WordprocessingMLPackage wordPackage = WordprocessingMLPackage.createPackage();
        MainDocumentPart mainDocumentPart = wordPackage.getMainDocumentPart();

        mainDocumentPart.addParagraphOfText("Łódź, " + LocalDate.now().format(dateFormat()));
mainDocumentPart.addStyledParagraphOfText("Normal",policeAddress);
        mainDocumentPart.addParagraphOfText("Welcome To Baeldung");
        File file = new File(fileName);
        wordPackage.save(file);


        byte[] data = convertToByteArray(fileName);
        FilesModel filesModel = FilesModel.builder()
                .name(fileName)
                .data(data)
                .type(String.valueOf(MediaType.APPLICATION_PDF))
                .size(data.length)
                .build();

        FilesEntity filesEntity =
                createFileEntity(filesModel);


        file.delete();
        return filesEntity;
    }

    private byte[] convertToByteArray(String path) throws IOException {
        File file = new File(path);
        return Files.readAllBytes(file.toPath());

    }

    private DateTimeFormatter dateFormat() {
        String europeanDatePattern = "dd.MM.yyyy";
        return DateTimeFormatter.ofPattern(europeanDatePattern);
    }
}
