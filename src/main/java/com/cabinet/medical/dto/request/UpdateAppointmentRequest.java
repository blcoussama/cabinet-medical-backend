package com.cabinet.medical.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * DTO UpdateAppointmentRequest
 * Modification d'un rendez-vous existant
 * Tous champs optionnels (seulement les modifiés sont envoyés)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateAppointmentRequest {

    /**
     * Nouvelle date/heure (optionnelle)
     * Validation: doit être dans le futur
     */
    @Future(message = "Le rendez-vous doit être dans le futur")
    private LocalDateTime dateTime;

    /**
     * Nouvelle durée (optionnelle)
     */
    @Min(value = 15, message = "La durée minimale est 15 minutes")
    @Max(value = 120, message = "La durée maximale est 120 minutes")
    private Integer duration;

    /**
     * Nouveau motif (optionnel)
     */
    @Size(max = 500, message = "Le motif ne peut dépasser 500 caractères")
    private String reason;
}
