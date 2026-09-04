package co.edu.sena.farmacia.controlador;

import java.io.IOException;

import co.edu.sena.farmacia.logica.ServicioReporte;
import co.edu.sena.farmacia.modelo.Usuario;
import co.edu.sena.farmacia.util.ConversorDatos;
import co.edu.sena.farmacia.util.FormatoMoneda;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Controlador de los reportes del panel administrativo.
 *
 * Atiende dos necesidades del administrador: analizar las ventas del negocio
 * y controlar el inventario. El reporte que se muestra se escoge con el
 * parametro tipo, que viaja por el metodo GET, de modo que cada reporte tiene
 * su propia direccion y se puede compartir o guardar.
 *
 * A diferencia de los demas modulos de gestion, este es exclusivo del rol
 * Administrador: el farmaceutico atiende pedidos y formulas, no analiza el
 * negocio.
 */
@WebServlet(name = "ServletReporte", urlPatterns = {"/reportes"})
public class ServletReporte extends ServletBase {

    private static final long serialVersionUID = 1L;

    private static final String VISTA = "reportes.jsp";

    private final transient ServicioReporte servicioReporte = new ServicioReporte();

    @Override
    protected void doGet(HttpServletRequest peticion, HttpServletResponse respuesta)
            throws ServletException, IOException {

        Usuario usuario = obtenerUsuario(peticion);

        if (usuario == null || !usuario.esAdministrador()) {
            publicarError(peticion, "Los reportes del negocio son exclusivos del rol "
                    + "Administrador.");
            redirigirAlModulo(peticion, respuesta, "panel");
            return;
        }

        String tipo = ConversorDatos.limpiar(peticion.getParameter("tipo"));
        boolean esInventario = "inventario".equalsIgnoreCase(tipo);
        peticion.setAttribute("tipoReporte", esInventario ? "inventario" : "ventas");

        try {
            if (esInventario) {
                cargarReporteDeInventario(peticion);
            } else {
                cargarReporteDeVentas(peticion);
            }

        } catch (RuntimeException error) {
            publicarErrorDeExcepcion(peticion, error);
        }

        reenviarAVista(peticion, respuesta, VISTA);
    }

    /**
     * Prepara los datos del reporte de ventas.
     *
     * @param peticion peticion HTTP en curso
     */
    private void cargarReporteDeVentas(HttpServletRequest peticion) {
        peticion.setAttribute("pedidosVendidos", servicioReporte.contarPedidosVendidos());
        peticion.setAttribute("totalFacturadoTexto",
                FormatoMoneda.enPesos(servicioReporte.calcularTotalFacturado()));
        peticion.setAttribute("ticketPromedioTexto",
                FormatoMoneda.enPesos(servicioReporte.calcularTicketPromedio()));
        peticion.setAttribute("unidadesVendidasTexto",
                FormatoMoneda.enUnidades(servicioReporte.contarUnidadesVendidas()));

        peticion.setAttribute("ventasPorEstado", servicioReporte.consultarVentasPorEstado());
        peticion.setAttribute("ventasPorMes", servicioReporte.consultarVentasPorMes());
        peticion.setAttribute("masVendidos", servicioReporte.consultarProductosMasVendidos());
        peticion.setAttribute("ventasPorCategoria", servicioReporte.consultarVentasPorCategoria());
        peticion.setAttribute("pagosPorMedio", servicioReporte.consultarPagosPorMedio());
    }

    /**
     * Prepara los datos del reporte de inventario.
     *
     * @param peticion peticion HTTP en curso
     */
    private void cargarReporteDeInventario(HttpServletRequest peticion) {
        peticion.setAttribute("valorInventarioTexto",
                FormatoMoneda.enPesos(servicioReporte.calcularValorInventario()));
        peticion.setAttribute("productosAgotados", servicioReporte.contarProductosAgotados());
        peticion.setAttribute("umbralBajoStock", ServicioReporte.UMBRAL_BAJO_STOCK);
        peticion.setAttribute("diasAlerta", ServicioReporte.DIAS_ALERTA_VENCIMIENTO);

        peticion.setAttribute("stockPorCategoria", servicioReporte.consultarStockPorCategoria());
        peticion.setAttribute("bajoStock", servicioReporte.consultarProductosBajoStock());
        peticion.setAttribute("lotesCriticos", servicioReporte.consultarLotesCriticos());
    }
}
