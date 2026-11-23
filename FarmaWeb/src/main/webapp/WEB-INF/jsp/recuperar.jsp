<%--
    Solicitud de recuperacion de contrasena.

    Pide el correo y, al enviarlo, muestra siempre el mismo aviso exista o no
    la cuenta. Cuando el correo si existe, muestra tambien el enlace que el
    sistema enviaria por correo: es una simulacion declarada porque el
    proyecto no cuenta con servidor de correo.
--%>
<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Recuperar contraseña | Farmacia en Línea</title>
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/estilos.css?v=${initParam.versionRecursos}">
</head>
<body>

<div class="pantalla-ingreso">
    <div class="tarjeta-ingreso">

        <h1>Recuperar contraseña</h1>
        <p>Escriba el correo con el que se registró y le generaremos un enlace para crear una contraseña nueva.</p>

        <c:if test="${not empty sessionScope.mensajeError}">
            <div class="alerta alerta-error" role="alert">
                <c:out value="${sessionScope.mensajeError}"/>
            </div>
            <% session.removeAttribute("mensajeError"); %>
        </c:if>

        <c:if test="${solicitudProcesada}">
            <div class="alerta alerta-exito" role="status">
                Si el correo está registrado, se generó un enlace de recuperación
                válido por ${minutosVigencia} minutos.
            </div>

            <c:if test="${not empty enlaceSimulado}">
                <div class="alerta alerta-advertencia" role="note">
                    <strong>Simulación del correo.</strong> El proyecto no tiene servidor
                    de correo, por eso el enlace se muestra aquí en lugar de enviarse:<br>
                    <a id="enlace-recuperacion" href="<c:out value='${enlaceSimulado}'/>">Crear una contraseña nueva</a>
                </div>
            </c:if>
        </c:if>

        <form action="${pageContext.request.contextPath}/recuperar" method="post">
            <div class="campo">
                <label for="email">Correo electrónico</label>
                <input type="email" id="email" name="email" required
                       autocomplete="username"
                       value="<c:out value='${emailDigitado}'/>"
                       placeholder="ejemplo: laura.gomez@correo.com">
            </div>
            <button type="submit">Generar enlace</button>
        </form>

        <p class="ayuda" style="margin-top:14px; text-align:center;">
            <a href="${pageContext.request.contextPath}/login">Volver al ingreso</a>
        </p>

    </div>
</div>

</body>
</html>
