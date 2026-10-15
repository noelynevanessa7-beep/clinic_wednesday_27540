package kigali.clinic.rw.service;

import java.sql.Date;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import kigali.clinic.rw.domain.AppointmentStatus;
import kigali.clinic.rw.repository.AppointmentRepository;
import kigali.clinic.rw.repository.DoctorRepository;
import kigali.clinic.rw.repository.OfficeRepository;
import kigali.clinic.rw.repository.PatientRepository;
import kigali.clinic.rw.repository.PrescriptionRepository;

@Service 
public class DashboardService {

    @Autowired 
    private PatientRepository patRepo;

    @Autowired 
    private DoctorRepository docRepo;

    @Autowired 
    private OfficeRepository offRepo;

    @Autowired 
    private AppointmentRepository appRepo;

    @Autowired 
    private PrescriptionRepository presRepo;

    public Map<String, Long> getSummary() {
        Map<String, Long> summary = new  LinkedHashMap<>();
        summary.put("patients", patRepo.count());
        summary.put("doctors", docRepo.count());
        summary.put("freeOffices", (long) offRepo.findOfficesWithoutDoctor().size());
        summary.put("appointmentsToday", appRepo.countByAppointmentDate(Date.valueOf(LocalDate.now())));
        summary.put("scheduled", appRepo.countByStatusIs(AppointmentStatus.SCHEDULED));
        summary.put("prescriptions", presRepo.count());
        return summary;
    }


    
    
}
