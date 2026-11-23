package co.edu.sena.farmacia.persistencia;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import co.edu.sena.farmacia.util.ConexionBaseDatos;

/**
 * Capa de Persistencia de los enlaces de recuperacion de contrasena.
 * Opera sobre TblRecuperacionClave.
 */
public class RecuperacionClaveDao {

    private static final String SQL_INSERTAR =
            "INSERT INTO TblRecuperacionClave (Rec_CodigoHash, Rec_FechaExpira, Rec_Usado, "
            + "TblUsu_UsuIdUsuario) VALUES (?, ?, 0, ?)";

    private static final String SQL_INVALIDAR_ANTERIORES =
            "UPDATE TblRecuperacionClave SET Rec_Usado = 1 "
            + "WHERE TblUsu_UsuIdUsuario = ? AND Rec_Usado = 0";

    private static final String SQL_BUSCAR_VIGENTE =
            "SELECT TblUsu_UsuIdUsuario FROM TblRecuperacionClave "
            + "WHERE Rec_CodigoHash = ? AND Rec_Usado = 0 AND Rec_FechaExpira > ?";

    private static final String SQL_MARCAR_USADO =
            "UPDATE TblRecuperacionClave SET Rec_Usado = 1 WHERE Rec_CodigoHash = ?";

    /**
     * Registra un enlace nuevo y deja sin efecto los que el usuario tuviera
     * pendientes, para que solo el ultimo enlace solicitado sea valido.
     *
     * @param idUsuario usuario que olvido su contrasena
     * @param codigoHash resumen SHA-256 del codigo entregado
     * @param expira momento en que el enlace deja de servir
     */
    public void insertar(int idUsuario, String codigoHash, LocalDateTime expira) {
        try (Connection conexion = ConexionBaseDatos.obtenerConexion()) {

            try (PreparedStatement anular = conexion.prepareStatement(SQL_INVALIDAR_ANTERIORES)) {
                anular.setInt(1, idUsuario);
                anular.executeUpdate();
            }

            try (PreparedStatement sentencia = conexion.prepareStatement(SQL_INSERTAR)) {
                sentencia.setString(1, codigoHash);
                sentencia.setTimestamp(2, Timestamp.valueOf(expira));
                sentencia.setInt(3, idUsuario);
                sentencia.executeUpdate();
            }

        } catch (SQLException error) {
            throw new ExcepcionPersistencia("No fue posible generar el enlace de recuperacion.", error);
        }
    }

    /**
     * Busca el usuario dueno de un enlace que siga vigente.
     *
     * @param codigoHash resumen SHA-256 del codigo recibido
     * @return identificador del usuario, o 0 si el enlace no existe, ya se
     *         uso o esta vencido
     */
    public int buscarUsuarioDeEnlaceVigente(String codigoHash) {
        try (Connection conexion = ConexionBaseDatos.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(SQL_BUSCAR_VIGENTE)) {

            sentencia.setString(1, codigoHash);
            sentencia.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));

            try (ResultSet resultado = sentencia.executeQuery()) {
                return resultado.next() ? resultado.getInt(1) : 0;
            }

        } catch (SQLException error) {
            throw new ExcepcionPersistencia("No fue posible verificar el enlace de recuperacion.", error);
        }
    }

    /**
     * Marca el enlace como usado para que no se pueda reutilizar.
     *
     * @param codigoHash resumen SHA-256 del codigo
     */
    public void marcarUsado(String codigoHash) {
        try (Connection conexion = ConexionBaseDatos.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(SQL_MARCAR_USADO)) {

            sentencia.setString(1, codigoHash);
            sentencia.executeUpdate();

        } catch (SQLException error) {
            throw new ExcepcionPersistencia("No fue posible cerrar el enlace de recuperacion.", error);
        }
    }
}
