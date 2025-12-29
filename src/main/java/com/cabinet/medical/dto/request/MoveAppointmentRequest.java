package com.cabinet.medical.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * MoveAppointmentRequest - DTO pour déplacer un rendez-vous (Admin uniquement)
 *
 * UTILISATION:
 * - UC-A12: Admin déplace un rendez-vous vers un autre médecin/créneau
 *
 * ENDPOINT:
 * POST /api/appointments/{id}/move
 *
 * DIFFÉRENCE AVEC UpdateAppointmentRequest:
 * - UpdateAppointmentRequest: Modifier dateTime/reason (même médecin)
 * - MoveAppointmentRequest: Changer médecin ET dateTime (déplacer complètement)
 *
 * VALIDATION:
 * - newDoctorId obligatoire
 * - newDateTime obligatoire et dans le futur
 * - Nouveau créneau disponible (vérifié par le Service)
 *
 * FLOW:
 * 1. Admin sélectionne RDV à déplacer
 * 2. Admin choisit nouveau médecin
 * 3. Admin choisit nouveau créneau
 * 4. Android envoie MoveAppointmentRequest
 * 5. Backend vérifie disponibilité nouveau créneau
 * 6. Backend met à jour Appointment (doctorId + dateTime)
 * 7. Backend crée notification au patient
 * 8. Backend retourne AppointmentResponse
 *
 * EXEMPLE JSON:
 * {
 * "newDoctorId": 2,
 * "newDateTime": "2025-12-31T15:00:00"
 * }
 *
 * CAS D'USAGE:
 * - Dr. Martin indisponible → Déplacer tous ses RDV vers Dr. Dupuis
 * - Patient veut changer de médecin
 * - Conflit de planning à résoudre
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MoveAppointmentRequest {

    /**
     * ID du nouveau médecin
     *
     * VALIDATION:
     * - @NotNull: Obligatoire
     * - Existence vérifiée par le Service
     *
     * UTILISATION:
     * Backend trouve le nouveau Doctor par cet ID
     *
     * EXEMPLE:
     * RDV initial: Dr. Martin (doctorId=1)
     * Déplacer vers: Dr. Dupuis (newDoctorId=2)
     */
    @NotNull(message = "L'ID du nouveau médecin est obligatoire")
    private Long newDoctorId;

    /**
     * Nouvelle date et heure du rendez-vous
     *
     * VALIDATION:
     * - @NotNull: Obligatoire
     * - @Future: Doit être dans le futur
     * - Nouveau créneau disponible vérifié par le Service
     *
     * FORMAT:
     * ISO 8601: "2025-12-31T15:00:00"
     *
     * EXEMPLES VALIDES:
     * - "2025-12-31T15:00:00" (31 déc 2025 à 15h)
     * - "2026-01-15T10:00:00" (15 jan 2026 à 10h)
     *
     * EXEMPLES INVALIDES:
     * - "2024-12-29T10:00:00" (passé)
     * - null
     */
    @NotNull(message = "La nouvelle date et heure sont obligatoires")
    @Future(message = "La nouvelle date doit être dans le futur")
    private LocalDateTime newDateTime;
}
