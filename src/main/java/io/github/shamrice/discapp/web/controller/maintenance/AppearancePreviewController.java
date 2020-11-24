package io.github.shamrice.discapp.web.controller.maintenance;

import io.github.shamrice.discapp.data.model.Application;
import io.github.shamrice.discapp.data.model.Thread;
import io.github.shamrice.discapp.service.configuration.ConfigurationProperty;
import io.github.shamrice.discapp.service.thread.ThreadSortOrder;
import io.github.shamrice.discapp.service.thread.ThreadTreeNode;
import io.github.shamrice.discapp.web.model.discapp.ThreadViewModel;
import io.github.shamrice.discapp.web.util.DiscAppHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.github.shamrice.discapp.web.define.CommonModelAttributeNames.*;
import static io.github.shamrice.discapp.web.define.CommonModelAttributeNames.ERROR;

@Controller
@Slf4j
public class AppearancePreviewController extends MaintenanceController {

    @Autowired
    private DiscAppHelper discAppHelper;

    private static final String FAKE_BODY_PLACEHOLDER = "This text is a placeholder example for the message's body text. This text is a placeholder example for the message's body text. This text is a placeholder example for the message's body text. This text is a placeholder example for the message's body text. This text is a placeholder example for the message's body text. This text is a placeholder example for the message's body text. This text is a placeholder example for the message's body text. This text is a placeholder example for the message's body text. This text is a placeholder example for the message's body text. This text is a placeholder example for the message's body text. This text is a placeholder example for the message's body text. This text is a placeholder example for the message's body text. This text is a placeholder example for the message's body text. This text is a placeholder example for the message's body text. This text is a placeholder example for the message's body text. This text is a placeholder example for the message's body text. ";
    private static final String FAKE_SUBMITTER_PLACEHOLDER = "Test Submitter";
    private static final String FAKE_SUBJECT_PLACEHOLDER = "Test message";

