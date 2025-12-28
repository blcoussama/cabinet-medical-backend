package com.cabinet.medical.repository;

import com.cabinet.medical.entity.Patient;
import com.cabinet.medical.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository PatientRepository
 * Interface pour accéder aux données de la table "patient"
 */
@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    // ═══════════════════════════════════════════════════════════
    // MÉTHODES SIMPLES (Query Methods)
    // ═══════════════════════════════════════════════════════════

    /**
     * Trouve un patient par son User associé
     * SQL généré: SELECT * FROM patient WHERE user_id = ?
     */
    Optional<Patient> findByUser(User user);

    /**
     * Trouve un patient par l'ID de son User
     * SQL généré: SELECT * FROM patient WHERE user_id = ?
     */
    Optional<Patient> findByUserId(Long userId);

    /**
     * Trouve patients nés après une certaine date
     * SQL généré: SELECT * FROM patient WHERE date_of_birth > ?
     */
    List<Patient> findByDateOfBirthAfter(LocalDate date);

    /**
     * Trouve patients nés avant une certaine date
     * SQL généré: SELECT * FROM patient WHERE date_of_birth < ?
     */
    List<Patient> findByDateOfBirthBefore(LocalDate date);

    // ═══════════════════════════════════════════════════════════
    // MÉTHODES AVANCÉES AVEC @Query (JPQL)
    // ═══════════════════════════════════════════════════════════

    /**
     * Trouve un patient par email (via relation User)
     * JPQL: Utilise les objets Java (pas SQL direct)
     * p.user.email = navigation dans les relations
     */
    @Query("SELECT p FROM Patient p WHERE p.user.email = :email")
    Optional<Patient> findByEmail(@Param("email") String email);

    /**
     * Trouve patients par nom ou prénom (via User)
     * JPQL avec LIKE pour recherche partielle
     * LOWER() pour ignorer casse (case-insensitive)
     */
    @Query("SELECT p FROM Patient p WHERE " +
            "LOWER(p.user.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(p.user.lastName) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Patient> searchByName(@Param("search") String search);

    /**
     * Compte patients actifs
     * JPQL: COUNT avec condition
     */
    @Query("SELECT COUNT(p) FROM Patient p WHERE p.user.isActive = true")
    long countActivePatients();

    /**
     * Trouve tous les patients actifs triés par nom
     * JPQL avec ORDER BY sur relation
     */
    @Query("SELECT p FROM Patient p WHERE p.user.isActive = true " +
            "ORDER BY p.user.lastName, p.user.firstName")
    List<Patient> findAllActiveOrderByName();

    /**
     * Trouve patients dans une tranche d'âge
     * SQL natif (nativeQuery = true) car calcul d'âge complexe en JPQL
     * EXTRACT(YEAR FROM age()) = fonction PostgreSQL
     */
    @Query(value = "SELECT p.* FROM patient p " +
            "JOIN users u ON p.user_id = u.id " +
            "WHERE EXTRACT(YEAR FROM age(p.date_of_birth)) BETWEEN :minAge AND :maxAge " +
            "AND u.is_active = true", nativeQuery = true)
    List<Patient> findByAgeBetween(@Param("minAge") int minAge,
            @Param("maxAge") int maxAge);
}