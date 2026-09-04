<%--
    Reportes del panel administrativo.

    Responde a dos historias de usuario del administrador: generar reportes
    de ventas para analizar el negocio y reportes de inventario para
    controlar el stock.

    El reporte que se muestra se escoge con el parametro tipo, que viaja por
    el metodo GET.
--%>
<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="tituloPagina" value="Reportes"/>
<c:set var="moduloActivo" value="reportes"/>
<%@ include file="/WEB-INF/jsp/comunes/cabecera.jsp" %>

<h1 class="titulo-modulo">Reportes del negocio</h1>
<p class="descripcion-modulo">
    Informacion agrupada para tomar decisiones. Los pedidos anulados no cuentan
    como venta.
</p>

<div class="tarjeta">
    <h2>Escoja el reporte</h2>
    <div class="acciones-formulario" style="margin-top:0;">
        <a class="boton ${tipoReporte eq 'ventas' ? '' : 'boton-secundario'}"
           href="${pageContext.request.contextPath}/reportes?tipo=ventas">Ventas</a>
        <a class="boton ${tipoReporte eq 'inventario' ? '' : 'boton-secundario'}"
           href="${pageContext.request.contextPath}/reportes?tipo=inventario">Inventario</a>
    </div>
</div>

<%-- ==================================================== REPORTE DE VENTAS --%>
<c:if test="${tipoReporte eq 'ventas'}">

    <div class="tarjeta">
        <h2>Indicadores de venta</h2>
        <div class="rejilla-indicadores">
            <div class="indicador indicador-destacado">
                <span class="cifra">$${totalFacturadoTexto}</span>
                <span class="rotulo">Total facturado</span>
            </div>
            <div class="indicador">
                <span class="cifra">${pedidosVendidos}</span>
                <span class="rotulo">Pedidos vendidos</span>
            </div>
            <div class="indicador">
                <span class="cifra">$${ticketPromedioTexto}</span>
                <span class="rotulo">Valor promedio por pedido</span>
            </div>
            <div class="indicador">
                <span class="cifra">${unidadesVendidasTexto}</span>
                <span class="rotulo">Unidades despachadas</span>
            </div>
        </div>
    </div>

    <div class="tarjeta">
        <h2>Productos mas vendidos</h2>
        <p class="descripcion-modulo">
            Los de mayor rotacion. Sirven para decidir que reponer primero.
        </p>

        <c:choose>
            <c:when test="${empty masVendidos}">
                <p class="sin-registros">Todavia no hay ventas registradas.</p>
            </c:when>
            <c:otherwise>
                <div class="contenedor-tabla">
                    <table class="tabla-datos">
                        <caption>Ordenados por unidades despachadas.</caption>
                        <thead>
                            <tr>
                                <th scope="col">#</th>
                                <th scope="col">Producto</th>
                                <th scope="col">Unidades</th>
                                <th scope="col">Valor vendido</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="fila" items="${masVendidos}" varStatus="orden">
                                <tr>
                                    <td>${orden.count}</td>
                                    <td><c:out value="${fila.etiqueta}"/></td>
                                    <td>${fila.cantidadTexto}</td>
                                    <td>$${fila.valorTexto}</td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:otherwise>
        </c:choose>
    </div>

    <div class="tarjeta">
        <h2>Ventas por categoria</h2>
        <c:choose>
            <c:when test="${empty ventasPorCategoria}">
                <p class="sin-registros">Sin datos para agrupar.</p>
            </c:when>
            <c:otherwise>
                <div class="contenedor-tabla">
                    <table class="tabla-datos">
                        <thead>
                            <tr>
                                <th scope="col">Categoria</th>
                                <th scope="col">Unidades</th>
                                <th scope="col">Valor vendido</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="fila" items="${ventasPorCategoria}">
                                <tr>
                                    <td><c:out value="${fila.etiqueta}"/></td>
                                    <td>${fila.cantidadTexto}</td>
                                    <td>$${fila.valorTexto}</td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:otherwise>
        </c:choose>
    </div>

    <div class="tarjeta">
        <h2>Ventas por mes</h2>
        <c:choose>
            <c:when test="${empty ventasPorMes}">
                <p class="sin-registros">Sin datos para agrupar.</p>
            </c:when>
            <c:otherwise>
                <div class="contenedor-tabla">
                    <table class="tabla-datos">
                        <caption>Del mes mas reciente al mas antiguo.</caption>
                        <thead>
                            <tr>
                                <th scope="col">Mes</th>
                                <th scope="col">Pedidos</th>
                                <th scope="col">Valor facturado</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="fila" items="${ventasPorMes}">
                                <tr>
                                    <td><c:out value="${fila.etiqueta}"/></td>
                                    <td>${fila.cantidadTexto}</td>
                                    <td>$${fila.valorTexto}</td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:otherwise>
        </c:choose>
    </div>

    <div class="tarjeta">
        <h2>Pedidos por estado</h2>
        <p class="descripcion-modulo">
            Muestra cuanto trabajo hay pendiente de atender y cuanto se anulo.
        </p>
        <c:choose>
            <c:when test="${empty ventasPorEstado}">
                <p class="sin-registros">Sin pedidos registrados.</p>
            </c:when>
            <c:otherwise>
                <div class="contenedor-tabla">
                    <table class="tabla-datos">
                        <thead>
                            <tr>
                                <th scope="col">Estado</th>
                                <th scope="col">Pedidos</th>
                                <th scope="col">Valor</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="fila" items="${ventasPorEstado}">
                                <tr>
                                    <td>
                                        <span class="etiqueta ${fila.etiqueta eq 'ANULADO'
                                                ? 'etiqueta-agotado' : 'etiqueta-disponible'}">
                                            <c:out value="${fila.etiqueta}"/>
                                        </span>
                                    </td>
                                    <td>${fila.cantidadTexto}</td>
                                    <td>$${fila.valorTexto}</td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:otherwise>
        </c:choose>
    </div>

    <div class="tarjeta">
        <h2>Recaudo por medio de pago</h2>
        <p class="descripcion-modulo">
            Lo que esta en PENDIENTE todavia no ha ingresado a la caja.
        </p>
        <c:choose>
            <c:when test="${empty pagosPorMedio}">
                <p class="sin-registros">Sin pagos registrados.</p>
            </c:when>
            <c:otherwise>
                <div class="contenedor-tabla">
                    <table class="tabla-datos">
                        <thead>
                            <tr>
                                <th scope="col">Medio de pago</th>
                                <th scope="col">Estado</th>
                                <th scope="col">Transacciones</th>
                                <th scope="col">Valor</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="fila" items="${pagosPorMedio}">
                                <tr>
                                    <td><c:out value="${fila.etiqueta}"/></td>
                                    <td>
                                        <span class="etiqueta ${fila.detalle eq 'APROBADA'
                                                ? 'etiqueta-disponible' : 'etiqueta-advertencia'}">
                                            <c:out value="${fila.detalle}"/>
                                        </span>
                                    </td>
                                    <td>${fila.cantidadTexto}</td>
                                    <td>$${fila.valorTexto}</td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:otherwise>
        </c:choose>
    </div>

