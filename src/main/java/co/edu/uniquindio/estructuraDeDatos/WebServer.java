package co.edu.uniquindio.estructuraDeDatos;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Servidor HTTP ligero para exponer interfaces HTML y APIs.
 * NOTA: versión “segura” para compilar aunque falten métodos en ArbolDistribuido
 * y Usuario sea abstracta. El login usa dos cuentas demo internas (admin/oper).
 */
public class WebServer {

    private final SistemaGestionDesastres sistema;
    private final Map<String, Ubicacion> ubicacionesPorNombre = new LinkedHashMap<>();

    // ======== Cuentas demo (no usa tu clase Usuario) =========
    private static final String ADMIN_USER = "admin";
    private static final String ADMIN_PASS = "admin123";
    private static final String ADMIN_ROLE = "ADMINISTRADOR";
    private static final String ADMIN_NAME = "Ana Admin";

    private static final String OPER_USER = "oper";
    private static final String OPER_PASS = "oper123";
    private static final String OPER_ROLE = "OPERADOR";
    private static final String OPER_NAME = "Oscar Operador";
    // =========================================================

    public WebServer(SistemaGestionDesastres sistema) {
        this.sistema = sistema;
    }

    public void start(int port) throws Exception {
        cargarDemo(); // ubicaciones + rutas básicas

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
            Headers h = ex.getResponseHeaders(); h.add("Content-Type", "application/json; charset=utf-8");
            String method = ex.getRequestMethod();
            if ("GET".equals(method)) { enviarTexto(ex, 200, listarUbicacionesJson()); return; }
            if ("POST".equals(method)) {
                Map<String,String> m = tinyJson(cuerpo(ex));
                String nombre = trimOrNull(m.get("nombre"));
                String tipo   = m.getOrDefault("tipo","CIUDAD").trim();
                String nivel  = m.getOrDefault("nivel","LEVE").trim();
                if (nombre==null || nombre.isEmpty()) { enviarTexto(ex,400,"{\"error\":\"nombre requerido\"}"); return; }
                Evacuacion evac = new Evacuacion("E"+System.nanoTime(),0,0,EstadoEvacuacion.PENDIENTE,null);
                Ubicacion u = new Ubicacion("U"+System.nanoTime(), nombre, TipoZona.valueOf(tipo),
                        NivelDeAfectacion.valueOf(nivel), evac, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
                registrarUbicacion(u); sistema.agregarUbicacion(u);
                enviarTexto(ex,200,"{\"ok\":true}"); return;
            }
            enviarTexto(ex,405,"{}");
        });

        // ---------- RUTAS ----------
        server.createContext("/api/rutas", ex -> {
            Headers h = ex.getResponseHeaders(); h.add("Content-Type", "application/json; charset=utf-8");
            String method = ex.getRequestMethod();
            if ("GET".equals(method)) { enviarTexto(ex,200,listarRutasJson()); return; }
            if ("POST".equals(method)) {
                Map<String,String> m = tinyJson(cuerpo(ex));
                String so=trimOrNull(m.get("origen")), sd=trimOrNull(m.get("destino"));
                double dist = parseDoubleSafe(m.get("distancia"), 0);
                if (so==null || sd==null) { enviarTexto(ex,400,"{\"error\":\"origen/destino\"}"); return; }
                if (dist<=0) { enviarTexto(ex,400,"{\"error\":\"distancia debe ser > 0\"}"); return; }
                Ubicacion o=ubicacionesPorNombre.get(so), d=ubicacionesPorNombre.get(sd);
                if (o==null||d==null){ enviarTexto(ex,400,"{\"error\":\"ubicación inválida\"}"); return; }
                sistema.agregarRuta(new Ruta("R"+System.nanoTime(),o,d,dist));
                enviarTexto(ex,200,"{\"ok\":true}"); return;
            }
            enviarTexto(ex,405,"{}");
        });

