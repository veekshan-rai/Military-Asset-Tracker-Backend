package com.mat.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.mat.entity.Base;
import com.mat.repository.BaseRepository;

/**
 * BaseService
 *
 * Contains the business logic for managing Base records.
 * Acts as the middle layer between BaseController and BaseRepository.
 */
@Service
public class BaseService {

    private final BaseRepository baseRepository;

    public BaseService(BaseRepository baseRepository) {
        this.baseRepository = baseRepository;
    }

    /**
     * Saves a Base record to the database.
     *
     * @param base the Base object to save
     * @return the saved Base (with generated id)
     */
    public Base saveBase(Base base) {
        return baseRepository.save(base);
    }

    /**
     * Retrieves all Base records from the database.
     *
     * @return a list of all Base objects
     */
    public List<Base> getAllBases() {
        return baseRepository.findAll();
    }
}
