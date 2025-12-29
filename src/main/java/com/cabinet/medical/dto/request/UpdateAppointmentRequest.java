package com.cabinet.medical.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * UpdateAppointmentRequest - DTO pour modifier un rendez-vous
 *
 * UTILISATION:
 * - UC-P07: Patient modifie son RDV
 * - UC-D05: Doctor modifie un RDV
 * - UC-A10: Admin modifie un RDV
 *
 * ENDPOINT:
 * PUT /api/appointments/{id}
 *
 * VALIDATION:
 * - Tous les champs optionnels (modifier seulement ce qui change)
 * - dateTime doit être dans le futur si fourni
 * - Nouveau créneau doit être disponible (vérifié par le Service)
 *
 * FLOW:
 * 1. Client envoie UpdateAppointmentRequest avec champs à modifier
 * 2. Backend charge Appointment existant
 * 3. Backend met à jour champs fournis
 * 4. Backend vérifie disponibilité si dateTime changé
 * 5. Backend sauvegarde modifications
 * 6. Backend crée nouvelle notification si dateTime changé
 * 7. Backend retourne AppointmentResponse
 *
 * EXEMPLE JSON (modifier date seulement):
 * {
 * "dateTime": "2025-12-31T15:00:00",
 * "reason": null
 * }
 *
 * EXEMPLE JSON (modifier motif seulement):
 * {
 * "dateTime": null,
 * "reason": "Consultation de contrôle"
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAppointmentRequest {

    /**
     * Nouvelle date et heure (optionnel)
     *
     * VALIDATION:
     * - @Future: Doit être dans le futur si fourni
     * - Créneau disponible vérifié par le Service
     *
     * UTILISATION:
     * - Si fourni: Change la date/heure du RDV
     * - Si null: Garde la date/heure actuelle
     */
    @Future(message = "La nouvelle date doit être dans le futur")
    private LocalDateTime dateTime;

    /**
     * Nouveau motif de consultation (optionnel)
     *
     * VALIDATION:
     * - @Size(max=500): Maximum 500 caractères
     *
     * UTILISATION:
     * - Si fourni: Change le motif
     * - Si null: Garde le motif actuel
     */
    @Size(max = 500, message = "Le motif ne peut pas dépasser 500 caractères")
    private String reason;
}
