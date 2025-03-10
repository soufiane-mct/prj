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




}
