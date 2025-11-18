package co.edu.uniquindio.estructuraDeDatos;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;

/**
 * Servidor HTTP ligero para exponer interfaces HTML y APIs.
 * Login con dos cuentas demo (admin/oper).
 */
public class WebServer {

    private final SistemaGestionDesastres sistema;
    private final Map<String, Ubicacion> ubicacionesPorNombre = new LinkedHashMap<>();

    // 🔹 Equipos que todavía no tienen ubicación asignada (se guardan en una "bodega" oculta)
    private final List<EquipoRescate> equiposSinUbicacion = new ArrayList<>();
    private Ubicacion ubicacionBodegaEquipos;
    private static final String NOMBRE_BODEGA_EQUIPOS = "__SIN_UBICACION__";

    // ======== Cuentas demo =========
    private static final String ADMIN_USER = "admin";
    private static final String ADMIN_PASS = "admin123";
    private static final String ADMIN_ROLE = "ADMINISTRADOR";
    private static final String ADMIN_NAME = "Ana Admin";

    private static final String OPER_USER = "oper";
    private static final String OPER_PASS = "oper123";
    private static final String OPER_ROLE = "OPERADOR";
    private static final String OPER_NAME = "Oscar Operador";
    // ===============================

    public WebServer(SistemaGestionDesastres sistema) {
        this.sistema = sistema;
    }

