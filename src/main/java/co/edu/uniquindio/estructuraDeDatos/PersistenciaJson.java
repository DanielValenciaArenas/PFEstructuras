package co.edu.uniquindio.estructuraDeDatos;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.time.LocalDate;
import java.util.*;

/**
 * Clase de persistencia para guardar y cargar el estado completo del sistema
 * en un archivo JSON (datos_sistema.json).
 *
 * Guarda:
 *  - Ubicaciones (incluye latitud y longitud)
 *  - Rutas
 *  - Recursos (alimento y medicina con sus campos específicos)
 *  - Equipos de rescate
 *  - Evacuaciones
 *
 * NOTA: Más adelante, si quieres, se puede extender para personas, etc.
 */
public class PersistenciaJson {

    private static final String ARCHIVO = "datos_sistema.json";

    // Gson para convertir objetos <-> JSON
    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    // ===================== DTOs internos para el JSON =====================

    private static class EstadoSistema {
        List<UbicacionJ> ubicaciones = new ArrayList<>();
        List<RutaJ> rutas = new ArrayList<>();
        List<RecursoJ> recursos = new ArrayList<>();
        List<EquipoJ> equipos = new ArrayList<>();
        List<EvacuacionJ> evacuaciones = new ArrayList<>();
    }

    private static class UbicacionJ {
        String id;
        String nombre;
        String tipoZona;
        String nivelAfectacion;
        double latitud;
        double longitud;
    }

    private static class RutaJ {
        String id;
        String origenNombre;
        String destinoNombre;
        double distancia;
    }

    /**
     * tipo:
     *  - "ALIMENTO": extra = fecha vencimiento (yyyy-MM-dd)
     *  - "MEDICINA": extra = tipoMedicamento
     *  - "GEN": extra = null
     */
    private static class RecursoJ {
        String id;
        String nombre;
        int cantidad;
        String nombreUbicacion;
        String tipo;   // ALIMENTO, MEDICINA, GEN
        String extra;  // fecha o tipoMedicamento
    }

    private static class EquipoJ {
        String id;
        String nombre;
        String tipo;
        int miembros;
        String nombreUbicacion;
    }

    private static class EvacuacionJ {
        String id;
        int prioridad;
        int cantidadPersonas;
        String estado;
        String nombreUbicacion;
    }

    // ===================== MÉTODO GUARDAR =====================

    public static void guardar(SistemaGestionDesastres sistema) {
        try (Writer writer = new FileWriter(ARCHIVO)) {

            EstadoSistema estado = new EstadoSistema();

            // ----- UBICACIONES -----
            for (Ubicacion u : sistema.getGrafo().obtenerTodasLasUbicaciones()) {
                UbicacionJ uj = new UbicacionJ();
                uj.id = u.getIdUbicacion();
                uj.nombre = u.getNombre();
                uj.tipoZona = u.getTipoZona().name();
                uj.nivelAfectacion = u.getNivelAfectacion().name();
                uj.latitud = u.getLatitud();
                uj.longitud = u.getLongitud();
                estado.ubicaciones.add(uj);
            }

            // ----- RUTAS -----
            for (Ruta r : sistema.getGrafo().obtenerTodasLasRutas()) {
                RutaJ rj = new RutaJ();
                rj.id = r.getIdRuta();
                rj.origenNombre = r.getOrigen().getNombre();
                rj.destinoNombre = r.getDestino().getNombre();
                rj.distancia = r.getDistancia();
                estado.rutas.add(rj);
            }

            // ----- RECURSOS -----
            for (Recurso r : sistema.getMapaRecursos().obtenerTodosLosRecursos()) {
                RecursoJ rj = new RecursoJ();
                rj.id = r.getIdRecurso();
                rj.nombre = r.getNombre();
                rj.cantidad = r.getCantidad();
                rj.nombreUbicacion = (r.getUbicacion() != null)
                        ? r.getUbicacion().getNombre()
                        : null;

                if (r instanceof RecursoAlimento ra) {
                    rj.tipo = "ALIMENTO";
                    rj.extra = (ra.getFechaVencimiento() != null)
                            ? ra.getFechaVencimiento().toString()
                            : null;
                } else if (r instanceof RecursoMedicina rm) {
                    rj.tipo = "MEDICINA";
                    rj.extra = rm.getTipoMedicamento();
                } else {
                    rj.tipo = "GEN";
                    rj.extra = null;
                }

                estado.recursos.add(rj);
            }

            // ----- EQUIPOS DE RESCATE -----
            // Los equipos están asociados a las ubicaciones
            Set<EquipoRescate> equipos = new HashSet<>();
            for (Ubicacion u : sistema.getGrafo().obtenerTodasLasUbicaciones()) {
                if (u.getEquiposDeRescate() != null) {
                    equipos.addAll(u.getEquiposDeRescate());
                }
            }

            for (EquipoRescate e : equipos) {
                EquipoJ ej = new EquipoJ();
                ej.id = e.getIdEquipo();
                ej.nombre = e.getNombre();
                ej.tipo = e.getTipo();
                ej.miembros = e.getMiembros();
                ej.nombreUbicacion = (e.getUbicacion() != null)
                        ? e.getUbicacion().getNombre()
                        : null;
                estado.equipos.add(ej);
            }

            // ----- EVACUACIONES -----
            for (Evacuacion e : sistema.getColaEvacuaciones().listarTodas()) {
                EvacuacionJ ej = new EvacuacionJ();
                ej.id = e.getIdEvacuacion();
                ej.prioridad = e.getPrioridad();
                ej.cantidadPersonas = e.getCantidadPersonas();
                ej.estado = e.getEstado().name();
                ej.nombreUbicacion = (e.getUbicacion() != null)
                        ? e.getUbicacion().getNombre()
                        : null;
                estado.evacuaciones.add(ej);
            }

            // ----- Escribir JSON al archivo -----
            gson.toJson(estado, writer);
            System.out.println(" Estado del sistema guardado en " + ARCHIVO);

        } catch (IOException e) {
            System.err.println("Error guardando JSON: " + e.getMessage());
        }
    }

