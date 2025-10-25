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
    public boolean autenticar(String user, String pass) {
        return false;
    }

    @Override
    public void cerrarSesion() {

    }
}
