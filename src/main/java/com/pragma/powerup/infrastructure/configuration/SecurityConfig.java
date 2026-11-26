package com.pragma.powerup.infrastructure.configuration;

import com.pragma.powerup.infrastructure.out.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, @Value("${jwt.secret}") String secret) throws Exception {

        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html",
                                "/v3/api-docs/**", "/error").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/restaurant").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/restaurant").hasRole("CLIENT")
                        .requestMatchers(HttpMethod.POST, "/api/v1/restaurant/*/employee").hasRole("OWNER")
                        .requestMatchers(HttpMethod.POST, "/api/v1/dish").hasRole("OWNER")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/dish/**").hasRole("OWNER")
                        .requestMatchers(HttpMethod.GET, "/api/v1/dish").hasRole("CLIENT")
                        .requestMatchers(HttpMethod.POST, "/api/v1/order").hasRole("CLIENT")
                        .requestMatchers(HttpMethod.GET, "/api/v1/order").hasRole("EMPLOYEE")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/order/*/assign").hasRole("EMPLOYEE")
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                        .accessDeniedHandler((request, response, e) ->
                                response.setStatus(HttpStatus.FORBIDDEN.value())))
                .addFilterBefore(new JwtAuthenticationFilter(secret),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
