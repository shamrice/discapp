package io.github.shamrice.discapp.web.util;

import org.springframework.stereotype.Component;

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
}
