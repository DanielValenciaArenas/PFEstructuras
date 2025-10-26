package co.edu.uniquindio.estructuraDeDatos;

public class Ruta {
    private String idRuta;
    private double distancia;
    private Ubicacion origen;
    private Ubicacion destino;

    public Ruta(String idRuta, double distancia, double tiempo, Ubicacion origen, Ubicacion destino) {
        this.idRuta = idRuta;
        this.distancia = distancia;
        this.origen = origen;
        this.destino = destino;
    }

    //Getters y Setters
    public String getIdRuta() { return idRuta; }
    public void setIdRuta(String idRuta) { this.idRuta = idRuta; }
    public double getDistancia() { return distancia; }
    public void setDistancia(double distancia) { this.distancia = distancia; }
    public Ubicacion getOrigen() { return origen; }
    public void setOrigen(Ubicacion origen) { this.origen = origen; }
    public Ubicacion getDestino() { return destino; }
    public void setDestino(Ubicacion destino) { this.destino = destino; }

}
