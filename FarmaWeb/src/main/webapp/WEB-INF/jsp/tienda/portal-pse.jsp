<%--
    Salida al portal de pagos PSE.

    Representa el momento en que una tienda entrega el control al portal del
    banco. Aqui es una simulacion declarada: el proyecto no tiene convenio con
    ACH Colombia ni acceso a un ambiente de pruebas de PSE, de modo que no se
    puede confirmar un debito real.

    Por eso la transaccion queda en estado PENDIENTE. Marcarla como aprobada
    daria por cierto un cobro que nunca ocurrio.
--%>
<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="tituloPagina" value="Pago por PSE"/>
<c:set var="seccionActiva" value="pago"/>
<%@ include file="/WEB-INF/jsp/tienda/cabecera-tienda.jsp" %>

<h1 class="titulo-modulo">Pago por PSE</h1>
<p class="descripcion-modulo">
    Pedido ${pedido.idPedido} &mdash; su intención de pago quedó registrada.
</p>

<div class="alerta alerta-advertencia" role="alert">
    <strong>Este es un entorno de práctica, no un pago real.</strong>
    En una tienda en producción, en este punto usted saldría al portal de su
    banco para autorizar el débito. Este proyecto formativo no tiene convenio
    con ACH Colombia ni acceso a un ambiente de pruebas de PSE, así que
    <strong>no se realiza ningún débito</strong> y la aplicación no puede
    confirmar la transacción.
</div>

<div class="columnas-carrito">

    <div class="tarjeta">
        <h2>Qué pasaría en un entorno real</h2>

        <ol>
            <li>La tienda envía al banco el número del pedido y el valor.</li>
            <li>Usted escoge su banco y autoriza el débito en el portal de PSE.</li>
            <li>El banco devuelve el resultado a la tienda.</li>
            <li>Solo entonces la transacción pasaría a <strong>APROBADA</strong>.</li>
        </ol>

        <p>
            Como el paso 3 no puede ocurrir aquí, su transacción quedó registrada
            en estado <strong>PENDIENTE</strong>. La farmacia la conciliará
            manualmente antes de despachar el pedido.
        </p>

        <div class="acciones-formulario">
            <a class="boton" href="${raiz}/micuenta">Volver a mi cuenta</a>
            <a class="boton boton-secundario" href="${raiz}/catalogo">Seguir comprando</a>
        </div>
    </div>

    <div class="resumen-compra">
        <h2 style="margin-top:0;">Datos de la transacción</h2>

        <div class="linea-resumen">
            <span>Pedido</span>
            <span>${pedido.idPedido}</span>
        </div>
        <div class="linea-resumen">
            <span>Medio de pago</span>
            <span>PSE</span>
        </div>
        <div class="linea-resumen">
            <span>Estado del pago</span>
            <span><span class="etiqueta etiqueta-advertencia">PENDIENTE</span></span>
        </div>
        <div class="linea-resumen">
            <span>Estado del pedido</span>
            <span><span class="etiqueta etiqueta-advertencia">${pedido.estado}</span></span>
        </div>
        <div class="linea-resumen total">
            <span>Valor</span>
            <span>$${pedido.totalTexto}</span>
        </div>

        <div class="aviso-invitado" style="margin-top:18px; margin-bottom:0;">
            Su pedido sigue pendiente de revisión por parte de un farmacéutico,
            quien validará la fórmula médica si incluye medicamentos controlados.
        </div>
    </div>

</div>

<%@ include file="/WEB-INF/jsp/tienda/pie-tienda.jsp" %>
