package co.edu.sena.farmacia.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

/**
 * Da presentacion a los valores en pesos colombianos.
 *
 * El formato se aplica en Java y no con la etiqueta fmt:formatNumber de las
 * paginas JSP, por dos razones: el comportamiento de esa etiqueta depende de
 * la version de la biblioteca JSTL que resuelva el contenedor, y de esta
 * forma el criterio de presentacion del dinero queda en un solo lugar.
 *
 * Convencion colombiana: punto como separador de miles y sin decimales,
 * porque los precios de la farmacia se manejan en pesos enteros.
 */
public final class FormatoMoneda {

    private static final String PATRON = "#,##0";

    private FormatoMoneda() {
    }

    /**
     * Convierte un valor monetario en el texto que se muestra en pantalla.
     *
     * @param valor cantidad a formatear; se admite nulo
     * @return texto con separador de miles, por ejemplo 24.200
     */
    public static String enPesos(BigDecimal valor) {
        if (valor == null) {
            return "0";
        }

        DecimalFormatSymbols simbolos = new DecimalFormatSymbols();
        simbolos.setGroupingSeparator('.');
        simbolos.setDecimalSeparator(',');

        DecimalFormat formato = new DecimalFormat(PATRON, simbolos);
        return formato.format(valor.setScale(0, RoundingMode.HALF_UP));
    }

    /**
     * Formatea una cantidad de unidades de inventario.
     *
     * El stock se almacena como numero flotante, pero se dispensa en
     * unidades enteras, de modo que se muestra sin decimales.
     *
     * @param cantidad unidades a formatear
     * @return texto sin decimales, por ejemplo 190
     */
    public static String enUnidades(double cantidad) {
        DecimalFormatSymbols simbolos = new DecimalFormatSymbols();
        simbolos.setGroupingSeparator('.');

        DecimalFormat formato = new DecimalFormat(PATRON, simbolos);
        return formato.format(cantidad);
    }
}
