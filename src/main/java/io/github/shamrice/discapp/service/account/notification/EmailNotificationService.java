package io.github.shamrice.discapp.service.account.notification;

import io.github.shamrice.discapp.service.configuration.ConfigurationProperty;
import io.github.shamrice.discapp.service.configuration.ConfigurationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.*;

@Component
@Slf4j
public class EmailNotificationService {

    @Autowired
    private JavaMailSender emailSender;

    @Autowired
    private ConfigurationService configurationService;

    public boolean sendMimeMessage(String to, NotificationType notificationType,
                                   Map<String, Object> subjectTemplateParams, Map<String, Object> bodyTemplateParams) {
        try {
            String subject = getSubjectForType(notificationType);
            String body = getBodyForType(notificationType);

            if (subject == null || body == null) {
                log.error("Failed to get subject or body template configurations for notification type: " + notificationType.name());
                return false;
            }

            //replace template parameters with values in body and subject
            for (String key : subjectTemplateParams.keySet()) {
                subject = subject.replace(key, subjectTemplateParams.get(key).toString());
            }
            for (String key : bodyTemplateParams.keySet()) {
                body = body.replace(key, bodyTemplateParams.get(key).toString());
            }

            MimeMessage mimeMessage = emailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, "UTF-8");
            mimeMessageHelper.setTo(to);
            mimeMessageHelper.setSubject(subject);
            mimeMessageHelper.setText(body, true);

            emailSender.send(mimeMessage);
            log.info("Sent email message to: " + to + " with subject: " + subject + " body: " + body
                    + " for notification type: " + notificationType.name());
            return true;
        } catch (MessagingException ex) {
            log.error("Failed to send message to: " + to + " notification type: " + notificationType.name() + " :: " + ex.getMessage(), ex);
        }
        return false;
    }

    public boolean send(String to, NotificationType notificationType, Map<String, Object> templateParams) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);

        String subject = getSubjectForType(notificationType);
        String body = getBodyForType(notificationType);

        if (subject == null || body == null) {
            log.error("Failed to find subject or message body for notification type: " + notificationType.name()
                    + " :: message not sent.");
            return false;
        }

        //replace template parameters with values in body
        for (String key : templateParams.keySet()) {
            body = body.replace(key, templateParams.get(key).toString());
        }
        body = body.replace("\\r\\n", "\r\n");
        message.setSubject(subject);
        message.setText(body);
        try {
            emailSender.send(message);
            log.info("Sent " + notificationType.name() + " email to: " + to + " message: " + message.toString());
        } catch (Exception sendExc) {
            log.error("Failed to send email notification to: " + to + " : notificationType: " + notificationType.name()
                    + " : subject: " + subject + " + body: " + body + " :: Error sending: "
                    + sendExc.getMessage(), sendExc);
            return false;
        }

        return true;
    }

    private String getSubjectForType(NotificationType notificationType) {
        switch (notificationType) {
            case PASSWORD_RESET:
                return configurationService.getStringValue(ConfigurationService.SITE_WIDE_CONFIGURATION_APP_ID, ConfigurationProperty.EMAIL_PASSWORD_RESET_SUBJECT, null);
            case NEW_ACCOUNT_CREATED:
                return configurationService.getStringValue(ConfigurationService.SITE_WIDE_CONFIGURATION_APP_ID, ConfigurationProperty.EMAIL_NEW_ACCOUNT_CREATED_SUBJECT, null);
            case ACCOUNT_LOCKED:
                return configurationService.getStringValue(ConfigurationService.SITE_WIDE_CONFIGURATION_APP_ID, ConfigurationProperty.EMAIL_ACCOUNT_LOCKED_SUBJECT, null);
            case MAILING_LIST_CONFIRMATION:
                return configurationService.getStringValue(ConfigurationService.SITE_WIDE_CONFIGURATION_APP_ID, ConfigurationProperty.MAILING_LIST_CONFIRMATION_EMAIL_SUBJECT_TEMPLATE, null);
        }

        return null;
    }

    private String getBodyForType(NotificationType notificationType) {
        switch (notificationType) {
            case PASSWORD_RESET:
                return configurationService.getStringValue(ConfigurationService.SITE_WIDE_CONFIGURATION_APP_ID, ConfigurationProperty.EMAIL_PASSWORD_RESET_MESSAGE, null);
            case NEW_ACCOUNT_CREATED:
                return configurationService.getStringValue(ConfigurationService.SITE_WIDE_CONFIGURATION_APP_ID, ConfigurationProperty.EMAIL_NEW_ACCOUNT_CREATED_MESSAGE, null);
            case ACCOUNT_LOCKED:
                return configurationService.getStringValue(ConfigurationService.SITE_WIDE_CONFIGURATION_APP_ID, ConfigurationProperty.EMAIL_ACCOUNT_LOCKED_MESSAGE, null);
            case MAILING_LIST_CONFIRMATION:
                return configurationService.getStringValue(ConfigurationService.SITE_WIDE_CONFIGURATION_APP_ID, ConfigurationProperty.MAILING_LIST_CONFIRMATION_EMAIL_BODY_TEMPLATE, null);
        }
        return  null;
    }

}
