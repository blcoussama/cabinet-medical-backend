package com.cabinet.medical.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * DTO CreateAppointmentRequest
 * Prise de rendez-vous par un patient
 * Validation stricte pour éviter conflits
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateAppointmentRequest {

    /**
     * ID du patient qui prend le RDV
     * Extrait du JWT token (pas envoyé par Android)
     */
    @NotNull(message = "L'ID du patient est obligatoire")
    private Long patientId;

    /**
     * ID du médecin choisi
     */
    @NotNull(message = "L'ID du médecin est obligatoire")
    private Long doctorId;

    /**
     * Date et heure souhaitées
     * Validation: doit être dans le futur
     */
    @NotNull(message = "La date et l'heure sont obligatoires")
    @Future(message = "Le rendez-vous doit être dans le futur")
    private LocalDateTime dateTime;

    /**
     * Durée en minutes (généralement issue de TimeSlot)
     * Par défaut: 30 minutes
     */
    @NotNull(message = "La durée est obligatoire")
    @Min(value = 15, message = "La durée minimale est 15 minutes")
    @Max(value = 120, message = "La durée maximale est 120 minutes")
    @Builder.Default
    private Integer duration = 30;

    /**
     * Motif de consultation (optionnel mais recommandé)
     */
    @Size(max = 500, message = "Le motif ne peut dépasser 500 caractères")
    private String reason;
}
