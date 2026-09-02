# FarmaWeb — Módulos del Sistema de Gestión de Farmacia en Línea

Aplicación web Java del proyecto formativo. Consta de una **tienda pública** que no
exige cuenta y un **panel de gestión** con los **13 módulos** derivados del modelo
relacional (evidencia GA6‐220501096‐AA2‐EV02), organizada en las tres capas definidas
en el documento de estándares de codificación GA7‐220501096‐AA1‐EV02.

74 clases Java, 29 páginas JSP y 20 servlets.

## Cumplimiento de los ítems de la evidencia

| Ítem | Dónde se evidencia |
|---|---|
| 1. Formularios HTML con servlets | 29 páginas JSP con formularios `<form>` que envían a 20 servlets en `co.edu.sena.farmacia.controlador` |
| 2. Métodos GET y POST para parámetros | GET para consultas: `?accion=editar\|eliminar\|detalle`, `?categoria=`, `?q=`, `?id=`. POST para todo lo que modifica datos: registro, edición, carrito, ingreso y confirmación de pedidos |
| 3. Elementos de JSP | Directivas `page`, `taglib` e `include`; expresiones EL con invocación de métodos; etiquetas JSTL (`c:forEach`, `c:if`, `c:choose`, `c:out`, `c:set`); objetos implícitos (`session`, `request`, `pageContext`, `response`); scriptlets; `jsp:forward`; plantillas maestras y fragmentos reutilizables |
| 4. Versionamiento del código | Repositorio Git con `.gitignore`, commits descriptivos por módulo y repositorio remoto en GitHub |

## Arquitectura en tres capas

```
src/main/java/co/edu/sena/farmacia/
├── modelo/          16 clases del dominio. POO: herencia Persona → Cliente y
│                    Farmaceutico. Carrito e ItemCarrito viven en la sesión.
├── persistencia/    13 DAOs con JDBC y PreparedStatement, más la interfaz
│                    DaoGenerico y la excepción de persistencia
├── logica/          16 servicios con las reglas de negocio y validaciones,
│                    incluidos ServicioCarrito y ServicioRegistro
├── controlador/     20 servlets (IGU ↔ Lógica), la clase base común y el
│                    filtro de seguridad con autorización por rol
└── util/            Conexión a la base, cifrado SHA-256, conversión de
                     parámetros y formato de moneda

src/main/webapp/
├── css/estilos.css              Paleta e identidad visual del proyecto
├── img/productos/               Fotografías del catálogo
├── index.jsp                    Entrada: reenvía a la tienda
└── WEB-INF/jsp/
    ├── comunes/                 Plantillas del panel (cabecera, menú, pie)
    ├── tienda/                  9 páginas de la tienda pública y sus fragmentos
    ├── login.jsp, panel.jsp, error.jsp
    └── modulos/                 Una página por cada uno de los 13 módulos
```

## Identidad visual aplicada

Tomada de la evidencia GA5‐220501095‐AA1‐EV04:

| Rol | Color |
|---|---|
| Principal | `#2E7D32` verde (salud y seguridad) |
| Secundario | `#FFFFFF` blanco (fondo, legibilidad) |
| Complementario | `#F5F5F5` gris claro (separación de secciones) |
| Hover / activo | `#1B5E20` |
| Estados de error y "Agotado" | `#C62828` |
| Alertas de caducidad | `#F9A825` |

Tipografía Roboto/Arial, contraste WCAG, etiquetas `<label>` vinculadas, enlace de salto
al contenido y diseño responsive.

## Requisitos previos

1. **JDK 17 o superior**
   Descargar Eclipse Temurin 17 (LTS) desde <https://adoptium.net/temurin/releases/?version=17>
   e instalar marcando la opción *Set JAVA_HOME variable*.
   Verificar en una terminal nueva: `java -version`

