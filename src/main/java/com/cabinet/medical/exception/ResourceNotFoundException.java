package com.cabinet.medical.exception;

/**
 * ResourceNotFoundException - Exception levée quand une ressource n'est pas
 * trouvée
 *
 * UTILISATION:
 * - User non trouvé par ID
 * - Doctor non trouvé par ID
 * - Appointment non trouvé par ID
 * - TimeSlot non trouvé par ID
 *
 * HTTP STATUS:
 * - 404 NOT FOUND
 *
 * EXEMPLE:
 * throw new ResourceNotFoundException("Utilisateur", "id", userId);
 * → Message: "Utilisateur non trouvé avec id: 123"
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Nom de la ressource (ex: "Utilisateur", "Rendez-vous")
     */
    private String resourceName;

    /**
     * Nom du champ (ex: "id", "email")
     */
    private String fieldName;

    /**
     * Valeur du champ (ex: 123, "test@example.com")
     */
    private Object fieldValue;

    /**
     * Constructeur avec nom de ressource, champ et valeur
     *
     * @param resourceName Nom de la ressource
     * @param fieldName    Nom du champ
     * @param fieldValue   Valeur du champ
     */
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s non trouvé avec %s: '%s'", resourceName, fieldName, fieldValue));
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    /**
     * Constructeur avec message personnalisé
     *
     * @param message Message d'erreur
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    // Getters

    public String getResourceName() {
        return resourceName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Object getFieldValue() {
        return fieldValue;
    }
}
