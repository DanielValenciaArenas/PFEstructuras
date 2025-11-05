package co.edu.uniquindio.estructuraDeDatos;

import java.util.ArrayList;
import java.util.List;

public class Ubicacion implements Comparable<Ubicacion>{
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
        this.personas = new ArrayList<>();
        this.recursos = new ArrayList<>();
        this.equiposDeRescate = new ArrayList<>();
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
    public void setEquiposDeRescate(List<EquipoRescate> equiposDeRecaste) { this.equiposDeRescate = equiposDeRecaste; }

    //Agregar una persona a la ubicación
    public void agregarPersona(Persona p) {
        if (p != null) {
            personas.add(p);
            System.out.println("Persona agregada en " + nombre + ": " + p.getNombre());
        }
    }

    // Evacuar personas desde esta ubicación hacia otra
    public void evacuarPersona(Ubicacion destino, int cantidad) {
        if (personas.isEmpty()) {
            System.out.println("No hay personas para evacuar en " + nombre);
            return;
        }

        if (cantidad > personas.size()) {
            cantidad = personas.size();
        }

        int contador = 0;
        while (contador < cantidad && !personas.isEmpty()) {
            Persona persona = personas.remove(0);
            destino.agregarPersona(persona);
            contador++;
        }

        System.out.println("Se ha evacuado " + contador + " personas desde " + nombre + " hacia " + destino.getNombre());
    }

    // Agregar un recurso en la ubicación
    public void agregarRecurso(Recurso r) {
        if (r != null) {
            recursos.add(r);
            r.setUbicacion(this);
            System.out.println("Recurso agregado a " + nombre + ": " + r.getNombre());
        }
    }

    public void eliminarRecurso(String idRecurso) {
        if (recursos == null || recursos.isEmpty()) {
            System.out.println("No hay recursos en " + nombre);
            return;
        }

        Recurso eliminado = null;
        for (Recurso r : recursos) {
            if (r.getIdRecurso().equals(idRecurso)) {
                eliminado = r;
                break;
            }
        }

        if (eliminado != null) {
            recursos.remove(eliminado);
            System.out.println("Recurso eliminado de " + nombre + ": " + eliminado.getNombre());
        } else {
            System.out.println("No se encontró el recurso con ID " + idRecurso + " en " + nombre);
        }
    }

    // Asignar equipo de rescate
    public void asignarEquipo(EquipoRescate e) {
        if (e != null) {
            equiposDeRescate.add(e);
            System.out.println("Equipo asignado a " + nombre + ": " + e.getNombre());
        }
    }

    // Actualizar nivel de afectación de la zona
    public void actualizarNivelAfectacion(NivelDeAfectacion n) {
        this.nivelAfectacion = n;
        System.out.println("Nivel de afectación de " + nombre + " actualizado a " + n);
    }

    public boolean eliminarEquipo(String idEquipo) {
        if (equiposDeRescate == null) return false;
        return equiposDeRescate.removeIf(e -> e.getIdEquipo().equals(idEquipo));
    }

    @Override
    public String toString() {
        return "Ubicación: " + nombre + " (" + tipoZona + ", " + nivelAfectacion + ")";
    }

    @Override
    public int compareTo(Ubicacion otra) {
        return this.nivelAfectacion.compareTo(otra.nivelAfectacion);
    }


}
