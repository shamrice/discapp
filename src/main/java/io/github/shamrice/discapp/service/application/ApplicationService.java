package io.github.shamrice.discapp.service.application;

import io.github.shamrice.discapp.data.model.Application;
import io.github.shamrice.discapp.data.model.Epilogue;
import io.github.shamrice.discapp.data.model.Owner;
import io.github.shamrice.discapp.data.model.Prologue;
import io.github.shamrice.discapp.data.repository.ApplicationRepository;
import io.github.shamrice.discapp.data.repository.EpilogueRepository;
import io.github.shamrice.discapp.data.repository.OwnerRepository;
import io.github.shamrice.discapp.data.repository.PrologueRepository;
import io.github.shamrice.discapp.service.account.AccountService;
import io.github.shamrice.discapp.service.account.DiscAppUserDetailsService;
import io.github.shamrice.discapp.service.application.cache.ApplicationCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class ApplicationService {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationService.class);

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private PrologueRepository prologueRepository;

    @Autowired
    private EpilogueRepository epilogueRepository;

    @Autowired
    private DiscAppUserDetailsService discAppUserDetailsService;

    @Autowired
    private AccountService accountService;


    private ApplicationCache<Prologue> prologueCache = new ApplicationCache<>();
    private ApplicationCache<Epilogue> epilogueCache = new ApplicationCache<>();


    public Application save(Application application) {
        return applicationRepository.save(application);
    }

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

            prologueCache.updateCache(prologue.getApplicationId(), prologue);
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

            epilogueCache.updateCache(epilogue.getApplicationId(), epilogue);
        }

        return savedApplication;
    }

    public String getPrologueText(Long applicationId) {

        Prologue cachedPrologue = prologueCache.getFromCache(applicationId);
        if (cachedPrologue != null && cachedPrologue.getText() != null) {
            logger.info("Found prologue text in cache for application id: " + applicationId);
            return cachedPrologue.getText();
        }

        Prologue prologue = prologueRepository.findOneByApplicationId(applicationId);
        if (prologue != null) {
            logger.info("Found prologue text in database for application id: " + applicationId);
            prologueCache.updateCache(applicationId, prologue);
            return prologue.getText();
        }
        logger.info("No prologue set yet for account id " + applicationId + ". Returning empty string.");
        return "";
    }

    public String getEpilogueText(Long applicationId) {

        Epilogue cachedEpilogue = epilogueCache.getFromCache(applicationId);
        if (cachedEpilogue != null && cachedEpilogue.getText() != null) {
            logger.info("Found epilogue text in cache for application id: " + applicationId);
            return cachedEpilogue.getText();
        }

        Epilogue epilogue = epilogueRepository.findOneByApplicationId(applicationId);
        if (epilogue != null) {
            logger.info("Found epilogue text in database for application id: " + applicationId);
            epilogueCache.updateCache(applicationId, epilogue);
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

    public boolean isOwnerOfApp(long appId, String username) {
        Long ownerIdForUsername = discAppUserDetailsService.getOwnerIdForUsername(username);

        if (ownerIdForUsername != null && ownerIdForUsername > 0) {
            Owner owner = accountService.getOwnerById(ownerIdForUsername);
            if (owner != null) {
                List<Application> ownedApps = applicationRepository.findByOwnerId(owner.getId());
                for (Application application : ownedApps) {
                    if (application.getId() != null && application.getId() == appId) {
                        logger.info("User: " + username + " is owner of appId: " + appId + " :: returning true.");
                        return true;
                    }
                }
            }
        } else {
            logger.info("User: " + username + " is not an owner of any applications. Returning false.");
        }

        logger.info("User: " + username + " does not own application id: " + appId + " :: returning false.");
        return false;

    }

    public List<Application> getByOwnerId(long ownerId) {
        return applicationRepository.findByOwnerId(ownerId);
    }

    public Prologue getPrologue(long applicationId, boolean useCache) {

        if (useCache) {
            Prologue cachedPrologue = prologueCache.getFromCache(applicationId);
            if (cachedPrologue != null && cachedPrologue.getText() != null) {
                logger.info("Found prologue in cache for application id: " + applicationId);
                return cachedPrologue;
            }
        }

        Prologue prologue = prologueRepository.findOneByApplicationId(applicationId);
        if (prologue != null) {
            logger.info("Found prologue  in database for application id: " + applicationId);
            prologueCache.updateCache(applicationId, prologue);
            return prologue;
        }
        logger.info("No prologue set yet for account id " + applicationId + ". Returning null.");
        return null;
    }

    public Epilogue getEpilogue(long applicationId, boolean useCache) {

        if (useCache) {
            Epilogue cachedEpilogue = epilogueCache.getFromCache(applicationId);
            if (cachedEpilogue != null && cachedEpilogue.getText() != null) {
                logger.info("Found epilogue in cache for application id: " + applicationId);
                return cachedEpilogue;
            }
        }

        Epilogue epilogue = epilogueRepository.findOneByApplicationId(applicationId);
        if (epilogue != null) {
            logger.info("Found epilogue  in database for application id: " + applicationId);
            epilogueCache.updateCache(applicationId, epilogue);
            return epilogue;
        }
        logger.info("No epilogue set yet for account id " + applicationId + ". Returning null.");
        return null;
    }

    public Prologue savePrologue(Prologue prologue) {

        Prologue savedPrologue = null;

        if (prologue != null && prologue.getApplicationId() != null) {

            if (prologue.getCreateDt() == null) {
                prologue.setCreateDt(new Date());
            }
            if (prologue.getModDt() == null) {
                prologue.setModDt(new Date());
            }

            savedPrologue = prologueRepository.save(prologue);
            if (savedPrologue != null) {
                logger.info("Saved epilogue for appId: " + prologue.getApplicationId());
                prologueCache.updateCache(prologue.getApplicationId(), prologue);
            }
        }
        return savedPrologue;
    }

    public Epilogue saveEpilogue(Epilogue epilogue) {

        Epilogue savedEpilogue = null;

        if (epilogue != null && epilogue.getApplicationId() != null) {

            if (epilogue.getCreateDt() == null) {
                epilogue.setCreateDt(new Date());
            }
            if (epilogue.getModDt() == null) {
                epilogue.setModDt(new Date());
            }

            savedEpilogue = epilogueRepository.save(epilogue);
            if (savedEpilogue != null) {
                logger.info("Saved epilogue for appId: " + epilogue.getApplicationId());
                epilogueCache.updateCache(epilogue.getApplicationId(), epilogue);
            }
        }
        return savedEpilogue;
    }

}
