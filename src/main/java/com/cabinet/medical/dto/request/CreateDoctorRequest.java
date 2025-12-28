package com.cabinet.medical.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/**
 * DTO CreateDoctorRequest
 * Créer dossier médecin après inscription User
 * Utilisé lors register avec role=DOCTOR
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateDoctorRequest {

    /**
     * ID de l'utilisateur (déjà créé via RegisterRequest)
     */
    @NotNull(message = "L'ID utilisateur est obligatoire")
    private Long userId;

    /**
     * Spécialité médicale (obligatoire)
     */
    @NotBlank(message = "La spécialité est obligatoire")
    @Size(min = 2, max = 150, message = "La spécialité doit contenir entre 2 et 150 caractères")
    private String specialty;

    /**
     * Numéro de licence/RPPS (obligatoire et unique)
     */
    @NotBlank(message = "Le numéro de licence est obligatoire")
    @Size(min = 5, max = 100, message = "Le numéro de licence doit contenir entre 5 et 100 caractères")
    private String licenseNumber;

    /**
     * Adresse du cabinet (optionnelle)
     */
    @Size(max = 500, message = "L'adresse ne peut dépasser 500 caractères")
    private String officeAddress;

    /**
     * Tarif consultation (optionnel, peut être défini plus tard)
     * Validation: >= 0 si renseigné
     */
    @DecimalMin(value = "0.0", inclusive = true, message = "Le tarif doit être positif")
    private BigDecimal consultationFee;

    /**
     * Biographie (optionnelle)
     */
    @Size(max = 5000, message = "La biographie ne peut dépasser 5000 caractères")
    private String bio;

    /**
     * Années d'expérience (optionnel)
     * Validation: >= 0 si renseigné
     */
    @Min(value = 0, message = "L'expérience ne peut pas être négative")
    private Integer yearsExperience;
}
