package com.shootingplace.shootingplace.controllers;

import com.shootingplace.shootingplace.domain.entities.FilesEntity;
import com.shootingplace.shootingplace.repositories.MemberRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/files")
@CrossOrigin
public class FilesController {

    private final MemberRepository memberRepository;


    public FilesController( MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @GetMapping("/downloadContribution/{memberUUID}")
    public ResponseEntity<byte[]> getContributionFile(@PathVariable UUID memberUUID) {
        FilesEntity filesEntity = memberRepository.findById(memberUUID).get().getContributionFile();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(filesEntity.getType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment:filename=\"" + filesEntity.getName() + "\"")
                .body(filesEntity.getData());

    }

    @GetMapping("/downloadPersonalCard/{memberUUID}")
    public ResponseEntity<byte[]> getPersonalCardFile(@PathVariable UUID memberUUID) {
        FilesEntity filesEntity = memberRepository.findById(memberUUID).get().getPersonalCardFile();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(filesEntity.getType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment:filename=\"" + filesEntity.getName() + "\"")
                .body(filesEntity.getData());

    }

}
