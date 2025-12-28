package com.cabinet.medical.repository;

import com.cabinet.medical.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

/**
 * Repository UserRepository
 * Interface pour accéder aux données de la table "users"
 * Spring Data JPA génère automatiquement l'implémentation!
 */
@Repository // Indique que c'est un composant Repository Spring
public interface UserRepository extends JpaRepository<User, Long> {
    // JpaRepository<User, Long>
    // │ │
    // │ └── Type de la clé primaire (Long id)
    // └──────── Type de l'entité (User)

    // ═══════════════════════════════════════════════════════════
    // MÉTHODES CRUD HÉRITÉES (automatiques, pas besoin de code!)
    // ═══════════════════════════════════════════════════════════
    // save(User user) → INSERT ou UPDATE
    // findById(Long id) → SELECT * FROM users WHERE id = ?
    // findAll() → SELECT * FROM users
    // deleteById(Long id) → DELETE FROM users WHERE id = ?
    // count() → SELECT COUNT(*) FROM users
    // existsById(Long id) → SELECT COUNT(*) > 0 FROM users WHERE id = ?

    // ═══════════════════════════════════════════════════════════
    // MÉTHODES PERSONNALISÉES (Spring génère SQL automatiquement!)
    // ═══════════════════════════════════════════════════════════

    /**
     * Trouve un utilisateur par email
     * Convention naming: findBy + NomChamp
     * SQL généré: SELECT * FROM users WHERE email = ?
     */
    Optional<User> findByEmail(String email);

    /**
     * Vérifie si un email existe déjà
     * Convention: existsBy + NomChamp
     * SQL généré: SELECT COUNT(*) > 0 FROM users WHERE email = ?
     */
    boolean existsByEmail(String email);

    /**
     * Trouve tous les utilisateurs par rôle
     * SQL généré: SELECT * FROM users WHERE role = ?
     */
    List<User> findByRole(User.Role role);

    /**
     * Trouve tous les utilisateurs actifs par rôle
     * SQL généré: SELECT * FROM users WHERE role = ? AND is_active = ?
     */
    List<User> findByRoleAndIsActive(User.Role role, Boolean isActive);

    /**
     * Trouve tous les utilisateurs actifs
     * SQL généré: SELECT * FROM users WHERE is_active = true
     */
    List<User> findByIsActiveTrue();

    /**
     * Trouve utilisateur par email ET actif
     * SQL généré: SELECT * FROM users WHERE email = ? AND is_active = true
     */
    Optional<User> findByEmailAndIsActiveTrue(String email);
}