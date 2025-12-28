package com.cabinet.medical.repository;

import com.cabinet.medical.entity.Notification;
import com.cabinet.medical.entity.Appointment;
import com.cabinet.medical.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository NotificationRepository
 * Interface pour accéder aux données de la table "notification"
 * Gère les notifications/rappels pour les rendez-vous
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // ═══════════════════════════════════════════════════════════
    // MÉTHODES SIMPLES (Query Methods)
    // ═══════════════════════════════════════════════════════════

    /**
     * Trouve toutes les notifications d'un utilisateur
     * SQL généré: SELECT * FROM notification WHERE user_id = ?
     */
    List<Notification> findByUser(User user);

    /**
     * Trouve toutes les notifications d'un utilisateur par son ID
     * SQL généré: SELECT * FROM notification WHERE user_id = ?
     */
    List<Notification> findByUserId(Long userId);

    /**
     * Trouve toutes les notifications d'un rendez-vous
     * SQL généré: SELECT * FROM notification WHERE appointment_id = ?
     */
    List<Notification> findByAppointment(Appointment appointment);

    /**
     * Trouve notifications par type
     * SQL généré: SELECT * FROM notification WHERE type = ?
     */
    List<Notification> findByType(Notification.NotificationType type);

    /**
     * Trouve notifications par statut
     * SQL généré: SELECT * FROM notification WHERE status = ?
     */
    List<Notification> findByStatus(Notification.NotificationStatus status);

    /**
     * Trouve notifications d'un utilisateur par statut
     * SQL généré: SELECT * FROM notification WHERE user_id = ? AND status = ?
     */
    List<Notification> findByUserAndStatus(User user,
            Notification.NotificationStatus status);

    /**
     * Trouve notifications d'un utilisateur par type
     * SQL généré: SELECT * FROM notification WHERE user_id = ? AND type = ?
     */
    List<Notification> findByUserAndType(User user,
            Notification.NotificationType type);

    // ═══════════════════════════════════════════════════════════
    // REQUÊTES MÉTIER (Envoi notifications)
    // ═══════════════════════════════════════════════════════════

    /**
     * Trouve notifications en attente d'envoi (pour job scheduler)
     * JPQL: PENDING et heure programmée dépassée
     * CRITIQUE: Job scheduler utilise cette méthode toutes les minutes
     */
    @Query("SELECT n FROM Notification n WHERE " +
            "n.status = 'PENDING' AND " +
            "n.scheduledFor <= :now " +
            "ORDER BY n.scheduledFor ASC")
    List<Notification> findPendingNotifications(@Param("now") LocalDateTime now);

    /**
     * Trouve notifications en attente pour un rendez-vous spécifique
     * JPQL: Utile pour annulation RDV (annuler notifs associées)
     */
    @Query("SELECT n FROM Notification n WHERE " +
            "n.appointment = :appointment AND " +
            "n.status = 'PENDING'")
    List<Notification> findPendingByAppointment(@Param("appointment") Appointment appointment);

    /**
     * Trouve notifications échouées pour retry
     * JPQL: FAILED et pas trop de tentatives
     * Scheduler peut réessayer envoi
     */
    @Query("SELECT n FROM Notification n WHERE " +
            "n.status = 'FAILED' AND " +
            "n.scheduledFor >= :minDate " +
            "ORDER BY n.scheduledFor ASC")
    List<Notification> findFailedForRetry(@Param("minDate") LocalDateTime minDate);

    /**
     * Compte notifications non lues d'un utilisateur
     * JPQL: SENT mais pas encore consultées (pour badge app)
     * Note: Nécessiterait champ "isRead" dans entité (futur)
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE " +
            "n.user = :user AND " +
            "n.status = 'SENT'")
    long countUnreadByUser(@Param("user") User user);

    /**
     * Trouve notifications récentes d'un utilisateur
     * JPQL: Dernières 30 jours, triées par date
     */
    @Query("SELECT n FROM Notification n WHERE " +
            "n.user = :user AND " +
            "n.createdAt >= :since " +
            "ORDER BY n.createdAt DESC")
    List<Notification> findRecentByUser(@Param("user") User user,
            @Param("since") LocalDateTime since);

    /**
     * Vérifie si notification déjà créée pour un RDV/type
     * Évite doublons (ex: 2 rappels 24h pour même RDV)
     */
    @Query("SELECT COUNT(n) > 0 FROM Notification n WHERE " +
            "n.appointment = :appointment AND " +
            "n.type = :type")
    boolean existsByAppointmentAndType(@Param("appointment") Appointment appointment,
            @Param("type") Notification.NotificationType type);

    // ═══════════════════════════════════════════════════════════
    // STATISTIQUES & MONITORING
    // ═══════════════════════════════════════════════════════════

    /**
     * Compte notifications par statut (dashboard admin)
     * JPQL: GROUP BY sur enum
     */
    @Query("SELECT n.status, COUNT(n) FROM Notification n GROUP BY n.status")
    List<Object[]> countByStatus();

    /**
     * Compte notifications par type (analytics)
     * JPQL: Voir types les plus utilisés
     */
    @Query("SELECT n.type, COUNT(n) FROM Notification n GROUP BY n.type")
    List<Object[]> countByType();

    /**
     * Taux de succès envoi notifications
     * SQL natif: Calcul pourcentage SENT vs total
     */
    @Query(value = "SELECT " +
            "  COUNT(*) FILTER (WHERE status = 'SENT') as sent_count, " +
            "  COUNT(*) FILTER (WHERE status = 'FAILED') as failed_count, " +
            "  COUNT(*) as total_count, " +
            "  CAST(COUNT(*) FILTER (WHERE status = 'SENT') AS FLOAT) / " +
            "  NULLIF(COUNT(*), 0) * 100 as success_rate " +
            "FROM notification " +
            "WHERE created_at >= :startDate", nativeQuery = true)
    Object[] getNotificationStats(@Param("startDate") LocalDateTime startDate);

    /**
     * Notifications échouées avec erreurs (debugging)
     * JPQL: FAILED avec message erreur non null
     */
    @Query("SELECT n FROM Notification n WHERE " +
            "n.status = 'FAILED' AND " +
            "n.errorMessage IS NOT NULL " +
            "ORDER BY n.createdAt DESC")
    List<Notification> findFailedWithErrors();

    /**
     * Notifications envoyées avec retard (monitoring qualité)
     * SQL natif: Comparaison scheduledFor vs sentAt
     * Retard si sentAt > scheduledFor + 5 minutes
     */
    @Query(value = "SELECT n.*, " +
            "       EXTRACT(EPOCH FROM (n.sent_at - n.scheduled_for)) / 60 as delay_minutes " +
            "FROM notification n " +
            "WHERE n.status = 'SENT' " +
            "  AND n.sent_at > n.scheduled_for + INTERVAL '5 minutes' " +
            "  AND n.sent_at >= :startDate " +
            "ORDER BY delay_minutes DESC " +
            "LIMIT :limit", nativeQuery = true)
    List<Object[]> findDelayedNotifications(@Param("startDate") LocalDateTime startDate,
            @Param("limit") int limit);

    /**
     * Nettoyer vieilles notifications (maintenance)
     * Supprime notifications > X jours pour optimiser DB
     * Note: À utiliser avec @Modifying dans Service
     */
    @Query("SELECT n FROM Notification n WHERE " +
            "n.status = 'SENT' AND " +
            "n.sentAt < :beforeDate")
    List<Notification> findOldSentNotifications(@Param("beforeDate") LocalDateTime beforeDate);
}