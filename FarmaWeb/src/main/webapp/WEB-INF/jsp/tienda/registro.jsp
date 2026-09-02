<%--
    Registro de clientes en la tienda publica.

    Es el unico camino por el que una persona obtiene cuenta sin que
    intervenga el administrador, y siempre recibe el rol Cliente. La
    asignacion de los perfiles Farmaceutico y Administrador se explica en el
    modulo de Usuarios del panel de gestion, no aqui.

    El formulario se envia por POST para que la contrasena no viaje visible
    en la barra de direcciones.
--%>
<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="tituloPagina" value="Crear cuenta"/>
<c:set var="seccionActiva" value="registro"/>
<%@ include file="/WEB-INF/jsp/tienda/cabecera-tienda.jsp" %>

<h1 class="titulo-modulo">Crear mi cuenta</h1>
<p class="descripcion-modulo">
    Complete sus datos para poder confirmar el pedido y consultar su historial.
</p>

<c:if test="${not carrito.vacio}">
    <div class="aviso-invitado">
        Tiene <strong>${carrito.cantidadRenglones} producto(s)</strong> en el carrito
        por $${carrito.totalTexto}.
        Al crear la cuenta podrá confirmar el pedido de inmediato.
    </div>
</c:if>

<div class="tarjeta">
    <h2>Datos de la cuenta</h2>

    <form action="${raiz}/registro" method="post">

        <div class="rejilla-formulario">
            <div class="campo">
                <label for="nombre">Nombre</label>
                <input type="text" id="nombre" name="nombre" required maxlength="50"
                       value="<c:out value='${sessionScope.registroNombre}'/>"
                       placeholder="ejemplo: Laura">
            </div>

            <div class="campo">
                <label for="apellido">Apellido</label>
                <input type="text" id="apellido" name="apellido" required maxlength="50"
                       value="<c:out value='${sessionScope.registroApellido}'/>"
                       placeholder="ejemplo: Gómez">
            </div>

            <div class="campo">
                <label for="email">Correo electrónico</label>
                <input type="email" id="email" name="email" required maxlength="150"
                       autocomplete="username"
                       value="<c:out value='${sessionScope.registroEmail}'/>"
                       placeholder="ejemplo: laura.gomez@correo.com">
                <span class="ayuda">Con este correo ingresará al sistema.</span>
            </div>

            <div class="campo">
                <label for="telefono">Teléfono</label>
                <input type="text" id="telefono" name="telefono" maxlength="20"
                       value="<c:out value='${sessionScope.registroTelefono}'/>"
                       placeholder="ejemplo: 3001234567">
            </div>

            <div class="campo">
                <label for="direccion">Dirección de entrega</label>
                <input type="text" id="direccion" name="direccion" maxlength="250"
                       value="<c:out value='${sessionScope.registroDireccion}'/>"
                       placeholder="ejemplo: Calle 10 # 4-25, Popayán">
            </div>

            <div class="campo">
                <label for="fechaNacimiento">Fecha de nacimiento</label>
                <input type="date" id="fechaNacimiento" name="fechaNacimiento"
                       value="<c:out value='${sessionScope.registroFechaNacimiento}'/>">
                <span class="ayuda">Con este dato el sistema calcula su edad.</span>
            </div>

            <div class="campo">
                <label for="password">Contraseña</label>
                <input type="password" id="password" name="password" required minlength="6"
                       autocomplete="new-password" placeholder="mínimo 6 caracteres">
            </div>

            <div class="campo">
                <label for="password2">Repetir contraseña</label>
                <input type="password" id="password2" name="password2" required minlength="6"
                       autocomplete="new-password" placeholder="vuelva a escribirla">
            </div>
        </div>

        <div class="campo-rol" style="margin-top:18px;">
            Rol que se le asignará:
            <strong><c:out value="${rolAsignado}"/></strong>
            <span class="insignia-rol"><c:out value="${rolAsignado}"/></span>
            <span class="ayuda">
                Con este perfil podrá comprar, consultar sus pedidos, cargar sus
                fórmulas médicas y marcar productos como favoritos.
            </span>
        </div>

        <div class="acciones-formulario">
            <button type="submit">Crear cuenta y continuar</button>
            <a class="boton boton-secundario" href="${raiz}/carrito">Volver al carrito</a>
        </div>
    </form>
</div>

<div class="tarjeta">
    <h2>¿Ya tiene cuenta?</h2>
    <p>Ingrese con su correo y contraseña. Su carrito se conserva.</p>
    <div class="acciones-formulario">
        <a class="boton boton-secundario" href="${raiz}/login">Ingresar a mi cuenta</a>
    </div>
</div>

<%-- Los datos digitados ya se usaron: se limpian para no repoblar el formulario --%>
<%
    session.removeAttribute("registroNombre");
    session.removeAttribute("registroApellido");
    session.removeAttribute("registroEmail");
    session.removeAttribute("registroTelefono");
    session.removeAttribute("registroDireccion");
    session.removeAttribute("registroFechaNacimiento");
%>

<%@ include file="/WEB-INF/jsp/tienda/pie-tienda.jsp" %>
