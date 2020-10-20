package io.github.shamrice.discapp.service.application;

import io.github.shamrice.discapp.data.model.ApplicationReportCode;
import io.github.shamrice.discapp.data.repository.ApplicationReportCodeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ApplicationReportCodeService {

    @Autowired
    private ApplicationReportCodeRepository applicationReportCodeRepository;

    public boolean verifyAdminReportCode(Long appId, String email, String code) {
        ApplicationReportCode adminCode = applicationReportCodeRepository.findByApplicationIdAndEmailAndCode(
                appId, email, code)
                .orElse(null);

        if (adminCode != null) {
            log.info("Found valid admin code to update report settings: " + adminCode.toString());
            return true;
        } else {
            log.warn("Unable to find admin report code for appId: " + appId + " : email: " + email + " : code: " + code);
            return false;
        }
    }
}
