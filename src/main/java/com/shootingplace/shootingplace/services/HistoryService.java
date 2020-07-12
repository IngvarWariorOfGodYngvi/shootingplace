package com.shootingplace.shootingplace.services;

import com.shootingplace.shootingplace.domain.entities.ContributionEntity;
import com.shootingplace.shootingplace.domain.entities.HistoryEntity;
import com.shootingplace.shootingplace.domain.entities.MemberEntity;
import com.shootingplace.shootingplace.domain.models.History;
import com.shootingplace.shootingplace.repositories.ContributionRepository;
import com.shootingplace.shootingplace.repositories.HistoryRepository;
import com.shootingplace.shootingplace.repositories.MemberRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class HistoryService {

    private final HistoryRepository historyRepository;
    private final ContributionRepository contributionRepository;
    private final MemberRepository memberRepository;
    private final Logger LOG = LogManager.getLogger(getClass());


    public HistoryService(HistoryRepository historyRepository, ContributionRepository contributionRepository, MemberRepository memberRepository) {
        this.historyRepository = historyRepository;
        this.contributionRepository = contributionRepository;
        this.memberRepository = memberRepository;
    }

    void createRecord(UUID memberUUID, History history) {
        MemberEntity memberEntity = memberRepository.findById(memberUUID)
                .orElseThrow(EntityNotFoundException::new);
        HistoryEntity historyEntity = Mapping.map(history);
        historyEntity.setRecord(LocalDate.now().toString());
        historyRepository.saveAndFlush(historyEntity);
        ContributionEntity contributionEntity = contributionRepository.findById(memberEntity.getContribution().getUuid())
                .orElseThrow(EntityNotFoundException::new);
        contributionEntity.setHistory(historyEntity);
        contributionRepository.saveAndFlush(contributionEntity);
        memberEntity.setContribution(contributionEntity);
        memberRepository.saveAndFlush(memberEntity);
        LOG.info("Data została zapisana");
    }

    void editRecord(UUID historyUUID, String newDate) {
        HistoryEntity historyEntity = historyRepository.findById(historyUUID)
                .orElseThrow(EntityNotFoundException::new);
        String newState = historyEntity.getRecord().concat(" ; " + newDate);
        historyEntity.setRecord(newState);
        historyRepository.saveAndFlush(historyEntity);
    }

}
