
package com.JSr.api_gateway.config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.server.SecurityWebFilterChain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * SecurityConfig - Global Security Configuration for API Gateway
 *
 * This configuration sets up OAuth2 resource server security with Keycloak integration.
 * It provides global authentication and authorization rules for all incoming requests
 * before they reach the individual route filters like KeycloakRoleFilter.
 *
 * Key Functionality:
 * • Configures JWT-based OAuth2 resource server
 * • Defines public vs secured endpoints
 * • Maps Keycloak roles to Spring Security authorities
 * • Provides global security context for downstream filters
 *
 * Analogy: This is the main entrance security that checks if you're allowed
 * into the building, while route filters check specific rooms.
 */
@Configuration
@EnableWebFluxSecurity  // Use WebFlux security for reactive applications
public class SecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri:}")
    private String issuerUri;

    /**
     * Configures the main security filter chain for the application
     *
     * Security Rules Hierarchy:
     * • Public endpoints: /api/public/**, /eureka/** - No authentication required
     * • Admin endpoints: /api/admin/** - Requires ADMIN role
     * • Business endpoints: /api/inventory/**, /api/order/**, etc. - Requires authentication
     * • All other endpoints: Require authentication
     */
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                // Disable CSRF for stateless API gateway
                .csrf(ServerHttpSecurity.CsrfSpec::disable)

                // Configure endpoint authorization rules
                .authorizeExchange(exchanges -> exchanges
                        // Public endpoints - no authentication required
                        .pathMatchers("/api/public/**", "/eureka/**").permitAll()

                        // Business endpoints - require authentication but no specific role
                        .pathMatchers("/api/inventory/**", "/api/order/**", "/api/product/**", "/api/test/**")
                        .authenticated()

                        // Admin endpoints - require ADMIN role
                        .pathMatchers("/api/admin/**").hasRole("ADMIN")

                        // Default rule - all other endpoints require authentication
                        .anyExchange().authenticated()
                )

                // Enable OAuth2 JWT resource server
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .build();
    }

    /**
     * Custom JWT authentication converter that extracts roles from Keycloak tokens
     *
     * This bean maps Keycloak-specific JWT claims to Spring Security authorities:
     * • Extracts realm-level roles from 'realm_access' claim
     * • Extracts client-specific roles from 'resource_access' claim
     * • Prefixes all roles with 'ROLE_' for Spring Security compatibility
     *
     * Note: Replace 'your-client-id' with your actual Keycloak client ID
     */
    @Bean
    public org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter jwtAuthenticationConverter() {
        var converter = new org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            List<GrantedAuthority> authorities = new ArrayList<>();

            // Extract realm-level roles (global roles in Keycloak)
            Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
            if (realmAccess != null) {
                @SuppressWarnings("unchecked")
                List<String> roles = (List<String>) realmAccess.get("roles");
                if (roles != null) {
                    authorities.addAll(roles.stream()
                            .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                            .toList());
                }
            }

            // Extract client-specific roles (roles scoped to specific client application)
            Map<String, Object> resourceAccess = jwt.getClaimAsMap("resource_access");
            if (resourceAccess != null) {
                // TODO: Replace 'your-client-id' with actual Keycloak client ID
                @SuppressWarnings("unchecked")
                Map<String, Object> client = (Map<String, Object>) resourceAccess.get("your-client-id");
                if (client != null) {
                    @SuppressWarnings("unchecked")
                    List<String> clientRoles = (List<String>) client.get("roles");
                    if (clientRoles != null) {
                        authorities.addAll(clientRoles.stream()
                                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                                .toList());
                    }
                }
            }

            return authorities;
        });
        return converter;
    }
}