package co.edu.sena.farmacia.persistencia;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import co.edu.sena.farmacia.modelo.FilaReporte;
import co.edu.sena.farmacia.util.ConexionBaseDatos;

/**
 * Capa de Persistencia de los reportes del panel administrativo.
 *
 * Reune las consultas de agrupacion que alimentan los reportes de ventas y
 * de inventario. Se resuelven con SQL y no recorriendo listas en Java,
 * porque agrupar y sumar es justamente lo que el motor de base de datos hace
 * mejor.
 *
 * Los pedidos anulados quedan por fuera de las cifras de venta: cuentan como
 * historia del negocio, no como ingreso.
 */
public class ReporteDao {

    // -------------------------------------------------------------- VENTAS

    private static final String SQL_INDICADORES_VENTAS =
            "SELECT COUNT(*) AS Pedidos, COALESCE(SUM(Ped_Total), 0) AS Total "
            + "FROM TblPedido WHERE Ped_Estado <> 'ANULADO'";

    private static final String SQL_UNIDADES_VENDIDAS =
            "SELECT COALESCE(SUM(l.Lin_Cantidad), 0) FROM TblLineaPedido l "
            + "INNER JOIN TblPedido p ON p.Ped_IdPedido = l.TblPed_PedIdPedido "
            + "WHERE p.Ped_Estado <> 'ANULADO'";

    private static final String SQL_VENTAS_POR_ESTADO =
            "SELECT Ped_Estado AS Etiqueta, COUNT(*) AS Cantidad, "
            + "COALESCE(SUM(Ped_Total), 0) AS Valor "
            + "FROM TblPedido GROUP BY Ped_Estado ORDER BY Valor DESC";

    private static final String SQL_VENTAS_POR_MES =
            "SELECT DATE_FORMAT(p.Ped_Fecha, '%Y-%m') AS Etiqueta, COUNT(*) AS Cantidad, "
            + "COALESCE(SUM(p.Ped_Total), 0) AS Valor "
            + "FROM TblPedido p WHERE p.Ped_Estado <> 'ANULADO' "
            + "GROUP BY Etiqueta ORDER BY Etiqueta DESC";

    private static final String SQL_PRODUCTOS_MAS_VENDIDOS =
            "SELECT pr.Pro_Nombre AS Etiqueta, SUM(l.Lin_Cantidad) AS Cantidad, "
            + "SUM(l.Lin_Subtotal) AS Valor "
            + "FROM TblLineaPedido l "
            + "INNER JOIN TblProducto pr ON pr.Pro_IdProducto = l.TblPro_ProIdProducto "
            + "INNER JOIN TblPedido p ON p.Ped_IdPedido = l.TblPed_PedIdPedido "
            + "WHERE p.Ped_Estado <> 'ANULADO' "
            + "GROUP BY pr.Pro_IdProducto, pr.Pro_Nombre "
            + "ORDER BY Cantidad DESC LIMIT ?";

    private static final String SQL_VENTAS_POR_CATEGORIA =
            "SELECT COALESCE(pr.Pro_Categoria, 'Sin clasificar') AS Etiqueta, "
            + "SUM(l.Lin_Cantidad) AS Cantidad, SUM(l.Lin_Subtotal) AS Valor "
            + "FROM TblLineaPedido l "
            + "INNER JOIN TblProducto pr ON pr.Pro_IdProducto = l.TblPro_ProIdProducto "
            + "INNER JOIN TblPedido p ON p.Ped_IdPedido = l.TblPed_PedIdPedido "
            + "WHERE p.Ped_Estado <> 'ANULADO' "
            + "GROUP BY Etiqueta ORDER BY Valor DESC";

    private static final String SQL_PAGOS_POR_MEDIO =
            "SELECT pa.Pas_NombreProveedor AS Etiqueta, COUNT(*) AS Cantidad, "
            + "COALESCE(SUM(t.Tran_Valor), 0) AS Valor, t.Tran_EstadoTransaccion AS Detalle "
            + "FROM TblTransaccionPago t "
            + "INNER JOIN TblPasarelaPago pa ON pa.Pas_IdPasarela = t.TblPas_PasIdPasarela "
            + "GROUP BY pa.Pas_NombreProveedor, t.Tran_EstadoTransaccion "
            + "ORDER BY Valor DESC";

    // ---------------------------------------------------------- INVENTARIO

    /** Solo cuentan los lotes vigentes: un lote vencido no se puede vender. */
    private static final String STOCK_VIGENTE =
            "CASE WHEN l.LotP_FechaVencimiento >= CURDATE() THEN l.LotP_StockActual ELSE 0 END";

