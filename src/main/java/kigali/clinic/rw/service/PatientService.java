package kigali.clinic.rw.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kigali.clinic.rw.domain.Patient;
import kigali.clinic.rw.repository.DoctorRepository;
import kigali.clinic.rw.repository.PatientRepository;

@Service
public class PatientService {

    @Autowired
    private PatientRepository patRepo;

    @Autowired
    private DoctorRepository docRepo;

    // CREATE
    public String savePatient(Patient patient) {
        patRepo.save(patient);
        return "Patient is saved successfully";
    }

    // READ ALL
    public List<Patient> getAllPatients() {
        return patRepo.findAll();
    }

    // READ ONE
    public Patient getPatientById(UUID id) {
        return patRepo.findById(id).orElse(null);
    }
    public List<Patient> getPatientsByLastName(String lastName) {
        return patRepo.findByLastNameContainingIgnoreCaseOrderByFirstNameAsc(lastName);
    }

    public List<Patient> getFrequent(long min) {
        return patRepo.findFrequentPatients(min);
    }

    // UPDATE
    public String updatePatient(UUID id, Patient updated) {
        Patient patient = patRepo.findById(id).orElse(null);
        if (patient == null) {
            return "Patient not found with id: " + id;
        }
        patient.setFirstName(updated.getFirstName());
        patient.setLastName(updated.getLastName());
        patient.setDateOfBirth(updated.getDateOfBirth());
        patient.setSsNumber(updated.getSsNumber());
        patRepo.save(patient);
        return "Patient is updated successfully";
    }

    // DELETE
    @Transactional
    public String deletePatient(UUID id) {
        Patient patient = patRepo.findById(id).orElse(null);
        if (patient == null) {
            return "Patient not found with id: " + id;
        }
        if (!patient.getAppointments().isEmpty()) {
            return "Patient has appointments, so it cannot be deleted";
        }
        patRepo.delete(patient);
        return "Patient is deleted successfully";
    }

    public List<Patient> getPatientsOfDoctor(UUID doctorId) {
        if (!docRepo.existsById(doctorId)) {
            return null; 
        }
        return patRepo.findPatientsOfDoctor(doctorId);
    }

}