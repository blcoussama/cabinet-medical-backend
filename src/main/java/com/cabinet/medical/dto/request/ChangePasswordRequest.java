package com.cabinet.medical.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO ChangePasswordRequest
 * Changement de mot de passe (depuis profil utilisateur)
 * Nécessite ancien mot de passe pour sécurité
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChangePasswordRequest {

    /**
     * Mot de passe actuel (pour vérification)
     * Sécurité: empêche changement si session volée
     */
    @NotBlank(message = "Le mot de passe actuel est obligatoire")
    private String currentPassword;

    /**
     * Nouveau mot de passe
     */
    @NotBlank(message = "Le nouveau mot de passe est obligatoire")
    @Size(min = 8, max = 100, message = "Le mot de passe doit contenir entre 8 et 100 caractères")
    private String newPassword;

    /**
     * Confirmation nouveau mot de passe
     */
    @NotBlank(message = "La confirmation est obligatoire")
    private String confirmNewPassword;
}
