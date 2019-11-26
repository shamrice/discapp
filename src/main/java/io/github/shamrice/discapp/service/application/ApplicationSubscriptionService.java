package io.github.shamrice.discapp.service.application;

import io.github.shamrice.discapp.data.model.ApplicationSubscription;
import io.github.shamrice.discapp.data.repository.ApplicationSubscriptionRepository;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@NoArgsConstructor
@Slf4j
public class ApplicationSubscriptionService {

    @Autowired
    private ApplicationSubscriptionRepository applicationSubscriptionRepository;

    public void unsubscribeFromApplication(long appId, String emailAddress) {
        ApplicationSubscription subscription = applicationSubscriptionRepository.findByApplicationIdAndSubscriberEmail(appId, emailAddress);
        if (subscription != null) {
            subscription.setEnabled(false);
            subscription.setModDt(new Date());
            if (applicationSubscriptionRepository.save(subscription) != null) {
                log.info("Unsubscribed email: " + emailAddress + " from application: " + appId);
            }
        } else {
            log.info("No subscription exists to unsubscribe for. appId: " + appId + " : email: " + emailAddress);
        }
    }

    public void subscribeToApplication(long appId, String emailAddress) {

        ApplicationSubscription subscription = applicationSubscriptionRepository.findByApplicationIdAndSubscriberEmail(appId, emailAddress);

        if (subscription == null) {
            subscription = new ApplicationSubscription();
            subscription.setApplicationId(appId);
            subscription.setSubscriberEmail(emailAddress);
            subscription.setEnabled(true);
            subscription.setCreateDt(new Date());
            subscription.setModDt(new Date());
        } else {
            subscription.setModDt(new Date());
            subscription.setEnabled(true);
        }

        if (applicationSubscriptionRepository.save(subscription) != null) {
            log.info("Added application subscription to appId: " +appId + " for email address: " + emailAddress);
        }
    }
}
