package com.cabinet.medical.controller;

import com.cabinet.medical.dto.request.LoginRequest;
import com.cabinet.medical.dto.request.RegisterRequest;
import com.cabinet.medical.dto.response.ApiResponse;
import com.cabinet.medical.dto.response.LoginResponse;
import com.cabinet.medical.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * AuthController - Contrôleur pour l'authentification
 *
 * ENDPOINTS:
 * - POST /api/auth/register : Inscription patient (UC-P01)
 * - POST /api/auth/login : Connexion (UC-P02, UC-D01, UC-A01)
 * - GET /api/auth/email-exists : Vérifier email disponible
 *
 * PERMISSIONS:
 * - register : PUBLIC (tout le monde peut créer un compte patient)
 * - login : PUBLIC (tout le monde peut se connecter)
 * - email-exists : PUBLIC (validation frontend)
 *
 * ANNOTATIONS:
 * - @RestController : Contrôleur REST (retourne JSON)
 * - @RequestMapping : Préfixe d'URL pour tous les endpoints
 * - @PostMapping : Endpoint POST
 * - @GetMapping : Endpoint GET
 * - @Valid : Validation automatique des DTOs
 * - @RequestBody : Corps de la requête (JSON → objet Java)
 * - @RequestParam : Paramètre de query string
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    /**
     * Constructeur avec injection de dépendances
     *
     * @param authService Service d'authentification
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Inscription d'un nouveau patient (UC-P01)
     *
     * ENDPOINT: POST /api/auth/register
     *
     * REQUEST BODY:
     * {
     * "email": "jean@gmail.com",
     * "password": "password123",
     * "firstName": "Jean",
     * "lastName": "Dupont",
     * "phone": "0612345678"
     * }
     *
     * RESPONSE 201 CREATED:
     * {
     * "userId": 1,
     * "patientId": 1,
     * "email": "jean@gmail.com",
     * "firstName": "Jean",
     * "lastName": "Dupont",
     * "role": "PATIENT",
     * "token": "TODO_JWT_TOKEN"
     * }
     *
     * ERREURS:
     * - 400 BAD REQUEST : Validation échouée (email invalide, champs vides, etc.)
     * Géré par GlobalExceptionHandler (MethodArgumentNotValidException)
     *
     * - 409 CONFLICT : Email déjà utilisé (RG-01)
     * Géré par GlobalExceptionHandler (EmailAlreadyExistsException)
     *
     * FLOW:
     * 1. @Valid valide automatiquement RegisterRequest
     * 2. AuthService.register() crée User + Patient
     * 3. Retourne LoginResponse avec 201 CREATED
     * 4. Si erreur → GlobalExceptionHandler intercepte
     *
     * @param request RegisterRequest (validé automatiquement par @Valid)
     * @return ResponseEntity<LoginResponse> avec status 201 CREATED
     */
    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        LoginResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Connexion utilisateur (UC-P02, UC-D01, UC-A01)
     *
     * ENDPOINT: POST /api/auth/login
     *
     * REQUEST BODY:
     * {
     * "email": "jean@gmail.com",
     * "password": "password123"
     * }
     *
     * RESPONSE 200 OK (Patient):
     * {
     * "userId": 1,
     * "patientId": 1,
     * "doctorId": null,
     * "email": "jean@gmail.com",
     * "firstName": "Jean",
     * "lastName": "Dupont",
     * "role": "PATIENT",
     * "token": "TODO_JWT_TOKEN"
     * }
     *
     * RESPONSE 200 OK (Doctor):
     * {
     * "userId": 2,
     * "patientId": null,
     * "doctorId": 1,
     * "email": "martin@doc.com",
     * "firstName": "Martin",
     * "lastName": "Durand",
     * "role": "DOCTOR",
     * "token": "TODO_JWT_TOKEN"
     * }
     *
     * ERREURS:
     * - 400 BAD REQUEST : Validation échouée (email/password vides)
     * Géré par GlobalExceptionHandler (MethodArgumentNotValidException)
     *
     * - 401 UNAUTHORIZED : Email ou mot de passe incorrect
     * Géré par GlobalExceptionHandler (InvalidCredentialsException)
     *
     * FLOW:
     * 1. @Valid valide automatiquement LoginRequest
     * 2. AuthService.login() vérifie credentials + charge role
     * 3. Retourne LoginResponse avec 200 OK
     * 4. Si erreur → GlobalExceptionHandler intercepte
     *
     * @param request LoginRequest (validé automatiquement par @Valid)
     * @return ResponseEntity<LoginResponse> avec status 200 OK
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Vérifier si un email existe déjà
     *
     * ENDPOINT: GET /api/auth/email-exists?email=test@example.com
     *
     * UTILISATION:
     * Validation frontend en temps réel lors de l'inscription
     *
     * EXEMPLE REQUÊTE:
     * GET /api/auth/email-exists?email=jean@gmail.com
     *
     * RESPONSE 200 OK (email existe):
     * {
     * "success": true,
     * "message": "Email déjà utilisé",
     * "data": true
     * }
     *
     * RESPONSE 200 OK (email disponible):
     * {
     * "success": true,
     * "message": "Email disponible",
     * "data": false
     * }
     *
     * UTILISATION FRONTEND:
     * - Lors de la saisie email dans formulaire inscription
     * - Afficher message "Email déjà utilisé" en temps réel
     * - Désactiver bouton "S'inscrire" si email existe
     *
     * @param email Email à vérifier (query parameter)
     * @return ResponseEntity<ApiResponse<Boolean>> avec status 200 OK
     */
    @GetMapping("/email-exists")
    public ResponseEntity<ApiResponse<Boolean>> emailExists(@RequestParam String email) {
        boolean exists = authService.emailExists(email);

        String message = exists ? "Email déjà utilisé" : "Email disponible";
        ApiResponse<Boolean> response = ApiResponse.<Boolean>builder()
                .success(true)
                .message(message)
                .data(exists)
                .build();

        return ResponseEntity.ok(response);
    }
}
