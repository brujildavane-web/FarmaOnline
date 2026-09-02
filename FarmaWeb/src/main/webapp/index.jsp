<%--
    Pagina de entrada de la aplicacion.

    Entrega el control al ServletTienda, que dibuja la pagina de inicio de la
    farmacia. Es una zona publica: no exige cuenta. Se usa jsp:forward en
    lugar de una redireccion para que la direccion del navegador siga siendo
    la raiz del sitio.
--%>
<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<jsp:forward page="/inicio"/>
