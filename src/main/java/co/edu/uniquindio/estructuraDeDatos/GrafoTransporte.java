package co.edu.uniquindio.estructuraDeDatos;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GrafoTransporte {

    private NodoUbicacion primero;

    // Agregar una ubicación (nodo) al grafo
    public void agregarUbicacion(Ubicacion u) {
        if (u == null || buscarNodo(u) != null) return;
        NodoUbicacion nuevo = new NodoUbicacion(u);
        nuevo.siguiente = primero;
        primero = nuevo;
    }

    // Agregar una ruta (arista) al grafo
    public void agregarRuta(Ruta r) {
        if (r == null) return;

        agregarUbicacion(r.getOrigen());
        agregarUbicacion(r.getDestino());

        NodoUbicacion nodoOrigen = buscarNodo(r.getOrigen());
        if (nodoOrigen == null) return;

        NodoRuta nueva = new NodoRuta(r);
        nueva.siguiente = nodoOrigen.primeraRuta;
        nodoOrigen.primeraRuta = nueva;
    }

    // Buscar un nodo de ubicación dentro del grafo
    private NodoUbicacion buscarNodo(Ubicacion u) {
        NodoUbicacion aux = primero;
        while (aux != null) {
            if (aux.ubicacion.equals(u)) return aux;
            aux = aux.siguiente;
        }
        return null;
    }

    public List<Ubicacion> buscarCaminoDijkstra(Ubicacion origen, Ubicacion destino) {

        if (origen == null || destino == null) return null;

        // Tabla de estados (NO toca Ubicacion)
        Map<Ubicacion, EstadoNodo> tabla = new HashMap<>();

        // Inicialización
        NodoUbicacion aux = primero;
        while (aux != null) {
            tabla.put(aux.ubicacion, new EstadoNodo());
            aux = aux.siguiente;
        }

        if (!tabla.containsKey(origen) || !tabla.containsKey(destino))
            return null;

        // Distancia inicial a 0
        tabla.get(origen).distancia = 0;

        // Algoritmo principal
        while (true) {
            // 1. Escoger el no visitado con menor distancia
            Ubicacion actual = null;
            double mejor = Double.MAX_VALUE;

            for (Ubicacion u : tabla.keySet()) {
                EstadoNodo e = tabla.get(u);
                if (!e.visitado && e.distancia < mejor) {
                    mejor = e.distancia;
                    actual = u;
                }
            }

            // No alcanzable
            if (actual == null) break;

            // Marcar como visitado
            tabla.get(actual).visitado = true;

            // Si llegamos al destino -> terminar
            if (actual.equals(destino)) break;

            // Obtener sus vecinos
            NodoUbicacion nodoActual = buscarNodo(actual);
            NodoRuta rutaAux = nodoActual.primeraRuta;

            while (rutaAux != null) {

                Ubicacion vecino = rutaAux.ruta.getDestino();
                double peso = rutaAux.ruta.getDistancia();

                EstadoNodo eActual = tabla.get(actual);
                EstadoNodo eVecino = tabla.get(vecino);

                double nuevo = eActual.distancia + peso;

                if (nuevo < eVecino.distancia) {
                    eVecino.distancia = nuevo;
                    eVecino.anterior = actual;
                }

                rutaAux = rutaAux.siguiente;
            }
        }

        // Reconstrucción del camino
        if (tabla.get(destino).distancia == Double.MAX_VALUE)
            return null; // NO HAY CAMINO

        List<Ubicacion> camino = new ArrayList<>();
        Ubicacion paso = destino;

        while (paso != null) {
            camino.add(0, paso);
            paso = tabla.get(paso).anterior;
        }

        return camino;
    }


    public List<Ruta> obtenerTodasLasRutas() {
        List<Ruta> todas = new ArrayList<>();

        NodoUbicacion actual = primero;
        while (actual != null) {
            NodoRuta rutaActual = actual.primeraRuta;

            while (rutaActual != null) {
                todas.add(rutaActual.ruta); // agregamos la Ruta real
                rutaActual = rutaActual.siguiente;
            }

            actual = actual.siguiente;
        }

        return todas;
    }


    public List<Ruta> obtenerRutasDesde(Ubicacion ubicacion) {
        List<Ruta> rutas = new ArrayList<>();

        NodoUbicacion actual = primero;

        // Buscar el nodo de esa ubicación
        while (actual != null) {
            if (actual.ubicacion.equals(ubicacion)) {

                NodoRuta rutaActual = actual.primeraRuta;

                while (rutaActual != null) {
                    rutas.add(rutaActual.ruta);
                    rutaActual = rutaActual.siguiente;
                }

                break; // ya lo encontramos
            }
            actual = actual.siguiente;
        }

        return rutas;
    }


}
