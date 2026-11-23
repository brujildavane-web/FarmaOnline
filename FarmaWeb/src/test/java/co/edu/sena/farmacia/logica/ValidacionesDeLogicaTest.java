package co.edu.sena.farmacia.logica;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.sena.farmacia.modelo.Cliente;
import co.edu.sena.farmacia.modelo.ModuloSistema;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

/**
 * Caja blanca: reglas de validacion de la capa de logica que se resuelven
 * antes de consultar la base de datos, por lo que no necesitan MySQL.
 */
class ValidacionesDeLogicaTest {

    private final ServicioRegistro registro = new ServicioRegistro();
    private final ServicioRecuperacion recuperacion = new ServicioRecuperacion();
    private final ServicioUsuario usuarios = new ServicioUsuario();
    private final ServicioPedido pedidos = new ServicioPedido();

    private Cliente clienteValido() {
        Cliente c = new Cliente();
        c.setNombre("Ana");
        c.setApellido("Perez");
        c.setEmail("ana@correo.com");
        return c;
    }

    private String mensaje(Runnable accion) {
        return assertThrows(ExcepcionNegocio.class, accion::run).getMessage();
    }

    // ---- Registro
    @Test
    void elRegistroExigeElNombre() {
        Cliente c = clienteValido();
        c.setNombre(" ");
        assertEquals("El nombre es obligatorio.",
                mensaje(() -> registro.registrarCliente(c, "abc123", "abc123")));
    }

    @Test
    void elRegistroExigeUnCorreoConFormatoValido() {
        Cliente c = clienteValido();
        c.setEmail("sin-arroba");
        assertEquals("El correo electronico no tiene un formato valido.",
                mensaje(() -> registro.registrarCliente(c, "abc123", "abc123")));
    }

    @Test
    void elRegistroRechazaUnaFechaDeNacimientoFutura() {
        Cliente c = clienteValido();
        c.setFechaNacimiento(LocalDate.now().plusDays(1));
        assertEquals("La fecha de nacimiento no puede ser posterior a hoy.",
                mensaje(() -> registro.registrarCliente(c, "abc123", "abc123")));
    }

    @Test
    void elRegistroExigeSeisCaracteresEnLaContrasena() {
        assertEquals("La contrasena debe tener al menos 6 caracteres.",
                mensaje(() -> registro.registrarCliente(clienteValido(), "abc", "abc")));
    }

    @Test
    void elRegistroExigeQueLasContrasenasCoincidan() {
        assertEquals("Las dos contrasenas no coinciden.",
                mensaje(() -> registro.registrarCliente(clienteValido(), "abc123", "abc124")));
    }

    // ---- Recuperacion de contrasena
    @Test
    void recuperarExigeElCorreo() {
        assertEquals("Debe digitar su correo electronico.", mensaje(() -> recuperacion.solicitar("")));
    }

    @Test
    void recuperarRechazaUnCorreoSinFormato() {
        assertEquals("El correo electronico no tiene un formato valido.",
                mensaje(() -> recuperacion.solicitar("correo")));
    }

    @Test
    void restablecerExigeUnEnlace() {
        assertEquals("El enlace de recuperacion no es valido.",
                mensaje(() -> recuperacion.restablecer("", "abc123", "abc123")));
    }

    @Test
    void restablecerExigeLaContrasenaNueva() {
        assertEquals("La contrasena nueva es obligatoria.",
                mensaje(() -> recuperacion.restablecer("codigo", "", "")));
    }

    @Test
    void restablecerExigeLongitudMinima() {
        assertEquals("La contrasena debe tener al menos 6 caracteres.",
                mensaje(() -> recuperacion.restablecer("codigo", "abc", "abc")));
    }

    @Test
    void restablecerExigeQueCoincidan() {
        assertEquals("Las dos contrasenas no coinciden.",
                mensaje(() -> recuperacion.restablecer("codigo", "abc123", "xyz789")));
    }

    @Test
    void unEnlaceVacioNuncaEsVigente() {
        assertFalse(recuperacion.esEnlaceVigente(""));
        assertFalse(recuperacion.esEnlaceVigente(null));
    }

    // ---- Inicio de sesion
    @Test
    void ingresarExigeCorreoYContrasena() {
        assertEquals("Debe digitar su correo y su contrasena.",
                mensaje(() -> usuarios.autenticar("", "")));
        assertEquals("Debe digitar su correo y su contrasena.",
                mensaje(() -> usuarios.autenticar("a@b.com", " ")));
    }

    // ---- Estados del pedido
    @Test
    void elEstadoDelPedidoEsObligatorio() {
        assertEquals("Debe indicar el estado del pedido.",
                mensaje(() -> pedidos.cambiarEstado(1, "")));
    }

    @Test
    void unEstadoInexistenteSeRechaza() {
        assertEquals("El estado volando no es valido para un pedido.",
                mensaje(() -> pedidos.cambiarEstado(1, "volando")));
    }

    @Test
    void loscincoEstadosDelCicloDeVidaEstanDefinidos() {
        assertEquals(5, ServicioPedido.ESTADOS_VALIDOS.size());
        assertTrue(ServicioPedido.ESTADOS_VALIDOS.contains("ANULADO"));
    }

    // ---- Control de acceso por rol
    @Test
    void soloElAdministradorAccedeAlModuloDeUsuarios() {
        ModuloSistema modulo = CatalogoModulos.buscarPorRuta("/usuarios");
        assertTrue(modulo.tieneAcceso("Administrador"));
        assertFalse(modulo.tieneAcceso("Farmaceutico"));
        assertFalse(modulo.tieneAcceso("Cliente"));
    }
}
