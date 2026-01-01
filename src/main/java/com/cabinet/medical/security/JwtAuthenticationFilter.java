package com.cabinet.medical.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JwtAuthenticationFilter - Filtre pour valider les tokens JWT
 *
 * RESPONSABILITÉS:
 * - Intercepter TOUTES les requêtes HTTP
 * - Extraire le token JWT du header "Authorization"
 * - Valider le token JWT
 * - Charger l'utilisateur dans le SecurityContext
 * - Laisser passer la requête si token valide
 *
 * FLOW:
 * 1. Requête arrive → Filter intercepte
 * 2. Extrait token du header "Authorization: Bearer xxx"
 * 3. Valide token avec JwtUtil
 * 4. Si valide → Charge user dans SecurityContext
 * 5. Passe la requête au controller
 * 6. Controller peut utiliser @AuthenticationPrincipal
 *
 * EXEMPLE HEADER:
 * Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
 *
 * FORMAT ATTENDU:
 * - Prefix: "Bearer "
 * - Token: JWT complet après "Bearer "
 *
 * CE FILTRE S'EXÉCUTE:
 * - AVANT tous les controllers
 * - APRÈS Spring Security filters
 * - UNE SEULE FOIS par requête (OncePerRequestFilter)
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    /**
     * Constructeur avec injection de dépendances
     *
     * @param jwtUtil Utilitaire JWT
     */
    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    /**
     * Filtrer chaque requête HTTP
     *
     * PROCESSUS:
     * 1. Extraire header "Authorization"
     * 2. Vérifier format "Bearer xxx"
     * 3. Extraire token JWT
     * 4. Extraire email et role du token
     * 5. Valider token
     * 6. Si valide → Charger user dans SecurityContext
     * 7. Continuer la chaîne de filtres
     *
     * EXEMPLES:
     *
     * CAS 1 - TOKEN VALIDE:
     * Request: GET /api/appointments/patient/1
     * Header: Authorization: Bearer eyJhbGc...
     * → Token validé ✅
     * → User chargé dans SecurityContext ✅
     * → Controller peut accéder @AuthenticationPrincipal ✅
     * → Requête autorisée ✅
     *
     * CAS 2 - TOKEN INVALIDE:
     * Request: GET /api/appointments/patient/1
     * Header: Authorization: Bearer invalid_token
     * → Token non validé ❌
     * → SecurityContext vide ❌
     * → Spring Security refuse l'accès ❌
     * → 401 Unauthorized ❌
     *
     * CAS 3 - PAS DE TOKEN (endpoint public):
     * Request: POST /api/auth/login
     * Header: (aucun)
     * → Pas de token (normal pour /login) ✅
     * → Passe au controller sans authentification ✅
     * → Controller gère le login ✅
     *
     * @param request     Requête HTTP
     * @param response    Réponse HTTP
     * @param filterChain Chaîne de filtres
     * @throws ServletException Exception servlet
     * @throws IOException      Exception I/O
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        try {
            // 1. Extraire le header "Authorization"
            final String authorizationHeader = request.getHeader("Authorization");

            // 2. Vérifier que le header existe et commence par "Bearer "
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                // Pas de token ou format incorrect
                // On laisse passer (endpoints publics comme /login)
                filterChain.doFilter(request, response);
                return;
            }

            // 3. Extraire le token JWT (enlever "Bearer ")
            String jwt = authorizationHeader.substring(7); // "Bearer " = 7 caractères

            // 4. Extraire email et role du token
            String email = jwtUtil.extractEmail(jwt);
            String role = jwtUtil.extractRole(jwt);

            // 5. Vérifier que l'utilisateur n'est pas déjà authentifié
            // (optimisation - éviter de valider 2 fois)
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // 6. Valider le token
                if (jwtUtil.validateToken(jwt, email)) {

                    // 7. Créer les authorities (rôles Spring Security)
                    // Format Spring Security: "ROLE_PATIENT", "ROLE_DOCTOR", etc.
                    SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);

                    // 8. Créer le token d'authentification Spring Security
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            email, // Principal (email)
                            null, // Credentials (pas besoin)
                            Collections.singletonList(authority) // Authorities (rôle)
                    );

                    // 9. Ajouter les détails de la requête
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request));

                    // 10. Charger l'utilisateur dans le SecurityContext
                    // Maintenant les controllers peuvent utiliser:
                    // - @PreAuthorize("hasRole('ADMIN')")
                    // - @AuthenticationPrincipal String email
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }

        } catch (Exception e) {
            // En cas d'erreur (token invalide, expiré, etc.)
            // On laisse SecurityContext vide
            // Spring Security refusera l'accès automatiquement
            logger.error("Cannot set user authentication: {}", e);
        }

        // 11. Continuer la chaîne de filtres
        // La requête passe au controller si autorisée
        filterChain.doFilter(request, response);
    }

    /**
     * NOTE IMPORTANTE - PRINCIPAL vs USER OBJECT
     *
     * Dans ce filtre, on charge seulement l'EMAIL dans le principal:
     * authToken = new UsernamePasswordAuthenticationToken(email, null, authorities)
     *
     * CONSÉQUENCE:
     * Dans les controllers, @AuthenticationPrincipal donne l'EMAIL (String)
     * PAS un objet User complet
     *
     * UTILISATION CONTROLLER:
     * @GetMapping("/me")
     * public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal String
     * email) {
     * // email = "jean@gmail.com" (String)
     * // Charger le User complet depuis DB si besoin
     * User user = userService.findByEmail(email);
     * return ResponseEntity.ok(user);
     * }
     *
     * ALTERNATIVE (Plus complexe):
     * Charger l'objet User complet dans le filtre
     * Mais nécessite UserDetailsService et accès à UserRepository
     * Pas implémenté ici pour simplicité
     */
}