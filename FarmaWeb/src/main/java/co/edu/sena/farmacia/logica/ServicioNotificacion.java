package co.edu.sena.farmacia.logica;

import java.util.List;

import co.edu.sena.farmacia.modelo.Cliente;
import co.edu.sena.farmacia.modelo.Notificacion;
import co.edu.sena.farmacia.persistencia.ClienteDao;
import co.edu.sena.farmacia.persistencia.NotificacionDao;

/**
 * Capa de Logica de las notificaciones al usuario.
 *
 * Los avisos son un complemento: si por cualquier razon no se pueden
 * guardar, la operacion principal (crear el pedido, cambiar su estado,
 * cambiar la contrasena) no debe fallar. Por eso los metodos que generan
 * avisos absorben sus propios errores.
 */
public class ServicioNotificacion {

    private final NotificacionDao notificacionDao = new NotificacionDao();
    private final ClienteDao clienteDao = new ClienteDao();

    /**
     * Deja un aviso a un usuario.
     *
     * @param idUsuario destinatario
     * @param mensaje texto del aviso
     */
    public void notificarUsuario(int idUsuario, String mensaje) {
        if (idUsuario <= 0 || mensaje == null || mensaje.isBlank()) {
            return;
        }
        try {
            String texto = mensaje.length() > 255 ? mensaje.substring(0, 255) : mensaje;
            notificacionDao.insertar(idUsuario, texto);
        } catch (RuntimeException error) {
            // El aviso es secundario: nunca debe tumbar la operacion principal.
        }
    }

    /**
     * Deja un aviso al usuario que corresponde a un cliente.
     *
     * @param idCliente cliente destinatario
     * @param mensaje texto del aviso
     */
    public void notificarCliente(int idCliente, String mensaje) {
        try {
            Cliente cliente = clienteDao.buscarPorId(idCliente);
            if (cliente != null) {
                notificarUsuario(cliente.getIdUsuario(), mensaje);
            }
        } catch (RuntimeException error) {
            // Ver notificarUsuario: el aviso no debe interrumpir el proceso.
        }
    }

    /**
     * Consulta las notificaciones de un usuario.
     *
     * @param idUsuario propietario
     * @return las ultimas notificaciones, la mas reciente primero
     */
    public List<Notificacion> consultar(int idUsuario) {
        return notificacionDao.listarPorUsuario(idUsuario);
    }

    /**
     * Cuenta los avisos sin leer, para el indicador de la cabecera.
     *
     * @param idUsuario propietario
     * @return cantidad sin leer, 0 si no se puede consultar
     */
    public int contarSinLeer(int idUsuario) {
        try {
            return notificacionDao.contarSinLeer(idUsuario);
        } catch (RuntimeException error) {
            return 0;
        }
    }

    /**
     * Marca todos los avisos del usuario como leidos.
     *
     * @param idUsuario propietario
     */
    public void marcarTodasLeidas(int idUsuario) {
        notificacionDao.marcarTodasLeidas(idUsuario);
    }
}
