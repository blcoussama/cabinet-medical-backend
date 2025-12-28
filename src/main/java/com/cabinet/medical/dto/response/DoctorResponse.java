package com.cabinet.medical.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO DoctorResponse
 * Informations complètes d'un médecin
 * Inclut données User + données Doctor
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorResponse {

    // ═══════════════════════════════════════════════════════════
    // INFORMATIONS DOCTOR
    // ═══════════════════════════════════════════════════════════

    /**
     * ID du médecin
     */
    private Long id;

    /**
     * Spécialité médicale
     * Ex: "Cardiologue", "Pédiatre", "Généraliste"
     */
    private String specialty;

    /**
     * Numéro de licence/ordre des médecins
     * Ex: "1234567890" (RPPS en France)
     */
    private String licenseNumber;

    /**
     * Adresse du cabinet médical
     */
    private String officeAddress;

    /**
     * Tarif consultation (en euros)
     */
    private BigDecimal consultationFee;

    /**
     * Biographie/présentation du médecin
     */
    private String bio;

    /**
     * Années d'expérience
     */
    private Integer yearsExperience;

    /**
     * Date de création du profil
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
