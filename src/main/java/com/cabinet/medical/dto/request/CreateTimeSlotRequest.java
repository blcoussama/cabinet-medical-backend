package com.cabinet.medical.dto.request;

import com.cabinet.medical.entity.TimeSlot;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalTime;

/**
 * DTO CreateTimeSlotRequest
 * Création d'un créneau horaire par un médecin
 * Validation stricte pour éviter chevauchements
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateTimeSlotRequest {

    /**
     * ID du médecin (extrait du JWT token)
     */
    @NotNull(message = "L'ID du médecin est obligatoire")
    private Long doctorId;

    /**
     * Jour de la semaine
     */
    @NotNull(message = "Le jour de la semaine est obligatoire")
    private TimeSlot.DayOfWeek dayOfWeek;

    /**
     * Heure de début
     */
    @NotNull(message = "L'heure de début est obligatoire")
    private LocalTime startTime;

    /**
     * Heure de fin
     */
    @NotNull(message = "L'heure de fin est obligatoire")
    private LocalTime endTime;

    /**
     * Durée consultation en minutes
     * Par défaut: 30 minutes
     */
    @NotNull(message = "La durée est obligatoire")
    @Min(value = 15, message = "La durée minimale est 15 minutes")
    @Max(value = 120, message = "La durée maximale est 120 minutes")
    @Builder.Default
    private Integer duration = 30;
}