    // ===================== MÉTODO CARGAR =====================

    public static void cargar(SistemaGestionDesastres sistema) {
        File f = new File(ARCHIVO);
        if (!f.exists()) {
            System.out.println("ℹ No hay archivo JSON, el sistema iniciará vacío (o con la demo).");
            return;
        }

        try (Reader reader = new FileReader(f)) {
            EstadoSistema estado = gson.fromJson(reader, EstadoSistema.class);
            if (estado == null) {
                System.out.println("ℹ El JSON está vacío.");
                return;
            }

            // Mapa para ubicar ubicaciones por nombre
            Map<String, Ubicacion> ubicPorNombre = new HashMap<>();

            // ----- UBICACIONES -----
            for (UbicacionJ uj : estado.ubicaciones) {
                // Evacuación neutra dummy, luego se usan las reales en cola
                Evacuacion evacDummy = new Evacuacion(
                        "E0", 0, 0, EstadoEvacuacion.PENDIENTE, null
                );

                Ubicacion u = new Ubicacion(
                        uj.id,
                        uj.nombre,
                        TipoZona.valueOf(uj.tipoZona),
                        NivelDeAfectacion.valueOf(uj.nivelAfectacion),
                        evacDummy,
                        new ArrayList<>(),
                        new ArrayList<>(),
                        new ArrayList<>(),
                        uj.latitud,
                        uj.longitud
                );

                sistema.agregarUbicacion(u);
                ubicPorNombre.put(u.getNombre(), u);
            }

            // ----- RUTAS -----
            if (estado.rutas != null) {
                for (RutaJ rj : estado.rutas) {
                    Ubicacion origen = ubicPorNombre.get(rj.origenNombre);
                    Ubicacion destino = ubicPorNombre.get(rj.destinoNombre);
                    if (origen != null && destino != null) {
                        Ruta ruta = new Ruta(rj.id, origen, destino, rj.distancia);
                        sistema.agregarRuta(ruta);
                    }
                }
            }

            // ----- RECURSOS -----
            if (estado.recursos != null) {
                for (RecursoJ rj : estado.recursos) {
                    Ubicacion u = (rj.nombreUbicacion != null)
                            ? ubicPorNombre.get(rj.nombreUbicacion)
                            : null;

                    Recurso recurso;
                    if ("ALIMENTO".equals(rj.tipo)) {
                        LocalDate fecha = (rj.extra != null && !rj.extra.isBlank())
                                ? LocalDate.parse(rj.extra)
                                : null;
                        recurso = new RecursoAlimento(
                                rj.id, rj.nombre, rj.cantidad, u, fecha
                        );
                    } else if ("MEDICINA".equals(rj.tipo)) {
                        String tipoMed = (rj.extra != null) ? rj.extra : "";
                        recurso = new RecursoMedicina(
                                rj.id, rj.nombre, rj.cantidad, u, tipoMed
                        );
                    } else {
                        // Recurso genérico como medicina simple
                        recurso = new RecursoMedicina(
                                rj.id, rj.nombre, rj.cantidad, u, "GEN"
                        );
                    }

                    if (u != null) {
                        sistema.getMapaRecursos().agregarRecurso(u, recurso);
                    }
                }
            }

            // ----- EQUIPOS DE RESCATE -----
            if (estado.equipos != null) {
                for (EquipoJ ej : estado.equipos) {
                    Ubicacion u = (ej.nombreUbicacion != null)
                            ? ubicPorNombre.get(ej.nombreUbicacion)
                            : null;

                    EquipoRescate eq = new EquipoRescate(
                            ej.id, ej.tipo, ej.miembros, u
                    );
                    eq.setNombre(ej.nombre);

                    if (u != null) {
                        u.asignarEquipo(eq);
                    }
                }
            }

            // ----- EVACUACIONES -----
            if (estado.evacuaciones != null) {
                for (EvacuacionJ ej : estado.evacuaciones) {
                    Ubicacion u = (ej.nombreUbicacion != null)
                            ? ubicPorNombre.get(ej.nombreUbicacion)
                            : null;

                    Evacuacion evac = new Evacuacion(
                            ej.id,
                            ej.prioridad,
                            ej.cantidadPersonas,
                            EstadoEvacuacion.valueOf(ej.estado),
                            u
                    );
                    sistema.registrarEvacuacion(evac);
                }
            }

            System.out.println(" Datos cargados desde JSON " + ARCHIVO);

        } catch (Exception e) {
            System.err.println("Error cargando JSON: " + e.getMessage());
        }
    }
}
