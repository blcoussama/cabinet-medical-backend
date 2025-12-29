package com.cabinet.medical.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CancelAppointmentRequest - DTO pour annuler un rendez-vous
 *
 * UTILISATION:
 * - UC-P08: Patient annule son RDV
 * - UC-D06: Doctor annule un RDV
 * - UC-A11: Admin annule un RDV
 *
 * ENDPOINT:
 * DELETE /api/appointments/{id}
 * ou
 * POST /api/appointments/{id}/cancel
 *
 * VALIDATION:
 * - cancellationReason optionnel (mais recommandé)
 * - cancelledBy déterminé automatiquement par le Backend (via JWT token)
 *
 * FLOW:
 * 1. Client envoie CancelAppointmentRequest (optionnel)
 * 2. Backend charge Appointment
 * 3. Backend détermine cancelledBy via JWT token (PATIENT, DOCTOR, ADMIN)
 * 4. Backend met à jour Appointment:
 * - status = CANCELLED
 * - cancelledBy = role de l'utilisateur
 * - cancellationReason = raison fournie
 * 5. Backend crée notification d'annulation
 * 6. Backend retourne AppointmentResponse
 *
 * EXEMPLE JSON:
 * {
 * "cancellationReason": "Empêchement de dernière minute"
 * }
 *
 * EXEMPLE JSON (sans raison):
 * {
 * "cancellationReason": null
 * }
 *
 * OU body vide: {}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CancelAppointmentRequest {

    /**
     * Raison de l'annulation (optionnel mais recommandé)
     *
     * VALIDATION:
     * - @Size(max=500): Maximum 500 caractères
     *
     * EXEMPLES:
     * - "Empêchement de dernière minute"
     * - "Problème de santé résolu"
     * - "Indisponibilité du médecin"
     * - null (optionnel)
     *
     * UTILISATION:
     * Permet de tracer pourquoi le RDV a été annulé
     * Utile pour statistiques et amélioration du service
     */
    @Size(max = 500, message = "La raison d'annulation ne peut pas dépasser 500 caractères")
    private String cancellationReason;
}
