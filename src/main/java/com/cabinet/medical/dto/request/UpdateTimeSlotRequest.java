package com.cabinet.medical.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * UpdateTimeSlotRequest - DTO pour modifier un créneau horaire
 *
 * UTILISATION:
 * - UC-D02: Doctor modifie ses créneaux horaires
 * - UC-A08: Admin modifie créneaux de n'importe quel médecin
 *
 * ENDPOINT:
 * PUT /api/timeslots/{id}
 *
 * VALIDATION:
 * - Tous les champs obligatoires (modification complète)
 * - endTime > startTime
 * - Pas de chevauchement avec autres créneaux
 *
 * FLOW:
 * 1. Client charge TimeSlot existant
 * 2. Client modifie jour/heures
 * 3. Android envoie UpdateTimeSlotRequest
 * 4. Backend vérifie pas de chevauchement
 * 5. Backend met à jour TimeSlot
 * 6. Backend retourne TimeSlotResponse
 *
 * EXEMPLE JSON:
 * {
 * "dayOfWeek": "TUESDAY",
 * "startTime": "14:00:00",
 * "endTime": "18:00:00"
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTimeSlotRequest {

    /**
     * Nouveau jour de la semaine
     *
     * VALIDATION:
     * - @NotNull: Obligatoire
     *
     * VALEURS:
     * MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
     */
    @NotNull(message = "Le jour de la semaine est obligatoire")
    private DayOfWeek dayOfWeek;

    /**
     * Nouvelle heure de début
     *
     * VALIDATION:
     * - @NotNull: Obligatoire
     * - Doit être < endTime
     */
    @NotNull(message = "L'heure de début est obligatoire")
    private LocalTime startTime;

    /**
     * Nouvelle heure de fin
     *
     * VALIDATION:
     * - @NotNull: Obligatoire
     * - Doit être > startTime
     */
    @NotNull(message = "L'heure de fin est obligatoire")
    private LocalTime endTime;
}
