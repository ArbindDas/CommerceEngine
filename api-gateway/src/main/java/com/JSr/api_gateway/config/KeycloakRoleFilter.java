package com.JSr.api_gateway.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

/**
 * KeycloakRoleFilter - Per-Route Role Authorization Filter
 *
 * This filter provides fine-grained role-based authorization for specific API routes.
 * It acts as a secondary security layer that checks if a user has the required role
 * for a particular route, beyond the global authentication provided by SecurityConfig.
 *
 * Key Functionality:
 * • Checks if current route requires a specific role
 * • Verifies user has the required role
 * • Blocks access if role is missing
 * • Adds user information to headers for downstream services
 *
 * Analogy: It's like having bouncers at specific doors (routes) rather than just
 * security at the main entrance (global authentication).
 */
@Component
public class KeycloakRoleFilter extends AbstractGatewayFilterFactory<KeycloakRoleFilter.Config> {

    public KeycloakRoleFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            return ReactiveSecurityContextHolder.getContext()
                    .map(SecurityContext::getAuthentication)
                    .flatMap(authentication -> {
                        // Check if user is authenticated
                        if (authentication == null || !authentication.isAuthenticated()) {
                            return onError(exchange, "Unauthorized", HttpStatus.UNAUTHORIZED);
                        }

                        // Extract and normalize roles from authentication
                        List<String> roles = authentication.getAuthorities().stream()
                                .map(GrantedAuthority::getAuthority)
                                .map(role -> role.startsWith("ROLE_") ? role.substring(5) : role)
                                .collect(Collectors.toList());

                        // Validate role requirements for this route
                        if (config.getRequiredRole() != null && !config.getRequiredRole().isEmpty()) {
                            if (!roles.contains(config.getRequiredRole())) {
                                return onError(exchange,
                                        "Insufficient permissions. Required role: " + config.getRequiredRole(),
                                        HttpStatus.FORBIDDEN);
                            }
                        }

                        // Propagate user context to downstream services
                        ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                                .header("X-User-Roles", String.join(",", roles))
                                .header("X-User-Name", authentication.getName())
                                .header("X-User-Authenticated", "true")
                                .build();

                        return chain.filter(exchange.mutate().request(modifiedRequest).build());
                    })
                    .switchIfEmpty(onError(exchange, "Unauthorized", HttpStatus.UNAUTHORIZED));
        };
    }

    /**
     * Handles authorization errors by setting appropriate HTTP status and error headers
     */
    private Mono<Void> onError(ServerWebExchange exchange, String error, HttpStatus httpStatus) {
        exchange.getResponse().setStatusCode(httpStatus);
        exchange.getResponse().getHeaders().add("X-Error-Message", error);
        return exchange.getResponse().setComplete();
    }

    /**
     * Configuration class for route-specific role requirements
     */

    public static class Config {

        /**
         * The specific role required to access this route
         * Example: "ADMIN", "INVENTORY_MANAGER", "ORDER_PROCESSOR"
         */
        private String requiredRole;

        /**
         * Optional client ID for client-specific role mapping
         */
        private String clientId;

        public String getRequiredRole() {
            return requiredRole;
        }

        public void setRequiredRole(String requiredRole) {
            this.requiredRole = requiredRole;
        }

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }
    }
}