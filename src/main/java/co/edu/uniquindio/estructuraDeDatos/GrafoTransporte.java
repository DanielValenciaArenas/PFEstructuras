package co.edu.uniquindio.estructuraDeDatos;

import java.util.List;

public class GrafoTransporte {
    private List<Ubicacion> ubicaciones;
    private List<Ruta> rutas;

    public GrafoTransporte(List<Ubicacion> ubicaciones, List<Ruta> rutas) {
        this.ubicaciones = ubicaciones;
        this.rutas = rutas;
    }

    public void agregarUbicacion(Ubicacion u) {}
    public void agregarRuta(Ruta r) {}
    public List<Ruta> buscarRuta(Ubicacion origen, Ubicacion destino) { return null; }
    public List<Ubicacion> obtenerVecinos(Ubicacion u) { return null; }
}
