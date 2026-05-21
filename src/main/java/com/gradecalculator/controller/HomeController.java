package com.gradecalculator.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Serves the frontend UI.
 */
@Controller
public class HomeController {

    @GetMapping("/")
    @ResponseBody
    public String home() throws IOException {
        return servePage("home.html");
    }

    @GetMapping("/home.html")
    @ResponseBody
    public String homeHtml() throws IOException {
        return servePage("home.html");
    }

    @GetMapping("/index.html")
    @ResponseBody
    public String indexHtml() throws IOException {
        return servePage("index.html");
    }

    @GetMapping("/analytics")
    @ResponseBody
    public String analytics() throws IOException {
        return servePage("analytics.html");
    }

    @GetMapping("/transcript")
    @ResponseBody
    public String transcript() throws IOException {
        return servePage("transcript.html");
    }

    @GetMapping("/faculty-grades")
    @ResponseBody
    public String facultyGrades() throws IOException {
        return servePage("faculty-grades.html");
    }

    @GetMapping("/student-dashboard")
    @ResponseBody
    public String studentDashboard() throws IOException {
        return servePage("student-dashboard.html");
    }

    @GetMapping("/profile")
    @ResponseBody
    public String profile() throws IOException {
        return servePage("profile.html");
    }

    private String servePage(String page) throws IOException {
        ClassPathResource resource = new ClassPathResource("META-INF/resources/" + page);
        try (InputStream is = resource.getInputStream()) {
            return StreamUtils.copyToString(is, StandardCharsets.UTF_8);
        }
    }
}