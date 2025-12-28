package com.cabinet.medical.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/**
 * DTO UpdateDoctorRequest
 * Mise à jour profil médecin
 * Tous champs optionnels (seulement les modifiés sont envoyés)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateDoctorRequest {

    /**
     * Nouvelle spécialité (optionnelle)
     */
    @Size(min = 2, max = 150, message = "La spécialité doit contenir entre 2 et 150 caractères")
    private String specialty;

    /**
     * Nouvelle adresse cabinet (optionnelle)
     */
    @Size(max = 500, message = "L'adresse ne peut dépasser 500 caractères")
    private String officeAddress;

    /**
     * Nouveau tarif consultation (optionnel)
     */
    @DecimalMin(value = "0.0", inclusive = true, message = "Le tarif doit être positif")
    private BigDecimal consultationFee;

    /**
     * Nouvelle biographie (optionnelle)
     */
    @Size(max = 5000, message = "La biographie ne peut dépasser 5000 caractères")
    private String bio;

    /**
     * Nouvelles années d'expérience (optionnel)
     */
    @Min(value = 0, message = "L'expérience ne peut pas être négative")
    private Integer yearsExperience;
}
