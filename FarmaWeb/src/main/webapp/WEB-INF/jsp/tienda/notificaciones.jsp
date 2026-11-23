<%--
    Notificaciones del usuario.

    Lista los avisos del usuario en sesion, el mas reciente primero. Los que
    llegaron sin leer se resaltan con la insignia "Nueva"; al abrir la pagina
    quedan marcados como leidos.
--%>
<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="tituloPagina" value="Notificaciones"/>
<c:set var="seccionActiva" value="notificaciones"/>
<%@ include file="/WEB-INF/jsp/tienda/cabecera-tienda.jsp" %>

<h1>Notificaciones</h1>

<c:choose>
    <c:when test="${empty notificaciones}">
        <p class="ayuda">No tiene notificaciones por ahora.</p>
    </c:when>
    <c:otherwise>
        <table class="tabla-datos">
            <caption>Sus avisos más recientes</caption>
            <thead>
                <tr><th scope="col">Fecha</th><th scope="col">Aviso</th><th scope="col">Estado</th></tr>
            </thead>
            <tbody>
                <c:forEach var="aviso" items="${notificaciones}">
                    <tr>
                        <td><c:out value="${aviso.fechaTexto}"/></td>
                        <td><c:out value="${aviso.mensaje}"/></td>
                        <td>
                            <c:choose>
                                <c:when test="${aviso.leida}">Leída</c:when>
                                <c:otherwise><strong>Nueva</strong></c:otherwise>
                            </c:choose>
                        </td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>
    </c:otherwise>
</c:choose>

<%@ include file="/WEB-INF/jsp/tienda/pie-tienda.jsp" %>
