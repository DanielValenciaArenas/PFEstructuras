package co.edu.uniquindio.estructuraDeDatos;

import java.time.LocalDate;

public class RecursoAlimento extends Recurso{
    private LocalDate fechaVencimiento;

    public RecursoAlimento(String idRecurso, String nombre, int cantidad, Ubicacion ubicacion, LocalDate fechaVencimiento) {
        super(idRecurso, nombre, cantidad, ubicacion);
        this.fechaVencimiento = fechaVencimiento;
    }

    //Getters y Setters
    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(LocalDate fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }


}
