package co.edu.sena.farmacia.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Caja blanca: cifrado SHA-256 y comparacion de contrasenas. */
class EncriptadorPasswordTest {

    @Test
    void generaHashHexadecimalDe64Caracteres() {
        String hash = EncriptadorPassword.generarHash("admin123");
        assertEquals(64, hash.length());
        assertTrue(hash.matches("[0-9a-f]+"));
    }

    @Test
    void coincideConElHashDelScriptDeLaBaseDeDatos() {
        // Hash de admin123 cargado por bd_farmacia_completa.sql
        assertTrue(EncriptadorPassword.coincide("admin123",
                "240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9"));
    }

    @Test
    void rechazaUnaContrasenaDistinta() {
        String hash = EncriptadorPassword.generarHash("admin123");
        assertFalse(EncriptadorPassword.coincide("admin124", hash));
    }

    @Test
    void dosContrasenasDistintasGeneranHashDistinto() {
        assertNotEquals(EncriptadorPassword.generarHash("clave1"),
                EncriptadorPassword.generarHash("clave2"));
    }

    @Test
    void valoresNulosNuncaCoinciden() {
        assertFalse(EncriptadorPassword.coincide(null, "abc"));
        assertFalse(EncriptadorPassword.coincide("abc", null));
    }

    @Test
    void noAceptaUnaContrasenaNulaAlCifrar() {
        assertThrows(IllegalArgumentException.class, () -> EncriptadorPassword.generarHash(null));
    }
}
