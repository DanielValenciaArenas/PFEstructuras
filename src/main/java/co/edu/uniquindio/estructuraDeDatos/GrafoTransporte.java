package co.edu.uniquindio.estructuraDeDatos;

import java.util.*;

public class GrafoTransporte {
    private final Map<Ubicacion, List<Ruta>> adyacencia;

    public GrafoTransporte() {
        adyacencia = new HashMap<>();
    }

    // Agregar ubicación (nodo) si no existe
    public void agregarUbicacion(Ubicacion ubicacion) {
        if (ubicacion == null) return;
        adyacencia.computeIfAbsent(ubicacion, k -> new ArrayList<>());
    }

    // Agregar ruta (asegura presencia de origen y destino)
    public void agregarRuta(Ruta ruta) {
        if (ruta == null) return;
        agregarUbicacion(ruta.getOrigen());
        agregarUbicacion(ruta.getDestino());
        adyacencia.get(ruta.getOrigen()).add(ruta);
    }

    // Dijkstra (usa "distancia" como peso). Devuelve lista de ubicaciones o null si no hay camino.
    public List<Ubicacion> buscarCaminoDijkstra(Ubicacion origen, Ubicacion destino) {
        if (origen == null || destino == null) return null;
        if (!adyacencia.containsKey(origen) || !adyacencia.containsKey(destino)) return null;

        Map<Ubicacion, Double> dist = new HashMap<>();
        Map<Ubicacion, Ubicacion> prev = new HashMap<>();
        Set<Ubicacion> Q = new HashSet<>(adyacencia.keySet());

        for (Ubicacion u : Q) dist.put(u, Double.MAX_VALUE);
        dist.put(origen, 0.0);

        while (!Q.isEmpty()) {
            Ubicacion u = null; double best = Double.MAX_VALUE;
            for (Ubicacion x : Q) {
                double dx = dist.getOrDefault(x, Double.MAX_VALUE);
                if (dx < best) { best = dx; u = x; }
            }
            if (u == null) break;          // nodos no alcanzables restantes
            Q.remove(u);
            if (u.equals(destino)) break;  // ya tenemos el mejor costo a destino

            for (Ruta r : adyacencia.getOrDefault(u, Collections.emptyList())) {
                Ubicacion v = r.getDestino();
                double nd = dist.get(u) + r.getDistancia();
                if (nd < dist.getOrDefault(v, Double.MAX_VALUE)) {
                    dist.put(v, nd);
                    prev.put(v, u);
                }
            }
        }

        // Reconstrucción
        if (!origen.equals(destino) && !prev.containsKey(destino)) return null;
        List<Ubicacion> camino = new ArrayList<>();
        for (Ubicacion at = destino; at != null; at = prev.get(at)) camino.add(0, at);
        return camino;
    }

    // Vecinos de una ubicación
    public List<Ubicacion> obtenerVecinos(Ubicacion ubicacion) {
        List<Ubicacion> vecinos = new ArrayList<>();
        for (Ruta r : adyacencia.getOrDefault(ubicacion, Collections.emptyList()))
            vecinos.add(r.getDestino());
        return vecinos;
    }

    //Obtencion de RUTAS desde la ubicacion especifica
    public List<Ruta> obtenerRutasDesde(Ubicacion ubicacion) {
        List<Ruta> rutas = new ArrayList<>();
        for (Ubicacion u : adyacencia.keySet()) {
            if (u.equals(ubicacion)) {
                for (Ruta r : adyacencia.get(u)) {
                    rutas.add(r);
                }
            }
        }
        return rutas;
    }

}
