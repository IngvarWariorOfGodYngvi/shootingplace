package com.shootingplace.shootingplace.Strategies;

public interface ProfileContext {
    ContributionStrategy getContributionStrategy();
    EmailStrategy getEmailStrategy();
    SystemPropertiesStrategy getSystemPropertiesStrategy();
}


