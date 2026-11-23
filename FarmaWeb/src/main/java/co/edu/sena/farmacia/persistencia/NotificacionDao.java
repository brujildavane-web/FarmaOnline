package co.edu.sena.farmacia.persistencia;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import co.edu.sena.farmacia.modelo.Notificacion;
import co.edu.sena.farmacia.util.ConexionBaseDatos;

/**
 * Capa de Persistencia de las notificaciones. Opera sobre TblNotificacion.
 */
public class NotificacionDao {

    private static final String SQL_INSERTAR =
            "INSERT INTO TblNotificacion (Not_Mensaje, Not_Fecha, Not_Leida, TblUsu_UsuIdUsuario) "
            + "VALUES (?, ?, 0, ?)";

    private static final String SQL_LISTAR_POR_USUARIO =
            "SELECT Not_IdNotificacion, Not_Mensaje, Not_Fecha, Not_Leida, TblUsu_UsuIdUsuario "
            + "FROM TblNotificacion WHERE TblUsu_UsuIdUsuario = ? "
            + "ORDER BY Not_Fecha DESC, Not_IdNotificacion DESC LIMIT 50";

    private static final String SQL_CONTAR_SIN_LEER =
            "SELECT COUNT(*) FROM TblNotificacion WHERE TblUsu_UsuIdUsuario = ? AND Not_Leida = 0";

    private static final String SQL_MARCAR_LEIDAS =
            "UPDATE TblNotificacion SET Not_Leida = 1 WHERE TblUsu_UsuIdUsuario = ? AND Not_Leida = 0";

    /**
     * Guarda una notificacion nueva, sin leer, para un usuario.
     *
     * @param idUsuario destinatario del aviso
     * @param mensaje texto del aviso
     */
    public void insertar(int idUsuario, String mensaje) {
        try (Connection conexion = ConexionBaseDatos.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(SQL_INSERTAR)) {

            sentencia.setString(1, mensaje);
            sentencia.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            sentencia.setInt(3, idUsuario);
            sentencia.executeUpdate();

        } catch (SQLException error) {
            throw new ExcepcionPersistencia("No fue posible registrar la notificacion.", error);
        }
    }

    /**
     * Lista las ultimas notificaciones de un usuario, la mas reciente primero.
     *
     * @param idUsuario propietario de las notificaciones
     * @return hasta cincuenta notificaciones
     */
    public List<Notificacion> listarPorUsuario(int idUsuario) {
        List<Notificacion> lista = new ArrayList<>();

        try (Connection conexion = ConexionBaseDatos.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(SQL_LISTAR_POR_USUARIO)) {

            sentencia.setInt(1, idUsuario);

            try (ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) {
                    Notificacion notificacion = new Notificacion();
                    notificacion.setIdNotificacion(resultado.getInt("Not_IdNotificacion"));
                    notificacion.setMensaje(resultado.getString("Not_Mensaje"));
                    notificacion.setFecha(resultado.getTimestamp("Not_Fecha").toLocalDateTime());
                    notificacion.setLeida(resultado.getBoolean("Not_Leida"));
                    notificacion.setIdUsuario(resultado.getInt("TblUsu_UsuIdUsuario"));
                    lista.add(notificacion);
                }
            }

        } catch (SQLException error) {
            throw new ExcepcionPersistencia("No fue posible consultar las notificaciones.", error);
        }
        return lista;
    }

    /**
     * Cuenta las notificaciones que el usuario todavia no ha leido.
     *
     * @param idUsuario propietario de las notificaciones
     * @return cantidad sin leer
     */
    public int contarSinLeer(int idUsuario) {
        try (Connection conexion = ConexionBaseDatos.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(SQL_CONTAR_SIN_LEER)) {

            sentencia.setInt(1, idUsuario);

            try (ResultSet resultado = sentencia.executeQuery()) {
                return resultado.next() ? resultado.getInt(1) : 0;
            }

        } catch (SQLException error) {
            throw new ExcepcionPersistencia("No fue posible contar las notificaciones.", error);
        }
    }

    /**
     * Marca como leidas todas las notificaciones del usuario.
     *
     * @param idUsuario propietario de las notificaciones
     */
    public void marcarTodasLeidas(int idUsuario) {
        try (Connection conexion = ConexionBaseDatos.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(SQL_MARCAR_LEIDAS)) {

            sentencia.setInt(1, idUsuario);
            sentencia.executeUpdate();

        } catch (SQLException error) {
            throw new ExcepcionPersistencia("No fue posible marcar las notificaciones.", error);
        }
    }
}
