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
    public String index() throws IOException {
        return servePage("index.html");
    }

    @GetMapping("/index.html")
    @ResponseBody
    public String indexHtml() throws IOException {
        return servePage("index.html");
    }

    @GetMapping("/admin.html")
    @ResponseBody
    public String adminHtml() throws IOException {
        return servePage("admin.html");
    }

    @GetMapping("/student.html")
    @ResponseBody
    public String studentHtml() throws IOException {
        return servePage("student.html");
    }

    private String servePage(String page) throws IOException {
        ClassPathResource resource = new ClassPathResource("META-INF/resources/" + page);
        try (InputStream is = resource.getInputStream()) {
            return StreamUtils.copyToString(is, StandardCharsets.UTF_8);
        }
    }
}