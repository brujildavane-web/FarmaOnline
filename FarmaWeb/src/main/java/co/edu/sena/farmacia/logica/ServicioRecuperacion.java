package co.edu.sena.farmacia.logica;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;

import co.edu.sena.farmacia.modelo.Usuario;
import co.edu.sena.farmacia.persistencia.RecuperacionClaveDao;
import co.edu.sena.farmacia.persistencia.UsuarioDao;
import co.edu.sena.farmacia.util.ConversorDatos;
import co.edu.sena.farmacia.util.EncriptadorPassword;

/**
 * Capa de Logica de la recuperacion de contrasena.
 *
 * Flujo: la persona escribe su correo, el sistema genera un codigo aleatorio
 * de un solo uso con vigencia de 30 minutos y lo entrega por correo; con ese
 * codigo la persona define una contrasena nueva.
 *
 * En la base de datos solo se guarda el resumen SHA-256 del codigo, igual que
 * las contrasenas: quien lea la tabla no puede usar los enlaces pendientes.
 *
 * Simulacion declarada: el proyecto no tiene servidor de correo, de modo que
 * el enlace no se envia; la pantalla lo muestra como lo haria el mensaje.
 */
public class ServicioRecuperacion {

    /** Minutos de vigencia del enlace. */
    public static final int MINUTOS_VIGENCIA = 30;

    private static final int LONGITUD_MINIMA_PASSWORD = 6;

    private final UsuarioDao usuarioDao = new UsuarioDao();
    private final RecuperacionClaveDao recuperacionDao = new RecuperacionClaveDao();
    private final ServicioNotificacion servicioNotificacion = new ServicioNotificacion();
    private final SecureRandom azar = new SecureRandom();

    /**
     * Genera un enlace de recuperacion para el correo indicado.
     *
     * @param email correo digitado por la persona
     * @return el codigo para armar el enlace, o null si el correo no esta
     *         registrado. El llamador no debe revelar esa diferencia a la
     *         persona, para no confirmar que correos existen.
     */
    public String solicitar(String email) {
        if (ConversorDatos.estaVacio(email)) {
            throw new ExcepcionNegocio("Debe digitar su correo electronico.");
        }
        if (!email.contains("@") || !email.contains(".")) {
            throw new ExcepcionNegocio("El correo electronico no tiene un formato valido.");
        }

        Usuario usuario = usuarioDao.buscarPorEmail(email.trim());
        if (usuario == null) {
            return null;
        }

        byte[] aleatorio = new byte[32];
        azar.nextBytes(aleatorio);
        String codigo = HexFormat.of().formatHex(aleatorio);

        recuperacionDao.insertar(usuario.getIdUsuario(),
                EncriptadorPassword.generarHash(codigo),
                LocalDateTime.now().plusMinutes(MINUTOS_VIGENCIA));

        return codigo;
    }

    /**
     * Comprueba que el codigo del enlace exista, no se haya usado y no haya
     * vencido.
     *
     * @param codigo codigo recibido en la direccion
     * @return true si todavia se puede usar
     */
    public boolean esEnlaceVigente(String codigo) {
        if (ConversorDatos.estaVacio(codigo)) {
            return false;
        }
        return recuperacionDao.buscarUsuarioDeEnlaceVigente(
                EncriptadorPassword.generarHash(codigo.trim())) > 0;
    }

    /**
     * Cambia la contrasena usando el codigo del enlace.
     *
     * @param codigo codigo recibido en la direccion
     * @param nueva contrasena nueva
     * @param confirmacion repeticion de la contrasena nueva
     */
    public void restablecer(String codigo, String nueva, String confirmacion) {
        if (ConversorDatos.estaVacio(codigo)) {
            throw new ExcepcionNegocio("El enlace de recuperacion no es valido.");
        }
        if (ConversorDatos.estaVacio(nueva)) {
            throw new ExcepcionNegocio("La contrasena nueva es obligatoria.");
        }
        if (nueva.trim().length() < LONGITUD_MINIMA_PASSWORD) {
            throw new ExcepcionNegocio("La contrasena debe tener al menos "
                    + LONGITUD_MINIMA_PASSWORD + " caracteres.");
        }
        if (!nueva.equals(confirmacion)) {
            throw new ExcepcionNegocio("Las dos contrasenas no coinciden.");
        }

        String hash = EncriptadorPassword.generarHash(codigo.trim());
        int idUsuario = recuperacionDao.buscarUsuarioDeEnlaceVigente(hash);

        if (idUsuario == 0) {
            throw new ExcepcionNegocio(
                    "El enlace de recuperacion ya se uso o vencio. Solicite uno nuevo.");
        }

        usuarioDao.actualizarPassword(idUsuario, EncriptadorPassword.generarHash(nueva));
        recuperacionDao.marcarUsado(hash);

        servicioNotificacion.notificarUsuario(idUsuario,
                "Su contrasena fue restablecida. Si no fue usted, comuniquese con la farmacia.");
    }
}
