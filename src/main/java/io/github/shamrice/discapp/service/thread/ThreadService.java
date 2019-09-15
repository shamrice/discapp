package io.github.shamrice.discapp.service.thread;

import io.github.shamrice.discapp.data.model.ReportedAbuse;
import io.github.shamrice.discapp.data.model.Thread;
import io.github.shamrice.discapp.data.model.ThreadBody;
import io.github.shamrice.discapp.data.repository.ReportedAbuseRepository;
import io.github.shamrice.discapp.data.repository.ThreadBodyRepository;
import io.github.shamrice.discapp.data.repository.ThreadRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class ThreadService {

    private static final String NO_MESSAGE_SUBJECT_ANNOTATION = " (nm)";
    private static final long TOP_LEVEL_THREAD_PARENT_ID = 0L;

    @Autowired
    private ThreadRepository threadRepository;

    @Autowired
    private ThreadBodyRepository threadBodyRepository;

    @Autowired
    private ReportedAbuseRepository reportedAbuseRepository;

    public boolean reportThreadForAbuse(long applicationId, long threadId, long reporterDiscAppUserId) {
        Optional<Thread> abuseThread = threadRepository.findById(threadId);

        if (!abuseThread.isPresent()) {
            log.error("Unable to find threadId: " + threadId + " to report.");
            return false;
        }

        if (!abuseThread.get().getApplicationId().equals(applicationId)) {
            log.warn("Attempted to report thread id: " + threadId + " for abuse when application id is not from " + applicationId);
            return false;
        }

        ReportedAbuse newAbuseReport = new ReportedAbuse();
        newAbuseReport.setApplicationId(applicationId);
        newAbuseReport.setIpAddress(abuseThread.get().getIpAddress());
        newAbuseReport.setThreadId(abuseThread.get().getId());
        newAbuseReport.setReportedBy(reporterDiscAppUserId);
        newAbuseReport.setModDt(new Date());
        newAbuseReport.setCreateDt(new Date());

        ReportedAbuse savedReport = reportedAbuseRepository.save(newAbuseReport);
        if (savedReport != null) {
            log.info("Saved new abuse report for application_id: " + applicationId
                    + " : thread_id: " + threadId + " :: Marking thread for deletion.");

            return deleteThread(applicationId, abuseThread.get().getId(), false);

        } else {
            log.error("Failed to report abuse of thread_id: " + threadId + " for application_id: " + applicationId);
        }

        return false;
    }

    public long getTotalThreadCountForApplicationId(long applicationId) {
        return threadRepository.countByApplicationIdAndDeleted(applicationId, false);
    }

    public boolean deleteThread(long applicationId, long threadId, boolean deleteSubThreads) {

        Optional<Thread> optionalThreadToDelete = threadRepository.findById(threadId);

        if (!optionalThreadToDelete.isPresent()) {
            log.error("Unable to find threadId: " + threadId + " to delete.");
            return false;
        }

        Thread threadToDelete = optionalThreadToDelete.get();

        if (!threadToDelete.getApplicationId().equals(applicationId)) {
            log.warn("Attempted to delete thread id: " + threadId + " which belongs to a different application id than " + applicationId);
            return false;
        }

        List<Thread> subThreads = threadRepository.findByApplicationIdAndParentIdAndDeleted(applicationId, threadToDelete.getId(), false);

        for (Thread subThread : subThreads) {
            if (deleteSubThreads) {

                log.info("Thread to deleted: " + threadToDelete.getId() + " : Deleting all sub threads recursively.");
                deleteThread(applicationId, subThread.getId(), true);
            } else {

                log.info("Thread to deleted: " + threadToDelete.getId()
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

                log.info("Successfully deleted thread id: " + threadToDelete.getId() + " for applicationId: " + applicationId);
                return true;

            } else {
                log.error("Failed to delete thread: " + threadToDelete + " for appid: " + applicationId
                        + " :: sub threads have been updated. Please retry this action.");
            }

        } else {
            log.error("Failed to update all sub threads of thread to delete: " + threadToDelete.getId()
                    + " :: not deleting thread. Check for errors and/or orphaned sub threads");
        }

        return false;

    }

    public Long saveThread(Thread thread, String threadBodyText) {

        if (thread != null) {

            //add (nm) to subjects with no body.
            if (!thread.getSubject().contains(NO_MESSAGE_SUBJECT_ANNOTATION) && (threadBodyText == null || threadBodyText.isEmpty())) {
                String noBodySubject = thread.getSubject() + NO_MESSAGE_SUBJECT_ANNOTATION;
                thread.setSubject(noBodySubject);
            }

            Thread createThread = threadRepository.save(thread);
            ThreadBody threadBody = threadBodyRepository.findByThreadId(createThread.getId());

            //new thread body that's not blank
            if (threadBody == null && threadBodyText != null && !threadBodyText.isEmpty()) {

                threadBody = new ThreadBody();
                threadBody.setApplicationId(createThread.getApplicationId());
                threadBody.setBody(threadBodyText);
                threadBody.setThreadId(createThread.getId());
                threadBody.setCreateDt(new Date());
                threadBody.setModDt(new Date());
                threadBodyRepository.save(threadBody);

            } else if (threadBody != null) {
                //update existing thread body even to blank text
                threadBody.setBody(threadBodyText);
                threadBody.setModDt(new Date());
                threadBodyRepository.save(threadBody);
            }

            if (createThread != null) {
                log.info("Saved thread: " + createThread.getId() + " :: for appId: " + createThread.getApplicationId());
                return createThread.getId();
            }

        } else {
            log.error("Tried to create a null new thread.");
        }

        return null;
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

    public List<Thread> searchThreadsByFields(long applicationId, String submitter, String email, String subject, String ipAddress, String messageBody) {

        //todo : refactor this so the query is on an "and" instead of querying each and then trimming

        List<Thread> foundThreads = new ArrayList<>();

        //search
        if (subject != null && !subject.trim().isEmpty()) {
            foundThreads.addAll(threadRepository.findByApplicationIdAndDeletedAndSubjectContainingIgnoreCaseOrderByCreateDtDesc(applicationId, false, subject));
        }

        if (messageBody != null && !messageBody.trim().isEmpty()) {

            List<ThreadBody> resultsInBody = threadBodyRepository.findByApplicationIdAndBodyContainingIgnoreCaseOrderByCreateDtDesc(applicationId, messageBody);

            for (ThreadBody threadBody : resultsInBody) {
                Optional<Thread> foundThread = threadRepository.findById(threadBody.getThreadId());
                if (foundThread.isPresent() && foundThread.get().getDeleted().equals(false)) {
                    foundThread.get().setBody(threadBody.getBody());
                    foundThread.ifPresent(foundThreads::add);
                }
            }
        }

        if (submitter != null && !submitter.trim().isEmpty()) {
            foundThreads.addAll(threadRepository.findByApplicationIdAndDeletedAndSubmitterContainingIgnoreCase(applicationId, false, submitter));
        }

        if (email != null && !email.trim().isEmpty()) {
            foundThreads.addAll(threadRepository.findByApplicationIdAndDeletedAndEmailContainingIgnoreCase(applicationId, false, email));
        }

        if (ipAddress != null && !ipAddress.trim().isEmpty()) {
            foundThreads.addAll(threadRepository.findByApplicationIdAndDeletedAndIpAddressContainingIgnoreCase(applicationId, false, ipAddress));
        }

        if (subject == null) subject = "";
        if (messageBody == null) messageBody = "";
        if (submitter == null) submitter = "";
        if (email == null) email = "";
        if (ipAddress == null) ipAddress = "";

        //remove duplicates and gross way to make sure threads are searched as "and" instead of "or"...
        List<Thread> uniqueThreadList = new ArrayList<>();
        for (Thread thread : foundThreads) {
            if (!uniqueThreadList.contains(thread)) {

                if (thread.getSubmitter().toLowerCase().contains(submitter.toLowerCase())
                        && thread.getSubject().toLowerCase().contains(subject.toLowerCase())
                        && (thread.getEmail() == null || thread.getEmail().toLowerCase().contains(email.toLowerCase()))
                        && (thread.getIpAddress() == null ||  thread.getIpAddress().contains(ipAddress))
                        && (thread.getBody() == null || thread.getBody().toLowerCase().contains(messageBody.toLowerCase()))) {

                    uniqueThreadList.add(thread);
                }
            }
        }

        //sort threads
        uniqueThreadList.sort((t1, t2) -> {
            if (t1.getId().equals(t2.getId())) {
                return 0;
            }
            return t1.getId() > t2.getId() ? -1 : 1;
        });

        return uniqueThreadList;
    }

    public List<ThreadTreeNode> getLatestThreads(Long applicationId, int page, int numThreads, ThreadSortOrder threadSortOrder) {

        //query to get latest parent threads (parentId = 0L) for an application
        Pageable limit = PageRequest.of(page, numThreads);
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

        //sort by activity if needed.
        if (threadSortOrder.equals(ThreadSortOrder.ACTIVITY)) {
            threadList.sort((t1, t2) -> {
                if (t1.getNewestCreateDateInNodes().equals(t2.getNewestCreateDateInNodes())) {
                    return 0;
                }
                return t1.getNewestCreateDateInNodes().after(t2.getNewestCreateDateInNodes()) ? -1 : 1;
            });
        }

        return threadList;
    }

    /**
     * Only returns the latest posted threads. includes threads that were replies
     * @param applicationId
     * @param numThreads
     * @return
     */
    public List<Thread> getLatestThreads(long applicationId, int page, int numThreads) {
        //query to get latest parent threads (parentId = 0L) for an application
        Pageable limit = PageRequest.of(page, numThreads);
        return threadRepository.findByApplicationIdAndDeletedOrderByCreateDtDesc(
                applicationId,
                false,
                limit
        );
    }

    /**
     * Only returns the latest parent threads. Does not include sub threads.
     * @param applicationId
     * @param numThreads
     * @return
     */
    public List<Thread> getLatestTopLevelThreads(long applicationId, int page, int numThreads) {
        //query to get latest parent threads (parentId = 0L) for an application
        Pageable limit = PageRequest.of(page, numThreads);
        return threadRepository.findByApplicationIdAndParentIdAndDeletedOrderByCreateDtDesc(
                applicationId,
                TOP_LEVEL_THREAD_PARENT_ID,
                false,
                limit
        );
    }

    public Thread getThread(long applicationId, long threadId) {
        Thread foundThread = threadRepository.getOneByApplicationIdAndId(applicationId, threadId);
        return foundThread != null && foundThread.getDeleted() ? null : foundThread;
    }

    public ThreadBody getThreadBody(Long threadId) {
        return threadBodyRepository.findByThreadId(threadId);
    }

    public String getThreadBodyText(Long threadId) {
        ThreadBody threadBody = threadBodyRepository.findByThreadId(threadId);
        if (threadBody != null) {
            return threadBody.getBody();
        }

        log.info("Unable to find thread body for thread id " + threadId + ". returning empty string.");
        return "";
    }

    public ThreadTreeNode getFullThreadTree(Long topLevelThreadId) {

        log.debug("Building full thread tree for top level thread id: " + topLevelThreadId);

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

            log.debug("Found top level thread id of: " + topLevelThread.get().getId() + " : subject: "
                    + topLevelThread.get().getSubject());
        }

        return topThreadNode;

    }

    private void buildThreadTree(ThreadTreeNode currentNode, List<Thread> nextSubThreads) {

        log.debug("buildThreads : start: " + currentNode.getCurrent().getId() + " :: " + currentNode.getCurrent().getSubject());

        for (Thread thread : nextSubThreads) {
            currentNode.addSubThread(new ThreadTreeNode(thread));
        }

        for (ThreadTreeNode subThread : currentNode.getSubThreads()) {
            List<Thread> nextThreads = threadRepository.findByApplicationIdAndParentIdAndDeleted(
                    subThread.getCurrent().getApplicationId(),
                    subThread.getCurrent().getId(),
                    false
            );

            buildThreadTree(subThread, nextThreads);
        }

        log.debug("buildThreads : end" + currentNode.getCurrent().getId() + " :: " + currentNode.getCurrent().getSubject());
    }

}
