package com.shootingplace.shootingplace.email;

import com.shootingplace.shootingplace.contributions.ContributionEntity;
import com.shootingplace.shootingplace.enums.MailType;
import com.shootingplace.shootingplace.enums.ProfilesEnum;
import com.shootingplace.shootingplace.history.HistoryEntity;
import com.shootingplace.shootingplace.history.HistoryRepository;
import com.shootingplace.shootingplace.history.LicensePaymentHistoryEntity;
import com.shootingplace.shootingplace.member.MemberEntity;
import com.shootingplace.shootingplace.member.MemberRepository;
import com.shootingplace.shootingplace.utils.CryptoUtil;
import com.shootingplace.shootingplace.utils.Mapping;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class EmailService {

    private JavaMailSender mailSender;
    private final SentEmailRepository sentEmailRepository;
    private final EmailConfigRepository emailConfigRepository;
    private final MemberRepository memberRepository;
    private final HistoryRepository historyRepository;


    private final Environment environment;

    private final ScheduledEmailRepository scheduledEmailRepository;

    private final Logger LOG = LogManager.getLogger(getClass());

    public EmailService(@Autowired(required = false) JavaMailSender mailSender, SentEmailRepository sentEmailRepository, EmailConfigRepository emailConfigRepository, MemberRepository memberRepository, HistoryRepository historyRepository, Environment environment, ScheduledEmailRepository scheduledEmailRepository) {
        this.mailSender = mailSender;
        this.sentEmailRepository = sentEmailRepository;
        this.emailConfigRepository = emailConfigRepository;
        this.memberRepository = memberRepository;
        this.historyRepository = historyRepository;
        this.environment = environment;
        this.scheduledEmailRepository = scheduledEmailRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendRichEmail(EmailRequest request, String mailType, String memberUUID) throws MessagingException {
        EmailConfig configEntity = emailConfigRepository.findAll().stream().findFirst().orElse(null);
        if (configEntity == null) return;
        mailSender = createMailSender(configEntity);
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(configEntity.getUsername());
        helper.setTo(request.getTo());
        helper.setBcc("odautomatu@ksdziesiatka.pl");
        helper.setSubject(request.getSubject());
        helper.setText(request.getHtmlContent(), true); // HTML
        // Załączniki
        if (request.getAttachments() != null) {
            for (EmailRequest.Attachment attachment : request.getAttachments()) {
                byte[] fileBytes = Base64.getDecoder().decode(attachment.getContentBase64());
                helper.addAttachment(attachment.getFilename(), new ByteArrayResource(fileBytes), attachment.getContentType());
            }
        }
        // Inline images
        if (request.getInlineImages() != null) {
            for (EmailRequest.InlineImage image : request.getInlineImages()) {
                byte[] imageBytes = Base64.getDecoder().decode(image.getContentBase64());
                helper.addInline(image.getContentId(), new ByteArrayResource(imageBytes), image.getContentType());
            }
        }
        sendAndSaveEmail(request, message, mailType, memberUUID);
    }

    public ResponseEntity<?> sendTestEmail(EmailRequest request) throws MessagingException {
        EmailConfig configEntity = emailConfigRepository.findAll().stream().findFirst().orElse(null);
        if (configEntity == null) return ResponseEntity.badRequest().body("brak konfiguracji połączenia z mailem");
        mailSender = createMailSender(configEntity);
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(configEntity.getUsername());
        helper.setTo(request.getTo());
        helper.setBcc("odautomatu@ksdziesiatka.pl");
        helper.setSubject(request.getSubject());
        helper.setText(request.getHtmlContent(), true); // HTML
        // Załączniki
        if (request.getAttachments() != null) {
            for (EmailRequest.Attachment attachment : request.getAttachments()) {
                byte[] fileBytes = Base64.getDecoder().decode(attachment.getContentBase64());
                helper.addAttachment(attachment.getFilename(), new ByteArrayResource(fileBytes), attachment.getContentType());
            }
        }
        // Inline images
        if (request.getInlineImages() != null) {
            for (EmailRequest.InlineImage image : request.getInlineImages()) {
                byte[] imageBytes = Base64.getDecoder().decode(image.getContentBase64());
                helper.addInline(image.getContentId(), new ByteArrayResource(imageBytes), image.getContentType());
            }
        }
        String sent = sendAndSaveEmail(request, message, MailType.TEST.getName(), null);
        return ResponseEntity.ok(sent);
    }

    private String sendAndSaveEmail(EmailRequest request, MimeMessage message, String mailType, String memberUUID) {
        MemberEntity memberEntity = memberRepository.getOne(memberUUID);
        SentEmail log = new SentEmail();

        log.setRecipient(request.getTo());
        log.setSubject(request.getSubject());
        log.setContent(request.getHtmlContent());
        log.setSentAt(LocalDateTime.now());
        log.setMailType(mailType);
        log.setMemberUUID(memberEntity != null ? memberEntity.getUuid() : null);
        try {
            log.setSuccess(true);
            if (environment.getActiveProfiles()[0].equals(ProfilesEnum.TEST.getName())) {
                log.setSuccess(true);
                LOG.info("Udaję, że wysyłam mail");
            } else {
                mailSender.send(message);
                LOG.info("Mail został wysłany do:" + log.getRecipient());
            }
            SentEmail save = sentEmailRepository.save(log);
            if (memberEntity != null) {
                HistoryEntity history = historyRepository.getByUuid(memberEntity.getHistory().getUuid());
                Set<SentEmail> sentEmailsHistory = history.getSentEmailsHistory();
                sentEmailsHistory.add(save);
                history.setSentEmailsHistory(sentEmailsHistory);
                historyRepository.save(history);
                LOG.info("Dodaję maila do historii " + memberEntity.getFullName());
            }
            LOG.info("Wysłano wiadomość i zapisano w bazie");
            return "Wysłano wiadomość i zapisano w bazie";
        } catch (Exception e) {
            log.setSuccess(false);
            log.setErrorMessage(e.getMessage());
            LOG.error(e.getMessage());
            SentEmail save = sentEmailRepository.save(log);
            if (memberEntity != null) {
                HistoryEntity history = historyRepository.getByUuid(memberEntity.getHistory().getUuid());
                Set<SentEmail> sentEmailsHistory = history.getSentEmailsHistory();
                sentEmailsHistory.add(save);
                history.setSentEmailsHistory(sentEmailsHistory);
                historyRepository.save(history);
                LOG.info("Dodaję maila do historii " + memberEntity.getFullName());
            }
            LOG.info("NIE WYSŁANO WIADOMOŚCI DO: " + request.getTo() + " i zapisano w bazie");
            return "NIE WYSŁANO WIADOMOŚCI DO: " + request.getTo() + " i zapisano w bazie";

        }
    }

    public void sendRemindersForActiveOneMonthBefore() {
        memberRepository.findAllByErasedFalseAndActiveTrue()
                .stream()
                .filter(f -> f.getHistory().getContributionList() != null)
                .filter(f -> f.getHistory().getContributionList().get(0).getValidThru().isAfter(LocalDate.now()) && f.getHistory().getContributionList().get(0).getValidThru().isBefore(LocalDate.now().plusMonths(1)))
                .forEach(e -> {
                    String validThru = "BRAK SKŁADEK";
                    if (e.getHistory().getContributionList() != null) {
                        if (e.getHistory().getContributionList().get(0) != null) {
                            validThru = e.getHistory().getContributionList().get(0).getValidThru().toString();
                        }
                    }
                    boolean alreadySent = sentEmailRepository.getEmailsByMemberUUIDAndMailTypeAndSuccessTrueAndSentAtIsBetween(
                            e.getUuid(),
                            MailType.SUBSCRIPTION_REMINDER_BEFORE.getName(),
                            LocalDateTime.now().minusMonths(1),
                            LocalDateTime.now()).size() > 0;
                    boolean notYetSent = scheduledEmailRepository.findTodayByMemberAndMailType(e.getUuid(),
                            MailType.SUBSCRIPTION_REMINDER_BEFORE.getName()).size() > 0;
                    if (alreadySent || notYetSent) {
                        LOG.info("Wiadomość do: " + e.getEmail() + " nie będzie wysłana bo była wysłana mniej niż miesiąc temu.");
                    } else {
                        ScheduledEmail email = new ScheduledEmail();
                        email.setRecipient(e.getEmail());
                        email.setScheduledFor(LocalDateTime.now().plusMinutes(60));
                        email.setMailType(MailType.SUBSCRIPTION_REMINDER_BEFORE.getName());
                        email.setMemberUUID(e.getUuid());
                        email.setSubject("Przypomnienie o Składkach Członkowskich KS Dziesiątka LOK Łódź");
                        email.setHtmlContent("<p>Dzień Dobry,</p>\n" +
                                "<p>Przypominamy, że za nie długo twoja składka członkowska traci ważność.</p>\n" +
                                "<p>Twoja składka wygasa dnia: <b>" + validThru + "</b></p>\n" +
                                "<p>Prosimy o opłacenie składki gotówką na miejscu w Klubie, w dniach i godzinach funkcjonowania Klubu.</p>\n" +
                                "<p>Przypominamy również, że osoba zalegająca ze składkami członkowskimi powyżej 6 miesięcy może zostać usunięta z Klubu.</p>\n" +
                                "<p>Pozdrawiamy</p>\n" +
                                "<p>Zespół KS Dziesiątka LOK Łódź</p>\n" +
                                "<div>-------------</div>\n" +
                                "<div>Wiadomość automatyczna - prosimy na nią nie odpowiadać.<br />");
                        scheduledEmailRepository.save(email);
                        LOG.info("Zapisano mail do późniejszej wysyłki " + email.getRecipient());
                    }
                });
    }

    public void sendRemindersForNonActive() {
        memberRepository.findAllByErasedFalseAndActiveFalse().forEach(e -> {
            String validThru = "BRAK SKŁADEK";
            if (e.getHistory().getContributionList() != null) {
                if (e.getHistory().getContributionList().get(0) != null) {
                    validThru = e.getHistory().getContributionList().get(0).getValidThru().toString();
                }
            }
            boolean alreadySent = sentEmailRepository.getEmailsByMemberUUIDAndMailTypeAndSuccessTrueAndSentAtIsBetween(
                    e.getUuid(),
                    MailType.SUBSCRIPTION_REMINDER.getName(),
                    LocalDateTime.now().minusMonths(1),
                    LocalDateTime.now()).size() > 0;
            boolean notYetSent = scheduledEmailRepository.findTodayByMemberAndMailType(e.getUuid(),
                    MailType.SUBSCRIPTION_REMINDER.getName()).size() > 0;
            if (alreadySent || notYetSent) {
                LOG.info("Wiadomość do: " + e.getEmail() + " nie będzie wysłana bo była wysłana mniej niż miesiąc temu.");
            } else {
                ScheduledEmail email = new ScheduledEmail();
                email.setRecipient(e.getEmail());
                email.setScheduledFor(LocalDateTime.now().plusMinutes(60));
                email.setMailType(MailType.SUBSCRIPTION_REMINDER.getName());
                email.setMemberUUID(e.getUuid());
                email.setSubject("Przypomnienie o Składkach Członkowskich KS Dziesiątka LOK Łódź");
                email.setHtmlContent("<p>Dzień Dobry,</p>\n" +
                        "<p>Przypominamy o konieczności opłacenia składki członkowskiej.</p>\n" +
                        "<p>Twoja składka wygasła dnia: <b>" + validThru + "</b></p>\n" +
                        "<p>Prosimy o opłacenie składek gotówką na miejscu w Klubie, w dniach i godzinach funkcjonowania Klubu.</p>\n" +
                        "<p>Przypominamy również, że osoba zalegająca ze składkami członkowskimi powyżej 6 miesięcy może zostać usunięta z Klubu.</p>\n" +
                        "<p>Pozdrawiamy</p>\n" +
                        "<p>Zespół KS Dziesiątka LOK Łódź</p>\n" +
                        "<div>-------------</div>\n" +
                        "<div>Wiadomość automatyczna - prosimy na nią nie odpowiadać.<br />");
                scheduledEmailRepository.save(email);
                LOG.info("Zapisano mail do późniejszej wysyłki " + email.getRecipient());
            }
        });
    }

    public void sendCongratulationsOnTheAnniversary() {
        memberRepository.findAllByErasedFalse()
                .stream()
                .filter(f -> LocalDate.of(0,
                                LocalDate.now().getMonth(),
                                LocalDate.now().getDayOfMonth())
                        .equals(LocalDate.of(0,
                                f.getJoinDate().getMonth(),
                                f.getJoinDate().getDayOfMonth())))
                .forEach(e -> {
                    String s = "Dokładnie rok temu";
                    if (LocalDate.now().getYear() - e.getJoinDate().getYear() > 1) {
                        s = "To już kolejny rok jak";
                    }
                    ScheduledEmail email = new ScheduledEmail();
                    email.setRecipient(e.getEmail());
                    email.setScheduledFor(LocalDateTime.now().plusMinutes(60));
                    email.setMailType(MailType.CONGRATULATIONS_ANNIVERSSARY.getName());
                    email.setMemberUUID(e.getUuid());
                    email.setSubject("Dziękujemy, że jesteś z nami!\n");
                    email.setHtmlContent("<p>Dzień dobry,</p>\n" +
                            "<p>" + s + " dołączyłeś do <strong>Klubu Strzeleckiego Dziesiątka LOK Łódź</strong>!</p>\n" +
                            "<p>Chcemy Ci podziękować za ten wspólny czas i zaangażowanie. Cieszymy się, że jesteś częścią naszej społeczności pasjonatów strzelectwa.</p>\n" +
                            "<p>Mamy nadzieję, że kolejny rok przyniesie Ci jeszcze więcej celnych strzałów i satysfakcji z treningów oraz udziału w zawodach.</p>\n" +
                            "<p>Do zobaczenia na strzelnicy!</p>\n" +
                            "<p>Pozdrawiamy</p>\n" +
                            "<p>Zespół KS Dziesiątka LOK Łódź</p>\n" +
                            "<div>-------------</div>\n" +
                            "<div>Wiadomość automatyczna - prosimy na nią nie odpowiadać.<br />");
                    scheduledEmailRepository.save(email);
                    LOG.info("Zapisano mail do późniejszej wysyłki " + email.getRecipient());
                });
    }

    public void sendRegistrationConfirmation(String memberUUID) {
        MemberEntity memberEntity = memberRepository.getOne(memberUUID);
        ScheduledEmail email = new ScheduledEmail();
        email.setRecipient(memberEntity.getEmail());
        email.setSubject("Witamy w Gronie Klubowiczów KS Dziesiątka LOK Łódź!");
        String s = memberEntity.getSex() ? "Zapisałeś" : "Zapisałaś";
        email.setHtmlContent("<p>Gratulacje!</p>\n" +
                "<p>" + s + " się właśnie do Klubu Strzeleckiego \"Dziesiątka\" Ligi Obrony Kraju w Łodzi.</p>\n" +
                "<p>Nasze dni i godziny funkcjonowania Klubu to:</p>\n" +
                "<p>poniedziałek 13:00 - 20:00<br />wtorek 16:30 - 20:00<br />środa 13:00 - 20:00<br />czwartek - nieczynne<br />piątek 16:30 - 20:00<br />sobota - nieczynne<br />niedziela - nieczynne</p>\n" +
                "<p>Kalendarz imprez oraz aktualne informacje znajdziesz na naszej stronie:<br /><a href=\"https://www.ksdziesiatka.pl\">www.ksdziesiatka.pl</a></p>\n" +
                "<div>-------------</div>\n" +
                "<div>Wiadomość automatyczna - prosimy na nią nie odpowiadać.<br />");
        email.setScheduledFor(LocalDateTime.now().plusMinutes(1));
        email.setMailType(MailType.REGISTRATION_CONFIRMATION.getName());
        email.setMemberUUID(memberEntity.getUuid());
        scheduledEmailRepository.save(email);
        LOG.info("Zapisano mail do późniejszej wysyłki " + email.getRecipient());

    }

    public void sendContributionConfirmation(String memberUUID) {
        MemberEntity memberEntity = memberRepository.getOne(memberUUID);

        ContributionEntity contributionEntity = memberEntity.getHistory().getContributionList().get(0);

        int count = (int) memberEntity.getHistory().getContributionList().stream().filter(f -> f.getPaymentDay().equals(contributionEntity.getPaymentDay())).count();
        LocalDate contribution = contributionEntity.getPaymentDay();
        LocalDate validThru = contributionEntity.getValidThru();
        String contributionLevel = String.valueOf(90 * count);
        String counter;
        switch (count) {
            case 1:
                counter = "jedną kwartalną składkę członkowską";
                break;
            case 2:
                counter = "dwie kwartalne składki członkowskie";
                break;
            case 3:
                counter = "trzy kwartalne składki członkowskie";
                break;
            case 4:
                counter = "cztery kwartalne składki członkowskie";
                break;
            case 5:
                counter = "pięć kwartalnych składkek członkowskich";
                break;
            case 6:
                counter = "sześć kwartalnych składkek członkowskich";
                break;
            case 7:
                counter = "siedem kwartalnych składkek członkowskich";
                break;
            case 8:
                counter = "osiem kwartalnych składkek członkowskich";
                break;
            case 9:
                counter = "dziewięć kwartalnych składkek członkowskich";
                break;
            case 10:
                counter = "dziesięć kwartalnych składkek członkowskich";
                break;
            default:
                counter = count + " kwartalnych składkek członkowskich";
                break;
        }
        ScheduledEmail email = new ScheduledEmail();
        email.setRecipient(memberEntity.getEmail());
        email.setSubject("Potwierdzenie Opłacenia Składki Członkowskiej KS Dziesiątka LOK Łódź");
        email.setHtmlContent("<p>Dnia <b>" + contribution + "</b> opłacono " + counter + " w wysokości " + contributionLevel + "zł.</p>\n" +
                "<p>Data ważności Twoich aktualnych składek członkowskich to: <b>" + validThru + "</b></p><br/>\n" +
                "<p>Dziękujemy!</p>\n" +
                "<p>Zespół KS Dziesiątka LOK Łódź</p>\n" +
                "<div>-------------</div>\n" +
                "<div>Wiadomość automatyczna - prosimy na nią nie odpowiadać.</div>");
        email.setScheduledFor(LocalDateTime.now().plusMinutes(2));
        email.setMailType(MailType.CONTRIBUTION_CONFIRMATION.getName());
        email.setMemberUUID(memberEntity.getUuid());
        scheduledEmailRepository.save(email);
        LOG.info("Zapisano mail do późniejszej wysyłki " + email.getRecipient());

    }

    public void sendLicensePaymentConfirmation(String memberUUID) {
        MemberEntity memberEntity = memberRepository.getOne(memberUUID);
        LicensePaymentHistoryEntity licensePaymentHistoryEntity = memberEntity.getHistory().getLicensePaymentHistory().get(0);
        LocalDate paymentDate = licensePaymentHistoryEntity.getDate();
        Integer validForYear = licensePaymentHistoryEntity.getValidForYear();
        ScheduledEmail email = new ScheduledEmail();
        email.setRecipient(memberEntity.getEmail());
        email.setSubject("Potwierdzenie Opłacenia Licencji zawodniczej KS Dziesiątka LOK Łódź");
        email.setHtmlContent("<p>Dnia <b>" + paymentDate + "</b> opłacono licencję zawodniczą na rok " + validForYear + ".</p>\n" +
                "<p>W najbliższym czasie zostanie dokonana opłata w systemie PZSS</p><br/>\n" +
                "<p>Dziękujemy!</p>\n" +
                "<p>Zespół KS Dziesiątka LOK Łódź</p>\n" +
                "<div>-------------</div>\n" +
                "<div>Wiadomość automatyczna - prosimy na nią nie odpowiadać.</div>");
        email.setScheduledFor(LocalDateTime.now().plusMinutes(2));
        email.setMailType(MailType.LICENSE_PAYMENT_CONFIRMATION.getName());
        email.setMemberUUID(memberEntity.getUuid());
        scheduledEmailRepository.save(email);
        LOG.info("Zapisano mail do późniejszej wysyłki " + email.getRecipient());

    }

    public ResponseEntity<?> saveConnection(EmailConfig emailConfig) {
        LOG.info("Zapisuję połączenie: " + emailConfig.getConnectionName() + "\n" + emailConfig.getHost() + "\n" + emailConfig.getUsername());
        emailConfigRepository.save(emailConfig);
        return ResponseEntity.ok("zapisano połączenie");
    }

    public ResponseEntity<?> getAllConnections() {
        List<EmailConfig> list = new ArrayList<>();
        emailConfigRepository.findAll().forEach(e -> {
            e.setPassword("###");
            list.add(e);
        });
        return ResponseEntity.ok(list);
    }

    public ResponseEntity<?> deleteScheduledMail(String uuid) {
        scheduledEmailRepository.delete(scheduledEmailRepository.getByUuid(uuid));
        return ResponseEntity.ok("skasowano");
    }

    public ResponseEntity<?> saveScheduledEmail(EmailRequest request, String uuid) {
        scheduledEmailRepository.getByUuid(uuid).setHtmlContent(request.getHtmlContent());
        return ResponseEntity.ok("Zmieniono treść wiadomości");
    }
    private JavaMailSender createMailSender(EmailConfig config) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(config.getHost());
        mailSender.setPort(config.getPort());
        mailSender.setUsername(config.getUsername());
        mailSender.setPassword(config.getPassword());

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.smtp.auth", config.isAuth());
        props.put("mail.smtp.starttls.enable", config.isStarttls());
        props.put("mail.smtp.ssl.trust", config.getSslTrust());

        return mailSender;
    }

    public ResponseEntity<?> editConnection(EmailConfig newConfig, String uuid) {

        EmailConfig oldConfig = emailConfigRepository.getOne(uuid);

        if (newConfig.getConnectionName() != null && !newConfig.getConnectionName().isEmpty()) {
            oldConfig.setConnectionName(newConfig.getConnectionName());
        }
        if (newConfig.getHost() != null && !newConfig.getHost().isEmpty()) {
            oldConfig.setHost(newConfig.getHost());
        }
        if (newConfig.getPort() != null) {
            oldConfig.setPort(newConfig.getPort());
        }
        if (newConfig.getUsername() != null && !newConfig.getUsername().isEmpty()) {
            oldConfig.setUsername(newConfig.getUsername());
        }
        if (newConfig.getPassword() != null && !newConfig.getPassword().isEmpty()) {
            oldConfig.setPassword(CryptoUtil.encrypt(newConfig.getPassword()));
        }

        emailConfigRepository.save(oldConfig);

        return ResponseEntity.ok("Zapisano");
    }

    public ResponseEntity<?> getScheduledEmails() {
        List<ScheduledEmailDTO> scheduledEmailDTOList = new ArrayList<>();
        scheduledEmailRepository.findAll().forEach(e->{
            ScheduledEmailDTO map = Mapping.map(e);
            map.setMemberName(memberRepository.getOne(e.getMemberUUID()).getFullName());
            scheduledEmailDTOList.add(map);
        });
        return ResponseEntity.ok(scheduledEmailDTOList);

    }
    public ResponseEntity<?> getSentEmails(LocalDate firstDate, LocalDate secondDate) {
        List<SentEmailDTO> sentEmailDTOList = new ArrayList<>();
        sentEmailRepository.getEmailSentBetween(firstDate,secondDate.plusDays(1)).forEach(e-> {
            SentEmailDTO map = Mapping.map(e);
            map.setMemberName(memberRepository.getOne(e.getMemberUUID()).getFullName());
            sentEmailDTOList.add(map);
        });
        return ResponseEntity.ok(sentEmailDTOList);
    }
}
