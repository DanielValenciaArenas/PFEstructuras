package co.edu.uniquindio.estructuraDeDatos;

public class EquipoRescate {
    private String idEquipo;
    private String nombre;
    private String tipo;
    private int miembros;
    private Ubicacion ubicacion;

    public EquipoRescate(String idEquipo, String tipo, int miembros, Ubicacion ubicacion) {
        this.idEquipo = idEquipo;
        this.tipo = tipo;
        this.miembros = miembros;
        this.ubicacion = ubicacion;
    }

    //Getters y Setters
    public String getNombre() {return nombre;}
    public void setNombre(String nombre) {this.nombre = nombre;}
    public String getIdEquipo() { return idEquipo; }
    public void setIdEquipo(String idEquipo) { this.idEquipo = idEquipo; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public int getMiembros() { return miembros; }
    public void setMiembros(int miembros) { this.miembros = miembros; }
    public Ubicacion getUbicacion() { return ubicacion; }

    public void setUbicacion(Ubicacion ubicacion) {
        this.ubicacion = ubicacion;
    }

    public void asignarUbicacion(Ubicacion u) {}
    public void atenderEmergencia(Ubicacion u) {}
}
