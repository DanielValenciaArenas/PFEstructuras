package co.edu.uniquindio.estructuraDeDatos;

public class ArbolDistribuido {
    private NodoDistribucion raiz;

    public ArbolDistribuido() {
        this.raiz = null;
    }

    // === INSERTAR ZONA (si no existe) y asignar recurso ===
    public void insertar(Ubicacion ubicacion, Recurso recurso) {
        raiz = insertarRecursivo(raiz, ubicacion, recurso);
    }

    private NodoDistribucion insertarRecursivo(NodoDistribucion nodo, Ubicacion ubicacion, Recurso recurso) {
        if (nodo == null) {
            ubicacion.agregarRecurso(recurso);
            return new NodoDistribucion(recurso, ubicacion);
        }

        int comparacion = ubicacion.getIdUbicacion().compareTo(nodo.getUbicacion().getIdUbicacion());

        if (comparacion < 0) {
            nodo.setIzquierdo(insertarRecursivo(nodo.getIzquierdo(), ubicacion, recurso));
        } else if (comparacion > 0) {
            nodo.setDerecho(insertarRecursivo(nodo.getDerecho(), ubicacion, recurso));
        } else {
            nodo.getUbicacion().agregarRecurso(recurso);
        }
        return nodo;
    }

    // === BUSCAR UBICACIÓN (por ID de zona) ===
    public NodoDistribucion buscarZona(String idUbicacion) {
        return buscarRecursivo(raiz, idUbicacion);
    }

    private NodoDistribucion buscarRecursivo(NodoDistribucion nodo, String idUbicacion) {
        if (nodo == null) return null;

        int cmp = idUbicacion.compareTo(nodo.getUbicacion().getIdUbicacion());

        if (cmp == 0) return nodo;
        if (cmp < 0) return buscarRecursivo(nodo.getIzquierdo(), idUbicacion);
        return buscarRecursivo(nodo.getDerecho(), idUbicacion);
    }

    // === ELIMINAR ===
    public void eliminarZona(String idRecurso) {
        raiz = eliminarRecursivo(raiz, idRecurso);
    }

    private NodoDistribucion eliminarRecursivo(NodoDistribucion nodo, String idRecurso) {
        if (nodo == null) {
            System.out.println("No se encontró el recurso con ID: " + idRecurso);
            return null;
        }

        int cmp = idRecurso.compareTo(nodo.getRecurso().getIdRecurso());

        if (cmp < 0) {
            nodo.setIzquierdo(eliminarRecursivo(nodo.getIzquierdo(), idRecurso));
        } else if (cmp > 0) {
            nodo.setDerecho(eliminarRecursivo(nodo.getDerecho(), idRecurso));
        } else {
            // === CASO 1: sin hijos ===
            if (nodo.getIzquierdo() == null && nodo.getDerecho() == null) {
                return null;
            }

            // === CASO 2: un solo hijo ===
            else if (nodo.getIzquierdo() == null) {
                return nodo.getDerecho();
            } else if (nodo.getDerecho() == null) {
                return nodo.getIzquierdo();
            }

            // === CASO 3: dos hijos ===
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


    // === MOVER RECURSO ===
    public void moverRecurso(String idRecurso, Ubicacion nuevaUbicacion) {
        if (idRecurso == null || idRecurso.isEmpty() || nuevaUbicacion == null) {
            System.out.println("Datos inválidos para mover el recurso");
            return;
        }

        NodoDistribucion nodo = buscarRecurso(idRecurso);
        if (nodo == null) {
            System.out.println("No se encontró el recurso con ID: " + idRecurso);
            return;
        }

        // Eliminar el recurso de la ubicación actual
        nodo.getUbicacion().eliminarRecurso(idRecurso);

        // Cambiar la ubicación
        nodo.setUbicacion(nuevaUbicacion);

        // Agregar el recurso a la nueva ubicación
        nuevaUbicacion.agregarRecurso(nodo.getRecurso());

        System.out.println("Recurso " + nodo.getRecurso().getNombre() +
                " movido a la ubicación " + nuevaUbicacion.getNombre());
    }

    public NodoDistribucion buscarRecurso(String idRecurso) {
        return buscarRecursoRecursivo(raiz, idRecurso);
    }

    private NodoDistribucion buscarRecursoRecursivo(NodoDistribucion nodo, String idRecurso) {
        if (nodo == null) return null;

        if (nodo.getRecurso().getIdRecurso().equals(idRecurso)) {
            return nodo;
        }

        NodoDistribucion izq = buscarRecursoRecursivo(nodo.getIzquierdo(), idRecurso);
        if (izq != null) return izq;

        return buscarRecursoRecursivo(nodo.getDerecho(), idRecurso);
    }

    // === VISUALIZACIÓN ===
    public void mostrarDistribucion() {
        System.out.println("\nDistribución de recursos por zonas:");
        recorrerInorden(raiz);
    }

    private void recorrerInorden(NodoDistribucion nodo) {
        if (nodo != null) {
            recorrerInorden(nodo.getIzquierdo());

            System.out.println("Zona: " + nodo.getUbicacion().getNombre() +
                    " (" + nodo.getUbicacion().getNivelAfectacion() + ")");
            for (Recurso r : nodo.getUbicacion().getRecursos()) {
                System.out.println("   → Recurso: " + r.getNombre() +
                        " (ID: " + r.getIdRecurso() + ", cantidad=" + r.getCantidad() + ")");
            }

            recorrerInorden(nodo.getDerecho());
        }
    }
}
