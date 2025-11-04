package co.edu.uniquindio.estructuraDeDatos;

public class OperadorEmergencia extends Usuario {

    public OperadorEmergencia(String idUsuario, String nombre, String usuario, String contrasena, RolUsuario rol) {
        super(idUsuario, nombre, usuario, contrasena, rol);
    }

    // 1) Monitorear/actualizar la situación en tiempo real
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

    // 2) Priorizar evacuaciones según urgencia (insertar en cola)
    public void priorizarEvacuacion(ColaPrioridadEvacuacion cola, Evacuacion e) {
        if (cola == null || e == null) {
            System.out.println("Error: cola o evacuación nulas");
            return;
        }
        cola.insertar(e);
        System.out.println("Operador " + getNombre() + ": evacuación priorizada → " + e);
    }

    // Ejecutar la siguiente evacuación (del tope de la cola)
    public void coordinarEvacuacion(ColaPrioridadEvacuacion cola) {
        if (cola == null || cola.estaVacia()) {
            System.out.println("No hay evacuaciones pendientes.");
            return;
        }
        Evacuacion ev = cola.extraerMayorPrioridad();
        ev.iniciarEvacuacion();
        // Aquí podrías disparar lógica de movimiento de personas, etc.
        ev.completarEvacuacion();
        System.out.println("Operador " + getNombre() + ": evacuación ejecutada → " + ev.getIdEvacuacion()
                + " (estado " + ev.getEstado() + ")");
    }

    // 3) Coordinar la distribución de recursos entre zonas
    // - Usa MapaRecursos para mover total o parcialmente un recurso por id
    // - Actualiza el ArbolDistribuido (si es movimiento total → mover; si es parcial → insertar una copia)
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

        // Si el recurso que llegó al destino tiene el mismo id, fue un movimiento total → solo mover en el árbol.
        // Si llegó con id distinto, fue una copia parcial → insertar nuevo en el árbol.
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

    @Override
    public boolean autenticar(String user, String pass) {
        return this.getUsuario().equals(user) && this.getContrasena().equals(pass);
    }

    @Override
    public void cerrarSesion() {
        System.out.println(getNombre() + " (operador/a) ha cerrado sesión.");
    }
}
