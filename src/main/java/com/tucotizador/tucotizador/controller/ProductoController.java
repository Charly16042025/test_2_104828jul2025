package com.tucotizador.tucotizador.controller;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;


@RestController
@RequestMapping("/producto")
public class ProductoController {

    private final String SUPABASE_URL = "https://itgiewpjoyketotyzzhy.supabase.co/rest/v1/products";
    private final String API_KEY = " eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Iml0Z2lld3Bqb3lrZXRvdHl6emh5Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDgzNjM0OTIsImV4cCI6MjA2MzkzOTQ5Mn0.Ip72t0UOiWJGq_RZ780GMzAj1xMN9SBY7aQjnLDaGTQ" + //
                ""; // tu anon-key

    @GetMapping
    public ResponseEntity<String> getProductoPorNombre(@RequestParam String nombre) {
        try {
            HttpClient client = HttpClient.newHttpClient();

            String url = SUPABASE_URL + "?select=name,sale_price&name=eq." + URLEncoder.encode(nombre, StandardCharsets.UTF_8);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("apikey", API_KEY)
                    .header("Authorization", "Bearer " + API_KEY)
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            return ResponseEntity.ok(response.body());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
}
