package com.mat.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.mat.entity.Equipment;
import com.mat.repository.EquipmentRepository;

/**
 * EquipmentService
 *
 * Contains the business logic for managing Equipment records.
 * Acts as the middle layer between EquipmentController and EquipmentRepository.
 */
@Service
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;

    public EquipmentService(EquipmentRepository equipmentRepository) {
        this.equipmentRepository = equipmentRepository;
    }

    /**
     * Saves an Equipment record to the database.
     *
     * @param equipment the Equipment object to save
     * @return the saved Equipment (with generated id)
     */
    public Equipment saveEquipment(Equipment equipment) {
        return equipmentRepository.save(equipment);
    }

    /**
     * Retrieves all Equipment records from the database.
     *
     * @return a list of all Equipment objects
     */
    public List<Equipment> getAllEquipment() {
        return equipmentRepository.findAll();
    }
}
