package co.edu.uniquindio.estructuraDeDatos;

public abstract class Recurso {
    private String idRecurso;
    private String nombre;
    private int cantidad;
    private Ubicacion ubicacion;

    public Recurso(String idRecurso, String nombre, int cantidad, Ubicacion ubicacion) {
        this.idRecurso = idRecurso;
        this.nombre = nombre;
        this.cantidad = cantidad;
        this.ubicacion = ubicacion;
    }

    //Getters y Setters
    public String getIdRecurso() { return idRecurso; }
    public void setIdRecurso(String idRecurso) { this.idRecurso = idRecurso; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public Ubicacion getUbicacion() { return ubicacion; }
    public void setUbicacion(Ubicacion ubicacion) { this.ubicacion = ubicacion; }

    public void asignarUbicacion(Ubicacion u) {}
    public void consumir(int cantidad) {}
}
