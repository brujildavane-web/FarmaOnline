<%--
    Menu de navegacion de los modulos del sistema.

    Fragmento incluido por cabecera.jsp con la directiva include, por lo que
    no declara su propia directiva page: hereda la de la pagina que lo incluye.

    Las pestanas no estan escritas una por una: se recorren con forEach desde
    el CatalogoModulos, que el FiltroSeguridad deja en la peticion ya filtrado
    segun el rol de la sesion. De esa forma el menu muestra exactamente los
    modulos que el filtro autoriza, y coincide con la matriz que el
    administrador consulta en el modulo de Roles.

    Refleja el mapa de navegacion definido en la evidencia
    GA5-220501095-AA1-EV05 y marca el modulo activo comparando la clave del
    modulo con el atributo moduloActivo que publica cada pagina.
--%>
<nav class="menu-modulos" aria-label="Modulos del sistema">
    <c:set var="raiz" value="${pageContext.request.contextPath}"/>

    <c:forEach var="modulo" items="${modulosDelMenu}">
        <a href="${raiz}${modulo.ruta}"
           class="${moduloActivo eq modulo.clave ? 'activo' : ''}"
           title="<c:out value='${modulo.proposito}'/>"><c:out value="${modulo.etiqueta}"/></a>
    </c:forEach>

    <a href="${raiz}/inicio">Ver la tienda</a>
</nav>