        // Calcular ruta más corta
        server.createContext("/api/rutas/corta", ex -> {
            Map<String,String> q = query(ex.getRequestURI().getQuery());
            Ubicacion o = ubicacionesPorNombre.get(trimOrNull(q.get("origen")));
            Ubicacion d = ubicacionesPorNombre.get(trimOrNull(q.get("destino")));
            if (o==null||d==null){ enviarTexto(ex,400,"Ubicaciones no válidas"); return; }
            var camino = sistema.getGrafo().buscarCaminoDijkstra(o,d);
            if (camino==null || camino.isEmpty()){ enviarTexto(ex,200,"No existe una ruta entre esas ubicaciones"); return; }
            double total=0;
            for(int i=0;i<camino.size()-1;i++){
                Ubicacion a=camino.get(i), b=camino.get(i+1);
                for(Ruta r: sistema.getGrafo().obtenerRutasDesde(a)) if (r.getDestino().equals(b)) total+=r.getDistancia();
            }
            StringBuilder sb=new StringBuilder("Ruta más corta:\n");
            for(int i=0;i<camino.size();i++){ sb.append(camino.get(i).getNombre()); if(i<camino.size()-1) sb.append(" -> "); }
            sb.append("\nDistancia total: ").append(total).append(" km");
            enviarTexto(ex,200,sb.toString());
        });

        // ---------- RECURSOS ----------
        server.createContext("/api/recursos", ex -> {
            Headers h = ex.getResponseHeaders(); h.add("Content-Type","application/json; charset=utf-8");
            String method = ex.getRequestMethod();
            if ("GET".equals(method)) {
                String ubic = query(ex.getRequestURI().getQuery()).get("ubicacion");
                if (ubic!=null) {
                    Ubicacion u = ubicacionesPorNombre.get(ubic);
                    List<String> arr=new ArrayList<>();
                    for(Recurso r: sistema.getMapaRecursos().obtenerRecursos(u)){
                        arr.add(String.format("{\"tipo\":\"%s\",\"cantidad\":%d}", r.getClass().getSimpleName(), r.getCantidad()));
                    }
                    enviarTexto(ex,200,"["+String.join(",",arr)+"]");
                    return;
                }
                List<String> json=new ArrayList<>();
                for(Recurso r: sistema.getMapaRecursos().obtenerTodosLosRecursos()){
                    String nom = r.getUbicacion()!=null? r.getUbicacion().getNombre() : "Sin ubicación";
                    json.add(String.format("{\"tipo\":\"%s\",\"cantidad\":%d,\"ubicacion\":\"%s\"}",
                            r.getClass().getSimpleName(), r.getCantidad(), esc(nom)));
                }
                enviarTexto(ex,200,"["+String.join(",",json)+"]"); return;
            }
            if ("POST".equals(method)) {
                Map<String,String> m=tinyJson(cuerpo(ex));
                String tipo=m.getOrDefault("tipo","ALIMENTO").trim();
                int cantidad=(int)parseDoubleSafe(m.getOrDefault("cantidad","0"),0);
                Ubicacion u=ubicacionesPorNombre.get(trimOrNull(m.get("ubicacion")));
                if(cantidad<=0||u==null){ enviarTexto(ex,400,"{\"error\":\"datos\"}"); return; }
                Recurso rec = tipo.equalsIgnoreCase("ALIMENTO")
                        ? new RecursoAlimento("RA"+System.nanoTime(),"ALIMENTO",cantidad,u, java.time.LocalDate.now().plusDays(30))
                        : new RecursoMedicina("RM"+System.nanoTime(),"MEDICINA",cantidad,u,"General");
                sistema.getMapaRecursos().agregarRecurso(u, rec);
                enviarTexto(ex,200,"{\"ok\":true}"); return;
            }
            enviarTexto(ex,405,"{}");
        });

        server.createContext("/api/recursos/transferir", ex -> {
            if (!"POST".equals(ex.getRequestMethod())) { enviarTexto(ex,405,"{}"); return; }
            Map<String,String> m=tinyJson(cuerpo(ex));
            Ubicacion o=ubicacionesPorNombre.get(trimOrNull(m.get("origen")));
            Ubicacion d=ubicacionesPorNombre.get(trimOrNull(m.get("destino")));
            String id = trimOrNull(m.get("idRecurso"));
            int cant = (int)parseDoubleSafe(m.getOrDefault("cantidad","0"),0);
            var result = sistema.getMapaRecursos().transferirRecurso(o,d,id,cant);
            enviarTexto(ex,200, result!=null? "{\"ok\":true}" : "{\"ok\":false}");
        });

