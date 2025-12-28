package com.cabinet.medical.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO LoginRequest
 * Données reçues de l'app Android lors du login
 * Validation automatique avec Jakarta Validation
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {

    /**
     * Email de l'utilisateur
     * Validation: format email valide + non vide
     */
    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format email invalide")
    private String email;

    /**
     * Mot de passe en clair (sera hashé côté backend)
     * Validation: non vide
     */
    @NotBlank(message = "Le mot de passe est obligatoire")
    private String password;
}
