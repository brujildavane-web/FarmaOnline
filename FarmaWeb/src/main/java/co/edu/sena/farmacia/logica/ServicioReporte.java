package co.edu.sena.farmacia.logica;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import co.edu.sena.farmacia.modelo.FilaReporte;
import co.edu.sena.farmacia.persistencia.ReporteDao;

/**
 * Capa de Logica de los reportes del panel administrativo.
 *
 * Responde a dos necesidades del administrador: analizar las ventas del
 * negocio y controlar el inventario. Define los umbrales con que se decide
 * que un producto esta escaso o que un lote esta por vencer, y calcula los
 * indicadores derivados, como el ticket promedio.
 */
public class ServicioReporte {

    /** Existencias con que un producto se considera escaso. */
    public static final int UMBRAL_BAJO_STOCK = 60;

    /** Dias de anticipacion con que se alerta el vencimiento de un lote. */
    public static final int DIAS_ALERTA_VENCIMIENTO = 60;

    /** Cuantos productos se muestran en el reporte de mas vendidos. */
    public static final int CUANTOS_MAS_VENDIDOS = 10;

    private final ReporteDao reporteDao = new ReporteDao();

    // -------------------------------------------------------------- VENTAS

    /**
     * Cuenta los pedidos que representan una venta efectiva.
     *
     * @return cantidad de pedidos no anulados
     */
    public int contarPedidosVendidos() {
        return (Integer) reporteDao.consultarIndicadoresDeVentas()[0];
    }

    /**
     * Suma el valor facturado.
     *
     * @return total vendido, sin contar los pedidos anulados
     */
    public BigDecimal calcularTotalFacturado() {
        return (BigDecimal) reporteDao.consultarIndicadoresDeVentas()[1];
    }

    /**
     * Calcula el valor promedio de cada pedido.
     *
     * Es el indicador que le dice al administrador cuanto gasta un cliente
     * en promedio por compra.
     *
     * @return valor facturado dividido entre la cantidad de pedidos
     */
    public BigDecimal calcularTicketPromedio() {
        Object[] indicadores = reporteDao.consultarIndicadoresDeVentas();
        int pedidos = (Integer) indicadores[0];
        BigDecimal total = (BigDecimal) indicadores[1];

        if (pedidos == 0) {
            return BigDecimal.ZERO;
        }
        return total.divide(BigDecimal.valueOf(pedidos), 2, RoundingMode.HALF_UP);
    }

    /**
     * Suma las unidades despachadas.
     *
     * @return cantidad de unidades vendidas
     */
    public double contarUnidadesVendidas() {
        return reporteDao.consultarUnidadesVendidas();
    }

    /**
     * Agrupa los pedidos por estado, para ver cuantos estan pendientes de
     * atender y cuantos se anularon.
     *
     * @return pedidos y valor en cada estado
     */
    public List<FilaReporte> consultarVentasPorEstado() {
        return reporteDao.consultarVentasPorEstado();
    }

    /**
     * Muestra la evolucion de las ventas mes a mes.
     *
     * @return pedidos y valor facturado por mes
     */
    public List<FilaReporte> consultarVentasPorMes() {
        return reporteDao.consultarVentasPorMes();
    }

    /**
     * Identifica los productos de mayor rotacion.
     *
     * @return productos ordenados por unidades vendidas
     */
    public List<FilaReporte> consultarProductosMasVendidos() {
        return reporteDao.consultarProductosMasVendidos(CUANTOS_MAS_VENDIDOS);
    }

    /**
     * Muestra en que secciones del catalogo se concentra la venta.
     *
     * @return unidades y valor vendido por categoria
     */
    public List<FilaReporte> consultarVentasPorCategoria() {
        return reporteDao.consultarVentasPorCategoria();
    }

    /**
     * Muestra como paga la gente y cuanto esta pendiente de recaudo.
     *
     * @return cantidad y valor por medio de pago y estado
     */
    public List<FilaReporte> consultarPagosPorMedio() {
        return reporteDao.consultarPagosPorMedio();
    }

    // ---------------------------------------------------------- INVENTARIO

    /**
     * Calcula cuanto dinero hay inmovilizado en existencias.
     *
     * @return valor del inventario vigente a precio de venta
     */
    public BigDecimal calcularValorInventario() {
        return reporteDao.consultarValorInventario();
    }

    /**
     * Agrupa las existencias por categoria del catalogo.
     *
     * @return unidades y valor del inventario por categoria
     */
    public List<FilaReporte> consultarStockPorCategoria() {
        return reporteDao.consultarStockPorCategoria();
    }

    /**
     * Lista los productos que necesitan reposicion.
     *
     * @return productos con existencias en el umbral o por debajo
     */
    public List<FilaReporte> consultarProductosBajoStock() {
        return reporteDao.consultarProductosBajoStock(UMBRAL_BAJO_STOCK);
    }

    /**
     * Lista los lotes vencidos y los que estan por vencer.
     *
     * Es el reporte que evita que la farmacia se quede con mercancia que ya
     * no puede dispensar.
     *
     * @return lotes que exigen atencion, del mas urgente al menos urgente
     */
    public List<FilaReporte> consultarLotesCriticos() {
        return reporteDao.consultarLotesCriticos(DIAS_ALERTA_VENCIMIENTO);
    }

    /**
     * Cuenta los productos agotados, es decir sin existencias vigentes.
     *
     * @return cantidad de productos que no se pueden vender
     */
    public int contarProductosAgotados() {
        int agotados = 0;

        for (FilaReporte fila : reporteDao.consultarProductosBajoStock(0)) {
            if (fila.getCantidad() <= 0) {
                agotados++;
            }
        }
        return agotados;
    }
}
