package com.shootingplace.shootingplace.ammoEvidence;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class AmmoInEvidenceDTO {

    private String caliberName;

    private Integer quantity;

    private List<AmmoUsedToEvidenceDTO> ammoUsedToEvidenceDTOList;
}
