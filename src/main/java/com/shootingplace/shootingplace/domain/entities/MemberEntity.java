package com.shootingplace.shootingplace.domain.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.shootingplace.shootingplace.validators.ValidPESEL;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.*;
import javax.validation.constraints.Pattern;
import java.time.LocalDate;
import java.util.UUID;


@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MemberEntity {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID uuid;

    private LocalDate joinDate;
    private Integer legitimationNumber;
    @NotBlank
    private String firstName;
    @NotBlank
    private String secondName;
    @OneToOne(orphanRemoval = true)
    private LicenseEntity license;
    @OneToOne(orphanRemoval = true)
    private ShootingPatentEntity shootingPatent;
    @NotBlank
    @Email
    private String email;
    @ValidPESEL
    @Pattern(regexp = "[0-9]*")
    private String pesel;
    private String IDCard;

    @ManyToOne @EqualsAndHashCode.Exclude
    @ToString.Exclude @JsonIgnoreProperties("members")
    private AddressEntity address;
    @NotBlank
    @Pattern(regexp = "^\\+[0-9]{11}$")
    private String phoneNumber;
    private Boolean weaponPermission;
    @OneToOne(orphanRemoval = true)
    private ContributionEntity contribution;
    private Boolean active = false;
}
