package kigali.clinic.rw.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import kigali.clinic.rw.domain.Office;
import kigali.clinic.rw.service.OfficeService;

@RestController
@RequestMapping(value = {"/api/office", "/api/offices"})
public class OfficeController {

    @Autowired
    private OfficeService offServe;

    private HttpStatus statusFor(String msg) {
        if (msg.endsWith("successfully")) return HttpStatus.OK;
        if (msg.contains("not found")) return HttpStatus.NOT_FOUND;
        return HttpStatus.CONFLICT;
    }

    // CREATE
    @PostMapping(value = "/save")
    public ResponseEntity<?> saveOffice(@RequestBody Office office) {
        String msg = offServe.saveOffice(office);
        return new ResponseEntity<>(msg, statusFor(msg));
    }

    // READ ALL
    @GetMapping
    public ResponseEntity<List<Office>> getAllOffices() {
        return new ResponseEntity<>(offServe.getAllOffices(), HttpStatus.OK);
    }

    @GetMapping ("/busiest")
    public ResponseEntity<?> getBusiestOffices() {
        List<Object[]> rows = offServe.getBusiestOffices();
        if (rows.isEmpty()) {
            return new ResponseEntity<>("No appointments yet", HttpStatus.OK);
        }
        return new ResponseEntity<>(rows.get(0), HttpStatus.OK);
    }

    // READ ONE
    @GetMapping("/{id}")
    public ResponseEntity<?> getOfficeById(@PathVariable UUID id) {
        Office office = offServe.getOfficeById(id);
        if (office == null) {
            return new ResponseEntity<>("Office not found with id: " + id, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(office, HttpStatus.OK);
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<?> updateOffice(@PathVariable UUID id, @RequestBody Office office) {
        String msg = offServe.updateOffice(id, office);
        return new ResponseEntity<>(msg, statusFor(msg));
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteOffice(@PathVariable UUID id) {
        String msg = offServe.deleteOffice(id);
        return new ResponseEntity<>(msg, statusFor(msg));
    }

    @GetMapping ("/free")
    public ResponseEntity<List<Office>> getOfficesWithoutDoctor() {
        return new ResponseEntity<>(offServe.getOfficesWithoutDoctor(), HttpStatus.OK);
    }
}