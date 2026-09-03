<%--
    Catalogo publico de productos.

    El filtro por categoria y la busqueda viajan por el metodo GET, de modo
    que el resultado se puede compartir o guardar como direccion web.
--%>
<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="tituloPagina" value="${tituloCatalogo}"/>
<c:set var="seccionActiva" value="catalogo"/>
<%@ include file="/WEB-INF/jsp/tienda/cabecera-tienda.jsp" %>

<h1 class="titulo-modulo"><c:out value="${tituloCatalogo}"/></h1>
<p class="descripcion-modulo">
    ${listaProductos.size()} producto(s).
    La disponibilidad se calcula sobre los lotes que no están vencidos.
</p>

<div class="tarjeta">
    <h2>Filtrar por categoría</h2>

    <div class="acciones-formulario" style="margin-top:0;">
        <a class="boton ${empty categoriaActiva ? '' : 'boton-secundario'} boton-pequeno"
           href="${raiz}/catalogo">Todas</a>

        <c:forEach var="categoria" items="${categorias}">
            <c:url var="enlaceCategoria" value="/catalogo">
                <c:param name="categoria" value="${categoria}"/>
            </c:url>
            <a class="boton ${categoriaActiva eq categoria ? '' : 'boton-secundario'} boton-pequeno"
               href="${enlaceCategoria}">
                <c:out value="${categoria}"/>
            </a>
        </c:forEach>
    </div>

    <form action="${raiz}/catalogo" method="get" style="margin-top:18px;">
        <div class="rejilla-formulario">
            <div class="campo">
                <label for="busqueda">Buscar por nombre o código</label>
                <input type="search" id="busqueda" name="q"
                       value="<c:out value='${busquedaActiva}'/>"
                       placeholder="ejemplo: paracetamol">
                <span class="ayuda">La búsqueda se envía por el método GET.</span>
            </div>
        </div>
        <div class="acciones-formulario">
            <button type="submit" class="boton-secundario">Buscar</button>
        </div>
    </form>
</div>

<c:choose>
    <c:when test="${empty listaProductos}">
        <p class="sin-registros">
            No hay productos que coincidan con el criterio seleccionado.
            <a href="${raiz}/catalogo">Ver todo el catálogo</a>
        </p>
    </c:when>
    <c:otherwise>
        <div class="rejilla-productos">
            <c:forEach var="producto" items="${listaProductos}">
                <%@ include file="/WEB-INF/jsp/tienda/tarjeta-producto.jsp" %>
            </c:forEach>
        </div>
    </c:otherwise>
</c:choose>

<%@ include file="/WEB-INF/jsp/tienda/pie-tienda.jsp" %>
