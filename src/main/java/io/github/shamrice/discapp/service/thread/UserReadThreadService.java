package io.github.shamrice.discapp.service.thread;

import io.github.shamrice.discapp.data.model.UserReadThread;
import io.github.shamrice.discapp.data.repository.UserReadThreadRepository;
import io.github.shamrice.discapp.service.configuration.ConfigurationService;
import io.github.shamrice.discapp.service.configuration.UserConfigurationProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class UserReadThreadService {

    @Autowired
    private UserReadThreadRepository userReadThreadRepository;

    @Autowired
    private ConfigurationService configurationService;

    public void resetReadThreads(long applicationId, long discappUserId) {
        UserReadThread readThread = userReadThreadRepository.findOneByApplicationIdAndDiscappUserId(applicationId, discappUserId);
        if (readThread != null) {
            readThread.setReadThreads("");
            readThread.setModDt(new Date());
            userReadThreadRepository.save(readThread);
            log.info("Reset read threads for userId: " + discappUserId + " on appId: " + applicationId + " to empty string.");
        }
    }

    public List<UserReadThread> getAllUserReadThreads(long discappUserId) {

        List<UserReadThread> results = new ArrayList<>();

        List<UserReadThread> readThreads = userReadThreadRepository.findAllByDiscappUserId(discappUserId);
        for (UserReadThread userReadThread : readThreads) {
            if (userReadThread.getReadThreads() != null && !userReadThread.getReadThreads().trim().isEmpty()) {
                results.add(userReadThread);
            }
        }

        return results;
    }

    public void markThreadAsRead(long applicationId, long discappUserId, long threadId) {

        //check to see if thread history is enabled before continuing.
        if (!configurationService.getUserConfigBooleanValue(discappUserId, UserConfigurationProperty.THREAD_READ_TRACKING_ENABLED, false)) {
            log.info("UserId: " + discappUserId + " has read thread history turned off. Not marking thread as read.");
            return;
        }

        String threadIdStr = String.valueOf(threadId);

        UserReadThread userReadThread = userReadThreadRepository.findOneByApplicationIdAndDiscappUserId(applicationId, discappUserId);
        if (userReadThread == null) {
            log.info("No existing user read thread record found for user: " + discappUserId + " on appId: "
                    + applicationId + " :: creating new entry.");
            userReadThread = new UserReadThread();
            userReadThread.setApplicationId(applicationId);
            userReadThread.setDiscappUserId(discappUserId);
            userReadThread.setCreateDt(new Date());
            userReadThread.setModDt(new Date());
            userReadThread.setReadThreads(threadIdStr);
            userReadThreadRepository.save(userReadThread);
            log.info("Created new read thread entry for userId: " + discappUserId + " and applicationId: " + applicationId);
            return;
        }

        if (userReadThread.getReadThreads() == null) {
            userReadThread.setReadThreads("");
        }


        String userReadThreads = userReadThread.getReadThreads();

        if (csvContainsThreadId(userReadThreads.split(","), threadIdStr)) {
            log.info("Thread: " + threadIdStr + " for user: " + discappUserId + " and application: "
                    + applicationId + " already marked as read. Nothing to do.");
        } else {
            log.info("Marking thread: " + threadIdStr + " as read for user: " + discappUserId
                    + " on appId: " + applicationId);
            userReadThreads += "," + threadIdStr;
            userReadThread.setReadThreads(userReadThreads);
            userReadThread.setModDt(new Date());
            userReadThreadRepository.save(userReadThread);
        }
    }

    public String[] getReadThreadsCsv(long applicationId, long discappUserId) {
        UserReadThread readThreads = userReadThreadRepository.findOneByApplicationIdAndDiscappUserId(applicationId, discappUserId);
        if (readThreads != null && readThreads.getReadThreads() != null) {
            return readThreads.getReadThreads().split(",");
        }
        return null;
    }

    public boolean isThreadRead(long applicationId, long discappUserId, long threadId) {
        UserReadThread userReadThread = userReadThreadRepository.findOneByApplicationIdAndDiscappUserId(applicationId, discappUserId);

        if (userReadThread == null) {
            log.info("User record not found to check read status. user: " + discappUserId + " appId: " + applicationId
                    + " threadId: " + threadId);
            return false;
        }

        if (userReadThread.getReadThreads() != null) {
            return csvContainsThreadId(userReadThread.getReadThreads().split(","), String.valueOf(threadId));
        }
        return false;
    }

    public boolean csvContainsThreadId(String[] readThreadsCsv, Long threadId) {
        if (threadId == null) {
            return false;
        }
        return csvContainsThreadId(readThreadsCsv, String.valueOf(threadId));
    }

    public boolean csvContainsThreadId(String[] readThreadsCsv, String threadId) {

        if (threadId == null || threadId.isEmpty() || readThreadsCsv == null || readThreadsCsv.length == 0) {
            return false;
        }

        for (String readThread : readThreadsCsv) {
            if (threadId.equals(readThread)) {
                log.debug("Thread id: " + threadId + " found in read threads list provided.");
                return true;
            }
        }

        return false;
    }
}
