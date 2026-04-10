package com.fitness.fitnessaicoach.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy;
import org.springframework.security.web.header.writers.StaticHeadersWriter;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private static final String BASELINE_CSP =
            "default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'; " +
                    "img-src 'self' data:; connect-src 'self'";
    private static final String MINIMAL_PERMISSIONS_POLICY = "geolocation=(), camera=(), microphone=()";

    private final JwtFilter jwtFilter;
    private final Environment environment;
    @Value("${app.swagger.public:false}")
    private boolean swaggerPublic;
    @Value("${spring.h2.console.enabled:false}")
    private boolean h2ConsoleEnabled;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .headers(headers -> {
                    headers
                            .contentTypeOptions(withDefaults())
                            .referrerPolicy(referrer -> referrer.policy(ReferrerPolicy.NO_REFERRER))
                            .addHeaderWriter(new StaticHeadersWriter("Permissions-Policy", MINIMAL_PERMISSIONS_POLICY));

                    if (isDevOrLocalProfileActive() && h2ConsoleEnabled) {
                        headers.frameOptions(frame -> frame.sameOrigin());
                    } else {
                        headers.frameOptions(frame -> frame.deny());
                    }

                    // Swagger UI and test slices rely on inline assets, so keep CSP for app endpoints.
                    if (!isTestProfileActive() && !swaggerPublic) {
                        headers.contentSecurityPolicy(csp -> csp.policyDirectives(BASELINE_CSP));
                    }
                })

                .authorizeHttpRequests(auth -> {
                        var rules = auth

                        .requestMatchers(
                                "/api/auth/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/users").permitAll();
                        rules.requestMatchers("/api/health/**").permitAll();

                        if (swaggerPublic) {
                            rules.requestMatchers(
                                    "/v3/api-docs/**",
                                    "/swagger-ui/**",
                                    "/swagger-ui.html"
                            ).permitAll();
                        }

                        rules.anyRequest().authenticated();
                })

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) ->
                                response.sendError(HttpStatus.UNAUTHORIZED.value(), "Unauthorized"))
                )

                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private boolean isDevOrLocalProfileActive() {
        return environment.acceptsProfiles(Profiles.of("dev", "local"));
    }

    private boolean isTestProfileActive() {
        return environment.acceptsProfiles(Profiles.of("test"));
    }
}
