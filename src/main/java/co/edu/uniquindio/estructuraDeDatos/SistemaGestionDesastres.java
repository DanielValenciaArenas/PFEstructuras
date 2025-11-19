package co.edu.uniquindio.estructuraDeDatos;

import java.util.Map;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;

public class SistemaGestionDesastres {

    private GrafoTransporte grafo;
    private ColaPrioridadEvacuacion colaEvacuaciones;
    private MapaRecursos mapaRecursos;
    private ArbolDistribuido arbolDistribucion;
    private List<Usuario> usuarios;

    public SistemaGestionDesastres(GrafoTransporte grafo,
                                   ColaPrioridadEvacuacion colaEvacuaciones,
                                   MapaRecursos mapaRecursos,
                                   ArbolDistribuido arbolDistribucion,
                                   List<Usuario> usuarios) {
        this.grafo = grafo;
        this.colaEvacuaciones = colaEvacuaciones;
        this.mapaRecursos = mapaRecursos;
        this.arbolDistribucion = arbolDistribucion;
        this.usuarios = usuarios;
    }

    // =============================
    // USUARIOS
    // =============================

    public void registrarUsuario(Usuario nuevoUsuario) {
        for (Usuario usuario : usuarios) {
            if (usuario.getUsuario().equals(nuevoUsuario.getUsuario())) {
                System.out.println("Error: el nombre de usuario ya existe");
                return;
            }
        }
        usuarios.add(nuevoUsuario);
        System.out.println("Usuario registrado exitosamente: " + nuevoUsuario.getNombre());
    }

    public Usuario autenticar(String nombreUsuario, String contrasena) {
        for (Usuario usuario : usuarios) {
            if (usuario.autenticar(nombreUsuario, contrasena)) {
                System.out.println(usuario.getNombre() + " iniciaste sesión!, Bienvenid@");
                return usuario;
            }
        }
        System.out.println("Error: El nombre de usuario o la contraseña son incorrectos");
        return null;
    }

    // =============================
    // UBICACIONES Y RUTAS
    // =============================

    public void agregarUbicacion(Ubicacion u) {
        if (u != null) grafo.agregarUbicacion(u);
    }

    public void agregarRuta(Ruta r) {
        if (r != null) grafo.agregarRuta(r);
    }

    public List<Ubicacion> getUbicaciones() {
        return grafo.obtenerTodasLasUbicaciones();
    }

    // =============================
    // EVACUACIONES
    // =============================

    public void registrarEvacuacion(Evacuacion e) {
        if (e != null) colaEvacuaciones.insertar(e);
    }

    public ColaPrioridadEvacuacion getColaEvacuaciones() {
        return colaEvacuaciones;
    }

    public int contarEvacuacionesPendientes() {
        return (int) colaEvacuaciones.listarTodas().stream()
                .filter(e -> e.getEstado() == EstadoEvacuacion.PENDIENTE)
                .count();
    }

    /** Elimina una evacuación por id de la cola de prioridad. */
    public boolean eliminarEvacuacionPorId(String id) {
        if (id == null) return false;
        return colaEvacuaciones.eliminarPorId(id);
    }

    // =============================
    // RUTAS / SIMULACIONES
    // =============================

    public SimulacionRuta simularRuta(Ubicacion origen, Ubicacion destino) {
        var camino = grafo.buscarCaminoDijkstra(origen, destino);
        if (camino == null) return null;
        SimulacionRuta sim = new SimulacionRuta("SIM-" + System.nanoTime(), 0, java.util.List.of(), camino);
        sim.ejecutarSimulacion(grafo, origen, destino);
        return sim;
    }

    // =============================
    // GETTERS DE COMPONENTES
    // =============================

    public GrafoTransporte getGrafo() {
        return grafo;
    }

    public MapaRecursos getMapaRecursos() {
        return mapaRecursos;
    }

    public ArbolDistribuido getArbolDistribucion() {
        return arbolDistribucion;
    }

    public List<Usuario> getUsuarios() {
        return usuarios;
    }

    // =============================
    // MÉTRICAS
    // =============================

    public long contarUbicacionesPorNivel(NivelDeAfectacion nivel) {
        return grafo.obtenerTodasLasUbicaciones().stream()
                .filter(u -> u.getNivelAfectacion() == nivel)
                .count();
    }

