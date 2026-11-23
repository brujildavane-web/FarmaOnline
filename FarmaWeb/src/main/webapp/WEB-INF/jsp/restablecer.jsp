<%--
    Formulario para escribir la contrasena nueva.

    Solo se muestra cuando el enlace recibido existe, no se ha usado y no ha
    vencido; de lo contrario se explica la situacion y se ofrece pedir otro.
--%>
<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Nueva contraseña | Farmacia en Línea</title>
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/estilos.css?v=${initParam.versionRecursos}">
</head>
<body>

<div class="pantalla-ingreso">
    <div class="tarjeta-ingreso">

        <h1>Nueva contraseña</h1>

        <c:if test="${not empty sessionScope.mensajeError}">
            <div class="alerta alerta-error" role="alert">
                <c:out value="${sessionScope.mensajeError}"/>
            </div>
            <% session.removeAttribute("mensajeError"); %>
        </c:if>

        <c:choose>
            <c:when test="${enlaceVigente}">
                <p>Escriba la contraseña nueva dos veces.</p>

                <form action="${pageContext.request.contextPath}/restablecer" method="post">
                    <input type="hidden" name="codigo" value="<c:out value='${codigo}'/>">

                    <div class="campo">
                        <label for="password">Contraseña nueva</label>
                        <input type="password" id="password" name="password" required minlength="6"
                               autocomplete="new-password" placeholder="mínimo 6 caracteres">
                    </div>

                    <div class="campo">
                        <label for="password2">Repita la contraseña</label>
                        <input type="password" id="password2" name="password2" required minlength="6"
                               autocomplete="new-password">
                    </div>

                    <button type="submit">Cambiar contraseña</button>
                </form>
            </c:when>
            <c:otherwise>
                <div class="alerta alerta-advertencia" role="alert">
                    Este enlace no es válido: ya se usó, venció o fue reemplazado por uno más reciente.
                </div>
                <a class="boton" style="display:block; text-align:center;"
                   href="${pageContext.request.contextPath}/recuperar">Solicitar un enlace nuevo</a>
            </c:otherwise>
        </c:choose>

        <p class="ayuda" style="margin-top:14px; text-align:center;">
            <a href="${pageContext.request.contextPath}/login">Volver al ingreso</a>
        </p>

    </div>
</div>

</body>
</html>
