package co.edu.sena.farmacia.logica;

import java.time.LocalDate;

import co.edu.sena.farmacia.modelo.Cliente;
import co.edu.sena.farmacia.modelo.Rol;
import co.edu.sena.farmacia.modelo.Usuario;
import co.edu.sena.farmacia.persistencia.ClienteDao;
import co.edu.sena.farmacia.persistencia.RolDao;
import co.edu.sena.farmacia.persistencia.UsuarioDao;
import co.edu.sena.farmacia.util.ConversorDatos;
import co.edu.sena.farmacia.util.EncriptadorPassword;

/**
 * Capa de Logica del registro de clientes en la tienda publica.
 *
 * Es el unico camino por el que una persona obtiene una cuenta sin
 * intervencion del administrador, y siempre recibe el rol Cliente. Los roles
 * de Farmaceutico y Administrador se asignan exclusivamente desde el modulo
 * de Usuarios del panel de gestion.
 */
public class ServicioRegistro {

    /** Nombre del rol que se asigna a quien se registra por cuenta propia. */
    public static final String ROL_CLIENTE = "Cliente";

    /** Longitud minima exigida a la contrasena. */
    private static final int LONGITUD_MINIMA_PASSWORD = 6;

    private final UsuarioDao usuarioDao = new UsuarioDao();
    private final ClienteDao clienteDao = new ClienteDao();
    private final RolDao rolDao = new RolDao();

    /**
     * Crea la cuenta de un cliente nuevo.
     *
     * La operacion abarca dos tablas: primero TblUsuario con las credenciales
     * y el rol Cliente, y despues TblCliente con los datos de contacto, que
     * exige el identificador del usuario recien creado.
     *
     * @param cliente datos personales digitados en el formulario
     * @param passwordPlano contrasena escogida por la persona
     * @param passwordConfirmacion repeticion de la contrasena
     * @return el usuario creado, listo para abrir la sesion
     */
    public Usuario registrarCliente(Cliente cliente, String passwordPlano,
            String passwordConfirmacion) {

        validarDatosPersonales(cliente);
        validarPassword(passwordPlano, passwordConfirmacion);
        verificarCorreoDisponible(cliente.getEmail());

        Usuario usuario = new Usuario();
        usuario.setEmail(cliente.getEmail().trim());
        usuario.setPassword(EncriptadorPassword.generarHash(passwordPlano));
        usuario.setFechaRegistro(LocalDate.now());
        usuario.setIdRol(obtenerIdRolCliente());

        usuarioDao.insertar(usuario);

        Usuario creado = usuarioDao.buscarPorEmail(usuario.getEmail());
        if (creado == null) {
            throw new ExcepcionNegocio("La cuenta se creo pero no fue posible recuperarla.");
        }

        cliente.setIdUsuario(creado.getIdUsuario());
        cliente.setEdad(cliente.calcularEdadActual());
        clienteDao.insertar(cliente);

        new ServicioNotificacion().notificarUsuario(creado.getIdUsuario(),
                "Bienvenido a Farmacia en Linea. Su cuenta fue creada con el rol Cliente.");

        return creado;
    }

    /**
     * Ubica la fila del cliente que corresponde a un usuario autenticado.
     *
     * @param idUsuario identificador del usuario en sesion
     * @return el cliente encontrado, o null si el usuario no es un cliente
     */
    public Cliente consultarClienteDeUsuario(int idUsuario) {
        for (Cliente cliente : clienteDao.listar()) {
            if (cliente.getIdUsuario() == idUsuario) {
                return cliente;
            }
        }
        return null;
    }

    /**
     * Comprueba los datos minimos que exige la tabla TblCliente.
     */
    private void validarDatosPersonales(Cliente cliente) {
        if (ConversorDatos.estaVacio(cliente.getNombre())) {
            throw new ExcepcionNegocio("El nombre es obligatorio.");
        }
        if (ConversorDatos.estaVacio(cliente.getApellido())) {
            throw new ExcepcionNegocio("El apellido es obligatorio.");
        }
        if (ConversorDatos.estaVacio(cliente.getEmail())) {
            throw new ExcepcionNegocio("El correo electronico es obligatorio.");
        }
        if (!cliente.getEmail().contains("@") || !cliente.getEmail().contains(".")) {
            throw new ExcepcionNegocio("El correo electronico no tiene un formato valido.");
        }
        if (cliente.getFechaNacimiento() != null
                && cliente.getFechaNacimiento().isAfter(LocalDate.now())) {
            throw new ExcepcionNegocio("La fecha de nacimiento no puede ser posterior a hoy.");
        }
    }

    /**
     * Comprueba la contrasena y que su repeticion coincida.
     */
    private void validarPassword(String passwordPlano, String passwordConfirmacion) {
        if (ConversorDatos.estaVacio(passwordPlano)) {
            throw new ExcepcionNegocio("La contrasena es obligatoria.");
        }
        if (passwordPlano.trim().length() < LONGITUD_MINIMA_PASSWORD) {
            throw new ExcepcionNegocio("La contrasena debe tener al menos "
                    + LONGITUD_MINIMA_PASSWORD + " caracteres.");
        }
        if (!passwordPlano.equals(passwordConfirmacion)) {
            throw new ExcepcionNegocio("Las dos contrasenas no coinciden.");
        }
    }

    /**
     * Impide registrar dos cuentas con el mismo correo.
     */
    private void verificarCorreoDisponible(String email) {
        if (usuarioDao.buscarPorEmail(email.trim()) != null) {
            throw new ExcepcionNegocio("El correo " + email
                    + " ya tiene una cuenta. Ingrese con su contrasena.");
        }
    }

    /**
     * Busca el identificador del rol Cliente en la tabla TblRol.
     *
     * @return identificador del rol Cliente
     */
    private int obtenerIdRolCliente() {
        for (Rol rol : rolDao.listar()) {
            if (ROL_CLIENTE.equalsIgnoreCase(rol.getNombreRol())) {
                return rol.getIdRol();
            }
        }
        throw new ExcepcionNegocio("El rol Cliente no esta configurado en el sistema. "
                + "Ejecute el script bd_farmacia_completa.sql.");
    }
}