    public void start(int port) throws Exception {

        // Si el grafo viene vacío (no había JSON o estaba vacío) cargamos la demo.
        // Si ya hay ubicaciones (cargadas desde PersistenciaJson.cargar),
        // solo sincronizamos el mapa interno ubicacionesPorNombre y la bodega de equipos.
        if (sistema.getGrafo().obtenerTodasLasUbicaciones().isEmpty()) {
            cargarDemo(); // ubicaciones + rutas básicas
        } else {
            sincronizarUbicacionesDesdeSistema();
        }

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        // ---------- estáticos ----------
        server.createContext("/", ex -> enviarArchivo(ex, "public/index.html", "text/html; charset=utf-8"));
        server.createContext("/public/", ex -> {
            String path = ex.getRequestURI().getPath();
            if (path.startsWith("/")) path = path.substring(1);
            enviarArchivo(ex, path, mime(path));
        });

        // ---------- UBICACIONES ----------
        server.createContext("/api/ubicaciones", ex -> {
            Headers h = ex.getResponseHeaders();
            h.add("Content-Type", "application/json; charset=utf-8");
            String method = ex.getRequestMethod();

            // LISTAR
            if ("GET".equals(method)) {
                enviarTexto(ex, 200, listarUbicacionesJson());
                return;
            }

            // CREAR
            if ("POST".equals(method)) {
                Map<String,String> m = tinyJson(cuerpo(ex));
                String nombre = trimOrNull(m.get("nombre"));
                String tipo   = m.getOrDefault("tipoZona","CIUDAD").trim();
                String nivel  = m.getOrDefault("nivelAfectacion","LEVE").trim();
                double lat = parseDoubleSafe(m.get("latitud"), 0);
                double lng = parseDoubleSafe(m.get("longitud"), 0);

                if (nombre==null || nombre.isEmpty()) {
                    enviarTexto(ex,400,"{\"error\":\"nombre requerido\"}");
                    return;
                }

                Evacuacion evac = new Evacuacion("E"+System.nanoTime(),0,0,EstadoEvacuacion.PENDIENTE,null);
                Ubicacion u = new Ubicacion(
                        "U"+System.nanoTime(),
                        nombre,
                        TipoZona.valueOf(tipo),
                        NivelDeAfectacion.valueOf(nivel),
                        evac,
                        new ArrayList<>(),
                        new ArrayList<>(),
                        new ArrayList<>(),
                        lat,
                        lng
                );
                registrarUbicacion(u);
                sistema.agregarUbicacion(u);
                guardarSistemaEnJson();
                enviarTexto(ex,200,"{\"ok\":true}");
                return;
            }

            // ELIMINAR
            if ("DELETE".equals(method)) {
                Map<String,String> q = query(ex.getRequestURI().getQuery());
                String nombre = trimOrNull(q.get("nombre"));

                if (nombre == null) {
                    enviarTexto(ex,400,"{\"error\":\"nombre requerido\"}");
                    return;
                }

                // Buscar coincidencia real (ignorando mayúsculas y espacios)
                Ubicacion u = null;
                for (Map.Entry<String, Ubicacion> entry : ubicacionesPorNombre.entrySet()) {
                    if (entry.getKey().trim().equalsIgnoreCase(nombre.trim())) {
                        u = entry.getValue();
                        break;
                    }
                }

                if (u == null) {
                    enviarTexto(ex,400,"{\"error\":\"ubicacion no encontrada\"}");
                    return;
                }

                // Eliminar del mapa interno
                ubicacionesPorNombre.remove(u.getNombre());

                // Eliminar del grafo (incluye rutas asociadas)
                sistema.getGrafo().eliminarUbicacion(u);

                // Guardar cambios en JSON
                guardarSistemaEnJson();

                enviarTexto(ex,200,"{\"ok\":true}");
                return;
            }

            enviarTexto(ex,405,"{}");
        });

        // ---------- RUTAS ----------
        server.createContext("/api/rutas", ex -> {
            Headers h = ex.getResponseHeaders();
            h.add("Content-Type", "application/json; charset=utf-8");
            String method = ex.getRequestMethod();
            if ("GET".equals(method)) {
                enviarTexto(ex,200,listarRutasJson());
                return;
            }
            if ("POST".equals(method)) {
                Map<String,String> m = tinyJson(cuerpo(ex));
                String so = trimOrNull(m.get("origen"));
                String sd = trimOrNull(m.get("destino"));

                if (so==null || sd==null) {
                    enviarTexto(ex,400,"{\"error\":\"origen/destino\"}");
                    return;
                }

                Ubicacion o = ubicacionesPorNombre.get(so);
                Ubicacion d = ubicacionesPorNombre.get(sd);

                if (o==null || d==null){
                    enviarTexto(ex,400,"{\"error\":\"ubicacion invalida\"}");
                    return;
                }

                // Intentar leer distancia del JSON, si viene
                double dist = parseDoubleSafe(m.get("distancia"), -1);

                // Si no viene o es <= 0, calcular con lat/lng (distancia real aprox.)
                if (dist <= 0) {
                    dist = distanciaKm(o, d);
                }

                Ruta ruta = new Ruta("R"+System.nanoTime(), o, d, dist);
                sistema.agregarRuta(ruta);
                guardarSistemaEnJson();
                enviarTexto(ex,200,"{\"ok\":true}");
                return;
            }
            enviarTexto(ex,405,"{}");
        });

        // Calcular ruta más corta
        server.createContext("/api/rutas/corta", ex -> {
            Map<String,String> q = query(ex.getRequestURI().getQuery());
            Ubicacion o = ubicacionesPorNombre.get(trimOrNull(q.get("origen")));
            Ubicacion d = ubicacionesPorNombre.get(trimOrNull(q.get("destino")));
            if (o==null||d==null){
                try {
                    enviarTexto(ex,400,"Ubicaciones no válidas");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;
            }
            var camino = sistema.getGrafo().buscarCaminoDijkstra(o,d);
            if (camino==null || camino.isEmpty()){
                try {
                    enviarTexto(ex,200,"No existe una ruta entre esas ubicaciones");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;
            }
            double total=0;
            for(int i=0;i<camino.size()-1;i++){
                Ubicacion a=camino.get(i), b=camino.get(i+1);
                for(Ruta r: sistema.getGrafo().obtenerRutasDesde(a)) {
                    if (r.getDestino().equals(b)) {
                        total+=r.getDistancia();
                    }
                }
            }
            StringBuilder sb=new StringBuilder("Ruta más corta:\n");
            for(int i=0;i<camino.size();i++){
                sb.append(camino.get(i).getNombre());
                if(i<camino.size()-1) sb.append(" -> ");
            }
            sb.append("\nDistancia total: ").append(total).append(" km");
            try {
                enviarTexto(ex,200,sb.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // ---------- RECURSOS ----------
        server.createContext("/api/recursos", ex -> {
            Headers h = ex.getResponseHeaders(); h.add("Content-Type","application/json; charset=utf-8");
            String method = ex.getRequestMethod();

            // ===== GET =====
            if ("GET".equals(method)) {
                String ubic = query(ex.getRequestURI().getQuery()).get("ubicacion");

                // GET filtrando por ubicación (para ubicaciones.js)
                if (ubic!=null) {
                    Ubicacion u = ubicacionesPorNombre.get(ubic);
                    List<String> arr=new ArrayList<>();
                    if (u != null) {
                        for(Recurso r: sistema.getMapaRecursos().obtenerRecursos(u)){
                            String tipo = (r instanceof RecursoAlimento) ? "ALIMENTO" :
                                    (r instanceof RecursoMedicina) ? "MEDICINA" : "GEN";
                            arr.add(String.format(
                                    "{\"tipo\":\"%s\",\"cantidad\":%d}",
                                    tipo, r.getCantidad()
                            ));
                        }
                    }
                    try {
                        enviarTexto(ex,200,"["+String.join(",",arr)+"]");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return;
                }

                // GET general (para recursos.js y distribucion.js)
                List<String> json=new ArrayList<>();
                for(Recurso r: sistema.getMapaRecursos().obtenerTodosLosRecursos()){

                    String tipo;
                    String vencimiento = null;
                    String medicamento = null;

                    if (r instanceof RecursoAlimento ra) {
                        tipo = "ALIMENTO";
                        if (ra.getFechaVencimiento() != null) {
                            vencimiento = ra.getFechaVencimiento().toString();
                        }
                    } else if (r instanceof RecursoMedicina rm) {
                        tipo = "MEDICINA";
                        medicamento = rm.getTipoMedicamento();
                    } else {
                        tipo = "GEN";
                    }

                    String nombre = esc(r.getNombre() != null ? r.getNombre() : "");
                    String nomUbi = r.getUbicacion()!=null ? esc(r.getUbicacion().getNombre()) : "Sin ubicación";
                    String idRec  = esc(r.getIdRecurso() != null ? r.getIdRecurso() : "");

                    StringBuilder sb = new StringBuilder();
                    sb.append("{")
                            .append("\"id\":\"").append(idRec).append("\",")
                            .append("\"tipo\":\"").append(tipo).append("\",")
                            .append("\"nombre\":\"").append(nombre).append("\",")
                            .append("\"cantidad\":").append(r.getCantidad()).append(",")
                            .append("\"ubicacion\":\"").append(nomUbi).append("\"");

                    if (vencimiento != null) {
                        sb.append(",\"vencimiento\":\"").append(vencimiento).append("\"");
                    }
                    if (medicamento != null) {
                        sb.append(",\"medicamento\":\"").append(esc(medicamento)).append("\"");
                    }

                    sb.append("}");
                    json.add(sb.toString());
                }
                try {
                    enviarTexto(ex,200,"["+String.join(",",json)+"]");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;
            }

            // ===== POST =====
            if ("POST".equals(method)) {
                Map<String,String> m=tinyJson(cuerpo(ex));

                String tipo    = m.getOrDefault("tipo","ALIMENTO").trim();
                String nombre  = trimOrNull(m.get("nombre"));
                String vencStr = trimOrNull(m.get("vencimiento"));
                String medStr  = trimOrNull(m.get("medicamento"));

                int cantidad   = (int)parseDoubleSafe(m.getOrDefault("cantidad","0"),0);
                Ubicacion u    = ubicacionesPorNombre.get(trimOrNull(m.get("ubicacion")));

                if(cantidad<=0||u==null){
                    try {
                        enviarTexto(ex,400,"{\"error\":\"datos\"}");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return;
                }

                Recurso rec;

                if (tipo.equalsIgnoreCase("ALIMENTO")) {
                    LocalDate fecha;
                    if (vencStr != null && !vencStr.isBlank()) {
                        // Se espera formato yyyy-MM-dd
                        fecha = LocalDate.parse(vencStr);
                    } else {
                        // Por defecto, 30 días después de hoy
                        fecha = LocalDate.now().plusDays(30);
                    }
                    String nombreFinal = (nombre != null && !nombre.isBlank())
                            ? nombre
                            : "Alimento";
                    rec = new RecursoAlimento(
                            "RA"+System.nanoTime(),
                            nombreFinal,
                            cantidad,
                            u,
                            fecha
                    );
                } else if (tipo.equalsIgnoreCase("MEDICINA")) {
                    String tipoMedFinal = (medStr != null && !medStr.isBlank())
                            ? medStr
                            : "General";
                    String nombreFinal = (nombre != null && !nombre.isBlank())
                            ? nombre
                            : "Medicamento";
                    rec = new RecursoMedicina(
                            "RM"+System.nanoTime(),
                            nombreFinal,
                            cantidad,
                            u,
                            tipoMedFinal
                    );
                } else {
                    // Tipo genérico por si acaso
                    String nombreFinal = (nombre != null && !nombre.isBlank())
                            ? nombre
                            : "Recurso";
                    rec = new RecursoMedicina(
                            "RG"+System.nanoTime(),
                            nombreFinal,
                            cantidad,
                            u,
                            "GEN"
                    );
                }

                // ✅ Registramos en mapa + árbol de distribución
                sistema.registrarRecurso(u, rec);

                guardarSistemaEnJson();
                try {
                    enviarTexto(ex,200,"{\"ok\":true}");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;
            }

            try {
                enviarTexto(ex,405,"{}");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // 🔹 NUEVO ENDPOINT: DISTRIBUCIÓN PRIORITARIA DE RECURSOS
        server.createContext("/api/recursos/distribuirPrioridad", ex -> {
            Headers h = ex.getResponseHeaders();
            h.add("Content-Type","application/json; charset=utf-8");

            if (!"POST".equals(ex.getRequestMethod())) {
                try {
                    enviarTexto(ex,405,"{}");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;
            }

            try {
                Map<String,String> m = tinyJson(cuerpo(ex));
                String origen        = trimOrNull(m.get("origen"));
                String idRecurso     = trimOrNull(m.get("idRecurso"));
                int cantidadPorZona  = (int)parseDoubleSafe(m.getOrDefault("cantidadPorZona","0"),0);

                // Llamamos a la lógica de SistemaGestionDesastres (usa MapaRecursos + ArbolDistribuido)
                List<String> log = sistema.distribuirRecursoPrioritario(origen, idRecurso, cantidadPorZona);
                guardarSistemaEnJson();

                // Convertir lista de mensajes a JSON simple: ["msg1","msg2",...]
                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < log.size(); i++) {
                    if (i > 0) sb.append(",");
                    sb.append("\"").append(esc(log.get(i))).append("\"");
                }
                sb.append("]");

                enviarTexto(ex,200,sb.toString());
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    String msg = "[\"Error interno en la distribución: " + esc(e.getMessage()) + "\"]";
                    enviarTexto(ex,500,msg);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });

        server.createContext("/api/recursos/transferir", ex -> {
            if (!"POST".equals(ex.getRequestMethod())) {
                try {
                    enviarTexto(ex,405,"{}");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;
            }
            Map<String,String> m=tinyJson(cuerpo(ex));
            Ubicacion o=ubicacionesPorNombre.get(trimOrNull(m.get("origen")));
            Ubicacion d=ubicacionesPorNombre.get(trimOrNull(m.get("destino")));
            String id = trimOrNull(m.get("idRecurso"));
            int cant = (int)parseDoubleSafe(m.getOrDefault("cantidad","0"),0);
            var result = sistema.getMapaRecursos().transferirRecurso(o,d,id,cant);
            guardarSistemaEnJson();
            try {
                enviarTexto(ex,200, result!=null? "{\"ok\":true}" : "{\"ok\":false}");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // ---------- EQUIPOS ----------
        server.createContext("/api/equipos", ex -> {
            Headers h=ex.getResponseHeaders(); h.add("Content-Type","application/json; charset=utf-8");
            String method=ex.getRequestMethod();

            // LISTAR TODOS LOS EQUIPOS (con y sin ubicación)
            if ("GET".equals(method)) {
                List<String> items=new ArrayList<>();

                // Equipos sin ubicación (bodega)
                for (EquipoRescate e : equiposSinUbicacion) {
                    items.add(String.format(
                            "{\"nombre\":\"%s\",\"tipo\":\"%s\",\"miembros\":%d,\"ubicacion\":\"%s\"}",
                            esc(e.getNombre()), esc(e.getTipo()), e.getMiembros(), "Sin ubicación"
                    ));
                }

                // Equipos asociados a ubicaciones reales
                for(Ubicacion u: ubicacionesPorNombre.values()){
                    if (u.getEquiposDeRescate() != null) {
                        for(EquipoRescate e: u.getEquiposDeRescate()){
                            items.add(String.format(
                                    "{\"nombre\":\"%s\",\"tipo\":\"%s\",\"miembros\":%d,\"ubicacion\":\"%s\"}",
                                    esc(e.getNombre()), esc(e.getTipo()), e.getMiembros(), esc(u.getNombre())
                            ));
                        }
                    }
                }
                try {
                    enviarTexto(ex,200,"["+String.join(",",items)+"]");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;
            }

            // CREAR EQUIPO SIN UBICACIÓN (se guarda en bodega oculta)
            if ("POST".equals(method)) {
                Map<String,String> m=tinyJson(cuerpo(ex));
                String nombre=trimOrNull(m.get("nombre"));
                String tipo=m.getOrDefault("tipo","GENERAL").trim();
                int miembros=(int)parseDoubleSafe(m.getOrDefault("miembros","0"),0);

                if(nombre==null||miembros<=0){
                    try {
                        enviarTexto(ex,400,"{\"error\":\"datos\"}");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return;
                }

                Ubicacion bodega = obtenerUbicacionBodegaEquipos();
                EquipoRescate eq=new EquipoRescate("EQ"+System.nanoTime(),tipo,miembros,bodega);
                eq.setNombre(nombre);
                bodega.asignarEquipo(eq);      // queda persistido dentro de la bodega
                equiposSinUbicacion.add(eq);   // lista auxiliar para la API

                guardarSistemaEnJson();
                try {
                    enviarTexto(ex,200,"{\"ok\":true}");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;
            }

            try {
                enviarTexto(ex,405,"{}");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // 🔹 Asignar / reasignar equipo a una ubicación REAL
        server.createContext("/api/equipos/asignar", ex -> {
            Headers h = ex.getResponseHeaders();
            h.add("Content-Type","application/json; charset=utf-8");

            if (!"PUT".equals(ex.getRequestMethod())) {
                try { enviarTexto(ex,405,"{}"); } catch (IOException e) { e.printStackTrace(); }
                return;
            }

            Map<String,String> m = tinyJson(cuerpo(ex));
            String nombreEquipo = trimOrNull(m.get("equipo"));
            String nombreUbic   = trimOrNull(m.get("ubicacion"));

            if (nombreEquipo == null || nombreUbic == null) {
                try { enviarTexto(ex,400,"{\"error\":\"datos\"}"); } catch (IOException e) { e.printStackTrace(); }
                return;
            }

            Ubicacion nuevaUb = ubicacionesPorNombre.get(nombreUbic);
            if (nuevaUb == null) {
                try { enviarTexto(ex,400,"{\"error\":\"ubicacion\"}"); } catch (IOException e) { e.printStackTrace(); }
                return;
            }

            EquipoRescate encontrado = null;

            // Buscar primero en lista sin ubicación (bodega)
            Iterator<EquipoRescate> itSin = equiposSinUbicacion.iterator();
            while (itSin.hasNext()) {
                EquipoRescate e = itSin.next();
                if (nombreEquipo.equalsIgnoreCase(e.getNombre())) {
                    itSin.remove();
                    if (ubicacionBodegaEquipos != null &&
                            ubicacionBodegaEquipos.getEquiposDeRescate() != null) {
                        ubicacionBodegaEquipos.getEquiposDeRescate().remove(e);
                    }
                    encontrado = e;
                    break;
                }
            }

            // Si no estaba ahí, buscar en cada ubicación (reasignación)
            if (encontrado == null) {
                for (Ubicacion u : ubicacionesPorNombre.values()) {
                    if (u.getEquiposDeRescate() == null) continue;
                    Iterator<EquipoRescate> it = u.getEquiposDeRescate().iterator();
                    while (it.hasNext()) {
                        EquipoRescate e = it.next();
                        if (nombreEquipo.equalsIgnoreCase(e.getNombre())) {
                            it.remove();
                            encontrado = e;
                            break;
                        }
                    }
                    if (encontrado != null) break;
                }
            }

            if (encontrado == null) {
                try { enviarTexto(ex,400,"{\"error\":\"equipo no encontrado\"}"); } catch (IOException e) { e.printStackTrace(); }
                return;
            }

            encontrado.setUbicacion(nuevaUb);
            nuevaUb.asignarEquipo(encontrado);

            guardarSistemaEnJson();
            try { enviarTexto(ex,200,"{\"ok\":true}"); } catch (IOException e) { e.printStackTrace(); }
        });

        // 🔹 Eliminar equipo por nombre (tanto en bodega como en ubicaciones reales)
        server.createContext("/api/equipos/eliminar", ex -> {
            Headers h = ex.getResponseHeaders();
            h.add("Content-Type","application/json; charset=utf-8");

            if (!"DELETE".equals(ex.getRequestMethod())) {
                try { enviarTexto(ex,405,"{}"); } catch (IOException e) { e.printStackTrace(); }
                return;
            }

            Map<String,String> q = query(ex.getRequestURI().getQuery());
            String nombre = trimOrNull(q.get("nombre"));
            if (nombre == null) {
                try { enviarTexto(ex,400,"{\"error\":\"nombre requerido\"}"); } catch (IOException e) { e.printStackTrace(); }
                return;
            }

            boolean borrado = false;

            // 1) Lista sin ubicación + bodega
            Iterator<EquipoRescate> itSin = equiposSinUbicacion.iterator();
            while (itSin.hasNext()) {
                EquipoRescate e = itSin.next();
                if (nombre.equalsIgnoreCase(e.getNombre())) {
                    itSin.remove();
                    if (ubicacionBodegaEquipos != null &&
                            ubicacionBodegaEquipos.getEquiposDeRescate() != null) {
                        ubicacionBodegaEquipos.getEquiposDeRescate().remove(e);
                    }
                    borrado = true;
                    break;
                }
            }

            // 2) Equipos dentro de ubicaciones reales
            if (!borrado) {
                for (Ubicacion u : ubicacionesPorNombre.values()) {
                    if (u.getEquiposDeRescate() == null) continue;
                    Iterator<EquipoRescate> it = u.getEquiposDeRescate().iterator();
                    while (it.hasNext()) {
                        EquipoRescate e = it.next();
                        if (nombre.equalsIgnoreCase(e.getNombre())) {
                            it.remove();
                            borrado = true;
                            break;
                        }
                    }
                    if (borrado) break;
                }
            }

            if (!borrado) {
                try { enviarTexto(ex,400,"{\"error\":\"equipo no encontrado\"}"); } catch (IOException e) { e.printStackTrace(); }
                return;
            }

            guardarSistemaEnJson();
            try { enviarTexto(ex,200,"{\"ok\":true}"); } catch (IOException e) { e.printStackTrace(); }
        });

        // ---------- PERSONAS (extra, por si lo usas) ----------
        server.createContext("/api/personas", ex -> {
            Headers h = ex.getResponseHeaders();
            h.add("Content-Type", "application/json; charset=utf-8");

            String method = ex.getRequestMethod();
            if (!"GET".equals(method)) {
                try {
                    enviarTexto(ex, 405, "{}");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;
            }

            Map<String,String> q = query(ex.getRequestURI().getQuery());
            String nombreUbicacion = trimOrNull(q.get("ubicacion"));

            List<String> personasJson = new ArrayList<>();

            if (nombreUbicacion != null) {
                Ubicacion u = ubicacionesPorNombre.get(nombreUbicacion);
                if (u != null && u.getPersonas() != null) {
                    for (Persona p : u.getPersonas()) {
                        personasJson.add(
                                String.format("{\"nombre\":\"%s\"}", esc(p.getNombre()))
                        );
                    }
                }
            }

            try {
                enviarTexto(ex, 200, "[" + String.join(",", personasJson) + "]");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // ---------- LOGIN ----------
        server.createContext("/api/login", ex -> {
            Headers h=ex.getResponseHeaders(); h.add("Content-Type","application/json; charset=utf-8");
            if (!"POST".equals(ex.getRequestMethod())) {
                try {
                    enviarTexto(ex,405,"{}");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;
            }
            Map<String,String> m=tinyJson(cuerpo(ex));
            String usr = trimOrNull(m.get("usuario"));
            String pwd = trimOrNull(m.get("contrasena"));

            if (ADMIN_USER.equals(usr) && ADMIN_PASS.equals(pwd)) {
                try {
                    enviarTexto(ex,200,String.format("{\"ok\":true,\"rol\":\"%s\",\"nombre\":\"%s\"}", ADMIN_ROLE, ADMIN_NAME));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;
            }
            if (OPER_USER.equals(usr) && OPER_PASS.equals(pwd)) {
                try {
                    enviarTexto(ex,200,String.format("{\"ok\":true,\"rol\":\"%s\",\"nombre\":\"%s\"}", OPER_ROLE, OPER_NAME));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;
            }
            try {
                enviarTexto(ex,401,"{\"ok\":false}");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // ---------- RESUMEN ----------
        server.createContext("/api/resumen", ex -> {
            Headers h=ex.getResponseHeaders(); h.add("Content-Type","application/json; charset=utf-8");
            String json = String.format(
                    "{\"ubicaciones\":{\"leve\":%d,\"moderado\":%d,\"grave\":%d},\"recursos\":%d,\"equipos\":%d,\"pendientes\":%d}",
                    sistema.contarUbicacionesPorNivel(NivelDeAfectacion.LEVE),
                    sistema.contarUbicacionesPorNivel(NivelDeAfectacion.MODERADO),
                    sistema.contarUbicacionesPorNivel(NivelDeAfectacion.GRAVE),
                    sistema.contarRecursos(), sistema.contarEquipos(), sistema.contarEvacuacionesPendientes());
            try {
                enviarTexto(ex,200,json);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // ---------- EVACUACIONES ----------
        server.createContext("/api/evacuaciones", ex -> {
            Headers h=ex.getResponseHeaders(); h.add("Content-Type","application/json; charset=utf-8");
            String method = ex.getRequestMethod();
            if ("GET".equals(method)) {
                List<String> arr=new ArrayList<>();
                for(Evacuacion e: sistema.getColaEvacuaciones().listarTodas()){
                    String u = e.getUbicacion()!=null? e.getUbicacion().getNombre() : "Sin ubicación";
                    arr.add(String.format("{\"id\":\"%s\",\"prioridad\":%d,\"personas\":%d,\"estado\":\"%s\",\"ubicacion\":\"%s\"}",
                            e.getIdEvacuacion(), e.getPrioridad(), e.getCantidadPersonas(), e.getEstado(), esc(u)));
                }
                try {
                    enviarTexto(ex,200,"["+String.join(",",arr)+"]");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;
            }
            if ("POST".equals(method)) {
                Map<String,String> m=tinyJson(cuerpo(ex));
                Ubicacion u = ubicacionesPorNombre.get(trimOrNull(m.get("ubicacion")));
                int prio=(int)parseDoubleSafe(m.getOrDefault("prioridad","0"),0);
                int pers=(int)parseDoubleSafe(m.getOrDefault("personas","0"),0);
                if (u==null||prio<0||pers<=0){
                    try {
                        enviarTexto(ex,400,"{\"error\":\"datos\"}");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return;
                }
                sistema.getColaEvacuaciones().insertar(new Evacuacion("EV"+System.nanoTime(), prio, pers, EstadoEvacuacion.PENDIENTE, u));
                guardarSistemaEnJson();
                try {
                    enviarTexto(ex,200,"{\"ok\":true}");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;
            }
            if ("PUT".equals(method)) {
                Map<String,String> m=tinyJson(cuerpo(ex));
                sistema.getColaEvacuaciones().actualizarEstado(trimOrNull(m.get("id")),
                        EstadoEvacuacion.valueOf(trimOrNull(m.get("estado"))));
                guardarSistemaEnJson();
                try {
                    enviarTexto(ex,200,"{\"ok\":true}");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;
            }
            try {
                enviarTexto(ex,405,"{}");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // ---------- USUARIOS ----------
        server.createContext("/api/usuarios", ex -> {
            Headers h=ex.getResponseHeaders(); h.add("Content-Type","application/json; charset=utf-8");
            String json = String.format(
                    "[{\"usuario\":\"%s\",\"nombre\":\"%s\",\"rol\":\"%s\"}," +
                            "{\"usuario\":\"%s\",\"nombre\":\"%s\",\"rol\":\"%s\"}]",
                    ADMIN_USER, ADMIN_NAME, ADMIN_ROLE,
                    OPER_USER,  OPER_NAME,  OPER_ROLE
            );
            try {
                enviarTexto(ex,200,json);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // ---------- PING ----------
        server.createContext("/api/ping", ex -> {
            ex.getResponseHeaders().add("Content-Type","application/json; charset=utf-8");
            try {
                enviarTexto(ex, 200, "{\"pong\":true,\"webserver\":\"v-demo-hardcoded-users\"}");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        server.start();
        System.out.println("🌐 Servidor web activo en http://localhost:" + port);
    }

    // ================== Helpers / utilidades ==================

    private void sincronizarUbicacionesDesdeSistema() {
        equiposSinUbicacion.clear();
        ubicacionesPorNombre.clear();
        ubicacionBodegaEquipos = null;

        for (Ubicacion u : sistema.getGrafo().obtenerTodasLasUbicaciones()) {
            if (NOMBRE_BODEGA_EQUIPOS.equals(u.getNombre())) {
                ubicacionBodegaEquipos = u;
                if (u.getEquiposDeRescate() != null) {
                    equiposSinUbicacion.addAll(u.getEquiposDeRescate());
                }
            } else {
                registrarUbicacion(u);
            }
        }
    }

    private void cargarDemo() {
        // Evacuación neutra
        Evacuacion evac = new Evacuacion("E0",0,0,EstadoEvacuacion.PENDIENTE,null);
        Ubicacion a=new Ubicacion("U1","Ciudad A",TipoZona.CIUDAD,       NivelDeAfectacion.MODERADO,evac,new ArrayList<>(),new ArrayList<>(),new ArrayList<>(), 0.0, 0.0);
        Ubicacion b=new Ubicacion("U2","Refugio B",TipoZona.REFUGIO,     NivelDeAfectacion.LEVE,     evac,new ArrayList<>(),new ArrayList<>(),new ArrayList<>(), 1.0, 1.0);
        Ubicacion c=new Ubicacion("U3","Centro C", TipoZona.CENTRO_AYUDA,NivelDeAfectacion.GRAVE,    evac,new ArrayList<>(),new ArrayList<>(),new ArrayList<>(), 2.0, 2.0);
        registrarUbicacion(a); registrarUbicacion(b); registrarUbicacion(c);
        sistema.agregarUbicacion(a); sistema.agregarUbicacion(b); sistema.agregarUbicacion(c);
        sistema.agregarRuta(new Ruta("R1",a,b,2.5));
        sistema.agregarRuta(new Ruta("R2",b,c,3.0));
        sistema.agregarRuta(new Ruta("R3",a,c,5.0));
    }

    private Ubicacion obtenerUbicacionBodegaEquipos() {
        if (ubicacionBodegaEquipos == null) {
            Evacuacion evac = new Evacuacion("EB"+System.nanoTime(),0,0,EstadoEvacuacion.PENDIENTE,null);
            ubicacionBodegaEquipos = new Ubicacion(
                    "U_BODEGA_EQUIPOS",
                    NOMBRE_BODEGA_EQUIPOS,
                    TipoZona.CENTRO_AYUDA,
                    NivelDeAfectacion.LEVE,
                    evac,
                    new ArrayList<>(),
                    new ArrayList<>(),
                    new ArrayList<>(),
                    0.0,
                    0.0
            );
            // No la registramos en ubicacionesPorNombre para que no salga en el mapa
            sistema.agregarUbicacion(ubicacionBodegaEquipos);
        }
        return ubicacionBodegaEquipos;
    }

    private void registrarUbicacion(Ubicacion u){
        if(u!=null) ubicacionesPorNombre.put(u.getNombre(), u);
    }

    private String listarUbicacionesJson(){
        List<String> items = new ArrayList<>();
        for (Ubicacion u : ubicacionesPorNombre.values()) {
            items.add(String.format(Locale.US,
                    "{\"nombre\":\"%s\",\"tipoZona\":\"%s\",\"nivelAfectacion\":\"%s\",\"latitud\":%.6f,\"longitud\":%.6f}",
                    esc(u.getNombre()), u.getTipoZona(), u.getNivelAfectacion(), u.getLatitud(), u.getLongitud()
            ));
        }
        return "[" + String.join(",", items) + "]";
    }

    private String listarRutasJson(){
        List<String> items=new ArrayList<>();
        for(Ruta r: sistema.getGrafo().obtenerTodasLasRutas()){
            items.add(String.format(Locale.US,
                    "{\"origen\":\"%s\",\"destino\":\"%s\",\"latOrigen\":%.6f,\"lngOrigen\":%.6f,\"latDestino\":%.6f,\"lngDestino\":%.6f,\"distancia\":%.2f}",
                    esc(r.getOrigen().getNombre()),
                    esc(r.getDestino().getNombre()),
                    r.getOrigen().getLatitud(),
                    r.getOrigen().getLongitud(),
                    r.getDestino().getLatitud(),
                    r.getDestino().getLongitud(),
                    r.getDistancia()
            ));
        }
        return "["+String.join(",",items)+"]";
    }

    // --- distancia real aproximada entre dos ubicaciones (Haversine) ---
    private static double distanciaKm(Ubicacion o, Ubicacion d) {
        return distanciaKm(o.getLatitud(), o.getLongitud(), d.getLatitud(), d.getLongitud());
    }

    private static double distanciaKm(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371.0; // radio aproximado de la Tierra en km
        double rad = Math.PI / 180.0;

        double dLat = (lat2 - lat1) * rad;
        double dLon = (lon2 - lon1) * rad;

        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(lat1*rad) * Math.cos(lat2*rad) *
                        Math.sin(dLon/2) * Math.sin(dLon/2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return R * c;
    }

    // Guardar JSON
    private void guardarSistemaEnJson() {
        PersistenciaJson.guardar(sistema);
    }

    // ---- IO helpers ----
    private static String cuerpo(HttpExchange ex) throws IOException {
        return new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }
    private static void enviarTexto(HttpExchange ex,int status,String text)throws IOException{
        byte[] body=text.getBytes(StandardCharsets.UTF_8);
        ex.sendResponseHeaders(status,body.length);
        try(OutputStream os=ex.getResponseBody()){ os.write(body); }
    }
    private static void enviarArchivo(HttpExchange ex,String resourcePath,String contentType)throws IOException{
        byte[] body=leerRecurso(resourcePath);
        ex.getResponseHeaders().add("Content-Type",contentType);
        ex.sendResponseHeaders(200,body.length);
        try(OutputStream os=ex.getResponseBody()){ os.write(body); }
    }
    private static byte[] leerRecurso(String ruta)throws IOException{
        try(InputStream is=WebServer.class.getClassLoader().getResourceAsStream(ruta)){
            if(is!=null) return is.readAllBytes();
        }
        String dev="src/main/resources/"+ruta;
        if(Files.exists(Paths.get(dev))) return Files.readAllBytes(Paths.get(dev));
        return ("No encontrado: "+ruta).getBytes(StandardCharsets.UTF_8);
    }
    private static String mime(String path){
        String p = path.toLowerCase(Locale.ROOT);
        if(p.endsWith(".css")) return "text/css; charset=utf-8";
        if(p.endsWith(".html")) return "text/html; charset=utf-8";
        if(p.endsWith(".js")) return "application/javascript; charset=utf-8";
        if(p.endsWith(".json")) return "application/json; charset=utf-8";
        if(p.endsWith(".png")) return "image/png";
        if(p.endsWith(".jpg")||p.endsWith(".jpeg")) return "image/jpeg";
        if(p.endsWith(".svg")) return "image/svg+xml";
        return "text/plain; charset=utf-8";
    }

    // ---- util JSON/query simple ----
    private static Map<String,String> tinyJson(String json){
        Map<String,String> map=new HashMap<>();
        if(json==null) return map;
        json=json.trim();
        if(json.isEmpty()) return map;
        json=json.replaceAll("[{}\"]","");
        if(json.isBlank()) return map;
        for(String p: json.split(",")){
            String[] kv=p.split(":",2);
            if(kv.length==2) map.put(kv[0].trim(),kv[1].trim());
        }
        return map;
    }
    private static Map<String,String> query(String q){
        Map<String,String> map=new HashMap<>();
        if(q==null) return map;
        for(String p: q.split("&")){
            String[] kv=p.split("=",2);
            if(kv.length==2) map.put(kv[0],kv[1]);
        }
        return map;
    }
    private static double parseDoubleSafe(String s,double def){
        try{
            return (s==null||s.isBlank())?def:Double.parseDouble(s);
        }catch(Exception e){
            return def;
        }
    }
    private static String trimOrNull(String s){ return s==null? null : s.trim(); }
    private static String esc(String s){ return s==null? "" : s.replace("\"","\\\""); }
}
