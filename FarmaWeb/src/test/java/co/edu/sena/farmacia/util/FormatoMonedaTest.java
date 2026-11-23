package co.edu.sena.farmacia.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

/** Caja blanca: formato de los montos en pesos colombianos. */
class FormatoMonedaTest {

    @Test
    void usaPuntoComoSeparadorDeMiles() {
        assertEquals("24.200", FormatoMoneda.enPesos(new BigDecimal("24200")));
    }

    @Test
    void redondeaSinDecimales() {
        assertEquals("8.500", FormatoMoneda.enPesos(new BigDecimal("8499.60")));
    }

    @Test
    void unValorNuloSeMuestraComoCero() {
        assertEquals("0", FormatoMoneda.enPesos(null));
    }

    @Test
    void lasUnidadesSeMuestranSinDecimales() {
        assertEquals("190", FormatoMoneda.enUnidades(190));
    }
}
