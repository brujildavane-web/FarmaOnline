package co.edu.sena.farmacia.modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import co.edu.sena.farmacia.util.FormatoMoneda;

/**
 * Representa la tabla TblPedido: el encabezado de una compra realizada por
 * un cliente y atendida por un farmaceutico.
 */
public class Pedido {

    /** Impuesto al valor agregado vigente en Colombia. */
    public static final BigDecimal IVA_COLOMBIA = new BigDecimal("0.19");

    private int idPedido;
    private LocalDateTime fecha;
    private BigDecimal total;
    private String estado;
    private int idCliente;
    private int idFarmaceutico;

    /** Datos obtenidos por union para mostrarlos en la interfaz. */
    private String nombreCliente;
    private String nombreFarmaceutico;

    public Pedido() {
    }

    public Pedido(int idPedido, LocalDateTime fecha, BigDecimal total, String estado) {
        this.idPedido = idPedido;
        this.fecha = fecha;
        this.total = total;
        this.estado = estado;
    }

    /**
     * Calcula el valor del impuesto que viene incluido en el total.
     *
     * El total del pedido ya trae el IVA sumado, de modo que el impuesto no
     * se obtiene multiplicando el total por la tarifa: hay que extraerlo. Con
     * una tarifa del 19 por ciento, el impuesto contenido en un valor con IVA
     * equivale a total por 19 dividido 119.
     *
     * @return valor del IVA contenido en el total registrado
     */
    public BigDecimal calcularValorImpuesto() {
        if (total == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal divisor = BigDecimal.ONE.add(IVA_COLOMBIA);
        BigDecimal sinImpuesto = total.divide(divisor, 2, java.math.RoundingMode.HALF_UP);

        return total.subtract(sinImpuesto).setScale(2, java.math.RoundingMode.HALF_UP);
    }

    public int getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(int idPedido) {
        this.idPedido = idPedido;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public int getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }

    public int getIdFarmaceutico() {
        return idFarmaceutico;
    }

    public void setIdFarmaceutico(int idFarmaceutico) {
        this.idFarmaceutico = idFarmaceutico;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public String getNombreFarmaceutico() {
        return nombreFarmaceutico;
    }

    public void setNombreFarmaceutico(String nombreFarmaceutico) {
        this.nombreFarmaceutico = nombreFarmaceutico;
    }

    /** Total del pedido con separador de miles. */
    public String getTotalTexto() {
        return FormatoMoneda.enPesos(total);
    }

    /** Valor del impuesto con separador de miles. */
    public String getValorImpuestoTexto() {
        return FormatoMoneda.enPesos(calcularValorImpuesto());
    }

    /**
     * Total en pesos enteros, sin separadores ni centavos.
     *
     * Se usa para llenar los campos de formulario de tipo number, que solo
     * admiten un numero plano: si se les pone un separador de miles el
     * navegador lo interpreta como parte decimal.
     *
     * @return total redondeado a pesos, por ejemplo 55216
     */
    public BigDecimal getTotalEntero() {
        if (total == null) {
            return BigDecimal.ZERO;
        }
        return total.setScale(0, java.math.RoundingMode.HALF_UP);
    }

}
