package kigali.clinic.rw.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import kigali.clinic.rw.domain.Prescription;
import kigali.clinic.rw.service.PrescriptionService;

@RestController
@RequestMapping(value = {"/api/prescription", "/api/prescriptions"})
public class PrescriptionController {

    @Autowired
    private PrescriptionService presServe;

    
    @PostMapping("/save")
    public ResponseEntity<String> savePrescription(@RequestBody Prescription prescription,
                                                   @RequestParam UUID appointmentId) {
        String msg = presServe.savePrescription(prescription, appointmentId);
        HttpStatus status = HttpStatus.CREATED;
        if (msg.contains("does not exist")) {
            status = HttpStatus.NOT_FOUND;
        } else if (msg.startsWith("Prescriptions are only")) {
            status = HttpStatus.BAD_REQUEST;
        }
        return new ResponseEntity<>(msg, status);
    }

    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<?> getByAppointment(@PathVariable UUID appointmentId) {
        List<Prescription> list = presServe.getByAppointment(appointmentId);
        if (list == null) {
            return new ResponseEntity<>("The appointment with that id does not exist", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(list, HttpStatus.OK);
        
    }

    @GetMapping("/patient/{ssNumber}")
    public ResponseEntity<List<Prescription>> getHistoryOfPatient(@PathVariable String ssNumber) {
        return new ResponseEntity<>(presServe.getHistoryOfPatient(ssNumber), HttpStatus.OK);
    }

    @PutMapping ("/update/{id}")
    public ResponseEntity<String> updatePrescription(@PathVariable UUID id, @RequestBody Prescription prescription) {
        String msg = presServe.updatePrescription(id, prescription);
        HttpStatus status = HttpStatus.OK;
        if (msg.contains("does not exist")) {
            status = HttpStatus.NOT_FOUND;
        }
        return new ResponseEntity<>(msg, status);
    }


    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deletePrescription(@PathVariable UUID id) {
        String msg = presServe.deletePrescription(id);
        HttpStatus status = HttpStatus.OK;
        if (msg.contains("does not exist")) {
            status = HttpStatus.NOT_FOUND;
        }
        return new ResponseEntity<>(msg, status);
    }

}
