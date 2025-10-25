package co.edu.uniquindio.estructuraDeDatos;

public class RecursoMedicina extends Recurso{
    private String tipoMedicamento;

    public RecursoMedicina(String idRecurso, String nombre, int cantidad, Ubicacion ubicacion, String tipoMedicamento) {
        super(idRecurso, nombre, cantidad, ubicacion);
        this.tipoMedicamento = tipoMedicamento;
    }

    //Getters y Setters
    public String getTipoMedicamento() { return tipoMedicamento; }
    public void setTipoMedicamento(String tipoMedicamento) { this.tipoMedicamento = tipoMedicamento; }

}
