package io.github.shamrice.discapp.service;

import io.github.shamrice.discapp.data.model.Application;
import io.github.shamrice.discapp.data.model.Prologue;
import io.github.shamrice.discapp.data.repository.ApplicationRepository;
import io.github.shamrice.discapp.data.repository.PrologueRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ApplicationService {

    private static Logger logger = LoggerFactory.getLogger(ApplicationService.class);

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private PrologueRepository prologueRepository;

    public String getPrologueText(Long applicationId) {
        Prologue prologue = prologueRepository.findOneByApplicationId(applicationId);
        if (prologue != null) {
            return prologue.getText();
        }
        return "";
    }

    public List<Application> list() {
        return applicationRepository.findAll();
    }

    public Application get(Long id) {
        return applicationRepository.getOne(id);
    }
}
