package com.cabinet.medical.dto.response;

import com.cabinet.medical.entity.Notification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * DTO NotificationResponse
 * Informations d'une notification
 * Pour affichage dans app Android (liste notifications)
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
     * Type de notification
     */
    private Notification.NotificationType type;

    /**
     * Message de la notification
     */
    private String message;

    /**
     * Date/heure programmée pour envoi
     */
    private LocalDateTime scheduledFor;

    /**
     * Date/heure réelle d'envoi (null si pas encore envoyée)
     */
    private LocalDateTime sentAt;

    /**
     * Statut de la notification
     */
    private Notification.NotificationStatus status;

    /**
     * Date de création
     */
    private LocalDateTime createdAt;

    /**
     * ID du rendez-vous associé (peut être null pour notifs génériques)
     */
    private Long appointmentId;

    /**
     * Date/heure du RDV (si applicable)
     * Pour affichage rapide sans charger Appointment complet
     */
    private LocalDateTime appointmentDateTime;

    /**
     * Nom du médecin (si applicable)
     * Ex: "Dr. Marie Martin"
     */
    private String doctorName;
}
