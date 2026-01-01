package com.cabinet.medical.config;

import com.cabinet.medical.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * SecurityConfig - Configuration de la sécurité Spring Security avec JWT
 *
 * RESPONSABILITÉS:
 * - Configurer les règles d'autorisation (qui peut accéder à quoi)
 * - Ajouter le filtre JWT dans la chaîne de sécurité
 * - Définir les endpoints publics (sans authentification)
 * - Définir les endpoints protégés (avec authentification + rôle)
 * - Configurer le password encoder (BCrypt)
 *
 * ARCHITECTURE:
 * Request → JwtAuthenticationFilter → Spring Security Filters → Controller
 * (valide token JWT) (vérifie autorisation)
 *
 * TYPES D'ENDPOINTS:
 * 1. PUBLIC: Accessible sans token (login, register)
 * 2. AUTHENTICATED: Nécessite token valide (n'importe quel rôle)
 * 3. ROLE-BASED: Nécessite token + rôle spécifique (ADMIN, DOCTOR, etc.)
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // Active @PreAuthorize
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Constructeur avec injection de dépendances
     *
     * @param jwtAuthenticationFilter Filtre JWT
     */
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * Configurer la chaîne de sécurité
     *
     * RÈGLES D'AUTORISATION:
     *
     * ┌─────────────────────────────────────────────────────────┐
     * │ ENDPOINTS PUBLICS │
     * ├─────────────────────────────────────────────────────────┤
     * │ POST /api/auth/register → Inscription patient │
     * │ POST /api/auth/login → Connexion │
     * │ GET /api/doctors → Liste médecins (public) │
     * │ GET /api/doctors/{id} → Détails médecin (public) │
     * └─────────────────────────────────────────────────────────┘
     *
     * ┌─────────────────────────────────────────────────────────┐
     * │ ENDPOINTS ADMIN UNIQUEMENT │
     * ├─────────────────────────────────────────────────────────┤
     * │ /api/users/** → CRUD utilisateurs │
     * │ /api/admin/** → Dashboard admin │
     * └─────────────────────────────────────────────────────────┘
     *
     * ┌─────────────────────────────────────────────────────────┐
     * │ ENDPOINTS DOCTOR OU ADMIN │
     * ├─────────────────────────────────────────────────────────┤
     * │ POST /api/timeslots → Créer créneau │
     * │ PUT /api/timeslots/* → Modifier créneau │
     * │ DELETE /api/timeslots/* → Supprimer créneau │
     * └─────────────────────────────────────────────────────────┘
     *
     * ┌─────────────────────────────────────────────────────────┐
     * │ ENDPOINTS AUTHENTICATED (TOUS) │
     * ├─────────────────────────────────────────────────────────┤
     * │ /api/appointments/** → Gestion RDV │
     * │ /api/timeslots (GET) → Consulter créneaux │
     * └─────────────────────────────────────────────────────────┘
     *
     * @param http HttpSecurity
     * @return SecurityFilterChain configurée
     * @throws Exception Exception de configuration
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // ═══════════════════════════════════════════════════════════
                // CSRF DÉSACTIVÉ (API REST avec JWT)
                // ═══════════════════════════════════════════════════════════
                // CSRF protection pas nécessaire pour API REST stateless
                // JWT dans header suffit comme protection
                .csrf(csrf -> csrf.disable())

                // ═══════════════════════════════════════════════════════════
                // RÈGLES D'AUTORISATION
                // ═══════════════════════════════════════════════════════════
                .authorizeHttpRequests(auth -> auth
                        // ─────────────────────────────────────────────────────
                        // ENDPOINTS PUBLICS (pas d'authentification)
                        // ─────────────────────────────────────────────────────
                        .requestMatchers("/api/auth/register").permitAll() // Inscription
                        .requestMatchers("/api/auth/login").permitAll() // Connexion
                        .requestMatchers(HttpMethod.GET, "/api/doctors").permitAll() // Liste médecins
                        .requestMatchers(HttpMethod.GET, "/api/doctors/**").permitAll() // Détails médecin

                        // ─────────────────────────────────────────────────────
                        // ENDPOINTS ADMIN UNIQUEMENT
                        // ─────────────────────────────────────────────────────
                        .requestMatchers("/api/users/**").hasRole("ADMIN") // CRUD users
                        .requestMatchers("/api/admin/**").hasRole("ADMIN") // Dashboard admin

                        // ─────────────────────────────────────────────────────
                        // ENDPOINTS DOCTOR OU ADMIN
                        // ─────────────────────────────────────────────────────
                        .requestMatchers(HttpMethod.POST, "/api/timeslots").hasAnyRole("DOCTOR", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/timeslots/**").hasAnyRole("DOCTOR", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/timeslots/**").hasAnyRole("DOCTOR", "ADMIN")

                        // ─────────────────────────────────────────────────────
                        // TOUS LES AUTRES ENDPOINTS
                        // ─────────────────────────────────────────────────────
                        // Nécessitent authentification (n'importe quel rôle)
                        .anyRequest().authenticated())

                // ═══════════════════════════════════════════════════════════
                // SESSION STATELESS (pas de session HTTP)
                // ═══════════════════════════════════════════════════════════
                // API REST avec JWT = pas besoin de session
                // Chaque requête contient le token JWT
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // ═══════════════════════════════════════════════════════════
                // AJOUTER LE FILTRE JWT
                // ═══════════════════════════════════════════════════════════
                // JwtAuthenticationFilter s'exécute AVANT les filtres Spring Security
                // Il valide le token et charge l'utilisateur dans SecurityContext
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Password Encoder pour hasher les mots de passe
     *
     * ALGORITHME: BCrypt
     * - Hash one-way (irréversible)
     * - Salt automatique (unique par mot de passe)
     * - Résistant aux attaques brute-force
     *
     * UTILISATION:
     * // Hasher un mot de passe
     * String hashedPassword = passwordEncoder.encode("password123");
     * // $2a$10$N9qo8uLO... (60 caractères)
     *
     * // Vérifier un mot de passe
     * boolean matches = passwordEncoder.matches("password123", hashedPassword);
     * // true si correct, false sinon
     *
     * @return BCryptPasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * NOTE - PROTECTION DES ENDPOINTS PAR RÔLE
     *
     * MÉTHODE 1: Configuration globale (ci-dessus)
     * .requestMatchers("/api/admin/**").hasRole("ADMIN")
     *
     * MÉTHODE 2: Annotations sur les méthodes des controllers
     * @PreAuthorize("hasRole('ADMIN')")
     * @GetMapping("/api/admin/dashboard")
     * public ResponseEntity<?> getDashboard() { ... }
     *
     * RECOMMANDATION:
     * - Utiliser Méthode 1 pour règles générales (globales)
     * - Utiliser Méthode 2 pour règles spécifiques (méthode par méthode)
     * - Les deux peuvent coexister (cumulent les restrictions)
     *
     * EXEMPLES ANNOTATIONS:
     * @PreAuthorize("hasRole('ADMIN')") // Admin uniquement
     * @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')") // Doctor ou Admin
     * @PreAuthorize("hasRole('PATIENT') or hasRole('ADMIN')") // Patient ou Admin
     * @PreAuthorize("authenticated") // N'importe quel user authentifié
     */

    /**
     * NOTE - GESTION DES ERREURS D'AUTORISATION
     *
     * Si un utilisateur essaie d'accéder à un endpoint sans autorisation:
     *
     * CAS 1 - PAS DE TOKEN:
     * → 401 Unauthorized
     * → Message: "Full authentication is required"
     *
     * CAS 2 - TOKEN INVALIDE:
     * → 401 Unauthorized
     * → Message: "Invalid or expired token"
     *
     * CAS 3 - TOKEN VALIDE MAIS MAUVAIS RÔLE:
     * → 403 Forbidden
     * → Message: "Access is denied"
     *
     * CES ERREURS SONT GÉRÉES AUTOMATIQUEMENT PAR SPRING SECURITY
     * Pas besoin de code supplémentaire dans les controllers
     */
}