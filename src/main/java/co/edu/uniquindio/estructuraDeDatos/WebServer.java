package co.edu.uniquindio.estructuraDeDatos;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Servidor web para conectar las interfaces HTML con la lógica del proyecto.
 * Compatible con tus clases reales: Ubicacion, Ruta, Recurso, EquipoRescate, etc.
 */
public class WebServer {

    private final SistemaGestionDesastres sistema;

    // Para la interfaz
    private final Map<String, Ubicacion> ubicacionesPorNombre = new LinkedHashMap<>();
    private final List<Ruta> rutasRegistradas = new ArrayList<>();

    public WebServer(SistemaGestionDesastres sistema) {
        this.sistema = sistema;
    }

    // ------------------ INICIO DEL SERVIDOR ------------------
    public void start(int port) throws Exception {
        cargarDemo();

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        // Archivos HTML
        server.createContext("/", ex -> enviarArchivo(ex, "public/index.html", "text/html; charset=utf-8"));
        server.createContext("/public/", ex -> {
            String path = ex.getRequestURI().getPath().substring(1);
            enviarArchivo(ex, path, mime(path));
        });

        // ---- API UBICACIONES ----
        server.createContext("/api/ubicaciones", ex -> {
            Headers h = ex.getResponseHeaders();
            h.add("Content-Type", "application/json; charset=utf-8");

            if ("GET".equals(ex.getRequestMethod())) {
                enviarTexto(ex, 200, listarUbicacionesJson());
            } else if ("POST".equals(ex.getRequestMethod())) {
                Map<String, String> m = tinyJson(cuerpo(ex));
                String nombre = trimOrNull(m.get("nombre"));
                String tipo = m.getOrDefault("tipo", "CIUDAD").trim();
                String nivel = m.getOrDefault("nivel", "LEVE").trim();

                if (nombre == null || nombre.isEmpty()) {
                    enviarTexto(ex, 400, "{\"error\":\"El nombre es obligatorio\"}");
                    return;
                }

                // Creamos una evacuación vacía para cumplir con tu constructor
                Evacuacion evac = new Evacuacion("E" + System.nanoTime(), 0, 0, EstadoEvacuacion.PENDIENTE, null);
                Ubicacion u = new Ubicacion(
                        "U" + System.nanoTime(),
                        nombre,
                        TipoZona.valueOf(tipo),
                        NivelDeAfectacion.valueOf(nivel),
                        evac,
                        new ArrayList<>(),
                        new ArrayList<>(),
                        new ArrayList<>()
                );

                registrarUbicacion(u);
                sistema.agregarUbicacion(u);
                enviarTexto(ex, 200, "{\"ok\":true}");
            } else {
                enviarTexto(ex, 405, "{}");
            }
        });

        // ---- API RUTAS ----
        server.createContext("/api/rutas", ex -> {
            Headers h = ex.getResponseHeaders();
            h.add("Content-Type", "application/json; charset=utf-8");
            String method = ex.getRequestMethod();

            if ("GET".equals(method)) {
                enviarTexto(ex, 200, listarRutasJson());
            } else if ("POST".equals(method)) {
                Map<String, String> m = tinyJson(cuerpo(ex));
                String so = trimOrNull(m.get("origen"));
                String sd = trimOrNull(m.get("destino"));
                double dist = parseDoubleSafe(m.get("distancia"), 1.0);

                if (so == null || sd == null) {
                    enviarTexto(ex, 400, "{\"error\":\"Debes especificar origen y destino\"}");
                    return;
                }
                if (dist <= 0) {
                    enviarTexto(ex, 400, "{\"error\":\"La distancia debe ser mayor que 0\"}");
                    return;
                }

                Ubicacion o = ubicacionesPorNombre.get(so);
                Ubicacion d = ubicacionesPorNombre.get(sd);
                if (o == null || d == null) {
                    enviarTexto(ex, 400, "{\"error\":\"Ubicación no encontrada\"}");
                    return;
                }

                Ruta r = new Ruta("R" + System.nanoTime(), o, d, dist);
                rutasRegistradas.add(r);
                sistema.agregarRuta(r);
                enviarTexto(ex, 200, "{\"ok\":true}");
            } else {
                enviarTexto(ex, 405, "{}");
            }
        });

        // ---- API RUTA MÁS CORTA ----
        server.createContext("/api/rutas/corta", ex -> {
            Map<String, String> q = query(ex.getRequestURI().getQuery());
            Ubicacion o = ubicacionesPorNombre.get(trimOrNull(q.get("origen")));
            Ubicacion d = ubicacionesPorNombre.get(trimOrNull(q.get("destino")));

            if (o == null || d == null) {
                enviarTexto(ex, 400, "Ubicaciones no válidas");
                return;
            }

            var camino = sistema.getGrafo().buscarCaminoDijkstra(o, d);
            if (camino == null) {
                enviarTexto(ex, 200, "No existe una ruta entre esas ubicaciones");
                return;
            }

            double total = 0;
            for (int i = 0; i < camino.size() - 1; i++) {
                Ubicacion a = camino.get(i);
                Ubicacion b = camino.get(i + 1);
                for (Ruta r : sistema.getGrafo().obtenerRutasDesde(a)) {
                    if (r.getDestino().equals(b)) total += r.getDistancia();
                }
            }

            StringBuilder sb = new StringBuilder("Ruta más corta:\n");
            for (int i = 0; i < camino.size(); i++) {
                sb.append(camino.get(i).getNombre());
                if (i < camino.size() - 1) sb.append(" -> ");
            }
            sb.append("\nDistancia total: ").append(total).append(" km");

            enviarTexto(ex, 200, sb.toString());
        });

        // ---- API RECURSOS ----
        server.createContext("/api/recursos", ex -> {
            Headers h = ex.getResponseHeaders();
            h.add("Content-Type", "application/json; charset=utf-8");
            String method = ex.getRequestMethod();

            if ("GET".equals(method)) {
                List<Recurso> lista = sistema.getMapaRecursos().obtenerTodosLosRecursos();
                List<String> json = new ArrayList<>();
                for (Recurso r : lista) {
                    String ubic = (r.getUbicacion() != null) ? r.getUbicacion().getNombre() : "Sin ubicación";
                    json.add(String.format("{\"nombre\":\"%s\",\"cantidad\":%d,\"ubicacion\":\"%s\"}",
                            esc(r.getNombre()), r.getCantidad(), esc(ubic)));
                }
                enviarTexto(ex, 200, "[" + String.join(",", json) + "]");
            } else if ("POST".equals(method)) {
                Map<String, String> m = tinyJson(cuerpo(ex));
                String nombre = trimOrNull(m.get("nombre"));
                int cantidad = (int) parseDoubleSafe(m.getOrDefault("cantidad", "0"), 0);
                String tipo = m.getOrDefault("tipo", "ALIMENTO").trim();
                Ubicacion u = ubicacionesPorNombre.get(trimOrNull(m.get("ubicacion")));

                if (nombre == null || cantidad <= 0 || u == null) {
                    enviarTexto(ex, 400, "{\"error\":\"Datos inválidos\"}");
                    return;
                }

                Recurso recurso;
                if (tipo.equalsIgnoreCase("ALIMENTO")) {
                    recurso = new RecursoAlimento("RA" + System.nanoTime(), nombre, cantidad, u, java.time.LocalDate.now().plusDays(10));
                } else {
                    recurso = new RecursoMedicina("RM" + System.nanoTime(), nombre, cantidad, u, "General");
                }

                sistema.getMapaRecursos().agregarRecurso(u, recurso);
                enviarTexto(ex, 200, "{\"ok\":true}");
            } else {
                enviarTexto(ex, 405, "{}");
            }
        });

        // ---- API EQUIPOS DE RESCATE ----
        server.createContext("/api/equipos", ex -> {
            Headers h = ex.getResponseHeaders();
            h.add("Content-Type", "application/json; charset=utf-8");

            if ("POST".equals(ex.getRequestMethod())) {
                Map<String, String> m = tinyJson(cuerpo(ex));
                String nombre = trimOrNull(m.get("nombre"));
                String tipo = m.getOrDefault("tipo", "GENERAL").trim();
                int miembros = (int) parseDoubleSafe(m.getOrDefault("miembros", "0"), 0);
                Ubicacion u = ubicacionesPorNombre.get(trimOrNull(m.get("ubicacion")));

                if (nombre == null || miembros <= 0 || u == null) {
                    enviarTexto(ex, 400, "{\"error\":\"Datos inválidos\"}");
                    return;
                }

                EquipoRescate eq = new EquipoRescate("EQ" + System.nanoTime(), tipo, miembros, u);
                eq.setNombre(nombre);
                u.asignarEquipo(eq);
                enviarTexto(ex, 200, "{\"ok\":true}");
            } else {
                enviarTexto(ex, 405, "{}");
            }
        });

        server.start();
        System.out.println("🌐 Servidor web activo en http://localhost:" + port);
    }

