package co.edu.uniquindio.estructuraDeDatos;

public class Ruta {
    private String idRuta;
    private double distancia;
    private Ubicacion origen;
    private Ubicacion destino;

    // Constructor coherente con los campos (origen, destino, distancia)
    public Ruta(String idRuta, Ubicacion origen, Ubicacion destino, double distancia) {
        this.idRuta = idRuta;
        this.origen = origen;
        this.destino = destino;
        this.distancia = distancia;
    }

    // Getters y Setters
    public String getIdRuta() { return idRuta; }
    public void setIdRuta(String idRuta) { this.idRuta = idRuta; }
    public double getDistancia() { return distancia; }
    public void setDistancia(double distancia) { this.distancia = distancia; }
    public Ubicacion getOrigen() { return origen; }
    public void setOrigen(Ubicacion origen) { this.origen = origen; }
    public Ubicacion getDestino() { return destino; }
    public void setDestino(Ubicacion destino) { this.destino = destino; }
}
