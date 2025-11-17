package co.edu.uniquindio.estructuraDeDatos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GrafoTransporte {

    private NodoUbicacion primero;

    // ================== MÉTODOS BÁSICOS DEL GRAFO ==================

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

        // Asegurar que origen y destino están en el grafo
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

    // ================== ELIMINAR UBICACIÓN + RUTAS ==================

    /**
     * Elimina una ubicación del grafo y todas las rutas que la usen
     * como origen o destino.
     */
    public void eliminarUbicacion(Ubicacion u) {
        if (u == null || primero == null) return;

        // 1. Eliminar el nodo de la lista de ubicaciones
        if (primero.ubicacion.equals(u)) {
            primero = primero.siguiente;
        } else {
            NodoUbicacion ant = primero;
            NodoUbicacion act = primero.siguiente;
            while (act != null) {
                if (act.ubicacion.equals(u)) {
                    ant.siguiente = act.siguiente;
                    break;
                }
                ant = act;
                act = act.siguiente;
            }
        }

        // 2. Eliminar todas las rutas que apunten a esa ubicación
        NodoUbicacion aux = primero;
        while (aux != null) {
            NodoRuta rAct = aux.primeraRuta;
            NodoRuta antR = null;

            while (rAct != null) {
                boolean tocaBorrar =
                        rAct.ruta.getOrigen().equals(u) ||
                                rAct.ruta.getDestino().equals(u);

                if (tocaBorrar) {
                    if (antR == null) {
                        aux.primeraRuta = rAct.siguiente;
                    } else {
                        antR.siguiente = rAct.siguiente;
                    }
                    rAct = (antR == null) ? aux.primeraRuta : antR.siguiente;
                } else {
                    antR = rAct;
                    rAct = rAct.siguiente;
                }
            }

            aux = aux.siguiente;
        }
    }

    // ================== DIJKSTRA ==================

    /**
     * Calcula el camino más corto entre origen y destino usando Dijkstra.
     * Devuelve una lista de ubicaciones en orden (origen -> ... -> destino).
     * Si no hay camino, devuelve null.
     */
    public List<Ubicacion> buscarCaminoDijkstra(Ubicacion origen, Ubicacion destino) {

        if (origen == null || destino == null) return null;

        // Tabla de estados
        Map<Ubicacion, EstadoNodo> tabla = new HashMap<>();

        // Inicialización
        NodoUbicacion aux = primero;
        while (aux != null) {
            tabla.put(aux.ubicacion, new EstadoNodo());
            aux = aux.siguiente;
        }

        // Validación
        if (!tabla.containsKey(origen) || !tabla.containsKey(destino))
            return null;

        // Distancia inicial del origen
        tabla.get(origen).distancia = 0;

        // Algoritmo principal
        while (true) {

            // 1. Escoger no visitado con menor distancia
            Ubicacion actual = null;
            double mejor = Double.MAX_VALUE;

            for (Ubicacion u : tabla.keySet()) {
                EstadoNodo e = tabla.get(u);
                if (!e.visitado && e.distancia < mejor) {
                    mejor = e.distancia;
                    actual = u;
                }
            }

            // No quedan alcanzables
            if (actual == null) break;

            tabla.get(actual).visitado = true;

            // Si llegamos al destino
            if (actual.equals(destino)) break;

            // 2. Relajar aristas
            NodoUbicacion nodoActual = buscarNodo(actual);
            if (nodoActual == null) continue;

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

        // No hay camino
        if (tabla.get(destino).distancia == Double.MAX_VALUE)
            return null;

        // Reconstrucción del camino
        List<Ubicacion> camino = new ArrayList<>();
        Ubicacion paso = destino;

        while (paso != null) {
            camino.add(0, paso);
            paso = tabla.get(paso).anterior;
        }

        return camino;
    }

    // Lista de TODAS las rutas del grafo
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

    // Lista de rutas que salen desde una ubicación
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

    // ✅ ESTA ES LA QUE LE FALTABA A WEBServer
    public List<Ubicacion> obtenerTodasLasUbicaciones() {
        List<Ubicacion> lista = new ArrayList<>();

        NodoUbicacion actual = primero;
        while (actual != null) {
            lista.add(actual.ubicacion);
            actual = actual.siguiente;
        }

        return lista;
    }

    // ================== CLASE DE APOYO PARA DIJKSTRA ==================

    /**
     * Clase interna para guardar el estado de cada nodo en Dijkstra.
     */
    private static class EstadoNodo {
        double distancia;
        boolean visitado;
        Ubicacion anterior;

        EstadoNodo() {
            this.distancia = Double.MAX_VALUE;
            this.visitado = false;
            this.anterior = null;
        }
    }
}
