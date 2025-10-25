package co.edu.uniquindio.estructuraDeDatos;

import java.util.List;

public class Ubicacion {
    private String idUbicacion;
    private String nombre;
    private TipoZona tipoZona;
    private NivelDeAfectacion nivelAfectacion;
    private Evacuacion evacuacion;
    private List<Persona> personas;
    private List<Recurso> recursos;
    private List<EquipoRescate> equiposDeRecaste;


    public Ubicacion(String idUbicacion, String nombre, TipoZona tipoZona, NivelDeAfectacion nivelAfectacion,
                     Evacuacion evacuacion, List<Persona> personas, List<Recurso> recursos, List<EquipoRescate> equiposDeRecaste) {
        this.idUbicacion = idUbicacion;
        this.nombre = nombre;
        this.tipoZona = tipoZona;
        this.nivelAfectacion = nivelAfectacion;
        this.evacuacion = evacuacion;
        this.personas = personas;
        this.recursos = recursos;
        this.equiposDeRecaste = equiposDeRecaste;
    }

    // Getters y Setters
    public String getIdUbicacion() { return idUbicacion; }
    public void setIdUbicacion(String idUbicacion) { this.idUbicacion = idUbicacion; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public TipoZona getTipoZona() { return tipoZona; }
    public void setTipoZona(TipoZona tipoZona) { this.tipoZona = tipoZona; }
    public NivelDeAfectacion getNivelAfectacion() { return nivelAfectacion; }
    public void setNivelAfectacion(NivelDeAfectacion nivelAfectacion) { this.nivelAfectacion = nivelAfectacion; }
    public Evacuacion getEvacuacion() { return evacuacion; }
    public void setEvacuacion(Evacuacion evacuacion) { this.evacuacion = evacuacion; }
    public List<Persona> getPersonas() { return personas; }
    public void setPersonas(List<Persona> personas) { this.personas = personas; }
    public List<Recurso> getRecursos() { return recursos; }
    public void setRecursos(List<Recurso> recursos) { this.recursos = recursos; }
    public List<EquipoRescate> getEquiposDeRecaste() { return equiposDeRecaste; }
    public void setEquiposDeRecaste(List<EquipoRescate> equiposDeRecaste) { this.equiposDeRecaste = equiposDeRecaste; }

    public void agregarPersona(Persona p) {}
    public void evacuarPersona(Ubicacion destino, int cantidad) {}
    public void agregarRecurso(Recurso r) {}
    public void asignarEquipo(EquipoRescate e) {}
    public void actualizarNivelAfectacion(NivelDeAfectacion n) {}
}
