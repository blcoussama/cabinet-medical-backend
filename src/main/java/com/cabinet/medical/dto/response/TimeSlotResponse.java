package com.cabinet.medical.dto.response;

import com.cabinet.medical.entity.TimeSlot;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalTime;
import java.time.LocalDateTime;

/**
 * DTO TimeSlotResponse
 * Informations d'un créneau horaire récurrent
 * Inclut infos Doctor simplifiées
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeSlotResponse {

    // ═══════════════════════════════════════════════════════════
    // INFORMATIONS CRÉNEAU
    // ═══════════════════════════════════════════════════════════

    /**
     * ID du créneau
     */
    private Long id;

    /**
     * Jour de la semaine
     */
    private TimeSlot.DayOfWeek dayOfWeek;

    /**
     * Heure de début
     */
    private LocalTime startTime;

    /**
     * Heure de fin
     */
    private LocalTime endTime;

    /**
     * Durée consultation en minutes
     */
    private Integer duration;

    /**
     * Créneau actif ou désactivé
     */
    private Boolean isActive;

    /**
     * Date de création
     */
    private LocalDateTime createdAt;

    // ═══════════════════════════════════════════════════════════
    // INFORMATIONS DOCTOR (simplifiées)
    // ═══════════════════════════════════════════════════════════

    /**
     * ID du médecin
     */
    private Long doctorId;

    /**
     * Nom complet médecin
     */
    private String doctorName;

    /**
     * Spécialité
     */
    private String doctorSpecialty;
}