</c:if>

<%-- ================================================ REPORTE DE INVENTARIO --%>
<c:if test="${tipoReporte eq 'inventario'}">

    <div class="tarjeta">
        <h2>Indicadores de inventario</h2>
        <div class="rejilla-indicadores">
            <div class="indicador indicador-destacado">
                <span class="cifra">$${valorInventarioTexto}</span>
                <span class="rotulo">Valor de las existencias vigentes</span>
            </div>
            <div class="indicador">
                <span class="cifra">${productosAgotados}</span>
                <span class="rotulo">Productos agotados</span>
            </div>
            <div class="indicador">
                <span class="cifra">${empty bajoStock ? 0 : bajoStock.size()}</span>
                <span class="rotulo">Productos por reponer</span>
            </div>
            <div class="indicador">
                <span class="cifra">${empty lotesCriticos ? 0 : lotesCriticos.size()}</span>
                <span class="rotulo">Lotes vencidos o por vencer</span>
            </div>
        </div>
    </div>

    <div class="tarjeta">
        <h2>Productos que necesitan reposicion</h2>
        <p class="descripcion-modulo">
            Con ${umbralBajoStock} unidades vigentes o menos. Un producto en cero
            no se puede vender.
        </p>

        <c:choose>
            <c:when test="${empty bajoStock}">
                <p class="sin-registros">
                    Ningun producto esta por debajo del umbral. El inventario esta sano.
                </p>
            </c:when>
            <c:otherwise>
                <div class="contenedor-tabla">
                    <table class="tabla-datos">
                        <caption>Del mas escaso al menos escaso.</caption>
                        <thead>
                            <tr>
                                <th scope="col">Producto</th>
                                <th scope="col">Categoria</th>
                                <th scope="col">Existencias</th>
                                <th scope="col">Precio</th>
                                <th scope="col">Estado</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="fila" items="${bajoStock}">
                                <tr>
                                    <td><c:out value="${fila.etiqueta}"/></td>
                                    <td><c:out value="${fila.detalle}"/></td>
                                    <td>${fila.cantidadTexto}</td>
                                    <td>$${fila.valorTexto}</td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${fila.cantidad le 0}">
                                                <span class="etiqueta etiqueta-agotado">AGOTADO</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="etiqueta etiqueta-advertencia">ESCASO</span>
                                            </c:otherwise>
                                        </c:choose>
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
        <h2>Lotes vencidos o por vencer</h2>
        <p class="descripcion-modulo">
            Lotes que caducan en los proximos ${diasAlerta} dias o que ya
            caducaron. Un lote vencido no cuenta como existencia disponible.
        </p>

        <c:choose>
            <c:when test="${empty lotesCriticos}">
                <p class="sin-registros">
                    Ningun lote vence en los proximos ${diasAlerta} dias.
                </p>
            </c:when>
            <c:otherwise>
                <div class="contenedor-tabla">
                    <table class="tabla-datos">
                        <caption>Del mas urgente al menos urgente.</caption>
                        <thead>
                            <tr>
                                <th scope="col">Producto y lote</th>
                                <th scope="col">Vence</th>
                                <th scope="col">Unidades</th>
                                <th scope="col">Valor en riesgo</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="fila" items="${lotesCriticos}">
                                <tr>
                                    <td><c:out value="${fila.etiqueta}"/></td>
                                    <td>
                                        <c:set var="estaVencido"
                                               value="${not empty fila.detalle
                                                        and fila.detalle.endsWith('VENCIDO')}"/>
                                        <span class="etiqueta ${estaVencido
                                                ? 'etiqueta-agotado' : 'etiqueta-advertencia'}">
                                            <c:out value="${fila.detalle}"/>
                                        </span>
                                    </td>
                                    <td>${fila.cantidadTexto}</td>
                                    <td>$${fila.valorTexto}</td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:otherwise>
        </c:choose>
    </div>

    <div class="tarjeta">
        <h2>Existencias por categoria</h2>
        <c:choose>
            <c:when test="${empty stockPorCategoria}">
                <p class="sin-registros">Sin datos para agrupar.</p>
            </c:when>
            <c:otherwise>
                <div class="contenedor-tabla">
                    <table class="tabla-datos">
                        <thead>
                            <tr>
                                <th scope="col">Categoria</th>
                                <th scope="col">Unidades vigentes</th>
                                <th scope="col">Valor</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="fila" items="${stockPorCategoria}">
                                <tr>
                                    <td><c:out value="${fila.etiqueta}"/></td>
                                    <td>${fila.cantidadTexto}</td>
                                    <td>$${fila.valorTexto}</td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:otherwise>
        </c:choose>
    </div>

</c:if>

<%@ include file="/WEB-INF/jsp/comunes/pie.jsp" %>
