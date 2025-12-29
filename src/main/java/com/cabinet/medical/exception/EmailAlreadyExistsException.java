package com.cabinet.medical.exception;

/**
 * EmailAlreadyExistsException - Exception levée quand l'email existe déjà
 *
 * UTILISATION:
 * - Inscription (UC-P01)
 * - Admin crée utilisateur (UC-A04, UC-A05)
 *
 * HTTP STATUS:
 * - 409 CONFLICT
 *
 * EXEMPLE:
 * throw new EmailAlreadyExistsException("jean@gmail.com");
 * → Message: "Un compte existe déjà avec l'email: jean@gmail.com"
 */
public class EmailAlreadyExistsException extends RuntimeException {

    /**
     * Email en conflit
     */
    private String email;

    /**
     * Constructeur avec email
     *
     * @param email Email déjà utilisé
     */
    public EmailAlreadyExistsException(String email) {
        super(String.format("Un compte existe déjà avec l'email: %s", email));
        this.email = email;
    }

    // Getter

    public String getEmail() {
        return email;
    }
}
