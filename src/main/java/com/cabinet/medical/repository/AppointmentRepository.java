package com.cabinet.medical.repository;

import com.cabinet.medical.entity.Appointment;
import com.cabinet.medical.entity.Patient;
import com.cabinet.medical.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository AppointmentRepository
 * Interface pour accéder aux données de la table "appointment"
 * Gère les rendez-vous spécifiques entre patients et médecins
 */
@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // ═══════════════════════════════════════════════════════════
    // MÉTHODES SIMPLES (Query Methods)
    // ═══════════════════════════════════════════════════════════

    /**
     * Trouve tous les RDV d'un patient
     * SQL généré: SELECT * FROM appointment WHERE patient_id = ?
     */
    List<Appointment> findByPatient(Patient patient);

    /**
     * Trouve tous les RDV d'un patient par son ID
     * SQL généré: SELECT * FROM appointment WHERE patient_id = ?
     */
    List<Appointment> findByPatientId(Long patientId);

    /**
     * Trouve tous les RDV d'un médecin
     * SQL généré: SELECT * FROM appointment WHERE doctor_id = ?
     */
    List<Appointment> findByDoctor(Doctor doctor);

    /**
     * Trouve tous les RDV d'un médecin par son ID
     * SQL généré: SELECT * FROM appointment WHERE doctor_id = ?
     */
    List<Appointment> findByDoctorId(Long doctorId);

    /**
     * Trouve RDV par statut
     * SQL généré: SELECT * FROM appointment WHERE status = ?
     */
    List<Appointment> findByStatus(Appointment.AppointmentStatus status);

    /**
     * Trouve RDV d'un patient par statut
     * SQL généré: SELECT * FROM appointment WHERE patient_id = ? AND status = ?
     */
    List<Appointment> findByPatientAndStatus(Patient patient,
            Appointment.AppointmentStatus status);

    /**
     * Trouve RDV d'un médecin par statut
     * SQL généré: SELECT * FROM appointment WHERE doctor_id = ? AND status = ?
     */
    List<Appointment> findByDoctorAndStatus(Doctor doctor,
            Appointment.AppointmentStatus status);

    /**
     * Trouve RDV après une date
     * SQL généré: SELECT * FROM appointment WHERE date_time >= ?
     */
    List<Appointment> findByDateTimeAfter(LocalDateTime dateTime);

    /**
     * Trouve RDV avant une date
     * SQL généré: SELECT * FROM appointment WHERE date_time < ?
     */
    List<Appointment> findByDateTimeBefore(LocalDateTime dateTime);

    /**
     * Trouve RDV entre deux dates
     * SQL généré: SELECT * FROM appointment WHERE date_time BETWEEN ? AND ?
     */
    List<Appointment> findByDateTimeBetween(LocalDateTime start, LocalDateTime end);

    // ═══════════════════════════════════════════════════════════
    // REQUÊTES MÉTIER CRITIQUES (Disponibilité & Conflits)
    // ═══════════════════════════════════════════════════════════

    /**
     * Vérifie si un médecin a déjà un RDV à une date/heure exacte
     * CRITIQUE: Empêche double-booking
     * JPQL: Condition sur doctor ET dateTime
     */
    @Query("SELECT a FROM Appointment a WHERE " +
            "a.doctor = :doctor AND " +
            "a.dateTime = :dateTime AND " +
            "a.status NOT IN ('CANCELLED', 'NO_SHOW')")
    Optional<Appointment> findByDoctorAndDateTime(@Param("doctor") Doctor doctor,
            @Param("dateTime") LocalDateTime dateTime);

    /**
     * Trouve RDV qui chevauchent une période donnée pour un médecin
     * LOGIQUE: Un RDV chevauche si:
     * (rdv_start < période_end) AND (rdv_end > période_start)
     * Où rdv_end = rdv_start + duration minutes
     */
    @Query("SELECT a FROM Appointment a WHERE " +
            "a.doctor = :doctor AND " +
            "a.status NOT IN ('CANCELLED', 'NO_SHOW') AND " +
            "a.dateTime < :endTime AND " +
            "FUNCTION('DATEADD', MINUTE, a.duration, a.dateTime) > :startTime")
    List<Appointment> findConflictingAppointments(@Param("doctor") Doctor doctor,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * Trouve RDV futurs d'un patient triés par date
     * JPQL: Comparaison avec date actuelle + ORDER BY
     */
    @Query("SELECT a FROM Appointment a WHERE " +
            "a.patient = :patient AND " +
            "a.dateTime >= :now AND " +
            "a.status NOT IN ('CANCELLED', 'NO_SHOW') " +
            "ORDER BY a.dateTime ASC")
    List<Appointment> findUpcomingByPatient(@Param("patient") Patient patient,
            @Param("now") LocalDateTime now);

    /**
     * Trouve RDV futurs d'un médecin triés par date
     */
    @Query("SELECT a FROM Appointment a WHERE " +
            "a.doctor = :doctor AND " +
            "a.dateTime >= :now AND " +
            "a.status NOT IN ('CANCELLED', 'NO_SHOW') " +
            "ORDER BY a.dateTime ASC")
    List<Appointment> findUpcomingByDoctor(@Param("doctor") Doctor doctor,
            @Param("now") LocalDateTime now);

    /**
     * Trouve RDV passés d'un patient (historique)
     */
    @Query("SELECT a FROM Appointment a WHERE " +
            "a.patient = :patient AND " +
            "a.dateTime < :now " +
            "ORDER BY a.dateTime DESC")
    List<Appointment> findPastByPatient(@Param("patient") Patient patient,
            @Param("now") LocalDateTime now);

    /**
     * Trouve RDV passés d'un médecin
     */
    @Query("SELECT a FROM Appointment a WHERE " +
            "a.doctor = :doctor AND " +
            "a.dateTime < :now " +
            "ORDER BY a.dateTime DESC")
    List<Appointment> findPastByDoctor(@Param("doctor") Doctor doctor,
            @Param("now") LocalDateTime now);

    // ═══════════════════════════════════════════════════════════
    // REQUÊTES AVANCÉES (Filtres & Recherche)
    // ═══════════════════════════════════════════════════════════

    /**
     * Recherche RDV par nom patient ou médecin
     * JPQL: Navigation relations + LIKE case-insensitive
     */
    @Query("SELECT a FROM Appointment a WHERE " +
            "LOWER(a.patient.user.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(a.patient.user.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(a.doctor.user.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(a.doctor.user.lastName) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Appointment> searchByName(@Param("search") String search);

    /**
     * Filtre avancé RDV (Admin dashboard)
     * Tous paramètres optionnels (NULL-safe)
     */
    @Query("SELECT a FROM Appointment a WHERE " +
            "(:patientId IS NULL OR a.patient.id = :patientId) AND " +
            "(:doctorId IS NULL OR a.doctor.id = :doctorId) AND " +
            "(:status IS NULL OR a.status = :status) AND " +
            "(:startDate IS NULL OR a.dateTime >= :startDate) AND " +
            "(:endDate IS NULL OR a.dateTime <= :endDate) " +
            "ORDER BY a.dateTime DESC")
    List<Appointment> findByFilters(@Param("patientId") Long patientId,
            @Param("doctorId") Long doctorId,
            @Param("status") Appointment.AppointmentStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Trouve RDV d'un médecin pour une journée spécifique
     * JPQL: Filtrage par date (pas heure)
     * FUNCTION('DATE', ...) extrait juste la date
     */
    @Query("SELECT a FROM Appointment a WHERE " +
            "a.doctor = :doctor AND " +
            "FUNCTION('DATE', a.dateTime) = :date AND " +
            "a.status NOT IN ('CANCELLED', 'NO_SHOW') " +
            "ORDER BY a.dateTime ASC")
    List<Appointment> findByDoctorAndDate(@Param("doctor") Doctor doctor,
            @Param("date") LocalDateTime date);

    /**
     * Vérifie si un patient a déjà un RDV avec ce médecin (pas annulé)
     * Empêche doublons pour même patient/médecin
     */
    @Query("SELECT COUNT(a) > 0 FROM Appointment a WHERE " +
            "a.patient = :patient AND " +
            "a.doctor = :doctor AND " +
            "a.status NOT IN ('CANCELLED', 'NO_SHOW')")
    boolean hasActiveAppointmentWithDoctor(@Param("patient") Patient patient,
            @Param("doctor") Doctor doctor);

    // ═══════════════════════════════════════════════════════════
    // STATISTIQUES & ANALYTICS
    // ═══════════════════════════════════════════════════════════

    /**
     * Compte RDV par statut
     * JPQL: GROUP BY sur enum
     */
    @Query("SELECT a.status, COUNT(a) FROM Appointment a GROUP BY a.status")
    List<Object[]> countByStatus();

    /**
     * Compte RDV d'un médecin par statut
     */
    @Query("SELECT a.status, COUNT(a) FROM Appointment a " +
            "WHERE a.doctor = :doctor " +
            "GROUP BY a.status")
    List<Object[]> countByStatusForDoctor(@Param("doctor") Doctor doctor);

    /**
     * Compte RDV futurs d'un patient
     */
    @Query("SELECT COUNT(a) FROM Appointment a WHERE " +
            "a.patient = :patient AND " +
            "a.dateTime >= :now AND " +
            "a.status NOT IN ('CANCELLED', 'NO_SHOW')")
    long countUpcomingByPatient(@Param("patient") Patient patient,
            @Param("now") LocalDateTime now);

    /**
     * Trouve RDV nécessitant rappel 24h
     * JPQL: RDV dans 24h (entre now et now+24h)
     */
    @Query("SELECT a FROM Appointment a WHERE " +
            "a.dateTime BETWEEN :now AND :tomorrow AND " +
            "a.status IN ('PENDING', 'CONFIRMED')")
    List<Appointment> findAppointmentsFor24HReminder(@Param("now") LocalDateTime now,
            @Param("tomorrow") LocalDateTime tomorrow);

    /**
     * Trouve RDV nécessitant rappel 1h
     */
    @Query("SELECT a FROM Appointment a WHERE " +
            "a.dateTime BETWEEN :now AND :oneHourLater AND " +
            "a.status IN ('PENDING', 'CONFIRMED')")
    List<Appointment> findAppointmentsFor1HReminder(@Param("now") LocalDateTime now,
            @Param("oneHourLater") LocalDateTime oneHourLater);

    /**
     * Taux de no-show par médecin
     * SQL natif: Calcul pourcentage avec CAST pour division décimale
     */
    @Query(value = "SELECT d.id, " +
            "       u.first_name, " +
            "       u.last_name, " +
            "       COUNT(*) FILTER (WHERE a.status = 'NO_SHOW') as no_show_count, " +
            "       COUNT(*) as total_appointments, " +
            "       CAST(COUNT(*) FILTER (WHERE a.status = 'NO_SHOW') AS FLOAT) / " +
            "       NULLIF(COUNT(*), 0) * 100 as no_show_rate " +
            "FROM appointment a " +
            "JOIN doctor d ON a.doctor_id = d.id " +
            "JOIN users u ON d.user_id = u.id " +
            "WHERE a.date_time < :now " +
            "GROUP BY d.id, u.first_name, u.last_name " +
            "ORDER BY no_show_rate DESC", nativeQuery = true)
    List<Object[]> getNoShowRateByDoctor(@Param("now") LocalDateTime now);

    /**
     * RDV par jour de la semaine (statistiques)
     * SQL natif: EXTRACT(DOW FROM ...) = Day Of Week (0=Dimanche, 6=Samedi)
     */
    @Query(value = "SELECT EXTRACT(DOW FROM date_time) as day_of_week, " +
            "       COUNT(*) as appointment_count " +
            "FROM appointment " +
            "WHERE date_time >= :startDate AND date_time <= :endDate " +
            "GROUP BY day_of_week " +
            "ORDER BY day_of_week", nativeQuery = true)
    List<Object[]> getAppointmentsByDayOfWeek(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Top médecins par nombre de RDV complétés
     * SQL natif: JOIN, COUNT, ORDER BY avec LIMIT
     */
    @Query(value = "SELECT d.id, " +
            "       u.first_name, " +
            "       u.last_name, " +
            "       d.specialty, " +
            "       COUNT(*) as completed_count " +
            "FROM appointment a " +
            "JOIN doctor d ON a.doctor_id = d.id " +
            "JOIN users u ON d.user_id = u.id " +
            "WHERE a.status = 'COMPLETED' " +
            "  AND a.date_time >= :startDate " +
            "GROUP BY d.id, u.first_name, u.last_name, d.specialty " +
            "ORDER BY completed_count DESC " +
            "LIMIT :limit", nativeQuery = true)
    List<Object[]> getTopDoctorsByCompletedAppointments(@Param("startDate") LocalDateTime startDate,
            @Param("limit") int limit);
}