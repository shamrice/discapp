package io.github.shamrice.discapp.web.util;

import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class InputHelper {

    /**
     * Remove HTML via regex and trailing white spaces from text.
     * @param text Text to clean
     * @return Cleaned up text
     */
    public String sanitizeInput(String text) {
        return text.replaceAll("<[^>]*>", " ").trim();
    }

    /**
     * Adds anchor HTML tags to the text supplied. Does not add anchors for ones only starting with www.
     * Source modified from: https://stackoverflow.com/questions/49425990/replace-url-in-text-with-href-in-java
     * @param text text to add anchors to
     * @return returns text with html anchor tags added where found.
     */
    public String addUrlHtmlLinksToString(String text) {
        String urlRegex = "(https?|ftp):\\/\\/[-a-zA-Z0-9@:%._\\+~#=]{2,256}\\.[A-Za-z]{2,6}\\b(\\/[-a-zA-Z0-9@:%_\\+.~#?&\\/\\/=]*)*(?:\\/|\\b)";
        Pattern pattern = Pattern.compile(urlRegex);
        Matcher matcher = pattern.matcher(text);

        StringBuffer stringBuffer = new StringBuffer();
        while (matcher.find()) {
            String foundUrl = matcher.group(0);
            matcher.appendReplacement(stringBuffer, "<a href=\"" + foundUrl + "\">" + foundUrl + "</a>");
        }

        matcher.appendTail(stringBuffer);
        return stringBuffer.toString();
    }
}
