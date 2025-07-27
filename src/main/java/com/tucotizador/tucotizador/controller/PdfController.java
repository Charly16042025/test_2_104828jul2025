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
import java.io.IOException;
/* 
@RestController
@CrossOrigin(origins = "*")  // 🔓 Habilita CORS para cualquier origen
public class PdfController {

    @GetMapping("/tabla-de-precios")
    public ResponseEntity<byte[]> getPdf() throws IOException {
        ClassPathResource pdfFile = new ClassPathResource("static/tabla_de_precios.pdf");

        byte[] pdfBytes = pdfFile.getInputStream().readAllBytes();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.inline().filename("tabla_de_precios.pdf").build());

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}
*/

/*package com.tucotizador.tucotizador.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.*;


*/
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
                    .map(f -> "/" + f.getFileName().toString())
                    .collect(Collectors.toList());
        }
    }
}