    @GetMapping(CONTROLLER_URL_DIRECTORY + "appearance-preview.cgi")
    public ModelAndView getAppearancePreviewView(@RequestParam(name = "id") long appId,
                                                 Model model,
                                                 HttpServletRequest request) {
        try {
            Application app = applicationService.get(appId);

            if (app != null) {

                model.addAttribute(APP_NAME, app.getName());
                model.addAttribute(APP_ID, app.getId());

                model.addAttribute(PROLOGUE_TEXT, applicationService.getPrologueText(app.getId()));
                model.addAttribute(EPILOGUE_TEXT, applicationService.getEpilogueText(app.getId()));

                discAppHelper.setButtonModelAttributes(app.getId(), model);

                model.addAttribute(HAS_PREVIOUS_PAGE, true);
                model.addAttribute(PREVIOUS_PAGE, 2);

                model.addAttribute(CURRENT_PAGE, 3);
                model.addAttribute(NEXT_PAGE, 4);
                model.addAttribute(HAS_NEXT_PAGE, true); // default.

                //get threads
                int maxThreads = configurationService.getIntegerValue(appId, ConfigurationProperty.MAX_THREADS_ON_INDEX_PAGE, 25);
                boolean showTopLevelPreview = configurationService.getBooleanValue(appId, ConfigurationProperty.PREVIEW_FIRST_MESSAGE_OF_THREAD_ON_INDEX_PAGE, true);
                boolean isExpandOnIndex = configurationService.getBooleanValue(appId, ConfigurationProperty.EXPAND_THREADS_ON_INDEX_PAGE, false);
                String threadSortOrder = configurationService.getStringValue(appId, ConfigurationProperty.THREAD_SORT_ORDER, ThreadSortOrder.CREATION.name());

                //List<ThreadTreeNode> threadTreeNodeList = threadService.getLatestThreads(app.getId(), 0, maxThreads, ThreadSortOrder.valueOf(threadSortOrder.toUpperCase()), isExpandOnIndex);
                List<ThreadTreeNode> threadTreeNodeList = generateFakeThreadTreeNodes(appId, ThreadSortOrder.valueOf(threadSortOrder.toUpperCase()), isExpandOnIndex);


                if (isExpandOnIndex) {

                    List<String> threadTreeHtml = new ArrayList<>();
                    String entryBreakString = configurationService.getStringValue(appId, ConfigurationProperty.ENTRY_BREAK_TEXT, "-");
                    int maxPreviewLengthTopLevelThread = configurationService.getIntegerValue(appId, ConfigurationProperty.PREVIEW_FIRST_MESSAGE_LENGTH_IN_NUM_CHARS, 320);
                    int maxPreviewLengthReplies = configurationService.getIntegerValue(appId, ConfigurationProperty.PREVIEW_REPLY_LENGTH_IN_NUM_CHARS, 200);
                    int maxThreadDepth = configurationService.getIntegerValue(appId, ConfigurationProperty.THREAD_DEPTH_ON_INDEX_PAGE, 30);

                    for (ThreadTreeNode threadTreeNode : threadTreeNodeList) {

                        String currentHtml = discAppHelper.getAppViewTopThreadHtml(threadTreeNode, entryBreakString, showTopLevelPreview,
                                0, maxPreviewLengthTopLevelThread, new String[0]);

                        //get replies if they exist and add on HTML.
                        if (threadTreeNode.getSubThreads() != null && threadTreeNode.getSubThreads().size() > 0) {
                            currentHtml += "<div class=\"responses\">";
                            currentHtml += discAppHelper.getAppViewThreadHtml(threadTreeNode, "",
                                    entryBreakString, true, -1,
                                    false, 0, maxPreviewLengthReplies,
                                    0, maxThreadDepth, new String[0]);
                            currentHtml = currentHtml.substring(0, currentHtml.lastIndexOf("</ul>")); //remove trailing ul tag
                            currentHtml += "</div>";
                        }

                        currentHtml = replaceUrlsWithPlaceholder(currentHtml);
                        threadTreeHtml.add(currentHtml);
                    }

                    model.addAttribute(THREAD_NODE_LIST, threadTreeHtml);
                } else {
                    model.addAttribute(DATE_LABEL, configurationService.getStringValue(appId, ConfigurationProperty.DATE_LABEL_TEXT, "Date:"));
                    model.addAttribute(SUBMITTER_LABEL, configurationService.getStringValue(appId, ConfigurationProperty.SUBMITTER_LABEL_TEXT, "Submitter:"));
                    model.addAttribute(SUBJECT_LABEL, configurationService.getStringValue(appId, ConfigurationProperty.SUBJECT_LABEL_TEXT, "Subject:"));

                    List<ThreadViewModel> threads = new ArrayList<>();

                    //generate fake threads.
                    for (int i = 0; i < 5; i++) {

                        ThreadViewModel threadViewModel = new ThreadViewModel();

                        threadViewModel.setSubmitter(FAKE_SUBMITTER_PLACEHOLDER);
                        threadViewModel.setSubject(FAKE_SUBJECT_PLACEHOLDER);
                        threadViewModel.setCreateDt(discAppHelper.getAdjustedDateStringForConfiguredTimeZone(appId, new Date(), false));
                        threadViewModel.setId(String.valueOf(i));
                        threadViewModel.setShowMoreOnPreviewText(false);
                        threadViewModel.setHighlighted(false);
                        threadViewModel.setAdminPost(false);

                        String previewText = inputHelper.sanitizeInput(FAKE_BODY_PLACEHOLDER.substring(0, 320));
                        previewText += "...";
                        threadViewModel.setShowMoreOnPreviewText(true);
                        threadViewModel.setPreviewText(previewText);

                        threads.add(threadViewModel);
                    }
                    model.addAttribute(THREADS, threads);

                }

                model.addAttribute(HEADER_TEXT, configurationService.getStringValue(appId, ConfigurationProperty.HEADER_TEXT, ""));
                model.addAttribute(FOOTER_TEXT, configurationService.getStringValue(appId, ConfigurationProperty.FOOTER_TEXT, ""));
                model.addAttribute(THREAD_SEPARATOR, configurationService.getStringValue(appId, ConfigurationProperty.THREAD_BREAK_TEXT, "<hr />"));
                model.addAttribute(FAVICON_URL, configurationService.getStringValue(appId, ConfigurationProperty.FAVICON_URL, "/favicon.ico"));
                model.addAttribute(STYLE_SHEET_URL, configurationService.getStringValue(appId, ConfigurationProperty.STYLE_SHEET_URL, "/styles/default.css"));
                model.addAttribute(IS_EXPAND_ON_INDEX, isExpandOnIndex);
                model.addAttribute(IS_SHOW_TOP_LEVEL_PREVIEW, showTopLevelPreview);

                return new ModelAndView("admin/appearance-preview");

            } else {
                model.addAttribute(ERROR, "Disc app with id " + appId + " returned null.");
                log.info("Disc app with application id of " + appId + " does not exist. Returning null.");
            }
        } catch (Exception ex) {
            model.addAttribute(ERROR, "No disc app with id " + appId + " found. " + ex.getMessage());
            log.error("Error getting disc app with id of " + appId + ". Returning null. ", ex);
        }

        return new ModelAndView("admin/appearance-preview");

    }

