package io.github.shamrice.discapp.web.util;

import io.github.shamrice.discapp.service.configuration.ConfigurationProperty;
import io.github.shamrice.discapp.service.configuration.ConfigurationService;
import io.github.shamrice.discapp.service.configuration.UserConfigurationProperty;
import io.github.shamrice.discapp.service.thread.ThreadTreeNode;
import io.github.shamrice.discapp.service.thread.UserReadThreadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.TimeZone;

import static io.github.shamrice.discapp.web.define.CommonModelAttributeNames.*;
import static io.github.shamrice.discapp.web.define.CommonModelAttributeNames.REPLY_BUTTON_IS_IMAGE;

@Component
@Slf4j
public class DiscAppHelper {

    @Autowired
    private AccountHelper accountHelper;

    @Autowired
    private InputHelper inputHelper;

    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    private UserReadThreadService userReadThreadService;

    public void setButtonModelAttributes(long appId, Model model) {

        //post message button.
        String postMessageButtonText = configurationService.getStringValue(appId, ConfigurationProperty.POST_MESSAGE_BUTTON_TEXT, "Post Message");
        model.addAttribute(POST_MESSAGE_BUTTON_TEXT, postMessageButtonText);

        if (postMessageButtonText.toLowerCase().startsWith("http")) {
            model.addAttribute(POST_MESSAGE_BUTTON_IS_IMAGE, true);
        }

        String previousButtonText = configurationService.getStringValue(appId, ConfigurationProperty.PREVIOUS_PAGE_BUTTON_TEXT, "Previous Page");
        model.addAttribute(PREVIOUS_PAGE_BUTTON_TEXT, previousButtonText);

        if (previousButtonText.toLowerCase().startsWith("http")) {
            model.addAttribute(PREVIOUS_PAGE_BUTTON_IS_IMAGE, true);
        }

        String nextButtonText = configurationService.getStringValue(appId, ConfigurationProperty.NEXT_PAGE_BUTTON_TEXT, "Next Page");
        model.addAttribute(NEXT_PAGE_BUTTON_TEXT, nextButtonText);

        if (nextButtonText.toLowerCase().startsWith("http")) {
            model.addAttribute(NEXT_PAGE_BUTTON_IS_IMAGE, true);
        }

        String previewButtonText = configurationService.getStringValue(appId, ConfigurationProperty.PREVIEW_BUTTON_TEXT, "Preview");
        model.addAttribute(PREVIEW_BUTTON_TEXT, previewButtonText);

        if (previewButtonText.toLowerCase().startsWith("http")) {
            model.addAttribute(PREVIEW_BUTTON_IS_IMAGE, true);
        }

        String returnButtonText = configurationService.getStringValue(appId, ConfigurationProperty.RETURN_TO_MESSAGES_BUTTON_TEXT, "Return to Messages");
        model.addAttribute(RETURN_BUTTON_TEXT, returnButtonText);

        if (returnButtonText.toLowerCase().startsWith("http")) {
            model.addAttribute(RETURN_BUTTON_IS_IMAGE, true);
        }

        String editButtonText = configurationService.getStringValue(appId, ConfigurationProperty.EDIT_BUTTON_TEXT, "Edit Message");
        model.addAttribute(EDIT_BUTTON_TEXT, editButtonText);

        if (editButtonText.toLowerCase().startsWith("http")) {
            model.addAttribute(EDIT_BUTTON_IS_IMAGE, true);
        }

        String replyButtonText = configurationService.getStringValue(appId, ConfigurationProperty.POST_REPLY_MESSAGE_BUTTON_TEXT, "Post Reply");
        model.addAttribute(REPLY_BUTTON_TEXT, replyButtonText);

        if (replyButtonText.toLowerCase().startsWith("http")) {
            model.addAttribute(REPLY_BUTTON_IS_IMAGE, true);
        }
    }


