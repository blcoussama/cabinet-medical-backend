package com.cabinet.medical.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * LoginRequest - DTO pour la requête de connexion
 *
 * UTILISATION:
 * - UC-P02: Patient se connecte
 * - UC-D01: Doctor se connecte
 * - UC-A01: Admin se connecte
 *
 * ENDPOINT:
 * POST /api/auth/login
 *
 * VALIDATION:
 * - Email obligatoire et format valide
 * - Password obligatoire
 *
 * EXEMPLE JSON:
 * {
 * "email": "jean@gmail.com",
 * "password": "password123"
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    /**
     * Email de l'utilisateur (login)
     *
     * VALIDATION:
     * - @NotBlank: Ne peut pas être vide
     * - @Email: Doit être un email valide
     *
     * EXEMPLES VALIDES:
     * - "jean@gmail.com"
     * - "martin@cabinet.com"
     *
     * EXEMPLES INVALIDES:
     * - "" (vide)
     * - "notanemail" (pas de @)
     * - null
     */
    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    private String email;

    /**
     * Mot de passe de l'utilisateur
     *
     * VALIDATION:
     * - @NotBlank: Ne peut pas être vide
     *
     * NOTE:
     * Le password sera hashé en BCrypt côté Backend
     * Le client envoie le password en clair (HTTPS requis en production!)
     */
    @NotBlank(message = "Le mot de passe est obligatoire")
    private String password;
}
