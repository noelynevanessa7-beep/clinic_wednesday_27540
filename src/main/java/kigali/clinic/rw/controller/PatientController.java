package kigali.clinic.rw.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import kigali.clinic.rw.domain.Patient;
import kigali.clinic.rw.domain.Appointment;
import kigali.clinic.rw.service.AppointmentService;
import kigali.clinic.rw.service.PatientService;

@RestController
@RequestMapping(value = {"/api/patient", "/api/patients"})
public class PatientController {

    @Autowired
    private PatientService patServe;

    @Autowired 
    private AppointmentService appServe;

    private HttpStatus statusFor(String msg) {
        if (msg.endsWith("successfully")) return HttpStatus.OK;
        if (msg.contains("not found")) return HttpStatus.NOT_FOUND;
        return HttpStatus.CONFLICT;
    }

    // CREATE
    @PostMapping(value = "/save")
    public ResponseEntity<?> savePatient(@RequestBody Patient patient) {
        String msg = patServe.savePatient(patient);
        return new ResponseEntity<>(msg, statusFor(msg));
    }

    // READ ALL
    @GetMapping
    public ResponseEntity<List<Patient>> getAllPatients() {
        return new ResponseEntity<>(patServe.getAllPatients(), HttpStatus.OK);
    }
    
    @GetMapping("/by-last-name")
    public ResponseEntity<List<Patient>> getPatientsByLastName(@RequestParam String lastName) {
        return new ResponseEntity<>(patServe.getPatientsByLastName(lastName), HttpStatus.OK);
    }

    // READ ONE
    @GetMapping("/{id}")
    public ResponseEntity<?> getPatientById(@PathVariable UUID id) {
        Patient patient = patServe.getPatientById(id);
        if (patient == null) {
            return new ResponseEntity<>("Patient not found with id: " + id, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(patient, HttpStatus.OK);
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<?> updatePatient(@PathVariable UUID id, @RequestBody Patient patient) {
        String msg = patServe.updatePatient(id, patient);
        return new ResponseEntity<>(msg, statusFor(msg));
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePatient(@PathVariable UUID id) {
        String msg = patServe.deletePatient(id);
        return new ResponseEntity<>(msg, statusFor(msg));
    }

    @GetMapping ("/of-doctor/{doctorId}")
    public ResponseEntity<?> getPatientsOfDoctor(@PathVariable UUID doctorId) {
        List<Patient> patients = patServe.getPatientsOfDoctor(doctorId);
        if (patients == null) {
            return new ResponseEntity<>("Doctor not found with id: " + doctorId, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(patients, HttpStatus.OK);
    }

    @GetMapping ("/frequent")
    public ResponseEntity<List<Patient>> getFrequent(@RequestParam long min) {
        return new ResponseEntity<>(patServe.getFrequent(min), HttpStatus.OK);
    }

    @GetMapping("/{ssNumber}/upcoming")
    public ResponseEntity<?> getUpcoming(@PathVariable String ssNumber) {
        List<Appointment> list = appServe.getUpcoming(ssNumber);
        if (list == null) {
            return new ResponseEntity<>("That patient with that ssnumber does not exist", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(list, HttpStatus.OK);
    }
}