package com.resumeanalyzer.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Serves robots.txt dynamically with the correct Content-Type.
 */
@RestController
public class RobotsTxtController {

    private static final String ROBOTS_TXT = """
            User-agent: *
            Allow: /

            Sitemap: https://resume-anlyzer-0cit.onrender.com/sitemap.xml
            """;

    @GetMapping(value = "/robots.txt", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> robots() {
        return ResponseEntity.ok()
                .header("Content-Type", "text/plain; charset=UTF-8")
                .header("Cache-Control", "public, max-age=86400")
                .body(ROBOTS_TXT);
    }
}
