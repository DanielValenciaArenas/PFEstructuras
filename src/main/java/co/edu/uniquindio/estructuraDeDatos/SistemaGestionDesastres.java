package co.edu.uniquindio.estructuraDeDatos;

import java.util.List;

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

    public void agregarUbicacion(Ubicacion u) {
        if (u != null) grafo.agregarUbicacion(u);
    }

    public void agregarRuta(Ruta r) {
        if (r != null) grafo.agregarRuta(r);
    }

    public void registrarEvacuacion(Evacuacion e) {
        if (e != null) colaEvacuaciones.insertar(e);
    }

    public SimulacionRuta simularRuta(Ubicacion origen, Ubicacion destino) {
        var nodos = grafo.buscarCaminoDijkstra(origen, destino);
        if (nodos == null) return null;
        return new SimulacionRuta("SIM-" + System.nanoTime(), 0, java.util.List.of(), nodos);
    }

    public GrafoTransporte getGrafo() {
        return grafo;
    }

    public MapaRecursos getMapaRecursos() {
        return mapaRecursos;
    }

    public List<Usuario> getUsuarios() {
        return usuarios;
    }

    public List<Ubicacion> getUbicaciones() {
        return grafo.obtenerTodasLasUbicaciones();
    }

    public ColaPrioridadEvacuacion getColaEvacuaciones() {
        return colaEvacuaciones;
    }

    public ArbolDistribuido getArbolDistribucion() {
        return arbolDistribucion;
    }

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
        // Si tienes MapaRecursos.obtenerTodosLosRecursos(), úsalo aquí; si no, cuenta por ubicación desde WebServer.
        return mapaRecursos.obtenerTodosLosRecursos().size();
    }

    public int contarEvacuacionesPendientes() {
        return (int) colaEvacuaciones.listarTodas().stream()
                .filter(e -> e.getEstado() == EstadoEvacuacion.PENDIENTE)
                .count();
    }
}
