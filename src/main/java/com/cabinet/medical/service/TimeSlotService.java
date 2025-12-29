package com.cabinet.medical.service;

import com.cabinet.medical.dto.request.CreateTimeSlotRequest;
import com.cabinet.medical.dto.request.UpdateTimeSlotRequest;
import com.cabinet.medical.dto.response.TimeSlotResponse;
import com.cabinet.medical.entity.Doctor;
import com.cabinet.medical.entity.TimeSlot;
import com.cabinet.medical.exception.ResourceNotFoundException;
import com.cabinet.medical.exception.TimeSlotConflictException;
import com.cabinet.medical.repository.TimeSlotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * TimeSlotService - Service de gestion des créneaux horaires
 *
 * RESPONSABILITÉS:
 * - CRUD des créneaux horaires (TimeSlots)
 * - Vérification des chevauchements (RG-08)
 * - Calcul des créneaux disponibles pour une date
 * - Gestion des créneaux par médecin
 * - Conversion TimeSlot Entity ↔ TimeSlotResponse DTO
 *
 * USE CASES:
 * - UC-D02: Doctor gérer ses créneaux horaires (créer, modifier, supprimer)
 * - UC-A08: Admin gérer créneaux de tous les médecins
 * - UC-P05: Patient voir créneaux disponibles médecin
 *
 * RÈGLES MÉTIER:
 * - RG-08: Créneaux ne peuvent pas chevaucher (même doctor, même jour)
 * - UNIQUE(doctorId, dayOfWeek, startTime) en base de données
 */
@Service
public class TimeSlotService {

    private final TimeSlotRepository timeSlotRepository;
    private final DoctorService doctorService;

    /**
     * Constructeur avec injection de dépendances
     *
     * @param timeSlotRepository Repository TimeSlot
     * @param doctorService      Service Doctor (pour valider existence médecin)
     */
    public TimeSlotService(TimeSlotRepository timeSlotRepository,
            DoctorService doctorService) {
        this.timeSlotRepository = timeSlotRepository;
        this.doctorService = doctorService;
    }

    /**
     * Créer un créneau horaire (UC-D02, UC-A08)
     *
     * FLOW:
     * 1. Valider existence du médecin
     * 2. Vérifier pas de chevauchement (RG-08)
     * 3. Vérifier startTime < endTime
     * 4. Créer TimeSlot
     * 5. Retourner TimeSlotResponse
     *
     * RÈGLE MÉTIER:
     * RG-08: Un médecin ne peut pas avoir 2 créneaux qui chevauchent le même jour
     *
     * EXEMPLES VALIDES:
     * - Lundi 09:00-12:00 et Lundi 14:00-18:00 ✅ (pas de chevauchement)
     * - Lundi 09:00-12:00 et Mardi 09:00-12:00 ✅ (jours différents)
     *
     * EXEMPLES INVALIDES:
     * - Lundi 09:00-12:00 et Lundi 10:00-13:00 ❌ (chevauchement)
     * - Lundi 09:00-12:00 et Lundi 11:00-14:00 ❌ (chevauchement)
     *
     * @param request CreateTimeSlotRequest
     * @return TimeSlotResponse
     * @throws ResourceNotFoundException si médecin non trouvé
     * @throws TimeSlotConflictException si chevauchement détecté
     * @throws IllegalArgumentException  si startTime >= endTime
     */
    @Transactional
    public TimeSlotResponse createTimeSlot(CreateTimeSlotRequest request) {
        // 1. Valider existence du médecin
        Doctor doctor = doctorService.getDoctorEntityById(request.getDoctorId());

        // 2. Vérifier startTime < endTime
        if (!request.getStartTime().isBefore(request.getEndTime())) {
            throw new IllegalArgumentException(
                    "L'heure de début doit être avant l'heure de fin");
        }

        // 3. Vérifier pas de chevauchement (RG-08)
        List<TimeSlot> overlappingSlots = timeSlotRepository.findOverlappingTimeSlots(
                doctor,
                request.getDayOfWeek(),
                request.getStartTime(),
                request.getEndTime(),
                null // excludeId = null (création, pas de créneau à exclure)
        );

        if (!overlappingSlots.isEmpty()) {
            throw new TimeSlotConflictException(
                    request.getDayOfWeek(),
                    request.getStartTime(),
                    request.getEndTime());
        }

        // 4. Créer TimeSlot
        TimeSlot timeSlot = new TimeSlot();
        timeSlot.setDoctor(doctor);
        timeSlot.setDayOfWeek(request.getDayOfWeek());
        timeSlot.setStartTime(request.getStartTime());
        timeSlot.setEndTime(request.getEndTime());

        TimeSlot savedTimeSlot = timeSlotRepository.save(timeSlot);

        // 5. Retourner TimeSlotResponse
        return TimeSlotResponse.from(savedTimeSlot);
    }

