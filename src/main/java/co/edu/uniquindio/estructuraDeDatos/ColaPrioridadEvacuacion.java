package co.edu.uniquindio.estructuraDeDatos;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class ColaPrioridadEvacuacion {

    private List<Evacuacion> evacuaciones;

    public ColaPrioridadEvacuacion() {
        evacuaciones = new ArrayList<>();
    }

    // Insertar con orden de prioridad
    public void insertar(Evacuacion evacuacion) {
        int i = 0;
        while (i < evacuaciones.size() && evacuacion.compareTo(evacuaciones.get(i)) < 0) {
            i++;
        }
        evacuaciones.add(i, evacuacion);
    }

    // Extraer la evacuación de mayor prioridad (la primera)
    public Evacuacion extraerMayorPrioridad() {
        if (evacuaciones.isEmpty()) return null;
        return evacuaciones.remove(0);
    }


    // Mostrar la COla de evacuaciones
    public void mostrarCola() {
        System.out.println("Cola de evacuaciones:");
        for (Evacuacion e : evacuaciones) {
            System.out.println(e);
        }
    }
}
