package co.edu.uniquindio.estructuraDeDatos;

public class NodoDistribucion {
    private Ubicacion ubicacion;
    private Recurso recurso;
    private NodoDistribucion izquierdo;
    private NodoDistribucion derecho;

    public NodoDistribucion(Ubicacion ubicacion, Recurso recurso) {
        this.ubicacion = ubicacion;
        this.recurso = recurso;
        this.izquierdo = null;
        this.derecho = null;
    }

    // Getters y Setters
    public Ubicacion getUbicacion() { return ubicacion; }
    public void setUbicacion(Ubicacion ubicacion) { this.ubicacion = ubicacion; }

    public Recurso getRecurso() { return recurso; }
    public void setRecurso(Recurso recurso) { this.recurso = recurso; }

    public NodoDistribucion getIzquierdo() { return izquierdo; }
    public void setIzquierdo(NodoDistribucion izquierdo) { this.izquierdo = izquierdo; }

    public NodoDistribucion getDerecho() { return derecho; }
    public void setDerecho(NodoDistribucion derecho) { this.derecho = derecho; }
}

