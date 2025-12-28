package com.cabinet.medical.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalTime;

/**
 * DTO UpdateTimeSlotRequest
 * Modification d'un créneau horaire existant
 * Tous champs optionnels (seulement les modifiés sont envoyés)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateTimeSlotRequest {

    /**
     * Nouvelle heure de début (optionnelle)
     */
    private LocalTime startTime;

    /**
     * Nouvelle heure de fin (optionnelle)
     */
    private LocalTime endTime;

    /**
     * Nouvelle durée (optionnelle)
     */
    @Min(value = 15, message = "La durée minimale est 15 minutes")
    @Max(value = 120, message = "La durée maximale est 120 minutes")
    private Integer duration;

    /**
     * Activer/désactiver créneau (optionnel)
     */
    private Boolean isActive;
}
