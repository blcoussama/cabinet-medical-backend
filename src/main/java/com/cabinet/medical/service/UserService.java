package com.cabinet.medical.service;

import com.cabinet.medical.dto.request.CreateUserRequest;
import com.cabinet.medical.dto.response.UserResponse;
import com.cabinet.medical.entity.Doctor;
import com.cabinet.medical.entity.Patient;
import com.cabinet.medical.entity.User;
import com.cabinet.medical.exception.EmailAlreadyExistsException;
import com.cabinet.medical.exception.ResourceNotFoundException;
import com.cabinet.medical.repository.DoctorRepository;
import com.cabinet.medical.repository.PatientRepository;
import com.cabinet.medical.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * UserService - Service de gestion des utilisateurs (ADMIN uniquement)
 *
 * RESPONSABILITÉS:
 * - CRUD complet des utilisateurs (Admin uniquement)
 * - Création de patients avec entité Patient automatique
 * - Création de médecins avec entité Doctor automatique
 * - Modification des utilisateurs
 * - Suppression des utilisateurs
 * - Conversion Entity ↔ DTO
 *
 * USE CASES:
 * - UC-A03: Admin liste tous les utilisateurs
 * - UC-A04: Admin crée un patient
 * - UC-A05: Admin crée un médecin
 * - UC-A06: Admin modifie un utilisateur
 * - UC-A07: Admin supprime un utilisateur
 *
 * PERMISSIONS:
 * - Toutes les méthodes réservées aux ADMIN
 * - Vérification faite dans les Controllers (@PreAuthorize)
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    /**
     * Constructeur avec injection de dépendances
     *
     * @param userRepository    Repository User
     * @param patientRepository Repository Patient
     * @param doctorRepository  Repository Doctor
     */
    public UserService(UserRepository userRepository,
            PatientRepository patientRepository,
            DoctorRepository doctorRepository) {
        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    /**
     * Lister tous les utilisateurs (UC-A03)
     *
     * RETOURNE:
     * Liste de TOUS les utilisateurs (patients, doctors, admins)
     *
     * @return List<UserResponse>
     */
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * Lister les utilisateurs par rôle
     *
     * UTILISATION:
     * - Filtrer seulement les patients
     * - Filtrer seulement les doctors
     * - Filtrer seulement les admins
     *
     * @param role Rôle à filtrer
     * @return List<UserResponse>
     */
    public List<UserResponse> getUsersByRole(User.Role role) {
        return userRepository.findByRole(role)
                .stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * Obtenir un utilisateur par ID
     *
     * @param userId ID de l'utilisateur
     * @return UserResponse
     * @throws ResourceNotFoundException si user non trouvé
     */
    public UserResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", userId));

        return UserResponse.from(user);
    }

    /**
     * Créer un patient (UC-A04)
     *
     * FLOW:
     * 1. Vérifier email unique (RG-01)
     * 2. Créer User (role=PATIENT, password hashé)
     * 3. Créer Patient (lié au User)
     * 4. Retourner UserResponse
     *
     * @param request CreateUserRequest (sans specialty)
     * @return UserResponse
     * @throws EmailAlreadyExistsException si email existe déjà
     */
    @Transactional
    public UserResponse createPatient(CreateUserRequest request) {
        // 1. Vérifier email unique (RG-01)
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }

        // 2. Créer User
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(User.Role.PATIENT);
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        user.setCreatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);

        // 3. Créer Patient
        Patient patient = new Patient();
        patient.setUser(savedUser);
        patient.setCreatedAt(LocalDateTime.now());

        patientRepository.save(patient);

        // 4. Retourner UserResponse
        return UserResponse.from(savedUser);
    }

    /**
     * Créer un médecin (UC-A05)
     *
     * FLOW:
     * 1. Vérifier email unique (RG-01)
     * 2. Créer User (role=DOCTOR, password hashé)
     * 3. Créer Doctor (lié au User, avec specialty)
     * 4. Retourner UserResponse
     *
     * @param request CreateUserRequest (avec specialty)
     * @return UserResponse
     * @throws EmailAlreadyExistsException si email existe déjà
     */
    @Transactional
    public UserResponse createDoctor(CreateUserRequest request) {
        // 1. Vérifier email unique (RG-01)
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }

        // 2. Créer User
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(User.Role.DOCTOR);
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        user.setCreatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);

        // 3. Créer Doctor avec specialty
        Doctor doctor = new Doctor();
        doctor.setUser(savedUser);
        doctor.setSpecialty(request.getSpecialty()); // Peut être null
        doctor.setCreatedAt(LocalDateTime.now());

        doctorRepository.save(doctor);

        // 4. Retourner UserResponse
        return UserResponse.from(savedUser);
    }

    /**
     * Créer un admin
     *
     * FLOW:
     * 1. Vérifier email unique (RG-01)
     * 2. Créer User (role=ADMIN, password hashé)
     * 3. Retourner UserResponse
     *
     * NOTE:
     * Admin n'a pas d'entité Patient ou Doctor associée
     *
     * @param request CreateUserRequest (sans specialty)
     * @return UserResponse
     * @throws EmailAlreadyExistsException si email existe déjà
     */
    @Transactional
    public UserResponse createAdmin(CreateUserRequest request) {
        // 1. Vérifier email unique (RG-01)
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }

        // 2. Créer User
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(User.Role.ADMIN);
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        user.setCreatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);

        // 3. Retourner UserResponse
        return UserResponse.from(savedUser);
    }

    /**
     * Modifier un utilisateur (UC-A06)
     *
     * FLOW:
     * 1. Charger User existant
     * 2. Vérifier nouvel email unique si changé (RG-01)
     * 3. Mettre à jour champs (sauf password si non fourni)
     * 4. Sauvegarder
     * 5. Retourner UserResponse
     *
     * RÈGLES:
     * - Email unique si changé
     * - Password hashé si fourni (sinon gardé)
     * - Role ne peut pas être changé (pour simplicité)
     *
     * @param userId  ID de l'utilisateur
     * @param request CreateUserRequest avec nouveaux champs
     * @return UserResponse
     * @throws ResourceNotFoundException   si user non trouvé
     * @throws EmailAlreadyExistsException si nouvel email existe déjà
     */
    @Transactional
    public UserResponse updateUser(Long userId, CreateUserRequest request) {
        // 1. Charger User existant
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", userId));

        // 2. Vérifier nouvel email unique si changé (RG-01)
        if (!user.getEmail().equals(request.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new EmailAlreadyExistsException(request.getEmail());
            }
            user.setEmail(request.getEmail());
        }

        // 3. Mettre à jour champs
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());

        // Mettre à jour password seulement si fourni (non vide)
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        // 4. Sauvegarder
        User updatedUser = userRepository.save(user);

        // 5. Retourner UserResponse
        return UserResponse.from(updatedUser);
    }

    /**
     * Supprimer un utilisateur (UC-A07)
     *
     * FLOW:
     * 1. Vérifier User existe
     * 2. Supprimer entités liées (Patient ou Doctor) en cascade
     * 3. Supprimer User
     *
     * NOTE:
     * - Les entités liées (Patient, Doctor) seront supprimées en cascade
     * - Les RDV et TimeSlots associés seront aussi supprimés en cascade
     * - Attention: Perte de données définitive
     *
     * @param userId ID de l'utilisateur
     * @throws ResourceNotFoundException si user non trouvé
     */
    @Transactional
    public void deleteUser(Long userId) {
        // 1. Vérifier User existe
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", userId));

        // 2 & 3. Supprimer (cascade automatique via JPA)
        userRepository.delete(user);

        // Note: Les entités Patient/Doctor/Appointments/TimeSlots
        // seront supprimées automatiquement grâce à cascade=CascadeType.ALL
    }

    /**
     * Vérifier si un email existe
     *
     * UTILISATION:
     * Validation frontend en temps réel
     *
     * @param email Email à vérifier
     * @return true si existe, false sinon
     */
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }
}