2. **Apache Maven 3.9+**
   Descargar el archivo *Binary zip* desde <https://maven.apache.org/download.cgi>,
   descomprimir en `C:\maven` y añadir `C:\maven\bin` a la variable `Path`.
   Verificar: `mvn -v`

3. **MySQL o MariaDB en ejecución**
   Abrir el panel de control de XAMPP y pulsar **Start** en la fila *MySQL*.

> No hace falta instalar Tomcat: Maven lo descarga y lo ejecuta automáticamente.

## Puesta en marcha

### 1. Crear la base de datos con datos de prueba

```bash
C:\xampp\mysql\bin\mysql.exe -u root < src/main/resources/sql/bd_farmacia_completa.sql
```

O bien abrir <http://localhost/phpmyadmin>, pestaña **SQL**, y pegar el contenido de
`src/main/resources/sql/bd_farmacia_completa.sql`.

Al final el script muestra una tabla con el conteo de registros de las 13 tablas.

### 2. Ajustar la conexión si es necesario

Editar `src/main/resources/db.properties` si el usuario o la contraseña de MySQL
son distintos de los valores por defecto de XAMPP (`root` sin contraseña).

### 3. Compilar y ejecutar

```bash
cd FarmaWeb
mvn clean package
mvn cargo:run
```

La primera ejecución descarga Tomcat 10.1 (unos 15 MB) y las dependencias.

### 4. Abrir la aplicación

<http://localhost:8080/farmaweb>

> **Nota sobre la ruta del proyecto.** La carpeta de esta evidencia contiene
> caracteres no ASCII (la `Ó` de "EJECUCIÓN" y guiones `‐` U+2010 en
> "GA7‐220501096‐AA2‐EV01"). Al construir la línea de comandos que arranca
> Tomcat esos caracteres se corrompen y el servidor falla con
> `ClassNotFoundException: org.apache.catalina.startup.Bootstrap`.
> Por eso el `pom.xml` instala y ejecuta el servidor en `C:\farmaweb-servidor`,
> una ruta sin tildes. Si mueves el proyecto a una ruta ya limpia puedes
> cambiar o quitar la propiedad `dir.tomcat`.

### Usuarios de prueba

| Correo | Contraseña | Rol |
|---|---|---|
| admin@farmacia.com | admin123 | Administrador |
| carlos.ruiz@farmacia.com | farma123 | Farmacéutico |
| laura.gomez@correo.com | cliente123 | Cliente |

Las contraseñas se almacenan cifradas con SHA‐256; nunca en texto plano.

## Tienda pública

Zona que no exige cuenta. Es la puerta de entrada del sistema.

| Pantalla | Ruta | Qué hace |
|---|---|---|
| Inicio | `/` o `/inicio` | Banner, categorías con su conteo y productos del catálogo |
| Catálogo | `/catalogo` | Filtro por categoría (`?categoria=`) y buscador (`?q=`), ambos por GET |
| Detalle de producto | `/producto?id=N` | Ficha, disponibilidad y aviso si requiere receta |
| Carrito | `/carrito` | Vive en la sesión HTTP; agregar, actualizar, quitar y confirmar por POST |
| Registro | `/registro` | Crea `TblUsuario` con rol Cliente y su fila en `TblCliente` |
| Mi cuenta | `/micuenta` | Pedidos, fórmulas y favoritos del cliente, con su rol visible |

El carrito **no se guarda en la base de datos**: `TblPedido` exige un cliente y
un farmacéutico que aún no se conocen mientras la persona solo mira el catálogo.
Al confirmar, el carrito se traduce a un `TblPedido` en estado `PENDIENTE`, sus
`TblLineaPedido` y el primer registro de `TblHistorial`. El farmacéutico se
asigna automáticamente porque su columna no admite nulos.

## Los 13 módulos de gestión y su tabla

Zona reservada a los perfiles Administrador y Farmacéutico.

