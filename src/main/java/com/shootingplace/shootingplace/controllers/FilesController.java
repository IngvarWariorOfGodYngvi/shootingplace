package com.shootingplace.shootingplace.controllers;

import com.itextpdf.text.DocumentException;
import com.shootingplace.shootingplace.domain.entities.FilesEntity;
import com.shootingplace.shootingplace.services.FilesService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/files")
@CrossOrigin
public class FilesController {

    private final FilesService filesService;


    public FilesController(FilesService filesService) {
        this.filesService = filesService;
    }

    //@Transactional
//    @PostMapping("/addImage")
//    public ResponseEntity<?> storeFile(@ModelAttribute MultipartFile file) throws IOException {
//        System.out.println("coś");
//        if(filesService.store(file)){
//            return ResponseEntity.ok().build();
//        }else {return ResponseEntity.}
//        return "OK";
//
//    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        String message = "";
        try {
            filesService.store(file);

            message = "Uploaded the file successfully: " + file.getName();
            return ResponseEntity.status(HttpStatus.OK).body(message);
        } catch (Exception e) {
            message = "Could not upload the file: " + file.getOriginalFilename() + "!";
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(message);
        }
    }

    @GetMapping("/downloadContribution/{memberUUID}")
    public ResponseEntity<byte[]> getContributionFile(@PathVariable String memberUUID, @RequestParam String contributionUUID) throws IOException, DocumentException {
        FilesEntity filesEntity = filesService.contributionConfirm(memberUUID, contributionUUID);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(filesEntity.getType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment:filename=\"" + filesEntity.getName().trim() + "\"")
                .body(filesEntity.getData());
    }

    @GetMapping("/downloadPersonalCard/{memberUUID}")
    public ResponseEntity<byte[]> getPersonalCardFile(@PathVariable String memberUUID) throws IOException, DocumentException {
        FilesEntity filesEntity = filesService.personalCardFile(memberUUID);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(filesEntity.getType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment:filename=\"" + filesEntity.getName().trim() + "\"")
                .body(filesEntity.getData());
    }

    @GetMapping("/downloadCSVFile/{memberUUID}")
    public ResponseEntity<byte[]> getMemberCSVFile(@PathVariable String memberUUID) throws IOException, DocumentException {
        FilesEntity filesEntity = filesService.getMemberCSVFile(memberUUID);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(filesEntity.getType()))
                .header(HttpHeaders.CONTENT_TYPE, filesEntity.getName())
                .body(filesEntity.getData());
    }

    @GetMapping("/downloadAmmunitionList/{ammoEvidenceUUID}")
    public ResponseEntity<byte[]> getAmmoListFile(@PathVariable String ammoEvidenceUUID) throws IOException, DocumentException {
        FilesEntity filesEntity = filesService.createAmmunitionListDocument(ammoEvidenceUUID);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(filesEntity.getType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment:filename=\"" + filesEntity.getName().trim() + "\"")
                .body(filesEntity.getData());
    }

    @GetMapping("/downloadApplication/{memberUUID}")
    public ResponseEntity<byte[]> getApplicationForExtensionOfTheCompetitorsLicense(@PathVariable String memberUUID) throws IOException, DocumentException {
        FilesEntity filesEntity = filesService.createApplicationForExtensionOfTheCompetitorsLicense(memberUUID);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(filesEntity.getType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment:filename=\"" + filesEntity.getName().trim() + "\"")
                .body(filesEntity.getData());
    }

    @GetMapping("/downloadAnnouncementFromCompetition/{tournamentUUID}")
    public ResponseEntity<byte[]> getAnnouncementFromCompetition(@PathVariable String tournamentUUID) throws IOException, DocumentException {
        FilesEntity filesEntity = filesService.createAnnouncementFromCompetition(tournamentUUID);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(filesEntity.getType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment:filename=\"" + filesEntity.getName().trim() + "\"")
                .body(filesEntity.getData());
    }

    @GetMapping("/downloadRanking")
    public ResponseEntity<byte[]> getRankingCompetitions() throws IOException, DocumentException {
        FilesEntity filesEntity = filesService.getRankingCompetitions();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(filesEntity.getType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment:filename=\"" + filesEntity.getName().trim() + "\"")
                .body(filesEntity.getData());
    }

    @GetMapping("/downloadAllMembers")
    public ResponseEntity<byte[]> getAllMembersToTable(@RequestParam boolean condition) throws IOException, DocumentException {
        FilesEntity filesEntity = filesService.getAllMembersToTable(condition);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(filesEntity.getType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment:filename=\"" + filesEntity.getName().trim() + "\"")
                .body(filesEntity.getData());
    }

    @GetMapping("/downloadAllMembersToElection")
    public ResponseEntity<byte[]> getAllMembersToElection() throws IOException, DocumentException {
        FilesEntity filesEntity = filesService.getAllMembersToElection();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(filesEntity.getType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment:filename=\"" + filesEntity.getName().trim() + "\"")
                .body(filesEntity.getData());
    }

    @GetMapping("/downloadAllMembersWithNoValidLicenseNoContribution")
    public ResponseEntity<byte[]> getAllMembersWithLicenceNotValidAndContributionNotValid() throws IOException, DocumentException {
        FilesEntity filesEntity = filesService.getAllMembersWithLicenceNotValidAndContributionNotValid();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(filesEntity.getType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment:filename=\"" + filesEntity.getName().trim() + "\"")
                .body(filesEntity.getData());
    }

    @GetMapping("/downloadAllErasedMembers")
    public ResponseEntity<byte[]> getAllErasedMembers() throws IOException, DocumentException {
        FilesEntity filesEntity = filesService.getAllErasedMembers();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(filesEntity.getType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment:filename=\"" + filesEntity.getName().trim() + "\"")
                .body(filesEntity.getData());
    }

    @GetMapping("/downloadAllMembersWithValidLicenseNoContribution")
    public ResponseEntity<byte[]> getAllMembersWithLicenceValidAndContributionNotValid() throws IOException, DocumentException {
        FilesEntity filesEntity = filesService.getAllMembersWithLicenceValidAndContributionNotValid();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(filesEntity.getType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment:filename=\"" + filesEntity.getName().trim() + "\"")
                .body(filesEntity.getData());
    }

    @GetMapping("/downloadAllMembersToErased")
    public ResponseEntity<byte[]> getAllMembersToErased() throws IOException, DocumentException {
        FilesEntity filesEntity = filesService.getAllMembersToErased();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(filesEntity.getType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment:filename=\"" + filesEntity.getName().trim() + "\"")
                .body(filesEntity.getData());
    }

    @GetMapping("/downloadCertificateOfClubMembership/{memberUUID}")
    public ResponseEntity<byte[]> CertificateOfClubMembership(@PathVariable String memberUUID, @RequestParam String reason) throws IOException, DocumentException {
        FilesEntity filesEntity = filesService.CertificateOfClubMembership(memberUUID, reason);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(filesEntity.getType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment:filename=\"" + filesEntity.getName().trim() + "\"")
                .body(filesEntity.getData());
    }

    @GetMapping("/downloadGunRegistry")
    public ResponseEntity<byte[]> getGunRegistry(@RequestParam List<String> guns) throws IOException, DocumentException {
        FilesEntity filesEntity = filesService.getGunRegistry(guns);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(filesEntity.getType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment:filename=\"" + filesEntity.getName().trim() + "\"")
                .body(filesEntity.getData());
    }

    @GetMapping("/downloadGunTransportCertificate")
    public ResponseEntity<byte[]> getGunTransportCertificate(@RequestParam List<String> guns, @RequestParam String date, @RequestParam String date1) throws IOException, DocumentException {
        LocalDate parse = LocalDate.parse(date);
        LocalDate parse1 = LocalDate.parse(date1);
        FilesEntity filesEntity = filesService.getGunTransportCertificate(guns, parse, parse1);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(filesEntity.getType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment:filename=\"" + filesEntity.getName().trim() + "\"")
                .body(filesEntity.getData());
    }

    @GetMapping("/downloadMetric/{tournamentUUID}")
    public ResponseEntity<byte[]> getMemberMetrics(@RequestParam String memberUUID, @RequestParam String otherID, @PathVariable String tournamentUUID, @RequestParam List<String> competitions, @RequestParam String startNumber) throws IOException, DocumentException {
        if (otherID.equals("0")) {
            otherID = null;
        } else {
            memberUUID = null;
        }
        if (competitions.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        FilesEntity filesEntity = filesService.getStartsMetric(memberUUID, otherID, tournamentUUID, competitions, startNumber);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(filesEntity.getType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment:filename=\"" + filesEntity.getName().trim() + "\"")
                .body(filesEntity.getData());
    }

    @GetMapping("/downloadJudge/{tournamentUUID}")
    public ResponseEntity<byte[]> getJudge(@PathVariable String tournamentUUID) throws IOException, DocumentException {

        FilesEntity filesEntity = filesService.getJudge(tournamentUUID);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(filesEntity.getType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment:filename=\"" + filesEntity.getName().trim() + "\"")
                .body(filesEntity.getData());
    }

    @GetMapping("/getAllFiles")
    public ResponseEntity<?> getAllFiles() {

        return ResponseEntity.ok(filesService.getAllFilesList());
    }
    @GetMapping("/getAllImages")
    public ResponseEntity<?> getAllImages(){
        return ResponseEntity.ok(filesService.getAllImages());
    }

    @GetMapping("/getFile")
    public ResponseEntity<byte[]> getFile(@RequestParam String uuid) {
        FilesEntity filesEntity = filesService.getFile(uuid);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(filesEntity.getType()))
                .header(HttpHeaders.CONTENT_TYPE, filesEntity.getType())
                .header("filename", filesEntity.getName())
                .body(filesEntity.getData());
    }


}