    private List<ThreadTreeNode> generateFakeThreadTreeNodes(long appId, ThreadSortOrder threadSortOrder, boolean isExpandOnIndex) {

        List<ThreadTreeNode> threadTreeNodeList = new ArrayList<>();
        List<ThreadTreeNode> subThread1 = new ArrayList<>();
        List<ThreadTreeNode> subThread2 = new ArrayList<>();

        //add new node to demonstrate highlight
        ThreadTreeNode newestNode = new ThreadTreeNode(getFakeThread(appId, true));
        threadTreeNodeList.add(newestNode);

        //nested message
        ThreadTreeNode node = new ThreadTreeNode(getFakeThread(appId, false));
        subThread2.add(node);

        //nested message
        ThreadTreeNode node1 = new ThreadTreeNode(getFakeThread(appId, false));
        node1.setSubThreads(subThread2);
        subThread1.add(node1);

        //create two threads with nested sub threads.
        for (int i = 0; i < 2; i++) {
            ThreadTreeNode node2 = new ThreadTreeNode(getFakeThread(appId, false));
            node2.setSubThreads(subThread1);
            threadTreeNodeList.add(node2);
        }

        return threadTreeNodeList;
    }

    Thread getFakeThread(long appId, boolean useCurrentDate) {

        Date createDt = new Date();
        if (!useCurrentDate) {
            createDt = Date.from(LocalDate.parse("2020-10-01").atStartOfDay().toInstant(ZoneOffset.UTC));
        }

        Thread testThread = new Thread();
        testThread.setSubject(FAKE_SUBJECT_PLACEHOLDER);
        testThread.setBody(FAKE_BODY_PLACEHOLDER);
        testThread.setParentId(0L);
        testThread.setSubmitter(FAKE_SUBMITTER_PLACEHOLDER);
        testThread.setId(9999L);
        testThread.setApplicationId(appId);
        testThread.setIsAdminPost(false);
        testThread.setApproved(true);
        testThread.setDeleted(false);
        testThread.setCreateDt(createDt);
        return testThread;
    }


    private String replaceUrlsWithPlaceholder(String text) {
        //replace anchor tags with "#" instead of actual urls.
        String urlRegex = "href=([\"'])(?:(?=(\\\\?))\\2.)*?\\1";
        Pattern pattern = Pattern.compile(urlRegex);
        Matcher matcher = pattern.matcher(text);

        StringBuffer stringBuffer = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(stringBuffer, "href=\"#\"");
        }

        matcher.appendTail(stringBuffer);

        //remove form tags.
        String formRegex = "(?i)<(.*)form(.*?)>";
        pattern = Pattern.compile(formRegex);
        matcher = pattern.matcher(stringBuffer.toString());

        stringBuffer = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(stringBuffer, "<br />");
        }

        matcher.appendTail(stringBuffer);

        return stringBuffer.toString();
    }
}
