package com.cabinet.medical.dto.request;

import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

/**
 * DTO UpdatePatientRequest
 * Mise à jour dossier patient
 * Tous champs optionnels (seulement les modifiés sont envoyés)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdatePatientRequest {

    /**
     * Nouvelle date de naissance (optionnelle)
     */
    @Past(message = "La date de naissance doit être dans le passé")
    private LocalDate dateOfBirth;

    /**
     * Nouvelle adresse (optionnelle)
     */
    @Size(max = 500, message = "L'adresse ne peut dépasser 500 caractères")
    private String address;

    /**
     * Nouvel historique médical (optionnel)
     */
    @Size(max = 5000, message = "L'historique médical ne peut dépasser 5000 caractères")
    private String medicalHistory;
}