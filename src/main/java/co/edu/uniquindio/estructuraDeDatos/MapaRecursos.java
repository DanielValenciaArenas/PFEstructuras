package co.edu.uniquindio.estructuraDeDatos;

import java.util.*;

public class MapaRecursos {

    private final Map<Ubicacion, List<Recurso>> mapa = new HashMap<>();

    public MapaRecursos() {}

    public void agregarRecurso(Ubicacion u, Recurso r) {
        if (u == null || r == null) return;
        mapa.computeIfAbsent(u, k -> new ArrayList<>()).add(r);
        r.asignarUbicacion(u);
    }

    public List<Recurso> obtenerRecursos(Ubicacion u) {
        return mapa.getOrDefault(u, Collections.emptyList());
    }

    public void actualizarCantidad(Ubicacion u, Recurso r, int nuevaCantidad) {
        if (u == null || r == null || nuevaCantidad < 0) return;
        r.setCantidad(nuevaCantidad);
    }
}
