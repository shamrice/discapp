package io.github.shamrice.discapp.web.util;

import io.github.shamrice.discapp.service.configuration.ConfigurationProperty;
import io.github.shamrice.discapp.service.configuration.ConfigurationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.json.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class InputHelper {

    @Autowired
    private ConfigurationService configurationService;

    public String convertHtmlToPlainText(String text) {
        return text.replace("<", "&lt;").replace(">", "&gt;").trim();
    }

    public String convertScriptAndStyleTags(String text) {
        return text.replaceAll("(?i)<\\s*script", "&lt;script")
                .replaceAll("(?i)<\\s*style", "&lt;style")
                .replaceAll("(?i)<\\s*/\\s*script\\s*>", "&lt;/script&gt;")
                .replaceAll("(?i)<\\s*/\\s*style\\s*>", "&lt;/style&gt;");
    }

    /**
     * Remove HTML via regex and trailing white spaces from text.
     *
     * @param text Text to clean
     * @return Cleaned up text
     */
    public String sanitizeInput(String text) {
        return text.replaceAll("<[^>]*>", " ").trim();
    }

    /**
     * Adds anchor HTML tags to the text supplied. Does not add anchors for ones only starting with www.
     * Source modified from: https://stackoverflow.com/questions/49425990/replace-url-in-text-with-href-in-java
     *
     * @param text text to add anchors to
     * @return returns text with html anchor tags added where found.
     */
    public String addUrlHtmlLinksToString(String text) {
        //String urlRegex = "        (https? |ftp):\\/\\/[-a-zA-Z0-9@:%._\\+~#=]{2,256}\\.[A-Za-z]{2,6}\\b(\\/[-a-zA-Z0-9@:%_\\+.~#?&\\/\\/=]*)*(?:\\/|\\b)";
        String urlRegex = "((\"https?|https?)|ftp):\\/\\/[-a-zA-Z0-9@:%._\\+~#=]{2,256}\\.[A-Za-z]{2,6}\\b(\\/[-a-zA-Z0-9@:%_\\+.~#?&\\/\\/=]*)*(?:\"|\\/|\\b)";
        Pattern pattern = Pattern.compile(urlRegex);
        Matcher matcher = pattern.matcher(text);

        StringBuffer stringBuffer = new StringBuffer();
        while (matcher.find()) {
            String foundUrl = matcher.group(0);
            if (!foundUrl.startsWith("\"") && !foundUrl.endsWith("\"")) { //ignore already quoted urls
                matcher.appendReplacement(stringBuffer, "<a target=\"_blank\" href=\"" + foundUrl + "\">" + foundUrl + "</a>");
            }
        }

        matcher.appendTail(stringBuffer);
        return stringBuffer.toString();
    }

    public boolean verifyReCaptchaResponse(String reCaptchaResponse) {

        if (reCaptchaResponse == null || reCaptchaResponse.trim().isEmpty()) {
            return false;
        }

        if (!configurationService.getBooleanValue(ConfigurationService.SITE_WIDE_CONFIGURATION_APP_ID, ConfigurationProperty.RE_CAPTCHA_VERIFY_ENABLED, false)) {
            log.warn("Skipping ReCaptcha response verification. Configuration: "
                    + ConfigurationProperty.RE_CAPTCHA_VERIFY_ENABLED.getPropName() + " is not set to true.");
            return true;
        }

        String secret = configurationService.getStringValue(ConfigurationService.SITE_WIDE_CONFIGURATION_APP_ID, ConfigurationProperty.RE_CAPTCHA_SECRET, "");

        if (secret.trim().isEmpty()) {
            log.warn("Skipping ReCaptcha response verification. ReCaptcha secret configuration value not set: "
                    + ConfigurationProperty.RE_CAPTCHA_SECRET.getPropName() + ".");
            return true;
        }

        log.info("Verifying ReCaptcha response: " + reCaptchaResponse);

        String verifyBaseUrl = configurationService.getStringValue(ConfigurationService.SITE_WIDE_CONFIGURATION_APP_ID, ConfigurationProperty.RE_CAPTCHA_VERIFY_URL, "https://www.google.com/recaptcha/api/siteverify");
        String url = verifyBaseUrl + "?secret=" + secret + "&response=" + reCaptchaResponse;

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        Map<String, String> map = new HashMap<>();
        map.put("Content-Type", "application/json");
        headers.setAll(map);

        Map<String, String> reqPayload = new HashMap<>();

        HttpEntity<?> request = new HttpEntity<>(reqPayload, headers);

        ResponseEntity<?> response = new RestTemplate().postForEntity(url, request, String.class);

        if (response != null && response.getBody() != null) {
            try {
                log.info("ReCaptcha response verification for: " + reCaptchaResponse + " = "
                        + response.getBody().toString());

                JSONParser jsonParser = new JSONParser(response.getBody().toString());
                LinkedHashMap jsonObject = (LinkedHashMap) jsonParser.parse();

                boolean success = Boolean.parseBoolean(jsonObject.get("success").toString());

                if (success) {
                    log.info("ReCaptcha verification success for response: " + reCaptchaResponse);
                    return true;
                } else {
                    log.warn("ReCaptcha verification failed for response: " + reCaptchaResponse);
                    return false;
                }

            } catch (Exception ex) {
                log.error("Exception getting ReCaptcha response for : " + reCaptchaResponse + " :: " + ex.getMessage(), ex);
                return false;
            }
        }
        log.error("ReCaptcha failed. Service returned null response or null response body for : " + reCaptchaResponse);
        return false;
    }
}
