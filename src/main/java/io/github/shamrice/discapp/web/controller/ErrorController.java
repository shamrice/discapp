package io.github.shamrice.discapp.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

@Controller
@Slf4j
public class ErrorController implements org.springframework.boot.web.servlet.error.ErrorController {

    private static final String STATUS_CODE_ATTRIBUTE_NAME = "javax.servlet.error.status_code";
    private static final String ERROR_EXCEPTION_ATTRIBUTE_NAME = "javax.servlet.error.exception";
    private static final String ATTEMPTED_URI_ATTRIBUTE_NAME = "javax.servlet.error.request_uri";

    @RequestMapping(value = "/error", produces = "text/html")
    public ModelAndView getErrorView(HttpServletRequest request, Model model) {

        Integer statusCode = (Integer) request.getAttribute(STATUS_CODE_ATTRIBUTE_NAME);
        Exception exception = (Exception) request.getAttribute(ERROR_EXCEPTION_ATTRIBUTE_NAME);
        String attemptedUri = (String) request.getAttribute(ATTEMPTED_URI_ATTRIBUTE_NAME);


        String errorText = "";
        if (exception != null) {
            errorText = exception.getMessage();
        }


        if (statusCode.equals(HttpStatus.FORBIDDEN.value())) {
            log.info("Status code 403 " + errorText, exception);
            return new ModelAndView("redirect:/login?relogin=true&" + request.getQueryString());
        }

        log.error("Error: status code: " + statusCode + " : " + errorText + " :: attempted uri: "
                + attemptedUri, exception);

        model.addAttribute("errorStatusCode", statusCode);
        model.addAttribute("errorText", errorText);

        return new ModelAndView("error/error", "model", model);

    }

    @RequestMapping(value = "/error/notfound")
    public ModelAndView getNotFoundView(String errorText, Model model) {
        model.addAttribute("errorText", errorText);
        return new ModelAndView("error/notFound");
    }

    @RequestMapping(value = "/error/permissionDenied")
    public ModelAndView getPermissionDeniedView(String errorText, Model model) {
        model.addAttribute("errorText", errorText);
        return new ModelAndView("error/permissionDenied");
    }

    public String getErrorPath() {
        return "/error";
    }

}
