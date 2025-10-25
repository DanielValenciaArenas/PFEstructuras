package co.edu.uniquindio.estructuraDeDatos;

public class Evacuacion {
    private String idEvacuacion;
    private int prioridad;
    private int cantidadPersonas;
    private EstadoEvacuacion estado;
    private Ubicacion ubicacion;

    public Evacuacion(String idEvacuacion, int prioridad, int cantidadPersonas, EstadoEvacuacion estado, Ubicacion ubicacion) {
        this.idEvacuacion = idEvacuacion;
        this.prioridad = prioridad;
        this.cantidadPersonas = cantidadPersonas;
        this.estado = estado;
        this.ubicacion = ubicacion;
    }

    //Getters y Setters
    public String getIdEvacuacion() { return idEvacuacion; }
    public void setIdEvacuacion(String idEvacuacion) { this.idEvacuacion = idEvacuacion; }
    public int getPrioridad() { return prioridad; }
    public void setPrioridad(int prioridad) { this.prioridad = prioridad; }
    public int getCantidadPersonas() { return cantidadPersonas; }
    public void setCantidadPersonas(int cantidadPersonas) { this.cantidadPersonas = cantidadPersonas; }
    public EstadoEvacuacion getEstado() { return estado; }
    public void setEstado(EstadoEvacuacion estado) { this.estado = estado; }
    public Ubicacion getUbicacion() { return ubicacion; }
    public void setUbicacion(Ubicacion ubicacion) { this.ubicacion = ubicacion; }

    public void iniciarEvacuacion() {}
    public void completarEvacuacion() {}
    public void actualizarPrioridad(int nuevaPrioridad) {}
}