    // -------------------- MÉTODOS AUXILIARES --------------------
    private void cargarDemo() {
        // Pequeña demo inicial
        Evacuacion evac = new Evacuacion("E0", 0, 0, EstadoEvacuacion.PENDIENTE, null);
        Ubicacion a = new Ubicacion("U1", "Ciudad A", TipoZona.CIUDAD, NivelDeAfectacion.MODERADO, evac, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        Ubicacion b = new Ubicacion("U2", "Refugio B", TipoZona.REFUGIO, NivelDeAfectacion.LEVE, evac, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        Ubicacion c = new Ubicacion("U3", "Centro C", TipoZona.CENTRO_AYUDA, NivelDeAfectacion.GRAVE, evac, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());

        registrarUbicacion(a); registrarUbicacion(b); registrarUbicacion(c);
        sistema.agregarUbicacion(a); sistema.agregarUbicacion(b); sistema.agregarUbicacion(c);

        Ruta r1 = new Ruta("R1", a, b, 2.5);
        Ruta r2 = new Ruta("R2", b, c, 3.0);
        Ruta r3 = new Ruta("R3", a, c, 5.0);

        rutasRegistradas.add(r1); rutasRegistradas.add(r2); rutasRegistradas.add(r3);
        sistema.agregarRuta(r1); sistema.agregarRuta(r2); sistema.agregarRuta(r3);
    }

    private void registrarUbicacion(Ubicacion u) {
        if (u != null) ubicacionesPorNombre.put(u.getNombre(), u);
    }

    private String listarUbicacionesJson() {
        List<String> items = new ArrayList<>();
        for (Ubicacion u : ubicacionesPorNombre.values()) {
            items.add(String.format("{\"nombre\":\"%s\",\"tipo\":\"%s\",\"afectacion\":\"%s\"}",
                    esc(u.getNombre()), u.getTipoZona(), u.getNivelAfectacion()));
        }
        return "[" + String.join(",", items) + "]";
    }

    private String listarRutasJson() {
        List<String> items = new ArrayList<>();

        // Obtenemos las rutas directamente del grafo real
        List<Ruta> rutasGrafo = sistema.getGrafo().obtenerTodasLasRutas();

        if (rutasGrafo.isEmpty()) {
            return "[]";
        }

        for (Ruta r : rutasGrafo) {
            items.add(String.format("{\"origen\":\"%s\",\"destino\":\"%s\",\"distancia\":%.2f}",
                    esc(r.getOrigen().getNombre()),
                    esc(r.getDestino().getNombre()),
                    r.getDistancia()));
        }

        return "[" + String.join(",", items) + "]";
    }


    // --- Métodos utilitarios ---
    private static String cuerpo(com.sun.net.httpserver.HttpExchange ex) throws IOException {
        return new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    private static void enviarTexto(com.sun.net.httpserver.HttpExchange ex, int status, String text) throws IOException {
        byte[] body = text.getBytes(StandardCharsets.UTF_8);
        ex.sendResponseHeaders(status, body.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(body); }
    }

    private static void enviarArchivo(com.sun.net.httpserver.HttpExchange ex, String resourcePath, String contentType) throws IOException {
        byte[] body = leerRecurso(resourcePath);
        ex.getResponseHeaders().add("Content-Type", contentType);
        ex.sendResponseHeaders(200, body.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(body); }
    }

    private static byte[] leerRecurso(String ruta) throws IOException {
        try (InputStream is = WebServer.class.getClassLoader().getResourceAsStream(ruta)) {
            if (is != null) return is.readAllBytes();
        }
        String dev = "src/main/resources/" + ruta;
        if (Files.exists(Paths.get(dev))) return Files.readAllBytes(Paths.get(dev));
        return ("No encontrado: " + ruta).getBytes(StandardCharsets.UTF_8);
    }

    private static String mime(String path) {
        if (path.endsWith(".css")) return "text/css";
        if (path.endsWith(".html")) return "text/html";
        return "text/plain";
    }

    private static Map<String, String> tinyJson(String json) {
        Map<String, String> map = new HashMap<>();
        if (json == null) return map;
        json = json.trim().replaceAll("[{}\"]", "");
        for (String part : json.split(",")) {
            String[] kv = part.split(":", 2);
            if (kv.length == 2) map.put(kv[0], kv[1]);
        }
        return map;
    }

    private static Map<String, String> query(String q) {
        Map<String, String> map = new HashMap<>();
        if (q == null) return map;
        for (String p : q.split("&")) {
            String[] kv = p.split("=", 2);
            if (kv.length == 2) map.put(kv[0], kv[1]);
        }
        return map;
    }

    private static double parseDoubleSafe(String s, double def) {
        try { return (s == null || s.isBlank()) ? def : Double.parseDouble(s); }
        catch (Exception e) { return def; }
    }

    private static String trimOrNull(String s) { return (s == null) ? null : s.trim(); }

    private static String esc(String s) { return (s == null) ? "" : s.replace("\"", "\\\""); }

    // Fábrica de servidor
    public static WebServer crearPorDefecto() {
        GrafoTransporte grafo = new GrafoTransporte();
        ColaPrioridadEvacuacion cola = new ColaPrioridadEvacuacion();
        MapaRecursos mapa = new MapaRecursos();
        ArbolDistribuido arbol = new ArbolDistribuido();
        SistemaGestionDesastres sistema = new SistemaGestionDesastres(grafo, cola, mapa, arbol, new ArrayList<>());
        return new WebServer(sistema);
    }
}
