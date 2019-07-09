package io.github.shamrice.discapp.service;

import io.github.shamrice.discapp.data.model.Owner;
import io.github.shamrice.discapp.data.repository.OwnerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountService {

    private static Logger logger = LoggerFactory.getLogger(AccountService.class);

    @Autowired
    private OwnerRepository ownerRepository;

    public List<Owner> listOwners() {
        return ownerRepository.findAll();
    }
}
