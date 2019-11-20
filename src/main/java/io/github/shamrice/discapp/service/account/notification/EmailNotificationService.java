package io.github.shamrice.discapp.service.account.notification;

import io.github.shamrice.discapp.service.configuration.ConfigurationProperty;
import io.github.shamrice.discapp.service.configuration.ConfigurationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Slf4j
public class EmailNotificationService {

    @Autowired
    private JavaMailSender emailSender;

    @Autowired
    private ConfigurationService configurationService;

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
        }
        return  null;
    }

}
