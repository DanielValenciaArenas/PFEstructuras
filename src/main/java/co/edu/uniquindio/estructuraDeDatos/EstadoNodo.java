package co.edu.uniquindio.estructuraDeDatos;

public class EstadoNodo {
    double distancia;
    boolean visitado;
    Ubicacion anterior;

    public EstadoNodo() {
        distancia = Double.MAX_VALUE;
        visitado = false;
        anterior = null;
    }
}
