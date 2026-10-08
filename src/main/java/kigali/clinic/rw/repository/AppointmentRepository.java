package kigali.clinic.rw.repository;

import java.sql.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import kigali.clinic.rw.domain.Appointment;
import kigali.clinic.rw.domain.AppointmentStatus;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    // 1. DERIVED: all appointments of a doctor with a given status, oldest date first
    List<Appointment> findByDoctorIdAndStatusOrderByAppointmentDateAsc(UUID doctorId, AppointmentStatus status);

    // 2. JPQL: appointments with a given status whose date has already passed
    //    (JOIN FETCH loads the patient and the doctor in the same SQL query)
    @Query("SELECT a FROM Appointment a "
         + "JOIN FETCH a.patient p "
         + "JOIN FETCH a.doctor d "
         + "WHERE a.status = :status AND a.appointmentDate < :today "
         + "ORDER BY a.appointmentDate ASC")
    List<Appointment> findOverdueByStatus(@Param("status") AppointmentStatus status,
                                          @Param("today") Date today);

    // 3. AGGREGATE: number of appointments per doctor, busiest first
    //    each row = [firstName, lastName, count]
    @Query("SELECT d.firstName, d.lastName, COUNT(a) "
         + "FROM Appointment a JOIN a.doctor d "
         + "GROUP BY d.id, d.firstName, d.lastName "
         + "ORDER BY COUNT(a) DESC, d.lastName ASC")
    List<Object[]> countAppointmentsPerDoctor();

    // 4. JOIN across 2 relationships: Appointment -> Doctor -> Specialization
    @Query("SELECT a FROM Appointment a "
         + "JOIN a.doctor d "
         + "JOIN d.specializations s "
         + "WHERE s.name = :name "
         + "ORDER BY a.appointmentDate ASC")
    List<Appointment> findByDoctorSpecialization(@Param("name") String name);

    // 5. PAGINATION: one page of a doctor's appointments (page size and sorting come from the Pageable)
    Page<Appointment> findByDoctorId(UUID doctorId, Pageable pageable);
    List<Appointment> findByStatusOrderByAppointmentDateAsc(AppointmentStatus status);
    List<Appointment> findByAppointmentDateBetweenOrderByAppointmentDateAsc(Date startDate, Date endDate);
     
    boolean existsByDoctorIdAndAppointmentDateAndStatusNot(UUID doctorId, Date appointmentDate, AppointmentStatus status);

}
