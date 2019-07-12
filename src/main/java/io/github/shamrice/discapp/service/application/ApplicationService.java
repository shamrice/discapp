package io.github.shamrice.discapp.service.application;

import io.github.shamrice.discapp.data.model.Application;
import io.github.shamrice.discapp.data.model.Epilogue;
import io.github.shamrice.discapp.data.model.Prologue;
import io.github.shamrice.discapp.data.repository.ApplicationRepository;
import io.github.shamrice.discapp.data.repository.EpilogueRepository;
import io.github.shamrice.discapp.data.repository.PrologueRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class ApplicationService {

    private static Logger logger = LoggerFactory.getLogger(ApplicationService.class);

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private PrologueRepository prologueRepository;

    @Autowired
    private EpilogueRepository epilogueRepository;

    public Application saveApplication(Application application, Prologue prologue, Epilogue epilogue) {

        Application savedApplication;

        if (application != null) {
            if (application.getCreateDt() == null) {
                application.setCreateDt(new Date());
            }
            application.setModDt(new Date());
            savedApplication = applicationRepository.save(application);

            logger.info("Saved application id: " + savedApplication.getId() + " : owner id: "
                    + savedApplication.getOwnerId() + " : name: " + savedApplication.getName());

        } else {
            logger.error("Cannot save null application. Returning null.");
            return null;
        }

        if (prologue != null) {
            if (prologue.getCreateDt() == null) {
                prologue.setCreateDt(new Date());
            }
            prologue.setModDt(new Date());
            prologue.setApplicationId(savedApplication.getId());
            Prologue savedPrologue = prologueRepository.save(prologue);
            logger.info("Saved prologue id: " + savedPrologue.getId() + " for applicationId: " + savedPrologue.getApplicationId()
                    + " : text:" + savedPrologue.getText());
        }

        if (epilogue != null) {
            if (epilogue.getCreateDt() == null) {
                epilogue.setCreateDt(new Date());
            }
            epilogue.setModDt(new Date());
            epilogue.setApplicationId(savedApplication.getId());
            Epilogue savedEpilogue = epilogueRepository.save(epilogue);
            logger.info("Saved epilogue id: " + savedEpilogue.getId() + " for applicationId: " + savedEpilogue.getApplicationId()
                    + " : text:" + savedEpilogue.getText());
        }

        return savedApplication;
    }

    public String getPrologueText(Long applicationId) {
        Prologue prologue = prologueRepository.findOneByApplicationId(applicationId);
        if (prologue != null) {
            return prologue.getText();
        }
        logger.info("No prologue set yet for account id " + applicationId + ". Returning empty string.");
        return "";
    }

    public String getEpilogueText(Long applicationId) {
        Epilogue epilogue = epilogueRepository.findOneByApplicationId(applicationId);
        if (epilogue != null) {
            return epilogue.getText();
        }
        logger.info("No epilogue set yet for account id " + applicationId + ". Returning empty string.");
        return "";
    }

    public List<Application> list() {
        return applicationRepository.findAll();
    }

    public Application get(Long id) {
        return applicationRepository.getOne(id);
    }
}
