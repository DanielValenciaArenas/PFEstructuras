package co.edu.uniquindio.estructuraDeDatos;

public class OperadorEmergencia extends Usuario {

    public OperadorEmergencia(String idUsuario, String nombre, String usuario, String contrasena, RolUsuario rol) {
        super(idUsuario, nombre, usuario, contrasena, rol);
    }

    public void actualizarEstadoUbicacion(Ubicacion u, NivelDeAfectacion nivel) {}
    public void priorizarEvacuacion(ColaPrioridadEvacuacion cola, Evacuacion e) {}
    public void coordinarEvacuacion(ColaPrioridadEvacuacion cola) {}

    @Override
    public boolean autenticar(String user, String pass) {
        return false;
    }

    @Override
    public void cerrarSesion() {

    }

}
