package com.shootingplace.shootingplace.file;

import com.itextpdf.text.DocumentException;
import com.shootingplace.shootingplace.member.MemberEntity;
import com.shootingplace.shootingplace.member.MemberService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
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
    private final XLSXFiles xlsxFiles;
    private final MemberService memberService;


    public FilesController(FilesService filesService, XLSXFiles xlsxFiles, MemberService memberService) {
        this.filesService = filesService;
        this.xlsxFiles = xlsxFiles;
        this.memberService = memberService;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        String message;
        try {
            filesService.store(file);

            message = "Uploaded the file successfully: " + file.getName();
            return ResponseEntity.status(HttpStatus.OK).body(message);
        } catch (Exception e) {
            message = "Could not upload the file: " + file.getOriginalFilename() + "!";
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(message);
        }
    }

    @PostMapping("/member/{uuid}")
    public ResponseEntity<?> addImageToMember(@PathVariable String uuid, @RequestParam("file") MultipartFile file){
        String message;
        try {
            MemberEntity member = memberService.getMember(uuid);
            String store = filesService.store(file,member);

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
    public ResponseEntity<byte[]> getMemberCSVFile(@PathVariable String memberUUID) throws IOException {
        FilesEntity filesEntity = filesService.getMemberCSVFile(memberUUID);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(filesEntity.getType()))
                .header(HttpHeaders.CONTENT_TYPE, filesEntity.getName().replaceAll("ó", "o"))
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

    @GetMapping("/downloadAnnouncementFromCompetitionXLSX/{tournamentUUID}")
    public ResponseEntity<byte[]> getAnnouncementFromCompetitionXSLX(@PathVariable String tournamentUUID) throws IOException {
        FilesEntity filesEntity = xlsxFiles.createAnnouncementInXLSXType(tournamentUUID);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(filesEntity.getType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment:filename=\"" + filesEntity.getName().replaceAll(" ","") + "\"")
                .body(filesEntity.getData());
    }

    @GetMapping("/downloadAllMembers")
    public ResponseEntity<byte[]> getAllMembersToTable(@RequestParam boolean condition) throws IOException, DocumentException {
        FilesEntity filesEntity = filesService.generateMembersListWithCondition(condition);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(filesEntity.getType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment:filename=\"" + filesEntity.getName().trim() + "\"")
                .body(filesEntity.getData());
    }

    @GetMapping("/downloadAllMembersToElection")
    public ResponseEntity<byte[]> getAllMembersToElection() throws IOException, DocumentException {
        FilesEntity filesEntity = filesService.generateAllMembersList();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(filesEntity.getType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment:filename=\"" + filesEntity.getName().trim() + "\"")
                .body(filesEntity.getData());
    }

    @GetMapping("/generateListOfMembersToReportToPolice")
    public ResponseEntity<byte[]> generateListOfMembersToReportToPolice() throws IOException, DocumentException {
        FilesEntity filesEntity = filesService.generateListOfMembersToReportToPolice();
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
        FilesEntity filesEntity = filesService.generateAllMembersToErasedList();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(filesEntity.getType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment:filename=\"" + filesEntity.getName().trim() + "\"")
                .body(filesEntity.getData());
    }

    @GetMapping("/downloadCertificateOfClubMembership/{memberUUID}")
    public ResponseEntity<byte[]> CertificateOfClubMembership(@PathVariable String memberUUID, @RequestParam String reason,@RequestParam String city) throws IOException, DocumentException {
        FilesEntity filesEntity = filesService.CertificateOfClubMembership(memberUUID, reason, city);
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
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment:filename=\"" + filesEntity.getName().replaceAll(" ","") + "\"")
                .body(filesEntity.getData());
    }

    @GetMapping("/downloadJudgingReport")
    public ResponseEntity<byte[]> getJudgingReportInChosenTime(/*@RequestParam LocalDate from, @RequestParam LocalDate to*/) throws DocumentException, IOException {
        FilesEntity filesEntity = filesService.getJudgingReportInChosenTime(/*from,to*/);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(filesEntity.getType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment:filename=\"" + filesEntity.getName().replaceAll(" ","") + "\"")
                .body(filesEntity.getData());
    }
    @GetMapping("/downloadWorkReport")
    public ResponseEntity<byte[]> getWorkTimeReport(@RequestParam String month, @RequestParam String workType,@Nullable @RequestParam String uuid, @RequestParam boolean detailed, @Nullable @RequestParam boolean incrementVersion) throws DocumentException, IOException {
        FilesEntity filesEntity = filesService.getWorkTimeReport(month, workType,uuid, detailed,incrementVersion);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(filesEntity.getType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment:filename=\"" + filesEntity.getName().replaceAll(" ","") + "\"")
                .body(filesEntity.getData());
    }

    @GetMapping("/downloadJudge/{tournamentUUID}")
    public ResponseEntity<byte[]> getJudge(@PathVariable String tournamentUUID) throws IOException, DocumentException {

        FilesEntity filesEntity = filesService.getJudge(tournamentUUID);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(filesEntity.getType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment:filename=\"" + filesEntity.getName().replaceAll(" ","") + "\"")
                .body(filesEntity.getData());
    }

    @GetMapping("/getAllFiles")
    public ResponseEntity<?> getAllFiles(Pageable page) {

        return ResponseEntity.ok(filesService.getAllFilesList(page));
    }

    @GetMapping("/getAllImages")
    public ResponseEntity<?> getAllImages() {
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

    @DeleteMapping("/deleteFile")
    public ResponseEntity<?> deleteFile(@RequestParam String uuid) {
        return filesService.delete(uuid);
    }


}
