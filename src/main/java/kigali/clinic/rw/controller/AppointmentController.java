package kigali.clinic.rw.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import kigali.clinic.rw.domain.Appointment;
import kigali.clinic.rw.domain.AppointmentStatus;
import kigali.clinic.rw.service.AppointmentService;

@RestController
@RequestMapping(value = {"/api/appointment", "/api/appointments"})
public class AppointmentController {

    @Autowired
    private AppointmentService appServe;

    // CREATE (so that the queries have data): /save?patientId=...&doctorId=...
    @PostMapping(value = "/save")
    public ResponseEntity<?> saveAppointment(@RequestBody Appointment appointment,
                                             @RequestParam UUID patientId,
                                             @RequestParam UUID doctorId) {
        String msg = appServe.saveAppointment(appointment, patientId, doctorId);
        HttpStatus status = HttpStatus.OK;
        if (msg.contains("not found")) {
            status = HttpStatus.NOT_FOUND;
        } else if (!msg.endsWith("successfully")) {
            status = HttpStatus.CONFLICT;
        }
        return new ResponseEntity<>(msg, status);
    }

    // READ ALL
    @GetMapping
    public ResponseEntity<List<Appointment>> getAllAppointments() {
        return new ResponseEntity<>(appServe.getAllAppointments(), HttpStatus.OK);
    }

    // QUERY 1 (derived): PENDING appointments of a doctor, ordered by date
    @GetMapping("/doctor/{doctorId}/pending")
    public ResponseEntity<List<Appointment>> getPendingOfDoctor(@PathVariable UUID doctorId) {
        return new ResponseEntity<>(appServe.getPendingOfDoctor(doctorId), HttpStatus.OK);
    }

    // QUERY 2 (JPQL): PENDING appointments whose date has passed
    @GetMapping("/pending/overdue")
    public ResponseEntity<List<Appointment>> getOverduePending() {
        return new ResponseEntity<>(appServe.getOverduePending(), HttpStatus.OK);
    }

    // QUERY 3 (aggregate): number of appointments per doctor, busiest first
    @GetMapping("/count-per-doctor")
    public ResponseEntity<List<Object[]>> countAppointmentsPerDoctor() {
        return new ResponseEntity<>(appServe.countAppointmentsPerDoctor(), HttpStatus.OK);
    }

    // QUERY 4 (join across 2 relationships): appointments of doctors having a specialization
    @GetMapping("/by-specialization")
    public ResponseEntity<List<Appointment>> getBySpecialization(@RequestParam String name) {
        return new ResponseEntity<>(appServe.getBySpecialization(name), HttpStatus.OK);
    }

    
    @GetMapping("/doctor/{doctorId}/page")
    public ResponseEntity<Page<Appointment>> getPageOfDoctor(@PathVariable UUID doctorId,
                                                             @RequestParam(defaultValue = "1") int page,
                                                             @RequestParam(defaultValue = "10") int size) {
        return new ResponseEntity<>(appServe.getPageOfDoctor(doctorId, page, size), HttpStatus.OK);
    }
    
    @GetMapping ("/by-status")
    public ResponseEntity<List<Appointment>> getByStatus(@RequestParam AppointmentStatus status) {
        return new ResponseEntity<>(appServe.getByStatus(status), HttpStatus.OK);
    }

    @GetMapping ("/between")
    public ResponseEntity<List<Appointment>> getBetween(@RequestParam String start,
                                                         @RequestParam String end) {
        LocalDate startDate = LocalDate.parse(start);
        LocalDate endDate = LocalDate.parse(end);                                                    
        return new ResponseEntity<>(appServe.getBetween(startDate, endDate), HttpStatus.OK);
    }
}
