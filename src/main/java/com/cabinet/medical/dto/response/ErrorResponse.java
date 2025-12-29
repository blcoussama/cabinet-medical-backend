package com.cabinet.medical.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ErrorResponse - DTO pour les erreurs API
 *
 * UTILISATION:
 * - Erreurs de validation (@Valid)
 * - Erreurs métier (créneau occupé, email déjà utilisé, etc.)
 * - Erreurs techniques (500 Internal Server Error)
 * - Erreurs d'autorisation (403 Forbidden)
 * - Erreurs d'authentification (401 Unauthorized)
 *
 * STANDARDISATION:
 * Toutes les erreurs API retournent ce format uniforme
 * Facilite le traitement côté Android
 *
 * EXEMPLE JSON (Validation):
 * {
 * "timestamp": "2025-12-29T10:00:00",
 * "status": 400,
 * "error": "Bad Request",
 * "message": "Erreur de validation",
 * "path": "/api/appointments",
 * "errors": [
 * "L'email est obligatoire",
 * "Le mot de passe doit contenir au moins 6 caractères"
 * ]
 * }
 *
 * EXEMPLE JSON (Business Logic):
 * {
 * "timestamp": "2025-12-29T10:00:00",
 * "status": 409,
 * "error": "Conflict",
 * "message": "Ce créneau est déjà réservé",
 * "path": "/api/appointments",
 * "errors": null
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {

    /**
     * Date et heure de l'erreur
     *
     * UTILISATION:
     * Pour logs et debugging
     */
    private LocalDateTime timestamp;

    /**
     * Code HTTP status
     *
     * VALEURS COURANTES:
     * - 400: Bad Request (validation échouée)
     * - 401: Unauthorized (pas authentifié)
     * - 403: Forbidden (pas les permissions)
     * - 404: Not Found (ressource inexistante)
     * - 409: Conflict (conflit métier)
     * - 500: Internal Server Error (erreur serveur)
     */
    private int status;

    /**
     * Nom de l'erreur HTTP
     *
     * EXEMPLES:
     * - "Bad Request"
     * - "Unauthorized"
     * - "Forbidden"
     * - "Not Found"
     * - "Conflict"
     * - "Internal Server Error"
     */
    private String error;

    /**
     * Message principal de l'erreur
     *
     * EXEMPLES:
     * - "Erreur de validation"
     * - "Email déjà utilisé"
     * - "Créneau déjà réservé"
     * - "Utilisateur non trouvé"
     * - "Accès refusé"
     */
    private String message;

    /**
     * Chemin de l'API qui a généré l'erreur
     *
     * EXEMPLES:
     * - "/api/auth/login"
     * - "/api/appointments"
     * - "/api/users/123"
     */
    private String path;

    /**
     * Liste détaillée des erreurs (pour validation)
     *
     * UTILISATION:
     * - Erreurs de validation multiple (@Valid)
     * - null si erreur simple
     *
     * EXEMPLES:
     * [
     * "L'email est obligatoire",
     * "Le mot de passe doit contenir au moins 6 caractères",
     * "Le téléphone doit contenir 10 chiffres"
     * ]
     */
    private List<String> errors;

    // ═══════════════════════════════════════════════════════════
    // MÉTHODES FACTORY
    // ═══════════════════════════════════════════════════════════

    /**
     * Crée une ErrorResponse simple (1 message)
     *
     * UTILISATION:
     * return ErrorResponse.of(404, "Not Found", "Utilisateur non trouvé",
     * "/api/users/999");
     *
     * @param status  Code HTTP
     * @param error   Nom erreur HTTP
     * @param message Message principal
     * @param path    Chemin API
     * @return ErrorResponse
     */
    public static ErrorResponse of(int status, String error, String message, String path) {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status)
                .error(error)
                .message(message)
                .path(path)
                .errors(null)
                .build();
    }

    /**
     * Crée une ErrorResponse avec liste d'erreurs (validation)
     *
     * UTILISATION:
     * return ErrorResponse.of(400, "Bad Request", "Erreur de validation",
     * "/api/auth/register", validationErrors);
     *
     * @param status  Code HTTP
     * @param error   Nom erreur HTTP
     * @param message Message principal
     * @param path    Chemin API
     * @param errors  Liste erreurs détaillées
     * @return ErrorResponse
     */
    public static ErrorResponse of(int status, String error, String message, String path, List<String> errors) {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status)
                .error(error)
                .message(message)
                .path(path)
                .errors(errors)
                .build();
    }
}
