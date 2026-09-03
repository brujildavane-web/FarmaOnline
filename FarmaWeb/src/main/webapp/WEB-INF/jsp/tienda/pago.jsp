<%--
    Pago del pedido.

    Se muestra despues de confirmar el carrito. Ofrece las formas de pago
    habilitadas y pide el monto con que paga el cliente.

    El pago no reemplaza la validacion del farmaceutico: el pedido sigue en
    estado PENDIENTE hasta que el profesional lo revise.
--%>
<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="tituloPagina" value="Pago del pedido"/>
<c:set var="seccionActiva" value="pago"/>
<%@ include file="/WEB-INF/jsp/tienda/cabecera-tienda.jsp" %>

<h1 class="titulo-modulo">Pago del pedido ${pedido.idPedido}</h1>
<p class="descripcion-modulo">
    Su pedido quedó registrado. Escoja cómo desea pagarlo.
</p>

<div class="alerta alerta-exito" role="status">
    <strong>Pedido ${pedido.idPedido} creado.</strong>
    Quedará en estado <strong>PENDIENTE</strong> hasta que un farmacéutico lo
    revise y lo confirme. Si incluye medicamentos controlados, también validará
    la fórmula médica que adjuntó.
</div>

<c:choose>
    <%-- El pedido ya fue pagado: no se cobra dos veces --%>
    <c:when test="${pagoRegistrado}">
        <div class="tarjeta">
            <h2>Este pedido ya tiene su pago registrado</h2>
            <p>
                No es necesario pagarlo de nuevo. Puede consultar el estado del
                pedido y de su pago desde su cuenta.
            </p>
            <div class="acciones-formulario">
                <a class="boton" href="${raiz}/micuenta">Ir a mi cuenta</a>
                <a class="boton boton-secundario" href="${raiz}/catalogo">Seguir comprando</a>
            </div>
        </div>
    </c:when>

    <c:otherwise>
        <div class="columnas-carrito">

            <div class="tarjeta">
                <h2>Forma de pago</h2>

                <form action="${raiz}/pago" method="post">
                    <input type="hidden" name="idPedido" value="${pedido.idPedido}">

                    <%-- Pago en linea --%>
                    <div class="campo-rol" style="margin-bottom:16px;">
                        <div class="campo-casilla">
                            <input type="radio" id="medio-pse" name="medioDePago"
                                   value="PSE" required>
                            <label for="medio-pse"><strong>PSE</strong> &mdash; pago en línea</label>
                        </div>
                        <span class="ayuda">
                            Se debita de su cuenta bancaria. El pago queda aprobado de
                            inmediato, por el valor exacto del pedido.
                        </span>
                    </div>

                    <%-- Pago contraentrega --%>
                    <div class="aviso-invitado" style="margin-bottom:16px;">
                        <strong>Contraentrega</strong> &mdash; pague al recibir su pedido

                        <div class="campo-casilla" style="margin-top:10px;">
                            <input type="radio" id="medio-efectivo" name="medioDePago"
                                   value="Contraentrega efectivo">
                            <label for="medio-efectivo">Efectivo</label>
                        </div>
                        <span class="ayuda">
                            Indique con cuánto va a pagar y el sistema calcula el cambio.
                        </span>

                        <div class="campo-casilla" style="margin-top:10px;">
                            <input type="radio" id="medio-datafono" name="medioDePago"
                                   value="Contraentrega datafono">
                            <label for="medio-datafono">Datáfono</label>
                        </div>
                        <span class="ayuda">
                            El mensajero lleva el datáfono. Se cobra el valor exacto.
                        </span>
                    </div>

                    <div class="campo">
                        <label for="monto">Monto con que paga</label>
                        <input type="number" id="monto" name="monto" required
                               min="${pedido.total}" step="1"
                               value="${pedido.total}">
                        <span class="ayuda">
                            El pedido vale $${pedido.totalTexto}. Con PSE y con datáfono
                            debe ser el valor exacto; en efectivo puede ser mayor y se le
                            devuelve el cambio.
                        </span>
                    </div>

                    <div class="acciones-formulario">
                        <button type="submit">Registrar el pago</button>
                        <a class="boton boton-secundario" href="${raiz}/micuenta">Pagar después</a>
                    </div>
                </form>
            </div>

            <div class="resumen-compra">
                <h2 style="margin-top:0;">Su pedido</h2>

                <div class="linea-resumen">
                    <span>Pedido</span>
                    <span>${pedido.idPedido}</span>
                </div>
                <div class="linea-resumen">
                    <span>Fecha</span>
                    <span>${pedido.fecha}</span>
                </div>
                <div class="linea-resumen">
                    <span>Estado</span>
                    <span><span class="etiqueta etiqueta-advertencia">${pedido.estado}</span></span>
                </div>
                <div class="linea-resumen">
                    <span>Atiende</span>
                    <span><c:out value="${pedido.nombreFarmaceutico}"/></span>
                </div>
                <div class="linea-resumen total">
                    <span>Total a pagar</span>
                    <span>$${pedido.totalTexto}</span>
                </div>

                <h2 style="margin-top:22px;">Medios habilitados</h2>
                <c:choose>
                    <c:when test="${empty mediosDePago}">
                        <p class="sin-registros">
                            No hay medios de pago habilitados. Pida al administrador que
                            los registre en el módulo de Pasarelas de pago.
                        </p>
                    </c:when>
                    <c:otherwise>
                        <ul>
                            <c:forEach var="medio" items="${mediosDePago}">
                                <li><c:out value="${medio.nombreProveedor}"/></li>
                            </c:forEach>
                        </ul>
                    </c:otherwise>
                </c:choose>
            </div>

        </div>
    </c:otherwise>
</c:choose>

<%@ include file="/WEB-INF/jsp/tienda/pie-tienda.jsp" %>
