import co.edu.uniquindio.estructuraDeDatos.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PruebaSistemaTest {

    GrafoTransporte grafo;
    ColaPrioridadEvacuacion cola;
    MapaRecursos mapa;
    ArbolDistribuido arbol;
    List<Usuario> usuarios;
    SistemaGestionDesastres sistema;

    Ubicacion ciudad;
    Ubicacion centro;
    Ubicacion refugio;

    @BeforeEach
    void setUp() {
        grafo = new GrafoTransporte();
        cola = new ColaPrioridadEvacuacion();
        mapa = new MapaRecursos();
        arbol = new ArbolDistribuido();
        usuarios = new ArrayList<>();
        sistema = new SistemaGestionDesastres(grafo, cola, mapa, arbol, usuarios);

        ciudad = new Ubicacion("U1", "Ciudad Central", TipoZona.CIUDAD,
                NivelDeAfectacion.MODERADO, null, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        centro = new Ubicacion("U2", "Centro de Ayuda", TipoZona.CENTRO_AYUDA,
                NivelDeAfectacion.LEVE, null, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        refugio = new Ubicacion("U3", "Refugio Norte", TipoZona.REFUGIO,
                NivelDeAfectacion.LEVE, null, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());

        sistema.agregarUbicacion(ciudad);
        sistema.agregarUbicacion(centro);
        sistema.agregarUbicacion(refugio);

        sistema.agregarRuta(new Ruta("R1", ciudad, centro, 5));    // Ciudad -> Centro
        sistema.agregarRuta(new Ruta("R2", centro, refugio, 3));   // Centro -> Refugio
        sistema.agregarRuta(new Ruta("R3", ciudad, refugio, 12));  // Directo
    }

    @Test
    void testInsertarColaPrioridad() {
        Evacuacion e1 = new Evacuacion("EV1", 5, 30, EstadoEvacuacion.PENDIENTE, ciudad);
        Evacuacion e2 = new Evacuacion("EV2", 8, 10, EstadoEvacuacion.PENDIENTE, centro);
        Evacuacion e3 = new Evacuacion("EV3", 3, 50, EstadoEvacuacion.PENDIENTE, refugio);

        cola.insertar(e1);
        cola.insertar(e2);
        cola.insertar(e3);

        assertEquals("EV2", cola.extraerMayorPrioridad().getIdEvacuacion());
        assertEquals("EV1", cola.extraerMayorPrioridad().getIdEvacuacion());
        assertEquals("EV3", cola.extraerMayorPrioridad().getIdEvacuacion());
        assertTrue(cola.estaVacia());
    }

    @Test
    void testBuscarCaminoDijkstra() {
        var camino = grafo.buscarCaminoDijkstra(ciudad, refugio);
        assertNotNull(camino);
        assertEquals(3, camino.size());
        assertEquals(ciudad, camino.get(0));
        assertEquals(centro, camino.get(1));
        assertEquals(refugio, camino.get(2));
    }

    @Test
    void testInsertarEnArbolDistribuido() {
        RecursoAlimento agua = new RecursoAlimento("R01", "Agua", 100, ciudad, LocalDate.of(2026,12,31));
        arbol.insertar(ciudad, agua);
        var nodo = arbol.buscar("R01");
        assertNotNull(nodo);
        assertEquals("R01", nodo.getRecurso().getIdRecurso());
        assertEquals(ciudad, nodo.getUbicacion());
    }

    @Test
    void testAsignarRecursoYActualizarCantidad() {
        RecursoMedicina botiquin = new RecursoMedicina("M01", "Botiquín", 50, centro, "Primeros auxilios");
        mapa.agregarRecurso(centro, botiquin);
        assertFalse(mapa.obtenerRecursos(centro).isEmpty());

        mapa.actualizarCantidad(centro, botiquin, 80);
        assertEquals(80, botiquin.getCantidad());
    }

    @Test
    void testEvacuacionEstados() {
        Evacuacion e = new Evacuacion("EVX", 7, 20, EstadoEvacuacion.PENDIENTE, ciudad);
        e.iniciarEvacuacion();
        assertEquals(EstadoEvacuacion.EN_PROCESO, e.getEstado());
        e.completarEvacuacion();
        assertEquals(EstadoEvacuacion.COMPLETADA, e.getEstado());
    }

    @Test
    void testSimulacionRuta() {
        SimulacionRuta sim = new SimulacionRuta("SIM1", 0, List.of(), List.of());
        sim.ejecutarSimulacion(grafo, ciudad, refugio);
        assertNotNull(sim.getUbicaciones());
        assertEquals(3, sim.getUbicaciones().size());
        assertEquals(8.0, sim.getDistanciaTotal(), 0.0001);
    }

    @Test
    void testRegistrarUsuario() {
        Administrador a1 = new Administrador("A1","Ana","ana","123", RolUsuario.ADMINISTRADOR);
        Administrador a2 = new Administrador("A2","Ana2","ana","999", RolUsuario.ADMINISTRADOR);

        sistema.registrarUsuario(a1);
        sistema.registrarUsuario(a2); // duplicado

        assertEquals(1, usuarios.size());
        assertEquals("ana", usuarios.get(0).getUsuario());
    }
}
