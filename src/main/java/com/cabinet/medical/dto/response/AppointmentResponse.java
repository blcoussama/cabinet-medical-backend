package com.cabinet.medical.dto.response;

import com.cabinet.medical.entity.Appointment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * DTO AppointmentResponse
 * Informations complètes d'un rendez-vous
 * Inclut données Patient + Doctor simplifiées
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentResponse {

    // ═══════════════════════════════════════════════════════════
    // INFORMATIONS RENDEZ-VOUS
    // ═══════════════════════════════════════════════════════════

    /**
     * ID du rendez-vous
     */
    private Long id;

    /**
     * Date et heure du RDV
     */
    private LocalDateTime dateTime;

    /**
     * Durée en minutes
     */
    private Integer duration;

    /**
     * Motif de consultation
     */
    private String reason;

    /**
     * Statut du RDV
     */
    private Appointment.AppointmentStatus status;

    /**
     * Qui a annulé (si applicable)
     */
    private Appointment.CancelledBy cancelledBy;

    /**
     * Raison d'annulation (si applicable)
     */
    private String cancellationReason;

    /**
     * Date de création
     */
    private LocalDateTime createdAt;

    /**
     * Dernière modification
     */
    private LocalDateTime updatedAt;

    // ═══════════════════════════════════════════════════════════
    // INFORMATIONS PATIENT (simplifiées)
    // ═══════════════════════════════════════════════════════════

    /**
     * ID du patient
     */
    private Long patientId;

    /**
     * Nom complet patient (firstName + lastName)
     * Ex: "Jean Dupont"
     */
    private String patientName;

    /**
     * Email patient
     */
    private String patientEmail;

    /**
     * Téléphone patient
     */
    private String patientPhone;

    // ═══════════════════════════════════════════════════════════
    // INFORMATIONS DOCTOR (simplifiées)
    // ═══════════════════════════════════════════════════════════

    /**
     * ID du médecin
     */
    private Long doctorId;

    /**
     * Nom complet médecin (Dr. firstName lastName)
     * Ex: "Dr. Marie Martin"
     */
    private String doctorName;

    /**
     * Spécialité du médecin
     */
    private String doctorSpecialty;

    /**
     * Email médecin
     */
    private String doctorEmail;

    /**
     * Téléphone médecin
     */
    private String doctorPhone;

    /**
     * Adresse cabinet
     */
    private String doctorOfficeAddress;
}
