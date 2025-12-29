package com.cabinet.medical.dto.response;

import com.cabinet.medical.entity.Appointment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * AppointmentResponse - DTO pour les informations d'un rendez-vous
 *
 * UTILISATION:
 * - UC-P03: Patient consulte historique RDV
 * - UC-P06: Patient prend RDV (confirmation)
 * - UC-D03: Doctor consulte ses RDV
 * - UC-A09: Admin voir tous les RDV
 *
 * CONTENU:
 * - Informations RDV (date, heure, motif, status)
 * - Informations Patient (nom complet)
 * - Informations Doctor (nom complet, spécialité)
 * - Informations annulation (si annulé)
 *
 * EXEMPLE JSON:
 * {
 * "id": 1,
 * "patientId": 1,
 * "patientName": "Jean Dupont",
 * "patientPhone": "0612345678",
 * "doctorId": 1,
 * "doctorName": "Dr. Martin Durand",
 * "doctorSpecialty": "Cardiologue",
 * "dateTime": "2025-12-30T14:00:00",
 * "reason": "Consultation cardiaque",
 * "status": "PENDING",
 * "cancelledBy": null,
 * "cancellationReason": null,
 * "createdAt": "2025-12-29T10:00:00",
 * "updatedAt": "2025-12-29T10:00:00"
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentResponse {

    /**
     * ID du rendez-vous
     */
    private Long id;

    /**
     * ID du patient
     */
    private Long patientId;

    /**
     * Nom complet du patient
     * Format: "Prénom Nom"
     */
    private String patientName;

    /**
     * Email du patient
     */
    private String patientEmail;

    /**
     * Téléphone du patient
     */
    private String patientPhone;

    /**
     * ID du médecin
     */
    private Long doctorId;

    /**
     * Nom complet du médecin
     * Format: "Dr. Prénom Nom"
     */
    private String doctorName;

    /**
     * Spécialité du médecin
     */
    private String doctorSpecialty;

    /**
     * Date et heure du rendez-vous
     */
    private LocalDateTime dateTime;

    /**
     * Motif de consultation (optionnel)
     */
    private String reason;

    /**
     * Status du rendez-vous
     *
     * VALEURS:
     * - "PENDING": En attente de confirmation
     * - "CONFIRMED": Confirmé
     * - "CANCELLED": Annulé
     */
    private String status;

    /**
     * Qui a annulé le RDV (si status=CANCELLED)
     *
     * VALEURS:
     * - null (si pas annulé)
     * - "PATIENT": Annulé par le patient
     * - "DOCTOR": Annulé par le médecin
     * - "ADMIN": Annulé par l'admin
     */
    private String cancelledBy;

    /**
     * Raison de l'annulation (si annulé)
     */
    private String cancellationReason;

    /**
     * Date de création du RDV
     */
    private LocalDateTime createdAt;

    /**
     * Date de dernière modification
     */
    private LocalDateTime updatedAt;

    // ═══════════════════════════════════════════════════════════
    // MÉTHODE DE CONVERSION
    // ═══════════════════════════════════════════════════════════

    /**
     * Convertit une entité Appointment en AppointmentResponse DTO
     *
     * UTILISATION:
     * Appointment appointment = appointmentRepository.findById(1).orElseThrow();
     * AppointmentResponse response = AppointmentResponse.from(appointment);
     *
     * IMPORTANT:
     * Accède à appointment.getPatient().getUser() et
     * appointment.getDoctor().getUser()
     * pour récupérer les informations complètes
     *
     * @param appointment L'entité Appointment (avec Patient et Doctor chargés)
     * @return AppointmentResponse DTO
     */
    public static AppointmentResponse from(Appointment appointment) {
        return AppointmentResponse.builder()
                .id(appointment.getId())
                .patientId(appointment.getPatient().getId())
                .patientName(appointment.getPatient().getUser().getFirstName() + " " +
                        appointment.getPatient().getUser().getLastName())
                .patientEmail(appointment.getPatient().getUser().getEmail())
                .patientPhone(appointment.getPatient().getUser().getPhone())
                .doctorId(appointment.getDoctor().getId())
                .doctorName("Dr. " + appointment.getDoctor().getUser().getFirstName() + " " +
                        appointment.getDoctor().getUser().getLastName())
                .doctorSpecialty(appointment.getDoctor().getSpecialty())
                .dateTime(appointment.getDateTime())
                .reason(appointment.getReason())
                .status(appointment.getStatus().name())
                .cancelledBy(appointment.getCancelledBy() != null ? appointment.getCancelledBy().name() : null)
                .cancellationReason(appointment.getCancellationReason())
                .createdAt(appointment.getCreatedAt())
                .updatedAt(appointment.getUpdatedAt())
                .build();
    }
}
