package io.github.shamrice.discapp.service.application;

import io.github.shamrice.discapp.data.model.ApplicationSubscription;
import io.github.shamrice.discapp.data.repository.ApplicationSubscriptionRepository;
import io.github.shamrice.discapp.service.account.notification.EmailNotificationService;
import io.github.shamrice.discapp.service.account.notification.NotificationType;
import io.github.shamrice.discapp.service.configuration.ConfigurationProperty;
import io.github.shamrice.discapp.service.configuration.ConfigurationService;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@NoArgsConstructor
@Slf4j
public class ApplicationSubscriptionService {

    @Autowired
    private ApplicationSubscriptionRepository applicationSubscriptionRepository;

    @Autowired
    private EmailNotificationService emailNotificationService;

    @Autowired
    private ConfigurationService configurationService;

    public List<ApplicationSubscription> getSubscribers(long appId) {
        return applicationSubscriptionRepository.findByApplicationIdAndEnabled(appId, true);
    }

    public boolean isEmailAlreadySubscribed(long appId, String emailAddress) {

        if (emailAddress == null || emailAddress.trim().isEmpty()) {
            return false;
        }

        ApplicationSubscription subscription = applicationSubscriptionRepository.findByApplicationIdAndSubscriberEmail(appId, emailAddress);
        return subscription != null && subscription.getEnabled();
    }

    public void unsubscribeFromApplication(long appId, String emailAddress) {
        ApplicationSubscription subscription = applicationSubscriptionRepository.findByApplicationIdAndSubscriberEmail(appId, emailAddress);
        if (subscription != null) {
            subscription.setEnabled(false);
            subscription.setModDt(new Date());
            applicationSubscriptionRepository.save(subscription);
            log.info("Unsubscribed email: " + emailAddress + " from application: " + appId);
        } else {
            log.info("No subscription exists to unsubscribe for. appId: " + appId + " : email: " + emailAddress);
        }
    }

    public void createSubscriptionRequest(long appId, String appName, String confirmationUrl, String confirmationMessage,
                                          String emailAddress) {
        //generate confirmation code
        int generatedConfirmationCode = new Random().nextInt(1000000);
        confirmationUrl += generatedConfirmationCode;

        //update existing record or create new one. enabled is false until confirmation is returned by user.
        ApplicationSubscription subscription = applicationSubscriptionRepository.findByApplicationIdAndSubscriberEmail(appId, emailAddress);

        if (subscription == null) {
            subscription = new ApplicationSubscription();
            subscription.setApplicationId(appId);
            subscription.setSubscriberEmail(emailAddress);
            subscription.setCreateDt(new Date());
        }

        subscription.setModDt(new Date());
        subscription.setEnabled(false);
        subscription.setConfirmationCode(generatedConfirmationCode);

        applicationSubscriptionRepository.save(subscription);
        log.info("Added pending application subscription to appId: " + appId + " for email address: " + emailAddress);

        //send out email message to user for confirmation.
        Map<String, Object> subjectParams = new HashMap<>();
        subjectParams.put("APPLICATION_NAME", appName);

        Map<String, Object> bodyParams = new HashMap<>();
        bodyParams.put("APPLICATION_NAME", appName);
        bodyParams.put("CONFIRMATION_MESSAGE", confirmationMessage);
        bodyParams.put("CONFIRMATION_URL", confirmationUrl);

        emailNotificationService.sendMimeMessage(emailAddress, NotificationType.MAILING_LIST_CONFIRMATION, subjectParams, bodyParams);
    }

    public boolean subscribeToApplication(long appId, String emailAddress, int confirmationCode) {

        ApplicationSubscription subscription = applicationSubscriptionRepository.findByApplicationIdAndSubscriberEmail(appId, emailAddress);

        if (subscription != null && subscription.getConfirmationCode().equals(confirmationCode)) {
            subscription.setModDt(new Date());
            subscription.setEnabled(true);
            applicationSubscriptionRepository.save(subscription);
            log.info("Added confirmed application subscription to appId: " +appId + " for email address: " + emailAddress);
            return true;
        } else {
            log.warn("Pending subscription was either not found or confirmation codes do not match. No subscription added for email address: " + emailAddress);
            return false;
        }
    }

    //todo : should this be moved to just email notification service or somewhere else besides this service?
    public void sendReplyEmailNotification(long appId, String appName, String discussionUrl, String emailAddress, long newThreadId) {

        //send out email message
        Map<String, Object> subjectParams = new HashMap<>();
        subjectParams.put("APPLICATION_NAME", appName);

        Map<String, Object> bodyParams = new HashMap<>();
        bodyParams.put("APPLICATION_NAME", appName);
        bodyParams.put("APP_ID", appId);
        bodyParams.put("APP_DISCUSSION_URL", discussionUrl);
        bodyParams.put("THREAD_ID", newThreadId);

        emailNotificationService.sendMimeMessage(emailAddress, NotificationType.REPLY_NOTIFICATION, subjectParams, bodyParams);
        log.info("Sent reply notification for thread: " + newThreadId + " to: " + emailAddress);
    }
}
