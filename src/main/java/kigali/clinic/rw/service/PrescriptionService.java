package kigali.clinic.rw.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import kigali.clinic.rw.domain.Appointment;
import kigali.clinic.rw.domain.AppointmentStatus;
import kigali.clinic.rw.domain.Prescription;
import kigali.clinic.rw.repository.AppointmentRepository;
import kigali.clinic.rw.repository.PrescriptionRepository;

@Service 
public class PrescriptionService {

    @Autowired 
    private PrescriptionRepository presRepo;

    @Autowired
    private AppointmentRepository appRepo;

    public String savePrescription(Prescription prescription, UUID appointmentId) {
        Appointment appointment = appRepo.findById(appointmentId).orElse(null);
        if (appointment == null) {
            return "The appointment with that id does not exist";
        }
        if (appointment.getStatus() != AppointmentStatus.COMPLETED) {
            return "Prescriptions are only for COMPLETED appointments";
        }
        prescription.setIssuedDate(LocalDate.now());
        prescription.setAppointment(appointment);
        presRepo.save(prescription);
        return "Prescription saved successfully";
    }

    public List<Prescription> getByAppointment(UUID appointmentId) {
        Appointment appointment = appRepo.findById(appointmentId).orElse(null);
        if (appointment == null) {
            return null;
        }
        return presRepo.findByAppointment(appointment);
    }

    public List<Prescription> getHistoryOfPatient(String ssNumber) {
        return presRepo.findHistoryOfPatient(ssNumber);
    }

    public String updatePrescription(UUID Id, Prescription updated) {
        Prescription prescription = presRepo.findById(Id).orElse(null);
        if (prescription == null) {
            return "The prescription with that id does not exist";
        }
        prescription.setMedicineName(updated.getMedicineName());
        prescription.setDosage(updated.getDosage());
        prescription.setDurationDays(updated.getDurationDays());
        presRepo.save(prescription);
        return "Prescription updated successfully";
    }

    public String deletePrescription(UUID Id) {
        Prescription prescription = presRepo.findById(Id).orElse(null);
        if (prescription == null) {
            return "The prescription with that id does not exist";
        }
        presRepo.delete(prescription);
        return "Prescription deleted successfully";
    }
    
}
