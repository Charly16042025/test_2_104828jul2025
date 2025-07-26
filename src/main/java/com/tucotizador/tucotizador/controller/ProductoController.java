/*

package com.tucotizador.tucotizador.controller;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*")
public class ProductoController {
    
    private final String SUPABASE_URL = "https://itgiewpjoyketotyzzhy.supabase.co/rest/v1/products";
    private final String API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Iml0Z2lld3Bqb3lrZXRvdHl6emh5Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDgzNjM0OTIsImV4cCI6MjA2MzkzOTQ5Mn0.Ip72t0UOiWJGq_RZ780GMzAj1xMN9SBY7aQjnLDaGTQ";

    @GetMapping("/producto")
    public ResponseEntity<String> getProductoPorNombre(@RequestParam String nombre) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            // CORREGIDO: usar product_name en lugar de name
            String url = SUPABASE_URL + "?select=product_name,sale_price&product_name=ilike.*" + URLEncoder.encode(nombre, StandardCharsets.UTF_8) + "*";
            
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
            return ResponseEntity.status(500).body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    // Endpoint para servir el ai-plugin.json
    @GetMapping("/.well-known/ai-plugin.json")
    public ResponseEntity<String> getAiPlugin() {
        String aiPlugin = """
        {
            "schema_version": "v1",
            "name_for_human": "Agente de Productos",
            "name_for_model": "productos_agent",
            "description_for_human": "Consulta productos por nombre desde Supabase.",
            "description_for_model": "Consulta productos usando el endpoint /producto",
            "auth": {
                "type": "none"
            },
            "api": {
                "type": "openapi",
                "url": "https://supabase-products-agent.onrender.com/.well-known/openapi.yaml"
            }
        }
        """;
        return ResponseEntity.ok()
                .header("Content-Type", "application/json")
                .body(aiPlugin);
    }

    // Endpoint para servir el openapi.yaml
    @GetMapping("/.well-known/openapi.yaml")
    public ResponseEntity<String> getOpenApiSpec() {
        String openApiSpec = """
        openapi: 3.0.1
        info:
          title: Supabase Products API
          version: 1.0
        servers:
          - url: https://supabase-products-agent.onrender.com
        paths:
          /producto:
            get:
              summary: Obtener producto por nombre
              parameters:
                - name: nombre
                  in: query
                  required: true
                  schema:
                    type: string
              responses:
                '200':
                  description: Producto con precio
                  content:
                    application/json:
                      schema:
                        type: array
                        items:
                          type: object
                          properties:
                            product_name:
                              type: string
                            sale_price:
                              type: number
        """;
        return ResponseEntity.ok()
                .header("Content-Type", "text/yaml")
                .body(openApiSpec);
    }
}*/

package com.tucotizador.tucotizador.controller;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*")
public class ProductoController {
    
    private final String SUPABASE_URL = "https://itgiewpjoyketotyzzhy.supabase.co/rest/v1/products";
    private final String API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Iml0Z2lld3Bqb3lrZXRvdHl6emh5Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDgzNjM0OTIsImV4cCI6MjA2MzkzOTQ5Mn0.Ip72t0UOiWJGq_RZ780GMzAj1xMN9SBY7aQjnLDaGTQ";

    // Endpoint original - búsqueda específica
    @GetMapping("/producto")
    public ResponseEntity<String> getProductoPorNombre(@RequestParam String nombre) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            String url = SUPABASE_URL + "?select=product_name,sale_price&product_name=ilike.*" + URLEncoder.encode(nombre, StandardCharsets.UTF_8) + "*";
            
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
            return ResponseEntity.status(500).body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    // NUEVO: Endpoint para obtener TODOS los productos (para análisis automático)
    @GetMapping("/productos/todos")
    public ResponseEntity<String> getTodosLosProductos() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            String url = SUPABASE_URL + "?select=*&limit=1000";
            
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
            return ResponseEntity.status(500).body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    // NUEVO: Endpoint para búsqueda inteligente/flexible
    @GetMapping("/productos/buscar")
    public ResponseEntity<String> buscarProductosInteligente(@RequestParam String consulta) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            
            // Buscar en múltiples campos con OR
            String url = SUPABASE_URL + "?select=*&or=(product_name.ilike.*" + 
                URLEncoder.encode(consulta, StandardCharsets.UTF_8) + "*,barcode.ilike.*" + 
                URLEncoder.encode(consulta, StandardCharsets.UTF_8) + "*)";
            
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
            return ResponseEntity.status(500).body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    // NUEVO: Endpoint para obtener estadísticas de la base de datos
    @GetMapping("/productos/info")
    public ResponseEntity<String> getInfoBaseDatos() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            String url = SUPABASE_URL + "?select=*&limit=5"; // Solo primeros 5 para mostrar estructura
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("apikey", API_KEY)
                    .header("Authorization", "Bearer " + API_KEY)
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            // Agregar información adicional
            String info = """
            {
                "database_info": {
                    "table": "products",
                    "main_columns": ["id", "barcode", "product_name", "sale_price", "purchase_price"],
                    "description": "Base de datos de productos con precios de venta",
                    "total_visible_records": "Máximo 1000",
                    "search_capabilities": ["Búsqueda por nombre", "Búsqueda por código de barras", "Búsqueda flexible"]
                },
                "sample_data": """ + response.body() + """
            }
            """;
            
            return ResponseEntity.ok(info);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    // Endpoints para el MCP (mantenemos los originales)
    @GetMapping("/.well-known/ai-plugin.json")
    public ResponseEntity<String> getAiPlugin() {
        String aiPlugin = """
        {
            "schema_version": "v1",
            "name_for_human": "Agente de Productos Inteligente",
            "name_for_model": "productos_agent",
            "description_for_human": "Consulta productos por nombre desde Supabase con búsqueda inteligente.",
            "description_for_model": "Consulta productos usando múltiples endpoints: /producto para búsqueda específica, /productos/todos para ver todo el inventario, /productos/buscar para búsqueda flexible, /productos/info para información de la base de datos",
            "auth": {
                "type": "none"
            },
            "api": {
                "type": "openapi",
                "url": "https://supabase-products-agent.onrender.com/.well-known/openapi.yaml"
            }
        }
        """;
        return ResponseEntity.ok()
                .header("Content-Type", "application/json")
                .body(aiPlugin);
    }

    @GetMapping("/.well-known/openapi.yaml")
    public ResponseEntity<String> getOpenApiSpec() {
        String openApiSpec = """
        openapi: 3.0.1
        info:
          title: Supabase Products API Inteligente
          version: 2.0
        servers:
          - url: https://supabase-products-agent.onrender.com
        paths:
          /producto:
            get:
              summary: Búsqueda específica de producto por nombre
              parameters:
                - name: nombre
                  in: query
                  required: true
                  schema:
                    type: string
              responses:
                '200':
                  description: Producto encontrado
          /productos/todos:
            get:
              summary: Obtener todos los productos del inventario
              responses:
                '200':
                  description: Lista completa de productos
          /productos/buscar:
            get:
              summary: Búsqueda inteligente en múltiples campos
              parameters:
                - name: consulta
                  in: query
                  required: true
                  schema:
                    type: string
              responses:
                '200':
                  description: Productos encontrados
          /productos/info:
            get:
              summary: Información sobre la estructura de la base de datos
              responses:
                '200':
                  description: Metadatos de la base de datos y datos de muestra
        """;
        return ResponseEntity.ok()
                .header("Content-Type", "text/yaml")
                .body(openApiSpec);
    }
}