| Módulo | Ruta | Tabla |
|---|---|---|
| Roles | `/roles` | `TblRol` |
| Usuarios | `/usuarios` | `TblUsuario` |
| Clientes | `/clientes` | `TblCliente` |
| Farmacéuticos | `/farmaceuticos` | `TblFarmaceutico` |
| Productos | `/productos` | `TblProducto` |
| Inventario por lotes | `/lotes` | `TblLoteProducto` |
| Favoritos | `/favoritos` | `TblFavorito` |
| Pedidos | `/pedidos` | `TblPedido` |
| Detalle de pedidos | `/lineas` | `TblLineaPedido` |
| Fórmulas médicas | `/formulas` | `TblFormulaMedica` |
| Historial | `/historial` | `TblHistorial` |
| Pasarelas de pago | `/pasarelas` | `TblPasarelaPago` |
| Transacciones | `/transacciones` | `TblTransaccionPago` |

## Reglas de negocio implementadas en la capa de lógica

- El subtotal de cada línea y el total del pedido **siempre** se recalculan en el
  servidor con el precio vigente del catálogo.
- No se puede vender más unidades de las disponibles en los lotes no vencidos.
- Los medicamentos marcados como controlados exigen una fórmula médica vigente
  asociada al pedido antes de poder agregarse.
- No se almacenan fórmulas médicas vencidas ni lotes con fecha de vencimiento pasada.
- Solo se aceptan archivos de receta en formato PDF, JPG o PNG.
- El sistema avisa si se intenta aprobar dos veces el pago del mismo pedido.
- Correos de usuario, nombres de rol, códigos SKU, registros profesionales y
  proveedores de pago no se pueden duplicar.
- El carrito no acepta más unidades de las que hay en inventario, y el stock se
  vuelve a comprobar al confirmar, porque pudo cambiar mientras el cliente compraba.
- Quien se registra en la tienda recibe siempre el rol Cliente. Los perfiles de
  Farmacéutico y Administrador solo se asignan desde el módulo de Usuarios.
- El acceso se controla por rol: un cliente autenticado que escriba la dirección
  de un módulo de gestión es devuelto a su cuenta.
- La edad del cliente se calcula a partir de su fecha de nacimiento.
- El inventario alerta los lotes que vencen en 30 días o menos.

## Despliegue alternativo en un Tomcat instalado

```bash
mvn clean package
```

Copiar `target/farmaweb.war` a la carpeta `webapps` de Tomcat 10.1 y abrir
<http://localhost:8080/farmaweb>.

## Dos decisiones técnicas que conviene conocer

**El modelo relacional se amplió.** `TblProducto` tenía 6 columnas en la
evidencia GA6‐220501096‐AA2‐EV02 y ahora tiene 8: se agregaron `Pro_Categoria`,
para poder navegar el catálogo por secciones, y `Pro_Imagen`, que guarda el
nombre del archivo ubicado en `webapp/img/productos/`. Ambas admiten nulos, así
que un producto sin clasificar o sin foto sigue siendo válido; cuando no hay
imagen el catálogo dibuja un recuadro con la letra inicial del producto.
**El diagrama y el documento de la evidencia GA6 deben actualizarse para que
coincidan.**

**El dinero no se formatea con `fmt:formatNumber`.** Esa etiqueta de JSTL no
surtía efecto en este montaje y devolvía el valor sin formato, por ejemplo
`24200.00` en lugar de `24.200`. El formato se resolvió en la capa de
utilidades con la clase `FormatoMoneda`, que aplica la convención colombiana
—punto como separador de miles y sin decimales— y expone los valores como
propiedades de texto, por ejemplo `${carrito.totalTexto}`. Así el criterio de
presentación del dinero queda en un solo lugar.

## Convenciones de código aplicadas

Según GA7‐220501096‐AA1‐EV02: clases en `PascalCase`, atributos y métodos en
`camelCase` (métodos iniciando con verbo), constantes en `UPPER_CASE`, indentación
de 4 espacios, llave de apertura al final de la declaración, línea en blanco entre
métodos y nombres descriptivos autodocumentados.
