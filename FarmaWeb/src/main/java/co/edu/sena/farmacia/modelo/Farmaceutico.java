package co.edu.sena.farmacia.modelo;

/**
 * Representa la tabla TblFarmaceutico: el profesional autorizado para
 * validar formulas medicas y confirmar la entrega de medicamentos.
 *
 * Hereda de Persona y agrega el registro profesional exigido por la
 * normativa sanitaria.
 */
public class Farmaceutico extends Persona {

    private int idFarmaceutico;
    private String registroProfesional;
    private String especialidad;
    private int idUsuario;

    /** Correo del usuario asociado, obtenido por union para la interfaz. */
    private String emailUsuario;

    public Farmaceutico() {
        super();
    }

    public Farmaceutico(int idFarmaceutico, String nombre, String apellido, String registroProfesional) {
        super(nombre, apellido, null);
        this.idFarmaceutico = idFarmaceutico;
        this.registroProfesional = registroProfesional;
    }

    public int getIdFarmaceutico() {
        return idFarmaceutico;
    }

    public void setIdFarmaceutico(int idFarmaceutico) {
        this.idFarmaceutico = idFarmaceutico;
    }

    public String getRegistroProfesional() {
        return registroProfesional;
    }

    public void setRegistroProfesional(String registroProfesional) {
        this.registroProfesional = registroProfesional;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(String especialidad) {
        this.especialidad = especialidad;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getEmailUsuario() {
        return emailUsuario;
    }

    public void setEmailUsuario(String emailUsuario) {
        this.emailUsuario = emailUsuario;
    }
}
