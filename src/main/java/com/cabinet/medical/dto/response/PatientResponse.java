package com.cabinet.medical.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO PatientResponse
 * Informations complètes d'un patient
 * Inclut données User + données Patient
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientResponse {

    // ═══════════════════════════════════════════════════════════
    // INFORMATIONS PATIENT
    // ═══════════════════════════════════════════════════════════

    /**
     * ID du patient
     */
    private Long id;

    /**
     * Date de naissance
     */
    private LocalDate dateOfBirth;

    /**
     * Adresse complète
     */
    private String address;

    /**
     * Historique médical
     */
    private String medicalHistory;

    /**
     * Date de création du dossier patient
     */
    private LocalDateTime createdAt;

    // ═══════════════════════════════════════════════════════════
    // INFORMATIONS USER (depuis relation OneToOne)
    // ═══════════════════════════════════════════════════════════

    /**
     * ID de l'utilisateur associé
     */
    private Long userId;

    /**
     * Email
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
     * Compte actif ou désactivé
     */
    private Boolean isActive;
}