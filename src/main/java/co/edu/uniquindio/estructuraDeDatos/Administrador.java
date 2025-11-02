package co.edu.uniquindio.estructuraDeDatos;

import java.util.List;

public class Administrador extends Usuario {

    public Administrador(String idUsuario, String nombre, String usuario, String contrasena, RolUsuario rol) {
        super(idUsuario, nombre, usuario, contrasena, rol);
    }

    // Crear una nueva ruta en el sistema
    public void crearRuta(GrafoTransporte g, Ubicacion origen, Ubicacion destino, double distancia) {
        if (g == null) {
            System.out.println("Error: no he asignado un grafo");
            return;
        }
        if (origen == null || destino == null) {
            System.out.println("Error: no he asignado ubicacion origen y destino");
            return;
        }
        if (distancia <= 0) {
            System.out.println("Error: la distancia debe ser un número positivo");
            return;
        }

        Ruta nuevaRuta = new Ruta(
                "RUTA: " + origen.getNombre() + "-" + destino.getNombre(),
                origen,
                destino,
                distancia
        );

        g.agregarRuta(nuevaRuta);

        System.out.println("Ruta agregada al sistema: " + nuevaRuta.getIdRuta());
    }

    // Agregar una ubicación nueva al Grafo
    public void agregarUbicacion(GrafoTransporte g, Ubicacion nueva) {
        if (g == null || nueva == null) {
            System.out.println("Error: no se puede agregar la ubicación, por favor ingrese GRAFO y UBICACIÓN a agregar");
            return;
        }
        g.agregarUbicacion(nueva);
        System.out.println("Administrador/a " + getNombre() + " ha añadido la ubicación: " + nueva.getNombre());
    }

    // Asignar recurso a una ubicación
    public void asignarRecurso(Ubicacion u, Recurso r, MapaRecursos mapa, ArbolDistribuido arbol) {
        if (u == null || r == null) {
            System.out.println("Error: ubicación o recurso nulos");
            return;
        }
        mapa.agregarRecurso(u, r);
        arbol.insertar(u, r);
        System.out.println("Recurso " + r.getNombre() + " asignado a " + u.getNombre());
    }

    // Eliminar un recurso según su ID
    public void eliminarRecurso(String idRecurso, ArbolDistribuido arbol) {
        arbol.eliminar(idRecurso);
        System.out.println("Recurso con ID " + idRecurso + " eliminado del árbol de distribución");
    }

    //Asignar un equipo de rescate en función de la ruta más corta
    public void asignarEquipo(GrafoTransporte grafo, Ubicacion origen, Ubicacion destino, EquipoRescate e, MapaRecursos mapa) {
        if (grafo == null || origen == null || destino == null || e == null) {
            System.out.println("Error: datos insuficientes para asignar equipo de rescate.");
            return;
        }

        if (!mapa.tieneRecursos(destino)) {
            System.out.println("No hay recursos suficientes en " + destino.getNombre() + " para recibir al equipo");
            return;
        }

        List<Ubicacion> rutaMasCorta = grafo.buscarCaminoDijkstra(origen, destino);
        if (rutaMasCorta == null) {
            System.out.println("No se encontró ruta disponible entre " + origen.getNombre() + " y " + destino.getNombre());
            return;
        }

        destino.asignarEquipo(e);
        System.out.println("Equipo " + e.getNombre() + " asignado a " + destino.getNombre()
                + " vía la ruta más corta desde " + origen.getNombre());
    }

    // Eliminar un grupo de rescate
    public void eliminarEquipo(Ubicacion u, String idEquipo) {
        if (u == null || idEquipo == null || idEquipo.isEmpty()) {
            System.out.println("Error: datos inválidos, no se ha podido eliminar el equipo");
            return;
        }

        boolean eliminado = u.eliminarEquipo(idEquipo);

        if (eliminado) {
            System.out.println("Equipo con ID " + idEquipo + " eliminado de " + u.getNombre());
        } else {
            System.out.println("No se encontró un equipo con ID " + idEquipo + " en " + u.getNombre());
        }
    }

    // Agendar una evacuación en la cola de prioridad
    public void planificarEvacuacion(ColaPrioridadEvacuacion cola, Evacuacion e) {
        if (cola == null || e == null) {
            System.out.println("Error: cola o evacuación nulas");
            return;
        }
        cola.insertar(e);
        System.out.println("Evacuación registrada: " + e);
    }

    // Ejecutar una evacuacion de la cola de prioridad
    public void ejecutarEvacuacion(ColaPrioridadEvacuacion cola) {
        if (cola == null || cola.estaVacia()) {
            System.out.println("No hay evacuaciones pendientes");
            return;
        }
        Evacuacion siguiente = cola.extraerMayorPrioridad();
        siguiente.iniciarEvacuacion();
        System.out.println("Ejecutando evacuación: " + siguiente);
    }


    @Override
    public boolean autenticar(String usuario, String contrasena) {
        return this.getUsuario().equals(usuario) && this.getContrasena().equals(contrasena);
    }

    @Override
    public void cerrarSesion() {
        System.out.println(getNombre() + " (administrador/a) ha cerrado sesión.");
    }
}
