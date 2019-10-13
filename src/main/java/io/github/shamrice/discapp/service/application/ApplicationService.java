package io.github.shamrice.discapp.service.application;

import io.github.shamrice.discapp.data.model.*;
import io.github.shamrice.discapp.data.repository.*;
import io.github.shamrice.discapp.service.account.AccountService;
import io.github.shamrice.discapp.service.account.DiscAppUserDetailsService;
import io.github.shamrice.discapp.service.application.cache.ApplicationCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class ApplicationService {

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private ApplicationPermissionRepository applicationPermissionRepository;

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

    public ApplicationService(@Value("${discapp.cache.duration}") Long cacheDuration) {
        prologueCache.setMaxCacheAgeMilliseconds(cacheDuration);
        epilogueCache.setMaxCacheAgeMilliseconds(cacheDuration);
    }

    public Application save(Application application) {
        return applicationRepository.save(application);
    }

    public Application saveApplication(Application application, Prologue prologue, Epilogue epilogue) {

        Application savedApplication;

        if (application != null) {
            if (application.getCreateDt() == null) {
                application.setCreateDt(new Date());
                application.setDeleted(false);
            }
            application.setModDt(new Date());
            savedApplication = applicationRepository.save(application);

            log.info("Saved application id: " + savedApplication.getId() + " : owner id: "
                    + savedApplication.getOwnerId() + " : name: " + savedApplication.getName());

        } else {
            log.error("Cannot save null application. Returning null.");
            return null;
        }

        if (prologue != null) {
            if (prologue.getCreateDt() == null) {
                prologue.setCreateDt(new Date());
            }
            prologue.setModDt(new Date());
            prologue.setApplicationId(savedApplication.getId());
            Prologue savedPrologue = prologueRepository.save(prologue);
            log.info("Saved prologue id: " + savedPrologue.getId() + " for applicationId: " + savedPrologue.getApplicationId()
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
            log.info("Saved epilogue id: " + savedEpilogue.getId() + " for applicationId: " + savedEpilogue.getApplicationId()
                    + " : text:" + savedEpilogue.getText());

            epilogueCache.updateCache(epilogue.getApplicationId(), epilogue);
        }

        return savedApplication;
    }

    public String getPrologueText(Long applicationId) {

        Prologue cachedPrologue = prologueCache.getFromCache(applicationId);
        if (cachedPrologue != null && cachedPrologue.getText() != null) {
            log.info("Found prologue text in cache for application id: " + applicationId);
            return cachedPrologue.getText();
        }

        Prologue prologue = prologueRepository.findOneByApplicationId(applicationId);
        if (prologue != null) {
            log.info("Found prologue text in database for application id: " + applicationId);
            prologueCache.updateCache(applicationId, prologue);
            return prologue.getText();
        }
        log.info("No prologue set yet for account id " + applicationId + ". Returning empty string.");
        return "";
    }

    public String getEpilogueText(Long applicationId) {

        Epilogue cachedEpilogue = epilogueCache.getFromCache(applicationId);
        if (cachedEpilogue != null && cachedEpilogue.getText() != null) {
            log.info("Found epilogue text in cache for application id: " + applicationId);
            return cachedEpilogue.getText();
        }

        Epilogue epilogue = epilogueRepository.findOneByApplicationId(applicationId);
        if (epilogue != null) {
            log.info("Found epilogue text in database for application id: " + applicationId);
            epilogueCache.updateCache(applicationId, epilogue);
            return epilogue.getText();
        }
        log.info("No epilogue set yet for account id " + applicationId + ". Returning empty string.");
        return "";
    }

    public List<Application> list() {
        return applicationRepository.findAll();
    }

    public Application get(Long id) {
        Optional<Application> app = applicationRepository.findById(id);
        if (app.isPresent()) {
            //check if app is enabled.
            if (!app.get().getEnabled() || app.get().getDeleted()) {
                log.warn("AppId: " + id + " is either not enabled or is deleted. : Enabled="
                        + app.get().getEnabled() + " : Deleted=" + app.get().getDeleted() + " :: Returning null.");
                return null;
            }
            //check if owner is enabled.
            Owner appOwner = accountService.getOwnerById(app.get().getOwnerId());
            if (appOwner != null && appOwner.getEnabled()) {
                return app.get();
            } else {
                log.warn("Owner of appId: " + id + " is either null or currently disabled. Returning null app.");
            }
        }
        log.error("No application found with appId: " + id + ". Returning null.");
        return null;
    }

    public boolean isOwnerOfApp(long appId, String email) {
        Long ownerIdForEmail = discAppUserDetailsService.getOwnerIdForEmail(email);

        if (ownerIdForEmail != null && ownerIdForEmail > 0) {
            Owner owner = accountService.getOwnerById(ownerIdForEmail);
            if (owner != null) {
                List<Application> ownedApps = applicationRepository.findByOwnerIdAndDeleted(owner.getId(), false);
                for (Application application : ownedApps) {
                    if (application.getId() != null && application.getId() == appId) {

                        DiscAppUser user = discAppUserDetailsService.getByEmail(email);
                        if (user != null && user.getIsUserAccount()) {
                            log.info("User: " + email + " is owner of appId: " + appId + " :: and is regular user :: returning true.");
                            return true;
                        } else if (user != null && !user.getIsUserAccount() && user.getUsername().equals(String.valueOf(appId))) {
                            log.info("User: " + email + " is owner of appId: " + appId + " :: and is matching system account to appId :: returning true.");
                            return true;
                        }
                    }
                }
            }
        } else {
            log.info("User: " + email     + " is not an owner of any applications. Returning false.");
        }

        log.info("User: " + email + " does not own application id: " + appId + " :: returning false.");
        return false;

    }

    public List<Application> getByOwnerId(long ownerId) {
        return applicationRepository.findByOwnerIdAndDeleted(ownerId, false);
    }

    public Prologue getPrologue(long applicationId, boolean useCache) {

        if (useCache) {
            Prologue cachedPrologue = prologueCache.getFromCache(applicationId);
            if (cachedPrologue != null && cachedPrologue.getText() != null) {
                log.info("Found prologue in cache for application id: " + applicationId);
                return cachedPrologue;
            }
        }

        Prologue prologue = prologueRepository.findOneByApplicationId(applicationId);
        if (prologue != null) {
            log.info("Found prologue  in database for application id: " + applicationId);
            prologueCache.updateCache(applicationId, prologue);
            return prologue;
        }
        log.info("No prologue set yet for account id " + applicationId + ". Returning null.");
        return null;
    }

    public Epilogue getEpilogue(long applicationId, boolean useCache) {

        if (useCache) {
            Epilogue cachedEpilogue = epilogueCache.getFromCache(applicationId);
            if (cachedEpilogue != null && cachedEpilogue.getText() != null) {
                log.info("Found epilogue in cache for application id: " + applicationId);
                return cachedEpilogue;
            }
        }

        Epilogue epilogue = epilogueRepository.findOneByApplicationId(applicationId);
        if (epilogue != null) {
            log.info("Found epilogue  in database for application id: " + applicationId);
            epilogueCache.updateCache(applicationId, epilogue);
            return epilogue;
        }
        log.info("No epilogue set yet for account id " + applicationId + ". Returning null.");
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
                log.info("Saved epilogue for appId: " + prologue.getApplicationId());
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
                log.info("Saved epilogue for appId: " + epilogue.getApplicationId());
                epilogueCache.updateCache(epilogue.getApplicationId(), epilogue);
            }
        }
        return savedEpilogue;
    }

    public ApplicationPermission getApplicationPermissions(long appId) {
        return applicationPermissionRepository.findOneByApplicationId(appId).orElse(null);
    }

    public boolean saveApplicationPermissions(ApplicationPermission applicationPermission) {
        if (applicationPermission == null) {
            return false;
        }

        return applicationPermissionRepository.save(applicationPermission) != null;
    }

    public ApplicationPermission getDefaultNewApplicationPermissions(long appId) {
        ApplicationPermission appPermission = new ApplicationPermission();
        appPermission.setCreateDt(new Date());
        appPermission.setModDt(new Date());
        appPermission.setApplicationId(appId);
        //set defaults
        appPermission.setDisplayIpAddress(true);
        appPermission.setBlockBadWords(false);
        appPermission.setBlockSearchEngines(false);
        //todo : these permission strings should live somewhere.
        appPermission.setAllowHtmlPermissions("subject");
        appPermission.setUnregisteredUserPermissions("rfp");
        appPermission.setRegisteredUserPermissions("rfp");

        return appPermission;
    }

}
