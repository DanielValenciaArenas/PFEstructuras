package co.edu.uniquindio.estructuraDeDatos;

import java.util.*;

public class GrafoTransporte {
    private Map<Ubicacion, List<Ruta>> adyacencia;

    public GrafoTransporte() {
        adyacencia = new HashMap<>();
    }

    // Agregar una ubicación (nodo)
    public void agregarUbicacion(Ubicacion ubicacion) {
        if (!adyacencia.containsKey(ubicacion)) {
            adyacencia.put(ubicacion, new ArrayList<>());
        }
    }

    // Agregar ruta (arista dirigida)
    public void agregarRuta(Ruta ruta) {
        adyacencia.get(ruta.getOrigen()).add(ruta);
    }

    // Buscar camino más corto (DIJKSTRA)
    public List<Ubicacion> buscarCaminoDijkstra(Ubicacion origen, Ubicacion destino) {

        Map<Ubicacion, Double> distancias = new HashMap<>();
        Map<Ubicacion, Ubicacion> nodosCamino = new HashMap<>();
        Set<Ubicacion> noVisitados = new HashSet<>(adyacencia.keySet());

        for (Ubicacion ubicacion : adyacencia.keySet()) {
            distancias.put(ubicacion, Double.MAX_VALUE);
        }
        distancias.put(origen, 0.0);

        while (!noVisitados.isEmpty()) {

            Ubicacion actual = null;

            double minDistancia = Double.MAX_VALUE;
            for (Ubicacion ubicacion : noVisitados) {
                if (distancias.get(ubicacion) < minDistancia) {
                    minDistancia = distancias.get(ubicacion);
                    actual = ubicacion;
                }
            }

            if (actual == null) break;
            noVisitados.remove(actual);

            for (Ruta ruta : adyacencia.get(actual)) {
                Ubicacion vecino = ruta.getDestino();
                double nuevaDistancia = distancias.get(actual) + ruta.getDistancia();
                if (nuevaDistancia < distancias.get(vecino)) {
                    distancias.put(vecino, nuevaDistancia);
                    nodosCamino.put(vecino, actual);
                }
            }
        }

        List<Ubicacion> camino = new ArrayList<>();
        Ubicacion paso = destino;
        while (paso != null) {
            camino.add(0, paso);
            paso = nodosCamino.get(paso);
        }

        if (camino.get(0).equals(origen)) return camino;
        else return null;
    }

    // Obtener vecinos de una ubicación
    public List<Ubicacion> obtenerVecinos(Ubicacion ubicacion) {
        List<Ubicacion> vecinos = new ArrayList<>();
        if (adyacencia.containsKey(ubicacion)) {
            for (Ruta ruta : adyacencia.get(ubicacion)) {
                vecinos.add(ruta.getDestino());
            }
        }
        return vecinos;
    }

}
