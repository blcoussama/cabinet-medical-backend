package com.cabinet.medical.service;

import com.cabinet.medical.dto.request.CancelAppointmentRequest;
import com.cabinet.medical.dto.request.CreateAppointmentRequest;
import com.cabinet.medical.dto.request.MoveAppointmentRequest;
import com.cabinet.medical.dto.request.UpdateAppointmentRequest;
import com.cabinet.medical.dto.response.AppointmentResponse;
import com.cabinet.medical.entity.Appointment;
import com.cabinet.medical.entity.Doctor;
import com.cabinet.medical.entity.Notification;
import com.cabinet.medical.entity.Patient;
import com.cabinet.medical.exception.AppointmentConflictException;
import com.cabinet.medical.exception.ResourceNotFoundException;
import com.cabinet.medical.repository.AppointmentRepository;
import com.cabinet.medical.repository.NotificationRepository;
import com.cabinet.medical.repository.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AppointmentService - Service de gestion des rendez-vous
 *
 * RESPONSABILITÉS:
 * - CRUD des rendez-vous (Appointments)
 * - Vérification disponibilité créneau (RG-02)
 * - Création automatique des notifications (RG-06, RG-07)
 * - Gestion des annulations avec raison
 * - Déplacement de RDV (Admin uniquement)
 * - Calcul des créneaux disponibles (résout TODO TimeSlotService)
 *
 * USE CASES:
 * - UC-P03: Patient consulter historique RDV
 * - UC-P06: Patient prendre RDV (+ notifications)
 * - UC-P07: Patient modifier RDV
 * - UC-P08: Patient annuler RDV
 * - UC-D03: Doctor consulter ses RDV
 * - UC-D04: Doctor voir détails RDV
 * - UC-D05: Doctor modifier RDV
 * - UC-D06: Doctor annuler RDV
 * - UC-A09: Admin voir TOUS les RDV
 * - UC-A10: Admin modifier RDV
 * - UC-A11: Admin annuler RDV
 * - UC-A12: Admin déplacer RDV
 *
 * RÈGLES MÉTIER:
 * - RG-02: Un seul RDV par créneau médecin
 * - RG-03: Patient peut annuler/modifier ses RDV
 * - RG-04: Doctor peut annuler/modifier ses RDV
 * - RG-05: Admin peut tout faire
 * - RG-06: Notification CONFIRMATION envoyée lors création RDV
 * - RG-07: Notifications REMINDER envoyées avant RDV
 */
