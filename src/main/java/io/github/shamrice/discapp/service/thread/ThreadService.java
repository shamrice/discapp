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

    public long getTotalThreadCountForApplicationId(long applicationId) {
        return threadRepository.countByApplicationIdAndDeleted(applicationId, false);
    }

    public boolean deleteThread(long applicationId, long threadId, boolean deleteSubThreads) {

        Thread threadToDelete = threadRepository.getOne(threadId);

        if (!threadToDelete.getApplicationId().equals(applicationId)) {
            logger.warn("Attempted to delete thread id: " + threadId + " which belongs to a different application id than " + applicationId);
            return false;
        }

        List<Thread> subThreads = threadRepository.findByApplicationIdAndParentIdAndDeleted(applicationId, threadToDelete.getId(), false);

        for (Thread subThread : subThreads) {
            if (deleteSubThreads) {

                logger.info("Thread to deleted: " + threadToDelete.getId() + " : Deleting all sub threads recursively.");
                deleteThread(applicationId, subThread.getId(), true);
            } else {

                logger.info("Thread to deleted: " + threadToDelete.getId()
                        + " : Setting parent id of sub threads to parent id= " + threadToDelete.getParentId()
                        + " of thread to delete.");

                subThread.setParentId(threadToDelete.getParentId());
                subThread.setModDt(new Date());
            }
        }

        List<Thread> savedSubThreads = threadRepository.saveAll(subThreads);

        if (savedSubThreads != null && savedSubThreads.size() == subThreads.size()) {
            threadToDelete.setModDt(new Date());
            threadToDelete.setDeleted(true);

            Thread savedThread = threadRepository.save(threadToDelete);

            if (savedThread != null && savedThread.getId().equals(threadToDelete.getId())) {

                logger.info("Successfully deleted thread id: " + threadToDelete.getId() + " for applicationId: " + applicationId);
                return true;

            } else {
                logger.error("Failed to delete thread: " + threadToDelete + " for appid: " + applicationId
                        + " :: sub threads have been updated. Please retry this action.");
            }

        } else {
            logger.error("Failed to update all sub threads of thread to delete: " + threadToDelete.getId()
                    + " :: not deleting thread. Check for errors and/or orphaned sub threads");
        }

        return false;

    }

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
        return threadRepository.findByApplicationIdAndDeleted(applicationId, false);
    }

    public List<Thread> searchThreads(Long applicationId, String searchText) {
        List<Thread> foundThreads = threadRepository.findByApplicationIdAndDeletedAndSubjectContainingIgnoreCaseOrderByCreateDtDesc(applicationId, false, searchText);
        List<ThreadBody> resultsInBody = threadBodyRepository.findByApplicationIdAndBodyContainingIgnoreCaseOrderByCreateDtDesc(applicationId, searchText);

        for (ThreadBody threadBody : resultsInBody) {
            Optional<Thread> foundThread = threadRepository.findById(threadBody.getThreadId());
            if (foundThread.isPresent() && foundThread.get().getDeleted().equals(false)) {
                foundThread.ifPresent(foundThreads::add);
            }
        }

        return foundThreads;
    }

    //TODO : accept page number.
    public List<ThreadTreeNode> getLatestThreads(Long applicationId, int numThreads) {

        //query to get latest parent threads (parentId = 0L) for an application
        Pageable limit = PageRequest.of(0, numThreads);
        List<Thread> parentThreads = threadRepository.findByApplicationIdAndParentIdAndDeletedOrderByCreateDtDesc(
                applicationId,
                TOP_LEVEL_THREAD_PARENT_ID,
                false,
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
        Thread foundThread = threadRepository.getOne(threadId);
        return foundThread.getDeleted() ? null : foundThread;
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
        Optional<Thread> topLevelThread = threadRepository.findById(topLevelThreadId);

        if (topLevelThread.isPresent() && topLevelThread.get().getDeleted().equals(false)) {
            topThreadNode = new ThreadTreeNode(topLevelThread.get());

            List<Thread> nextThreads = threadRepository.findByApplicationIdAndParentIdAndDeleted(
                    topLevelThread.get().getApplicationId(),
                    topLevelThread.get().getId(),
                    false
            );

            buildThreadTree(topThreadNode, nextThreads);

            logger.info("Found top level thread id of: " + topLevelThread.get().getId() + " : subject: "
                    + topLevelThread.get().getSubject());
        }

        return topThreadNode;

    }

    private void buildThreadTree(ThreadTreeNode currentNode, List<Thread> nextSubThreads) {

        logger.debug("buildThreads : start: " + currentNode.getCurrent().getId() + " :: " + currentNode.getCurrent().getSubject());

        for (Thread thread : nextSubThreads) {
            currentNode.addSubThread(new ThreadTreeNode(thread));
        }

        for (ThreadTreeNode subThread :  currentNode.getSubThreads()) {
            List<Thread> nextThreads = threadRepository.findByApplicationIdAndParentIdAndDeleted(
                    subThread.getCurrent().getApplicationId(),
                    subThread.getCurrent().getId(),
                    false
            );

            buildThreadTree(subThread, nextThreads);
        }

        logger.debug("buildThreads : end" + currentNode.getCurrent().getId() + " :: " + currentNode.getCurrent().getSubject());
    }

}
