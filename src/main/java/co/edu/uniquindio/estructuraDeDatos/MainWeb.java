package co.edu.uniquindio.estructuraDeDatos;

import java.util.ArrayList;
import java.util.List;

public class MainWeb {

    public static void main(String[] args) throws Exception {

        GrafoTransporte grafo = new GrafoTransporte();
        ColaPrioridadEvacuacion cola = new ColaPrioridadEvacuacion();
        MapaRecursos mapa = new MapaRecursos();
        ArbolDistribuido arbol = new ArbolDistribuido();
        List<Usuario> usuarios = new ArrayList<>();

        // Crear el sistema
        SistemaGestionDesastres sistema = new SistemaGestionDesastres(
                grafo, cola, mapa, arbol, usuarios
        );

        // Intentar cargar desde JSON si existe
        PersistenciaJson.cargar(sistema);

        // Guardar automáticamente cuando el programa se cierre
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println(" Guardando datos antes de cerrar...");
            PersistenciaJson.guardar(sistema);
        }));

        // Iniciar el servidor web
        WebServer server = new WebServer(sistema);
        server.start(8080);
    }
}
