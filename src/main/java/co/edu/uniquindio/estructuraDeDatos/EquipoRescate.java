package co.edu.uniquindio.estructuraDeDatos;

public class EquipoRescate {
    private String idEquipo;
    private String tipo;
    private int miembros;
    private Ubicacion ubicacion;

    public EquipoRescate(String idEquipo, String tipo, int miembros, Ubicacion ubicacion) {
        this.idEquipo = idEquipo;
        this.tipo = tipo;
        this.miembros = miembros;
        this.ubicacion = ubicacion;
    }

    // Getters y Setters
    public String getIdEquipo() { return idEquipo; }
    public void setIdEquipo(String idEquipo) { this.idEquipo = idEquipo; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public int getMiembros() { return miembros; }
    public void setMiembros(int miembros) { this.miembros = miembros; }
    public Ubicacion getUbicacion() { return ubicacion; }
    public void setUbicacion(Ubicacion ubicacion) { this.ubicacion = ubicacion; }

    public void asignarUbicacion(Ubicacion u) { this.ubicacion = u; }

    public void atenderEmergencia(Ubicacion u) {
        // Aquí puedes loggear o modificar estado; la orquestación la hará el Sistema
        System.out.println("Equipo " + idEquipo + " atiende emergencia en " + (u!=null?u.getNombre():"?"));
    }
}
