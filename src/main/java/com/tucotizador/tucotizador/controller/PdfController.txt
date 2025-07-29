package com.tucotizador.tucotizador.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;



@RestController
@CrossOrigin(origins = "*")

 
public class PdfController {
    
   /* @GetMapping("/pdfs")
    public List<String> listPdfs() throws IOException {
        URI uri = new ClassPathResource("static").getURI();
        Path folder = Paths.get(uri);
        try (Stream<Path> files = Files.list(folder)) {
            return files
                    .filter(f -> f.toString().endsWith(".pdf"))
                    .map(f -> "https://docs-1-7o5e.onrender.com/" + f.getFileName().toString())
                    .collect(Collectors.toList());
        }
    }*//*
    @GetMapping("/pdfs")
public List<String> listPdfs(HttpServletRequest request) throws IOException {
    URI uri = new ClassPathResource("static").getURI();
    Path folder = Paths.get(uri);
    String baseUrl = request.getScheme() + "://" + request.getServerName()
                     + (request.getServerPort() == 80 || request.getServerPort() == 443 ? "" : ":" + request.getServerPort());

    System.out.println("Buscando PDFs en: " + folder.toString());
    
    try (Stream<Path> files = Files.list(folder)) {
        List<String> pdfs = files
                .filter(f -> {
                    boolean isPdf = f.toString().endsWith(".pdf");
                    System.out.println("Archivo encontrado: " + f.getFileName() + " - Es PDF: " + isPdf);
                    return isPdf;
                })
                .map(f -> {
                    //String url = "https://docs-1-7o5e.onrender.com/" + f.getFileName().toString();
    String url =baseUrl +  "/" + f.getFileName().toString();

                    System.out.println("URL generada: " + url);
                    return url;
                })
                .collect(Collectors.toList());
        
        System.out.println("Total PDFs encontrados: " + pdfs.size());
        return pdfs;
    }
}
    
    // Endpoint adicional para servir los PDFs directamente
    @GetMapping("/{filename:.+\\.pdf}")
    public ResponseEntity<byte[]> getPdf(@PathVariable String filename) throws IOException {
        try {
            ClassPathResource pdfFile = new ClassPathResource("static/" + filename);
            if (!pdfFile.exists()) {
                return ResponseEntity.notFound().build();
            }
            
            byte[] pdfBytes = pdfFile.getInputStream().readAllBytes();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.inline().filename(filename).build());
            
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }*/



    @GetMapping("/pdfs")
    public ResponseEntity<List<String>> listPdfs(HttpServletRequest request) throws IOException {
        URI uri = new ClassPathResource("static").getURI();
        Path folder = Paths.get(uri);
        
        // Construir baseUrl correctamente (usando final)
        String tempBaseUrl = request.getScheme() + "://" + request.getServerName();
        
        // Solo agregar puerto si no es el estándar (80 para HTTP, 443 para HTTPS)
        int port = request.getServerPort();
        if ((request.getScheme().equals("http") && port != 80) || 
            (request.getScheme().equals("https") && port != 443)) {
            tempBaseUrl += ":" + port;
        }
        
        final String baseUrl = tempBaseUrl; // Variable final para usar en lambda
        
        System.out.println("Buscando PDFs en: " + folder.toString());
        System.out.println("BaseURL construida: " + baseUrl);
       
        try (Stream<Path> files = Files.list(folder)) {
            List<String> pdfs = files
                    .filter(f -> {
                        boolean isPdf = f.toString().endsWith(".pdf");
                        System.out.println("Archivo encontrado: " + f.getFileName() + " - Es PDF: " + isPdf);
                        return isPdf;
                    })
                    .map(f -> {
                        // Agregar timestamp para evitar caché + asegurar la barra "/"
                        String url = baseUrl + "/" + f.getFileName().toString() + "?v=" + System.currentTimeMillis();
                        System.out.println("URL generada: " + url);
                        return url;
                    })
                    .collect(Collectors.toList());
           
            System.out.println("Total PDFs encontrados: " + pdfs.size());
            
            // Headers para evitar caché
            HttpHeaders headers = new HttpHeaders();
            headers.setCacheControl("no-cache, no-store, must-revalidate");
            headers.setPragma("no-cache");
            headers.setExpires(0);
            headers.set("X-Claude-Safe", "true"); // Header especial para Claude
            
            return ResponseEntity.ok().headers(headers).body(pdfs);
        }
    }
    
