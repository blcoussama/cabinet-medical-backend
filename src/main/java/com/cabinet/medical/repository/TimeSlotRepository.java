package com.cabinet.medical.repository;

import com.cabinet.medical.entity.TimeSlot;
import com.cabinet.medical.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalTime;
import java.util.List;

/**
 * Repository TimeSlotRepository
 * Interface pour accéder aux données de la table "timeslot"
 * Gère les créneaux horaires récurrents des médecins
 */
@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {

    // ═══════════════════════════════════════════════════════════
    // MÉTHODES SIMPLES (Query Methods)
    // ═══════════════════════════════════════════════════════════

    /**
     * Trouve tous les créneaux d'un médecin
     * SQL généré: SELECT * FROM timeslot WHERE doctor_id = ?
     */
    List<TimeSlot> findByDoctor(Doctor doctor);

    /**
     * Trouve tous les créneaux d'un médecin par son ID
     * SQL généré: SELECT * FROM timeslot WHERE doctor_id = ?
     */
    List<TimeSlot> findByDoctorId(Long doctorId);

    /**
     * Trouve créneaux actifs d'un médecin
     * SQL généré: SELECT * FROM timeslot WHERE doctor_id = ? AND is_active = true
     */
    List<TimeSlot> findByDoctorAndIsActiveTrue(Doctor doctor);

    /**
     * Trouve créneaux d'un médecin pour un jour spécifique
     * SQL généré: SELECT * FROM timeslot WHERE doctor_id = ? AND day_of_week = ?
     */
    List<TimeSlot> findByDoctorAndDayOfWeek(Doctor doctor, TimeSlot.DayOfWeek dayOfWeek);

    /**
     * Trouve créneaux actifs d'un médecin pour un jour
     * SQL généré: SELECT * FROM timeslot
     * WHERE doctor_id = ? AND day_of_week = ? AND is_active = true
     */
    List<TimeSlot> findByDoctorAndDayOfWeekAndIsActiveTrue(Doctor doctor,
            TimeSlot.DayOfWeek dayOfWeek);

    /**
     * Trouve tous les créneaux pour un jour de la semaine (tous médecins)
     * SQL généré: SELECT * FROM timeslot WHERE day_of_week = ?
     */
    List<TimeSlot> findByDayOfWeek(TimeSlot.DayOfWeek dayOfWeek);

    /**
     * Trouve créneaux avec durée spécifique
     * SQL généré: SELECT * FROM timeslot WHERE duration = ?
     */
    List<TimeSlot> findByDuration(Integer duration);

    // ═══════════════════════════════════════════════════════════
    // MÉTHODES AVANCÉES AVEC @Query (JPQL)
    // ═══════════════════════════════════════════════════════════

    /**
     * Trouve créneaux d'un médecin triés par jour puis heure
     * JPQL: ORDER BY sur ENUM (ordre alphabétique par défaut)
     * Pour ordre logique (Lundi->Dimanche), utiliser CASE en SQL natif
     */
    @Query("SELECT t FROM TimeSlot t " +
            "WHERE t.doctor = :doctor AND t.isActive = true " +
            "ORDER BY t.dayOfWeek, t.startTime")
    List<TimeSlot> findActiveSlotsOrderedByDay(@Param("doctor") Doctor doctor);

    /**
     * Vérifie si un créneau chevauche d'autres créneaux du même médecin/jour
     * JPQL: Détection chevauchement horaire
     * Chevauchement si:
     * - (nouveau_start < existant_end) AND (nouveau_end > existant_start)
     */
    @Query("SELECT t FROM TimeSlot t WHERE " +
            "t.doctor = :doctor AND " +
            "t.dayOfWeek = :dayOfWeek AND " +
            "t.isActive = true AND " +
            "t.id != :excludeId AND " +
            "((t.startTime < :endTime AND t.endTime > :startTime))")
    List<TimeSlot> findOverlappingSlots(@Param("doctor") Doctor doctor,
            @Param("dayOfWeek") TimeSlot.DayOfWeek dayOfWeek,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("excludeId") Long excludeId);

    /**
     * Trouve créneaux d'un médecin pour une plage de jours
     * JPQL: IN clause avec liste de jours
     */
    @Query("SELECT t FROM TimeSlot t WHERE " +
            "t.doctor = :doctor AND " +
            "t.dayOfWeek IN :days AND " +
            "t.isActive = true " +
            "ORDER BY t.dayOfWeek, t.startTime")
    List<TimeSlot> findByDoctorAndDays(@Param("doctor") Doctor doctor,
            @Param("days") List<TimeSlot.DayOfWeek> days);

    /**
     * Compte créneaux actifs d'un médecin
     * JPQL: COUNT avec conditions
     */
    @Query("SELECT COUNT(t) FROM TimeSlot t WHERE t.doctor = :doctor AND t.isActive = true")
    long countActiveSlotsByDoctor(@Param("doctor") Doctor doctor);

    /**
     * Trouve créneaux avec durée minimale
     * JPQL: Filtre sur durée >= minimum
     */
    @Query("SELECT t FROM TimeSlot t WHERE " +
            "t.doctor = :doctor AND " +
            "t.duration >= :minDuration AND " +
            "t.isActive = true")
    List<TimeSlot> findByDoctorAndMinDuration(@Param("doctor") Doctor doctor,
            @Param("minDuration") Integer minDuration);

    /**
     * Statistiques: Nombre de créneaux par jour de la semaine
     * JPQL: GROUP BY sur enum
     */
    @Query("SELECT t.dayOfWeek, COUNT(t) FROM TimeSlot t " +
            "WHERE t.doctor = :doctor AND t.isActive = true " +
            "GROUP BY t.dayOfWeek")
    List<Object[]> countSlotsByDayOfWeek(@Param("doctor") Doctor doctor);

    /**
     * Trouve tous les médecins qui travaillent un jour spécifique
     * JPQL: DISTINCT sur relation
     */
    @Query("SELECT DISTINCT t.doctor FROM TimeSlot t " +
            "WHERE t.dayOfWeek = :dayOfWeek AND t.isActive = true")
    List<Doctor> findDoctorsWorkingOnDay(@Param("dayOfWeek") TimeSlot.DayOfWeek dayOfWeek);

    /**
     * Calcul heures totales de travail par semaine pour un médecin
     * SQL natif: Calcul avec EXTRACT et SUM
     * (end_time - start_time) pour obtenir durée en heures
     */
    @Query(value = "SELECT SUM(EXTRACT(EPOCH FROM (end_time - start_time))/3600) " +
            "FROM timeslot " +
            "WHERE doctor_id = :doctorId AND is_active = true", nativeQuery = true)
    Double calculateTotalWeeklyHours(@Param("doctorId") Long doctorId);

    /**
     * Trouve créneaux ordonnés logiquement (Lundi->Dimanche)
     * SQL natif: CASE pour ordre personnalisé des jours
     */
    @Query(value = "SELECT t.* FROM timeslot t " +
            "WHERE t.doctor_id = :doctorId AND t.is_active = true " +
            "ORDER BY " +
            "  CASE t.day_of_week " +
            "    WHEN 'MONDAY' THEN 1 " +
            "    WHEN 'TUESDAY' THEN 2 " +
            "    WHEN 'WEDNESDAY' THEN 3 " +
            "    WHEN 'THURSDAY' THEN 4 " +
            "    WHEN 'FRIDAY' THEN 5 " +
            "    WHEN 'SATURDAY' THEN 6 " +
            "    WHEN 'SUNDAY' THEN 7 " +
            "  END, " +
            "  t.start_time", nativeQuery = true)
    List<TimeSlot> findByDoctorOrderedByWeekday(@Param("doctorId") Long doctorId);

    /**
     * Trouve créneaux disponibles après une certaine heure
     * JPQL: Comparaison LocalTime
     */
    @Query("SELECT t FROM TimeSlot t WHERE " +
            "t.doctor = :doctor AND " +
            "t.dayOfWeek = :dayOfWeek AND " +
            "t.startTime >= :afterTime AND " +
            "t.isActive = true " +
            "ORDER BY t.startTime")
    List<TimeSlot> findSlotsAfterTime(@Param("doctor") Doctor doctor,
            @Param("dayOfWeek") TimeSlot.DayOfWeek dayOfWeek,
            @Param("afterTime") LocalTime afterTime);
}