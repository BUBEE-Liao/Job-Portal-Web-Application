package com.bubee.jobportal.controller;

import com.bubee.jobportal.entity.JobSeekerProfile;
import com.bubee.jobportal.entity.Skills;
import com.bubee.jobportal.entity.Users;
import com.bubee.jobportal.repository.UsersRepository;
import com.bubee.jobportal.services.JobSeekerProfileService;
import com.bubee.jobportal.util.FileDownloadUtil;
import com.bubee.jobportal.util.FileUploadUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Controller
@RequestMapping("/job-seeker-profile")
public class JobSeekerProfileController {

    private JobSeekerProfileService jobSeekerProfileService;

    private UsersRepository usersRepository;

    @Autowired
    public JobSeekerProfileController(JobSeekerProfileService jobSeekerProfileService, UsersRepository usersRepository) {
        this.jobSeekerProfileService = jobSeekerProfileService;
        this.usersRepository = usersRepository;
    }

    @GetMapping("/")
    public String jobSeekerProfile(Model model) {
        JobSeekerProfile jobSeekerProfile = new JobSeekerProfile();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        List<Skills> skills = new ArrayList<>();

        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            Users user = usersRepository.findByEmail(authentication.getName()).orElseThrow(() -> new UsernameNotFoundException("User not found."));
            Optional<JobSeekerProfile> seekerProfile = jobSeekerProfileService.getOne(user.getUserId());
            if (seekerProfile.isPresent()) {
                jobSeekerProfile = seekerProfile.get();
                if (jobSeekerProfile.getSkills().isEmpty()) {
                    skills.add(new Skills());
                    jobSeekerProfile.setSkills(skills);
                }
            }
            // why do we need to add this attribute?
            //model.addAttribute("skills", skills);
            model.addAttribute("profile", jobSeekerProfile);
        }

        return "job-seeker-profile";
    }

    @PostMapping("/addNew")
    public String addNew(JobSeekerProfile jobSeekerProfile,
                         @RequestParam("image") MultipartFile image,
                         @RequestParam("pdf") MultipartFile pdf,
                         Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            Users user = usersRepository.findByEmail(authentication.getName()).orElseThrow(() -> new UsernameNotFoundException("User not found."));
            jobSeekerProfile.setUserId(user);
            jobSeekerProfile.setUserAccountId(user.getUserId());
        }

        List<Skills> skillsList = new ArrayList<>();
        model.addAttribute("profile", jobSeekerProfile);
        model.addAttribute("skills", skillsList);

        // associate the skills with the appropriate job seeker profile accordingly
        // there's no repository related to Skills entity
        // so there's the reason we use @ManyToOne annotation to save skills with a cascade style
        for (Skills skills : jobSeekerProfile.getSkills()) {
            skills.setJobSeekerProfile(jobSeekerProfile);
        }

        String imageName = "";
        String resumeName = "";

        // if image path is not ""
        if (!Objects.equals(image.getOriginalFilename(), "")) {
            imageName = StringUtils.cleanPath(Objects.requireNonNull(image.getOriginalFilename()));
            jobSeekerProfile.setProfilePhoto(imageName);
        }
        // if pdf path is not ""
        if (!Objects.equals(pdf.getOriginalFilename(), "")) {
            resumeName = StringUtils.cleanPath(Objects.requireNonNull(pdf.getOriginalFilename()));
            jobSeekerProfile.setResume(resumeName);
        }

        JobSeekerProfile seekerProfile = jobSeekerProfileService.addNew(jobSeekerProfile);

        try {
            // similar way to save the file to the server
            String uploadDir = "photos/candidate/" + jobSeekerProfile.getUserAccountId();
            if (!Objects.equals(image.getOriginalFilename(), "")) {
                FileUploadUtil.saveFile(uploadDir, imageName, image);
            }
            if (!Objects.equals(pdf.getOriginalFilename(), "")) {
                FileUploadUtil.saveFile(uploadDir, resumeName, pdf);
            }
        }
        catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        return "redirect:/dashboard/";
    }

    // in "job-seeker-profile.html", it will iterate each apply for a post
    // id : user profile apply for this post
    // store the user profile and retrieve this profile to recruiter
    public String candidateProfile(@PathVariable("id") int id, Model model) {
        Optional<JobSeekerProfile> seekerProfile = jobSeekerProfileService.getOne(id);
        model.addAttribute("profile", seekerProfile.get());
        return "job-seeker-profile";
    }

    // if recruiter click "download" button, we have to handle this
    // the return type is special, it's "ResponseEntity"
    @GetMapping("/downloadResume")
    public ResponseEntity<?> downloadResume(@RequestParam(value = "fileName") String fileName, @RequestParam(value = "userID") String userId) {

        FileDownloadUtil downloadUtil = new FileDownloadUtil();
        Resource resource = null;

        try {
            resource = downloadUtil.getFileAsResourse("photos/candidate/" + userId, fileName);
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        }

        if (resource == null) {
            return new ResponseEntity<>("File not found", HttpStatus.NOT_FOUND);
        }

        // set the content type and the actual header value
        // because we're going to send back a file to download
        // so we set that accordingly with the content type of application octet stream
        // hence your browser will know that we're sending over a binary file or a stream of binary file or octets
        String contentType = "application/octet-stream";
        // we give the name of the file that we're passing over as far as that download
        String headerValue = "attachment; filename=\"" + resource.getFilename() + "\"";

        // return a response with the content type, headers and then the actual resource
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, headerValue)
                // resource will be placed in body
                .body(resource);

    }
}











