package com.tucotizador.tucotizador.controller;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

@RestController
@CrossOrigin(origins = "*")
public class MCPDatabaseController {
    
    private final String SUPABASE_URL = "https://itgiewpjoyketotyzzhy.supabase.co/rest/v1/products";
    private final String API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Iml0Z2lld3Bqb3lrZXRvdHl6emh5Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDgzNjM0OTIsImV4cCI6MjA2MzkzOTQ5Mn0.Ip72t0UOiWJGq_RZ780GMzAj1xMN9SBY7aQjnLDaGTQ";
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    // MCP Server Manifest - Claude lee esto automáticamente
    @GetMapping("/.well-known/ai-plugin.json")
    public ResponseEntity<String> getMCPManifest() {
        String manifest = """
        {
            "schema_version": "v1",
            "name_for_human": "Base de Datos de Productos",
            "name_for_model": "products_database",
            "description_for_human": "Acceso completo a base de datos de productos con precios",
            "description_for_model": "Sistema MCP que permite a Claude explorar libremente una base de datos de productos. Claude puede usar las tools disponibles para consultar, filtrar y analizar datos sin restricciones de palabras clave.",
            "auth": {
                "type": "none"
            },
            "api": {
                "type": "openapi",
                "url": "https://supabase-products-agent.onrender.com/.well-known/openapi.yaml"
            },
            "logo_url": "https://supabase.com/favicon.ico"
        }
        """;
        return ResponseEntity.ok()
                .header("Content-Type", "application/json")
                .body(manifest);
    }

    // OpenAPI spec que define las tools disponibles para Claude
    @GetMapping("/.well-known/openapi.yaml")
    public ResponseEntity<String> getOpenAPISpec() {
        String spec = """
        openapi: 3.0.1
        info:
          title: Products Database MCP Server
          description: MCP Server que expone base de datos de productos para exploración libre por Claude
          version: 1.0.0
        servers:
          - url: https://supabase-products-agent.onrender.com
        paths:
          /mcp/tools/list:
            get:
              summary: Lista todas las herramientas disponibles para Claude
              operationId: listTools
              responses:
                '200':
                  description: Lista de herramientas MCP
          /mcp/tools/call:
            post:
              summary: Ejecuta una herramienta específica
              operationId: callTool
              requestBody:
                required: true
                content:
                  application/json:
                    schema:
                      type: object
                      properties:
                        name:
                          type: string
                        arguments:
                          type: object
              responses:
                '200':
                  description: Resultado de la herramienta
          /mcp/resources/list:
            get:
              summary: Lista todos los recursos disponibles (tablas, vistas, etc.)
              operationId: listResources
              responses:
                '200':
                  description: Lista de recursos de la base de datos
          /mcp/resources/read:
            get:
              summary: Lee un recurso específico
              operationId: readResource
              parameters:
                - name: uri
                  in: query
                  required: true
                  schema:
                    type: string
              responses:
                '200':
                  description: Contenido del recurso
        """;
        return ResponseEntity.ok()
                .header("Content-Type", "text/yaml")
                .body(spec);
    }

    // MCP Tools List - Define qué herramientas tiene Claude disponibles
    @GetMapping("/mcp/tools/list")
    public ResponseEntity<String> listMCPTools() {
        String tools = """
        {
            "tools": [
                {
                    "name": "query_products",
                    "description": "Consulta productos en la base de datos con filtros flexibles",
                    "inputSchema": {
                        "type": "object",
                        "properties": {
                            "filter": {
                                "type": "string",
                                "description": "Filtro SQL-like para la consulta (ej: product_name.ilike.*bimbo*)"
                            },
                            "select": {
                                "type": "string", 
                                "description": "Campos a seleccionar (default: *)",
                                "default": "*"
                            },
                            "limit": {
                                "type": "integer",
                                "description": "Límite de registros (default: 100)",
                                "default": 100
                            },
                            "order": {
                                "type": "string",
                                "description": "Ordenamiento (ej: sale_price.desc)"
                            }
                        }
                    }
                },
                {
                    "name": "search_products",
                    "description": "Búsqueda libre de texto en productos",
                    "inputSchema": {
                        "type": "object",
                        "properties": {
                            "query": {
                                "type": "string",
                                "description": "Texto a buscar en nombre de productos"
                            }
                        },
                        "required": ["query"]
                    }
                },
                {
                    "name": "get_product_stats",
                    "description": "Obtiene estadísticas de la base de datos de productos",
                    "inputSchema": {
                        "type": "object",
                        "properties": {}
                    }
                },
                {
                    "name": "analyze_prices", 
                    "description": "Analiza rangos de precios y estadísticas de pricing",
                    "inputSchema": {
                        "type": "object",
                        "properties": {
                            "category_filter": {
                                "type": "string",
                                "description": "Filtro opcional por categoría de producto"
                            }
                        }
                    }
                }
            ]
        }
        """;
        return ResponseEntity.ok(tools);
    }

