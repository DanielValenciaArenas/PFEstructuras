package co.edu.uniquindio.estructuraDeDatos;

public class MainWeb {
    public static void main(String[] args) throws Exception {
        GrafoTransporte grafo = new GrafoTransporte();
        ColaPrioridadEvacuacion cola = new ColaPrioridadEvacuacion();
        MapaRecursos mapa = new MapaRecursos();
        ArbolDistribuido arbol = new ArbolDistribuido();
        java.util.List<Usuario> usuarios = new java.util.ArrayList<>(); // <— IMPORTANTE

        SistemaGestionDesastres sistema = new SistemaGestionDesastres(
                grafo, cola, mapa, arbol, usuarios
        );

        WebServer ws = new WebServer(sistema);
        ws.start(8080);
    }
}
