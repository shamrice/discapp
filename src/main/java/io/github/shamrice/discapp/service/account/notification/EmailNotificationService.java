package io.github.shamrice.discapp.service.account.notification;

import io.github.shamrice.discapp.service.configuration.ConfigurationProperty;
import io.github.shamrice.discapp.service.configuration.ConfigurationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import javax.mail.SendFailedException;
import java.util.*;

@Component
@Slf4j
public class EmailNotificationService {

    @Autowired
    private JavaMailSender emailSender;

    @Autowired
    private ConfigurationService configurationService;

    public void send(String to, NotificationType notificationType, Map<String, Object> templateParams) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);

        String subject = getSubjectForType(notificationType);
        String body = getBodyForType(notificationType);

        if (subject == null || body == null) {
            log.error("Failed to find subject or message body for notification type: " + notificationType.name()
                    + " :: message not sent.");
            return;
        }

        //replace template parameters with values in body
        for (String key : templateParams.keySet()) {
            body = body.replace(key, templateParams.get(key).toString());
        }
        body = body.replace("\\r\\n", "\r\n");
        message.setSubject(subject);
        message.setText(body);
        emailSender.send(message);
        log.info("Sent password reset request to: " + to + " message: " + message.toString());

    }

    private String getSubjectForType(NotificationType notificationType) {
        switch (notificationType) {
            case PASSWORD_RESET:
                return configurationService.getStringValue(0L, ConfigurationProperty.EMAIL_PASSWORD_RESET_SUBJECT, null);
        }

        return null;
    }

    private String getBodyForType(NotificationType notificationType) {
        switch (notificationType) {
            case PASSWORD_RESET:
                return configurationService.getStringValue(0L, ConfigurationProperty.EMAIL_PASSWORD_RESET_MESSAGE, null);

        }
        return  null;
    }

}
