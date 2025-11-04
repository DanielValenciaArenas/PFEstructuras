package co.edu.uniquindio.estructuraDeDatos;

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

        // 1) cola vacía
        if (estaVacia()) {
            primero = ultimo = nuevo;
            size = 1;
            return;
        }

        // 2) va al frente (si su prioridad es estrictamente mayor que la del primero)
        if (evac.compareTo(primero.getDato()) > 0) {
            nuevo.setSiguiente(primero);
            primero = nuevo;
            size++;
            return;
        }

        // 3) buscar posición intermedia o final (<= siguiente)
        Nodo<Evacuacion> actual = primero;
        while (actual.getSiguiente() != null &&
                evac.compareTo(actual.getSiguiente().getDato()) <= 0) {
            actual = actual.getSiguiente();
        }

        // insertar después de 'actual'
        nuevo.setSiguiente(actual.getSiguiente());
        actual.setSiguiente(nuevo);
        if (nuevo.getSiguiente() == null) {
            // se insertó al final
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
}