@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorService doctorService;
    private final TimeSlotService timeSlotService;
    private final NotificationRepository notificationRepository;

    /**
     * Constructeur avec injection de dépendances
     *
     * @param appointmentRepository  Repository Appointment
     * @param patientRepository      Repository Patient
     * @param doctorService          Service Doctor
     * @param timeSlotService        Service TimeSlot
     * @param notificationRepository Repository Notification
     */
    public AppointmentService(AppointmentRepository appointmentRepository,
            PatientRepository patientRepository,
            DoctorService doctorService,
            TimeSlotService timeSlotService,
            NotificationRepository notificationRepository) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.doctorService = doctorService;
        this.timeSlotService = timeSlotService;
        this.notificationRepository = notificationRepository;
    }

    /**
     * Créer un rendez-vous (UC-P06)
     *
     * FLOW:
     * 1. Valider existence médecin
     * 2. Valider existence patient
     * 3. Vérifier créneau disponible (RG-02)
     * 4. Créer Appointment (status=PENDING)
     * 5. Créer Notification CONFIRMATION (RG-06)
     * 6. Créer Notification REMINDER (RG-07)
     * 7. Retourner AppointmentResponse
     *
     * RÈGLES MÉTIER:
     * - RG-02: Un seul RDV par créneau médecin
     * - RG-06: Notification CONFIRMATION immédiate
     * - RG-07: Notification REMINDER différée
     *
     * @param patientId ID du patient qui prend RDV
     * @param request   CreateAppointmentRequest
     * @return AppointmentResponse
     * @throws ResourceNotFoundException    si médecin ou patient non trouvé
     * @throws AppointmentConflictException si créneau déjà pris
     */
    @Transactional
    public AppointmentResponse createAppointment(Long patientId, CreateAppointmentRequest request) {
        // 1. Valider existence médecin
        Doctor doctor = doctorService.getDoctorEntityById(request.getDoctorId());

        // 2. Valider existence patient
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", "id", patientId));

        // 3. Vérifier créneau disponible (RG-02)
        if (!isTimeSlotAvailable(doctor, request.getDateTime())) {
            throw new AppointmentConflictException(
                    doctor.getUser().getFirstName() + " " + doctor.getUser().getLastName(),
                    request.getDateTime());
        }

        // 4. Créer Appointment
        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setDateTime(request.getDateTime());
        appointment.setReason(request.getReason());
        appointment.setStatus(Appointment.AppointmentStatus.PENDING);

        Appointment savedAppointment = appointmentRepository.save(appointment);

        // 5. Créer Notification CONFIRMATION (RG-06)
        createConfirmationNotification(savedAppointment);

        // 6. Créer Notification REMINDER (RG-07)
        createReminderNotification(savedAppointment);

        // 7. Retourner AppointmentResponse
        return AppointmentResponse.from(savedAppointment);
    }

    /**
     * Vérifier si un créneau est disponible (RG-02)
     *
     * UTILISATION:
     * Avant de créer/modifier/déplacer un RDV
     *
     * RÈGLE:
     * Un médecin ne peut avoir qu'un seul RDV à une date/heure donnée
     *
     * @param doctor   Médecin
     * @param dateTime Date et heure du RDV
     * @return true si disponible, false sinon
     */
    private boolean isTimeSlotAvailable(Doctor doctor, LocalDateTime dateTime) {
        return !appointmentRepository.existsByDoctorAndDateTime(doctor, dateTime);
    }

    /**
     * Créer notification de confirmation (RG-06)
     *
     * TYPE: CONFIRMATION
     * ENVOI: Immédiat (sentAt = NOW)
     * DESTINATAIRE: Patient qui a pris le RDV
     *
     * @param appointment Rendez-vous créé
     */
    private void createConfirmationNotification(Appointment appointment) {
        Notification notification = new Notification();
        notification.setAppointment(appointment);
        notification.setUser(appointment.getPatient().getUser());
        notification.setType(Notification.NotificationType.CONFIRMATION);

        // Message de confirmation
        String doctorName = "Dr. " + appointment.getDoctor().getUser().getFirstName() +
                " " + appointment.getDoctor().getUser().getLastName();
        String message = String.format(
                "Votre rendez-vous avec %s le %s à %s a été confirmé.",
                doctorName,
                appointment.getDateTime().toLocalDate(),
                appointment.getDateTime().toLocalTime());
        notification.setMessage(message);

        // Envoi immédiat
        notification.setSentAt(LocalDateTime.now());

        notificationRepository.save(notification);
    }

    /**
     * Créer notification de rappel (RG-07)
     *
     * TYPE: REMINDER
     * ENVOI: Différé (sentAt = NULL, envoyé 24h avant par job Cron)
     * DESTINATAIRE: Patient qui a pris le RDV
     *
     * @param appointment Rendez-vous créé
     */
    private void createReminderNotification(Appointment appointment) {
        Notification notification = new Notification();
        notification.setAppointment(appointment);
        notification.setUser(appointment.getPatient().getUser());
        notification.setType(Notification.NotificationType.REMINDER);

        // Message de rappel
        String doctorName = "Dr. " + appointment.getDoctor().getUser().getFirstName() +
                " " + appointment.getDoctor().getUser().getLastName();
        String message = String.format(
                "Rappel: Vous avez rendez-vous avec %s demain à %s.",
                doctorName,
                appointment.getDateTime().toLocalTime());
        notification.setMessage(message);

        // Envoi différé (sera envoyé par NotificationService)
        notification.setSentAt(null);

        notificationRepository.save(notification);
    }

    /**
     * Obtenir tous les RDV d'un patient (UC-P03)
     *
     * @param patientId ID du patient
     * @return List<AppointmentResponse>
     * @throws ResourceNotFoundException si patient non trouvé
     */
    public List<AppointmentResponse> getAppointmentsByPatient(Long patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", "id", patientId));

        return appointmentRepository.findByPatient(patient)
                .stream()
                .map(AppointmentResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * Obtenir tous les RDV d'un médecin (UC-D03)
     *
     * @param doctorId ID du médecin
     * @return List<AppointmentResponse>
     * @throws ResourceNotFoundException si médecin non trouvé
     */
    public List<AppointmentResponse> getAppointmentsByDoctor(Long doctorId) {
        Doctor doctor = doctorService.getDoctorEntityById(doctorId);

        return appointmentRepository.findByDoctor(doctor)
                .stream()
                .map(AppointmentResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * Obtenir TOUS les RDV (UC-A09 - Admin uniquement)
     *
     * @return List<AppointmentResponse>
     */
    public List<AppointmentResponse> getAllAppointments() {
        return appointmentRepository.findAll()
                .stream()
                .map(AppointmentResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * Obtenir détails d'un RDV (UC-P03, UC-D04)
     *
     * @param appointmentId ID du RDV
     * @return AppointmentResponse
     * @throws ResourceNotFoundException si RDV non trouvé
     */
    public AppointmentResponse getAppointmentById(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Rendez-vous", "id", appointmentId));

        return AppointmentResponse.from(appointment);
    }

    /**
     * Modifier un RDV (UC-P07, UC-D05, UC-A10)
     *
     * FLOW:
     * 1. Charger Appointment existant
     * 2. Si dateTime changé, vérifier nouveau créneau disponible (RG-02)
     * 3. Mettre à jour champs
     * 4. Créer nouvelle notification si dateTime changé
     * 5. Retourner AppointmentResponse
     *
     * @param appointmentId ID du RDV
     * @param request       UpdateAppointmentRequest
     * @return AppointmentResponse
     * @throws ResourceNotFoundException    si RDV non trouvé
     * @throws AppointmentConflictException si nouveau créneau déjà pris
     */
    @Transactional
    public AppointmentResponse updateAppointment(Long appointmentId, UpdateAppointmentRequest request) {
        // 1. Charger Appointment existant
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Rendez-vous", "id", appointmentId));

        // 2. Si dateTime changé, vérifier disponibilité (RG-02)
        if (request.getDateTime() != null &&
                !request.getDateTime().equals(appointment.getDateTime())) {

            // Vérifier que le nouveau créneau est disponible
            if (!isTimeSlotAvailable(appointment.getDoctor(), request.getDateTime())) {
                throw new AppointmentConflictException(
                        appointment.getDoctor().getUser().getFirstName() + " " +
                                appointment.getDoctor().getUser().getLastName(),
                        request.getDateTime());
            }

            // Mettre à jour dateTime
            appointment.setDateTime(request.getDateTime());

            // Créer notification de modification
            createModificationNotification(appointment, "modifié");
        }

        // 3. Mettre à jour reason si fourni
        if (request.getReason() != null && !request.getReason().isEmpty()) {
            appointment.setReason(request.getReason());
        }

        // 4. Sauvegarder
        Appointment updatedAppointment = appointmentRepository.save(appointment);

        // 5. Retourner AppointmentResponse
        return AppointmentResponse.from(updatedAppointment);
    }

    /**
     * Annuler un RDV (UC-P08, UC-D06, UC-A11)
     *
     * FLOW:
     * 1. Charger Appointment existant
     * 2. Update status = CANCELLED
     * 3. Update cancelledBy (PATIENT/DOCTOR/ADMIN)
     * 4. Update cancellationReason
     * 5. Créer notification annulation
     * 6. Sauvegarder
     *
     * @param appointmentId ID du RDV
     * @param request       CancelAppointmentRequest
     * @param cancelledBy   Qui annule (PATIENT/DOCTOR/ADMIN)
     * @throws ResourceNotFoundException si RDV non trouvé
     */
    @Transactional
    public void cancelAppointment(Long appointmentId,
            CancelAppointmentRequest request,
            Appointment.CancelledBy cancelledBy) {
        // 1. Charger Appointment existant
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Rendez-vous", "id", appointmentId));

        // 2. Update status
        appointment.setStatus(Appointment.AppointmentStatus.CANCELLED);

        // 3. Update cancelledBy
        appointment.setCancelledBy(cancelledBy);

        // 4. Update cancellationReason
        if (request.getCancellationReason() != null) {
            appointment.setCancellationReason(request.getCancellationReason());
        }

        // 5. Créer notification annulation
        createCancellationNotification(appointment, cancelledBy);

        // 6. Sauvegarder
        appointmentRepository.save(appointment);
    }

    /**
     * Déplacer un RDV (UC-A12 - Admin uniquement)
     *
     * FLOW:
     * 1. Charger Appointment existant
     * 2. Valider nouveau médecin existe
     * 3. Vérifier nouveau créneau disponible (RG-02)
     * 4. Mettre à jour doctorId + dateTime
     * 5. Créer notification déplacement
     * 6. Retourner AppointmentResponse
     *
     * @param appointmentId ID du RDV
     * @param request       MoveAppointmentRequest
     * @return AppointmentResponse
     * @throws ResourceNotFoundException    si RDV ou médecin non trouvé
     * @throws AppointmentConflictException si nouveau créneau déjà pris
     */
    @Transactional
    public AppointmentResponse moveAppointment(Long appointmentId, MoveAppointmentRequest request) {
        // 1. Charger Appointment existant
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Rendez-vous", "id", appointmentId));

        // 2. Valider nouveau médecin existe
        Doctor newDoctor = doctorService.getDoctorEntityById(request.getNewDoctorId());

        // 3. Vérifier nouveau créneau disponible (RG-02)
        if (!isTimeSlotAvailable(newDoctor, request.getNewDateTime())) {
            throw new AppointmentConflictException(
                    newDoctor.getUser().getFirstName() + " " + newDoctor.getUser().getLastName(),
                    request.getNewDateTime());
        }

        // 4. Mettre à jour doctor + dateTime
        appointment.setDoctor(newDoctor);
        appointment.setDateTime(request.getNewDateTime());

        // 5. Créer notification déplacement
        createModificationNotification(appointment, "déplacé");

        // 6. Sauvegarder et retourner
        Appointment movedAppointment = appointmentRepository.save(appointment);
        return AppointmentResponse.from(movedAppointment);
    }

    /**
     * Créer notification de modification
     *
     * @param appointment RDV modifié
     * @param action      "modifié" ou "déplacé"
     */
    private void createModificationNotification(Appointment appointment, String action) {
        Notification notification = new Notification();
        notification.setAppointment(appointment);
        notification.setUser(appointment.getPatient().getUser());
        notification.setType(Notification.NotificationType.CONFIRMATION);

        String doctorName = "Dr. " + appointment.getDoctor().getUser().getFirstName() +
                " " + appointment.getDoctor().getUser().getLastName();
        String message = String.format(
                "Votre rendez-vous a été %s. Nouveau RDV avec %s le %s à %s.",
                action,
                doctorName,
                appointment.getDateTime().toLocalDate(),
                appointment.getDateTime().toLocalTime());
        notification.setMessage(message);
        notification.setSentAt(LocalDateTime.now());

        notificationRepository.save(notification);
    }

    /**
     * Créer notification d'annulation
     *
     * @param appointment RDV annulé
     * @param cancelledBy Qui a annulé
     */
    private void createCancellationNotification(Appointment appointment,
            Appointment.CancelledBy cancelledBy) {
        Notification notification = new Notification();
        notification.setAppointment(appointment);
        notification.setUser(appointment.getPatient().getUser());
        notification.setType(Notification.NotificationType.CONFIRMATION);

        String doctorName = "Dr. " + appointment.getDoctor().getUser().getFirstName() +
                " " + appointment.getDoctor().getUser().getLastName();

        String cancelledByText = cancelledBy == Appointment.CancelledBy.PATIENT ? "vous"
                : (cancelledBy == Appointment.CancelledBy.DOCTOR ? "le médecin" : "l'administration");

        String message = String.format(
                "Votre rendez-vous avec %s le %s à %s a été annulé par %s.",
                doctorName,
                appointment.getDateTime().toLocalDate(),
                appointment.getDateTime().toLocalTime(),
                cancelledByText);
        notification.setMessage(message);
        notification.setSentAt(LocalDateTime.now());

        notificationRepository.save(notification);
    }

    /**
     * Obtenir les heures disponibles pour une date (UC-P05)
     *
     * ⭐ RÉSOUT LE TODO DE TIMESLOTSERVICE
     *
     * FLOW:
     * 1. Appeler TimeSlotService.getAvailableTimesForDate()
     * 2. Charger RDV existants du médecin pour cette date
     * 3. Filtrer heures déjà prises (RG-02)
     * 4. Retourner heures disponibles
     *
     * @param doctorId ID du médecin
     * @param date     Date pour laquelle chercher les créneaux
     * @return List<LocalTime> heures disponibles
     * @throws ResourceNotFoundException si médecin non trouvé
     */
    public List<LocalTime> getAvailableTimesForDate(Long doctorId, LocalDate date) {
        // 1. Obtenir toutes les heures possibles depuis TimeSlotService
        List<LocalTime> allPossibleTimes = timeSlotService.getAvailableTimesForDate(doctorId, date);

        // 2. Charger RDV existants du médecin pour cette date
        Doctor doctor = doctorService.getDoctorEntityById(doctorId);

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);

        List<Appointment> existingAppointments = appointmentRepository.findByDoctorAndDateTimeBetween(doctor,
                startOfDay, endOfDay);

        // 3. Extraire heures déjà prises
        List<LocalTime> takenTimes = existingAppointments.stream()
                .filter(apt -> apt.getStatus() != Appointment.AppointmentStatus.CANCELLED) // Ignorer annulés
                .map(apt -> apt.getDateTime().toLocalTime())
                .collect(Collectors.toList());

        // 4. Filtrer et retourner heures disponibles
        return allPossibleTimes.stream()
                .filter(time -> !takenTimes.contains(time))
                .collect(Collectors.toList());
    }

    /**
     * Compter le nombre total de RDV
     *
     * UTILISATION:
     * Dashboard admin (statistiques)
     *
     * @return Nombre total de RDV
     */
    public long countAppointments() {
        return appointmentRepository.count();
    }

    /**
     * Compter RDV par status
     *
     * UTILISATION:
     * Dashboard admin (statistiques)
     *
     * @param status Status à compter
     * @return Nombre de RDV avec ce status
     */
    public long countAppointmentsByStatus(Appointment.AppointmentStatus status) {
        return appointmentRepository.countByStatus(status);
    }
}
