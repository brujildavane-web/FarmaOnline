<%--
    Detalle de un producto del catalogo.

    El identificador llega por el metodo GET en el parametro id. Agregar al
    carrito modifica la sesion, por lo que se envia por POST.
--%>
<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="tituloPagina" value="${producto.nombre}"/>
<c:set var="seccionActiva" value="catalogo"/>
<%@ include file="/WEB-INF/jsp/tienda/cabecera-tienda.jsp" %>

<p class="descripcion-modulo">
    <a href="${raiz}/catalogo">Catálogo</a>
    <c:if test="${not empty producto.categoria}">
        <c:url var="enlaceCategoria" value="/catalogo">
            <c:param name="categoria" value="${producto.categoria}"/>
        </c:url>
        &rsaquo; <a href="${enlaceCategoria}"><c:out value="${producto.categoria}"/></a>
    </c:if>
    &rsaquo; <c:out value="${producto.nombre}"/>
</p>

<div class="columnas-carrito">

    <div class="tarjeta">
        <div class="producto" style="box-shadow:none;">
            <div class="imagen" style="height:200px;">
                <c:choose>
                    <c:when test="${producto.conImagen}">
                        <img src="${raiz}/img/productos/${producto.imagen}"
                             alt="Fotografía de ${producto.nombre}"
                             style="max-height:200px; object-fit:contain;">
                    </c:when>
                    <c:otherwise>
                        <span aria-hidden="true">${producto.inicial}</span>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>

        <h1 class="titulo-modulo"><c:out value="${producto.nombre}"/></h1>

        <p>
            <strong>Código:</strong> <c:out value="${producto.skuCode}"/>
            <c:if test="${not empty producto.categoria}">
                &nbsp;|&nbsp; <strong>Categoría:</strong> <c:out value="${producto.categoria}"/>
            </c:if>
        </p>

        <p><c:out value="${producto.descripcion}"/></p>

        <c:if test="${producto.requiereReceta}">
            <div class="alerta alerta-advertencia" role="alert">
                <strong>Medicamento controlado.</strong> Para completar la compra deberá
                adjuntar la fórmula médica vigente. El farmacéutico la validará antes
                del despacho.
            </div>
        </c:if>
    </div>

    <div class="resumen-compra">
        <span class="precio"
              style="font-size:2rem; color:var(--color-principal-oscuro); font-weight:bold;">
            $${producto.precioTexto}
        </span>

        <p>
            <c:choose>
                <c:when test="${producto.disponible}">
                    <span class="etiqueta etiqueta-disponible">Disponible</span>
                    <span class="ayuda">
                        ${producto.stockTexto}
                        unidades en existencia
                    </span>
                </c:when>
                <c:otherwise>
                    <span class="etiqueta etiqueta-agotado">Agotado</span>
                </c:otherwise>
            </c:choose>
        </p>

        <c:choose>
            <c:when test="${producto.disponible}">
                <form action="${raiz}/carrito" method="post">
                    <input type="hidden" name="accion" value="agregar">
                    <input type="hidden" name="idProducto" value="${producto.idProducto}">

                    <div class="campo">
                        <label for="cantidad">Cantidad</label>
                        <input type="number" id="cantidad" name="cantidad" value="1"
                               min="1" max="${producto.stockTotal}" step="1">
                    </div>

                    <div class="acciones-formulario">
                        <button type="submit" style="width:100%;">Agregar al carrito</button>
                    </div>
                </form>
            </c:when>
            <c:otherwise>
                <p class="sin-registros">
                    Este producto no tiene existencias disponibles en este momento.
                </p>
            </c:otherwise>
        </c:choose>

        <p class="ayuda" style="margin-top:14px;">
            No necesita registrarse para agregar productos al carrito.
            La cuenta se solicita al confirmar el pedido.
        </p>
    </div>

</div>

<%@ include file="/WEB-INF/jsp/tienda/pie-tienda.jsp" %>
