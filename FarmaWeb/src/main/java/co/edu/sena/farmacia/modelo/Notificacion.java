package co.edu.sena.farmacia.modelo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Representa la tabla TblNotificacion: un aviso que el sistema deja a un
 * usuario cuando ocurre algo que le concierne, como un cambio de estado en
 * su pedido o el cambio de su contrasena.
 */
public class Notificacion {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private int idNotificacion;
    private String mensaje;
    private LocalDateTime fecha;
    private boolean leida;
    private int idUsuario;

    public int getIdNotificacion() {
        return idNotificacion;
    }

    public void setIdNotificacion(int idNotificacion) {
        this.idNotificacion = idNotificacion;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public boolean isLeida() {
        return leida;
    }

    public void setLeida(boolean leida) {
        this.leida = leida;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    /**
     * Entrega la fecha lista para mostrar, porque las paginas JSP no dan
     * formato a los objetos LocalDateTime por si solas.
     *
     * @return fecha con el formato dd/MM/yyyy HH:mm
     */
    public String getFechaTexto() {
        return fecha == null ? "" : fecha.format(FORMATO);
    }
}
