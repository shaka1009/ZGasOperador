package zgas.operador.models;

public class Registro {

    public String getNumNomina() {
        return numNomina;
    }

    public zgas.operador.models.Registro setNumNomina(String numNomina) {
        this.numNomina = numNomina;
        return this;
    }

    private String numNomina;

    public String getTelefono() {
        return telefono;
    }

    public zgas.operador.models.Registro setTelefono(String telefono) {
        this.telefono = telefono;
        return this;
    }

    private String telefono;

    private String nombre;

    public String getNombre() {
        return nombre;
    }

    public zgas.operador.models.Registro setNombre(String nombre) {
        this.nombre = nombre;
        return this;
    }

    public String getApellido() {
        return apellido;
    }

    public zgas.operador.models.Registro setApellido(String apellido) {
        this.apellido = apellido;
        return this;
    }

    private String apellido;
}
