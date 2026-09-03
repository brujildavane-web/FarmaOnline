<%--
    Pagina de inicio de la farmacia.

    Es la primera pantalla del sistema y no exige cuenta: cualquier visitante
    puede ver el catalogo y armar su carrito. La cuenta se solicita solo al
    confirmar el pedido.
--%>
<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="tituloPagina" value="Inicio"/>
<c:set var="seccionActiva" value="inicio"/>
<%@ include file="/WEB-INF/jsp/tienda/cabecera-tienda.jsp" %>

<c:if test="${empty sessionScope.usuarioSesion}">
    <div class="aviso-invitado" role="status">
        Está navegando como <span class="insignia-rol insignia-rol-invitado">Visitante</span>
        &nbsp; Puede ver el catálogo y armar su carrito sin registrarse.
        Al finalizar la compra se le pedirá crear una cuenta, que recibirá el rol
        <span class="insignia-rol">Cliente</span>.
    </div>
</c:if>

<section class="banner">
    <h1>Sus medicamentos, con entrega a domicilio</h1>
    <p>
        Consulte la disponibilidad en tiempo real, cargue su fórmula médica cuando el
        medicamento lo requiera y reciba su pedido en casa. Sin registro para mirar,
        registro solo al comprar.
    </p>
    <a class="boton" href="${raiz}/catalogo">Ver el catálogo completo</a>
</section>

<h2>Categorías</h2>
<div class="rejilla-categorias">
    <c:forEach var="categoria" items="${categorias}">
        <c:url var="enlaceCategoria" value="/catalogo">
            <c:param name="categoria" value="${categoria}"/>
        </c:url>
        <a class="categoria" href="${enlaceCategoria}">
            <%-- Cada categoria lleva su propio icono --%>
            <span class="icono" aria-hidden="true">
                <c:choose>
                    <c:when test="${categoria eq 'Medicamentos'}">&#128138;</c:when>
                    <c:when test="${categoria eq 'Cuidado personal'}">&#129380;</c:when>
                    <c:when test="${categoria eq 'Botiquin'}">&#129657;</c:when>
                    <c:when test="${categoria eq 'Bebes'}">&#127868;</c:when>
                    <c:when test="${categoria eq 'Nutricion'}">&#129367;</c:when>
                    <c:otherwise>&#128138;</c:otherwise>
                </c:choose>
            </span>
            <c:out value="${categoria}"/>
            <span class="ayuda" style="display:block;">
                <c:choose>
                    <c:when test="${empty conteosPorCategoria[categoria]}">
                        sin productos
                    </c:when>
                    <c:otherwise>
                        ${conteosPorCategoria[categoria]} producto(s)
                    </c:otherwise>
                </c:choose>
            </span>
        </a>
    </c:forEach>
</div>

<h2>Productos del catálogo</h2>

<c:choose>
    <c:when test="${empty listaProductos}">
        <p class="sin-registros">
            Todavía no hay productos en el catálogo. Si es el administrador,
            regístrelos desde el <a href="${raiz}/productos">módulo de Productos</a>.
        </p>
    </c:when>
    <c:otherwise>
        <div class="rejilla-productos">
            <c:forEach var="producto" items="${listaProductos}" end="7">
                <%@ include file="/WEB-INF/jsp/tienda/tarjeta-producto.jsp" %>
            </c:forEach>
        </div>

        <div class="acciones-formulario">
            <a class="boton boton-secundario" href="${raiz}/catalogo">
                Ver los ${listaProductos.size()} productos del catálogo
            </a>
        </div>
    </c:otherwise>
</c:choose>

<%@ include file="/WEB-INF/jsp/tienda/pie-tienda.jsp" %>