    public String getAppViewTopThreadHtml(ThreadTreeNode currentNode, String entryBreakString,
                                           boolean showPreviewText, int currentPage, int maxPreviewLength, String[] readThreads) {

        String messageDivText = "first_message_div";
        String messageHeaderText = "first_message_header";
        String messageSpanText = "first_message_span";

        //check if thread is read.
        boolean isRead = userReadThreadService.csvContainsThreadId(readThreads, currentNode.getCurrent().getId());

        //only highlight unread messages.
        if (!isRead && isNewMessageHighlighted(currentNode)) {
            messageSpanText += " new_message";
        }

        String topThreadHtml = "" +
                "        <div class=\"" + messageDivText + "\">" +
                "            <div class=\"" + messageHeaderText + "\">" +
                "               <span class=\"" + messageSpanText + "\">" +
                "                   <a class=\"article_link";

        //add read thread css to link if thread has been marked as read.
        if (isRead) {
            topThreadHtml += " read";
        }

        topThreadHtml +=        "\" href=\"/discussion.cgi?disc=" + currentNode.getCurrent().getApplicationId() +
                "&amp;article=" + currentNode.getCurrent().getId() +
                "&amp;page=" + currentPage + "\"" +
                " name=\"" + currentNode.getCurrent().getId() + "\">" +
                currentNode.getCurrent().getSubject() +
                "                   </a> ";

        topThreadHtml += entryBreakString +
                "                   <span class=\"author_cell\">" + currentNode.getCurrent().getSubmitter();

        //mark thread as admin post if it's set as one.
        if (currentNode.getCurrent().getIsAdminPost() != null && currentNode.getCurrent().getIsAdminPost()) {
            topThreadHtml += "<span class=\"admin_post\">Admin</span>";
        }

        topThreadHtml += ",</span> " +
                "                   <span class=\"date_cell\">" +
                getAdjustedDateStringForConfiguredTimeZone(
                        currentNode.getCurrent().getApplicationId(),
                        currentNode.getCurrent().getCreateDt(),
                        false) +
                "                   </span>" +
                "               </span>" +
                "            </div>" +
                "        </div>";

        if (showPreviewText) {
            String previewText = currentNode.getCurrent().getBody();
            if (previewText != null && !previewText.isEmpty()) {

                previewText = inputHelper.sanitizeInput(previewText); //remove html from thread preview

                if (previewText.length() > maxPreviewLength) {
                    previewText = previewText.substring(0, maxPreviewLength);
                    previewText +=  "...<a class=\"article_link\" href=\"/discussion.cgi?disc=" + currentNode.getCurrent().getApplicationId() +
                            "&amp;article=" + currentNode.getCurrent().getId() +
                            "&amp;page=" + currentPage + "\"" +
                            " name=\"" + currentNode.getCurrent().getId() + "\">" +
                            " more</a> ";
                }
                topThreadHtml += "<div class=\"first_message\">" + previewText + "</div>";
            }
        }

        return topThreadHtml;
    }

    /**
     * Recursive function that builds HTML for each thread in the app.
     * @param currentNode Current node being built. When calling, top level node is passed.
     * @param currentHtml Current html string. This is the the string that is built and returned
     * @return built HTML list structure for thread
     */
    public String getAppViewThreadHtml(ThreadTreeNode currentNode, String currentHtml, String entryBreakString,
                                        boolean skipCurrentNode, long currentlyViewedId,
                                        boolean showPreviewText, int currentPage, int maxPreviewLength,
                                        int currentThreadDepth, int maxThreadDepth, String[] readThreads) {

        if (!skipCurrentNode) {

            //check if thread is read.
            boolean isRead = userReadThreadService.csvContainsThreadId(readThreads, currentNode.getCurrent().getId());

            //increment thread depth. if hit max, set html and return back.
            currentThreadDepth++;
            if (currentThreadDepth >= maxThreadDepth) {
                int numOfReplies = getThreadCount(currentNode, 0);
                return currentHtml + "<li class=\"\">" +
                        "<a class=\"\" href=\"/discussion.cgi?disc=" + currentNode.getCurrent().getApplicationId() +
                        "&amp;article=" + currentNode.getCurrent().getId() +
                        "&amp;page=" + currentPage + "\">" + numOfReplies + " more comments</a>" +
                        "</ul>";
            }

            //current entry gets different css and no inner div.
            if (currentNode.getCurrent().getId().equals(currentlyViewedId)) {
                currentHtml += "<li class=\"current_entry\">";
            } else {
                currentHtml += "<li class=\"message_entry\">" +
                        " <div class=\"response_headers\">";
            }

            //only highlight new messages if they're not read.
            if (!isRead && isNewMessageHighlighted(currentNode)) {
                currentHtml += "   <span class=\"response_headers new_message\">";
            } else if (!isNewMessageHighlighted(currentNode) && currentNode.getCurrent().getId().equals(currentlyViewedId)) {
                //if current thread, apply different css class to span
                currentHtml += "   <span class=\"current_entry\">";
            } else {
                currentHtml += "   <span class=\"response_headers\">";
            }

            //if rendering current thread, subject line should not have an anchor tag.
            if (currentNode.getCurrent().getId().equals(currentlyViewedId)) {
                currentHtml += currentNode.getCurrent().getSubject();
            } else {
                currentHtml += "      <a class=\"article_link";

                //if thread is read, add css style to anchor tag.
                if (isRead) {
                    currentHtml += " read";
                }

                currentHtml += "\" href=\"/discussion.cgi?disc=" + currentNode.getCurrent().getApplicationId() +
                        "&amp;article=" + currentNode.getCurrent().getId() +
                        "&amp;page=" + currentPage + "\"" +
                        " name=\"" + currentNode.getCurrent().getId() + "\">" +
                        currentNode.getCurrent().getSubject() +
                        "</a> ";
            }

            currentHtml += entryBreakString +
                    "      <span class=\"author_cell\">" + currentNode.getCurrent().getSubmitter();

            //mark thread as admin post if it's set as one.
            if (currentNode.getCurrent().getIsAdminPost() != null && currentNode.getCurrent().getIsAdminPost()) {
                currentHtml += "<span class=\"admin_post\">Admin</span>";
            }

            currentHtml += ",</span> ";

            currentHtml +=  "     <span class=\"date_cell\">" +
                    getAdjustedDateStringForConfiguredTimeZone(
                            currentNode.getCurrent().getApplicationId(),
                            currentNode.getCurrent().getCreateDt(),
                            false) +
                    "       </span>" +
                    "   </span>";
            //close div tag on non-current entries.
            if (!currentNode.getCurrent().getId().equals(currentlyViewedId)) {
                currentHtml += "</div>";
            }

            //only show preview if selected and not currently viewed thread
            if (showPreviewText && !currentNode.getCurrent().getId().equals(currentlyViewedId)) {
                String previewText = currentNode.getCurrent().getBody();
                if (previewText != null && !previewText.isEmpty()) {

                    //remove any css style tags or script tags from preview and the content inside the tags.
                    previewText = previewText.replaceAll("(?i)<style.+?</style>", "");
                    previewText = previewText.replaceAll("(?i)<script.+?</script>", "");
                    previewText = inputHelper.sanitizeInput(previewText); //clear HTML from preview thread body

                    if (previewText.length() > maxPreviewLength) {
                        previewText = previewText.substring(0, maxPreviewLength);
                        previewText +=  "...<a class=\"article_link\" href=\"/discussion.cgi?disc=" + currentNode.getCurrent().getApplicationId() +
                                "&amp;article=" + currentNode.getCurrent().getId() +
                                "&amp;page=" + currentPage + "\"" +
                                " name=\"" + currentNode.getCurrent().getId() + "\">" +
                                " more</a> ";
                    }
                    currentHtml += "<div class=\"message_preview\">" + previewText + "</div>";
                }
            }

            currentHtml += "</li>";
        }

        //recursively generate reply tree structure
        for (ThreadTreeNode node : currentNode.getSubThreads()) {

            currentHtml += "<li class=\"nested_list\"><ul>";
            currentHtml = getAppViewThreadHtml(node, currentHtml, entryBreakString, false,
                    currentlyViewedId, showPreviewText, currentPage, maxPreviewLength, currentThreadDepth, maxThreadDepth,
                    readThreads);
            currentHtml += "</li>";
        }

        currentHtml += "</ul>";


        return currentHtml;
    }


