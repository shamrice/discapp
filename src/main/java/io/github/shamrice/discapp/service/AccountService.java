package io.github.shamrice.discapp.service;

import io.github.shamrice.discapp.data.model.Owner;
import io.github.shamrice.discapp.data.repository.OwnerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountService {

    @Autowired
    private OwnerRepository ownerRepository;

    public List<Owner> listOwners() {
        return ownerRepository.findAll();
    }
}
