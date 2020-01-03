package io.github.shamrice.discapp.service.utility;

import io.github.shamrice.discapp.service.account.notification.EmailNotificationService;
import io.github.shamrice.discapp.service.account.notification.NotificationType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

@Service
@Slf4j
public class ReplyNotificationUtilityService {


    private static LinkedBlockingQueue<ReplyNotification> replyNotificationQueue = new LinkedBlockingQueue<>();

    @Autowired
    private EmailNotificationService emailNotificationService;

    public void start() {
        Thread replyNotificationThread = new Thread(this::run);
        replyNotificationThread.setName(ReplyNotificationUtilityService.class.getSimpleName());
        replyNotificationThread.start();
    }

    public static void addReplyToSend(ReplyNotification replyNotification) {

        if (replyNotification == null) {
            log.error("Cannot add null reply notification to email reply queue.");
            return;
        }

        log.info("Adding reply email notification to queue to be sent: " + replyNotification.toString());
        if (!replyNotificationQueue.offer(replyNotification)) {
            log.error("Failed to add reply to notification queue. Queue is full. Reply: " + replyNotification.toString());
        }
    }

    private void run() {
        log.info("Starting reply notification service.");

        while (true) {

            try {
                //get next item in queue or wait until one becomes available.
                ReplyNotification replyNotification = replyNotificationQueue.take();

                //send out email message
                Map<String, Object> subjectParams = new HashMap<>();
                subjectParams.put("APPLICATION_NAME", replyNotification.getAppName());

                Map<String, Object> bodyParams = new HashMap<>();
                bodyParams.put("APPLICATION_NAME", replyNotification.getAppName());
                bodyParams.put("APP_ID", replyNotification.getAppId());
                bodyParams.put("APP_DISCUSSION_URL", replyNotification.getDiscussionUrl());
                bodyParams.put("THREAD_ID", replyNotification.getNewThreadId());

                emailNotificationService.sendMimeMessage(replyNotification.getEmailAddress(),
                        NotificationType.REPLY_NOTIFICATION, subjectParams, bodyParams);
                log.info("Sent reply notification for thread: " + replyNotification.getNewThreadId() + " to: "
                        + replyNotification.getEmailAddress());

            } catch (Exception ex) {
                log.error("Error reading reply notification queue " + ReplyNotificationUtilityService.class.getSimpleName()
                        + " :: " + ex.getMessage(), ex);
            }
        }
    }
}
