package io.github.shamrice.discapp.service.thread;

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

import java.util.*;

@Service
public class ThreadService {

    private static final Logger logger = LoggerFactory.getLogger(ThreadService.class);

    private static final String NO_MESSAGE_SUBJECT_ANNOTATION = " (nm)";
    private static final long TOP_LEVEL_THREAD_PARENT_ID = 0L;

    @Autowired
    private ThreadRepository threadRepository;

    @Autowired
    private ThreadBodyRepository threadBodyRepository;

    public void createNewThread(Thread newThread, String threadBodyText) {

        //add (nm) to subjects with no body.
        if (threadBodyText == null || threadBodyText.isEmpty()) {
            String noBodySubject = newThread.getSubject() + NO_MESSAGE_SUBJECT_ANNOTATION;
            newThread.setSubject(noBodySubject);
        }

        if (newThread != null) {
            Thread createThread = threadRepository.save(newThread);
            if (threadBodyText != null && !threadBodyText.isEmpty() && createThread != null) {
                ThreadBody newThreadBody = new ThreadBody();
                newThreadBody.setApplicationId(createThread.getApplicationId());
                newThreadBody.setBody(threadBodyText);
                newThreadBody.setThreadId(createThread.getId());
                newThreadBody.setCreateDt(new Date());
                newThreadBody.setModDt(new Date());

                threadBodyRepository.save(newThreadBody);
            }
        }
    }

    public List<Thread> getThreads(Long applicationId) {
        return threadRepository.findByApplicationId(applicationId);
    }

    public List<Thread> searchThreads(Long applicationId, String searchText) {
        List<Thread> foundThreads = threadRepository.findByApplicationIdAndSubjectContainingIgnoreCaseOrderByCreateDtDesc(applicationId, searchText);
           /* if (foundThread.isPresent()) {
                foundThreads.add(foundThread.get());
            }*/
        List<ThreadBody> resultsInBody = threadBodyRepository.findByApplicationIdAndBodyContainingIgnoreCaseOrderByCreateDtDesc(applicationId, searchText);

        for (ThreadBody threadBody : resultsInBody) {
            Optional<Thread> foundThread = threadRepository.findById(threadBody.getThreadId());
            foundThread.ifPresent(foundThreads::add);
        }

        return foundThreads;
    }

    //TODO : accept page number.
    public List<ThreadTreeNode> getLatestThreads(Long applicationId, int numThreads) {

        //query to get latest parent threads (parentId = 0L) for an application
        Pageable limit = PageRequest.of(0, numThreads);
        List<Thread> parentThreads = threadRepository.findByApplicationIdAndParentIdOrderByCreateDtDesc(
                applicationId,
                TOP_LEVEL_THREAD_PARENT_ID,
                limit
        );

        //create full thread node list based on parent threads.
        List<ThreadTreeNode> threadList = new ArrayList<>();
        for (Thread parentThread : parentThreads) {
            threadList.add(getFullThreadTree(parentThread.getId()));
        }

        return threadList;
    }

    public Thread getThread(Long threadId) {
        return threadRepository.getOne(threadId);
    }

    public ThreadBody getThreadBody(Long threadId) {
        return threadBodyRepository.findByThreadId(threadId);
    }

    public String getThreadBodyText(Long threadId) {
        ThreadBody threadBody = threadBodyRepository.findByThreadId(threadId);
        if (threadBody != null) {
            return threadBody.getBody();
        }

        logger.info("Unable to find thread body for thread id " + threadId + ". returning empty string.");
        return "";
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

            logger.info("Found top level thread id of: " + topLevelThread.getId() + " : subject: "
                    + topLevelThread.getSubject());
        }

        return topThreadNode;

    }

    private void buildThreadTree(ThreadTreeNode currentNode, List<Thread> nextSubThreads) {

        logger.debug("buildThreads : start: " + currentNode.getCurrent().getId() + " :: " + currentNode.getCurrent().getSubject());

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

        logger.debug("buildThreads : end" + currentNode.getCurrent().getId() + " :: " + currentNode.getCurrent().getSubject());
    }

}
