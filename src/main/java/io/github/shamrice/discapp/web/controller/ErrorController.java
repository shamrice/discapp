package io.github.shamrice.discapp.web.controller;

import io.github.shamrice.discapp.web.define.url.ErrorUrl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

import static io.github.shamrice.discapp.web.define.CommonModelAttributeNames.ERROR_STATUS_CODE;
import static io.github.shamrice.discapp.web.define.CommonModelAttributeNames.ERROR_TEXT;
import static io.github.shamrice.discapp.web.define.url.ErrorUrl.ERROR_NOT_FOUND;
import static io.github.shamrice.discapp.web.define.url.ErrorUrl.ERROR_PERMISSION_DENIED;

@Controller
@Slf4j
public class ErrorController implements org.springframework.boot.web.servlet.error.ErrorController {

    private static final String STATUS_CODE_ATTRIBUTE_NAME = "javax.servlet.error.status_code";
    private static final String ERROR_EXCEPTION_ATTRIBUTE_NAME = "javax.servlet.error.exception";
    private static final String ATTEMPTED_URI_ATTRIBUTE_NAME = "javax.servlet.error.request_uri";

    @RequestMapping(value = ErrorUrl.CONTROLLER_DIRECTORY_URL, produces = "text/html", method = RequestMethod.GET)
    public ModelAndView getErrorView(HttpServletRequest request, Model model) {

        Integer statusCode = (Integer) request.getAttribute(STATUS_CODE_ATTRIBUTE_NAME);
        Exception exception = (Exception) request.getAttribute(ERROR_EXCEPTION_ATTRIBUTE_NAME);
        String attemptedUri = (String) request.getAttribute(ATTEMPTED_URI_ATTRIBUTE_NAME);

        String errorLogMessage = "";
        if (exception != null && exception.getMessage() != null) {
            errorLogMessage = exception.getMessage();
        }

        log.error("Error: status code: " + statusCode + " : " + errorLogMessage + " :: attempted uri: "
                + attemptedUri, exception);

        if (statusCode != null && statusCode.equals(HttpStatus.FORBIDDEN.value())) {
            log.info("Status code 403 " + errorLogMessage, exception);
            return new ModelAndView("redirect:/login?relogin=true&" + request.getQueryString());
        }

        String errorText = "An error has occurred. Please try again.";

        model.addAttribute(ERROR_STATUS_CODE, statusCode);
        model.addAttribute(ERROR_TEXT, errorText);

        return new ModelAndView("error/error", "model", model);

    }

    @PostMapping(ErrorUrl.CONTROLLER_DIRECTORY_URL)
    public ModelAndView postErrorController() {
        //always just redirect to homepage.
        return new ModelAndView("redirect:/");
    }

    @RequestMapping(value = ERROR_NOT_FOUND)
    public ModelAndView getNotFoundView(String errorText, Model model) {
        model.addAttribute(ERROR_TEXT, errorText);
        return new ModelAndView("error/notFound");
    }

    @RequestMapping(value = ERROR_PERMISSION_DENIED)
    public ModelAndView getPermissionDeniedView(String errorText, Model model) {
        model.addAttribute(ERROR_TEXT, errorText);
        return new ModelAndView("error/permissionDenied");
    }

    @Override
    @Deprecated
    public String getErrorPath() {
        return ErrorUrl.CONTROLLER_DIRECTORY_URL;
    }

}
