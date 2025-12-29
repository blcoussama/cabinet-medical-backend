package com.cabinet.medical.exception;

/**
 * InvalidCredentialsException - Exception levée quand les identifiants sont
 * invalides
 *
 * UTILISATION:
 * - Login échoué (UC-P02, UC-D01, UC-A01)
 * - Email ou password incorrect
 *
 * HTTP STATUS:
 * - 401 UNAUTHORIZED
 *
 * EXEMPLE:
 * throw new InvalidCredentialsException();
 * → Message: "Email ou mot de passe incorrect"
 */
public class InvalidCredentialsException extends RuntimeException {

    /**
     * Constructeur par défaut
     */
    public InvalidCredentialsException() {
        super("Email ou mot de passe incorrect");
    }

    /**
     * Constructeur avec message personnalisé
     *
     * @param message Message d'erreur
     */
    public InvalidCredentialsException(String message) {
        super(message);
    }
}
