<%--
    Tarjeta de un producto del catalogo.

    Fragmento reutilizado por el inicio y por el catalogo. Se incluye dentro
    de un c:forEach cuya variable debe llamarse producto.

    Si el producto tiene imagen registrada se muestra la foto; si no, se
    dibuja un recuadro con la letra inicial de su nombre.
--%>
<article class="producto ${producto.disponible ? '' : 'agotado'}">

    <div class="imagen">
        <c:choose>
            <c:when test="${producto.conImagen}">
                <img src="${raiz}/img/productos/${producto.imagen}"
                     alt="Fotografía de ${producto.nombre}"
                     style="max-height:130px; object-fit:contain;">
            </c:when>
            <c:otherwise>
                <span aria-hidden="true">${producto.inicial}</span>
            </c:otherwise>
        </c:choose>
    </div>

    <div class="cuerpo">
        <a class="nombre" href="${raiz}/producto?id=${producto.idProducto}">
            <c:out value="${producto.nombre}"/>
        </a>

        <span class="ayuda">
            <c:out value="${producto.skuCode}"/>
            <c:if test="${not empty producto.categoria}">
                &middot; <c:out value="${producto.categoria}"/>
            </c:if>
        </span>

        <span class="precio">
            $${producto.precioTexto}
        </span>

        <div class="etiquetas">
            <c:choose>
                <c:when test="${producto.disponible}">
                    <span class="etiqueta etiqueta-disponible">Disponible</span>
                </c:when>
                <c:otherwise>
                    <span class="etiqueta etiqueta-agotado">Agotado</span>
                </c:otherwise>
            </c:choose>

            <c:if test="${producto.requiereReceta}">
                <span class="etiqueta etiqueta-advertencia">Requiere receta</span>
            </c:if>
        </div>

        <div class="pie">
            <c:choose>
                <c:when test="${producto.disponible}">
                    <%-- Agregar al carrito modifica la sesion: se usa POST --%>
                    <form action="${raiz}/carrito" method="post">
                        <input type="hidden" name="accion" value="agregar">
                        <input type="hidden" name="idProducto" value="${producto.idProducto}">
                        <input type="hidden" name="cantidad" value="1">
                        <button type="submit">Agregar al carrito</button>
                    </form>
                </c:when>
                <c:otherwise>
                    <button type="button" class="boton-secundario" disabled>Sin existencias</button>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</article>
