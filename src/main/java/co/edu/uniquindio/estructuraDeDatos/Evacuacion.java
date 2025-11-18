package co.edu.uniquindio.estructuraDeDatos;

public class Evacuacion implements Comparable<Evacuacion> {

    private String idEvacuacion;
    private int prioridad;
    private int cantidadPersonas;
    private EstadoEvacuacion estado;
    private Ubicacion ubicacion;

    public Evacuacion(String idEvacuacion,
                      int prioridad,
                      int cantidadPersonas,
                      EstadoEvacuacion estado,
                      Ubicacion ubicacion) {

        this.idEvacuacion = idEvacuacion;
        this.prioridad = prioridad;
        this.cantidadPersonas = cantidadPersonas;
        this.estado = estado;
        this.ubicacion = ubicacion;
    }

    // ========================
    //    GETTERS y SETTERS
    // ========================
    public String getIdEvacuacion() {
        return idEvacuacion;
    }

    public void setIdEvacuacion(String idEvacuacion) {
        this.idEvacuacion = idEvacuacion;
    }

    public int getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(int prioridad) {
        this.prioridad = prioridad;
    }

    public int getCantidadPersonas() {
        return cantidadPersonas;
    }

    public void setCantidadPersonas(int cantidadPersonas) {
        this.cantidadPersonas = cantidadPersonas;
    }

    public EstadoEvacuacion getEstado() {
        return estado;
    }

    public void setEstado(EstadoEvacuacion estado) {
        this.estado = estado;
    }

    public Ubicacion getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(Ubicacion ubicacion) {
        this.ubicacion = ubicacion;
    }

    // ========================
    //        MÉTODOS
    // ========================

    public void iniciarEvacuacion() {
        if (estado == EstadoEvacuacion.PENDIENTE) {
            estado = EstadoEvacuacion.EN_PROCESO;
        }
    }

    public void completarEvacuacion() {
        if (estado == EstadoEvacuacion.EN_PROCESO) {
            estado = EstadoEvacuacion.COMPLETADA;
        }
    }

    public void actualizarPrioridad(int nuevaPrioridad) {
        if (nuevaPrioridad >= 0) {
            this.prioridad = nuevaPrioridad;
        }
    }

    // Orden: prioridad más alta primero
    @Override
    public int compareTo(Evacuacion otra) {
        return Integer.compare(otra.getPrioridad(), this.prioridad);
    }

    @Override
    public String toString() {
        return "Evacuacion{" +
                "id='" + idEvacuacion + '\'' +
                ", prioridad=" + prioridad +
                ", personas=" + cantidadPersonas +
                ", estado=" + estado +
                '}';
    }
}