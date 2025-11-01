package co.edu.uniquindio.estructuraDeDatos;

public class NodoDistribucion {
    private Recurso recurso;
    private Ubicacion ubicacion;
    private NodoDistribucion izquierdo;
    private NodoDistribucion derecho;

    public NodoDistribucion(Recurso recurso, Ubicacion ubicacion) {
        this.recurso = recurso;
        this.ubicacion = ubicacion;
        this.izquierdo = null;
        this.derecho = null;
    }

    // Getters y Setters
    public Recurso getRecurso() {return recurso;}
    public void setRecurso(Recurso recurso) {this.recurso = recurso;}
    public Ubicacion getUbicacion() {return ubicacion;}
    public void setUbicacion(Ubicacion ubicacion) {this.ubicacion = ubicacion;}
    public NodoDistribucion getIzquierdo() {return izquierdo;}
    public void setIzquierdo(NodoDistribucion izquierdo) {this.izquierdo = izquierdo;}
    public NodoDistribucion getDerecho() {return derecho;}
    public void setDerecho(NodoDistribucion derecho) {this.derecho = derecho;}




}