    public int contarEquipos() {
        return (int) grafo.obtenerTodasLasUbicaciones().stream()
                .flatMap(u -> u.getEquiposDeRescate().stream())
                .count();
    }

    public int contarRecursos() {
        return mapaRecursos.obtenerTodosLosRecursos().size();
    }

    // =========================================================
    //  LÓGICA DE RECURSOS + ÁRBOL DE DISTRIBUCIÓN
    // =========================================================

    /**
     * Registra un recurso en el sistema:
     *  - Lo agrega al MapaRecursos (para consultas por ubicación)
     *  - Lo inserta en el ArbolDistribuido (para representar la distribución)
     */
    public void registrarRecurso(Ubicacion ubicacion, Recurso recurso) {
        if (ubicacion == null || recurso == null) return;

        // Mantener el mapa de recursos
        mapaRecursos.agregarRecurso(ubicacion, recurso);

        // Mantener el árbol de distribución
        if (arbolDistribucion != null) {
            arbolDistribucion.insertar(ubicacion, recurso);
        }
    }

    /**
     * Asigna una prioridad numérica según el nivel de afectación.
     * GRAVE > MODERADO > LEVE
     */
    private int prioridadPorNivel(NivelDeAfectacion nivel) {
        if (nivel == null) return 0;
        switch (nivel) {
            case GRAVE:     return 3;
            case MODERADO:  return 2;
            case LEVE:      return 1;
            default:        return 0;
        }
    }

