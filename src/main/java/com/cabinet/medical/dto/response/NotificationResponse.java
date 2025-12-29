package com.cabinet.medical.dto.response;

import com.cabinet.medical.entity.Notification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * NotificationResponse - DTO pour les informations d'une notification
 *
 * UTILISATION:
 * - UC-P09: Patient recevoir notifications/rappels
 * - Affichage historique notifications
 * - Liste notifications en attente
 *
 * CONTENU:
 * - Informations notification (type, message, status envoi)
 * - Informations rendez-vous lié
 * - Date d'envoi (si envoyée)
 *
 * TYPES:
 * - CONFIRMATION: Envoyée immédiatement lors création RDV
 * - REMINDER: Envoyée automatiquement 24h avant RDV
 *
 * EXEMPLE JSON:
 * {
 * "id": 1,
 * "appointmentId": 1,
 * "userId": 1,
 * "userName": "Jean Dupont",
 * "type": "CONFIRMATION",
 * "message": "Votre rendez-vous avec Dr. Martin Durand (Cardiologue) est
 * confirmé pour le 30/12/2025 à 14:00.",
 * "sentAt": "2025-12-29T10:00:00",
 * "isSent": true,
 * "createdAt": "2025-12-29T10:00:00"
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {

    /**
     * ID de la notification
     */
    private Long id;

    /**
     * ID du rendez-vous lié
     */
    private Long appointmentId;

    /**
     * ID de l'utilisateur destinataire
     */
    private Long userId;

    /**
     * Nom complet du destinataire
     */
    private String userName;

    /**
     * Type de notification
     *
     * VALEURS:
     * - "CONFIRMATION": Confirmation immédiate lors création RDV
     * - "REMINDER": Rappel automatique 24h avant RDV
     */
    private String type;

    /**
     * Message de la notification
     *
     * EXEMPLES CONFIRMATION:
     * "Votre rendez-vous avec Dr. Martin Durand (Cardiologue) est confirmé pour le
     * 30/12/2025 à 14:00."
     *
     * EXEMPLES REMINDER:
     * "Rappel: Vous avez rendez-vous demain (30/12/2025) à 14:00 avec Dr. Martin
     * Durand (Cardiologue)."
     */
    private String message;

    /**
     * Date et heure d'envoi de la notification
     *
     * VALEURS:
     * - Non-null: Notification déjà envoyée
     * - null: Notification en attente d'envoi
     */
    private LocalDateTime sentAt;

    /**
     * Indicateur si la notification a été envoyée
     *
     * CALCUL:
     * isSent = (sentAt != null)
     */
    private boolean isSent;

    /**
     * Date de création de la notification
     */
    private LocalDateTime createdAt;

    // ═══════════════════════════════════════════════════════════
    // MÉTHODE DE CONVERSION
    // ═══════════════════════════════════════════════════════════

    /**
     * Convertit une entité Notification en NotificationResponse DTO
     *
     * UTILISATION:
     * Notification notification = notificationRepository.findById(1).orElseThrow();
     * NotificationResponse response = NotificationResponse.from(notification);
     *
     * @param notification L'entité Notification (avec User chargé)
     * @return NotificationResponse DTO
     */
    public static NotificationResponse from(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .appointmentId(notification.getAppointment().getId())
                .userId(notification.getUser().getId())
                .userName(notification.getUser().getFirstName() + " " +
                        notification.getUser().getLastName())
                .type(notification.getType().name())
                .message(notification.getMessage())
                .sentAt(notification.getSentAt())
                .isSent(notification.isSent())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
