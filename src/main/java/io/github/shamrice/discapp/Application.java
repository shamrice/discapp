package io.github.shamrice.discapp;

import io.github.shamrice.discapp.service.notification.ping.NotifierKeepAliveUtilityService;
import io.github.shamrice.discapp.service.notification.email.EmailNotificationQueueService;
import io.github.shamrice.discapp.service.stats.QueuedPageViewService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        ConfigurableApplicationContext app = SpringApplication.run(Application.class, args);

        NotifierKeepAliveUtilityService notifierKeepAliveUtilityService = app.getBean(NotifierKeepAliveUtilityService.class);
        notifierKeepAliveUtilityService.start();

        EmailNotificationQueueService emailNotificationQueueService = app.getBean(EmailNotificationQueueService.class);
        emailNotificationQueueService.start();

        QueuedPageViewService queuedPageViewService = app.getBean(QueuedPageViewService.class);
        queuedPageViewService.start();
    }
}
