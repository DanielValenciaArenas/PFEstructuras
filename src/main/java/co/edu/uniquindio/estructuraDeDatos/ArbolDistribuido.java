package co.edu.uniquindio.estructuraDeDatos;

public class ArbolDistribuido {
    private NodoDistribucion raiz;

    public ArbolDistribuido() {
        this.raiz = null;
    }

    // === Metodo: INSERTAR ===
    public void insertar(Ubicacion ubicacion, Recurso recurso) {
        raiz = insertarRecursivo(raiz, ubicacion, recurso);
        ubicacion.agregarRecurso(recurso); // refleja en la ubicación
    }

    private NodoDistribucion insertarRecursivo(NodoDistribucion nodo, Ubicacion ubicacion, Recurso recurso) {
        if (nodo == null) {
            return new NodoDistribucion(recurso, ubicacion);
        }

        int comparacion = recurso.getIdRecurso().compareTo(nodo.getRecurso().getIdRecurso());

        if (comparacion < 0) {
            nodo.setIzquierdo(insertarRecursivo(nodo.getIzquierdo(), ubicacion, recurso));
        } else if (comparacion > 0) {
            nodo.setDerecho(insertarRecursivo(nodo.getDerecho(), ubicacion, recurso));
        } else {
            System.out.println("⚠️ El recurso con ID " + recurso.getIdRecurso() + " ya está asignado.");
        }

        return nodo;
    }

    // === Metodo: BUSCAR ===
    public NodoDistribucion buscar(String idRecurso) {
        return buscarRecursivo(raiz, idRecurso);
    }

    private NodoDistribucion buscarRecursivo(NodoDistribucion nodo, String idRecurso) {
        if (nodo == null) {
            return null;
        }

        int comparacion = idRecurso.compareTo(nodo.getRecurso().getIdRecurso());

        if (comparacion == 0) {
            return nodo;
        } else if (comparacion < 0) {
            return buscarRecursivo(nodo.getIzquierdo(), idRecurso);
        } else {
            return buscarRecursivo(nodo.getDerecho(), idRecurso);
        }
    }

    // === Metodo: ELIMINAR ===
    public void eliminar(String idRecurso) {
        raiz = eliminarRecursivo(raiz, idRecurso);
    }

    private NodoDistribucion eliminarRecursivo(NodoDistribucion nodo, String idRecurso) {
        if (nodo == null) {
            System.out.println("No se encontró el recurso con ID: " + idRecurso);
            return null;
        }

        int comparacion = idRecurso.compareTo(nodo.getRecurso().getIdRecurso());

        if (comparacion < 0) {
            nodo.setIzquierdo(eliminarRecursivo(nodo.getIzquierdo(), idRecurso));
        } else if (comparacion > 0) {
            nodo.setDerecho(eliminarRecursivo(nodo.getDerecho(), idRecurso));
        } else {
            // Caso 1: sin hijos
            if (nodo.getIzquierdo() == null && nodo.getDerecho() == null) {
                return null;
            }
            // Caso 2: un hijo
            else if (nodo.getIzquierdo() == null) {
                return nodo.getDerecho();
            } else if (nodo.getDerecho() == null) {
                return nodo.getIzquierdo();
            }
            // Caso 3: dos hijos
            else {
                NodoDistribucion sucesor = encontrarMenor(nodo.getDerecho());
                nodo.setRecurso(sucesor.getRecurso());
                nodo.setUbicacion(sucesor.getUbicacion());
                nodo.setDerecho(eliminarRecursivo(nodo.getDerecho(), sucesor.getRecurso().getIdRecurso()));
            }
        }

        return nodo;
    }

    private NodoDistribucion encontrarMenor(NodoDistribucion nodo) {
        while (nodo.getIzquierdo() != null) {
            nodo = nodo.getIzquierdo();
        }
        return nodo;
    }

    // === RECORRIDO / VISUALIZACIÓN ===
    public void mostrarDistribucion() {
        System.out.println("\n Distribución de los recursos en las zonas afectadas:");
        recorrerInorden(raiz);
    }

    private void recorrerInorden(NodoDistribucion nodo) {
        if (nodo != null) {
            recorrerInorden(nodo.getIzquierdo());
            System.out.println(" - Recurso: " + nodo.getRecurso().getNombre() +
                    " (ID: " + nodo.getRecurso().getIdRecurso() + ")" +
                    " → Ubicación: " + nodo.getUbicacion().getNombre());
            recorrerInorden(nodo.getDerecho());
        }
    }
}
