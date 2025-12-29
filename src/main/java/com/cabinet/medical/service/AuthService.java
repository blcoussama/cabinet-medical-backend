package com.cabinet.medical.service;

import com.cabinet.medical.dto.request.LoginRequest;
import com.cabinet.medical.dto.request.RegisterRequest;
import com.cabinet.medical.dto.response.LoginResponse;
import com.cabinet.medical.entity.Patient;
import com.cabinet.medical.entity.User;
import com.cabinet.medical.exception.EmailAlreadyExistsException;
import com.cabinet.medical.exception.InvalidCredentialsException;
import com.cabinet.medical.repository.PatientRepository;
import com.cabinet.medical.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * AuthService - Service d'authentification et inscription
 *
 * RESPONSABILITÉS:
 * - Inscription des patients (UC-P01)
 * - Connexion (login) de tous les utilisateurs (UC-P02, UC-D01, UC-A01)
 * - Hashage sécurisé des mots de passe (BCrypt)
 * - Création automatique de l'entité Patient lors inscription
 * - Validation des identifiants
 *
 * USE CASES:
 * - UC-P01: Patient crée un compte
 * - UC-P02: Patient se connecte
 * - UC-D01: Doctor se connecte
 * - UC-A01: Admin se connecte
 *
 * SÉCURITÉ:
 * - Passwords hashés avec BCrypt (coût: 10)
 * - JAMAIS de password en clair en base
 * - Vérification hash lors login
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    /**
     * Constructeur avec injection de dépendances
     *
     * @param userRepository    Repository User
     * @param patientRepository Repository Patient
     */
    public AuthService(UserRepository userRepository,
            PatientRepository patientRepository) {
        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    /**
     * Inscription d'un nouveau patient (UC-P01)
     *
     * FLOW:
     * 1. Vérifier email unique (RG-01)
     * 2. Créer User (role=PATIENT, password hashé)
     * 3. Créer Patient (lié au User)
     * 4. Retourner LoginResponse
     *
     * RÈGLES MÉTIER:
     * - RG-01: Email doit être unique
     * - Password hashé en BCrypt
     * - Patient créé automatiquement
     *
     * @param request RegisterRequest (email, password, firstName, lastName, phone)
     * @return LoginResponse avec infos user + patientId
     * @throws EmailAlreadyExistsException si email existe déjà
     */
    @Transactional
    public LoginResponse register(RegisterRequest request) {
        // 1. Vérifier si email existe déjà (RG-01)
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }

        // 2. Créer User avec password hashé
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword())); // Hash BCrypt
        user.setRole(User.Role.PATIENT); // Inscription = toujours PATIENT
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        user.setCreatedAt(LocalDateTime.now());

        // Sauvegarder User
        User savedUser = userRepository.save(user);

        // 3. Créer Patient lié au User (OneToOne)
        Patient patient = new Patient();
        patient.setUser(savedUser);
        patient.setCreatedAt(LocalDateTime.now());

        // Sauvegarder Patient
        Patient savedPatient = patientRepository.save(patient);

        // 4. Retourner LoginResponse
        return LoginResponse.builder()
                .token("TODO_JWT_TOKEN") // TODO: Implémenter JWT dans partie Security
                .userId(savedUser.getId())
                .email(savedUser.getEmail())
                .firstName(savedUser.getFirstName())
                .lastName(savedUser.getLastName())
                .role(savedUser.getRole().name())
                .patientId(savedPatient.getId())
                .doctorId(null) // Pas de doctorId pour un patient
                .build();
    }

    /**
     * Connexion (login) pour tous les utilisateurs (UC-P02, UC-D01, UC-A01)
     *
     * FLOW:
     * 1. Chercher User par email
     * 2. Vérifier password (BCrypt)
     * 3. Mettre à jour lastLoginAt
     * 4. Charger Patient ou Doctor si applicable
     * 5. Retourner LoginResponse
     *
     * SÉCURITÉ:
     * - Vérification BCrypt du password
     * - Message d'erreur générique (sécurité)
     *
     * @param request LoginRequest (email, password)
     * @return LoginResponse avec infos user + patientId/doctorId
     * @throws InvalidCredentialsException si email ou password incorrect
     */
    @Transactional
    public LoginResponse login(LoginRequest request) {
        // 1. Chercher User par email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException());

        // 2. Vérifier password avec BCrypt
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        // 3. Mettre à jour lastLoginAt
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        // 4. Charger Patient ou Doctor selon le rôle
        Long patientId = null;
        Long doctorId = null;

        switch (user.getRole()) {
            case PATIENT:
                // Chercher Patient lié à ce User
                patientId = patientRepository.findByUser(user)
                        .map(Patient::getId)
                        .orElse(null);
                break;

            case DOCTOR:
                // TODO: Implémenter dans DoctorService
                // Pour l'instant, on laisse doctorId = null
                // On l'implémentera dans la prochaine partie
                break;

            case ADMIN:
                // Admin n'a ni patientId ni doctorId
                break;
        }

        // 5. Retourner LoginResponse
        return LoginResponse.builder()
                .token("TODO_JWT_TOKEN") // TODO: Implémenter JWT dans partie Security
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole().name())
                .patientId(patientId)
                .doctorId(doctorId)
                .build();
    }

    /**
     * Vérifier si un email existe déjà
     *
     * UTILISATION:
     * Peut être utilisé par le frontend pour validation en temps réel
     *
     * @param email Email à vérifier
     * @return true si email existe, false sinon
     */
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }
}
