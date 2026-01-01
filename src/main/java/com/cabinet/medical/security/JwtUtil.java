package com.cabinet.medical.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JwtUtil - Utilitaire pour la gestion des tokens JWT (VERSION 0.11.5)
 *
 * RESPONSABILITÉS:
 * - Générer des tokens JWT lors du login
 * - Valider les tokens JWT reçus dans les requêtes
 * - Extraire les informations du token (email, role)
 *
 * FORMAT TOKEN JWT:
 * eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJqZWFuQGdtYWlsLmNvbSIsInJvbGUiOiJQQVRJRU5UIiwiaWF0IjoxNjQwOTk1MjAwLCJleHAiOjE2NDA5OTg4MDB9.signature
 * │ HEADER │ PAYLOAD (claims) │ SIGNATURE │
 *
 * CLAIMS (données dans le token):
 * - sub: Subject (email de l'utilisateur)
 * - role: Rôle de l'utilisateur (PATIENT, DOCTOR, ADMIN)
 * - iat: Issued At (date de création)
 * - exp: Expiration (date d'expiration)
 *
 * SÉCURITÉ:
 * - Secret key de 256 bits minimum (HS256)
 * - Token expire après 1 heure
 * - Signature vérifiée à chaque requête
 *
 * VERSION JJWT: 0.11.5
 */
@Component
public class JwtUtil {

    /**
     * Clé secrète pour signer les tokens JWT
     * 
     * PRODUCTION: Stocker dans variable d'environnement ou fichier sécurisé
     * DÉVELOPPEMENT: Hardcodée (acceptable pour projet école)
     * 
     * IMPORTANT:
     * - Minimum 256 bits (32 caractères) pour HS256
     * - Ne jamais commiter la vraie clé secrète dans Git
     * - Changer en production
     */
    @Value("${jwt.secret:cabinetMedicalSecretKeyForJWT2025PleaseChangeInProduction}")
    private String secretKey;

    /**
     * Durée de validité du token en millisecondes
     * 
     * VALEUR: 1 heure (3600000 ms)
     * 
     * RAISON:
     * - Assez long pour une session normale
     * - Assez court pour limiter les risques si token volé
     * 
     * ALTERNATIVES:
     * - 15 minutes: Très sécurisé (APIs sensibles)
     * - 24 heures: Confortable (APIs internes)
     * - 7 jours: Avec refresh token
     */
    @Value("${jwt.expiration:3600000}")
    private long jwtExpirationMs;

    // ═══════════════════════════════════════════════════════════
    // GÉNÉRATION DE TOKEN
    // ═══════════════════════════════════════════════════════════

    /**
     * Générer un token JWT pour un utilisateur
     *
     * UTILISATION:
     * String token = jwtUtil.generateToken("jean@gmail.com", "PATIENT");
     *
     * PROCESSUS:
     * 1. Créer claims (sub=email, role=PATIENT)
     * 2. Définir date création (now)
     * 3. Définir date expiration (now + 1h)
     * 4. Signer avec secret key (HS256)
     * 5. Retourner token complet
     *
     * @param email Email de l'utilisateur (unique)
     * @param role  Rôle de l'utilisateur (PATIENT, DOCTOR, ADMIN)
     * @return Token JWT signé
     */
    public String generateToken(String email, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        return createToken(claims, email);
    }

    /**
     * Créer un token JWT avec les claims fournis
     *
     * STRUCTURE TOKEN:
     * {
     * "sub": "jean@gmail.com", // Subject (email)
     * "role": "PATIENT", // Rôle personnalisé
     * "iat": 1640995200, // Issued At (timestamp)
     * "exp": 1640998800 // Expiration (timestamp)
     * }
     *
     * @param claims  Claims à inclure dans le token
     * @param subject Subject du token (email utilisateur)
     * @return Token JWT complet
     */
    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .setClaims(claims) // Données personnalisées
                .setSubject(subject) // Email utilisateur
                .setIssuedAt(now) // Date de création
                .setExpiration(expiryDate) // Date d'expiration
                .signWith(getSigningKey(), SignatureAlgorithm.HS256) // Signature
                .compact();
    }

    // ═══════════════════════════════════════════════════════════
    // VALIDATION DE TOKEN
    // ═══════════════════════════════════════════════════════════

    /**
     * Valider un token JWT
     *
     * VÉRIFICATIONS:
     * 1. Signature valide (pas modifié)
     * 2. Pas expiré (exp > now)
     * 3. Format correct
     *
     * UTILISATION:
     * if (jwtUtil.validateToken(token, email)) {
     * // Token valide
     * }
     *
     * @param token Token JWT à valider
     * @param email Email de l'utilisateur
     * @return true si token valide, false sinon
     */
    public boolean validateToken(String token, String email) {
        try {
            final String extractedEmail = extractEmail(token);
            return (extractedEmail.equals(email) && !isTokenExpired(token));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Vérifier si le token est expiré
     *
     * @param token Token JWT
     * @return true si expiré, false sinon
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // ═══════════════════════════════════════════════════════════
    // EXTRACTION DES CLAIMS
    // ═══════════════════════════════════════════════════════════

    /**
     * Extraire l'email du token JWT
     *
     * UTILISATION:
     * String email = jwtUtil.extractEmail(token);
     * // email = "jean@gmail.com"
     *
     * @param token Token JWT
     * @return Email de l'utilisateur
     */
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extraire le rôle du token JWT
     *
     * UTILISATION:
     * String role = jwtUtil.extractRole(token);
     * // role = "PATIENT"
     *
     * @param token Token JWT
     * @return Rôle de l'utilisateur
     */
    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    /**
     * Extraire la date d'expiration du token
     *
     * @param token Token JWT
     * @return Date d'expiration
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extraire un claim spécifique du token
     *
     * MÉTHODE GÉNÉRIQUE:
     * Permet d'extraire n'importe quel claim avec une fonction
     *
     * @param token          Token JWT
     * @param claimsResolver Fonction pour extraire le claim
     * @param <T>            Type du claim
     * @return Valeur du claim
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extraire tous les claims du token
     *
     * PROCESSUS:
     * 1. Parser le token avec la secret key
     * 2. Vérifier la signature
     * 3. Décoder le payload
     * 4. Retourner tous les claims
     *
     * @param token Token JWT
     * @return Tous les claims du token
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // ═══════════════════════════════════════════════════════════
    // UTILITAIRES
    // ═══════════════════════════════════════════════════════════

    /**
     * Obtenir la clé de signature
     *
     * SÉCURITÉ:
     * - Utilise HMAC-SHA256 (HS256)
     * - Clé minimum 256 bits
     * - Clé secrète partagée (symmetric key)
     *
     * COMPATIBLE AVEC JJWT 0.11.5
     *
     * @return Clé de signature SecretKey
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}