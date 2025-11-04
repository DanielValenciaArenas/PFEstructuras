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
    public Recurso transferirRecurso(Ubicacion origen, Ubicacion destino, String idRecurso, int cantidad) {
        if (origen == null || destino == null || idRecurso == null || idRecurso.isEmpty() || cantidad <= 0) return null;

        List<Recurso> listaOrigen = mapa.get(origen);
        if (listaOrigen == null) return null;

        Recurso r = null;
        for (Recurso x : listaOrigen) {
            if (idRecurso.equals(x.getIdRecurso())) { r = x; break; }
        }
        if (r == null) return null;
        if (cantidad > r.getCantidad()) return null;

        // Transferencia total -> mover el mismo objeto
        if (cantidad == r.getCantidad()) {
            listaOrigen.remove(r);
            agregarRecurso(destino, r); // setea ubicacion
            return r;
        }

        // Transferencia parcial -> disminuir en origen y crear copia en destino
        r.setCantidad(r.getCantidad() - cantidad);

        Recurso copia = clonarParcial(r, destino, cantidad);
        if (copia == null) return null;

        agregarRecurso(destino, copia);
        return copia;
    }

    // Clona parcialmente el recurso respetando su subtipo y atributos relevantes.
    private Recurso clonarParcial(Recurso original, Ubicacion destino, int cantidad) {
        String nuevoId = original.getIdRecurso() + "-MOV" + System.nanoTime();
        if (original instanceof RecursoAlimento) {
            RecursoAlimento ra = (RecursoAlimento) original;
            LocalDate fv = ra.getFechaVencimiento();
            return new RecursoAlimento(nuevoId, original.getNombre(), cantidad, destino, fv);
        } else if (original instanceof RecursoMedicina) {
            RecursoMedicina rm = (RecursoMedicina) original;
            String tipo = rm.getTipoMedicamento();
            return new RecursoMedicina(nuevoId, original.getNombre(), cantidad, destino, tipo);
        } else {
            // Si en el futuro hay más subtipos, agregarlos aquí:
            return null;
        }
    }
}
