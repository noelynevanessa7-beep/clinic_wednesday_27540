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

@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository appRepo;

    @Autowired
    private PatientRepository patRepo;

    @Autowired
    private DoctorRepository docRepo;

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
        if (appointment.getStatus() == null) {
            appointment.setStatus(AppointmentStatus.PENDING);
        }
        boolean booked = appRepo.existsByDoctorIdAndAppointmentDateAndStatusNot(doctorId, appointment.getAppointmentDate(), AppointmentStatus.CANCELLED);
        if (booked) {
            return "Doctor is already booked on that date";
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
}