    // MCP Tool Execution - Claude ejecuta herramientas aquí
    @PostMapping("/mcp/tools/call")
    public ResponseEntity<String> callMCPTool(@RequestBody Map<String, Object> request) {
        try {
            String toolName = (String) request.get("name");
            Map<String, Object> arguments = (Map<String, Object>) request.get("arguments");
            
            switch (toolName) {
                case "query_products":
                    return executeQueryProducts(arguments);
                case "search_products":
                    return executeSearchProducts(arguments);
                case "get_product_stats":
                    return executeGetProductStats(arguments);
                case "analyze_prices":
                    return executeAnalyzePrices(arguments);
                default:
                    return ResponseEntity.badRequest()
                        .body("{\"error\": \"Herramienta no encontrada: " + toolName + "\"}");
            }
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    // MCP Resources List - Lista los "archivos" que Claude puede leer
    @GetMapping("/mcp/resources/list") 
    public ResponseEntity<String> listMCPResources() {
        String resources = """
        {
            "resources": [
                {
                    "uri": "database://products/schema",
                    "name": "Esquema de Base de Datos",
                    "description": "Estructura completa de la base de datos de productos",
                    "mimeType": "application/json"
                },
                {
                    "uri": "database://products/sample",
                    "name": "Datos de Muestra", 
                    "description": "Muestra de productos para entender la estructura",
                    "mimeType": "application/json"
                },
                {
                    "uri": "database://products/all",
                    "name": "Todos los Productos",
                    "description": "Dataset completo de productos (puede ser grande)",
                    "mimeType": "application/json"
                }
            ]
        }
        """;
        return ResponseEntity.ok(resources);
    }

    // MCP Resource Reader - Claude "lee" recursos como archivos
    @GetMapping("/mcp/resources/read")
    public ResponseEntity<String> readMCPResource(@RequestParam String uri) {
        try {
            switch (uri) {
                case "database://products/schema":
                    return getSchemaResource();
                case "database://products/sample":
                    return getSampleResource();
                case "database://products/all":
                    return getAllProductsResource();
                default:
                    return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    // Implementaciones de herramientas MCP
    private ResponseEntity<String> executeQueryProducts(Map<String, Object> args) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        StringBuilder url = new StringBuilder(SUPABASE_URL + "?");
        
        String select = (String) args.getOrDefault("select", "*");
        url.append("select=").append(URLEncoder.encode(select, StandardCharsets.UTF_8));
        
        if (args.containsKey("filter")) {
            url.append("&").append(args.get("filter"));
        }
        
        if (args.containsKey("order")) {
            url.append("&order=").append(URLEncoder.encode((String) args.get("order"), StandardCharsets.UTF_8));
        }
        
        Integer limit = (Integer) args.getOrDefault("limit", 100);
        url.append("&limit=").append(limit);

        HttpRequest request = createSupabaseRequest(url.toString());
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        return ResponseEntity.ok("{\"content\": [" + response.body() + "]}");
    }

    private ResponseEntity<String> executeSearchProducts(Map<String, Object> args) throws Exception {
        String query = (String) args.get("query");
        HttpClient client = HttpClient.newHttpClient();
        
        String url = SUPABASE_URL + "?select=*&product_name=ilike.*" + 
            URLEncoder.encode(query, StandardCharsets.UTF_8) + "*&limit=50";

        HttpRequest request = createSupabaseRequest(url);
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        return ResponseEntity.ok("{\"content\": [" + response.body() + "]}");
    }

    private ResponseEntity<String> executeGetProductStats(Map<String, Object> args) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        String url = SUPABASE_URL + "?select=count";

        HttpRequest request = createSupabaseRequest(url);
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        // También obtener rangos de precios
        String pricesUrl = SUPABASE_URL + "?select=sale_price&order=sale_price.desc";
        HttpRequest pricesRequest = createSupabaseRequest(pricesUrl);
        HttpResponse<String> pricesResponse = client.send(pricesRequest, HttpResponse.BodyHandlers.ofString());
        
        String stats = String.format("""
        {
            "content": [{
                "total_count": %s,
                "price_data": %s,
                "database_info": {
                    "table": "products",
                    "main_fields": ["id", "barcode", "product_name", "sale_price", "purchase_price"],
                    "last_updated": "2025-07-25"
                }
            }]
        }
        """, response.body(), pricesResponse.body());
        
        return ResponseEntity.ok(stats);
    }

    private ResponseEntity<String> executeAnalyzePrices(Map<String, Object> args) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        String url = SUPABASE_URL + "?select=product_name,sale_price&order=sale_price.desc&limit=1000";

        HttpRequest request = createSupabaseRequest(url);
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        return ResponseEntity.ok("{\"content\": [" + response.body() + "]}");
    }

    // Recursos MCP
    private ResponseEntity<String> getSchemaResource() throws Exception {
        String schema = """
        {
            "content": [{
                "database": "supabase_products",
                "table": "products", 
                "columns": {
                    "id": {"type": "integer", "primary_key": true},
                    "barcode": {"type": "text", "description": "Código de barras del producto"},
                    "product_name": {"type": "text", "description": "Nombre completo del producto"},
                    "sale_price": {"type": "numeric", "description": "Precio de venta al público"},
                    "purchase_price": {"type": "numeric", "description": "Precio de compra/costo"}
                },
                "indexes": ["product_name", "barcode"],
                "total_records": "Variable",
                "last_updated": "2025-07-25"
            }]
        }
        """;
        return ResponseEntity.ok(schema);
    }

    private ResponseEntity<String> getSampleResource() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        String url = SUPABASE_URL + "?select=*&limit=10";

        HttpRequest request = createSupabaseRequest(url);
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        return ResponseEntity.ok("{\"content\": " + response.body() + "}");
    }

    private ResponseEntity<String> getAllProductsResource() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        String url = SUPABASE_URL + "?select=*&limit=1000";

        HttpRequest request = createSupabaseRequest(url);
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        return ResponseEntity.ok("{\"content\": " + response.body() + "}");
    }

