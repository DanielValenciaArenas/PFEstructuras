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


    public void agregarRecurso(Recurso r) {
        if (r != null) {
            recursos.add(r);
            r.setUbicacion(this);
            System.out.println("Recurso agregado a " + nombre + ": " + r.getNombre());
        }
    }

    // Asignar equipo de rescate
    public void asignarEquipo(EquipoRescate e) {
        if (e != null) {
            equiposDeRecaste.add(e);
            System.out.println("Equipo asignado a " + nombre + ": " + e.getNombre());
        }
    }

    // Actualizar nivel de afectación
    public void actualizarNivelAfectacion(NivelDeAfectacion n) {
        this.nivelAfectacion = n;
        System.out.println("Nivel de afectación de " + nombre + " actualizado a " + n);
    }

    @Override
    public String toString() {
        return "Ubicación: " + nombre + " (" + tipoZona + ", " + nivelAfectacion + ")";
    }

}