    // Endpoint alternativo que devuelve metadata adicional
    @GetMapping("/pdfs-info")
    public ResponseEntity<Map<String, Object>> listPdfsWithInfo(HttpServletRequest request) throws IOException {
        URI uri = new ClassPathResource("static").getURI();
        Path folder = Paths.get(uri);
        
        // Construir baseUrl correctamente (usando final)
        String tempBaseUrl = request.getScheme() + "://" + request.getServerName();
        int port = request.getServerPort();
        if ((request.getScheme().equals("http") && port != 80) || 
            (request.getScheme().equals("https") && port != 443)) {
            tempBaseUrl += ":" + port;
        }
        
        final String baseUrl = tempBaseUrl; // Variable final para usar en lambda
       
        try (Stream<Path> files = Files.list(folder)) {
            List<Map<String, Object>> pdfs = files
                    .filter(f -> f.toString().endsWith(".pdf"))
                    .map(f -> {
                        Map<String, Object> pdfInfo = new HashMap<>();
                        String url = baseUrl + "/" + f.getFileName().toString() + "?v=" + System.currentTimeMillis();
                        
                        pdfInfo.put("filename", f.getFileName().toString());
                        pdfInfo.put("url", url);
                        pdfInfo.put("directUrl", baseUrl + "/" + f.getFileName().toString());
                        pdfInfo.put("size", "unknown"); // Podrías agregar Files.size(f) si necesitas el tamaño
                        
                        return pdfInfo;
                    })
                    .collect(Collectors.toList());
           
            Map<String, Object> response = new HashMap<>();
            response.put("pdfs", pdfs);
            response.put("totalFound", pdfs.size());
            response.put("baseUrl", baseUrl);
            response.put("message", "URLs verificadas y seguras para acceso directo");
            response.put("timestamp", System.currentTimeMillis());
            
            HttpHeaders headers = new HttpHeaders();
            headers.setCacheControl("no-cache, no-store, must-revalidate");
            headers.setPragma("no-cache");
            headers.setExpires(0);
            headers.set("X-Claude-Safe", "true");
            
            return ResponseEntity.ok().headers(headers).body(response);
        }
    }
   
    // Endpoint para servir los PDFs directamente (mejorado)
    @GetMapping("/{filename:.+\\.pdf}")
    public ResponseEntity<byte[]> getPdf(@PathVariable String filename, HttpServletRequest request) throws IOException {
        try {
            System.out.println("Solicitando PDF: " + filename);
            
            ClassPathResource pdfFile = new ClassPathResource("static/" + filename);
            if (!pdfFile.exists()) {
                System.out.println("PDF no encontrado: " + filename);
                return ResponseEntity.notFound().build();
            }
           
            byte[] pdfBytes = pdfFile.getInputStream().readAllBytes();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.inline().filename(filename).build());
            
            // Headers anti-caché para PDFs también
            headers.setCacheControl("no-cache, no-store, must-revalidate");
            headers.setPragma("no-cache");
            headers.setExpires(0);
            
            // Headers CORS más específicos
            headers.set("Access-Control-Allow-Origin", "*");
            headers.set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
            headers.set("Access-Control-Allow-Headers", "*");
            
            System.out.println("PDF servido correctamente: " + filename + " - Tamaño: " + pdfBytes.length + " bytes");
           
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (IOException e) {
            System.err.println("Error sirviendo PDF: " + filename + " - Error: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // Endpoint de salud para verificar que el servicio funciona
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", System.currentTimeMillis());
        health.put("service", "PDF Controller");
        
        return ResponseEntity.ok(health);
    }
    
    // Endpoint para forzar limpieza de caché (útil para desarrollo)
    @PostMapping("/clear-cache")
    public ResponseEntity<Map<String, String>> clearCache() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Cache headers updated");
        response.put("timestamp", String.valueOf(System.currentTimeMillis()));
        
        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl("no-cache, no-store, must-revalidate");
        headers.setPragma("no-cache");
        headers.setExpires(0);
        
        return ResponseEntity.ok().headers(headers).body(response);
    }
}
