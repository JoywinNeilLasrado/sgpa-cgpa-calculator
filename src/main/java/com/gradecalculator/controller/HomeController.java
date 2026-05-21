package com.gradecalculator.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
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
        ClassPathResource resource = new ClassPathResource("META-INF/resources/index.html");
        try (InputStream is = resource.getInputStream()) {
            return StreamUtils.copyToString(is, StandardCharsets.UTF_8);
        }
    }
}