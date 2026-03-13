package com.resumeanalyzer.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Prevents Render free-tier from spinning down the server.
 * Pings the health endpoint every 10 minutes so the app stays warm
 * and Googlebot can always reach /sitemap.xml.
 */
@Service
public class KeepAliveService {

    private static final Logger log = LoggerFactory.getLogger(KeepAliveService.class);
    private static final String HEALTH_URL = "https://resume-anlyzer-0cit.onrender.com/api/health";

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    @Scheduled(fixedRate = 600_000) // every 10 minutes
    public void keepAlive() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(HEALTH_URL))
                    .timeout(Duration.ofSeconds(30))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            log.info("Keep-alive ping: status={}", response.statusCode());
        } catch (Exception e) {
            log.warn("Keep-alive ping failed: {}", e.getMessage());
        }
    }
}
