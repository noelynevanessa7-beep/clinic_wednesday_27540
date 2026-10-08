package kigali.clinic.rw.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kigali.clinic.rw.domain.Doctor;
import kigali.clinic.rw.domain.Office;
import kigali.clinic.rw.domain.Specialization;
import kigali.clinic.rw.repository.DoctorRepository;
import kigali.clinic.rw.repository.OfficeRepository;

@Service
public class DoctorService {

    @Autowired
    private DoctorRepository docRepo;

    @Autowired
    private OfficeRepository offRepo;

    // CREATE (officeId is optional)
    public String saveDoctor(Doctor doctor, UUID officeId) {
        if (officeId != null) {
            Office office = offRepo.findById(officeId).orElse(null);
            if (office == null) {
                return "Office not found with id: " + officeId;
            }
            if (office.getDoctor() != null) {
                return "Office is already assigned to another doctor";
            }
            doctor.setOffice(office);
        }
        docRepo.save(doctor);
        return "Doctor is saved successfully";
    }

    // READ ALL
    public List<Doctor> getAllDoctors() {
        return docRepo.findAll();
    }

    // READ ONE
    public Doctor getDoctorById(UUID id) {
        return docRepo.findById(id).orElse(null);
    }

    // UPDATE (officeId is optional, send it only to change the office)
    @Transactional
    public String updateDoctor(UUID id, Doctor updated, UUID officeId) {
        Doctor doctor = docRepo.findById(id).orElse(null);
        if (doctor == null) {
            return "Doctor not found with id: " + id;
        }
        if (officeId != null) {
            Office office = offRepo.findById(officeId).orElse(null);
            if (office == null) {
                return "Office not found with id: " + officeId;
            }
            if (office.getDoctor() != null && !office.getDoctor().getId().equals(id)) {
                return "Office is already assigned to another doctor";
            }
            doctor.setOffice(office);
        }
        doctor.setFirstName(updated.getFirstName());
        doctor.setLastName(updated.getLastName());
        doctor.setDateOfBirth(updated.getDateOfBirth());
        docRepo.save(doctor);
        return "Doctor is updated successfully";
    }

    // READ the specializations of one doctor (null if the doctor does not exist)
    @Transactional
    public List<Specialization> getSpecializationsOfDoctor(UUID id) {
        Doctor doctor = docRepo.findById(id).orElse(null);
        if (doctor == null) {
            return null;
        }
        return new ArrayList<>(doctor.getSpecializations());
    }

    // DELETE
    @Transactional
    public String deleteDoctor(UUID id) {
        Doctor doctor = docRepo.findById(id).orElse(null);
        if (doctor == null) {
            return "Doctor not found with id: " + id;
        }
        if (!doctor.getAppointments().isEmpty()) {
            return "Doctor has appointments, so it cannot be deleted";
        }
        // Doctor is the inverse side of the Many-to-Many: unlink it from every specialization first,
        // otherwise the rows in doctor_specialization would block the delete
        for (Specialization s : new ArrayList<>(doctor.getSpecializations())) {
            s.getDoctors().remove(doctor);
        }
        docRepo.delete(doctor);
        return "Doctor is deleted successfully";
    }

    public List<Doctor> getBySpecialization(String name) {
        return docRepo.findBySpecializationName(name);
    }

    public List<Doctor> getWithoutOffice() {
        return docRepo.findDoctorsWithoutOffice();
    }
}