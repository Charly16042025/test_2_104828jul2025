package com.tucotizador.tucotizador.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@CrossOrigin(origins = "*")
public class PdfController {
    
    @GetMapping("/pdfs")
    public List<String> listPdfs() throws IOException {
        URI uri = new ClassPathResource("static").getURI();
        Path folder = Paths.get(uri);
        try (Stream<Path> files = Files.list(folder)) {
            return files
                    .filter(f -> f.toString().endsWith(".pdf"))
                    .map(f -> "https://docs-1-7o5e.onrender.com/" + f.getFileName().toString())
                    .collect(Collectors.toList());
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
    }
}