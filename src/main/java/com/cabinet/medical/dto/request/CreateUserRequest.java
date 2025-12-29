package com.cabinet.medical.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CreateUserRequest - DTO pour créer un utilisateur (Admin uniquement)
 *
 * UTILISATION:
 * - UC-A04: Admin crée un patient
 * - UC-A05: Admin crée un médecin
 * - Admin peut aussi créer un admin
 *
 * ENDPOINTS:
 * POST /api/admin/users/patient
 * POST /api/admin/users/doctor
 * POST /api/admin/users/admin
 *
 * DIFFÉRENCE AVEC RegisterRequest:
 * - RegisterRequest: Patient s'inscrit lui-même (public)
 * - CreateUserRequest: Admin crée n'importe quel utilisateur (protégé)
 *
 * VALIDATION:
 * - Email unique
 * - Password minimum 6 caractères
 * - Specialty obligatoire SI role=DOCTOR
 *
 * EXEMPLE JSON (créer médecin):
 * {
 * "email": "martin@cabinet.com",
 * "password": "password123",
 * "firstName": "Martin",
 * "lastName": "Durand",
 * "phone": "0612345678",
 * "specialty": "Cardiologue"
 * }
 *
 * EXEMPLE JSON (créer patient):
 * {
 * "email": "marie@gmail.com",
 * "password": "password123",
 * "firstName": "Marie",
 * "lastName": "Martin",
 * "phone": "0698765432",
 * "specialty": null
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {

    /**
     * Email de l'utilisateur (login)
     */
    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    private String email;

    /**
     * Mot de passe en clair
     * Sera hashé en BCrypt côté Backend
     */
    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères")
    private String password;

    /**
     * Prénom
     */
    @NotBlank(message = "Le prénom est obligatoire")
    private String firstName;

    /**
     * Nom
     */
    @NotBlank(message = "Le nom est obligatoire")
    private String lastName;

    /**
     * Téléphone (optionnel)
     * Format: 10 chiffres
     */
    @Pattern(regexp = "^[0-9]{10}$", message = "Le téléphone doit contenir 10 chiffres")
    private String phone;

    /**
     * Spécialité du médecin (optionnel)
     *
     * UTILISATION:
     * - Obligatoire si Admin crée un DOCTOR
     * - null si Admin crée un PATIENT ou ADMIN
     *
     * EXEMPLES:
     * - "Cardiologue"
     * - "Pédiatre"
     * - "Généraliste"
     * - "Dermatologue"
     * - null (si patient)
     */
    private String specialty;
}
