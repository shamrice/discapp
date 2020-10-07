package io.github.shamrice.discapp.service.thread;

import io.github.shamrice.discapp.data.model.Thread;
import io.github.shamrice.discapp.data.model.ThreadActivity;
import io.github.shamrice.discapp.data.repository.ThreadActivityRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class ThreadActivityService {

    @Autowired
    private ThreadActivityRepository threadActivityRepository;

    public void updateThreadActivity(Thread rootThread) {
        if (rootThread != null && rootThread.getApplicationId() != null) {
            ThreadActivity threadActivity = threadActivityRepository.findByThreadId(rootThread.getId()).orElse(null);
            if (threadActivity == null) {
                log.info("Creating new thread activity entry for appId: " + rootThread.getApplicationId()
                        + " :: root thread id: " + rootThread.getId());
                threadActivity = new ThreadActivity();
                threadActivity.setApplicationId(rootThread.getApplicationId());
                threadActivity.setThread(rootThread);
                threadActivity.setCreateDt(new Date());
            }
            threadActivity.setModDt(new Date());
            threadActivityRepository.save(threadActivity);
            log.info("Updated thread activity: " + threadActivity.toString());
        }
    }

    public List<ThreadActivity> getLatestThreadActivity(Long applicationId, Pageable pageable) {
        return threadActivityRepository.findByApplicationIdAndThreadDeletedAndThreadIsApprovedOrderByModDtDesc(
                applicationId,
                false,
                true,
                pageable
        );
    }
}
