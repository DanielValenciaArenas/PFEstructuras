package co.edu.uniquindio.estructuraDeDatos;

import java.util.List;

public class SistemaGestionDesastres {
    private GrafoTransporte grafo;
    private ColaPrioridadEvacuacion colaEvacuaciones;
    private MapaRecursos mapaRecursos;
    private ArbolDistribuido arbolDistribucion;
    private List<Usuario> usuarios;

    public SistemaGestionDesastres(GrafoTransporte grafo, ColaPrioridadEvacuacion colaEvacuaciones, MapaRecursos mapaRecursos,
                                   ArbolDistribuido arbolDistribucion, List<Usuario> usuarios) {
        this.grafo = grafo;
        this.colaEvacuaciones = colaEvacuaciones;
        this.mapaRecursos = mapaRecursos;
        this.arbolDistribucion = arbolDistribucion;
        this.usuarios = usuarios;
    }

    // Metodo para registrar usuarios en el sistema sin duplicar
    public void registrarUsuario(Usuario nuevoUsuario) {
        for (Usuario usuario : usuarios) {
            if (usuario.getUsuario().equals(nuevoUsuario.getUsuario())) {
                System.out.println("Error: el nombre de usuario ya existe");
                return;
            }
        }
        usuarios.add(nuevoUsuario);
        System.out.println("Usuario registrado exitosamente: " + nuevoUsuario.getNombre());
    }

    // Metodo para autenticar credenciales de usuario
    public Usuario autenticar(String NombreUsuario, String contrasena) {
        for (Usuario usuario : usuarios) {
            if (usuario.autenticar(NombreUsuario, contrasena)) {
                System.out.println(usuario.getNombre() + " iniciaste sesión!, Bienvenid@");
                return usuario;
            }
        }
        System.out.println("Error: El nombre de usuario o la contraseña son incorrectos");
        return null;
    }


    public void agregarUbicacion(Ubicacion u) {}
    public void agregarRuta(Ruta r) {}
    public void registrarEvacuacion(Evacuacion e) {}
    public SimulacionRuta simularRuta(Ubicacion origen, Ubicacion destino) { return null; }

}
