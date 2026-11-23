package co.edu.sena.farmacia.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.sena.farmacia.logica.ExcepcionNegocio;
import org.junit.jupiter.api.Test;

/** Caja blanca: validacion y conversion de los datos de los formularios. */
class ConversorDatosTest {

    @Test
    void detectaTextosVacios() {
        assertTrue(ConversorDatos.estaVacio(null));
        assertTrue(ConversorDatos.estaVacio(""));
        assertTrue(ConversorDatos.estaVacio("   "));
        assertFalse(ConversorDatos.estaVacio("a"));
    }

    @Test
    void convierteEnterosValidos() {
        assertEquals(15, ConversorDatos.aEntero(" 15 ", "cantidad"));
    }

    @Test
    void rechazaEnterosInvalidosConUnMensajeClaro() {
        ExcepcionNegocio error = assertThrows(ExcepcionNegocio.class,
                () -> ConversorDatos.aEntero("abc", "cantidad"));
        assertEquals("El campo cantidad debe ser un numero entero valido.", error.getMessage());
    }

    @Test
    void rechazaEnterosNulos() {
        assertThrows(ExcepcionNegocio.class, () -> ConversorDatos.aEntero(null, "cantidad"));
    }

    @Test
    void elEnteroOpcionalVacioDevuelveNulo() {
        assertNull(ConversorDatos.aEnteroOpcional("", "cantidad"));
    }
}
