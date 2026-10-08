package kigali.clinic.rw.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import kigali.clinic.rw.domain.Specialization;
import kigali.clinic.rw.service.SpecializationService;

@RestController
@RequestMapping(value = {"/api/specialization", "/api/specializations"})
public class SpecializationController {

    @Autowired
    private SpecializationService specServe;

    private HttpStatus statusFor(String msg) {
        if (msg.endsWith("successfully")) return HttpStatus.OK;
        if (msg.contains("not found")) return HttpStatus.NOT_FOUND;
        if (msg.contains("required")) return HttpStatus.BAD_REQUEST;
        return HttpStatus.CONFLICT;
    }

    // CREATE
    @PostMapping(value = "/save")
    public ResponseEntity<?> saveSpecialization(@RequestBody Specialization specialization) {
        String msg = specServe.saveSpecialization(specialization);
        return new ResponseEntity<>(msg, statusFor(msg));
    }

    // READ ALL
    @GetMapping
    public ResponseEntity<List<Specialization>> getAllSpecializations() {
        return new ResponseEntity<>(specServe.getAllSpecializations(), HttpStatus.OK);
    }

    // READ ONE
    @GetMapping("/{id}")
    public ResponseEntity<?> getSpecializationById(@PathVariable UUID id) {
        Specialization spec = specServe.getSpecializationById(id);
        if (spec == null) {
            return new ResponseEntity<>("Specialization not found with id: " + id, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(spec, HttpStatus.OK);
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<?> updateSpecialization(@PathVariable UUID id,
                                                  @RequestBody Specialization specialization) {
        String msg = specServe.updateSpecialization(id, specialization);
        return new ResponseEntity<>(msg, statusFor(msg));
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSpecialization(@PathVariable UUID id) {
        String msg = specServe.deleteSpecialization(id);
        return new ResponseEntity<>(msg, statusFor(msg));
    }

    // MANY-TO-MANY: give a doctor this specialization
    @PostMapping("/{id}/doctor/{doctorId}")
    public ResponseEntity<?> addDoctor(@PathVariable UUID id, @PathVariable UUID doctorId) {
        String msg = specServe.addDoctor(id, doctorId);
        return new ResponseEntity<>(msg, statusFor(msg));
    }

    // MANY-TO-MANY: take this specialization away from a doctor
    @DeleteMapping("/{id}/doctor/{doctorId}")
    public ResponseEntity<?> removeDoctor(@PathVariable UUID id, @PathVariable UUID doctorId) {
        String msg = specServe.removeDoctor(id, doctorId);
        return new ResponseEntity<>(msg, statusFor(msg));
    }

    @GetMapping("/unused")
    public ResponseEntity<List<Specialization>> getUnusedSpecializations() {
        return new ResponseEntity<>(specServe.getUnused(), HttpStatus.OK);
    }
}
