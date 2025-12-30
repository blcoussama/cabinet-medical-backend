package com.cabinet.medical.service;

import com.cabinet.medical.dto.response.AppointmentResponse;
import com.cabinet.medical.dto.response.DashboardResponse;
import com.cabinet.medical.entity.Appointment;
import com.cabinet.medical.repository.AppointmentRepository;
import com.cabinet.medical.repository.PatientRepository;
import com.cabinet.medical.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * DashboardService - Service pour statistiques administrateur (VERSION
 * OPTIMISÉE)
 *
 * RESPONSABILITÉS:
 * - Calculer statistiques rendez-vous
 * - Compter RDV par période (aujourd'hui, semaine)
 * - Compter RDV par status (PENDING, CONFIRMED, CANCELLED)
 * - Lister RDV récents (OPTIMISÉ avec pagination)
 * - Compter utilisateurs (patients, doctors, total)
 *
 * USE CASES:
 * - UC-A02: Admin voir tableau de bord état RDV
 *
 * PERMISSIONS:
 * - Admin uniquement (vérification dans Controller)
 *
 * AMÉLIORATION PERFORMANCE:
 * - Utilisation de PageRequest pour récupérer les RDV récents
 * - Évite de charger TOUS les RDV en mémoire
 * - SQL optimisé : SELECT ... ORDER BY ... LIMIT 10
 */
@Service
public class DashboardService {

        private final AppointmentRepository appointmentRepository;
        private final UserRepository userRepository;
        private final PatientRepository patientRepository;
        private final DoctorService doctorService;

        /**
         * Constructeur avec injection de dépendances
         *
         * @param appointmentRepository Repository Appointment
         * @param userRepository        Repository User
         * @param patientRepository     Repository Patient
         * @param doctorService         Service Doctor
         */
        public DashboardService(AppointmentRepository appointmentRepository,
                        UserRepository userRepository,
                        PatientRepository patientRepository,
                        DoctorService doctorService) {
                this.appointmentRepository = appointmentRepository;
                this.userRepository = userRepository;
                this.patientRepository = patientRepository;
                this.doctorService = doctorService;
        }

        /**
         * Obtenir le tableau de bord complet (UC-A02)
         *
         * RETOURNE:
         * - Total RDV aujourd'hui
         * - Total RDV cette semaine
         * - RDV par status (Map<Status, Long>)
         * - Liste RDV récents (10 derniers) ⚡ OPTIMISÉ
         * - Total médecins
         * - Total patients
         * - Total utilisateurs
         *
         * @return DashboardResponse avec toutes les statistiques
         */
        public DashboardResponse getDashboard() {
                // Calculer dates
                LocalDate today = LocalDate.now();
                LocalDateTime startOfToday = today.atStartOfDay();
                LocalDateTime endOfToday = today.atTime(LocalTime.MAX);

                // Début de semaine (lundi)
                LocalDate startOfWeekDate = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                LocalDateTime startOfWeek = startOfWeekDate.atStartOfDay();

                // Fin de semaine (dimanche)
                LocalDate endOfWeekDate = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
                LocalDateTime endOfWeek = endOfWeekDate.atTime(LocalTime.MAX);

                // Compter RDV aujourd'hui
                long totalAppointmentsToday = appointmentRepository.countAppointmentsToday(
                                startOfToday,
                                endOfToday);

                // Compter RDV cette semaine
                long totalAppointmentsWeek = appointmentRepository.countAppointmentsThisWeek(
                                startOfWeek,
                                endOfWeek);

                // Compter RDV par status
                Map<String, Long> appointmentsByStatus = new HashMap<>();
                appointmentsByStatus.put("PENDING",
                                appointmentRepository.countByStatus(Appointment.AppointmentStatus.PENDING));
                appointmentsByStatus.put("CONFIRMED",
                                appointmentRepository.countByStatus(Appointment.AppointmentStatus.CONFIRMED));
                appointmentsByStatus.put("CANCELLED",
                                appointmentRepository.countByStatus(Appointment.AppointmentStatus.CANCELLED));

                // ⚡ OPTIMISATION : Liste RDV récents avec pagination
                // Au lieu de findAll() → stream() → sorted() → limit()
                // On utilise PageRequest avec tri SQL direct
                List<AppointmentResponse> recentAppointments = getRecentAppointmentsOptimized(10);

                // Compter utilisateurs
                long totalDoctors = doctorService.countDoctors();
                long totalPatients = patientRepository.count();
                long totalUsers = userRepository.count();

                // Construire DashboardResponse
                return DashboardResponse.builder()
                                .totalAppointmentsToday(totalAppointmentsToday)
                                .totalAppointmentsWeek(totalAppointmentsWeek)
                                .appointmentsByStatus(appointmentsByStatus)
                                .recentAppointments(recentAppointments)
                                .totalDoctors(totalDoctors)
                                .totalPatients(totalPatients)
                                .totalUsers(totalUsers)
                                .build();
        }

        /**
         * ⚡ MÉTHODE OPTIMISÉE : Récupérer les N derniers RDV
         *
         * ANCIENNE VERSION (NON OPTIMISÉE) :
         * appointmentRepository.findAll()
         * .stream()
         * .sorted(...)
         * .limit(10)
         * .collect(...)
         *
         * PROBLÈME :
         * - Charge TOUS les RDV en mémoire (10, 100, 10000...)
         * - Trie tout en mémoire
         * - Prend seulement 10
         * - Gaspillage de RAM et CPU
         *
         * NOUVELLE VERSION (OPTIMISÉE) :
         * PageRequest avec tri SQL
         *
         * AVANTAGE :
         * - PostgreSQL charge seulement 10 RDV
         * - Tri fait par PostgreSQL (optimisé index)
         * - SQL: SELECT ... ORDER BY created_at DESC LIMIT 10
         * - Rapide même avec 100,000 RDV
         *
         * @param limit Nombre de RDV à récupérer (généralement 10)
         * @return Liste des N derniers RDV créés
         */
        private List<AppointmentResponse> getRecentAppointmentsOptimized(int limit) {
                // Créer PageRequest avec tri DESC sur createdAt
                PageRequest pageRequest = PageRequest.of(
                                0, // Page 0 (première page)
                                limit, // Taille page = limit
                                Sort.by(Sort.Direction.DESC, "createdAt") // Tri décroissant sur createdAt
                );

                // Récupérer les RDV avec pagination
                // SQL généré : SELECT * FROM appointment ORDER BY created_at DESC LIMIT 10
                List<Appointment> appointments = appointmentRepository
                                .findAll(pageRequest)
                                .getContent();

                // Convertir en AppointmentResponse
                return appointments.stream()
                                .map(AppointmentResponse::from) // Utilise la méthode static de AppointmentResponse
                                .collect(Collectors.toList());
        }

        /**
         * Obtenir statistiques RDV pour une période personnalisée
         *
         * @param startDate Date de début
         * @param endDate   Date de fin
         * @return Map avec statistiques
         */
        public Map<String, Object> getAppointmentStatistics(LocalDate startDate, LocalDate endDate) {
                LocalDateTime start = startDate.atStartOfDay();
                LocalDateTime end = endDate.atTime(LocalTime.MAX);

                List<Appointment> appointments = appointmentRepository.findByDateTimeBetween(start, end);

                Map<String, Object> stats = new HashMap<>();
                stats.put("total", appointments.size());
                stats.put("pending", appointments.stream()
                                .filter(a -> a.getStatus() == Appointment.AppointmentStatus.PENDING)
                                .count());
                stats.put("confirmed", appointments.stream()
                                .filter(a -> a.getStatus() == Appointment.AppointmentStatus.CONFIRMED)
                                .count());
                stats.put("cancelled", appointments.stream()
                                .filter(a -> a.getStatus() == Appointment.AppointmentStatus.CANCELLED)
                                .count());

                return stats;
        }

        /**
         * Obtenir taux de confirmation
         *
         * CALCUL:
         * (Nombre CONFIRMED / Nombre total non-CANCELLED) * 100
         *
         * @return Pourcentage de confirmation (0-100)
         */
        public double getConfirmationRate() {
                long total = appointmentRepository.count();
                long cancelled = appointmentRepository.countByStatus(Appointment.AppointmentStatus.CANCELLED);
                long nonCancelled = total - cancelled;

                if (nonCancelled == 0) {
                        return 0.0;
                }

                long confirmed = appointmentRepository.countByStatus(Appointment.AppointmentStatus.CONFIRMED);
                return (confirmed * 100.0) / nonCancelled;
        }

        /**
         * Obtenir taux d'annulation
         *
         * CALCUL:
         * (Nombre CANCELLED / Nombre total) * 100
         *
         * @return Pourcentage d'annulation (0-100)
         */
        public double getCancellationRate() {
                long total = appointmentRepository.count();

                if (total == 0) {
                        return 0.0;
                }

                long cancelled = appointmentRepository.countByStatus(Appointment.AppointmentStatus.CANCELLED);
                return (cancelled * 100.0) / total;
        }
}