    private static final String SQL_VALOR_INVENTARIO =
            "SELECT COALESCE(SUM(" + STOCK_VIGENTE + " * p.Pro_Precio), 0) "
            + "FROM TblProducto p "
            + "LEFT JOIN TblLoteProducto l ON l.TblPro_ProIdProducto = p.Pro_IdProducto";

    private static final String SQL_STOCK_POR_CATEGORIA =
            "SELECT COALESCE(p.Pro_Categoria, 'Sin clasificar') AS Etiqueta, "
            + "COALESCE(SUM(" + STOCK_VIGENTE + "), 0) AS Cantidad, "
            + "COALESCE(SUM(" + STOCK_VIGENTE + " * p.Pro_Precio), 0) AS Valor "
            + "FROM TblProducto p "
            + "LEFT JOIN TblLoteProducto l ON l.TblPro_ProIdProducto = p.Pro_IdProducto "
            + "GROUP BY Etiqueta ORDER BY Valor DESC";

    private static final String SQL_PRODUCTOS_BAJO_STOCK =
            "SELECT p.Pro_Nombre AS Etiqueta, "
            + "COALESCE(SUM(" + STOCK_VIGENTE + "), 0) AS Cantidad, "
            + "p.Pro_Precio AS Valor, "
            + "COALESCE(p.Pro_Categoria, 'Sin clasificar') AS Detalle "
            + "FROM TblProducto p "
            + "LEFT JOIN TblLoteProducto l ON l.TblPro_ProIdProducto = p.Pro_IdProducto "
            + "GROUP BY p.Pro_IdProducto, p.Pro_Nombre, p.Pro_Precio, p.Pro_Categoria "
            + "HAVING Cantidad <= ? ORDER BY Cantidad ASC, p.Pro_Nombre";

    private static final String SQL_LOTES_CRITICOS =
            "SELECT CONCAT(p.Pro_Nombre, ' | lote ', l.LotP_RegistroSanitario) AS Etiqueta, "
            + "l.LotP_StockActual AS Cantidad, "
            + "(l.LotP_StockActual * p.Pro_Precio) AS Valor, "
            + "CONCAT(l.LotP_FechaVencimiento, ' | ', "
            + "  CASE WHEN l.LotP_FechaVencimiento < CURDATE() THEN 'VENCIDO' "
            + "       ELSE CONCAT(DATEDIFF(l.LotP_FechaVencimiento, CURDATE()), ' dias') END"
            + ") AS Detalle "
            + "FROM TblLoteProducto l "
            + "INNER JOIN TblProducto p ON p.Pro_IdProducto = l.TblPro_ProIdProducto "
            + "WHERE l.LotP_FechaVencimiento <= DATE_ADD(CURDATE(), INTERVAL ? DAY) "
            + "ORDER BY l.LotP_FechaVencimiento";

    // ------------------------------------------------------------- METODOS

    /**
     * Consulta la cantidad de pedidos y el valor facturado.
     *
     * @return arreglo con la cantidad de pedidos en la posicion cero y el
     *         valor facturado en la posicion uno
     */
    public Object[] consultarIndicadoresDeVentas() {
        try (Connection conexion = ConexionBaseDatos.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(SQL_INDICADORES_VENTAS);
             ResultSet resultado = sentencia.executeQuery()) {

            if (resultado.next()) {
                return new Object[] {resultado.getInt("Pedidos"), resultado.getBigDecimal("Total")};
            }
            return new Object[] {0, BigDecimal.ZERO};

        } catch (SQLException error) {
            throw new ExcepcionPersistencia("No fue posible calcular los indicadores de ventas.", error);
        }
    }

    /**
     * Suma las unidades vendidas en los pedidos no anulados.
     *
     * @return total de unidades despachadas
     */
    public double consultarUnidadesVendidas() {
        return consultarEscalar(SQL_UNIDADES_VENDIDAS,
                "No fue posible calcular las unidades vendidas.").doubleValue();
    }

    /**
     * Calcula el valor del inventario disponible a precio de venta.
     *
     * @return valor de las existencias vigentes
     */
    public BigDecimal consultarValorInventario() {
        return consultarEscalar(SQL_VALOR_INVENTARIO,
                "No fue posible calcular el valor del inventario.");
    }

    /**
     * Agrupa los pedidos por su estado.
     *
     * @return cantidad y valor de los pedidos en cada estado
     */
    public List<FilaReporte> consultarVentasPorEstado() {
        return consultarFilas(SQL_VENTAS_POR_ESTADO, null,
                "No fue posible agrupar las ventas por estado.");
    }

    /**
     * Agrupa las ventas por mes.
     *
     * @return cantidad de pedidos y valor facturado en cada mes
     */
    public List<FilaReporte> consultarVentasPorMes() {
        return consultarFilas(SQL_VENTAS_POR_MES, null,
                "No fue posible agrupar las ventas por mes.");
    }

