<%--
    Gestion que el cliente hace de uno de sus pedidos.

    Permite cambiar cantidades, retirar productos y cancelar, mientras el
    pedido siga en estado PENDIENTE. Cuando ya tiene un pago registrado se
    muestra la advertencia sobre la devolucion del dinero.
--%>
<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="tituloPagina" value="Pedido ${pedido.idPedido}"/>
<c:set var="seccionActiva" value="mipedido"/>
<%@ include file="/WEB-INF/jsp/tienda/cabecera-tienda.jsp" %>

<c:set var="tienePago" value="${not empty pagos}"/>

<p class="descripcion-modulo">
    <a href="${raiz}/micuenta">Mi cuenta</a> &rsaquo; Pedido ${pedido.idPedido}
</p>

<h1 class="titulo-modulo">Pedido ${pedido.idPedido}</h1>
<p class="descripcion-modulo">
    Realizado el ${pedido.fecha} &mdash; atiende
    <c:out value="${pedido.nombreFarmaceutico}"/>
</p>

<%-- Aviso segun el estado del pedido --%>
<c:choose>
    <c:when test="${pedido.estado eq 'ANULADO'}">
        <div class="alerta alerta-error" role="status">
            <strong>Este pedido está cancelado.</strong>
            Se conserva en su historial para dejar constancia.
        </div>
    </c:when>
    <c:when test="${modificable}">
        <div class="alerta alerta-advertencia" role="status">
            <strong>Pedido pendiente de revisión.</strong>
            Mientras un farmacéutico no lo confirme, usted puede cambiar las
            cantidades, retirar productos o cancelarlo.
        </div>
    </c:when>
    <c:otherwise>
        <div class="alerta alerta-exito" role="status">
            Este pedido está en estado <strong>${pedido.estado}</strong>, así que
            ya no se puede modificar. Si necesita un cambio, comuníquese con la
            farmacia.
        </div>
    </c:otherwise>
</c:choose>

<%-- Advertencia sobre el dinero cuando el pedido ya tiene pago --%>
<c:if test="${tienePago and modificable}">
    <div class="campo-rol">
        <strong>* Este pedido ya tiene un pago registrado.</strong>
        <span class="ayuda">
            Si modifica o cancela el pedido, tenga en cuenta que
            <strong>si su pago fue por PSE el reembolso dependerá de la entidad
            bancaria</strong> y de sus tiempos de respuesta. Si escogió
            contraentrega, no se le cobrará nada porque el dinero se recibe al
            momento de la entrega. Cualquier diferencia de valor se ajusta con la
            farmacia antes del despacho.
        </span>
    </div>
</c:if>

