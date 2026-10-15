package kigali.clinic.rw.repository;

import java.sql.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import kigali.clinic.rw.domain.Appointment;
import kigali.clinic.rw.domain.AppointmentStatus;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    
    List<Appointment> findByDoctorIdAndStatusOrderByAppointmentDateAsc(UUID doctorId, AppointmentStatus status);

    
    @Query("SELECT a FROM Appointment a "
         + "JOIN FETCH a.patient p "
         + "JOIN FETCH a.doctor d "
         + "WHERE a.status = :status AND a.appointmentDate < :today "
         + "ORDER BY a.appointmentDate ASC")
    List<Appointment> findOverdueByStatus(@Param("status") AppointmentStatus status,
                                          @Param("today") Date today);

    
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

    @Query ("SELECT a.status, COUNT(a) FROM Appointment a GROUP BY a.status")
    List<Object[]> countByStatus();

    @Query("SELECT o.name, o.officeNumber, COUNT(a) FROM Appointment a JOIN a.doctor d JOIN d.office o GROUP BY o.id, o.name, o.officeNumber ORDER BY COUNT(a) DESC")
     List<Object[]> findBusiestOffice(Pageable pageable);

     @Modifying 
     @Query("UPDATE Appointment a SET a.status = :newStatus WHERE a.doctor.id = :doctorId AND a.appointmentDate = :date AND a.status <> :keepStatus")
     int cancelDayOfDoctor(@Param("doctorId") UUID doctorId, @Param("date") Date date, @Param("newStatus") AppointmentStatus newStatus, @Param("keepStatus") AppointmentStatus keepStatus);
     
     @Modifying 
     @Query ("DELETE FROM Appointment a WHERE a.status = :status AND a.appointmentDate < :date")
     int deleteByStatusBefore(@Param("status") AppointmentStatus status, @Param("date") Date date);

     List<Appointment> findByDoctorIdAndAppointmentDateAndStatusNot(UUID doctorId, Date appointmentDate, AppointmentStatus status);

     List<Appointment> findByPatientSsNumberAndAppointmentDateGreaterThanEqualAndStatusInOrderByAppointmentDateAsc(String ssNumber, Date date, List<AppointmentStatus> statuses);

     long countByAppointmentDate(Date appointmentDate);

     long countByStatusIs(AppointmentStatus status);

     @Query ("SELECT d.firstName, d.lastName, COUNT(a) FROM Appointment a JOIN a.doctor d WHERE a.status = :status AND a.appointmentDate BETWEEN :start AND :end GROUP BY d.id, d,firstName, d.lastName ORDER BY COUNT(a) DESC")
     List<Object[]> doctorWorkload(@Param ("status") AppointmentStatus status, @Param ("start") Date start, @Param ("end") Date end);
}