    // Utility method
    private HttpRequest createSupabaseRequest(String url) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("apikey", API_KEY)
                .header("Authorization", "Bearer " + API_KEY)
                .header("Accept", "application/json")
                .GET()
                .build();
    }

    // Endpoint de bienvenida para usuarios
    @GetMapping("/")
    public ResponseEntity<String> getWelcomePage() {
        String welcome = """
        <!DOCTYPE html>
        <html>
        <head>
            <title>MCP Database Server</title>
        </head>
        <body>
            <h1>🤖 MCP Database Server - Activo</h1>
            <p><strong>Para Claude:</strong> Esta URL expone una base de datos de productos vía MCP Protocol.</p>
            <p><strong>Capacidades disponibles:</strong></p>
            <ul>
                <li>Consulta libre de productos</li>
                <li>Búsqueda por texto</li>
                <li>Análisis de precios</li>
                <li>Estadísticas de inventario</li>
            </ul>
            <p><strong>Uso:</strong> Simplemente comparte esta URL con Claude y pídele que explore la base de datos.</p>
            <hr>
            <p><em>MCP Endpoints:</em></p>
            <ul>
                <li><a href="/.well-known/ai-plugin.json">Manifest</a></li>
                <li><a href="/mcp/tools/list">Tools</a></li>
                <li><a href="/mcp/resources/list">Resources</a></li>
            </ul>
        </body>
        </html>
        """;
        return ResponseEntity.ok()
                .header("Content-Type", "text/html")
                .body(welcome);
    }
