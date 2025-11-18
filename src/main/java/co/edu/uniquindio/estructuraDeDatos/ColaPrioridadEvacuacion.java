package co.edu.uniquindio.estructuraDeDatos;

import java.util.ArrayList;
import java.util.List;

public class ColaPrioridadEvacuacion {

    private Nodo<Evacuacion> primero;
    private Nodo<Evacuacion> ultimo;
    private int size;

    public ColaPrioridadEvacuacion() {
        this.primero = null;
        this.ultimo = null;
        this.size = 0;
    }

    public boolean estaVacia() { return size == 0; }
    public int getSize() { return size; }

    /** Inserta manteniendo ORDEN por prioridad (mayor prioridad al frente). */
    public void insertar(Evacuacion evac) {
        if (evac == null) return;

        Nodo<Evacuacion> nuevo = new Nodo<>(evac);
        int p = evac.getPrioridad();

        // 1) cola vacía
        if (estaVacia()) {
            primero = ultimo = nuevo;
            size = 1;
            return;
        }

        // 2) va al frente si su prioridad es estrictamente MAYOR que la del primero
        if (p > primero.getDato().getPrioridad()) {
            nuevo.setSiguiente(primero);
            primero = nuevo;
            size++;
            return;
        }

        // 3) buscar posición intermedia o final para mantener ORDEN DESCENDENTE
        // avanzamos mientras el siguiente tenga prioridad >= p
        Nodo<Evacuacion> actual = primero;
        while (actual.getSiguiente() != null &&
                actual.getSiguiente().getDato().getPrioridad() >= p) {
            actual = actual.getSiguiente();
        }

        // insertar después de 'actual'
        nuevo.setSiguiente(actual.getSiguiente());
        actual.setSiguiente(nuevo);

        // si quedó al final, actualizamos 'ultimo'
        if (nuevo.getSiguiente() == null) {
            ultimo = nuevo;
        }

        size++;
    }

    /** Extrae el elemento de mayor prioridad (frente de la cola). */
    public Evacuacion extraerMayorPrioridad() {
        if (estaVacia()) return null;
        Evacuacion dato = primero.getDato();
        primero = primero.getSiguiente();
        if (primero == null) ultimo = null;
        size--;
        return dato;
    }

    /** Impresión de apoyo en orden de atención. */
    public void mostrarCola() {
        System.out.println("Cola de evacuaciones (mayor prioridad primero):");
        Nodo<Evacuacion> aux = primero;
        while (aux != null) {
            System.out.println(" - " + aux.getDato());
            aux = aux.getSiguiente();
        }
    }

    /** Devuelve todas las evacuaciones en una lista (en orden de prioridad). */
    public List<Evacuacion> listarTodas() {
        List<Evacuacion> lista = new ArrayList<>();
        Nodo<Evacuacion> aux = primero;
        while (aux != null) {
            lista.add(aux.getDato());
            aux = aux.getSiguiente();
        }
        return lista;
    }

    public Evacuacion buscarPorId(String id) {
        if (id == null) return null;
        Nodo<Evacuacion> aux = primero;
        while (aux != null) {
            Evacuacion e = aux.getDato();
            if (id.equals(e.getIdEvacuacion())) return e;
            aux = aux.getSiguiente();
        }
        return null;
    }

    public void actualizarEstado(String id, EstadoEvacuacion nuevoEstado) {
        if (nuevoEstado == null) return;
        Evacuacion e = buscarPorId(id);
        if (e != null) {
            e.setEstado(nuevoEstado);
        }
    }

    /**
     * Elimina una evacuación por id de la cola.
     * Devuelve true si la encontró y la eliminó, false si no estaba.
     */
    public boolean eliminarPorId(String id) {
        if (id == null || estaVacia()) return false;

        // Caso: está en el primero
        if (id.equals(primero.getDato().getIdEvacuacion())) {
            primero = primero.getSiguiente();
            if (primero == null) {
                ultimo = null;
            }
            size--;
            return true;
        }

        // Buscar en el resto de la lista
        Nodo<Evacuacion> actual = primero;
        while (actual.getSiguiente() != null &&
                !id.equals(actual.getSiguiente().getDato().getIdEvacuacion())) {
            actual = actual.getSiguiente();
        }

        // No lo encontró
        if (actual.getSiguiente() == null) {
            return false;
        }

        // Quitar el nodo siguiente
        Nodo<Evacuacion> nodoAEliminar = actual.getSiguiente();
        actual.setSiguiente(nodoAEliminar.getSiguiente());

        // Si el que borramos era el último, actualizar 'ultimo'
        if (nodoAEliminar == ultimo) {
            ultimo = actual;
        }

        size--;
        return true;
    }
}