    public String getAdjustedDateStringForConfiguredTimeZone(long appId, Date date, boolean includeComma) {

        String timeZoneLocation = configurationService.getStringValue(appId, ConfigurationProperty.TIMEZONE_LOCATION, "UTC");

        //if user is logged in and they have their time zone set to override. Use that instead.
        Long userId = accountHelper.getLoggedInDiscAppUserId();
        if (userId != null) {
            if (configurationService.getUserConfigBooleanValue(userId, UserConfigurationProperty.USER_TIMEZONE_ENABLED, false)) {
                timeZoneLocation = configurationService.getUserConfigStringValue(userId, UserConfigurationProperty.USER_TIMEZONE_LOCATION, timeZoneLocation);
                log.debug("User id: " + userId + " has time zone override enabled. Setting time zone to: " + timeZoneLocation);
            }
        }

        String dateFormatPattern = configurationService.getStringValue(appId, ConfigurationProperty.DATE_FORMAT_PATTERN, "EEE MMM dd, yyyy h:mma");

        DateFormat dateFormat = new SimpleDateFormat(dateFormatPattern);
        dateFormat.setTimeZone(TimeZone.getTimeZone(timeZoneLocation));

        //am and pm should be lowercase.
        String formattedString = dateFormat.format(date).replace("AM", "am").replace("PM", "pm");

        if (!includeComma) {
            formattedString = formattedString.replace(",", "");
        }

        return formattedString;
    }

    public boolean isNewMessageHighlighted(ThreadTreeNode currentNode) {

        if (currentNode != null && currentNode.getCurrent() != null && currentNode.getCurrent().getCreateDt() != null) {

            boolean highlightNewMessages = configurationService.getBooleanValue(
                    currentNode.getCurrent().getApplicationId(),
                    ConfigurationProperty.HIGHLIGHT_NEW_MESSAGES, false);

            Instant now = Instant.now();
            Instant currentNodeInstant = currentNode.getCurrent().getCreateDt().toInstant();

            return highlightNewMessages && currentNodeInstant.isAfter(now.minus(24, ChronoUnit.HOURS));
        }

        log.warn("Null thread node or create date sent to be checked if highlight functionality should be applied.");
        return false;
    }

    private int getThreadCount(ThreadTreeNode threadTreeNode, int currentCount) {
        for (ThreadTreeNode subThread : threadTreeNode.getSubThreads()) {
            currentCount = getThreadCount(subThread, currentCount);
        }
        currentCount++;
        return currentCount;
    }

}
