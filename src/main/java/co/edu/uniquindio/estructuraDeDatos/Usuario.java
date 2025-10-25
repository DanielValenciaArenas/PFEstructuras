package co.edu.uniquindio.estructuraDeDatos;

public abstract class Usuario {
    private String idUsuario;
    private String nombre;
    private String usuario;
    private String contrasena;
    private RolUsuario rol;

    public Usuario(String idUsuario, String nombre, String usuario, String contrasena, RolUsuario rol) {
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.usuario = usuario;
        this.contrasena = contrasena;
        this.rol = rol;
    }

    // Getters y Setters
    public String getIdUsuario() { return idUsuario; }
    public void setIdUsuario(String idUsuario) { this.idUsuario = idUsuario; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }
    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }
    public RolUsuario getRol() { return rol; }
    public void setRol(RolUsuario rol) { this.rol = rol; }

    public abstract boolean autenticar(String user, String contrasena);
    public abstract void cerrarSesion();
}
