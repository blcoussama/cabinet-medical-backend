package com.cabinet.medical.repository;

import com.cabinet.medical.entity.Doctor;
import com.cabinet.medical.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository DoctorRepository
 * Interface pour accéder aux données de la table "doctor"
 */
@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    // ═══════════════════════════════════════════════════════════
    // MÉTHODES SIMPLES (Query Methods)
    // ═══════════════════════════════════════════════════════════

    /**
     * Trouve un médecin par son User associé
     * SQL généré: SELECT * FROM doctor WHERE user_id = ?
     */
    Optional<Doctor> findByUser(User user);

    /**
     * Trouve un médecin par l'ID de son User
     * SQL généré: SELECT * FROM doctor WHERE user_id = ?
     */
    Optional<Doctor> findByUserId(Long userId);

    /**
     * Trouve un médecin par numéro de licence (unique)
     * SQL généré: SELECT * FROM doctor WHERE license_number = ?
     */
    Optional<Doctor> findByLicenseNumber(String licenseNumber);

    /**
     * Vérifie si un numéro de licence existe déjà
     * SQL généré: SELECT COUNT(*) > 0 FROM doctor WHERE license_number = ?
     */
    boolean existsByLicenseNumber(String licenseNumber);

    /**
     * Trouve tous les médecins d'une spécialité
     * SQL généré: SELECT * FROM doctor WHERE specialty = ?
     */
    List<Doctor> findBySpecialty(String specialty);

    /**
     * Trouve médecins avec tarif inférieur ou égal à un montant
     * SQL généré: SELECT * FROM doctor WHERE consultation_fee <= ?
     */
    List<Doctor> findByConsultationFeeLessThanEqual(BigDecimal maxFee);

    /**
     * Trouve médecins avec années d'expérience minimales
     * SQL généré: SELECT * FROM doctor WHERE years_experience >= ?
     */
    List<Doctor> findByYearsExperienceGreaterThanEqual(Integer minYears);

    // ═══════════════════════════════════════════════════════════
    // MÉTHODES AVANCÉES AVEC @Query (JPQL)
    // ═══════════════════════════════════════════════════════════

    /**
     * Trouve un médecin par email (via relation User)
     * JPQL: Navigation dans relation
     */
    @Query("SELECT d FROM Doctor d WHERE d.user.email = :email")
    Optional<Doctor> findByEmail(@Param("email") String email);

    /**
     * Trouve médecins actifs par spécialité
     * JPQL: Condition sur relation + champ local
     */
    @Query("SELECT d FROM Doctor d WHERE d.specialty = :specialty AND d.user.isActive = true")
    List<Doctor> findActiveBySpecialty(@Param("specialty") String specialty);

    /**
     * Recherche médecins par nom ou spécialité (case-insensitive)
     * JPQL: Recherche multi-champs avec LIKE
     */
    @Query("SELECT d FROM Doctor d WHERE " +
            "LOWER(d.user.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(d.user.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(d.specialty) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Doctor> searchByNameOrSpecialty(@Param("search") String search);

    /**
     * Trouve tous les médecins actifs triés par spécialité puis nom
     * JPQL: ORDER BY multiple colonnes (relation + local)
     */
    @Query("SELECT d FROM Doctor d WHERE d.user.isActive = true " +
            "ORDER BY d.specialty, d.user.lastName, d.user.firstName")
    List<Doctor> findAllActiveOrderBySpecialtyAndName();

    /**
     * Compte médecins actifs par spécialité
     * JPQL: COUNT avec GROUP BY
     */
    @Query("SELECT d.specialty, COUNT(d) FROM Doctor d " +
            "WHERE d.user.isActive = true " +
            "GROUP BY d.specialty")
    List<Object[]> countActiveBySpecialty();

    /**
     * Trouve médecins avec critères multiples (filtre avancé)
     * JPQL: Combinaison AND avec paramètres optionnels gérés en Service
     */
    @Query("SELECT d FROM Doctor d WHERE " +
            "(:specialty IS NULL OR d.specialty = :specialty) AND " +
            "(:minFee IS NULL OR d.consultationFee >= :minFee) AND " +
            "(:maxFee IS NULL OR d.consultationFee <= :maxFee) AND " +
            "(:minExp IS NULL OR d.yearsExperience >= :minExp) AND " +
            "d.user.isActive = true " +
            "ORDER BY d.consultationFee")
    List<Doctor> findByFilters(@Param("specialty") String specialty,
            @Param("minFee") BigDecimal minFee,
            @Param("maxFee") BigDecimal maxFee,
            @Param("minExp") Integer minExp);

    /**
     * Trouve les spécialités disponibles (distinctes)
     * JPQL: DISTINCT pour liste unique
     */
    @Query("SELECT DISTINCT d.specialty FROM Doctor d " +
            "WHERE d.user.isActive = true " +
            "ORDER BY d.specialty")
    List<String> findDistinctSpecialties();

    /**
     * Statistiques: Tarif moyen par spécialité
     * SQL natif: Fonctions agrégation AVG avec formatage
     */
    @Query(value = "SELECT d.specialty, " +
            "       AVG(d.consultation_fee) as avg_fee, " +
            "       MIN(d.consultation_fee) as min_fee, " +
            "       MAX(d.consultation_fee) as max_fee, " +
            "       COUNT(*) as doctor_count " +
            "FROM doctor d " +
            "JOIN users u ON d.user_id = u.id " +
            "WHERE u.is_active = true " +
            "GROUP BY d.specialty " +
            "ORDER BY avg_fee DESC", nativeQuery = true)
    List<Object[]> getSpecialtyStatistics();

    /**
     * Top médecins par expérience dans une spécialité
     * JPQL: ORDER BY + LIMIT via Pageable (alternative)
     */
    @Query("SELECT d FROM Doctor d " +
            "WHERE d.specialty = :specialty AND d.user.isActive = true " +
            "ORDER BY d.yearsExperience DESC, d.user.lastName")
    List<Doctor> findTopBySpecialty(@Param("specialty") String specialty);
}