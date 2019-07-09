package io.github.shamrice.discapp.service;

import io.github.shamrice.discapp.data.model.Thread;
import io.github.shamrice.discapp.data.model.ThreadBody;
import io.github.shamrice.discapp.data.repository.ThreadBodyRepository;
import io.github.shamrice.discapp.data.repository.ThreadRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ThreadService {

    private static Logger logger = LoggerFactory.getLogger(ThreadService.class);

    @Autowired
    private ThreadRepository threadRepository;

    @Autowired
    private ThreadBodyRepository threadBodyRepository;

    public List<Thread> getThreads(Long applicationId) {
        return threadRepository.findByApplicationId(applicationId);
    }

    public List<ThreadTreeNode> getLatestThreads(Long applicationId, int numThreads) {

        //query to get latest parent threads (parentId = 0L) for an application
        Pageable limit = PageRequest.of(0, numThreads);
        List<Thread> parentThreads = threadRepository.findByApplicationIdAndParentIdOrderByCreateDtDesc(
                applicationId,
                0L,
                limit
        );

        //create full thread node list based on parent threads.
        List<ThreadTreeNode> threadList = new ArrayList<>();
        for (Thread parentThread : parentThreads) {
            threadList.add(getFullThreadTree(parentThread.getId()));
        }

        return threadList;
    }

    public ThreadBody getThreadBody(Long threadId) {
        return threadBodyRepository.findByThreadId(threadId);
    }

    public ThreadTreeNode getFullThreadTree(Long topLevelThreadId) {

        logger.info("Building full thread tree for top level thread id: " + topLevelThreadId);

        ThreadTreeNode topThreadNode = null;

        if (threadRepository.findById(topLevelThreadId).isPresent()) {
            Thread topLevelThread = threadRepository.findById(topLevelThreadId).get();
            topThreadNode = new ThreadTreeNode(topLevelThread);


            List<Thread> nextThreads = threadRepository.findByApplicationIdAndParentId(
                    topLevelThread.getApplicationId(),
                    topLevelThread.getId()
            );

            buildThreadTree(topThreadNode, nextThreads);

            logger.info("Found top level thread id of: " + topLevelThread.getId() + " : subject: " + topLevelThread.getSubject());
        }

        return topThreadNode;

    }

    private void buildThreadTree(ThreadTreeNode currentNode, List<Thread> nextSubThreads) {

        logger.info("buildThreads : start");

        for (Thread thread : nextSubThreads) {
            currentNode.addSubThread(new ThreadTreeNode(thread));
        }

        for (ThreadTreeNode subThread :  currentNode.getSubThreads()) {
            List<Thread> nextThreads = threadRepository.findByApplicationIdAndParentId(
                    subThread.getCurrent().getApplicationId(),
                    subThread.getCurrent().getId()
            );

            buildThreadTree(subThread, nextThreads);
        }

        logger.info("buildThreads : end");
    }

}
