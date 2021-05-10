package com.shootingplace.shootingplace.controllers;

import com.itextpdf.text.DocumentException;
import com.shootingplace.shootingplace.domain.entities.FilesEntity;
import com.shootingplace.shootingplace.services.FilesService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        try {
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(filesEntity.getType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment:filename=\"" + filesEntity.getName().trim() + "\"")
                    .body(filesEntity.getData());
        } finally {
//            filesService.delete(filesEntity);
        }
    }

    @GetMapping("/downloadCSVFile/{memberUUID}")
    public ResponseEntity<byte[]> getMemberCSVFile(@PathVariable String memberUUID) throws IOException, DocumentException {
        FilesEntity filesEntity = filesService.getMemberCSVFile(memberUUID);
        try {
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(filesEntity.getType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment:filename=\"" + filesEntity.getName().trim() + "\"")
                    .body(filesEntity.getData());
        } finally {
//            filesService.delete(filesEntity);
        }
    }

    @GetMapping("/downloadAmmunitionList/{ammoEvidenceUUID}")
    public ResponseEntity<byte[]> getAmmoListFile(@PathVariable String ammoEvidenceUUID) throws IOException, DocumentException {
        FilesEntity filesEntity = filesService.createAmmunitionListDocument(ammoEvidenceUUID);
        try {
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(filesEntity.getType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment:filename=\"" + filesEntity.getName().trim() + "\"")
                    .body(filesEntity.getData());
        } finally {
//            filesService.delete(filesEntity);
        }
    }

    @GetMapping("/downloadApplication/{memberUUID}")
    public ResponseEntity<byte[]> getApplicationForExtensionOfTheCompetitorsLicense(@PathVariable String memberUUID) throws IOException, DocumentException {
        FilesEntity filesEntity = filesService.createApplicationForExtensionOfTheCompetitorsLicense(memberUUID);
        try {
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(filesEntity.getType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment:filename=\"" + filesEntity.getName().trim() + "\"")
                    .body(filesEntity.getData());
        } finally {
//            filesService.delete(filesEntity);
        }
    }

    @GetMapping("/downloadAnnouncementFromCompetition/{tournamentUUID}")
    public ResponseEntity<byte[]> getAnnouncementFromCompetition(@PathVariable String tournamentUUID) throws IOException, DocumentException {
        FilesEntity filesEntity = filesService.createAnnouncementFromCompetition(tournamentUUID);
        try {
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(filesEntity.getType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment:filename=\"" + filesEntity.getName().trim() + "\"")
                    .body(filesEntity.getData());
        } finally {
//            filesService.delete(filesEntity);
        }
    }

    @GetMapping("/downloadRanking")
    public ResponseEntity<byte[]> getRankingCompetitions() throws IOException, DocumentException {
        FilesEntity filesEntity = filesService.getRankingCompetitions();
        try {
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(filesEntity.getType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment:filename=\"" + filesEntity.getName().trim() + "\"")
                    .body(filesEntity.getData());
        } finally {
//            filesService.delete(filesEntity);
        }
    }

    @GetMapping("/downloadAllMembers")
    public ResponseEntity<byte[]> getAllMembersToTable(@RequestParam boolean condition) throws IOException, DocumentException {
        FilesEntity filesEntity = filesService.getAllMembersToTable(condition);
        try {
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(filesEntity.getType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment:filename=\"" + filesEntity.getName().trim() + "\"")
                    .body(filesEntity.getData());
        } finally {
//            filesService.delete(filesEntity);
        }
    }

    @GetMapping("/downloadAllMembersWithNoValidLicenseNoContribution")
    public ResponseEntity<byte[]> getAllMembersWithLicenceNotValidAndContributionNotValid() throws IOException, DocumentException {
        FilesEntity filesEntity = filesService.getAllMembersWithLicenceNotValidAndContributionNotValid();
        try {
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(filesEntity.getType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment:filename=\"" + filesEntity.getName().trim() + "\"")
                    .body(filesEntity.getData());
        } finally {
//            filesService.delete(filesEntity);
        }
    }

    @GetMapping("/downloadAllErasedMembers")
    public ResponseEntity<byte[]> getAllErasedMembers() throws IOException, DocumentException {
        FilesEntity filesEntity = filesService.getAllErasedMembers();
        try {
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(filesEntity.getType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment:filename=\"" + filesEntity.getName().trim() + "\"")
                    .body(filesEntity.getData());
        } finally {
//            filesService.delete(filesEntity);
        }
    }

    @GetMapping("/downloadAllMembersWithValidLicenseNoContribution")
    public ResponseEntity<byte[]> getAllMembersWithLicenceValidAndContributionNotValid() throws IOException, DocumentException {
        FilesEntity filesEntity = filesService.getAllMembersWithLicenceValidAndContributionNotValid();
        try {
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(filesEntity.getType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment:filename=\"" + filesEntity.getName().trim() + "\"")
                    .body(filesEntity.getData());
        } finally {
//            filesService.delete(filesEntity);
        }
    }

    @GetMapping("/downloadAllMembersToErased")
    public ResponseEntity<byte[]> getAllMembersToErased() throws IOException, DocumentException {
        FilesEntity filesEntity = filesService.getAllMembersToErased();
        try {
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(filesEntity.getType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment:filename=\"" + filesEntity.getName().trim() + "\"")
                    .body(filesEntity.getData());
        } finally {
//            filesService.delete(filesEntity);
        }
    }

    @GetMapping("/downloadCertificateOfClubMembership/{memberUUID}")
    public ResponseEntity<byte[]> CertificateOfClubMembership(@PathVariable String memberUUID,@RequestParam String reason) throws IOException, DocumentException {
        FilesEntity filesEntity = filesService.CertificateOfClubMembership(memberUUID,reason);
        try {
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(filesEntity.getType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment:filename=\"" + filesEntity.getName().trim() + "\"")
                    .body(filesEntity.getData());
        } finally {
//            filesService.delete(filesEntity);
        }
    }

    @GetMapping("/downloadGunRegistry")
    public ResponseEntity<byte[]> getGunRegistry() throws IOException, DocumentException {
        FilesEntity filesEntity = filesService.getGunRegistry();
        try {
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(filesEntity.getType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment:filename=\"" + filesEntity.getName().trim() + "\"")
                    .body(filesEntity.getData());
        } finally {
//            filesService.delete(filesEntity);
        }
    }

    @GetMapping("/downloadGunTransportCertificate")
    public ResponseEntity<byte[]> getGunTransportCertificate(@RequestParam List<String> guns, @RequestParam String date, @RequestParam String date1) throws IOException, DocumentException {
        LocalDate parse = LocalDate.parse(date);
        LocalDate parse1 = LocalDate.parse(date1);
        FilesEntity filesEntity = filesService.getGunTransportCertificate(guns, parse, parse1);
        try {
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(filesEntity.getType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment:filename=\"" + filesEntity.getName().trim() + "\"")
                    .body(filesEntity.getData());
        } finally {
//            filesService.delete(filesEntity);
        }
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
        try {
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(filesEntity.getType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment:filename=\"" + filesEntity.getName().trim() + "\"")
                    .body(filesEntity.getData());
        } finally {
//            filesService.delete(filesEntity);
        }
    }

    @GetMapping("/downloadJudge/{tournamentUUID}")
    public ResponseEntity<byte[]> getJudge(@PathVariable String tournamentUUID) throws IOException, DocumentException {

        FilesEntity filesEntity = filesService.getJudge(tournamentUUID);
        try {
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(filesEntity.getType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment:filename=\"" + filesEntity.getName().trim() + "\"")
                    .body(filesEntity.getData());
        } finally {
//            filesService.delete(filesEntity);
        }
    }


}
