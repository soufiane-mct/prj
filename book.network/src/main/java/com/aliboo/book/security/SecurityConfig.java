package com.aliboo.book.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity(securedEnabled = true)


public class SecurityConfig {
    private final JwtFilter jwtAuthFilter;
    //mhd endna RequiredArgsConstructor kn khdmo b final bsh ngolo l lombok
    private final AuthenticationProvider authenticationProvider;

    @Bean //hdi bhal bsh dir shi method o tkhdm biha
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer:: disable)
                .authorizeHttpRequests(req->
                        req.requestMatchers(
                                "/auth/**",
                                "/v2/api-docs",
                                "/v3/api-docs",
                                "/v3/api-docs/**",
                                "/swagger-resources",
                                "/swagger-resources/**",
                                "/configuration/ui",
                                "/configuration/security",
                                "/swagger-ui/**",
                                "/webjars/**",
                                "/swagger-ui.html",
                                "/books",
                                "/books/",
                                "/books/**",
                                "/categories",
                                "/categories/",
                                "/guest-rent",
                                "/guest-rent/",
                                "/api/v1/guest-rent",
                                "/feedbacks",
                                "/feedbacks/**"
                        ).permitAll()
                        .requestMatchers("/categories/**").permitAll()
                        .requestMatchers("/books").permitAll()
                        .requestMatchers("/books/").permitAll()
                        .requestMatchers("/books/**").permitAll()
                        .requestMatchers("/guest-rent").permitAll()
                        .requestMatchers("/guest-rent/").permitAll()
                        .requestMatchers("/feedbacks/**").permitAll()
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        .requestMatchers("/admin-panel/login").permitAll()
                        .requestMatchers("/admin-panel/**").hasRole("ADMIN")
                        .anyRequest()
                        .authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) //hna fsh user aysift shi request spring ayreacti bhala maerfsh dk request
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);//hna drna filter akher dyalna lihowa jwtAuthFilter (o ydirha 9bl mn UsernamePasswordAuthenticationFilter class)
        return http.build();
    }
}




















