package co.edu.uniquindio.estructuraDeDatos;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Ubicacion {
    private String idUbicacion;
    private String nombre;
    private TipoZona tipoZona;
    private NivelDeAfectacion nivelAfectacion;
    private Evacuacion evacuacion;
    private List<Persona> personas;
    private List<Recurso> recursos;
    private List<EquipoRescate> equiposDeRescate;

    public Ubicacion(String idUbicacion, String nombre, TipoZona tipoZona, NivelDeAfectacion nivelAfectacion,
                     Evacuacion evacuacion, List<Persona> personas, List<Recurso> recursos, List<EquipoRescate> equiposDeRescate) {
        this.idUbicacion = idUbicacion;
        this.nombre = nombre;
        this.tipoZona = tipoZona;
        this.nivelAfectacion = nivelAfectacion;
        this.evacuacion = evacuacion;
        this.personas = (personas != null) ? personas : new ArrayList<>();
        this.recursos = (recursos != null) ? recursos : new ArrayList<>();
        this.equiposDeRescate = (equiposDeRescate != null) ? equiposDeRescate : new ArrayList<>();
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
    public List<EquipoRescate> getEquiposDeRescate() { return equiposDeRescate; }
    public void setEquiposDeRescate(List<EquipoRescate> equiposDeRescate) { this.equiposDeRescate = equiposDeRescate; }

    public void agregarPersona(Persona p) {
        if (p != null) {
            personas.add(p);
            p.setUbicacion(this);
        }
    }

    // Evacúa 'cantidad' personas (si hay menos, evacúa todas) hacia 'destino'
    public void evacuarPersona(Ubicacion destino, int cantidad) {
        if (destino == null || cantidad <= 0) return;
        int mover = Math.min(cantidad, personas.size());
        for (int i = 0; i < mover; i++) {
            Persona p = personas.remove(0);
            destino.agregarPersona(p); // agrega y actualiza su ubicación
            p.setEstado(EstadoPersona.EVACUADO);
        }
    }

    public void agregarRecurso(Recurso r) {
        if (r != null) {
            recursos.add(r);
            r.asignarUbicacion(this);
        }
    }

    public void asignarEquipo(EquipoRescate e) {
        if (e != null) {
            equiposDeRescate.add(e);
            e.asignarUbicacion(this);
        }
    }

    public void actualizarNivelAfectacion(NivelDeAfectacion n) {
        if (n != null) this.nivelAfectacion = n;
    }

    // Igualdad por id para usar como clave en mapas
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Ubicacion)) return false;
        Ubicacion that = (Ubicacion) o;
        return Objects.equals(idUbicacion, that.idUbicacion);
    }
    @Override public int hashCode() { return Objects.hash(idUbicacion); }
}
