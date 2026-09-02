<%--
    Carrito de compras.

    El carrito vive en la sesion HTTP, no en la base de datos, porque
    TblPedido exige un cliente y un farmaceutico que todavia no se conocen
    mientras la persona solo esta mirando el catalogo.

    Todas las acciones que modifican el carrito se envian por POST.
--%>
<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="tituloPagina" value="Mi carrito"/>
<c:set var="seccionActiva" value="carrito"/>
<%@ include file="/WEB-INF/jsp/tienda/cabecera-tienda.jsp" %>

<h1 class="titulo-modulo">Mi carrito</h1>

<c:choose>
    <c:when test="${carrito.vacio}">
        <p class="descripcion-modulo">Su carrito está vacío.</p>

        <div class="tarjeta">
            <h2>Todavía no ha agregado productos</h2>
            <p>
                Recorra el catálogo y agregue lo que necesite. No hace falta
                tener cuenta para armar el carrito.
            </p>
            <div class="acciones-formulario">
                <a class="boton" href="${raiz}/catalogo">Ir al catálogo</a>
            </div>
        </div>
    </c:when>

    <c:otherwise>
        <p class="descripcion-modulo">
            ${carrito.cantidadRenglones} producto(s) distintos.
            El carrito se conserva mientras dure su visita, aunque no tenga cuenta.
        </p>

        <div class="columnas-carrito">

            <div class="tarjeta">
                <h2>Productos seleccionados</h2>

                <div class="contenedor-tabla">
                    <table class="tabla-datos">
                        <caption>
                            Para cambiar una cantidad escriba el número nuevo y pulse Actualizar.
                        </caption>
                        <thead>
                            <tr>
                                <th scope="col">Producto</th>
                                <th scope="col">Precio</th>
                                <th scope="col">Cantidad</th>
                                <th scope="col">Subtotal</th>
                                <th scope="col">Quitar</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="item" items="${carrito.items}">
                                <tr>
                                    <td>
                                        <a href="${raiz}/producto?id=${item.idProducto}">
                                            <c:out value="${item.nombre}"/></a>
                                        <br>
                                        <span class="ayuda">
                                            <c:out value="${item.skuCode}"/>
                                            <c:choose>
                                                <c:when test="${item.requiereReceta}">
                                                    &middot; requiere receta
                                                </c:when>
                                                <c:otherwise>&middot; venta libre</c:otherwise>
                                            </c:choose>
                                        </span>
                                    </td>

                                    <td>$${item.precioTexto}</td>

                                    <td>
                                        <form action="${raiz}/carrito" method="post"
                                              class="control-cantidad">
                                            <input type="hidden" name="accion" value="actualizar">
                                            <input type="hidden" name="idProducto"
                                                   value="${item.idProducto}">
                                            <label class="oculto"
                                                   for="cantidad-${item.idProducto}">
                                                Cantidad de ${item.nombre}</label>
                                            <input type="number" id="cantidad-${item.idProducto}"
                                                   name="cantidad" min="1" step="1"
                                                   value="${item.cantidadTexto}">
                                            <button type="submit" class="boton-pequeno">Actualizar</button>
                                        </form>
                                    </td>

                                    <td>$${item.subtotalTexto}</td>

                                    <td>
                                        <form action="${raiz}/carrito" method="post">
                                            <input type="hidden" name="accion" value="quitar">
                                            <input type="hidden" name="idProducto"
                                                   value="${item.idProducto}">
                                            <button type="submit"
                                                    class="boton-peligro boton-pequeno">Quitar</button>
                                        </form>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>

                <div class="acciones-formulario">
                    <a class="boton boton-secundario" href="${raiz}/catalogo">Seguir comprando</a>

                    <form action="${raiz}/carrito" method="post">
                        <input type="hidden" name="accion" value="vaciar">
                        <button type="submit" class="boton-secundario">Vaciar carrito</button>
                    </form>
                </div>
            </div>

            <div class="resumen-compra">
                <h2 style="margin-top:0;">Resumen</h2>

                <div class="linea-resumen">
                    <span>Subtotal</span>
                    <span>$${carrito.subtotalTexto}</span>
                </div>
                <div class="linea-resumen">
                    <span>IVA 19 %</span>
                    <span>$${carrito.impuestoTexto}</span>
                </div>
                <div class="linea-resumen total">
                    <span>Total</span>
                    <span>$${carrito.totalTexto}</span>
                </div>

                <c:if test="${carrito.conFormulaMedica}">
                    <div class="alerta alerta-advertencia" role="alert" style="margin-top:16px;">
                        Su carrito tiene medicamentos controlados. Deberá adjuntar la
                        fórmula médica para que el farmacéutico autorice el despacho.
                    </div>
                </c:if>

                <c:choose>
                    <c:when test="${empty sessionScope.usuarioSesion}">
                        <div class="acciones-formulario" style="margin-top:18px;">
                            <a class="boton" href="${raiz}/registro"
                               style="width:100%; text-align:center;">Continuar con la compra</a>
                        </div>

                        <div class="aviso-invitado" style="margin-top:18px; margin-bottom:0;">
                            Para confirmar el pedido necesita una cuenta. Al registrarse se le
                            asignará automáticamente el rol
                            <span class="insignia-rol">Cliente</span>.
                            <br><br>
                            ¿Ya tiene cuenta? <a href="${raiz}/login">Ingrese aquí</a>
                            y su carrito se conserva.
                        </div>
                    </c:when>

                    <c:otherwise>
                        <form action="${raiz}/carrito" method="post" style="margin-top:18px;">
                            <input type="hidden" name="accion" value="confirmar">
                            <button type="submit" style="width:100%;">Confirmar mi pedido</button>
                        </form>

                        <p class="ayuda" style="margin-top:12px;">
                            El pedido quedará en estado PENDIENTE hasta que un farmacéutico
                            lo revise y lo confirme.
                        </p>
                    </c:otherwise>
                </c:choose>
            </div>

        </div>
    </c:otherwise>
</c:choose>

<%@ include file="/WEB-INF/jsp/tienda/pie-tienda.jsp" %>
