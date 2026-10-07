package kigali.clinic.rw.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kigali.clinic.rw.domain.Office;
import kigali.clinic.rw.repository.OfficeRepository;

@Service
public class OfficeService {

    @Autowired
    private OfficeRepository offRepo;

    // CREATE
    public String saveOffice(Office office) {
        if (offRepo.findByOfficeNumber(office.getOfficeNumber()).isPresent()) {
            return "Office number already exists";
        }
        offRepo.save(office);
        return "Office is saved successfully";
    }

    // READ ALL
    public List<Office> getAllOffices() {
        return offRepo.findAll();
    }

    // READ ONE
    public Office getOfficeById(UUID id) {
        return offRepo.findById(id).orElse(null);
    }

    // UPDATE
    public String updateOffice(UUID id, Office updated) {
        Office office = offRepo.findById(id).orElse(null);
        if (office == null) {
            return "Office not found with id: " + id;
        }
        if (office.getOfficeNumber() != updated.getOfficeNumber()
                && offRepo.findByOfficeNumber(updated.getOfficeNumber()).isPresent()) {
            return "Office number already exists";
        }
        office.setName(updated.getName());
        office.setOfficeNumber(updated.getOfficeNumber());
        offRepo.save(office);
        return "Office is updated successfully";
    }

    // DELETE
    @Transactional
    public String deleteOffice(UUID id) {
        Office office = offRepo.findById(id).orElse(null);
        if (office == null) {
            return "Office not found with id: " + id;
        }
        if (office.getDoctor() != null) {
            return "Office is assigned to a doctor, so it cannot be deleted";
        }
        offRepo.delete(office);
        return "Office is deleted successfully";
    }
}