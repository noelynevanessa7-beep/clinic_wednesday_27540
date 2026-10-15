package kigali.clinic.rw.service;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kigali.clinic.rw.domain.Appointment;
import kigali.clinic.rw.domain.AppointmentStatus;
import kigali.clinic.rw.domain.Doctor;
import kigali.clinic.rw.domain.Patient;
import kigali.clinic.rw.repository.AppointmentRepository;
import kigali.clinic.rw.repository.DoctorRepository;
import kigali.clinic.rw.repository.PatientRepository;
import kigali.clinic.rw.repository.PrescriptionRepository;

@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository appRepo;

    @Autowired
    private PatientRepository patRepo;

    @Autowired
    private DoctorRepository docRepo;

    @Autowired
    private PrescriptionRepository presRepo;

    // CREATE (needed so the queries below have data to work on)
    public String saveAppointment(Appointment appointment, UUID patientId, UUID doctorId) {
        Patient patient = patRepo.findById(patientId).orElse(null);
        if (patient == null) {
            return "Patient not found with id: " + patientId;
        }
        Doctor doctor = docRepo.findById(doctorId).orElse(null);
        if (doctor == null) {
            return "Doctor not found with id: " + doctorId;
        }
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        
        boolean booked = appRepo.existsByDoctorIdAndAppointmentDateAndStatusNot(doctorId, appointment.getAppointmentDate(), AppointmentStatus.CANCELLED);
        if (booked) {
            return "Doctor is already booked on that date";
        }
        if (appointment.getAppointmentDate().toLocalDate().isBefore(LocalDate.now())) {
            return "Appointment date cannot be in the past";
        }
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appRepo.save(appointment);
        return "Appointment is saved successfully";
    }

    public List<Appointment> getAllAppointments() {
        return appRepo.findAll();
    }

    public List<Appointment> getPage(Pageable pageable) {
        return appRepo.findAll(pageable).getContent();
    }

    // QUERY 1 (derived)
    public List<Appointment> getPendingOfDoctor(UUID doctorId) {
        return appRepo.findByDoctorIdAndStatusOrderByAppointmentDateAsc(doctorId, AppointmentStatus.PENDING);
    }

    // QUERY 2 (JPQL)
    public List<Appointment> getOverduePending() {
        return appRepo.findOverdueByStatus(AppointmentStatus.PENDING, Date.valueOf(LocalDate.now()));
    }

    // QUERY 3 (aggregate)
    public List<Object[]> countAppointmentsPerDoctor() {
        return appRepo.countAppointmentsPerDoctor();
    }

    // QUERY 4 (join across 2 relationships)
    public List<Appointment> getBySpecialization(String name) {
        return appRepo.findByDoctorSpecialization(name);
    }

    // QUERY 5 (pagination) - page number starts at 1 for the user, Spring counts from 0
    public Page<Appointment> getPageOfDoctor(UUID doctorId, int page, int size) {
        int pageIndex = Math.max(page - 1, 0);
        int pageSize = Math.max(size, 1);
        return appRepo.findByDoctorId(doctorId,
                PageRequest.of(pageIndex, pageSize, Sort.by("appointmentDate").ascending()));
    }

    public List<Appointment> getByStatus(AppointmentStatus status) {
        return appRepo.findByStatusOrderByAppointmentDateAsc(status);
    }

    public List<Appointment> getBetween(LocalDate startDate, LocalDate endDate) {
        return appRepo.findByAppointmentDateBetweenOrderByAppointmentDateAsc(Date.valueOf(startDate), Date.valueOf(endDate));
    }

    public List<Object[]> countByStatus() {
        return appRepo.countByStatus();
    }

    @Transactional 
    public String cancelDay(UUID doctorId, String date) {
        int n = appRepo.cancelDayOfDoctor(doctorId, Date.valueOf(date), AppointmentStatus.CANCELLED, AppointmentStatus.COMPLETED);
        return n + " appointments cancelled";
    }

    @Transactional
    public String deleteCancelledBefore( String date) {
        int n = appRepo.deleteByStatusBefore(AppointmentStatus.CANCELLED, Date.valueOf(date));
        return n + " appointments deleted";
    }

    public String changeStatus(UUID appointmentId, AppointmentStatus newStatus) {
        Appointment appointment = appRepo.findById(appointmentId).orElse(null);
        if (appointment == null) {
            return "The appointment with that id does not exist";
        }
        AppointmentStatus current = appointment.getStatus();
        boolean allowed = false;
        if (current == AppointmentStatus.SCHEDULED) {
            if (newStatus == AppointmentStatus.CONFIRMED || newStatus == AppointmentStatus.CANCELLED) {
                allowed = true;
            }
        
        } else if (current == AppointmentStatus.CONFIRMED){
            if (newStatus == AppointmentStatus.COMPLETED || newStatus == AppointmentStatus.CANCELLED) {
                allowed = true;
            }
        
        }
        if (!allowed) {
            return "Cannot change status from " + current + " to " + newStatus;
        }
        
        appointment.setStatus(newStatus);
        appRepo.save(appointment);
        return "Appointment status updated successfully";
    }

    public String reschedule(UUID appointmentId, String newDate) {
        Appointment appointment = appRepo.findById(appointmentId).orElse(null);
        if (appointment == null) {
            return "The appointment with that id does not exist";
        }
        if (appointment.getStatus() == AppointmentStatus.COMPLETED || appointment.getStatus() == AppointmentStatus.CANCELLED) {
            return "A " + appointment.getStatus() + " appointment cannot be rescheduled";
        }
        Date date = Date.valueOf(newDate);
        if (date.toLocalDate().isBefore(LocalDate.now())) {
            return "Appointment date cannot be in the past";
        }
        boolean booked = appRepo.existsByDoctorIdAndAppointmentDateAndStatusNot(appointment.getDoctor().getId(), date, AppointmentStatus.CANCELLED);
        if (booked) {
            return "Doctor is already booked on that date";
        }
        appointment.setAppointmentDate(date);
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appRepo.save(appointment);
        return "Appointment rescheduled successfully";
    }

    public String deleteAppointment(UUID appointmentId) {
        if(!appRepo.existsById(appointmentId)) {
            return "The appointment with that id does not exist";
        }
        if (presRepo.existsByAppointmentId(appointmentId)) {
            return "The appointment has a prescriptions, delete them first";
        }
        appRepo.deleteById(appointmentId);
        return "Appointment deleted successfully";
    }

    public List<Appointment> getAgenda(UUID doctorId, String date) {
        if (!docRepo.existsById(doctorId)) {
            return null;
        }
        return appRepo.findByDoctorIdAndAppointmentDateAndStatusNot(doctorId, Date.valueOf(date), AppointmentStatus.CANCELLED);
    }

    public List<Appointment> getUpcoming(String ssNumber) {
        if (!patRepo.existsBySsNumber(ssNumber)) {
            return null;

        } 
        List<AppointmentStatus> statuses = List.of(AppointmentStatus.SCHEDULED, AppointmentStatus.CONFIRMED);
        return appRepo.findByPatientSsNumberAndAppointmentDateGreaterThanEqualAndStatusInOrderByAppointmentDateAsc(ssNumber,Date.valueOf(LocalDate.now()), statuses);
    }
}