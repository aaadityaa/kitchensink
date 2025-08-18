package com.mongodb.kitchensink.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping("/")
    public String home() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/signup")
    public String signupPage() {
        return "signup";
    }

    @GetMapping("/dashboard")
    public String dashboardPage() {
        return "dashboard";
    }

    @GetMapping("/user/dashboard")
    public String userDashboardPage() {
        return "user-dashboard";
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboardPage() {
        return "admin-dashboard";
    }
}

