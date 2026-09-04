<%--
    Modulo de Roles (tabla TblRol).

    El formulario envia los datos por POST al ServletRol; los enlaces de la
    tabla envian el identificador por GET.
--%>
<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="tituloPagina" value="Roles"/>
<c:set var="moduloActivo" value="roles"/>
<c:set var="enEdicion" value="${not empty rolEnEdicion}"/>
<%@ include file="/WEB-INF/jsp/comunes/cabecera.jsp" %>

<h1 class="titulo-modulo">Gestion de roles</h1>
<p class="descripcion-modulo">
    Perfiles de acceso al sistema y permisos asignados a cada uno.
</p>

<div class="tarjeta">
    <h2>${enEdicion ? 'Editar rol' : 'Registrar rol nuevo'}</h2>

    <form action="${pageContext.request.contextPath}/roles" method="post">
        <input type="hidden" name="idRol" value="${rolEnEdicion.idRol}">

        <div class="rejilla-formulario">
            <div class="campo">
                <label for="nombreRol">Nombre del rol</label>
                <input type="text" id="nombreRol" name="nombreRol" required maxlength="50"
                       value="<c:out value='${rolEnEdicion.nombreRol}'/>"
                       placeholder="ejemplo: Administrador">
            </div>

            <div class="campo">
                <label for="permisos">Permisos del rol</label>
                <input type="text" id="permisos" name="permisos" required maxlength="255"
                       value="<c:out value='${rolEnEdicion.permisos}'/>"
                       placeholder="ejemplo: gestionar usuarios, inventario y reportes">
                <span class="ayuda">Describa que puede hacer este perfil en el sistema.</span>
            </div>
        </div>

        <div class="acciones-formulario">
            <button type="submit">${enEdicion ? 'Actualizar rol' : 'Registrar rol'}</button>
            <c:if test="${enEdicion}">
                <a class="boton boton-secundario"
                   href="${pageContext.request.contextPath}/roles">Cancelar edicion</a>
            </c:if>
        </div>
    </form>
</div>

<div class="tarjeta">
    <h2>Roles registrados</h2>

    <c:choose>
        <c:when test="${empty listaRoles}">
            <p class="sin-registros">Todavia no hay roles registrados en el sistema.</p>
        </c:when>
        <c:otherwise>
            <div class="contenedor-tabla">
                <table class="tabla-datos">
                    <caption>Total de roles: ${listaRoles.size()}</caption>
                    <thead>
                        <tr>
                            <th scope="col">Codigo</th>
                            <th scope="col">Nombre</th>
                            <th scope="col">Permisos</th>
                            <th scope="col">Acciones</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="rol" items="${listaRoles}">
                            <tr>
                                <td>${rol.idRol}</td>
                                <td><c:out value="${rol.nombreRol}"/></td>
                                <td style="white-space: normal;"><c:out value="${rol.permisos}"/></td>
                                <td>
                                    <div class="columna-acciones">
                                        <a class="boton boton-secundario boton-pequeno"
                                           href="${pageContext.request.contextPath}/roles?accion=editar&id=${rol.idRol}">Editar</a>
                                        <a class="boton boton-peligro boton-pequeno"
                                           href="${pageContext.request.contextPath}/roles?accion=eliminar&id=${rol.idRol}"
                                           onclick="return confirm('Confirma eliminar el rol ${rol.nombreRol}?');">Eliminar</a>
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

<div class="tarjeta">
    <h2>Pestanas habilitadas a cada rol</h2>
    <p>
        Ademas del texto de permisos, aqui se ve el menu real que encuentra
        cada perfil cuando inicia sesion. Es la misma informacion que consulta
        el filtro de seguridad para autorizar cada modulo, por lo que siempre
        coincide con lo que el usuario puede abrir.
    </p>

    <c:forEach var="rol" items="${listaRoles}">
        <c:set var="pestanas" value="${modulosPorRol[rol.nombreRol]}"/>

        <div class="bloque-rol">
            <h3>
                <span class="insignia-rol"><c:out value="${rol.nombreRol}"/></span>
                <span class="nota-rol">
                    <c:choose>
                        <c:when test="${empty pestanas}">sin pestanas habilitadas</c:when>
                        <c:otherwise>${pestanas.size()} pestanas habilitadas</c:otherwise>
                    </c:choose>
                </span>
            </h3>

            <c:choose>
                <c:when test="${empty pestanas}">
                    <p class="sin-registros">
                        Este perfil no tiene modulos asignados en el mapa de navegacion,
                        asi que solo alcanza la tienda publica. Los perfiles que el sistema
                        reconoce son Administrador, Farmaceutico y Cliente.
                    </p>
                </c:when>
                <c:otherwise>
                    <div class="contenedor-tabla">
                        <table class="tabla-datos">
                            <thead>
                                <tr>
                                    <th scope="col">Zona</th>
                                    <th scope="col">Pestana</th>
                                    <th scope="col">Que encuentra alli</th>
                                    <th scope="col">Acceso</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="modulo" items="${pestanas}">
                                    <tr>
                                        <td><c:out value="${modulo.zona}"/></td>
                                        <td><c:out value="${modulo.etiqueta}"/></td>
                                        <td style="white-space: normal;">
                                            <c:out value="${modulo.proposito}"/>
                                        </td>
                                        <td>${modulo.exigeCuenta ? 'Con sesion' : 'Publica'}</td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </c:forEach>
</div>

<%@ include file="/WEB-INF/jsp/comunes/pie.jsp" %>
