package co.edu.sena.farmacia.modelo;

import java.math.BigDecimal;

import co.edu.sena.farmacia.util.FormatoMoneda;

/**
 * Renglon de un reporte.
 *
 * No corresponde a ninguna tabla: es el resultado de una consulta de
 * agrupacion, con una etiqueta que describe la fila, una cantidad y un valor
 * en pesos. El campo detalle lleva la informacion adicional que cada reporte
 * necesite, como los dias que le faltan a un lote para vencer.
 */
public class FilaReporte {

    private String etiqueta;
    private double cantidad;
    private BigDecimal valor;
    private String detalle;

    public FilaReporte() {
    }

    public FilaReporte(String etiqueta, double cantidad, BigDecimal valor) {
        this.etiqueta = etiqueta;
        this.cantidad = cantidad;
        this.valor = valor;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public void setEtiqueta(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public double getCantidad() {
        return cantidad;
    }

    public void setCantidad(double cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public String getDetalle() {
        return detalle;
    }

    public void setDetalle(String detalle) {
        this.detalle = detalle;
    }

    /** Cantidad sin decimales, para mostrarla en la tabla. */
    public String getCantidadTexto() {
        return FormatoMoneda.enUnidades(cantidad);
    }

    /** Valor con separador de miles, para mostrarlo en la tabla. */
    public String getValorTexto() {
        return FormatoMoneda.enPesos(valor);
    }
}
