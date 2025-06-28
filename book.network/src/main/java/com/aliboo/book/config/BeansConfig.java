package com.aliboo.book.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.Collections;

import static org.springframework.http.HttpHeaders.*;

@Configuration
@RequiredArgsConstructor

public class BeansConfig {
    private final UserDetailsService userDetailsService;

    @Bean
    public AuthenticationProvider authenticationProvider(){//AuthenticationProvider interface fiha bzf mnha DaoAuthenticationProvider li ktmnkna nkhdmo b passencoder... o userdetails li hia interface thia o fiha load by username...
        DaoAuthenticationProvider authprovider = new DaoAuthenticationProvider(); //hna dao drnaha f var bsh nkhrjo mnha ltht l function libaghyin
        authprovider.setUserDetailsService(userDetailsService);
        authprovider.setPasswordEncoder(passwordEncoder());
        return authprovider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager(); //jib lia l authenticationmanager mn AuthenticationConfiguration hd obj ankhdmo biha f authservice
    }

    @Bean
    public AuditorAware<Integer> auditorAware() {
        return new ApplicationAuditAware(); //class dyalna 3iytna liha hna bch ykhdm biha spring
    }

    @Bean
    public PasswordEncoder passwordEncoder() { //PasswordEncoder interface fiha bzf bch t encoder pass mnhom BCryptPasswordEncoder
        return new BCryptPasswordEncoder();
    }

    //hd bean bsh ymkn fsh y dir login fl front end ankhliw spring y dir allowed l data li jaya mn front end mn angular
    @Bean
    public CorsFilter corsFilter() {
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        final CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOrigins (Collections.singletonList("http://localhost:4200"));
        config.setAllowedHeaders (Arrays.asList(
                ORIGIN,
                CONTENT_TYPE,
                ACCEPT,
                AUTHORIZATION
        ));
        config.setAllowedMethods (Arrays.asList(
                "GET",
                "POST",
                "DELETE",
                "PUT",
                "PATCH"
        ));
        source.registerCorsConfiguration("/**", config);// hd l config khdmha f gae paths /books /feedbacks...
        return new CorsFilter(source);
    }

}
