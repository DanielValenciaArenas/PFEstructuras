package co.edu.uniquindio.estructuraDeDatos;

import java.util.List;

public class OperadorEmergencia extends Usuario {

    public OperadorEmergencia(String idUsuario, String nombre, String usuario, String contrasena, RolUsuario rol) {
        super(idUsuario, nombre, usuario, contrasena, rol);
    }

    public void actualizarEstadoUbicacion(Ubicacion u, NivelDeAfectacion nivel) {
        if (u == null || nivel == null) {
            System.out.println("Error: ubicación o nivel nulos");
            return;
        }
        var anterior = u.getNivelAfectacion();
        u.actualizarNivelAfectacion(nivel);
        System.out.println("Operador " + getNombre() + ": nivel de " + u.getNombre()
                + " cambió de " + anterior + " a " + nivel);
    }


    // Priorizar evacuaciones según urgencia
    public void priorizarEvacuacion(ColaPrioridadEvacuacion cola, Evacuacion e) {
        if (cola == null || e == null) {
            System.out.println("Error: cola o evacuación nulas");
            return;
        }
        cola.insertar(e);
        System.out.println("Operador " + getNombre() + ": evacuación priorizada → " + e);
    }

    // Versión 2: generar evacuación automática basada en nivel de afectación
    public void priorizarEvacuacionAutomatica(ColaPrioridadEvacuacion cola, Ubicacion zona) {
        if (cola == null || zona == null) {
            System.out.println("Error: cola o zona nula");
            return;
        }

        int prioridad = switch (zona.getNivelAfectacion()) {
            case GRAVE -> 10;
            case MODERADO -> 5;
            default -> 2;
        };

        int cantidadPersonas = zona.getPersonas() != null ? zona.getPersonas().size() : 0;

        Evacuacion e = new Evacuacion(
                "EV-" + zona.getIdUbicacion(),
                prioridad,
                cantidadPersonas,
                EstadoEvacuacion.PENDIENTE,
                zona
        );

        cola.insertar(e);
        System.out.println("Evacuación automática registrada para " + zona.getNombre()
                + " (nivel " + zona.getNivelAfectacion() + ", prioridad " + prioridad + ")");
    }


    // 3) Ejecutar la siguiente evacuación (del tope de la cola)
    public void coordinarEvacuacion(ColaPrioridadEvacuacion cola) {
        if (cola == null || cola.estaVacia()) {
            System.out.println("No hay evacuaciones pendientes.");
            return;
        }

        Evacuacion ev = cola.extraerMayorPrioridad();
        ev.iniciarEvacuacion();
        ev.completarEvacuacion();

        System.out.println("Operador " + getNombre() + ": evacuación ejecutada → "
                + ev.getIdEvacuacion() + " (estado " + ev.getEstado() + ")");
    }


    // 4) Coordinar distribución manual de recursos

    public void coordinarDistribucionRecursos(MapaRecursos mapa, ArbolDistribuido arbol,
                                              Ubicacion origen, Ubicacion destino,
                                              String idRecurso, int cantidad) {
        if (mapa == null || arbol == null || origen == null || destino == null
                || idRecurso == null || idRecurso.isEmpty() || cantidad <= 0) {
            System.out.println("Error: datos inválidos para distribuir recursos.");
            return;
        }

        Recurso transferido = mapa.transferirRecurso(origen, destino, idRecurso, cantidad);
        if (transferido == null) {
            System.out.println("No fue posible transferir el recurso " + idRecurso
                    + " desde " + origen.getNombre() + " hacia " + destino.getNombre());
            return;
        }

        if (transferido.getIdRecurso().equals(idRecurso)) {
            arbol.moverRecurso(idRecurso, destino);
        } else {
            arbol.insertar(destino, transferido);
        }

        System.out.println("Operador " + getNombre() + ": recurso "
                + transferido.getNombre() + " (" + transferido.getIdRecurso()
                + ") trasladado a " + destino.getNombre()
                + " (cantidad " + transferido.getCantidad() + ")");
    }


    // 5) Distribuir recurso según urgencia y proximidad
    public void distribuirRecursoUrgente(GrafoTransporte grafo, MapaRecursos mapa,
                                         Ubicacion origen, List<Ubicacion> zonas, Recurso recurso) {
        if (grafo == null || mapa == null || origen == null || zonas == null || zonas.isEmpty() || recurso == null) {
            System.out.println("Error: datos insuficientes para distribuir recurso.");
            return;
        }

        // 1. Filtrar zonas urgentes (GRAVE > MODERADO)
        List<Ubicacion> urgentes = zonas.stream()
                .filter(z -> z.getNivelAfectacion() == NivelDeAfectacion.GRAVE)
                .toList();

        if (urgentes.isEmpty()) {
            urgentes = zonas.stream()
                    .filter(z -> z.getNivelAfectacion() == NivelDeAfectacion.MODERADO)
                    .toList();
        }

        if (urgentes.isEmpty()) {
            System.out.println("No hay zonas urgentes para enviar el recurso " + recurso.getNombre());
            return;
        }

        // 2. Escoger la más cercana
        Ubicacion destinoMasCercano = null;
        double menorDistancia = Double.MAX_VALUE;

        for (Ubicacion z : urgentes) {
            List<Ubicacion> camino = grafo.buscarCaminoDijkstra(origen, z);
            if (camino != null) {
                double d = calcularDistanciaTotal(grafo, camino);
                if (d < menorDistancia) {
                    menorDistancia = d;
                    destinoMasCercano = z;
                }
            }
        }

        if (destinoMasCercano == null) {
            System.out.println("No se encontró ruta a zonas urgentes.");
            return;
        }

        // 3. Asignar el recurso en la zona seleccionada
        mapa.agregarRecurso(destinoMasCercano, recurso);
        System.out.println("Recurso " + recurso.getNombre() + " distribuido a "
                + destinoMasCercano.getNombre()
                + " (nivel " + destinoMasCercano.getNivelAfectacion()
                + ", distancia " + menorDistancia + ")");
    }

    private double calcularDistanciaTotal(GrafoTransporte grafo, List<Ubicacion> camino) {
        double total = 0;
        for (int i = 0; i < camino.size() - 1; i++) {
            Ubicacion a = camino.get(i);
            Ubicacion b = camino.get(i + 1);
            for (Ruta r : grafo.obtenerRutasDesde(a)) {
                if (r.getDestino().equals(b)) {
                    total += r.getDistancia();
                    break;
                }
            }
        }
        return total;
    }


    // 6) Autenticación / sesión
    @Override
    public boolean autenticar(String user, String pass) {
        return this.getUsuario().equals(user) && this.getContrasena().equals(pass);
    }

    @Override
    public void cerrarSesion() {
        System.out.println(getNombre() + " (operador/a) ha cerrado sesión.");
    }
}
