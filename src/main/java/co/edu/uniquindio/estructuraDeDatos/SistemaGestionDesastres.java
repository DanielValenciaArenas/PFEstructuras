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

    public void registrarUsuario(Usuario u) {}
    public Usuario autenticar(String username, String password) { return null; }
    public void agregarUbicacion(Ubicacion u) {}
    public void agregarRuta(Ruta r) {}
    public void registrarEvacuacion(Evacuacion e) {}
    public SimulacionRuta simularRuta(Ubicacion origen, Ubicacion destino) { return null; }

}
