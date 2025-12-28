package com.cabinet.medical.dto.request;

import com.cabinet.medical.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO RegisterRequest
 * Données reçues lors de l'inscription d'un nouvel utilisateur
 * Validation stricte pour sécurité
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {

    /**
     * Email unique de l'utilisateur
     */
    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format email invalide")
    private String email;

    /**
     * Mot de passe (sera hashé avec BCrypt)
     * Validation: 8-100 caractères
     */
    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 8, max = 100, message = "Le mot de passe doit contenir entre 8 et 100 caractères")
    private String password;

    /**
     * Confirmation du mot de passe
     * Validation égalité faite dans Service
     */
    @NotBlank(message = "La confirmation du mot de passe est obligatoire")
    private String confirmPassword;

    /**
     * Prénom de l'utilisateur
     */
    @NotBlank(message = "Le prénom est obligatoire")
    @Size(min = 2, max = 100, message = "Le prénom doit contenir entre 2 et 100 caractères")
    private String firstName;

    /**
     * Nom de famille de l'utilisateur
     */
    @NotBlank(message = "Le nom est obligatoire")
    @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
    private String lastName;

    /**
     * Numéro de téléphone (optionnel)
     * Format: 10 chiffres (ex: 0612345678)
     */
    @Pattern(regexp = "^(0[1-9]\\d{8})?$", message = "Format téléphone invalide (10 chiffres commençant par 0)")
    private String phone;

    /**
     * Rôle de l'utilisateur (PATIENT, DOCTOR, ADMIN)
     * Par défaut PATIENT si non spécifié
     */
    @NotNull(message = "Le rôle est obligatoire")
    private User.Role role;
}