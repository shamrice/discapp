package io.github.shamrice.discapp.service.thread;

import io.github.shamrice.discapp.data.model.ApplicationPermission;
import io.github.shamrice.discapp.data.model.ReportedAbuse;
import io.github.shamrice.discapp.data.model.Thread;
import io.github.shamrice.discapp.data.model.ThreadBody;
import io.github.shamrice.discapp.data.repository.ReportedAbuseRepository;
import io.github.shamrice.discapp.data.repository.ThreadBodyRepository;
import io.github.shamrice.discapp.data.repository.ThreadRepository;
import io.github.shamrice.discapp.service.application.ApplicationService;
import io.github.shamrice.discapp.service.configuration.ConfigurationProperty;
import io.github.shamrice.discapp.service.configuration.ConfigurationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
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

    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    private ApplicationService applicationService;

    public List<Thread> getAllDeletedThreads() {
        return threadRepository.findByDeletedOrderByCreateDtDesc(true);
    }

    public boolean setThreadAsTopLevelArticle(long appId, long threadId) {
        Thread thread = getThread(appId, threadId);
        if (thread != null) {
            thread.setParentId(0L);
            thread.setModDt(new Date());
            if (threadRepository.save(thread) != null) {
                log.info("Successfully set threadId: " +threadId + " to a top level thread for appId: " + appId);
                return true;
            }
        }
        log.error("Failed to set threadId: " + threadId + " to top level thread. Either thread was not found or error while saving.");
        return false;
    }

    public List<Thread> getUnapprovedThreads(long appId) {
        return threadRepository.findByApplicationIdAndDeletedAndIsApprovedOrderByCreateDtDesc(appId, false, false);
    }

    public boolean isNewThreadPostTooSoon(long applicationId, String ipAddress) {
        if (ipAddress != null && !ipAddress.trim().isEmpty()) {
            Thread lastThreadSubmittedByIp = threadRepository.findTopByApplicationIdAndIpAddressOrderByCreateDtDesc(applicationId, ipAddress);
            if (lastThreadSubmittedByIp != null) {
                int minPostInterval = configurationService.getIntegerValue(ConfigurationService.SITE_WIDE_CONFIGURATION_APP_ID, ConfigurationProperty.MIN_THREAD_POST_INTERVAL_IN_SECONDS, 60);
                long currentInterval = (new Date().getTime() - lastThreadSubmittedByIp.getCreateDt().getTime()) / 1000;
                if (currentInterval < minPostInterval) {
                    log.warn("Attempt to create thread too soon for IpAddress: " + ipAddress + " on appId: " + applicationId + " :: minInterval="
                            + minPostInterval + " :: currentInterval: " + currentInterval);
                    return true;
                }
            }
        }
        return false;
    }

    public boolean deleteReportedAbuse(long reportedAbuseId) {
        log.info("Deleting reported abuse record Id: " + reportedAbuseId);
        ReportedAbuse abuseToDelete = reportedAbuseRepository.findById(reportedAbuseId).orElse(null);
        if (abuseToDelete != null) {

            abuseToDelete.setIsDeleted(true);
            abuseToDelete.setModDt(new Date());
            abuseToDelete.getThread().setModDt(new Date());
            abuseToDelete.getThread().setDeleted(false);
            reportedAbuseRepository.save(abuseToDelete);
            log.info("Maked abuseId: " + reportedAbuseId + " as deleted. :: " + abuseToDelete.toString());
            return true;
        }
        log.warn("Could not find reported abuse to mark as deleted. Id attempted: " + reportedAbuseId);
        return false;
    }

    public ReportedAbuse getReportedAbuse(long reportedAbuseId) {
        return reportedAbuseRepository.findById(reportedAbuseId).orElse(null);
    }

    public List<ReportedAbuse> searchForReportedAbuse(Long appId, String submitter, String email, String ipAddress, String subject, String body) {

        boolean searchByAppId = appId != null;
        boolean searchBySubmitter = submitter != null && !submitter.trim().isEmpty();
        boolean searchByEmail = email != null && !email.trim().isEmpty();
        boolean searchByIpAddress = ipAddress != null && !ipAddress.trim().isEmpty();
        boolean searchBySubject = subject != null && !subject.trim().isEmpty();
        boolean searchByBody = body != null && !body.trim().isEmpty();

        List<ReportedAbuse> results = new ArrayList<>(reportedAbuseRepository.findAll());
        List<ReportedAbuse> finalList = new ArrayList<>();
        for (ReportedAbuse result : results) {
            if (!result.getIsDeleted()) {
                boolean validResult = true;
                if (searchByAppId && !result.getApplicationId().equals(appId)) validResult = false;
                if (searchByIpAddress && result.getIpAddress() != null && !result.getIpAddress().contains(ipAddress))
                    validResult = false;

                if (result.getThread() != null) {
                    if (searchBySubmitter && !result.getThread().getSubmitter().toLowerCase().contains(submitter.toLowerCase()))
                        validResult = false;
                    if (searchByEmail && !result.getThread().getEmail().toLowerCase().contains(email.toLowerCase()))
                        validResult = false;
                    if (searchBySubject && !result.getThread().getSubject().toLowerCase().contains(subject.toLowerCase()))
                        validResult = false;

                    if (result.getThread().getBody() != null) {
                        if (searchByBody && !result.getThread().getBody().toLowerCase().contains(body.toLowerCase()))
                            validResult = false;
                    }
                }

                if (validResult) {
                    finalList.add(result);
                }
            }
        }

        return finalList;
    }

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

        Thread reportedThread = threadRepository.findById(threadId).orElse(null);

        if (reportedThread != null) {
            ReportedAbuse newAbuseReport = new ReportedAbuse();
            newAbuseReport.setApplicationId(applicationId);
            newAbuseReport.setIpAddress(abuseThread.get().getIpAddress());
            newAbuseReport.setThread(reportedThread);
            newAbuseReport.setReportedBy(reporterDiscAppUserId);
            newAbuseReport.setModDt(new Date());
            newAbuseReport.setCreateDt(new Date());
            newAbuseReport.setIsDeleted(false);

            ReportedAbuse savedReport = reportedAbuseRepository.save(newAbuseReport);
            if (savedReport != null) {
                log.info("Saved new abuse report for application_id: " + applicationId
                        + " : thread_id: " + threadId + " :: Marking thread for deletion.");

                return deleteThread(applicationId, abuseThread.get().getId(), false);

            } else {
                log.error("Failed to report abuse of thread_id: " + threadId + " for application_id: " + applicationId);
            }
        }

        return false;
    }

    public long getTotalThreadCountForApplicationId(long applicationId) {
        //total count includes non-approved threads.
        return threadRepository.countByApplicationIdAndDeleted(applicationId, false);
    }

    public void deleteAllThreadsInApplication(long applicationId) {
        log.warn("Marking all threads for application Id: " + applicationId + " as deleted.");
        List<Thread> threadsToMarkDeleted = threadRepository.findByApplicationIdAndDeleted(applicationId, false);
        for (Thread thread : threadsToMarkDeleted) {
            thread.setDeleted(true);
            thread.setModDt(new Date());
        }
        List<Thread> markedThreads = threadRepository.saveAll(threadsToMarkDeleted);
        if (markedThreads != null ) {
            log.warn("Successfully marked " + markedThreads.size() + " threads as deleted for application id: " + applicationId);
            return;
        }

        log.warn("No threads to mark as deleted for application id: " + applicationId);
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

            //check bad words filter and act as needed.
            ApplicationPermission applicationPermission = applicationService.getApplicationPermissions(thread.getApplicationId());
            if (applicationPermission != null && applicationPermission.getBlockBadWords()) {
                List<String> badWordsList = configurationService.getStringListValue(ConfigurationService.SITE_WIDE_CONFIGURATION_APP_ID, ConfigurationProperty.BAD_WORDS_LIST, new ArrayList<>());
                for (String badWord : badWordsList) {
                    thread.setSubject(thread.getSubject().replaceAll("(?i)" + badWord, " ... "));
                    thread.setSubmitter(thread.getSubmitter().replaceAll("(?i)" + badWord, " ... "));
                    if (thread.getEmail().contains(badWord)) {
                        thread.setShowEmail(false);
                        thread.setEmail(thread.getEmail().replaceAll("(?i)" + badWord, " ... "));
                    }
                    if (threadBodyText != null && !threadBodyText.trim().isEmpty()) {
                        threadBodyText = threadBodyText.replaceAll("(?i)" + badWord, " ... ");
                    }
                }
            }

            //add (nm) to subjects with no body.
            if (!thread.getSubject().contains(NO_MESSAGE_SUBJECT_ANNOTATION) && (threadBodyText == null || threadBodyText.isEmpty())) {
                String noBodySubject = thread.getSubject() + NO_MESSAGE_SUBJECT_ANNOTATION;
                thread.setSubject(noBodySubject);
            }

            Thread createThread = threadRepository.save(thread);
            ThreadBody threadBody = threadBodyRepository.findByThreadId(createThread.getId());

            //new thread body that's not blank
            if (threadBody == null && threadBodyText != null && !threadBodyText.trim().isEmpty()) {

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
        return threadRepository.findByApplicationIdAndDeletedAndIsApproved(applicationId, false, true);
    }

    public List<Thread> searchThreads(Long applicationId, String searchText) {
        List<Thread> foundThreads = threadRepository.findByApplicationIdAndDeletedAndIsApprovedAndSubjectContainingIgnoreCaseOrderByCreateDtDesc(applicationId, false, true, searchText);
        List<ThreadBody> resultsInBody = threadBodyRepository.findByApplicationIdAndBodyContainingIgnoreCaseOrderByCreateDtDesc(applicationId, searchText);

        for (ThreadBody threadBody : resultsInBody) {
            Optional<Thread> foundThread = threadRepository.findById(threadBody.getThreadId());
            if (foundThread.isPresent() && foundThread.get().getDeleted().equals(false) && foundThread.get().isApproved()) {
                foundThread.ifPresent(foundThreads::add);
            }
        }

        return foundThreads;
    }

    public List<Thread> searchThreadsByFields(long applicationId, String submitter, String email, String subject, String ipAddress, String messageBody, boolean isApproved) {

        //todo : refactor this so the query is on an "and" instead of querying each and then trimming

        List<Thread> foundThreads = new ArrayList<>();

        //search
        if (subject != null && !subject.trim().isEmpty()) {
            foundThreads.addAll(threadRepository.findByApplicationIdAndDeletedAndIsApprovedAndSubjectContainingIgnoreCaseOrderByCreateDtDesc(applicationId, false, isApproved, subject));
        }

        if (messageBody != null && !messageBody.trim().isEmpty()) {

            List<ThreadBody> resultsInBody = threadBodyRepository.findByApplicationIdAndBodyContainingIgnoreCaseOrderByCreateDtDesc(applicationId, messageBody);

            for (ThreadBody threadBody : resultsInBody) {
                Optional<Thread> foundThread = threadRepository.findById(threadBody.getThreadId());
                if (foundThread.isPresent() && foundThread.get().getDeleted().equals(false) && (foundThread.get().isApproved() == isApproved)) {
                    foundThread.get().setBody(threadBody.getBody());
                    foundThread.ifPresent(foundThreads::add);
                }
            }
        }

        if (submitter != null && !submitter.trim().isEmpty()) {
            foundThreads.addAll(threadRepository.findByApplicationIdAndDeletedAndIsApprovedAndSubmitterContainingIgnoreCase(applicationId, false, isApproved, submitter));
        }

        if (email != null && !email.trim().isEmpty()) {
            foundThreads.addAll(threadRepository.findByApplicationIdAndDeletedAndIsApprovedAndEmailContainingIgnoreCase(applicationId, false, isApproved, email));
        }

        if (ipAddress != null && !ipAddress.trim().isEmpty()) {
            foundThreads.addAll(threadRepository.findByApplicationIdAndDeletedAndIsApprovedAndIpAddressContainingIgnoreCase(applicationId, false, isApproved, ipAddress));
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
        List<Thread> parentThreads = threadRepository.findByApplicationIdAndParentIdAndDeletedAndIsApprovedOrderByCreateDtDesc(
                applicationId,
                TOP_LEVEL_THREAD_PARENT_ID,
                false,
                true,
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
        return threadRepository.findByApplicationIdAndDeletedAndIsApprovedOrderByCreateDtDesc(
                applicationId,
                false,
                true,
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
        return threadRepository.findByApplicationIdAndParentIdAndDeletedAndIsApprovedOrderByCreateDtDesc(
                applicationId,
                TOP_LEVEL_THREAD_PARENT_ID,
                false,
                true,
                limit
        );
    }

    public Thread getThreadById(long threadId) {
        //includes threads marked as deleted.
        return threadRepository.findById(threadId).orElse(null);
    }

    public Thread getThread(long applicationId, long threadId) {
        Thread foundThread = threadRepository.getOneByApplicationIdAndId(applicationId, threadId);
        return foundThread != null && foundThread.getDeleted() && foundThread.isApproved() ? null : foundThread;
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

            List<Thread> nextThreads = threadRepository.findByApplicationIdAndParentIdAndDeletedAndIsApproved(
                    topLevelThread.get().getApplicationId(),
                    topLevelThread.get().getId(),
                    false,
                    true
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
            List<Thread> nextThreads = threadRepository.findByApplicationIdAndParentIdAndDeletedAndIsApproved(
                    subThread.getCurrent().getApplicationId(),
                    subThread.getCurrent().getId(),
                    false,
                    true
            );

            buildThreadTree(subThread, nextThreads);
        }

        log.debug("buildThreads : end" + currentNode.getCurrent().getId() + " :: " + currentNode.getCurrent().getSubject());
    }

}