        // ---------- EQUIPOS ----------
        server.createContext("/api/equipos", ex -> {
            Headers h=ex.getResponseHeaders(); h.add("Content-Type","application/json; charset=utf-8");
            String method=ex.getRequestMethod();
            if ("GET".equals(method)) {
                List<String> items=new ArrayList<>();
                for(Ubicacion u: ubicacionesPorNombre.values()){
                    for(EquipoRescate e: u.getEquiposDeRescate()){
                        items.add(String.format("{\"nombre\":\"%s\",\"tipo\":\"%s\",\"miembros\":%d,\"ubicacion\":\"%s\"}",
                                esc(e.getNombre()), esc(e.getTipo()), e.getMiembros(), esc(u.getNombre())));
                    }
                }
                enviarTexto(ex,200,"["+String.join(",",items)+"]"); return;
            }
            if ("POST".equals(method)) {
                Map<String,String> m=tinyJson(cuerpo(ex));
                String nombre=trimOrNull(m.get("nombre")), tipo=m.getOrDefault("tipo","GENERAL").trim();
                int miembros=(int)parseDoubleSafe(m.getOrDefault("miembros","0"),0);
                Ubicacion u=ubicacionesPorNombre.get(trimOrNull(m.get("ubicacion")));
                if(nombre==null||miembros<=0||u==null){ enviarTexto(ex,400,"{\"error\":\"datos\"}"); return; }
                EquipoRescate eq=new EquipoRescate("EQ"+System.nanoTime(),tipo,miembros,u); eq.setNombre(nombre);
                u.asignarEquipo(eq); enviarTexto(ex,200,"{\"ok\":true}"); return;
            }
            enviarTexto(ex,405,"{}");
        });

        // ---------- LOGIN (no usa tu modelo Usuario abstracto) ----------
        server.createContext("/api/login", ex -> {
            Headers h=ex.getResponseHeaders(); h.add("Content-Type","application/json; charset=utf-8");
            if (!"POST".equals(ex.getRequestMethod())) { enviarTexto(ex,405,"{}"); return; }
            Map<String,String> m=tinyJson(cuerpo(ex));
            String usr = trimOrNull(m.get("usuario"));
            String pwd = trimOrNull(m.get("contrasena"));

            if (ADMIN_USER.equals(usr) && ADMIN_PASS.equals(pwd)) {
                enviarTexto(ex,200,String.format("{\"ok\":true,\"rol\":\"%s\",\"nombre\":\"%s\"}", ADMIN_ROLE, ADMIN_NAME));
                return;
            }
            if (OPER_USER.equals(usr) && OPER_PASS.equals(pwd)) {
                enviarTexto(ex,200,String.format("{\"ok\":true,\"rol\":\"%s\",\"nombre\":\"%s\"}", OPER_ROLE, OPER_NAME));
                return;
            }
            enviarTexto(ex,401,"{\"ok\":false}");
        });