    /**
     * Obtenir tous les créneaux d'un médecin (UC-D02, UC-A08)
     *
     * UTILISATION:
     * - Doctor voir ses créneaux
     * - Admin voir créneaux d'un médecin
     *
     * @param doctorId ID du médecin
     * @return List<TimeSlotResponse>
     * @throws ResourceNotFoundException si médecin non trouvé
     */
    public List<TimeSlotResponse> getTimeSlotsByDoctor(Long doctorId) {
        // Valider existence du médecin
        Doctor doctor = doctorService.getDoctorEntityById(doctorId);

        return timeSlotRepository.findByDoctor(doctor)
                .stream()
                .map(TimeSlotResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * Obtenir les créneaux d'un médecin pour un jour spécifique
     *
     * @param doctorId  ID du médecin
     * @param dayOfWeek Jour de la semaine
     * @return List<TimeSlotResponse>
     * @throws ResourceNotFoundException si médecin non trouvé
     */
    public List<TimeSlotResponse> getTimeSlotsByDoctorAndDay(Long doctorId, DayOfWeek dayOfWeek) {
        Doctor doctor = doctorService.getDoctorEntityById(doctorId);

        return timeSlotRepository.findByDoctorAndDayOfWeek(doctor, dayOfWeek)
                .stream()
                .map(TimeSlotResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * Obtenir un créneau par ID
     *
     * @param timeSlotId ID du créneau
     * @return TimeSlotResponse
     * @throws ResourceNotFoundException si créneau non trouvé
     */
    public TimeSlotResponse getTimeSlotById(Long timeSlotId) {
        TimeSlot timeSlot = timeSlotRepository.findById(timeSlotId)
                .orElseThrow(() -> new ResourceNotFoundException("Créneau horaire", "id", timeSlotId));

        return TimeSlotResponse.from(timeSlot);
    }

    /**
     * Modifier un créneau horaire (UC-D02, UC-A08)
     *
     * FLOW:
     * 1. Charger TimeSlot existant
     * 2. Vérifier startTime < endTime
     * 3. Vérifier pas de chevauchement (exclure le créneau actuel)
     * 4. Mettre à jour
     * 5. Retourner TimeSlotResponse
     *
     * @param timeSlotId ID du créneau
     * @param request    UpdateTimeSlotRequest
     * @return TimeSlotResponse
     * @throws ResourceNotFoundException si créneau non trouvé
     * @throws TimeSlotConflictException si chevauchement détecté
     * @throws IllegalArgumentException  si startTime >= endTime
     */
    @Transactional
    public TimeSlotResponse updateTimeSlot(Long timeSlotId, UpdateTimeSlotRequest request) {
        // 1. Charger TimeSlot existant
        TimeSlot timeSlot = timeSlotRepository.findById(timeSlotId)
                .orElseThrow(() -> new ResourceNotFoundException("Créneau horaire", "id", timeSlotId));

        // 2. Vérifier startTime < endTime
        if (!request.getStartTime().isBefore(request.getEndTime())) {
            throw new IllegalArgumentException(
                    "L'heure de début doit être avant l'heure de fin");
        }

        // 3. Vérifier pas de chevauchement (RG-08)
        // Exclure le créneau actuel de la vérification
        List<TimeSlot> overlappingSlots = timeSlotRepository.findOverlappingTimeSlots(
                timeSlot.getDoctor(),
                request.getDayOfWeek(),
                request.getStartTime(),
                request.getEndTime(),
                timeSlotId // Exclure ce créneau de la recherche
        );

        if (!overlappingSlots.isEmpty()) {
            throw new TimeSlotConflictException(
                    request.getDayOfWeek(),
                    request.getStartTime(),
                    request.getEndTime());
        }

        // 4. Mettre à jour
        timeSlot.setDayOfWeek(request.getDayOfWeek());
        timeSlot.setStartTime(request.getStartTime());
        timeSlot.setEndTime(request.getEndTime());

        TimeSlot updatedTimeSlot = timeSlotRepository.save(timeSlot);

        // 5. Retourner TimeSlotResponse
        return TimeSlotResponse.from(updatedTimeSlot);
    }

    /**
     * Supprimer un créneau horaire (UC-D02, UC-A08)
     *
     * NOTE:
     * Suppression en cascade affectera les RDV liés à ce créneau
     * (si implémenté dans les entités)
     *
     * @param timeSlotId ID du créneau
     * @throws ResourceNotFoundException si créneau non trouvé
     */
    @Transactional
    public void deleteTimeSlot(Long timeSlotId) {
        TimeSlot timeSlot = timeSlotRepository.findById(timeSlotId)
                .orElseThrow(() -> new ResourceNotFoundException("Créneau horaire", "id", timeSlotId));

        timeSlotRepository.delete(timeSlot);
    }

    /**
     * Obtenir les heures disponibles pour un médecin à une date donnée (UC-P05)
     *
     * FLOW:
     * 1. Déterminer le jour de la semaine de la date
     * 2. Charger les créneaux du médecin pour ce jour
     * 3. Pour chaque créneau, générer les heures (ex: 09:00, 09:30, 10:00...)
     * 4. Filtrer les heures déjà réservées (via AppointmentService)
     * 5. Retourner liste des heures disponibles
     *
     * UTILISATION:
     * - Patient sélectionne un médecin et une date
     * - System affiche les heures disponibles pour prendre RDV
     *
     * EXEMPLE:
     * - Date: 2025-12-30 (Lundi)
     * - Créneaux: Lundi 09:00-12:00, Lundi 14:00-18:00
     * - Génère: 09:00, 09:30, 10:00, 10:30, 11:00, 11:30, 14:00, 14:30...
     * - Filtre heures déjà prises
     * - Retourne: heures disponibles
     *
     * @param doctorId ID du médecin
     * @param date     Date pour laquelle chercher les créneaux
     * @return List<LocalTime> heures disponibles
     * @throws ResourceNotFoundException si médecin non trouvé
     */
    public List<LocalTime> getAvailableTimesForDate(Long doctorId, LocalDate date) {
        // 1. Déterminer jour de la semaine
        DayOfWeek dayOfWeek = date.getDayOfWeek();

        // 2. Charger créneaux du médecin pour ce jour
        Doctor doctor = doctorService.getDoctorEntityById(doctorId);
        List<TimeSlot> timeSlots = timeSlotRepository.findByDoctorAndDayOfWeek(doctor, dayOfWeek);

        // 3. Générer toutes les heures possibles (intervalles de 30 minutes)
        List<LocalTime> availableTimes = timeSlots.stream()
                .flatMap(slot -> {
                    // Générer heures de slot.startTime à slot.endTime par pas de 30 min
                    List<LocalTime> times = new java.util.ArrayList<>();
                    LocalTime current = slot.getStartTime();

                    while (current.isBefore(slot.getEndTime())) {
                        times.add(current);
                        current = current.plusMinutes(30); // Intervalles de 30 minutes
                    }

                    return times.stream();
                })
                .sorted()
                .collect(Collectors.toList());

        // TODO: Filtrer les heures déjà réservées
        // Ce sera fait dans AppointmentService.getAvailableTimesForDate()
        // qui appellera cette méthode puis filtrera les heures prises

        return availableTimes;
    }

    /**
     * Compter le nombre total de créneaux
     *
     * UTILISATION:
     * Dashboard admin (statistiques)
     *
     * @return Nombre total de créneaux
     */
    public long countTimeSlots() {
        return timeSlotRepository.count();
    }
}
