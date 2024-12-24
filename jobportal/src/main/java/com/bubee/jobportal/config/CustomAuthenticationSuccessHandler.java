package com.bubee.jobportal.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

// Once logged in successfully, retrieve the user profile, check the roles for the user
// if role belongs to job seeker or recruiter, send them to dashboard page
// need to implement inference "AuthenticationSuccessHandler"
@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        // get the logged in user's detail, cast it accordingly
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();
        System.out.println("The username " + username + " is logged in.");
        // see if the user match the roles we expect
        // getAuthority() is override by CustomUserDetails.java(extend UserDetails class)
        boolean hasJobSeekerRole = authentication.getAuthorities().stream().anyMatch(r->r.getAuthority().equals("Job Seeker"));
        boolean hasRecruiterRole = authentication.getAuthorities().stream().anyMatch(r->r.getAuthority().equals("Recruiter"));

        if (hasRecruiterRole || hasJobSeekerRole) {
            // if match, redirect to the dashboard
            response.sendRedirect("/dashboard/");
        }
    }
}
