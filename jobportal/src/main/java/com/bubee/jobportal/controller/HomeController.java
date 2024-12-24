package com.bubee.jobportal.controller;

import org.springframework.stereotype.Controller;

// get request mapping for "/"
@Controller
public class HomeController {

    public String home() {
        return "index";
    }
}
