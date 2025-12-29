package com.cabinet.medical.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * CreateAppointmentRequest - DTO pour créer un rendez-vous
 *
 * UTILISATION:
 * - UC-P06: Patient prend un rendez-vous
 * - UC-A09: Admin crée un rendez-vous (optionnel)
 *
 * ENDPOINT:
 * POST /api/appointments
 *
 * VALIDATION:
 * - doctorId obligatoire
 * - dateTime obligatoire et dans le futur
 * - Créneau doit être disponible (vérifié par le Service)
 * - reason optionnel (max 500 caractères)
 *
 * FLOW:
 * 1. Patient sélectionne médecin (doctorId)
 * 2. Patient sélectionne date et heure (dateTime)
 * 3. Patient entre motif (reason)
 * 4. Android envoie CreateAppointmentRequest
 * 5. Backend vérifie disponibilité
 * 6. Backend crée Appointment (status=PENDING)
 * 7. Backend crée Notifications (CONFIRMATION + REMINDER)
 * 8. Backend retourne AppointmentResponse
 *
 * EXEMPLE JSON:
 * {
 * "doctorId": 1,
 * "dateTime": "2025-12-30T14:00:00",
 * "reason": "Consultation cardiaque"
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateAppointmentRequest {

    /**
     * ID du médecin
     *
     * VALIDATION:
     * - @NotNull: Obligatoire
     * - Existence vérifiée par le Service
     *
     * UTILISATION:
     * Backend trouve le Doctor par cet ID
     */
    @NotNull(message = "L'ID du médecin est obligatoire")
    private Long doctorId;

    /**
     * Date et heure du rendez-vous
     *
     * VALIDATION:
     * - @NotNull: Obligatoire
     * - @Future: Doit être dans le futur
     * - Créneau disponible vérifié par le Service
     *
     * FORMAT:
     * ISO 8601: "2025-12-30T14:00:00"
     *
     * EXEMPLES VALIDES:
     * - "2025-12-30T14:00:00" (30 déc 2025 à 14h)
     * - "2026-01-15T09:30:00" (15 jan 2026 à 9h30)
     *
     * EXEMPLES INVALIDES:
     * - "2024-12-29T10:00:00" (passé)
     * - null
     */
    @NotNull(message = "La date et l'heure sont obligatoires")
    @Future(message = "La date doit être dans le futur")
    private LocalDateTime dateTime;

    /**
     * Motif de consultation (optionnel)
     *
     * VALIDATION:
     * - @Size(max=500): Maximum 500 caractères
     *
     * EXEMPLES:
     * - "Consultation de suivi"
     * - "Douleurs thoraciques"
     * - "Contrôle annuel"
     * - null (optionnel)
     */
    @Size(max = 500, message = "Le motif ne peut pas dépasser 500 caractères")
    private String reason;
}
