package kigali.clinic.rw.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kigali.clinic.rw.domain.Doctor;
import kigali.clinic.rw.domain.Specialization;
import kigali.clinic.rw.repository.DoctorRepository;
import kigali.clinic.rw.repository.SpecializationRepository;

@Service
public class SpecializationService {

    @Autowired
    private SpecializationRepository specRepo;

    @Autowired
    private DoctorRepository docRepo;

    // CREATE
    public String saveSpecialization(Specialization specialization) {
        if (specialization.getName() == null || specialization.getName().isBlank()) {
            return "Specialization name is required";
        }
        if (specRepo.findByName(specialization.getName()).isPresent()) {
            return "Specialization name already exists";
        }
        // only the name is taken from the request (doctors are linked with the endpoints below)
        Specialization spec = new Specialization();
        spec.setName(specialization.getName());
        specRepo.save(spec);
        return "Specialization is saved successfully";
    }

    // READ ALL
    public List<Specialization> getAllSpecializations() {
        return specRepo.findAll();
    }

    // READ ONE
    public Specialization getSpecializationById(UUID id) {
        return specRepo.findById(id).orElse(null);
    }

    // UPDATE (only the name changes; the doctors stay linked)
    @Transactional
    public String updateSpecialization(UUID id, Specialization updated) {
        Specialization spec = specRepo.findById(id).orElse(null);
        if (spec == null) {
            return "Specialization not found with id: " + id;
        }
        if (updated.getName() == null || updated.getName().isBlank()) {
            return "Specialization name is required";
        }
        if (!updated.getName().equals(spec.getName())
                && specRepo.findByName(updated.getName()).isPresent()) {
            return "Specialization name already exists";
        }
        spec.setName(updated.getName());
        specRepo.save(spec);
        return "Specialization is updated successfully";
    }

    // DELETE (the links to doctors in doctor_specialization are removed too)
    @Transactional
    public String deleteSpecialization(UUID id) {
        Specialization spec = specRepo.findById(id).orElse(null);
        if (spec == null) {
            return "Specialization not found with id: " + id;
        }
        spec.getDoctors().clear();
        specRepo.delete(spec);
        return "Specialization is deleted successfully";
    }

    // MANY-TO-MANY: link a doctor to a specialization
    @Transactional
    public String addDoctor(UUID specializationId, UUID doctorId) {
        Specialization spec = specRepo.findById(specializationId).orElse(null);
        if (spec == null) {
            return "Specialization not found with id: " + specializationId;
        }
        Doctor doctor = docRepo.findById(doctorId).orElse(null);
        if (doctor == null) {
            return "Doctor not found with id: " + doctorId;
        }
        if (spec.getDoctors().contains(doctor)) {
            return "Doctor already has this specialization";
        }
        spec.getDoctors().add(doctor);            // owner side: this writes the join table row
        doctor.getSpecializations().add(spec);    // inverse side: keep both sides in sync
        specRepo.save(spec);
        return "Doctor is added to specialization successfully";
    }

    // MANY-TO-MANY: unlink a doctor from a specialization
    @Transactional
    public String removeDoctor(UUID specializationId, UUID doctorId) {
        Specialization spec = specRepo.findById(specializationId).orElse(null);
        if (spec == null) {
            return "Specialization not found with id: " + specializationId;
        }
        Doctor doctor = docRepo.findById(doctorId).orElse(null);
        if (doctor == null) {
            return "Doctor not found with id: " + doctorId;
        }
        if (!spec.getDoctors().contains(doctor)) {
            return "Doctor does not have this specialization";
        }
        spec.getDoctors().remove(doctor);
        doctor.getSpecializations().remove(spec);
        specRepo.save(spec);
        return "Doctor is removed from specialization successfully";
    }

    public List<Specialization> getUnused() {
        return specRepo.findUnusedSpecializations();
    }
}
