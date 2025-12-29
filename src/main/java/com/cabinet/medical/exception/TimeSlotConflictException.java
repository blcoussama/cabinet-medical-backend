package com.cabinet.medical.exception;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * TimeSlotConflictException - Exception levée quand un créneau est en conflit
 *
 * UTILISATION:
 * - Créer créneau qui chevauche un existant (UC-D02, UC-A08)
 * - Modifier créneau qui crée un chevauchement
 *
 * HTTP STATUS:
 * - 409 CONFLICT
 *
 * RÈGLE MÉTIER:
 * Un médecin ne peut pas avoir 2 créneaux qui se chevauchent le même jour
 *
 * EXEMPLE:
 * throw new TimeSlotConflictException("Lundi", "09:00", "12:00");
 * → Message: "Un créneau existe déjà pour Lundi entre 09:00 et 12:00"
 */
public class TimeSlotConflictException extends RuntimeException {

    /**
     * Jour de la semaine en conflit
     */
    private String dayOfWeek;

    /**
     * Heure de début du créneau
     */
    private String startTime;

    /**
     * Heure de fin du créneau
     */
    private String endTime;

    /**
     * Constructeur avec jour et heures
     *
     * @param dayOfWeek Jour de la semaine
     * @param startTime Heure début
     * @param endTime   Heure fin
     */
    public TimeSlotConflictException(String dayOfWeek, String startTime, String endTime) {
        super(String.format("Un créneau existe déjà pour %s entre %s et %s",
                dayOfWeek, startTime, endTime));
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    /**
     * Constructeur avec DayOfWeek et LocalTime
     *
     * @param dayOfWeek Jour de la semaine (enum)
     * @param startTime Heure début (LocalTime)
     * @param endTime   Heure fin (LocalTime)
     */
    public TimeSlotConflictException(DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
        this(dayOfWeek.toString(), startTime.toString(), endTime.toString());
    }

    /**
     * Constructeur avec message personnalisé
     *
     * @param message Message d'erreur
     */
    public TimeSlotConflictException(String message) {
        super(message);
    }

    // Getters

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }
}
