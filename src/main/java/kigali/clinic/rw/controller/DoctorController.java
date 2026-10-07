package kigali.clinic.rw.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import kigali.clinic.rw.domain.Doctor;
import kigali.clinic.rw.domain.Specialization;
import kigali.clinic.rw.service.DoctorService;

@RestController
@RequestMapping(value = "/api/doctor")
public class DoctorController {

    @Autowired
    private DoctorService docServe;

    private HttpStatus statusFor(String msg) {
        if (msg.endsWith("successfully")) return HttpStatus.OK;
        if (msg.contains("not found")) return HttpStatus.NOT_FOUND;
        return HttpStatus.CONFLICT;
    }

    // CREATE  (add ?officeId=... to assign an office)
    @PostMapping(value = "/save")
    public ResponseEntity<?> saveDoctor(@RequestBody Doctor doctor,
                                         @RequestParam(required = false) UUID officeId) {
        String msg = docServe.saveDoctor(doctor, officeId);
        return new ResponseEntity<>(msg, statusFor(msg));
    }

    // READ ALL
    @GetMapping
    public ResponseEntity<List<Doctor>> getAllDoctors() {
        return new ResponseEntity<>(docServe.getAllDoctors(), HttpStatus.OK);
    }

    // READ ONE
    @GetMapping("/{id}")
    public ResponseEntity<?> getDoctorById(@PathVariable UUID id) {
        Doctor doctor = docServe.getDoctorById(id);
        if (doctor == null) {
            return new ResponseEntity<>("Doctor not found with id: " + id, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(doctor, HttpStatus.OK);
    }

    // UPDATE  (add ?officeId=... only if you want to change the office)
    @PutMapping("/{id}")
    public ResponseEntity<?> updateDoctor(@PathVariable UUID id,
                                           @RequestBody Doctor doctor,
                                           @RequestParam(required = false) UUID officeId) {
        String msg = docServe.updateDoctor(id, doctor, officeId);
        return new ResponseEntity<>(msg, statusFor(msg));
    }
    
     // READ the specializations of one doctor
    @GetMapping("/{id}/specializations")
    public ResponseEntity<?> getSpecializationsOfDoctor(@PathVariable UUID id) {
        List<Specialization> list = docServe.getSpecializationsOfDoctor(id);
        if (list == null) {
            return new ResponseEntity<>("Doctor not found with id: " + id, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDoctor(@PathVariable UUID id) {
        String msg = docServe.deleteDoctor(id);
        return new ResponseEntity<>(msg, statusFor(msg));
    }
}