<div class="columnas-carrito">

    <div class="tarjeta">
        <h2>Productos del pedido</h2>

        <c:choose>
            <c:when test="${empty lineas}">
                <p class="sin-registros">Este pedido no tiene productos.</p>
            </c:when>
            <c:otherwise>
                <div class="contenedor-tabla">
                    <table class="tabla-datos">
                        <caption>
                            <c:choose>
                                <c:when test="${modificable}">
                                    Escriba la cantidad nueva y pulse Actualizar.
                                    El total se recalcula solo.
                                </c:when>
                                <c:otherwise>
                                    Detalle del pedido.
                                </c:otherwise>
                            </c:choose>
                        </caption>
                        <thead>
                            <tr>
                                <th scope="col">Producto</th>
                                <th scope="col">Precio</th>
                                <th scope="col">Cantidad</th>
                                <th scope="col">Subtotal</th>
                                <c:if test="${modificable}">
                                    <th scope="col">Retirar</th>
                                </c:if>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="linea" items="${lineas}">
                                <tr>
                                    <td>
                                        <a href="${raiz}/producto?id=${linea.idProducto}">
                                            <c:out value="${linea.nombreProducto}"/></a>
                                    </td>
                                    <td>$${linea.precioTexto}</td>

                                    <td>
                                        <c:choose>
                                            <c:when test="${modificable}">
                                                <form action="${raiz}/mipedido" method="post"
                                                      class="control-cantidad">
                                                    <input type="hidden" name="accion"
                                                           value="actualizar">
                                                    <input type="hidden" name="idPedido"
                                                           value="${pedido.idPedido}">
                                                    <input type="hidden" name="idLinea"
                                                           value="${linea.idLinea}">
                                                    <label class="oculto"
                                                           for="cant-${linea.idLinea}">
                                                        Cantidad de ${linea.nombreProducto}</label>
                                                    <input type="number" id="cant-${linea.idLinea}"
                                                           name="cantidad" min="1" step="1"
                                                           value="${linea.cantidadTexto}">
                                                    <button type="submit" class="boton-pequeno">
                                                        Actualizar</button>
                                                </form>
                                            </c:when>
                                            <c:otherwise>${linea.cantidadTexto}</c:otherwise>
                                        </c:choose>
                                    </td>

                                    <td>$${linea.subtotalTexto}</td>

                                    <c:if test="${modificable}">
                                        <td>
                                            <form action="${raiz}/mipedido" method="post">
                                                <input type="hidden" name="accion" value="quitar">
                                                <input type="hidden" name="idPedido"
                                                       value="${pedido.idPedido}">
                                                <input type="hidden" name="idLinea"
                                                       value="${linea.idLinea}">
                                                <button type="submit"
                                                        class="boton-peligro boton-pequeno"
                                                        onclick="return confirm('¿Retirar este producto del pedido?');">
                                                    Retirar</button>
                                            </form>
                                        </td>
                                    </c:if>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:otherwise>
        </c:choose>

        <c:if test="${modificable}">
            <div class="acciones-formulario">
                <form action="${raiz}/mipedido" method="post">
                    <input type="hidden" name="accion" value="cancelar">
                    <input type="hidden" name="idPedido" value="${pedido.idPedido}">
                    <button type="submit" class="boton-peligro"
                            onclick="return confirm('¿Cancelar el pedido ${pedido.idPedido}? Esta acción no se puede deshacer.');">
                        Cancelar el pedido<c:if test="${tienePago}"> *</c:if>
                    </button>
                </form>

                <a class="boton boton-secundario" href="${raiz}/catalogo">Seguir comprando</a>
            </div>
        </c:if>
    </div>

    <div class="resumen-compra">
        <h2 style="margin-top:0;">Resumen</h2>

        <div class="linea-resumen">
            <span>Estado del pedido</span>
            <span>
                <span class="etiqueta ${pedido.estado eq 'ANULADO'
                        ? 'etiqueta-agotado' : 'etiqueta-advertencia'}">
                    ${pedido.estado}
                </span>
            </span>
        </div>
        <div class="linea-resumen total">
            <span>Total</span>
            <span>$${pedido.totalTexto}</span>
        </div>

        <h2 style="margin-top:22px;">Pago</h2>

        <c:choose>
            <c:when test="${empty pagos}">
                <p class="sin-registros">Este pedido no tiene pago registrado.</p>
                <c:if test="${modificable}">
                    <div class="acciones-formulario">
                        <a class="boton" href="${raiz}/pago?idPedido=${pedido.idPedido}"
                           style="width:100%; text-align:center;">Pagar ahora</a>
                    </div>
                </c:if>
            </c:when>
            <c:otherwise>
                <c:forEach var="pago" items="${pagos}">
                    <div class="linea-resumen">
                        <span><c:out value="${pago.nombreProveedor}"/></span>
                        <span>$${pago.valorTexto}</span>
                    </div>
                    <div class="linea-resumen">
                        <span>Estado del pago</span>
                        <span>
                            <span class="etiqueta ${pago.aprobada
                                    ? 'etiqueta-disponible' : 'etiqueta-advertencia'}">
                                <c:out value="${pago.estadoTransaccion}"/>
                            </span>
                        </span>
                    </div>
                </c:forEach>
            </c:otherwise>
        </c:choose>
    </div>

</div>

<%@ include file="/WEB-INF/jsp/tienda/pie-tienda.jsp" %>
