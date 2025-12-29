package com.cabinet.medical.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * CreateTimeSlotRequest - DTO pour créer un créneau horaire
 *
 * UTILISATION:
 * - UC-D02: Doctor crée ses créneaux horaires
 * - UC-A08: Admin crée créneaux pour n'importe quel médecin
 *
 * ENDPOINT:
 * POST /api/timeslots
 *
 * VALIDATION:
 * - dayOfWeek obligatoire (MONDAY, TUESDAY, etc.)
 * - startTime obligatoire
 * - endTime obligatoire
 * - endTime > startTime (vérifié par le Service)
 * - Pas de chevauchement (vérifié par le Service)
 * - UNIQUE(doctorId, dayOfWeek, startTime) vérifié par DB
 *
 * FLOW DOCTOR:
 * 1. Doctor sélectionne jour de la semaine
 * 2. Doctor entre heure début et heure fin
 * 3. Android envoie CreateTimeSlotRequest
 * 4. Backend vérifie pas de chevauchement
 * 5. Backend crée TimeSlot
 * 6. Backend retourne TimeSlotResponse
 *
 * FLOW ADMIN:
 * 1. Admin sélectionne médecin (doctorId)
 * 2. Admin sélectionne jour et heures
 * 3. Android envoie CreateTimeSlotRequest
 * 4. Backend crée TimeSlot pour ce médecin
 * 5. Backend retourne TimeSlotResponse
 *
 * EXEMPLE JSON:
 * {
 * "doctorId": 1,
 * "dayOfWeek": "MONDAY",
 * "startTime": "09:00:00",
 * "endTime": "12:00:00"
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTimeSlotRequest {

    /**
     * ID du médecin (optionnel pour Doctor, obligatoire pour Admin)
     *
     * UTILISATION:
     * - Doctor: Backend utilise doctorId du JWT token (ignoré si fourni)
     * - Admin: Doit fournir doctorId du médecin
     *
     * VALIDATION:
     * - Existence du médecin vérifiée par le Service
     */
    private Long doctorId;

    /**
     * Jour de la semaine
     *
     * VALIDATION:
     * - @NotNull: Obligatoire
     *
     * VALEURS ACCEPTÉES:
     * - MONDAY
     * - TUESDAY
     * - WEDNESDAY
     * - THURSDAY
     * - FRIDAY
     * - SATURDAY
     * - SUNDAY
     *
     * FORMAT JSON:
     * "dayOfWeek": "MONDAY"
     */
    @NotNull(message = "Le jour de la semaine est obligatoire")
    private DayOfWeek dayOfWeek;

    /**
     * Heure de début du créneau
     *
     * VALIDATION:
     * - @NotNull: Obligatoire
     * - Doit être < endTime (vérifié par le Service)
     *
     * FORMAT:
     * "HH:mm:ss" ou "HH:mm"
     *
     * EXEMPLES VALIDES:
     * - "09:00:00" ou "09:00"
     * - "14:30:00" ou "14:30"
     * - "08:00:00" ou "08:00"
     *
     * EXEMPLES INVALIDES:
     * - null
     * - "25:00:00" (heure invalide)
     * - "9:00" (format incorrect, doit être 09:00)
     */
    @NotNull(message = "L'heure de début est obligatoire")
    private LocalTime startTime;

    /**
     * Heure de fin du créneau
     *
     * VALIDATION:
     * - @NotNull: Obligatoire
     * - Doit être > startTime (vérifié par le Service)
     *
     * FORMAT:
     * "HH:mm:ss" ou "HH:mm"
     *
     * EXEMPLES VALIDES:
     * - "12:00:00" ou "12:00"
     * - "18:00:00" ou "18:00"
     * - "13:30:00" ou "13:30"
     *
     * RÈGLE:
     * endTime DOIT être après startTime
     * Exemple: startTime=09:00, endTime=12:00 ✅
     * Exemple: startTime=14:00, endTime=13:00 ❌
     */
    @NotNull(message = "L'heure de fin est obligatoire")
    private LocalTime endTime;
}
