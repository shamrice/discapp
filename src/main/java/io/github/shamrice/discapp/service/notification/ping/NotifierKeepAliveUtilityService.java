package io.github.shamrice.discapp.service.notification.ping;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Calendar;

@Service
@Slf4j
public class NotifierKeepAliveUtilityService {

    @Value("${discapp.notifier.url}")
    private String notifierUrl;

    @Value("${discapp.notifier.send-hour}")
    private int sendHour;

    public void start() {
        Thread pingThread = new Thread(this::run);
        pingThread.setName(NotifierKeepAliveUtilityService.class.getSimpleName());
        pingThread.start();
    }


    private void run() {
        log.info("Starting notifier keep alive service.");

        while (true) {

            Calendar calendar = Calendar.getInstance();
            int currentHour = calendar.get(Calendar.HOUR);

            if (currentHour >= sendHour - 1 && currentHour <= sendHour + 1) {
                log.info("Current hour: " + currentHour + " is close to send hour: " + sendHour
                        + ": Creating notification service ping.");

                try {
                    RestTemplate restTemplate = new RestTemplate();
                    ResponseEntity<String> getResponse = restTemplate.getForEntity(notifierUrl, String.class);
                    log.info("Notifier response: " + getResponse.toString());
                    if (!getResponse.getStatusCode().is2xxSuccessful()) {
                        log.warn("Error getting notifier check url. Did not return status 200!");
                    }
                } catch (Exception ex) {
                    log.error("Error getting notifier ping url. " + ex.getMessage(), ex);
                }
            } else {
                log.info("Current hour is: " + currentHour + " : not close to send hour: " + sendHour + ". Skipping notifier ping.");
            }

            try {
                log.info("Sleeping notifier keep alive thread for 5 minutes.");
                Thread.sleep(300000);
            } catch (InterruptedException ex) {
                log.error("Error sleeping thread for " + NotifierKeepAliveUtilityService.class.getSimpleName());
            }
        }
    }
}
