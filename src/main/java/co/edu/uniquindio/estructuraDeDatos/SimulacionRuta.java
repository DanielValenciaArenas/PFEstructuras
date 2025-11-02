package co.edu.uniquindio.estructuraDeDatos;

import java.util.List;

public class SimulacionRuta {
    private String idSimulacion;
    private double distanciaTotal;
    private List<Ruta> rutas;
    private List<Ubicacion> ubicaciones;

    public SimulacionRuta(String idSimulacion, double tiempoTotal, List<Ruta> rutas, List<Ubicacion> ubicaciones) {
        this.idSimulacion = idSimulacion;
        this.distanciaTotal = tiempoTotal;
        this.rutas = rutas;
        this.ubicaciones = ubicaciones;
    }

    //Getters y Setters
    public String getIdSimulacion() { return idSimulacion; }
    public void setIdSimulacion(String idSimulacion) { this.idSimulacion = idSimulacion; }
    public double getDistanciaTotal() { return distanciaTotal; }
    public void setDistanciaTotal(double distanciaTotal) { this.distanciaTotal = distanciaTotal; }
    public List<Ruta> getRutas() { return rutas; }
    public void setRutas(List<Ruta> rutas) { this.rutas = rutas; }
    public List<Ubicacion> getUbicaciones() { return ubicaciones; }
    public void setUbicaciones(List<Ubicacion> ubicaciones) { this.ubicaciones = ubicaciones; }

    // Ejecutar la simulación de la ruta
    public void ejecutarSimulacion(GrafoTransporte grafo, Ubicacion origen, Ubicacion destino) {
        System.out.println("Iniciando simulación...");

        List<Ubicacion> camino = grafo.buscarCaminoDijkstra(origen, destino);

        if (camino == null) {
            System.out.println("No existe ruta entre las ubicaciones seleccionadas.");
            return;
        }

        // Calculo del tiempo total
        double distanciaTotal = 0;
        for (int i = 0; i < camino.size() - 1; i++) {
            Ubicacion actual = camino.get(i);
            Ubicacion siguiente = camino.get(i + 1);
            for (Ruta r : grafo.obtenerRutasDesde(actual)) {
                if (r.getDestino().equals(siguiente)) {
                    distanciaTotal += r.getDistancia();
                    break;
                }
            }
        }

        this.ubicaciones = camino;
        this.distanciaTotal = distanciaTotal;

        mostrarResultado();
    }

    // Mostrar resultado de la simulación
    public void mostrarResultado() {
        System.out.println("Resultado de la simulación " + idSimulacion);
        System.out.println("Ruta más corta:");
        for (Ubicacion u : ubicaciones) {
            System.out.print(u.getNombre() + " -> ");
        }
        System.out.println("\nDistancia total es de: " + distanciaTotal + " km");
    }

}
