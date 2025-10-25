package co.edu.uniquindio.estructuraDeDatos;

import java.util.List;

public class SimulacionRuta {
    private String idSimulacion;
    private double TiempoTotal;
    private List<Ruta> rutas;
    private List<Ubicacion> ubicaciones;

    public SimulacionRuta(String idSimulacion, double tiempoTotal, List<Ruta> rutas, List<Ubicacion> ubicaciones) {
        this.idSimulacion = idSimulacion;
        this.TiempoTotal = tiempoTotal;
        this.rutas = rutas;
        this.ubicaciones = ubicaciones;
    }

    //Getters y Setters
    public String getIdSimulacion() { return idSimulacion; }
    public void setIdSimulacion(String idSimulacion) { this.idSimulacion = idSimulacion; }
    public double getTiempoTotal() { return TiempoTotal; }
    public void setTiempoTotal(double tiempoTotal) { TiempoTotal = tiempoTotal; }
    public List<Ruta> getRutas() { return rutas; }
    public void setRutas(List<Ruta> rutas) { this.rutas = rutas; }
    public List<Ubicacion> getUbicaciones() { return ubicaciones; }
    public void setUbicaciones(List<Ubicacion> ubicaciones) { this.ubicaciones = ubicaciones; }


    public void ejecutarSimulacion(GrafoTransporte g){}
    public void mostrarResultado(){}

}
