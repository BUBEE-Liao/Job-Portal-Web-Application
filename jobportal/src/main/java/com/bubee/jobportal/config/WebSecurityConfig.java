package com.bubee.jobportal.config;

import com.bubee.jobportal.services.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import javax.swing.*;

@Configuration
public class WebSecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;
    @Autowired
    public WebSecurityConfig(CustomUserDetailsService customUserDetailsService, CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler) {
        this.customUserDetailsService = customUserDetailsService;
        this.customAuthenticationSuccessHandler = customAuthenticationSuccessHandler;
    }

    private final String[] publicUrl = {"/",
            "/global-search/**",
            "/register",
            "/register/**",
            "/webjars/**",
            "/resources/**",
            "/assets/**",
            "/css/**",
            "/summernote/**",
            "/js/**",
            "/*.css",
            "/*.js",
            "/*.js.map",
            "/fonts**", "/favicon.ico", "/resources/**", "/error"};

    @Bean
    protected SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.authenticationProvider(authenticationProvider());

        http.authorizeHttpRequests(auth -> {
            // anyone can access these endpoints without having to log in
            auth.requestMatchers(publicUrl).permitAll();
            // any other request that comes in has to be authenticated
            // i.e. the user have to log in
            auth.anyRequest().authenticated();
        });

        http.formLogin(form-> form.loginPage("/login").permitAll() // anyone can access the log in page
                // need a success handler, implements this handler
                // when user logged in, we can handle it, e.g. sent them to "/dashboard/"
                .successHandler(customAuthenticationSuccessHandler))
                .logout(logout-> {
                    // specify the logout url and logout success url
                    logout.logoutUrl("/logout");
                    logout.logoutSuccessUrl("/");
                    // additional configuration, need to figure out latter
                }).cors(Customizer.withDefaults())
                .csrf(csrf->csrf.disable());

        return http.build();
    }

    // Our custom authentication provider
    // Tell Spring Security how to find our users and how to authenticate passwords for the users
    // need to : 1. create a DaoAuthenticationProvider object
    //           2. set password encoder
    //           3. set UserDetailsService
    @Bean
    public AuthenticationProvider authenticationProvider() {

        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        // Tell Spring Security how to retrieve the users from the database
        authenticationProvider.setUserDetailsService(customUserDetailsService);
        return authenticationProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
