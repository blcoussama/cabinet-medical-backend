package com.cabinet.medical.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * LoginResponse - DTO pour la réponse de connexion
 *
 * UTILISATION:
 * - Retourné après login réussi (UC-P02, UC-D01, UC-A01)
 * - Retourné après inscription réussie (UC-P01)
 *
 * CONTENU:
 * - JWT token (pour authentification des requêtes suivantes)
 * - Informations utilisateur (id, email, role, nom)
 *
 * FLOW:
 * 1. Client envoie LoginRequest
 * 2. Backend valide email/password
 * 3. Backend génère JWT token
 * 4. Backend retourne LoginResponse
 * 5. Client stocke token et infos user
 * 6. Client envoie token dans Header: "Authorization: Bearer {token}"
 *
 * EXEMPLE JSON:
 * {
 * "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
 * "userId": 1,
 * "email": "jean@gmail.com",
 * "firstName": "Jean",
 * "lastName": "Dupont",
 * "role": "PATIENT",
 * "patientId": 1,
 * "doctorId": null
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {

    /**
     * JWT Token
     *
     * UTILISATION:
     * Android stocke ce token et l'envoie dans toutes les requêtes:
     * Header: Authorization: Bearer {token}
     *
     * CONTENU DU TOKEN (encodé):
     * - userId
     * - email
     * - role
     * - expiration (ex: 24h)
     *
     * EXEMPLE:
     * "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOjEsImVtYWlsIjoiamVhbkBnbWFpbC5jb20iLCJyb2xlIjoiUEFUSUVOVCIsImlhdCI6MTYxNjIzOTAyMiwiZXhwIjoxNjE2MzI1NDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"
     */
    private String token;

    /**
     * ID de l'utilisateur dans la table users
     */
    private Long userId;

    /**
     * Email de l'utilisateur (login)
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
     * Rôle de l'utilisateur
     *
     * VALEURS POSSIBLES:
     * - "PATIENT"
     * - "DOCTOR"
     * - "ADMIN"
     *
     * UTILISATION:
     * Android utilise ce rôle pour afficher l'écran approprié:
     * - PATIENT → Home Patient (SCR-P02)
     * - DOCTOR → Home Doctor (SCR-D01)
     * - ADMIN → Dashboard Admin (SCR-A01)
     */
    private String role;

    /**
     * ID du patient (si role=PATIENT)
     *
     * UTILISATION:
     * Permet à Android de faire des requêtes directes:
     * GET /api/appointments/patient/{patientId}
     *
     * VALEURS:
     * - Non-null si role=PATIENT
     * - null si role=DOCTOR ou ADMIN
     */
    private Long patientId;

    /**
     * ID du médecin (si role=DOCTOR)
     *
     * UTILISATION:
     * Permet à Android de faire des requêtes directes:
     * GET /api/appointments/doctor/{doctorId}
     * GET /api/timeslots/doctor/{doctorId}
     *
     * VALEURS:
     * - Non-null si role=DOCTOR
     * - null si role=PATIENT ou ADMIN
     */
    private Long doctorId;
}
