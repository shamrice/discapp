package io.github.shamrice.discapp.web.controller.application;

import io.github.shamrice.discapp.data.model.ApplicationFavicon;
import io.github.shamrice.discapp.service.application.data.ApplicationFaviconService;
import io.github.shamrice.discapp.service.configuration.ConfigurationService;
import io.github.shamrice.discapp.web.define.url.AppCustomFaviconUrl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletResponse;

@Controller
@Slf4j
public class CustomFaviconController {

    @Autowired
    private ApplicationFaviconService applicationFaviconService;

    @RequestMapping(value = AppCustomFaviconUrl.CONTROLLER_DIRECTORY_URL + "{appId}/{fileName}",
            method = RequestMethod.GET, produces = "image/x-icon")
    public ResponseEntity<byte[]> getCustomCss(@PathVariable(name = "appId") Long appId,
                                                 @PathVariable(name = "fileName") String fileName,
                                                 Model model,
                                                 HttpServletResponse response) {

        log.info("Getting custom favicon for appId: " + appId + " filename: " + fileName);
        response.setContentType("image/x-icon");
        response.setCharacterEncoding("UTF-8");

        try {

            ApplicationFavicon applicationFavicon = applicationFaviconService.getFaviconData(appId);
            if (applicationFavicon != null && fileName.equals(applicationFavicon.getFileName())) {
                log.info("Found custom favicon :: appId: " + appId + " : file name: " + applicationFavicon.getFileName());

                HttpHeaders headers = new HttpHeaders();
                headers.setCacheControl(CacheControl.noCache().getHeaderValue());

                return new ResponseEntity<>(applicationFavicon.getFaviconData(), headers, HttpStatus.OK);
            }

        } catch (Exception ex) {
            log.error("Error getting favicon for appId: " + appId + " :: " + ex.getMessage(), ex);
        }

        return ResponseEntity.badRequest().build();
    }

}
