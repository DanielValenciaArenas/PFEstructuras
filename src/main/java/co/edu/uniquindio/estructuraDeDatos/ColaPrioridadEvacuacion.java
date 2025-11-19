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

    // Insertar manteniendo ORDEN por prioridad (mayor prioridad primero)
    public void insertar(Evacuacion evac) {
        if (evac == null) return;

        Nodo<Evacuacion> nuevo = new Nodo<>(evac);
        int p = evac.getPrioridad();

        if (estaVacia()) {
            primero = ultimo = nuevo;
            size = 1;
            return;
        }

        if (p > primero.getDato().getPrioridad()) {
            nuevo.setSiguiente(primero);
            primero = nuevo;
            size++;
            return;
        }

        Nodo<Evacuacion> actual = primero;
        while (actual.getSiguiente() != null &&
                actual.getSiguiente().getDato().getPrioridad() >= p) {
            actual = actual.getSiguiente();
        }

        nuevo.setSiguiente(actual.getSiguiente());
        actual.setSiguiente(nuevo);

        if (nuevo.getSiguiente() == null) {
            ultimo = nuevo;
        }

        size++;
    }

    // Extraer el elemento de mayor prioridad
    public Evacuacion extraerMayorPrioridad() {
        if (estaVacia()) return null;
        Evacuacion dato = primero.getDato();
        primero = primero.getSiguiente();
        if (primero == null) ultimo = null;
        size--;
        return dato;
    }

    // Impresión de apoyo en orden de atención
    public void mostrarCola() {
        System.out.println("Cola de evacuaciones (mayor prioridad primero):");
        Nodo<Evacuacion> aux = primero;
        while (aux != null) {
            System.out.println(" - " + aux.getDato());
            aux = aux.getSiguiente();
        }
    }

    // Devolver todas las evacuaciones en una lista (en orden de prioridad
    public List<Evacuacion> listarTodas() {
        List<Evacuacion> lista = new ArrayList<>();
        Nodo<Evacuacion> aux = primero;
        while (aux != null) {
            lista.add(aux.getDato());
            aux = aux.getSiguiente();
        }
        return lista;
    }

    // Buscar evacuaciones segun ID
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

    // Actualizar estado de las evacuaciones
    public void actualizarEstado(String id, EstadoEvacuacion nuevoEstado) {
        if (nuevoEstado == null) return;
        Evacuacion e = buscarPorId(id);
        if (e != null) {
            e.setEstado(nuevoEstado);
        }
    }

    // Eliminar una evacuacion por ID de la cola
    public boolean eliminarPorId(String id) {
        if (id == null || estaVacia()) return false;

        if (id.equals(primero.getDato().getIdEvacuacion())) {
            primero = primero.getSiguiente();
            if (primero == null) {
                ultimo = null;
            }
            size--;
            return true;
        }

        Nodo<Evacuacion> actual = primero;
        while (actual.getSiguiente() != null &&
                !id.equals(actual.getSiguiente().getDato().getIdEvacuacion())) {
            actual = actual.getSiguiente();
        }

        if (actual.getSiguiente() == null) {
            return false;
        }

        Nodo<Evacuacion> nodoAEliminar = actual.getSiguiente();
        actual.setSiguiente(nodoAEliminar.getSiguiente());

        if (nodoAEliminar == ultimo) {
            ultimo = actual;
        }

        size--;
        return true;
    }
}