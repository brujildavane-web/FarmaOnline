# ModuloFarma — Codificación de módulos del software

Evidencia **GA7‐220501096‐AA2‐EV01** del proyecto formativo 
SENA — Tecnólogo en Análisis y Desarrollo de Software — Ficha 3235909.

## Contenido del repositorio

| Carpeta / archivo | Descripción |
|---|---|
| [FarmaWeb/](FarmaWeb/) | **Entrega actual.** Aplicación web Java con los 13 módulos del modelo relacional: Servlets, JSP, JDBC y arquitectura en tres capas. Ver [FarmaWeb/README.md](FarmaWeb/README.md) |
| [script.js](script.js), [config/](config/), [controllers/](controllers/), [public/](public/) | Primera versión del módulo de medicamentos en Node.js, conservada como evidencia del avance anterior |
| [Script_BD_Farmacia.sql](Script_BD_Farmacia.sql) | Script DDL original del modelo relacional (GA6‐220501096‐AA2‐EV02) |
| [BD_Evidencia_Farmacia.mwb](BD_Evidencia_Farmacia.mwb) | Modelo relacional en MySQL Workbench |

## Cómo ejecutar

Las instrucciones completas de instalación, creación de la base de datos y ejecución
están en [FarmaWeb/README.md](FarmaWeb/README.md).

Resumen:

```bash
# 1. Iniciar MySQL desde el panel de XAMPP
# 2. Crear la base de datos con datos de prueba
C:\xampp\mysql\bin\mysql.exe -u root < FarmaWeb/src/main/resources/sql/bd_farmacia_completa.sql

# 3. Compilar y levantar el servidor (Tomcat embebido)
cd FarmaWeb
mvn clean package
mvn cargo:run

# 4. Abrir http://localhost:8080/farmaweb
```

Requiere **JDK 17** y **Maven 3.9+** instalados.

## Estructura del sistema

La aplicación tiene dos zonas y tres perfiles de acceso.

### Tienda pública, sin cuenta

Página de inicio, catálogo por categorías, detalle de producto y carrito de
compras. Cualquier visitante puede recorrerla y armar su carrito; la cuenta se
solicita solo al confirmar el pedido. Quien se registra recibe automáticamente
el rol **Cliente**, el único que se asigna sin intervención del administrador.

### Panel de gestión, con cuenta y rol autorizado

Los 13 módulos derivados del modelo relacional, más el tablero de indicadores:

Roles · Usuarios · Clientes · Farmacéuticos · Productos · Inventario por lotes ·
Favoritos · Pedidos · Detalle de pedidos · Fórmulas médicas · Historial ·
Pasarelas de pago · Transacciones de pago

Cada módulo ofrece consulta, registro, edición y eliminación, con validaciones de
negocio en la capa de lógica. Esta zona está reservada a los perfiles
**Administrador** y **Farmacéutico**; un cliente autenticado que intente entrar
es devuelto a su cuenta.

### Perfiles

| Rol | Quién lo asigna | A dónde entra |
|---|---|---|
| Cliente | Se asigna solo al registrarse en la tienda | Mi cuenta: pedidos, favoritos y fórmulas |
| Farmacéutico | El administrador, desde el módulo de Usuarios | Panel de gestión |
| Administrador | El administrador, desde el módulo de Usuarios | Panel de gestión completo |

## Identidad visual

Paleta definida en la evidencia GA5‐220501095‐AA1‐EV04:
verde `#2E7D32` (principal), blanco `#FFFFFF` (secundario), gris `#F5F5F5`
(complementario), tipografía Roboto/Arial y contraste conforme a WCAG.
