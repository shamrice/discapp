package io.github.shamrice.discapp.service.thread;

import io.github.shamrice.discapp.data.model.UserReadThread;
import io.github.shamrice.discapp.data.repository.UserReadThreadRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@Slf4j
public class UserReadThreadService {

    @Autowired
    private UserReadThreadRepository userReadThreadRepository;

    public void markThreadAsRead(long applicationId, long discappUserId, long threadId) {

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

        if (csvContainsThreadId(userReadThreads, threadIdStr)) {
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

    public String getReadThreadsCsv(long applicationId, long discappUserId) {
        UserReadThread readThreads = userReadThreadRepository.findOneByApplicationIdAndDiscappUserId(applicationId, discappUserId);
        if (readThreads != null && readThreads.getReadThreads() != null) {
            return readThreads.getReadThreads();
        }
        return "";
    }

    public boolean isThreadRead(long applicationId, long discappUserId, long threadId) {
        UserReadThread userReadThread = userReadThreadRepository.findOneByApplicationIdAndDiscappUserId(applicationId, discappUserId);

        if (userReadThread == null) {
            log.info("User record not found to check read status. user: " + discappUserId + " appId: " + applicationId
                    + " threadId: " + threadId);
            return false;
        }

        return csvContainsThreadId(userReadThread.getReadThreads(), String.valueOf(threadId));
    }

    public boolean csvContainsThreadId(String readThreadsCsv, Long threadId) {
        if (threadId == null) {
            return false;
        }
        return csvContainsThreadId(readThreadsCsv, String.valueOf(threadId));
    }

    public boolean csvContainsThreadId(String readThreadsCsv, String threadId) {

        if (threadId == null || threadId.isEmpty() || readThreadsCsv == null || readThreadsCsv.isEmpty()) {
            return false;
        }

        String[] readThreads = readThreadsCsv.split(",");
        for (String readThread : readThreads) {
            if (threadId.equals(readThread)) {
                log.debug("Thread id: " + threadId + " found in read threads list provided.");
                return true;
            }
        }

        return false;
    }
}
