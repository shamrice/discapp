package io.github.shamrice.discapp;

import io.github.shamrice.discapp.service.utility.NotifierKeepAliveUtilityService;
import io.github.shamrice.discapp.service.utility.ReplyNotificationUtilityService;
import lombok.var;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        var app = SpringApplication.run(Application.class, args);

        var notifierKeepAliveUtilityService = app.getBean(NotifierKeepAliveUtilityService.class);
        notifierKeepAliveUtilityService.start();

        var replyNotificationService = app.getBean(ReplyNotificationUtilityService.class);
        replyNotificationService.start();
    }
}