    /**
     * Consulta los productos con mas unidades despachadas.
     *
     * @param limite cuantos productos devolver
     * @return productos ordenados de mayor a menor rotacion
     */
    public List<FilaReporte> consultarProductosMasVendidos(int limite) {
        return consultarFilas(SQL_PRODUCTOS_MAS_VENDIDOS, limite,
                "No fue posible consultar los productos mas vendidos.");
    }

    /**
     * Agrupa las ventas por categoria del catalogo.
     *
     * @return unidades y valor vendido en cada categoria
     */
    public List<FilaReporte> consultarVentasPorCategoria() {
        return consultarFilas(SQL_VENTAS_POR_CATEGORIA, null,
                "No fue posible agrupar las ventas por categoria.");
    }

    /**
     * Agrupa los pagos por medio y por estado de la transaccion.
     *
     * @return cantidad y valor recaudado en cada medio de pago
     */
    public List<FilaReporte> consultarPagosPorMedio() {
        return consultarFilas(SQL_PAGOS_POR_MEDIO, null,
                "No fue posible agrupar los pagos por medio.");
    }

    /**
     * Agrupa las existencias vigentes por categoria.
     *
     * @return unidades y valor del inventario en cada categoria
     */
    public List<FilaReporte> consultarStockPorCategoria() {
        return consultarFilas(SQL_STOCK_POR_CATEGORIA, null,
                "No fue posible agrupar el inventario por categoria.");
    }

    /**
     * Consulta los productos cuyas existencias estan en el limite o por debajo.
     *
     * @param umbral cantidad minima que se considera aceptable
     * @return productos que requieren reposicion
     */
    public List<FilaReporte> consultarProductosBajoStock(int umbral) {
        return consultarFilas(SQL_PRODUCTOS_BAJO_STOCK, umbral,
                "No fue posible consultar los productos con bajo inventario.");
    }

    /**
     * Consulta los lotes vencidos y los que estan por vencer.
     *
     * @param dias plazo de anticipacion en dias
     * @return lotes que exigen atencion, del mas urgente al menos urgente
     */
    public List<FilaReporte> consultarLotesCriticos(int dias) {
        return consultarFilas(SQL_LOTES_CRITICOS, dias,
                "No fue posible consultar los lotes por vencer.");
    }

    /**
     * Ejecuta una consulta que devuelve un solo valor numerico.
     */
    private BigDecimal consultarEscalar(String sentenciaSql, String mensajeError) {
        try (Connection conexion = ConexionBaseDatos.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sentenciaSql);
             ResultSet resultado = sentencia.executeQuery()) {

            BigDecimal valor = resultado.next() ? resultado.getBigDecimal(1) : null;
            return valor == null ? BigDecimal.ZERO : valor;

        } catch (SQLException error) {
            throw new ExcepcionPersistencia(mensajeError, error);
        }
    }

    /**
     * Ejecuta una consulta de agrupacion y traduce sus filas al modelo.
     *
     * @param sentenciaSql consulta a ejecutar
     * @param filtro valor del unico parametro, o null si la consulta no lleva
     * @param mensajeError texto a mostrar si la consulta falla
     */
    private List<FilaReporte> consultarFilas(String sentenciaSql, Integer filtro,
            String mensajeError) {

        List<FilaReporte> filas = new ArrayList<>();

        try (Connection conexion = ConexionBaseDatos.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sentenciaSql)) {

            if (filtro != null) {
                sentencia.setInt(1, filtro);
            }

            try (ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) {
                    filas.add(convertirFila(resultado));
                }
            }

        } catch (SQLException error) {
            throw new ExcepcionPersistencia(mensajeError, error);
        }
        return filas;
    }

    /**
     * Traduce una fila del ResultSet al modelo del reporte.
     *
     * La columna Detalle es opcional: solo algunos reportes la incluyen.
     */
    private FilaReporte convertirFila(ResultSet resultado) throws SQLException {
        FilaReporte fila = new FilaReporte();
        fila.setEtiqueta(resultado.getString("Etiqueta"));
        fila.setCantidad(resultado.getDouble("Cantidad"));
        fila.setValor(resultado.getBigDecimal("Valor"));

        if (tieneColumna(resultado, "Detalle")) {
            fila.setDetalle(resultado.getString("Detalle"));
        }
        return fila;
    }

    /**
     * Comprueba si el resultado trae una columna con ese nombre.
     */
    private boolean tieneColumna(ResultSet resultado, String nombre) throws SQLException {
        int columnas = resultado.getMetaData().getColumnCount();

        for (int i = 1; i <= columnas; i++) {
            if (nombre.equalsIgnoreCase(resultado.getMetaData().getColumnLabel(i))) {
                return true;
            }
        }
        return false;
    }
}
