package co.edu.uniquindio.estructuraDeDatos;

import java.util.List;

public class SistemaGestionDesastres {
    private GrafoTransporte grafo;
    private ColaPrioridadEvacuacion colaEvacuaciones;
    private MapaRecursos mapaRecursos;
    private ArbolDistribuido arbolDistribucion;
    private List<Usuario> usuarios;

    public SistemaGestionDesastres(GrafoTransporte grafo, ColaPrioridadEvacuacion colaEvacuaciones, MapaRecursos mapaRecursos,
                                   ArbolDistribuido arbolDistribucion, List<Usuario> usuarios) {
        this.grafo = grafo;
        this.colaEvacuaciones = colaEvacuaciones;
        this.mapaRecursos = mapaRecursos;
        this.arbolDistribucion = arbolDistribucion;
        this.usuarios = usuarios;
    }

    // Registrar usuario si no existe username
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

    // Autenticación básica delegada a cada usuario
    public Usuario autenticar(String NombreUsuario, String contrasena) {
        for (Usuario usuario : usuarios) {
            if (usuario.autenticar(NombreUsuario, contrasena)) {
                System.out.println(usuario.getNombre() + " iniciaste sesión!, Bienvenid@");
                return usuario;
            }
        }
        System.out.println("Error: El nombre de usuario o la contraseña son incorrectos");
        return null;
    }

    // Facade simple para grafo/cola
    public void agregarUbicacion(Ubicacion u) { if (u != null) grafo.agregarUbicacion(u); }

    public void agregarRuta(Ruta r) { if (r != null) grafo.agregarRuta(r); }

    public void registrarEvacuacion(Evacuacion e) { if (e != null) colaEvacuaciones.insertar(e); }

    public SimulacionRuta simularRuta(Ubicacion origen, Ubicacion destino) {
        var nodos = grafo.buscarCaminoDijkstra(origen, destino);
        if (nodos == null) return null;
        return new SimulacionRuta("SIM-"+System.nanoTime(), /*tiempoTotal*/0, java.util.List.of(), nodos);
    }
    public GrafoTransporte getGrafo() { return grafo; }

    public MapaRecursos getMapaRecursos() {
        return mapaRecursos;
    }


}
