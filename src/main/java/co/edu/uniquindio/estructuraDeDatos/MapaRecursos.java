package co.edu.uniquindio.estructuraDeDatos;

import java.time.LocalDate;
import java.util.*;

public class MapaRecursos {

    private final Map<Ubicacion, List<Recurso>> mapa = new HashMap<>();

    public MapaRecursos() {}

    public void agregarRecurso(Ubicacion u, Recurso r) {
        if (u == null || r == null) return;
        mapa.computeIfAbsent(u, k -> new ArrayList<>()).add(r);
        r.asignarUbicacion(u);
    }

    public List<Recurso> obtenerRecursos(Ubicacion u) {
        return mapa.getOrDefault(u, Collections.emptyList());
    }

    public void actualizarCantidad(Ubicacion u, Recurso r, int nuevaCantidad) {
        if (u == null || r == null || nuevaCantidad < 0) return;
        r.setCantidad(nuevaCantidad);
    }

    public boolean tieneRecursos(Ubicacion u) {
        if (u == null) return false;
        List<Recurso> recursos = obtenerRecursos(u);
        return recursos != null && !recursos.isEmpty();
    }

    // === NUEVO: Transferir recurso por id desde 'origen' a 'destino' (total o parcial) ===
    // Devuelve el recurso que queda en destino (puede ser el mismo objeto si movimos todo,
    // o una copia si fue transferencia parcial).
    // En MapaRecursos
    public Recurso transferirRecurso(Ubicacion origen, Ubicacion destino, String idRecurso, int cantidad) {
        if (origen == null || destino == null || idRecurso == null || cantidad <= 0) return null;
        var lista = mapa.get(origen);
        if (lista == null) return null;

        for (int i = 0; i < lista.size(); i++) {
            Recurso r = lista.get(i);
            if (idRecurso.equals(r.getIdRecurso())) {
                if (cantidad >= r.getCantidad()) {
                    // movimiento TOTAL
                    lista.remove(i);
                    r.setUbicacion(destino);
                    mapa.computeIfAbsent(destino, k -> new java.util.ArrayList<>()).add(r);
                    return r; // mismo id
                } else {
                    // movimiento PARCIAL: clonar con nuevo id
                    r.setCantidad(r.getCantidad() - cantidad);
                    Recurso clon = clonarRecursoConCantidadNueva(r, cantidad, destino);
                    mapa.computeIfAbsent(destino, k -> new java.util.ArrayList<>()).add(clon);
                    return clon; // id diferente
                }
            }
        }
        return null;
    }

    private Recurso clonarRecursoConCantidadNueva(Recurso original, int cantidad, Ubicacion destino) {
        String nuevoId = original.getIdRecurso() + "-P" + System.nanoTime();
        if (original instanceof RecursoAlimento ra) {
            return new RecursoAlimento(nuevoId, ra.getNombre(), cantidad, destino, ra.getFechaVencimiento());
        } else if (original instanceof RecursoMedicina rm) {
            return new RecursoMedicina(nuevoId, rm.getNombre(), cantidad, destino, rm.getTipoMedicamento());
        } else {
            // Si aparece otro tipo de recurso en el futuro
            Recurso gen = new Recurso(nuevoId, original.getNombre(), cantidad, destino) {};
            return gen;
        }
    }


    //Metodo para listar todos los recursos existentes

    public List<Recurso> obtenerTodosLosRecursos() {
        List<Recurso> todos = new ArrayList<>();
        for (List<Recurso> lista : mapa.values()) {
            todos.addAll(lista);
        }
        return todos;
    }

}
