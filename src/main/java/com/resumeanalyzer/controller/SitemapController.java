package com.resumeanalyzer.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Serves sitemap.xml dynamically with the correct Content-Type header.
 * This ensures Google Search Console can always fetch the sitemap reliably,
 * even on Render's free tier where static file serving may behave inconsistently.
 */
@RestController
public class SitemapController {

    private static final String SITEMAP_XML = """
            <?xml version="1.0" encoding="UTF-8"?>
            <urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
              <url>
                <loc>https://resume-anlyzer-0cit.onrender.com/</loc>
                <lastmod>2026-03-13</lastmod>
                <changefreq>weekly</changefreq>
                <priority>1.0</priority>
              </url>
            </urlset>
            """;

    @GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> sitemap() {
        return ResponseEntity.ok()
                .header("Content-Type", "application/xml; charset=UTF-8")
                .header("Cache-Control", "public, max-age=86400")
                .header("X-Robots-Tag", "noindex")
                .body(SITEMAP_XML);
    }
}
