package com.cabinet.medical.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * RegisterRequest - DTO pour l'inscription d'un patient
 *
 * UTILISATION:
 * - UC-P01: Créer un compte patient
 *
 * ENDPOINT:
 * POST /api/auth/register
 *
 * VALIDATION:
 * - Email unique et format valide
 * - Password minimum 6 caractères
 * - FirstName et LastName obligatoires
 * - Phone optionnel mais format validé si fourni
 *
 * FLOW:
 * 1. Android envoie RegisterRequest
 * 2. Backend valide données
 * 3. Crée User (role=PATIENT)
 * 4. Crée Patient (lié à User)
 * 5. Retourne LoginResponse (avec JWT token)
 *
 * EXEMPLE JSON:
 * {
 * "email": "jean@gmail.com",
 * "password": "password123",
 * "firstName": "Jean",
 * "lastName": "Dupont",
 * "phone": "0612345678"
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    /**
     * Email de l'utilisateur (sera le login)
     *
     * VALIDATION:
     * - @NotBlank: Obligatoire
     * - @Email: Format email valide
     * - Unicité vérifiée par le Service (pas ici)
     */
    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    private String email;

    /**
     * Mot de passe en clair
     *
     * VALIDATION:
     * - @NotBlank: Obligatoire
     * - @Size(min=6): Minimum 6 caractères
     *
     * SÉCURITÉ:
     * Le password sera hashé en BCrypt avant stockage en DB
     * JAMAIS stocké en clair !
     */
    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères")
    private String password;

    /**
     * Prénom de l'utilisateur
     *
     * VALIDATION:
     * - @NotBlank: Obligatoire
     */
    @NotBlank(message = "Le prénom est obligatoire")
    private String firstName;

    /**
     * Nom de famille de l'utilisateur
     *
     * VALIDATION:
     * - @NotBlank: Obligatoire
     */
    @NotBlank(message = "Le nom est obligatoire")
    private String lastName;

    /**
     * Numéro de téléphone (optionnel)
     *
     * VALIDATION:
     * - @Pattern: Si fourni, doit être 10 chiffres
     *
     * EXEMPLES VALIDES:
     * - "0612345678"
     * - "0123456789"
     * - null (optionnel)
     *
     * EXEMPLES INVALIDES:
     * - "123" (trop court)
     * - "06 12 34 56 78" (espaces)
     * - "06-12-34-56-78" (tirets)
     */
    @Pattern(regexp = "^[0-9]{10}$", message = "Le téléphone doit contenir 10 chiffres (ex: 0612345678)")
    private String phone;
}
