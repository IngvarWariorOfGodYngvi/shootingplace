package com.shootingplace.shootingplace.email;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Getter
@Setter
public class EmailSendList {
    @Id
    @GeneratedValue(generator = "id")
    @GenericGenerator(name = "id", strategy = "org.hibernate.id.UUIDGenerator")
    private String uuid;

    private boolean sendRemindersForActiveOneMonthBefore;
    private boolean sendRemindersForNonActive;
    private boolean sendCongratulationsOnTheAnniversary;
    private boolean sendRegistrationConfirmation;
    private boolean sendContributionConfirmation;
    private boolean sendLicensePaymentConfirmation;
    private boolean sendIndividual;

}
