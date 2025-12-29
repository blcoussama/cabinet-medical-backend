package com.cabinet.medical.dto.response;

import com.cabinet.medical.entity.TimeSlot;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * TimeSlotResponse - DTO pour les informations d'un créneau horaire
 *
 * UTILISATION:
 * - UC-P05: Patient voir créneaux disponibles d'un médecin
 * - UC-D02: Doctor gérer ses créneaux horaires
 * - UC-A08: Admin gérer créneaux de tous les médecins
 *
 * CONTENU:
 * - Informations créneau (jour, heure début, heure fin)
 * - Informations médecin (nom, spécialité)
 * - Durée calculée du créneau
 *
 * EXEMPLE JSON:
 * {
 * "id": 1,
 * "doctorId": 1,
 * "doctorName": "Dr. Martin Durand",
 * "doctorSpecialty": "Cardiologue",
 * "dayOfWeek": "MONDAY",
 * "dayOfWeekFr": "Lundi",
 * "startTime": "09:00:00",
 * "endTime": "12:00:00",
 * "durationMinutes": 180
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeSlotResponse {

    /**
     * ID du créneau horaire
     */
    private Long id;

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
     * Jour de la semaine (anglais)
     *
     * VALEURS:
     * - "MONDAY"
     * - "TUESDAY"
     * - "WEDNESDAY"
     * - "THURSDAY"
     * - "FRIDAY"
     * - "SATURDAY"
     * - "SUNDAY"
     */
    private String dayOfWeek;

    /**
     * Jour de la semaine (français)
     *
     * VALEURS:
     * - "Lundi"
     * - "Mardi"
     * - "Mercredi"
     * - "Jeudi"
     * - "Vendredi"
     * - "Samedi"
     * - "Dimanche"
     *
     * UTILISATION:
     * Pour affichage dans l'interface Android en français
     */
    private String dayOfWeekFr;

    /**
     * Heure de début du créneau
     * Format: "HH:mm:ss"
     *
     * EXEMPLES:
     * - "09:00:00" (9h du matin)
     * - "14:00:00" (14h - 2h de l'après-midi)
     * - "17:30:00" (17h30)
     */
    private LocalTime startTime;

    /**
     * Heure de fin du créneau
     * Format: "HH:mm:ss"
     *
     * EXEMPLES:
     * - "12:00:00" (12h - midi)
     * - "18:00:00" (18h - 6h du soir)
     * - "20:00:00" (20h - 8h du soir)
     */
    private LocalTime endTime;

    /**
     * Durée du créneau en minutes
     *
     * CALCUL:
     * durationMinutes = (endTime - startTime) en minutes
     *
     * EXEMPLES:
     * - startTime=09:00, endTime=12:00 → 180 minutes (3h)
     * - startTime=14:00, endTime=18:00 → 240 minutes (4h)
     * - startTime=09:00, endTime=09:30 → 30 minutes
     */
    private long durationMinutes;

    // ═══════════════════════════════════════════════════════════
    // MÉTHODE DE CONVERSION
    // ═══════════════════════════════════════════════════════════

    /**
     * Convertit une entité TimeSlot en TimeSlotResponse DTO
     *
     * UTILISATION:
     * TimeSlot timeSlot = timeSlotRepository.findById(1).orElseThrow();
     * TimeSlotResponse response = TimeSlotResponse.from(timeSlot);
     *
     * @param timeSlot L'entité TimeSlot (avec Doctor chargé)
     * @return TimeSlotResponse DTO
     */
    public static TimeSlotResponse from(TimeSlot timeSlot) {
        return TimeSlotResponse.builder()
                .id(timeSlot.getId())
                .doctorId(timeSlot.getDoctor().getId())
                .doctorName("Dr. " + timeSlot.getDoctor().getUser().getFirstName() + " " +
                        timeSlot.getDoctor().getUser().getLastName())
                .doctorSpecialty(timeSlot.getDoctor().getSpecialty())
                .dayOfWeek(timeSlot.getDayOfWeek().name())
                .dayOfWeekFr(getDayOfWeekInFrench(timeSlot.getDayOfWeek()))
                .startTime(timeSlot.getStartTime())
                .endTime(timeSlot.getEndTime())
                .durationMinutes(timeSlot.getDurationInMinutes())
                .build();
    }

    /**
     * Convertit DayOfWeek anglais en français
     *
     * @param dayOfWeek Le jour en anglais
     * @return Le jour en français
     */
    private static String getDayOfWeekInFrench(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> "Lundi";
            case TUESDAY -> "Mardi";
            case WEDNESDAY -> "Mercredi";
            case THURSDAY -> "Jeudi";
            case FRIDAY -> "Vendredi";
            case SATURDAY -> "Samedi";
            case SUNDAY -> "Dimanche";
        };
    }

    /**
     * Retourne une représentation textuelle du créneau
     *
     * UTILISATION:
     * String display = timeSlotResponse.getDisplayText();
     * → "Lundi 09:00 - 12:00 (3h)"
     *
     * @return Texte formaté du créneau
     */
    public String getDisplayText() {
        long hours = durationMinutes / 60;
        long minutes = durationMinutes % 60;
        String duration = hours + "h" + (minutes > 0 ? minutes : "");
        return dayOfWeekFr + " " + startTime + " - " + endTime + " (" + duration + ")";
    }
}
