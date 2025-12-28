package com.cabinet.medical.dto.response;

import com.cabinet.medical.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * DTO UserResponse
 * Informations utilisateur renvoyées par l'API
 * Utilisé pour: profils, listes utilisateurs, recherches
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    /**
     * ID de l'utilisateur
     */
    private Long id;

    /**
     * Email de l'utilisateur
     */
    private String email;

    /**
     * Prénom
     */
    private String firstName;

    /**
     * Nom de famille
     */
    private String lastName;

    /**
     * Numéro de téléphone
     */
    private String phone;

    /**
     * Rôle (PATIENT, DOCTOR, ADMIN)
     */
    private User.Role role;

    /**
     * Compte actif ou désactivé
     */
    private Boolean isActive;

    /**
     * Date de création du compte
     */
    private LocalDateTime createdAt;

    /**
     * Dernière connexion
     */
    private LocalDateTime lastLoginAt;
}
