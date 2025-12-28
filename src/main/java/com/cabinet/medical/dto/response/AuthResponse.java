package com.cabinet.medical.dto.response;

import com.cabinet.medical.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO AuthResponse
 * Réponse renvoyée après login/register réussi
 * Contient JWT token + informations utilisateur
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    /**
     * JWT Token pour authentification
     * Format: "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
     * Durée validité: 24h (configurable)
     * Android stocke dans SharedPreferences
     */
    private String token;

    /**
     * Type de token (toujours "Bearer")
     * Pour header HTTP: "Authorization: Bearer {token}"
     */
    @Builder.Default
    private String tokenType = "Bearer";

    /**
     * ID de l'utilisateur
     */
    private Long userId;

    /**
     * Email de l'utilisateur
     */
    private String email;

    /**
     * Prénom de l'utilisateur
     */
    private String firstName;

    /**
     * Nom de l'utilisateur
     */
    private String lastName;

    /**
     * Rôle de l'utilisateur (PATIENT, DOCTOR, ADMIN)
     * Utilisé pour navigation app Android
     */
    private User.Role role;

    /**
     * Statut du compte (actif ou désactivé)
     */
    private Boolean isActive;
}