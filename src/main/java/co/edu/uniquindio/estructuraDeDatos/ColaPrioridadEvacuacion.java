package co.edu.uniquindio.estructuraDeDatos;

import java.util.ArrayList;
import java.util.List;

public class ColaPrioridadEvacuacion {

    private final List<Evacuacion> evacuaciones;

    public ColaPrioridadEvacuacion() {
        evacuaciones = new ArrayList<>();
    }

    // Insertar manteniendo orden (mayor prioridad primero)
    public void insertar(Evacuacion evacuacion) {
        int i = 0;
        while (i < evacuaciones.size() && evacuacion.compareTo(evacuaciones.get(i)) > 0) {
            i++;
        }
        evacuaciones.add(i, evacuacion);
    }

    // Extraer la evacuación de mayor prioridad (posición 0)
    public Evacuacion extraerMayorPrioridad() {
        if (evacuaciones.isEmpty()) return null;
        return evacuaciones.remove(0);
    }

    public boolean estaVacia() { return evacuaciones.isEmpty(); }

    public void mostrarCola() {
        System.out.println("Cola de evacuaciones:");
        for (Evacuacion e : evacuaciones) System.out.println(e);
    }
}
