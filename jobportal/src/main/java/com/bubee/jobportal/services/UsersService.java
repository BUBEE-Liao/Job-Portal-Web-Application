package com.bubee.jobportal.services;

import com.bubee.jobportal.entity.JobSeekerProfile;
import com.bubee.jobportal.entity.RecruiterProfile;
import com.bubee.jobportal.entity.Users;
import com.bubee.jobportal.repository.JobSeekerProfileRepository;
import com.bubee.jobportal.repository.RecruiterProfileRepository;
import com.bubee.jobportal.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
public class UsersService {

    private final UsersRepository usersRepository;
    private final JobSeekerProfileRepository jobSeekerProfileRepository;
    private final RecruiterProfileRepository recruiterProfileRepository;
    // During registration, encrypt user password
    // because of @Bean, we cna use Autowired to inject (maybe)
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UsersService(UsersRepository usersRepository, JobSeekerProfileRepository jobSeekerProfileRepository, RecruiterProfileRepository recruiterProfileRepository, PasswordEncoder passwordEncoder) {
        this.usersRepository = usersRepository;
        this.jobSeekerProfileRepository = jobSeekerProfileRepository;
        this.recruiterProfileRepository = recruiterProfileRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Users addNew(Users users) {
        users.setActive(true);
        users.setRegistrationDate(new Date(System.currentTimeMillis()));
        users.setPassword(passwordEncoder.encode(users.getPassword()));
        Users savedUser = usersRepository.save(users);
        int userTypeId = users.getUserTypeId().getUserTypeId();

        if (userTypeId == 1) {
            recruiterProfileRepository.save(new RecruiterProfile(savedUser));
        }
        else {
            jobSeekerProfileRepository.save(new JobSeekerProfile(savedUser));
        }

        return savedUser;
    }
    // for JobPostActivityController
    public Object getCurrentUserProfile() {

        // get current user's profile
        // It's common to execute this operation, it's important
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // make sure the user is not anonymous
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            // getName : overwrite by CustomUserDetails, return getEmail()
            String username = authentication.getName();
            // get the user
            Users users = usersRepository.findByEmail(username).orElseThrow(()-> new UsernameNotFoundException("Could not found " + "user"));
            int userId = users.getUserId();
            // getAuthorities : overwrite by CustomUserDetails
            if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("Recruiter"))) {
                // if the database not found, return a new one
                RecruiterProfile recruiterProfile = recruiterProfileRepository.findById(userId).orElse(new RecruiterProfile());
                return recruiterProfile;
            } else {
                JobSeekerProfile jobSeekerProfile = jobSeekerProfileRepository.findById(userId).orElse(new JobSeekerProfile());
                return jobSeekerProfile;
            }
        }

        return null;
    }

    public Users getCurrentUser() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String username = authentication.getName();
            Users user = usersRepository.findByEmail(username).orElseThrow(() -> new UsernameNotFoundException("Could not found " + "user"));
            return user;
        }

        return null;
    }

    public Users findByEmail(String currentUsername) {
        return usersRepository.findByEmail(currentUsername).orElseThrow(() -> new UsernameNotFoundException("User not " +
                "found"));
    }

    public Optional<Users> getUserByEmail(String email) {
        return usersRepository.findByEmail(email);
    }
    
}








