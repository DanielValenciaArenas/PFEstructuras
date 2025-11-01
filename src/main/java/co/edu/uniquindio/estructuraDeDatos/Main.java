package co.edu.uniquindio.estructuraDeDatos;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        // ----- Crear grafo y estructuras principales -----
        GrafoTransporte grafo = new GrafoTransporte();
        ColaPrioridadEvacuacion cola = new ColaPrioridadEvacuacion();
        MapaRecursos mapa = new MapaRecursos();
        ArbolDistribuido arbol = new ArbolDistribuido();
        List<Usuario> usuarios = new ArrayList<>();

        SistemaGestionDesastres sistema = new SistemaGestionDesastres(grafo, cola, mapa, arbol, usuarios);

        // ----- Crear ubicaciones -----
        Ubicacion ciudad = new Ubicacion("U1", "Ciudad Central", TipoZona.CIUDAD,
                NivelDeAfectacion.GRAVE, null, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());

        Ubicacion refugio = new Ubicacion("U2", "Refugio Norte", TipoZona.REFUGIO,
                NivelDeAfectacion.LEVE, null, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());

        Ubicacion centroAyuda = new Ubicacion("U3", "Centro de Ayuda Sur", TipoZona.CENTRO_AYUDA,
                NivelDeAfectacion.MODERADO, null, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());

        sistema.agregarUbicacion(ciudad);
        sistema.agregarUbicacion(refugio);
        sistema.agregarUbicacion(centroAyuda);

        // ----- Crear rutas -----
        Ruta r1 = new Ruta("R1", ciudad, refugio, 10);
        Ruta r2 = new Ruta("R2", ciudad, centroAyuda, 5);
        Ruta r3 = new Ruta("R3", centroAyuda, refugio, 3);

        sistema.agregarRuta(r1);
        sistema.agregarRuta(r2);
        sistema.agregarRuta(r3);

        // ----- Probar Dijkstra -----
        System.out.println("\n===> CAMINO MÍNIMO Ciudad -> Refugio");
        var camino = grafo.buscarCaminoDijkstra(ciudad, refugio);
        if (camino != null) {
            camino.forEach(u -> System.out.println(" - " + u.getNombre()));
        } else {
            System.out.println("No se encontró camino");
        }

        // ----- Crear personas y agregarlas a Ciudad -----
        Persona p1 = new Persona("P1", "Ana", EstadoPersona.EN_PELIGRO, ciudad);
        Persona p2 = new Persona("P2", "Luis", EstadoPersona.EN_PELIGRO, ciudad);
        ciudad.agregarPersona(p1);
        ciudad.agregarPersona(p2);

        System.out.println("\n===> Personas en Ciudad:");
        ciudad.getPersonas().forEach(p -> System.out.println(" - " + p.getNombre()));

        // ----- Crear y asignar recurso -----
        Recurso alimento = new RecursoAlimento("R01", "Agua Potable", 100, ciudad, LocalDate.of(2025, 12, 31));
        mapa.agregarRecurso(ciudad, alimento);

        System.out.println("\n===> Recursos en Ciudad:");
        mapa.obtenerRecursos(ciudad).forEach(r -> System.out.println(" - " + r.getNombre() + " (" + r.getCantidad() + ")"));

        // ----- Crear y asignar equipo de rescate -----
        EquipoRescate equipo1 = new EquipoRescate("E1", "Bomberos", 5, null);
        ciudad.asignarEquipo(equipo1);

        System.out.println("\n===> Equipos asignados a Ciudad:");
        ciudad.getEquiposDeRescate().forEach(e -> System.out.println(" - " + e.getTipo()));

        // ----- Crear evacuaciones y probar cola de prioridad -----
        Evacuacion e1 = new Evacuacion("EV1", 5, 30, EstadoEvacuacion.PENDIENTE, ciudad);
        Evacuacion e2 = new Evacuacion("EV2", 8, 10, EstadoEvacuacion.PENDIENTE, refugio);
        Evacuacion e3 = new Evacuacion("EV3", 3, 50, EstadoEvacuacion.PENDIENTE, centroAyuda);

        cola.insertar(e1);
        cola.insertar(e2);
        cola.insertar(e3);

        System.out.println("\n===> Cola de evacuaciones (mayor prioridad primero):");
        cola.mostrarCola();

        System.out.println("\nExtraer por prioridad:");
        while (!cola.estaVacia()) {
            Evacuacion evac = cola.extraerMayorPrioridad();
            System.out.println("Atendiendo " + evac.getIdEvacuacion() + " (prioridad " + evac.getPrioridad() + ")");
        }

        // ----- Simulación de ruta desde el sistema -----
        System.out.println("\n===> Simulación de ruta Ciudad -> Refugio");
        SimulacionRuta sim = sistema.simularRuta(ciudad, refugio);
        if (sim != null) {
            sim.getUbicaciones().forEach(u -> System.out.println(" - " + u.getNombre()));
        }

        System.out.println("\n=====> FIN DE PRUEBAS <=====");
    }
}
