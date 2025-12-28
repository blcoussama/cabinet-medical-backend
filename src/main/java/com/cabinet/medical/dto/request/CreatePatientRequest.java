package com.cabinet.medical.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

/**
 * DTO CreatePatientRequest
 * Créer dossier patient après inscription User
 * Utilisé lors register avec role=PATIENT
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePatientRequest {

    /**
     * ID de l'utilisateur (déjà créé via RegisterRequest)
     * Créé automatiquement après User.save()
     */
    @NotNull(message = "L'ID utilisateur est obligatoire")
    private Long userId;

    /**
     * Date de naissance (optionnelle lors inscription)
     * Validation: doit être dans le passé
     */
    @Past(message = "La date de naissance doit être dans le passé")
    private LocalDate dateOfBirth;

    /**
     * Adresse (optionnelle)
     */
    @Size(max = 500, message = "L'adresse ne peut dépasser 500 caractères")
    private String address;

    /**
     * Historique médical (optionnel)
     * Allergies, maladies chroniques, etc.
     */
    @Size(max = 5000, message = "L'historique médical ne peut dépasser 5000 caractères")
    private String medicalHistory;
}