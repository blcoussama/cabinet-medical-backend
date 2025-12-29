package com.cabinet.medical.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * DashboardResponse - DTO pour le tableau de bord admin
 *
 * UTILISATION:
 * - UC-A02: Admin voir tableau de bord état rendez-vous
 *
 * ENDPOINT:
 * GET /api/admin/dashboard
 *
 * CONTENU:
 * - Statistiques globales (RDV aujourd'hui, cette semaine)
 * - Répartition par status (PENDING, CONFIRMED, CANCELLED)
 * - Liste des derniers rendez-vous
 * - Statistiques médecins (nombre de médecins, patients)
 *
 * EXEMPLE JSON:
 * {
 * "totalAppointmentsToday": 12,
 * "totalAppointmentsWeek": 87,
 * "appointmentsByStatus": {
 * "PENDING": 15,
 * "CONFIRMED": 65,
 * "CANCELLED": 7
 * },
 * "recentAppointments": [
 * { ... },
 * { ... }
 * ],
 * "totalDoctors": 5,
 * "totalPatients": 142,
 * "totalUsers": 148
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardResponse {

    // ═══════════════════════════════════════════════════════════
    // STATISTIQUES RENDEZ-VOUS
    // ═══════════════════════════════════════════════════════════

    /**
     * Total rendez-vous aujourd'hui
     *
     * CALCUL:
     * COUNT(*) FROM appointment WHERE DATE(date_time) = TODAY
     *
     * UTILISATION:
     * Affichage card "RDV Aujourd'hui"
     */
    private Long totalAppointmentsToday;

    /**
     * Total rendez-vous cette semaine
     *
     * CALCUL:
     * COUNT(*) FROM appointment WHERE WEEK(date_time) = CURRENT_WEEK
     *
     * UTILISATION:
     * Affichage card "RDV Cette Semaine"
     */
    private Long totalAppointmentsWeek;

    /**
     * Répartition des rendez-vous par status
     *
     * FORMAT:
     * Map<String, Long>
     * {
     * "PENDING": 15,
     * "CONFIRMED": 65,
     * "CANCELLED": 7
     * }
     *
     * CALCUL:
     * Pour chaque status:
     * COUNT(*) FROM appointment WHERE status = ?
     *
     * UTILISATION:
     * Affichage chart (diagramme circulaire ou barres)
     */
    private Map<String, Long> appointmentsByStatus;

    /**
     * Liste des derniers rendez-vous créés
     *
     * CONTENU:
     * Les 10 derniers RDV créés (triés par createdAt DESC)
     *
     * UTILISATION:
     * Affichage liste récente pour monitoring
     */
    private List<AppointmentResponse> recentAppointments;

    // ═══════════════════════════════════════════════════════════
    // STATISTIQUES UTILISATEURS
    // ═══════════════════════════════════════════════════════════

    /**
     * Total nombre de médecins
     *
     * CALCUL:
     * COUNT(*) FROM doctor
     *
     * UTILISATION:
     * Affichage card "Médecins"
     */
    private Long totalDoctors;

    /**
     * Total nombre de patients
     *
     * CALCUL:
     * COUNT(*) FROM patient
     *
     * UTILISATION:
     * Affichage card "Patients"
     */
    private Long totalPatients;

    /**
     * Total nombre d'utilisateurs
     *
     * CALCUL:
     * COUNT(*) FROM users
     *
     * UTILISATION:
     * Affichage card "Utilisateurs"
     */
    private Long totalUsers;

    // ═══════════════════════════════════════════════════════════
    // STATISTIQUES SUPPLÉMENTAIRES (Optionnel)
    // ═══════════════════════════════════════════════════════════

    /**
     * Total créneaux horaires configurés
     *
     * CALCUL:
     * COUNT(*) FROM timeslot
     */
    private Long totalTimeSlots;

    /**
     * Total notifications en attente
     *
     * CALCUL:
     * COUNT(*) FROM notification WHERE sent_at IS NULL
     */
    private Long pendingNotifications;

    /**
     * Taux de confirmation des RDV (en pourcentage)
     *
     * CALCUL:
     * (COUNT(status=CONFIRMED) / COUNT(*)) * 100
     *
     * EXEMPLE:
     * 65 confirmés sur 87 total = 74.71%
     */
    private Double confirmationRate;

    /**
     * Taux d'annulation des RDV (en pourcentage)
     *
     * CALCUL:
     * (COUNT(status=CANCELLED) / COUNT(*)) * 100
     *
     * EXEMPLE:
     * 7 annulés sur 87 total = 8.05%
     */
    private Double cancellationRate;
}