/////////////////////////////////new endpoints
/// 
/// 
/// 
// SOLUCIÓN: MCP Web-Compatible que funciona con Claude Web
// Agrega estos endpoints a tu ProductoController existente

@GetMapping("/claude/explore")
public ResponseEntity<String> exploreForClaude(@RequestParam(required = false) String query) {
    try {
        if (query == null || query.trim().isEmpty()) {
            // Si no hay query, devolver información de la base de datos
            return getCompleteOverview();
        }
        
        // Buscar automáticamente basado en la query
        return performSmartSearch(query);
        
    } catch (Exception e) {
        return ResponseEntity.status(500)
            .body("{\"error\": \"" + e.getMessage() + "\", \"suggestion\": \"Intenta con una búsqueda más específica\"}");
    }
}

@GetMapping("/claude/search/{productName}")
public ResponseEntity<String> searchProductByName(@PathVariable String productName) {
    try {
        HttpClient client = HttpClient.newHttpClient();
        
        // Búsqueda flexible por nombre
        String url = SUPABASE_URL + "?select=*&product_name=ilike.*" + 
            URLEncoder.encode(productName, StandardCharsets.UTF_8) + "*&limit=20";
        
        HttpRequest request = createSupabaseRequest(url);
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        String result = String.format("""
        {
            "search_term": "%s",
            "products_found": %s,
            "claude_instructions": {
                "task": "Analiza los productos encontrados y extrae la información solicitada",
                "focus": "precio, disponibilidad, y detalles relevantes",
                "format": "Respuesta directa y clara al usuario"
            }
        }
        """, productName, response.body());
        
        return ResponseEntity.ok(result);
        
    } catch (Exception e) {
        return ResponseEntity.status(500).body("{\"error\": \"" + e.getMessage() + "\"}");
    }
}

@GetMapping("/claude/overview")
public ResponseEntity<String> getCompleteOverview() {
    try {
        HttpClient client = HttpClient.newHttpClient();
        
        // Obtener muestra de productos
        String sampleUrl = SUPABASE_URL + "?select=*&limit=10";
        HttpRequest sampleRequest = createSupabaseRequest(sampleUrl);
        HttpResponse<String> sampleResponse = client.send(sampleRequest, HttpResponse.BodyHandlers.ofString());
        
        // Obtener conteo total
        String countUrl = SUPABASE_URL + "?select=count";
        HttpRequest countRequest = createSupabaseRequest(countUrl);
        HttpResponse<String> countResponse = client.send(countRequest, HttpResponse.BodyHandlers.ofString());
        
        String overview = String.format("""
        {
            "database_type": "Products Database",
            "status": "Active and ready for queries",
            "total_products": %s,
            "sample_products": %s,
            "available_fields": [
                "id", "barcode", "product_name", "sale_price", "purchase_price"
            ],
            "search_capabilities": [
                "Product name search",
                "Price analysis", 
                "Inventory overview",
                "Custom queries"
            ],
            "claude_usage": {
                "direct_search": "/claude/search/{productname}",
                "exploration": "/claude/explore?query=your+question",
                "examples": [
                    "/claude/search/bimbo",
                    "/claude/search/cloralex", 
                    "/claude/explore?query=productos+de+limpieza"
                ]
            }
        }
        """, countResponse.body(), sampleResponse.body());
        
        return ResponseEntity.ok(overview);
        
    } catch (Exception e) {
        return ResponseEntity.status(500).body("{\"error\": \"" + e.getMessage() + "\"}");
    }
}

private ResponseEntity<String> performSmartSearch(String query) throws Exception {
    HttpClient client = HttpClient.newHttpClient();
    
    // Análisis inteligente de la query
    String normalizedQuery = query.toLowerCase();
    String searchTerms = extractSearchTerms(normalizedQuery);
    
    if (searchTerms.isEmpty()) {
        return getCompleteOverview();
    }
    
    // Buscar productos
    String url = SUPABASE_URL + "?select=*&product_name=ilike.*" + 
        URLEncoder.encode(searchTerms, StandardCharsets.UTF_8) + "*&limit=50";
    
    HttpRequest request = createSupabaseRequest(url);
    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    
    String result = String.format("""
    {
        "original_query": "%s",
        "extracted_search_terms": "%s",
        "query_type": "%s",
        "products_found": %s,
        "claude_analysis": {
            "instruction": "Analiza estos productos y responde la pregunta original del usuario",
            "focus_on": "%s",
            "response_style": "directo y útil"
        }
    }
    """, query, searchTerms, detectQueryType(normalizedQuery), response.body(), getExpectedFocus(normalizedQuery));
    
    return ResponseEntity.ok(result);
}

