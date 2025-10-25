package co.edu.uniquindio.estructuraDeDatos;

import java.util.List;

public class ColaPrioridadEvacuacion {

    private List<Evacuacion> evacuaciones;

    public ColaPrioridadEvacuacion(List<Evacuacion> evacuaciones) {
        this.evacuaciones = evacuaciones;
    }

    public void insertar(Evacuacion e) {}
    public Evacuacion extraerMayorPrioridad() { return null; }
    public boolean estaVacia() { return true; }
}
