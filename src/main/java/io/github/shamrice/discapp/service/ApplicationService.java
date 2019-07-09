package io.github.shamrice.discapp.service;

import io.github.shamrice.discapp.data.model.Application;
import io.github.shamrice.discapp.data.repository.ApplicationRepository;
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

    public List<Application> list() {
        return applicationRepository.findAll();
    }

    public Application get(Long id) {
        return applicationRepository.getOne(id);
    }
}
