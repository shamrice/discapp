package io.github.shamrice.discapp.service;

import io.github.shamrice.discapp.data.model.Thread;
import io.github.shamrice.discapp.data.model.ThreadBody;
import io.github.shamrice.discapp.data.repository.ThreadBodyRepository;
import io.github.shamrice.discapp.data.repository.ThreadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ThreadService {

    @Autowired
    private ThreadRepository threadRepository;

    @Autowired
    private ThreadBodyRepository threadBodyRepository;

    //TODO : get threads with a limit
    public List<Thread> getThreads(Long applicationId) {
        return threadRepository.findByApplicationId(applicationId);
    }

    public ThreadBody getThreadBody(Long threadId) {
        return threadBodyRepository.findByThreadId(threadId);
    }
}
