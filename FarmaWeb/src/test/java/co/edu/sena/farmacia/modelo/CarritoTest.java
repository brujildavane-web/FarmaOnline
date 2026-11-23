package co.edu.sena.farmacia.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Caja blanca: calculos y reglas del carrito de compras. */
class CarritoTest {

    private Carrito carrito;
    private Producto acetaminofen;
    private Producto ibuprofeno;

    @BeforeEach
    void preparar() {
        carrito = new Carrito();
        acetaminofen = new Producto(1, "Acetaminofen 500mg", new BigDecimal("8500"));
        ibuprofeno = new Producto(2, "Ibuprofeno 400mg", new BigDecimal("6200"));
    }

    @Test
    void unCarritoNuevoEstaVacio() {
        assertTrue(carrito.estaVacio());
        assertEquals(0, carrito.calcularTotal().compareTo(BigDecimal.ZERO));
    }

    @Test
    void agregarElMismoProductoAcumulaLaCantidad() {
        carrito.agregar(acetaminofen, 1);
        carrito.agregar(acetaminofen, 2);
        assertEquals(1, carrito.getCantidadRenglones());
        assertEquals(3, carrito.buscarItem(1).getCantidad());
    }

    @Test
    void calculaSubtotalIvaYTotal() {
        carrito.agregar(acetaminofen, 2);   // 17.000
        carrito.agregar(ibuprofeno, 1);     //  6.200 -> 23.200
        assertEquals(0, carrito.calcularSubtotal().compareTo(new BigDecimal("23200")));
        assertEquals(0, carrito.calcularImpuesto().compareTo(new BigDecimal("4408")));
        assertEquals(0, carrito.calcularTotal().compareTo(new BigDecimal("27608")));
    }

    @Test
    void cambiarLaCantidadACeroQuitaElRenglon() {
        carrito.agregar(acetaminofen, 2);
        carrito.cambiarCantidad(1, 0);
        assertTrue(carrito.estaVacio());
    }

    @Test
    void quitarUnProductoRecalculaElTotal() {
        carrito.agregar(acetaminofen, 1);
        carrito.agregar(ibuprofeno, 1);
        carrito.quitar(1);
        assertNull(carrito.buscarItem(1));
        assertEquals(0, carrito.calcularSubtotal().compareTo(new BigDecimal("6200")));
    }

    @Test
    void unMedicamentoControladoSinFormulaMarcaRecetasPendientes() {
        Producto controlado = new Producto(3, "Clonazepam", new BigDecimal("20000"));
        controlado.setRequiereReceta(true);
        carrito.agregar(controlado, 1);
        assertTrue(carrito.requiereFormulaMedica());
        assertTrue(carrito.faltanRecetas());
        assertEquals(1, carrito.obtenerItemsSinReceta().size());
    }

    @Test
    void unProductoLibreNoExigeFormula() {
        carrito.agregar(acetaminofen, 1);
        assertFalse(carrito.requiereFormulaMedica());
        assertFalse(carrito.faltanRecetas());
    }

    @Test
    void vaciarDejaElCarritoSinRenglones() {
        carrito.agregar(acetaminofen, 1);
        carrito.vaciar();
        assertTrue(carrito.estaVacio());
    }
}
