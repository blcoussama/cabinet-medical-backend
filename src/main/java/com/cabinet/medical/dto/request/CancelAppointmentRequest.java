package com.cabinet.medical.dto.request;

import com.cabinet.medical.entity.Appointment;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO CancelAppointmentRequest
 * Annulation d'un rendez-vous
 * Patient, Doctor ou Admin peut annuler
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CancelAppointmentRequest {

    /**
     * Qui annule le RDV (PATIENT, DOCTOR, ADMIN)
     * Déduit automatiquement du JWT token
     */
    @NotNull(message = "L'auteur de l'annulation est obligatoire")
    private Appointment.CancelledBy cancelledBy;

    /**
     * Raison d'annulation (optionnelle mais recommandée)
     * Ex: "Empêchement de dernière minute", "Maladie médecin"
     */
    @Size(max = 500, message = "La raison ne peut dépasser 500 caractères")
    private String cancellationReason;
}
