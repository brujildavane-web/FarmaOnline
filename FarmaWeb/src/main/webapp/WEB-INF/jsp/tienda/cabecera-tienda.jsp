<%--
    Plantilla maestra de la tienda publica.

    Se incluye al inicio de cada pagina de la tienda con la directiva include.
    Espera las variables tituloPagina y seccionActiva definidas antes de la
    inclusion.

    Fragmento incluido: no declara directiva page para no repetir el
    contentType de la pagina que lo incluye.

    Elementos JSP aplicados: directiva taglib, expresiones EL, etiquetas
    JSTL, objetos implicitos y un scriptlet para borrar los mensajes ya
    mostrados.
--%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><c:out value="${tituloPagina}"/> | Farmacia en Línea</title>
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/estilos.css?v=${initParam.versionRecursos}">
</head>
<body>

<a class="salto-contenido" href="#contenido-principal">Saltar al contenido principal</a>

<c:set var="raiz" value="${pageContext.request.contextPath}"/>

<header class="tienda-encabezado">
    <div class="tienda-marca">
        Farmacia en Línea<span>Medicamentos y cuidado personal</span>
    </div>

    <%-- El buscador viaja por GET para que el resultado se pueda compartir --%>
    <form class="tienda-buscador" action="${raiz}/catalogo" method="get">
        <label class="oculto" for="q">Buscar producto</label>
        <input type="search" id="q" name="q" value="<c:out value='${busquedaActiva}'/>"
               placeholder="Buscar medicamento o principio activo">
        <button type="submit">Buscar</button>
    </form>

    <nav class="tienda-acciones" aria-label="Acciones de cuenta">
        <a class="boton-carrito" href="${raiz}/carrito">
            Carrito
            <c:if test="${not empty carrito and carrito.unidades gt 0}">
                <span class="contador-carrito">
                    ${carrito.unidadesTexto}
                </span>
            </c:if>
        </a>

        <c:choose>
            <c:when test="${empty sessionScope.usuarioSesion}">
                <a class="boton boton-secundario" href="${raiz}/login">Ingresar</a>
                <a class="boton" href="${raiz}/registro">Registrarse</a>
            </c:when>
            <c:otherwise>
                <c:if test="${sessionScope.usuarioSesion.administrador
                              or sessionScope.usuarioSesion.farmaceutico}">
                    <a class="boton boton-secundario" href="${raiz}/panel">Panel de gestión</a>
                </c:if>
                <a class="boton boton-secundario" href="${raiz}/micuenta">Mi cuenta</a>
                <a class="boton-carrito" href="${raiz}/notificaciones">
                    Notificaciones
                    <c:if test="${notificacionesSinLeer gt 0}">
                        <span class="contador-carrito" id="contador-notificaciones">${notificacionesSinLeer}</span>
                    </c:if>
                </a>
                <a class="boton-salir" href="${raiz}/logout">Cerrar sesión</a>
            </c:otherwise>
        </c:choose>
    </nav>
</header>

<nav class="tienda-menu" aria-label="Navegación de la tienda">
    <a href="${raiz}/inicio" class="${seccionActiva eq 'inicio' ? 'activo' : ''}">Inicio</a>
    <a href="${raiz}/catalogo"
       class="${seccionActiva eq 'catalogo' and empty categoriaActiva ? 'activo' : ''}">Catálogo</a>

    <c:forEach var="categoria" items="${categorias}">
        <%-- c:url codifica los espacios y tildes del nombre de la categoria --%>
        <c:url var="enlaceCategoria" value="/catalogo">
            <c:param name="categoria" value="${categoria}"/>
        </c:url>
        <a href="${enlaceCategoria}"
           class="${categoriaActiva eq categoria ? 'activo' : ''}">
            <c:out value="${categoria}"/>
        </a>
    </c:forEach>

    <a href="${raiz}/carrito" class="${seccionActiva eq 'carrito' ? 'activo' : ''}">Mi carrito</a>
</nav>

<main class="contenido" id="contenido-principal">

    <%-- Mensajes de resultado publicados por los servlets --%>
    <c:if test="${not empty sessionScope.mensajeExito}">
        <div class="alerta alerta-exito" role="status">
            <c:out value="${sessionScope.mensajeExito}"/>
        </div>
        <% session.removeAttribute("mensajeExito"); %>
    </c:if>

    <c:if test="${not empty sessionScope.mensajeError}">
        <div class="alerta alerta-error" role="alert">
            <c:out value="${sessionScope.mensajeError}"/>
        </div>
        <% session.removeAttribute("mensajeError"); %>
    </c:if>