        // ---------- RESUMEN (no usa métodos del árbol) ----------
        server.createContext("/api/resumen", ex -> {
            Headers h=ex.getResponseHeaders(); h.add("Content-Type","application/json; charset=utf-8");
            String json = String.format(
                    "{\"ubicaciones\":{\"leve\":%d,\"moderado\":%d,\"grave\":%d},\"recursos\":%d,\"equipos\":%d,\"pendientes\":%d}",
                    sistema.contarUbicacionesPorNivel(NivelDeAfectacion.LEVE),
                    sistema.contarUbicacionesPorNivel(NivelDeAfectacion.MODERADO),
                    sistema.contarUbicacionesPorNivel(NivelDeAfectacion.GRAVE),
                    sistema.contarRecursos(), sistema.contarEquipos(), sistema.contarEvacuacionesPendientes());
            enviarTexto(ex,200,json);
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
                enviarTexto(ex,200,"["+String.join(",",arr)+"]"); return;
            }
            if ("POST".equals(method)) {
                Map<String,String> m=tinyJson(cuerpo(ex));
                Ubicacion u = ubicacionesPorNombre.get(trimOrNull(m.get("ubicacion")));
                int prio=(int)parseDoubleSafe(m.getOrDefault("prioridad","0"),0);
                int pers=(int)parseDoubleSafe(m.getOrDefault("personas","0"),0);
                if (u==null||prio<0||pers<=0){ enviarTexto(ex,400,"{\"error\":\"datos\"}"); return; }
                sistema.getColaEvacuaciones().insertar(new Evacuacion("EV"+System.nanoTime(), prio, pers, EstadoEvacuacion.PENDIENTE, u));
                enviarTexto(ex,200,"{\"ok\":true}"); return;
            }
            if ("PUT".equals(method)) {
                Map<String,String> m=tinyJson(cuerpo(ex));
                sistema.getColaEvacuaciones().actualizarEstado(trimOrNull(m.get("id")),
                        EstadoEvacuacion.valueOf(trimOrNull(m.get("estado"))));
                enviarTexto(ex,200,"{\"ok\":true}"); return;
            }
            enviarTexto(ex,405,"{}");
        });

        // ---------- USUARIOS (devuelve las cuentas demo) ----------
        server.createContext("/api/usuarios", ex -> {
            Headers h=ex.getResponseHeaders(); h.add("Content-Type","application/json; charset=utf-8");
            String json = String.format(
                    "[{\"usuario\":\"%s\",\"nombre\":\"%s\",\"rol\":\"%s\"}," +
                            "{\"usuario\":\"%s\",\"nombre\":\"%s\",\"rol\":\"%s\"}]",
                    ADMIN_USER, ADMIN_NAME, ADMIN_ROLE,
                    OPER_USER,  OPER_NAME,  OPER_ROLE
            );
            enviarTexto(ex,200,json);
        });

        // ---------- PING (verificación de versión en ejecución) ----------
        server.createContext("/api/ping", ex -> {
            ex.getResponseHeaders().add("Content-Type","application/json; charset=utf-8");
            enviarTexto(ex, 200, "{\"pong\":true,\"webserver\":\"v-demo-hardcoded-users\"}");
        });

        server.start();
        System.out.println("🌐 Servidor web activo en http://localhost:" + port);
    }

    // ================== Helpers / utilidades ==================

    private void cargarDemo() {
        // Evacuación neutra
        Evacuacion evac = new Evacuacion("E0",0,0,EstadoEvacuacion.PENDIENTE,null);
        Ubicacion a=new Ubicacion("U1","Ciudad A",TipoZona.CIUDAD,       NivelDeAfectacion.MODERADO,evac,new ArrayList<>(),new ArrayList<>(),new ArrayList<>());
        Ubicacion b=new Ubicacion("U2","Refugio B",TipoZona.REFUGIO,     NivelDeAfectacion.LEVE,     evac,new ArrayList<>(),new ArrayList<>(),new ArrayList<>());
        Ubicacion c=new Ubicacion("U3","Centro C", TipoZona.CENTRO_AYUDA,NivelDeAfectacion.GRAVE,    evac,new ArrayList<>(),new ArrayList<>(),new ArrayList<>());
        registrarUbicacion(a); registrarUbicacion(b); registrarUbicacion(c);
        sistema.agregarUbicacion(a); sistema.agregarUbicacion(b); sistema.agregarUbicacion(c);
        sistema.agregarRuta(new Ruta("R1",a,b,2.5));
        sistema.agregarRuta(new Ruta("R2",b,c,3.0));
        sistema.agregarRuta(new Ruta("R3",a,c,5.0));
    }

    private void registrarUbicacion(Ubicacion u){ if(u!=null) ubicacionesPorNombre.put(u.getNombre(), u); }

    private String listarUbicacionesJson(){
        List<String> items=new ArrayList<>();
        for(Ubicacion u: ubicacionesPorNombre.values()){
            items.add(String.format("{\"nombre\":\"%s\",\"tipo\":\"%s\",\"afectacion\":\"%s\"}",
                    esc(u.getNombre()),u.getTipoZona(),u.getNivelAfectacion()));
        }
        return "["+String.join(",",items)+"]";
    }
    private String listarRutasJson(){
        List<String> items=new ArrayList<>();
        for(Ruta r: sistema.getGrafo().obtenerTodasLasRutas()){
            items.add(String.format("{\"origen\":\"%s\",\"destino\":\"%s\",\"distancia\":%.2f}",
                    esc(r.getOrigen().getNombre()), esc(r.getDestino().getNombre()), r.getDistancia()));
        }
        return "["+String.join(",",items)+"]";
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
    private static double parseDoubleSafe(String s,double def){ try{ return (s==null||s.isBlank())?def:Double.parseDouble(s);}catch(Exception e){return def;} }
    private static String trimOrNull(String s){ return s==null? null : s.trim(); }
    private static String esc(String s){ return s==null? "" : s.replace("\"","\\\""); }
}
