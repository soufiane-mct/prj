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
                                         "/swagger-ui.html"
                        ).permitAll()//hna hd request li drna f url "" dkhlhom
                                .anyRequest()
                                .authenticated()//hna ayrequest akher dir lih authenticated (y3ni mdkhlosh)
                        )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) //hna fsh user aysift shi request spring ayreacti bhala maerfsh dk request
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);//hna drna filter akher dyalna lihowa jwtAuthFilter (o ydirha 9bl mn UsernamePasswordAuthenticationFilter class)
        return http.build();
    }
}




















