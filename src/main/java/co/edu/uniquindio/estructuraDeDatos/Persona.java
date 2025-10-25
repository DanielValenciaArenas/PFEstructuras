package co.edu.uniquindio.estructuraDeDatos;

public class Persona {
    private String idPersona;
    private String nombre;
    private EstadoPersona estado;
    private Ubicacion ubicacion;

    public Persona(String idPersona, String nombre, EstadoPersona estado, Ubicacion ubicacion) {
        this.idPersona = idPersona;
        this.nombre = nombre;
        this.estado = estado;
        this.ubicacion = ubicacion;
    }

    //Getters y Setters
    public String getIdPersona() { return idPersona; }
    public void setIdPersona(String idPersona) { this.idPersona = idPersona; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public EstadoPersona getEstado() { return estado; }
    public void setEstado(EstadoPersona estado) { this.estado = estado; }
    public Ubicacion getUbicacion() { return ubicacion; }
    public void setUbicacion(Ubicacion ubicacion) { this.ubicacion = ubicacion; }


    public void cambiarEstado(EstadoPersona nuevo) {}
}
