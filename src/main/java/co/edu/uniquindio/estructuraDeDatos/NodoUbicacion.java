package co.edu.uniquindio.estructuraDeDatos;

public class NodoUbicacion {
    Ubicacion ubicacion;
    NodoUbicacion siguiente;
    NodoRuta primeraRuta;

    NodoUbicacion(Ubicacion ubicacion) {
        this.ubicacion = ubicacion;
    }
}
