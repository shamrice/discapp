package io.github.shamrice.discapp.service.utility.email;

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
public class EmailNotificationQueueService {

    private static LinkedBlockingQueue<ReplyNotification> replyNotificationQueue = new LinkedBlockingQueue<>();

    private static LinkedBlockingQueue<TemplateEmail> templateEmailQueue = new LinkedBlockingQueue<>();

    @Autowired
    private EmailNotificationService emailNotificationService;

    public void start() {
        Thread replyNotificationThread = new Thread(this::runReplySender);
        replyNotificationThread.setName("ReplySender");
        replyNotificationThread.start();

        Thread templateEmailSendingThread = new Thread(this::runTemplateEmailSender);
        templateEmailSendingThread.setName("TemplateEmailSender");
        templateEmailSendingThread.start();
    }

    public static void addTemplateEmailToSend(TemplateEmail templateEmail) {
        if (templateEmail == null) {
            log.error("Cannot add null template email to queue.");
            return;
        }
        log.info("Adding template email to queue to be sent: " + templateEmail.toString());
        if (!templateEmailQueue.offer(templateEmail)) {
            log.error("Failed to add template email to queue. Queue is full. Email: " + templateEmail.toString());
        }
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

    private void runTemplateEmailSender() {
        log.info("Starting template email sending service.");
        while (true) {
            try {

                TemplateEmail templateEmail = templateEmailQueue.take();

                if (templateEmail.isMimeMessage()) {
                    emailNotificationService.sendMimeMessage(
                            templateEmail.getTo(),
                            templateEmail.getNotificationType(),
                            templateEmail.getSubjectTemplateParams(),
                            templateEmail.getBodyTemplateParams()
                    );
                } else {
                    emailNotificationService.send(
                            templateEmail.getTo(),
                            templateEmail.getNotificationType(),
                            templateEmail.getBodyTemplateParams()
                    );
                }
                log.info("Sent email: " + templateEmail.toString());

            } catch (Exception ex) {
                log.error("Error sending template email: " + ex.getMessage(), ex);
            }
        }
    }

    private void runReplySender() {
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
                log.error("Error reading reply notification queue " + EmailNotificationQueueService.class.getSimpleName()
                        + " :: " + ex.getMessage(), ex);
            }
        }
    }
}