package co.edu.uniquindio.estructuraDeDatos;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        // Crear grafo y estructuras principales
        GrafoTransporte grafo = new GrafoTransporte();
        ColaPrioridadEvacuacion cola = new ColaPrioridadEvacuacion();
        MapaRecursos mapa = new MapaRecursos();
        ArbolDistribuido arbol = new ArbolDistribuido();
        List<Usuario> usuarios = new ArrayList<>();

        SistemaGestionDesastres sistema = new SistemaGestionDesastres(grafo, cola, mapa, arbol, usuarios);

        // Usuarios (para probar Operador también)
        OperadorEmergencia op = new OperadorEmergencia("O1","Oscar","oscar","123", RolUsuario.OPERADOR);
        Administrador admin = new Administrador("A1","Ana","ana","123", RolUsuario.ADMINISTRADOR);
        usuarios.add(op);
        usuarios.add(admin);

        // Crear ubicaciones
        Ubicacion ciudad = new Ubicacion("U1", "Ciudad Central", TipoZona.CIUDAD,
                NivelDeAfectacion.GRAVE, null, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), 0.0, 0.0);

        Ubicacion refugio = new Ubicacion("U2", "Refugio Norte", TipoZona.REFUGIO,
                NivelDeAfectacion.LEVE, null, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), 1.0, 1.0);

        Ubicacion centroAyuda = new Ubicacion("U3", "Centro de Ayuda Sur", TipoZona.CENTRO_AYUDA,
                NivelDeAfectacion.MODERADO, null, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), 2.0, 2.0);

        sistema.agregarUbicacion(ciudad);
        sistema.agregarUbicacion(refugio);
        sistema.agregarUbicacion(centroAyuda);

        // Crear rutas
        Ruta r1 = new Ruta("R1", ciudad, refugio, 10);
        Ruta r2 = new Ruta("R2", ciudad, centroAyuda, 5);
        Ruta r3 = new Ruta("R3", centroAyuda, refugio, 3);

        sistema.agregarRuta(r1);
        sistema.agregarRuta(r2);
        sistema.agregarRuta(r3);

        // Probar Dijkstra
        System.out.println("\n===> CAMINO MÍNIMO Ciudad -> Refugio");
        var camino = grafo.buscarCaminoDijkstra(ciudad, refugio);
        if (camino != null) {
            camino.forEach(u -> System.out.println(" - " + u.getNombre()));
        } else {
            System.out.println("No se encontró camino");
        }

        // Crear personas y agregarlas a Ciudad
        Persona p1 = new Persona("P1", "Ana", EstadoPersona.EN_PELIGRO, ciudad);
        Persona p2 = new Persona("P2", "Luis", EstadoPersona.EN_PELIGRO, ciudad);
        ciudad.agregarPersona(p1);
        ciudad.agregarPersona(p2);

        System.out.println("\n===> Personas en Ciudad:");
        ciudad.getPersonas().forEach(p -> System.out.println(" - " + p.getNombre()));

        // Crear y asignar recursos (MAPA y ÁRBOL)
        Recurso alimento = new RecursoAlimento("R01", "Agua Potable", 100, ciudad, LocalDate.of(2025, 12, 31));
        mapa.agregarRecurso(ciudad, alimento);
        arbol.insertar(ciudad, alimento);

        Recurso medicina = new RecursoMedicina("M01", "Botiquín", 50, centroAyuda, "Primeros auxilios");
        mapa.agregarRecurso(centroAyuda, medicina);
        arbol.insertar(centroAyuda, medicina);

        System.out.println("\n===> Recursos en Ciudad:");
        mapa.obtenerRecursos(ciudad).forEach(r -> System.out.println(" - " + r.getNombre() + " (" + r.getCantidad() + ")"));

        System.out.println("\n===> Distribución inicial (Árbol):");
        arbol.mostrarDistribucion();

        // Crear y asignar equipo de rescate
        EquipoRescate equipo1 = new EquipoRescate("E1", "Bomberos", 5, null);
        equipo1.setNombre("Bomberos U1"); // opcional, ya que tu clase tiene 'nombre' aparte del 'tipo'
        ciudad.asignarEquipo(equipo1);

        System.out.println("\n===> Equipos asignados a Ciudad:");
        ciudad.getEquiposDeRescate().forEach(e -> System.out.println(" - " + e.getTipo()));

        // Crear evacuaciones y probar cola de prioridad
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

        // Simulación de ruta desde el sistema
        System.out.println("\n===> Simulación de ruta Ciudad -> Refugio");
        SimulacionRuta sim = sistema.simularRuta(ciudad, refugio);
        if (sim != null) {
            sim.getUbicaciones().forEach(u -> System.out.println(" - " + u.getNombre()));
        }

        // PRUEBAS DEL OPERADOR

        // Monitorear/actualizar estado de ubicaciones
        System.out.println("\n===> [Operador] Monitoreo / Actualización de estado");
        op.actualizarEstadoUbicacion(ciudad, NivelDeAfectacion.GRAVE);
        op.actualizarEstadoUbicacion(refugio, NivelDeAfectacion.MODERADO);

        // Priorizar y coordinar evacuaciones
        System.out.println("\n===> [Operador] Priorizar y coordinar evacuaciones");
        Evacuacion e4 = new Evacuacion("EV4", 9, 20, EstadoEvacuacion.PENDIENTE, ciudad);
        Evacuacion e5 = new Evacuacion("EV5", 6, 15, EstadoEvacuacion.PENDIENTE, centroAyuda);
        op.priorizarEvacuacion(cola, e4);
        op.priorizarEvacuacion(cola, e5);

        System.out.println("Cola actual:");
        cola.mostrarCola();
        op.coordinarEvacuacion(cola);
        op.coordinarEvacuacion(cola);

        // Coordinar distribución de recursos
        System.out.println("\n===> [Operador] Distribución de recursos");
        System.out.println("Antes:");
        imprimirRecursosPorUbicacion(mapa, ciudad);
        imprimirRecursosPorUbicacion(mapa, centroAyuda);
        imprimirRecursosPorUbicacion(mapa, refugio);

        // Transferencia PARCIAL: mover 30 de Agua Potable de Ciudad -> Refugio
        op.coordinarDistribucionRecursos(mapa, arbol, ciudad, refugio, "R01", 30);

        // Transferencia TOTAL: mover 50 de Botiquín de Centro de Ayuda -> Refugio
        op.coordinarDistribucionRecursos(mapa, arbol, centroAyuda, refugio, "M01", 50);

        System.out.println("\nDespués:");
        imprimirRecursosPorUbicacion(mapa, ciudad);
        imprimirRecursosPorUbicacion(mapa, centroAyuda);
        imprimirRecursosPorUbicacion(mapa, refugio);

        System.out.println("\n===> Distribución actualizada (Árbol):");
        arbol.mostrarDistribucion();

        System.out.println("\n=====> FIN DE PRUEBAS <=====");
    }

    private static void imprimirRecursosPorUbicacion(MapaRecursos mapa, Ubicacion u) {
        System.out.println(u.getNombre() + ":");
        var recursos = mapa.obtenerRecursos(u);
        if (recursos.isEmpty()) {
            System.out.println("  (sin recursos)");
        } else {
            for (Recurso r : recursos) {
                System.out.println("  - " + r.getNombre() + " [" + r.getIdRecurso() + "] x" + r.getCantidad());
            }
        }
    }
}
