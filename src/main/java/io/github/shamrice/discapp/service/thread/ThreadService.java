package io.github.shamrice.discapp.service.thread;

import io.github.shamrice.discapp.data.model.*;
import io.github.shamrice.discapp.data.model.Thread;
import io.github.shamrice.discapp.data.repository.ReportedAbuseRepository;
import io.github.shamrice.discapp.data.repository.ThreadBodyRepository;
import io.github.shamrice.discapp.data.repository.ThreadPostCodeRepository;
import io.github.shamrice.discapp.data.repository.ThreadRepository;
import io.github.shamrice.discapp.service.application.ApplicationService;
import io.github.shamrice.discapp.service.configuration.ConfigurationProperty;
import io.github.shamrice.discapp.service.configuration.ConfigurationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

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
    private ThreadPostCodeRepository threadPostCodeRepository;

    @Autowired
    private ThreadActivityService threadActivityService;

    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    private ApplicationService applicationService;

    /**
     * Generates a new post code and persists it in the database to be claimed later on post
     * @param applicationId application id to create post code for
     * @return the generated post code
     */
    public String generatePostCode(long applicationId) {
        UUID newPostCode = UUID.randomUUID();

        ThreadPostCode threadPostCode = new ThreadPostCode();
        threadPostCode.setPostCode(newPostCode.toString());
        threadPostCode.setApplicationId(applicationId);
        threadPostCode.setCreateDt(new Date());

        threadPostCodeRepository.save(threadPostCode);

        log.info("Generated new post code: " + threadPostCode.toString());

        return threadPostCode.getPostCode();
    }

    /**
     * Redeems post code and returns true if success, otherwise returns failure.
     * @param applicationId application id post code is owned by
     * @param postCode post code to redeem
     * @return returns true if post code is valid, otherwise returns false.
     */
    public boolean redeemPostCode(long applicationId, String postCode) {

        if (postCode == null || postCode.trim().isEmpty()) {
            log.warn("Cannot redeem null or empty post code.");
            return false;
        }

        ThreadPostCode threadPostCode = threadPostCodeRepository.findById(postCode).orElse(null);
        if (threadPostCode != null && threadPostCode.getApplicationId() != null && threadPostCode.getApplicationId().equals(applicationId)) {
            log.info("Successfully redeemed post code: " + threadPostCode.toString() + " :: deleting entry and returning true.");
            threadPostCodeRepository.delete(threadPostCode);
            return true;
        }
        log.warn("Invalid or already redeemed post code attempted for appId: " + applicationId + " : postCode: "
                + postCode + " :: returning false");
        return false;
    }

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

    public void deleteReportedAbuse(long reportedAbuseId) {
        log.info("Deleting reported abuse record Id: " + reportedAbuseId);
        ReportedAbuse abuseToDelete = reportedAbuseRepository.findById(reportedAbuseId).orElse(null);
        if (abuseToDelete != null) {

            abuseToDelete.setIsDeleted(true);
            abuseToDelete.setModDt(new Date());
            abuseToDelete.getThread().setModDt(new Date());
            abuseToDelete.getThread().setDeleted(false);
            reportedAbuseRepository.save(abuseToDelete);
            log.info("Marked abuseId: " + reportedAbuseId + " as deleted. for appId " + abuseToDelete.getApplicationId());
        } else {
            log.warn("Could not find reported abuse to mark as deleted. Id attempted: " + reportedAbuseId);
        }
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

    public Long saveThread(Thread thread, String threadBodyText, boolean updateThreadActivity) {

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

            //if admin post is not set, default it to false.
            if (thread.getIsAdminPost() == null) {
                thread.setIsAdminPost(false);
            }

            Thread createThread = threadRepository.save(thread);
            ThreadBody threadBody = threadBodyRepository.findByThreadId(createThread.getId());

            //new thread body that's not blank
            if (threadBody == null && threadBodyText != null && !threadBodyText.trim().isEmpty()) {

                //truncate thread body if attempted is over 32KB
                if (threadBodyText.length() > 32000) {
                    threadBodyText = threadBodyText.substring(0, 32000);
                    log.warn("Thread body for new thread truncated to fit database field. AppId: "
                            + createThread.getApplicationId() + " :: Subject: " + createThread.getSubject()
                            + " :: Submitter: " + createThread.getSubmitter());
                }

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

                //only update activity table if thread is approved.
                if (createThread.isApproved() && updateThreadActivity) {
                    //create/update thread activity table.
                    Thread activityToUpdate = createThread;
                    if (createThread.getParentId() != 0) {
                        activityToUpdate = getRootThread(createThread);
                    }
                    threadActivityService.updateThreadActivity(activityToUpdate);
                }

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

    public List<Thread> searchThreads(Long applicationId, String searchText, int page, int numThreads) {

        Pageable limit = PageRequest.of(page, numThreads);
        return threadRepository.findByApplicationIdAndDeletedAndIsApprovedAndSubjectContainingIgnoreCaseOrApplicationIdAndDeletedAndIsApprovedAndBodyContainingIgnoreCaseOrderByCreateDtDesc(applicationId, false, true, searchText, applicationId, false, true, searchText, limit);
    }

    public Page<Thread> searchThreadsByFields(long applicationId, String submitter, String email, String subject, String ipAddress, String messageBody, boolean isApproved, int page, int numThreads) {

        Pageable limit = PageRequest.of(page, numThreads, Sort.by("createDt").descending());
        Thread searchThread = new Thread();

        //treat empty strings as null values so they are ignored in query.
        if (submitter.isEmpty()) {
            submitter = null;
        }
        if (email.isEmpty()) {
            email = null;
        }
        if (ipAddress.isEmpty()) {
            ipAddress = null;
        }
        if (subject.isEmpty()) {
            subject = null;
        }
        if (messageBody.isEmpty()) {
            messageBody = null;
        }

        searchThread.setApplicationId(applicationId);
        searchThread.setSubmitter(submitter);
        searchThread.setSubject(subject);
        searchThread.setEmail(email);
        searchThread.setIpAddress(ipAddress);
        searchThread.setBody(messageBody);
        searchThread.setApproved(isApproved);
        searchThread.setDeleted(false);

        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnoreCase()
                .withIgnoreNullValues()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);

        Example<Thread> searchQuery = Example.of(searchThread, matcher);

        return threadRepository.findAll(searchQuery, limit);
    }

    public List<ThreadTreeNode> getLatestThreads(Long applicationId, int page, int numThreads,
                                                 ThreadSortOrder threadSortOrder, boolean isExpandOnIndex) {

        List<Thread> parentThreads;
        Pageable limit = PageRequest.of(page, numThreads);
        if (threadSortOrder.equals(ThreadSortOrder.CREATION)) {
            //query to get latest parent threads (parentId = 0L) for an application
            parentThreads = threadRepository.findByApplicationIdAndParentIdAndDeletedAndIsApprovedOrderByCreateDtDesc(
                    applicationId,
                    TOP_LEVEL_THREAD_PARENT_ID,
                    false,
                    true,
                    limit
            );
        } else {
            //sort by activity. get list of threads based on thread activity table instead.
            parentThreads = new ArrayList<>();
            List<ThreadActivity> latestThreads = threadActivityService.getLatestThreadActivity(applicationId, limit);
            for (ThreadActivity threadActivity : latestThreads) {
                Thread threadToAdd = threadActivity.getThread();
                if (threadToAdd != null && !threadToAdd.getDeleted() && threadToAdd.isApproved()) {
                    parentThreads.add(threadActivity.getThread());
                }
            }
        }

        List<ThreadTreeNode> threadList = new ArrayList<>();

        if (isExpandOnIndex) {
            //create full thread node list based on parent threads.
            for (Thread parentThread : parentThreads) {
                threadList.add(getFullThreadTree(parentThread.getId()));
            }
        } else {
            for (Thread parent : parentThreads) {
                threadList.add(new ThreadTreeNode(parent));
            }
        }

        return threadList;
    }

    /**
     * Recursive function that will follow up the thread list until it finds the top thread in the list.
     * @param startThread
     * @return
     */
    private Thread getRootThread(Thread startThread) {
        if (startThread.getParentId() != 0) {
            Thread nextThread = threadRepository.getOneByApplicationIdAndId(startThread.getApplicationId(), startThread.getParentId());
            startThread = getRootThread(nextThread);
        }
        return startThread;
    }

    /**
     * Only returns the latest posted threads. includes threads that were replies
     * @param applicationId
     * @param numThreads
     * @return
     */
    public List<Thread> getLatestThreads(long applicationId, int page, int numThreads) {
        Pageable limit = PageRequest.of(page, numThreads);
        return threadRepository.findByApplicationIdAndDeletedAndIsApprovedOrderByCreateDtDesc(
                applicationId,
                false,
                true,
                limit
        );
    }

    /**
     * Returns a list of thread tree nodes of latest threads.
     * @param applicationId
     * @param page
     * @param numThreads
     * @return
     */
    public List<ThreadTreeNode> getLatestThreadNodes(long applicationId, int page, int numThreads) {

        List<ThreadTreeNode> threadTreeNodes = new ArrayList<>();
        List<Thread> threadList = getLatestThreads(applicationId, page, numThreads);

        for (Thread thread : threadList) {
            threadTreeNodes.add(new ThreadTreeNode(thread));
        }

        return threadTreeNodes;
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
