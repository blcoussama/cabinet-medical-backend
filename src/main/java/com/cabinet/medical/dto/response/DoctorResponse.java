package com.cabinet.medical.dto.response;

import com.cabinet.medical.entity.Doctor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DoctorResponse - DTO pour les informations d'un médecin
 *
 * UTILISATION:
 * - UC-P04: Patient voir liste médecins
 * - UC-P05: Patient voir créneaux disponibles médecin
 * - UC-D01: Doctor voir son profil
 * - UC-A03: Admin voir détails médecin
 *
 * CONTENU:
 * - Informations User (email, firstName, lastName, phone)
 * - Informations Doctor (specialty)
 * - IDs (userId, doctorId)
 *
 * DIFFÉRENCE AVEC UserResponse:
 * - UserResponse: Infos générales (tous users)
 * - DoctorResponse: Infos spécifiques médecin (User + Doctor)
 *
 * EXEMPLE JSON:
 * {
 * "id": 1,
 * "userId": 2,
 * "email": "martin@cabinet.com",
 * "firstName": "Martin",
 * "lastName": "Durand",
 * "phone": "0612345678",
 * "specialty": "Cardiologue",
 * "createdAt": "2025-12-29T10:00:00"
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorResponse {

    /**
     * ID du médecin (table doctor)
     *
     * UTILISATION:
     * - Référencer le médecin dans les créneaux horaires
     * - Référencer le médecin dans les rendez-vous
     * - GET /api/doctors/{id}
     * - GET /api/timeslots/doctor/{doctorId}
     * - GET /api/appointments/doctor/{doctorId}
     */
    private Long id;

    /**
     * ID de l'utilisateur associé (table users)
     *
     * RELATION:
     * Doctor (1) ↔ User (1) : OneToOne
     */
    private Long userId;

    /**
     * Email du médecin (de la table users)
     */
    private String email;

    /**
     * Prénom du médecin (de la table users)
     */
    private String firstName;

    /**
     * Nom du médecin (de la table users)
     */
    private String lastName;

    /**
     * Téléphone du médecin (de la table users)
     * Optionnel
     */
    private String phone;

    /**
     * Spécialité du médecin (de la table doctor)
     *
     * EXEMPLES:
     * - "Cardiologue"
     * - "Pédiatre"
     * - "Généraliste"
     * - "Dermatologue"
     * - "Ophtalmologue"
     * - null (optionnel)
     *
     * UTILISATION:
     * - Affichage liste médecins (UC-P04)
     * - Filtrage par spécialité
     * - Recherche médecin
     */
    private String specialty;

    /**
     * Date de création du compte médecin
     */
    private LocalDateTime createdAt;

    // ═══════════════════════════════════════════════════════════
    // MÉTHODE DE CONVERSION
    // ═══════════════════════════════════════════════════════════

    /**
     * Convertit une entité Doctor en DoctorResponse DTO
     *
     * UTILISATION:
     * Doctor doctor = doctorRepository.findById(1).orElseThrow();
     * DoctorResponse response = DoctorResponse.from(doctor);
     *
     * IMPORTANT:
     * Accède à doctor.getUser() pour récupérer les infos User
     * (relation OneToOne entre Doctor et User)
     *
     * @param doctor L'entité Doctor (avec User chargé)
     * @return DoctorResponse DTO
     */
    public static DoctorResponse from(Doctor doctor) {
        return DoctorResponse.builder()
                .id(doctor.getId())
                .userId(doctor.getUser().getId())
                .email(doctor.getUser().getEmail())
                .firstName(doctor.getUser().getFirstName())
                .lastName(doctor.getUser().getLastName())
                .phone(doctor.getUser().getPhone())
                .specialty(doctor.getSpecialty())
                .createdAt(doctor.getCreatedAt())
                .build();
    }

    /**
     * Retourne le nom complet du médecin avec titre
     *
     * UTILISATION:
     * String displayName = doctorResponse.getFullName();
     * → "Dr. Martin Durand"
     *
     * @return Nom complet avec titre "Dr."
     */
    public String getFullName() {
        return "Dr. " + firstName + " " + lastName;
    }

    /**
     * Retourne le nom complet avec spécialité
     *
     * UTILISATION:
     * String displayName = doctorResponse.getFullNameWithSpecialty();
     * → "Dr. Martin Durand - Cardiologue"
     *
     * @return Nom complet avec spécialité
     */
    public String getFullNameWithSpecialty() {
        String fullName = getFullName();
        if (specialty != null && !specialty.isEmpty()) {
            return fullName + " - " + specialty;
        }
        return fullName;
    }
}