private String extractSearchTerms(String query) {
    // Extraer términos de búsqueda relevantes
    String[] commonWords = {"precio", "costo", "cuanto", "cuesta", "dime", "dame", "busca", "encuentra", "de", "del", "la", "el"};
    String[] words = query.split("\\s+");
    
    StringBuilder searchTerms = new StringBuilder();
    for (String word : words) {
        if (word.length() > 2 && !Arrays.asList(commonWords).contains(word)) {
            if (searchTerms.length() > 0) searchTerms.append(" ");
            searchTerms.append(word);
        }
    }
    
    return searchTerms.toString();
}

private String detectQueryType(String query) {
    if (query.contains("precio") || query.contains("cuesta") || query.contains("costo")) {
        return "price_inquiry";
    } else if (query.contains("donde") || query.contains("tienda")) {
        return "availability_inquiry";
    } else if (query.contains("compare") || query.contains("vs")) {
        return "comparison_inquiry";
    }
    return "general_search";
}

private String getExpectedFocus(String query) {
    if (query.contains("precio")) return "sale_price y product_name";
    if (query.contains("tienda")) return "availability y location info";
    return "product_name y sale_price";
}

// ENDPOINT PRINCIPAL PARA CLAUDE WEB
@GetMapping("/")
public ResponseEntity<String> getClaudeInterface() {
    String interfaceHtml = """
    <!DOCTYPE html>
    <html>
    <head>
        <title>🤖 Claude Database Interface</title>
        <style>
            body { font-family: Arial, sans-serif; max-width: 800px; margin: 0 auto; padding: 20px; }
            .endpoint { background: #f5f5f5; padding: 10px; margin: 10px 0; border-radius: 5px; }
            .example { background: #e8f4f8; padding: 8px; margin: 5px 0; border-radius: 3px; }
        </style>
    </head>
    <body>
        <h1>🤖 Base de Datos Lista para Claude</h1>
        
        <div class="endpoint">
            <h3>🔍 Para explorar automáticamente:</h3>
            <p><strong>URL base:</strong> <code>https://supabase-products-agent.onrender.com/</code></p>
            <p><strong>Instrucción para usuarios:</strong></p>
            <div class="example">
                "Claude, conectate a https://supabase-products-agent.onrender.com y dime el precio del cloralex"
            </div>
        </div>

        <div class="endpoint">
            <h3>📋 Endpoints disponibles para Claude:</h3>
            <ul>
                <li><code>/claude/overview</code> - Información general de la base de datos</li>
                <li><code>/claude/search/{producto}</code> - Búsqueda directa de productos</li>
                <li><code>/claude/explore?query=consulta</code> - Exploración inteligente</li>
            </ul>
        </div>

        <div class="endpoint">
            <h3>✅ Estado del sistema:</h3>
            <p>🟢 <strong>Activo</strong> - Listo para consultas de Claude</p>
            <p>🔧 <strong>Compatibilidad:</strong> Claude Web + Desktop</p>
            <p>📊 <strong>Base de datos:</strong> Productos con precios actualizados</p>
        </div>

        <div class="endpoint">
            <h3>🧪 Pruebas rápidas:</h3>
            <div class="example"><a href="/claude/overview">Ver información general</a></div>
            <div class="example"><a href="/claude/search/bimbo">Buscar Bimbo</a></div>
            <div class="example"><a href="/claude/explore?query=precio del cloralex">Explorar: precio del cloralex</a></div>
        </div>
    </body>
    </html>
    """;
    
    return ResponseEntity.ok()
        .header("Content-Type", "text/html; charset=UTF-8")
        .body(interfaceHtml);
}
}