    /**
     * Distancia "geográfica" aproximada usando latitud y longitud.
     */
    private double distanciaGeografica(Ubicacion a, Ubicacion b) {
        if (a == null || b == null) return Double.MAX_VALUE;
        double dx = a.getLatitud() - b.getLatitud();
        double dy = a.getLongitud() - b.getLongitud();
        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * Devuelve las zonas ordenadas:
     *  1) Primero por nivel de afectación (GRAVE > MODERADO > LEVE)
     *  2) Después por proximidad a una ubicación origen (más cerca primero)
     */
    private List<Ubicacion> ordenarZonasPorPrioridadYProximidad(Ubicacion origen) {
        List<Ubicacion> zonas = new ArrayList<>(grafo.obtenerTodasLasUbicaciones());
        zonas.remove(origen); // no queremos distribuir a la misma zona origen

        zonas.sort((u1, u2) -> {
            int p1 = prioridadPorNivel(u1.getNivelAfectacion());
            int p2 = prioridadPorNivel(u2.getNivelAfectacion());

            // Primero, comparar por prioridad (mayor primero)
            if (p1 != p2) {
                return Integer.compare(p2, p1); // GRAVE antes que LEVE
            }

            // Si tienen el mismo nivel, usar distancia (más cerca primero)
            double d1 = distanciaGeografica(origen, u1);
            double d2 = distanciaGeografica(origen, u2);
            return Double.compare(d1, d2);
        });

        return zonas;
    }

    /**
     * Busca un recurso por ID dentro de una ubicación específica usando el MapaRecursos.
     */
    private Recurso buscarRecursoEnUbicacion(Ubicacion ubicacion, String idRecurso) {
        if (ubicacion == null || idRecurso == null) return null;
        for (Recurso r : mapaRecursos.obtenerRecursos(ubicacion)) {
            if (idRecurso.equals(r.getIdRecurso())) {
                return r;
            }
        }
        return null;
    }

    /**
     * Distribuye un recurso desde una ubicación origen (bodega/stock) hacia
     * otras zonas, de forma proporcional a:
     *  - Nivel de afectación (GRAVE > MODERADO > LEVE)
     *  - Número de personas en la zona
     *
     * Cada zona se procesa UNA sola vez (sin repetir).
     */
    public List<String> distribuirRecursoPrioritario(String nombreUbicacionOrigen,
                                                     String idRecurso,
                                                     int cantidadSolicitada) {

        List<String> log = new ArrayList<>();
        final String NOMBRE_BODEGA_EQUIPOS = "__SIN_UBICACION__";

        if (cantidadSolicitada <= 0) {
            log.add("La cantidad a repartir debe ser mayor que 0.");
            return log;
        }

        // 1. Buscar ubicación origen por nombre
        Ubicacion origen = null;
        for (Ubicacion u : grafo.obtenerTodasLasUbicaciones()) {
            if (u.getNombre().equalsIgnoreCase(nombreUbicacionOrigen)) {
                origen = u;
                break;
            }
        }

        if (origen == null) {
            log.add("No se encontró la ubicación origen: " + nombreUbicacionOrigen);
            return log;
        }

        // 2. Buscar el recurso en el origen (bodega / stock)
        Recurso recursoStock = buscarRecursoEnUbicacion(origen, idRecurso);
        if (recursoStock == null) {
            log.add("No se encontró el recurso con ID " + idRecurso
                    + " en la ubicación " + origen.getNombre());
            return log;
        }

        int stockDisponible = recursoStock.getCantidad();
        if (stockDisponible <= 0) {
            log.add("No hay stock disponible del recurso en " + origen.getNombre() + ".");
            return log;
        }

        int totalARepartir = Math.min(stockDisponible, cantidadSolicitada);
        log.add("Stock disponible en " + origen.getNombre() + ": " + stockDisponible
                + ". Se intentará distribuir: " + totalARepartir + " unidades.");

        // 3. Construir la lista de destinos ÚNICOS (sin repetir ubicaciones)
        class DestinoPeso {
            Ubicacion u;
            int prioridad;
            int personas;
            int peso;
        }

        // Usamos un mapa por ID de ubicación para evitar duplicados
        Map<String, DestinoPeso> destinosMap = new LinkedHashMap<>();
        int sumaPesos = 0;

        for (Ubicacion u : grafo.obtenerTodasLasUbicaciones()) {
            // No repartimos al origen ni a la bodega oculta de equipos
            if (u == origen) continue;
            if (NOMBRE_BODEGA_EQUIPOS.equals(u.getNombre())) continue;

            // Evitar duplicados: si ya vimos esta id, la ignoramos
            if (destinosMap.containsKey(u.getIdUbicacion())) continue;

            int prio = prioridadPorNivel(u.getNivelAfectacion());
            if (prio <= 0) continue;

            int numPersonas = (u.getPersonas() != null) ? u.getPersonas().size() : 0;
            // si no hay personas, usamos 1 para que la zona no quede fuera
            int peso = prio * (numPersonas > 0 ? numPersonas : 1);

            DestinoPeso d = new DestinoPeso();
            d.u = u;
            d.prioridad = prio;
            d.personas = numPersonas;
            d.peso = peso;

            destinosMap.put(u.getIdUbicacion(), d);
            sumaPesos += peso;
        }

        List<DestinoPeso> destinos = new ArrayList<>(destinosMap.values());

        if (destinos.isEmpty() || sumaPesos == 0) {
            log.add("No hay zonas registradas con nivel de afectación para recibir recursos.");
            return log;
        }

        // 4. Reparto proporcional al peso de cada destino (una sola vez por zona)
        int restante = totalARepartir;

        for (int i = 0; i < destinos.size(); i++) {
            DestinoPeso d = destinos.get(i);

            int asignado;
            if (i == destinos.size() - 1) {
                // último destino: todo lo que queda (evita perder por redondeos)
                asignado = restante;
            } else {
                asignado = (int) Math.floor((double) totalARepartir * d.peso / sumaPesos);
                if (asignado > restante) asignado = restante;
            }

            if (asignado <= 0) continue;

            // Transferencia real desde la bodega hacia la zona
            Recurso movido = mapaRecursos.transferirRecurso(origen, d.u, idRecurso, asignado);
            if (movido != null) {
                // Reflejar en el árbol de distribución
                if (arbolDistribucion != null) {
                    arbolDistribucion.insertar(d.u, movido);
                }

                log.add("Se enviaron " + asignado +
                        " unidades de '" + movido.getNombre() +
                        "' hacia " + d.u.getNombre() +
                        " (nivel " + d.u.getNivelAfectacion() +
                        ", personas=" + d.personas + ").");

                restante -= asignado;
                if (restante <= 0) break;
            }
        }

        if (restante > 0) {
            log.add("Quedaron sin repartir " + restante +
                    " unidades en " + origen.getNombre() + ".");
        } else {
            log.add("No quedaron unidades sin repartir en " + origen.getNombre() + ".");
        }

        return log;
    }
}