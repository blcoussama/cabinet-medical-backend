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
 * - Statistiques RDV (UC-A02):
 * ├── Total RDV aujourd'hui
 * ├── Total RDV cette semaine
 * ├── Répartition par status (PENDING, CONFIRMED, CANCELLED)
 * └── Liste derniers RDV créés
 *
 * - Statistiques utilisateurs (bonus):
 * ├── Total médecins
 * ├── Total patients
 * └── Total utilisateurs
 *
 * EXEMPLE JSON:
 * {
 * "totalAppointmentsToday": 2,
 * "totalAppointmentsWeek": 3,
 * "appointmentsByStatus": {
 * "PENDING": 2,
 * "CONFIRMED": 0,
 * "CANCELLED": 1
 * },
 * "recentAppointments": [
 * { ... },
 * { ... }
 * ],
 * "totalDoctors": 1,
 * "totalPatients": 2,
 * "totalUsers": 4
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardResponse {

    // ═══════════════════════════════════════════════════════════
    // STATISTIQUES RENDEZ-VOUS (UC-A02)
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
     * "PENDING": 2,
     * "CONFIRMED": 0,
     * "CANCELLED": 1
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
    // STATISTIQUES UTILISATEURS (Bonus - Utiles pour dashboard)
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
}