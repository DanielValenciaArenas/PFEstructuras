package co.edu.uniquindio.estructuraDeDatos;

public class Administrador extends Usuario {

    public Administrador(String idUsuario, String nombre, String usuario, String contrasena, RolUsuario rol) {
        super(idUsuario, nombre, usuario, contrasena, rol);
    }

    public void crearRuta(GrafoTransporte g, Ubicacion origen, Ubicacion destino, double distancia) {}
    public void agregarUbicacion(GrafoTransporte g, Ubicacion nueva) {}
    public void asignarRecurso(Ubicacion u, Recurso r) {}
    public void asignarEquipo(Ubicacion u, EquipoRescate e) {}

    @Override
    public boolean autenticar(String usuario, String contrasena) {
        return this.getUsuario().equals(usuario) && this.getContrasena().equals(contrasena);
    }

    @Override
    public void cerrarSesion() {
        System.out.println(getNombre() + " (administrador/a) ha cerrado sesión.");
    }
}
