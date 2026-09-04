<%--
    Zona privada del cliente.

    Muestra el rol asignado debajo del nombre y da acceso a sus pedidos,
    favoritos y formulas medicas. No incluye el menu de los trece modulos:
    esa zona esta reservada a los perfiles Administrador y Farmaceutico.
--%>
<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="tituloPagina" value="Mi cuenta"/>
<c:set var="seccionActiva" value="micuenta"/>
<%@ include file="/WEB-INF/jsp/tienda/cabecera-tienda.jsp" %>

<div class="ficha-usuario">
    <div class="avatar" aria-hidden="true">
        <%-- Iniciales del cliente, calculadas con expresiones EL --%>
        <c:choose>
            <c:when test="${not empty cliente.nombre and not empty cliente.apellido}">
                ${cliente.nombre.substring(0,1).toUpperCase()}${cliente.apellido.substring(0,1).toUpperCase()}
            </c:when>
            <c:otherwise>?</c:otherwise>
        </c:choose>
    </div>

    <div>
        <h1 style="margin:0 0 4px; font-size:1.3rem;">
            <c:choose>
                <c:when test="${not empty cliente}">
                    <c:out value="${cliente.nombreCompleto}"/>
                </c:when>
                <c:otherwise>Mi cuenta</c:otherwise>
            </c:choose>
        </h1>

        <p style="margin:0 0 6px;" class="ayuda">
            <c:out value="${usuario.email}"/>
        </p>

        <span class="insignia-rol"><c:out value="${usuario.nombreRol}"/></span>
        <span class="ayuda" style="display:block; margin-top:6px;">
            Rol asignado automáticamente al registrarse.
            Solo el administrador puede cambiarlo.
        </span>
    </div>
</div>

<c:if test="${empty cliente}">
    <div class="alerta alerta-advertencia" role="alert">
        Su usuario no tiene una ficha de cliente asociada, por lo que no puede
        confirmar pedidos en la tienda. Comuníquese con la farmacia.
    </div>
</c:if>

<h2>Mi cuenta</h2>
<div class="rejilla-cuenta">

    <a class="opcion-cuenta" href="${raiz}/carrito">
        <strong>Mi carrito</strong>
        <span>
            <c:choose>
                <c:when test="${carrito.vacio}">Sin productos por ahora</c:when>
                <c:otherwise>
                    ${carrito.cantidadRenglones} producto(s) por
                    $${carrito.totalTexto}
                </c:otherwise>
            </c:choose>
        </span>
    </a>

    <a class="opcion-cuenta" href="${raiz}/catalogo">
        <strong>Seguir comprando</strong>
        <span>Recorrer el catálogo por categorías</span>
    </a>

    <a class="opcion-cuenta" href="#mis-pedidos">
        <strong>Mis pedidos</strong>
        <span>${empty misPedidos ? 0 : misPedidos.size()} pedido(s) registrados</span>
    </a>

    <a class="opcion-cuenta" href="#mis-formulas">
        <strong>Mis fórmulas médicas</strong>
        <span>${empty misFormulas ? 0 : misFormulas.size()} prescripción(es)</span>
    </a>

    <a class="opcion-cuenta" href="#mis-favoritos">
        <strong>Mis favoritos</strong>
        <span>${empty misFavoritos ? 0 : misFavoritos.size()} producto(s) marcados</span>
    </a>

</div>

<div class="tarjeta" id="mis-pedidos" style="margin-top:26px;">
    <h2>Mis pedidos</h2>

    <c:choose>
        <c:when test="${empty misPedidos}">
            <p class="sin-registros">
                Todavía no ha realizado pedidos.
                <a href="${raiz}/catalogo">Ver el catálogo</a>
            </p>
        </c:when>
        <c:otherwise>
            <div class="contenedor-tabla">
                <table class="tabla-datos">
                    <caption>Total de pedidos: ${misPedidos.size()}</caption>
                    <thead>
                        <tr>
                            <th scope="col">Pedido</th>
                            <th scope="col">Fecha</th>
                            <th scope="col">Total</th>
                            <th scope="col">Estado</th>
                            <th scope="col">Atiende</th>
                            <th scope="col">Gestión</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="pedido" items="${misPedidos}">
                            <tr>
                                <td>${pedido.idPedido}</td>
                                <td>${pedido.fecha}</td>
                                <td>$${pedido.totalTexto}</td>
                                <td>
                                    <span class="etiqueta ${pedido.estado eq 'ANULADO'
                                            ? 'etiqueta-agotado' : 'etiqueta-disponible'}">
                                        ${pedido.estado}
                                    </span>
                                </td>
                                <td><c:out value="${pedido.nombreFarmaceutico}"/></td>
                                <td>
                                    <div class="columna-acciones">
                                        <a class="boton boton-secundario boton-pequeno"
                                           href="${raiz}/mipedido?id=${pedido.idPedido}">
                                            Ver, editar o cancelar</a>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<div class="tarjeta" id="mis-formulas">
    <h2>Mis fórmulas médicas</h2>

    <c:choose>
        <c:when test="${empty misFormulas}">
            <p class="sin-registros">No tiene fórmulas médicas registradas.</p>
        </c:when>
        <c:otherwise>
            <div class="contenedor-tabla">
                <table class="tabla-datos">
                    <thead>
                        <tr>
                            <th scope="col">Pedido</th>
                            <th scope="col">Medicamento</th>
                            <th scope="col">Prescrita</th>
                            <th scope="col">Vence</th>
                            <th scope="col">Vigencia</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="formula" items="${misFormulas}">
                            <tr>
                                <td>${formula.idPedido}</td>
                                <td><c:out value="${formula.nombreProducto}"/></td>
                                <td>${formula.fechaPrescripcion}</td>
                                <td>${formula.fechaVencimiento}</td>
                                <td>
                                    <span class="etiqueta ${formula.vigencia eq 'VENCIDA'
                                            ? 'etiqueta-agotado' : 'etiqueta-disponible'}">
                                        ${formula.vigencia}
                                    </span>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<div class="tarjeta" id="mis-favoritos">
    <h2>Mis favoritos</h2>

    <c:choose>
        <c:when test="${empty misFavoritos}">
            <p class="sin-registros">No ha marcado productos como favoritos.</p>
        </c:when>
        <c:otherwise>
            <div class="contenedor-tabla">
                <table class="tabla-datos">
                    <thead>
                        <tr>
                            <th scope="col">Producto</th>
                            <th scope="col">Marcado el</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="favorito" items="${misFavoritos}">
                            <tr>
                                <td><c:out value="${favorito.nombreProducto}"/></td>
                                <td>${favorito.fechaMarcacion}</td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<%@ include file="/WEB-INF/jsp/tienda/pie-tienda.jsp" %>
