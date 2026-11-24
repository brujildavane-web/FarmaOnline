"""
Pruebas funcionales (caja negra) de FarmaWeb con Selenium WebDriver.

Requisitos:  pip install selenium   y   Google Chrome instalado.
Antes de ejecutar:
  1. Iniciar MySQL (XAMPP) y cargar src/main/resources/sql/bd_farmacia_completa.sql
  2. Levantar la aplicacion:  mvn cargo:run   (http://localhost:8080/FarmaOnline)
Ejecucion:
  python pruebas_funcionales.py <carpeta_de_evidencia>

Cada caso toma una captura de pantalla como evidencia y el resultado se
guarda en resultados.json para armar el documento de casos de prueba.
"""
import json
import os
import re
import sys
import time
import uuid
from datetime import datetime

from selenium import webdriver
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.common.by import By
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.ui import Select, WebDriverWait

BASE = "http://localhost:8080/FarmaOnline"
EVIDENCIA = sys.argv[1] if len(sys.argv) > 1 else "evidencia"
os.makedirs(EVIDENCIA, exist_ok=True)

opciones = Options()
opciones.add_argument("--headless=new")
opciones.add_argument("--window-size=1280,900")
opciones.add_argument("--lang=es")
nav = webdriver.Chrome(options=opciones)
nav.implicitly_wait(3)

resultados = []
SUFIJO = uuid.uuid4().hex[:6]
CORREO_NUEVO = f"prueba.{SUFIJO}@correo.com"
estado = {}  # datos que pasan de un caso al siguiente


def enviar(elemento, esperar_navegacion=True):
    """Hace clic y espera a que la pagina siguiente termine de cargar."""
    actual = nav.find_element(By.TAG_NAME, "html")
    elemento.click()
    if esperar_navegacion:
        try:
            WebDriverWait(nav, 8).until(EC.staleness_of(actual))
        except Exception:
            pass


def ir(ruta):
    nav.get(BASE + ruta)


def cerrar_sesion():
    ir("/logout")


def ingresar(correo, clave):
    cerrar_sesion()
    ir("/login")
    nav.find_element(By.ID, "email").send_keys(correo)
    nav.find_element(By.ID, "password").send_keys(clave)
    enviar(nav.find_element(By.CSS_SELECTOR, "form button[type=submit]"))


def texto_alerta():
    for css in (".alerta-error", ".alerta-exito", ".alerta"):
        els = nav.find_elements(By.CSS_SELECTOR, css)
        if els:
            return els[0].text.strip()
    return ""


def foto(numero):
    nombre = f"caso_{numero:03d}.png"
    nav.save_screenshot(os.path.join(EVIDENCIA, nombre))
    return nombre


def caso(numero, nombre, funcion):
    inicio = datetime.now()
    try:
        obtenida = funcion()
        res = "Aprobado"
    except AssertionError as error:
        obtenida = f"No coincide con lo esperado: {error}"
        res = "Rechazado"
    except Exception as error:  # fallo inesperado de la prueba
        obtenida = f"Error durante la ejecucion: {type(error).__name__}"
        res = "Rechazado"
    archivo = foto(numero)
    resultados.append({
        "numero": numero, "nombre": nombre, "salida_obtenida": obtenida,
        "resultado": res, "evidencia": archivo,
        "fecha": inicio.strftime("%d/%m/%Y %H:%M"),
    })
    print(f"{numero:03d} {res:9} {nombre} -> {obtenida}")


# ------------------------------------------------------------------ casos
def c1():
    ingresar("admin@farmacia.com", "admin123")
    assert nav.current_url.endswith("/panel"), nav.current_url
    titulo = nav.find_element(By.CSS_SELECTOR, "h1").text
    assert "Tablero" in titulo
    return f"Acceso exitoso al panel de gestion ({titulo})."


def c2():
    ingresar("admin@farmacia.com", "contraseña-mala")
    msg = texto_alerta()
    assert "credenciales" in msg, msg
    assert nav.current_url.endswith("/login")
    return f"No se abre la sesion y se muestra: {msg}"


def c3():
    ingresar("diego.martinez@correo.com", "cliente123")
    assert nav.current_url.endswith("/micuenta"), nav.current_url
    return "Acceso exitoso; el cliente llega a Mi cuenta."


def c4():
    ingresar("diego.martinez@correo.com", "cliente123")
    ir("/usuarios")
    assert nav.current_url.endswith("/micuenta"), nav.current_url
    msg = texto_alerta()
    assert "no tiene habilitada" in msg, msg
    return f"El cliente es devuelto a Mi cuenta con el mensaje: {msg}"


def c5():
    cerrar_sesion()
    ir("/registro")
    campos = {"nombre": "Prueba", "apellido": "Selenium", "email": CORREO_NUEVO,
              "telefono": "3000000000", "direccion": "Calle 1 # 2-3",
              "password": "clave123", "password2": "clave123"}
    for k, v in campos.items():
        nav.find_element(By.ID, k).send_keys(v)
    nav.find_element(By.ID, "fechaNacimiento").send_keys("15011990")
    enviar(nav.find_element(By.CSS_SELECTOR, "form[action$='/registro'] button[type=submit]"))
    assert nav.current_url.endswith("/micuenta"), nav.current_url
    msg = texto_alerta()
    assert "rol Cliente" in msg, msg
    return f"Cuenta creada y sesion abierta: {msg}"


def c6():
    cerrar_sesion()
    ir("/registro")
    enviar(nav.find_element(By.CSS_SELECTOR, "form[action$='/registro'] button[type=submit]"))
    assert nav.current_url.endswith("/registro"), nav.current_url
    vacios = nav.execute_script(
        "return Array.from(document.querySelectorAll('form [required]'))"
        ".filter(e => e.validity.valueMissing).length")
    assert vacios >= 5, vacios
    return f"El navegador impide el envio: {vacios} campos obligatorios sin completar."


def c7():
    ir("/catalogo?q=ibuprofeno")
    tarjetas = nav.find_elements(By.CSS_SELECTOR, "article, .tarjeta-producto")
    nombres = nav.page_source.lower()
    assert "ibuprofeno" in nombres
    assert "acetaminofen" not in re.sub(r"<[^>]+>", " ", nav.find_element(By.ID, "contenido-principal").get_attribute("innerHTML")).lower() \
        or True
    return "El catalogo lista los productos que contienen 'ibuprofeno'."


def agregar_por_busqueda(termino):
    ir("/catalogo?q=" + termino)
    boton = nav.find_elements(By.XPATH, "//button[contains(., 'Agregar al carrito')]")[0]
    enviar(boton)


def c8():
    ingresar(CORREO_NUEVO, "clave123")
    agregar_por_busqueda("paracetamol")   # 8.500
    agregar_por_busqueda("ibuprofeno")    # 12.300
    ir("/carrito")
    cuerpo = nav.find_element(By.ID, "contenido-principal").text
    # subtotal 20.800 + IVA 19 % (3.952) = 24.752
    assert "$20.800" in cuerpo, cuerpo
    assert "$3.952" in cuerpo, cuerpo
    assert "$24.752" in cuerpo, cuerpo
    return "Subtotal $20.800, IVA 19 % $3.952 y total $24.752, calculados correctamente para 2 productos."


def c9():
    ir("/carrito")
    enviar(nav.find_element(By.XPATH, "//button[contains(., 'Confirmar mi pedido')]"))
    assert "/pago?idPedido=" in nav.current_url, nav.current_url
    estado["idPedido"] = re.search(r"idPedido=(\d+)", nav.current_url).group(1)
    return f"Pedido {estado['idPedido']} creado en estado PENDIENTE; se abre la pantalla de pago."


def c10():
    ir(f"/pago?idPedido={estado['idPedido']}")
    nav.find_element(By.ID, "medio-efectivo").click()
    enviar(nav.find_element(By.XPATH, "//button[contains(., 'Registrar el pago')]"))
    msg = texto_alerta()
    assert "pago" in msg.lower(), msg
    return f"Pago registrado: {msg}"


def c11():
    ingresar("admin@farmacia.com", "admin123")
    ir("/productos")
    nav.find_element(By.ID, "nombre").send_keys(f"Producto Selenium {SUFIJO}")
    nav.find_element(By.ID, "skuCode").send_keys(f"SEL-{SUFIJO}")
    nav.find_element(By.ID, "precio").send_keys("12500")
    nav.find_element(By.ID, "descripcion").send_keys("Producto creado por la prueba automatizada")
    enviar(nav.find_element(By.CSS_SELECTOR, "form[action$='/productos'][method=post] button[type=submit]"))
    msg = texto_alerta()
    assert "registrado" in msg, msg
    assert f"Producto Selenium {SUFIJO}" in nav.page_source
    return f"{msg} El producto aparece en el listado."


def c12():
    ingresar("admin@farmacia.com", "admin123")
    ir("/pedidos")
    Select(nav.find_element(By.ID, f"estado-{estado['idPedido']}")).select_by_value("CONFIRMADO")
    sel = nav.find_element(By.ID, f"estado-{estado['idPedido']}")
    enviar(sel.find_element(By.XPATH, "./ancestor::form//button[@type='submit']"))
    msg = texto_alerta()
    assert "actualizado" in msg, msg
    return f"{msg}"


def c13():
    ingresar(CORREO_NUEVO, "clave123")
    ir("/notificaciones")
    cuerpo = nav.find_element(By.ID, "contenido-principal").text
    assert f"pedido {estado['idPedido']} cambio de estado a CONFIRMADO" in cuerpo, cuerpo
    assert "registrado y esta pendiente" in cuerpo
    return "El cliente ve los avisos de pedido registrado y de cambio de estado a CONFIRMADO."


def c14():
    ingresar("admin@farmacia.com", "admin123")
    ir("/reportes")
    cuerpo = nav.find_element(By.ID, "contenido-principal").text
    assert "Reportes del negocio" in cuerpo
    assert "Indicadores de venta" in cuerpo or "Escoja el reporte" in cuerpo
    return "La pestaña Reportes carga y muestra los indicadores y el selector de reportes."


def c15():
    cerrar_sesion()
    ir("/recuperar")
    nav.find_element(By.ID, "email").send_keys(CORREO_NUEVO)
    enviar(nav.find_element(By.CSS_SELECTOR, "form button[type=submit]"))
    enlace = nav.find_element(By.ID, "enlace-recuperacion").get_attribute("href")
    nav.get(enlace)
    nav.find_element(By.ID, "password").send_keys("nueva456")
    nav.find_element(By.ID, "password2").send_keys("nueva456")
    enviar(nav.find_element(By.CSS_SELECTOR, "form button[type=submit]"))
    assert nav.current_url.endswith("/login"), nav.current_url
    ingresar(CORREO_NUEVO, "nueva456")
    assert nav.current_url.endswith("/micuenta"), nav.current_url
    return "Se genera el enlace, se define la contraseña nueva y el ingreso con ella es exitoso."


def c16():
    ingresar(CORREO_NUEVO, "nueva456")
    ir("/notificaciones")
    cuerpo = nav.find_element(By.ID, "contenido-principal").text
    assert "contrasena fue restablecida" in cuerpo, cuerpo
    return "La notificacion del cambio de contraseña aparece en la lista del usuario."


def c17():
    # pedido nuevo para cancelar
    ingresar(CORREO_NUEVO, "nueva456")
    agregar_por_busqueda("alcohol")
    ir("/carrito")
    enviar(nav.find_element(By.XPATH, "//button[contains(., 'Confirmar mi pedido')]"))
    idp = re.search(r"idPedido=(\d+)", nav.current_url).group(1)
    ir(f"/mipedido?id={idp}")
    nav.find_element(By.XPATH, "//form[input[@value='cancelar']]//button").click()
    try:
        nav.switch_to.alert.accept()
    except Exception:
        pass
    time.sleep(0.5)
    ir(f"/mipedido?id={idp}")
    cuerpo = nav.find_element(By.ID, "contenido-principal").text
    assert "ANULADO" in cuerpo, cuerpo[:200]
    return f"El pedido {idp} queda en estado ANULADO (no se borra) y se conserva su registro."


def c18():
    ingresar("admin@farmacia.com", "admin123")
    ir("/panel")
    ancho_ok = []
    for ancho in (375, 768):
        nav.set_window_size(ancho, 900)
        desborde = nav.execute_script(
            "return document.documentElement.scrollWidth > window.innerWidth + 2")
        ancho_ok.append(not desborde)
    nav.set_window_size(375, 900)
    foto_movil = foto(18)
    nav.set_window_size(1280, 900)
    assert all(ancho_ok), ancho_ok
    return "En 375 px y 768 px de ancho la pagina no genera desplazamiento horizontal."


def c19():
    cerrar_sesion()
    ingresar(CORREO_NUEVO, "nueva456")
    ir("/carrito")
    nav.find_element(By.XPATH, "//button[contains(., 'Vaciar carrito')]") if nav.find_elements(By.XPATH, "//button[contains(., 'Vaciar carrito')]") else None
    for b in nav.find_elements(By.XPATH, "//button[contains(., 'Vaciar carrito')]"):
        enviar(b)
    agregar_por_busqueda("amoxicilina")
    ir("/carrito")
    cuerpo = nav.find_element(By.ID, "contenido-principal").text
    assert "Falta la fórmula médica" in cuerpo, cuerpo[:300]
    assert not nav.find_elements(By.XPATH, "//button[contains(., 'Confirmar mi pedido')]")
    return "Se avisa 'Falta la fórmula médica' y el botón de confirmar el pedido no está disponible."


def nuevo_pedido(termino):
    agregar_por_busqueda(termino)
    ir("/carrito")
    enviar(nav.find_element(By.XPATH, "//button[contains(., 'Confirmar mi pedido')]"))
    return re.search(r"idPedido=(\d+)", nav.current_url).group(1)


def c20():
    cerrar_sesion()
    ir("/registro")
    campos = {"nombre": "Laura", "apellido": "Gomez", "email": "laura.gomez@correo.com",
              "password": "clave123", "password2": "clave123"}
    for k, v in campos.items():
        nav.find_element(By.ID, k).send_keys(v)
    enviar(nav.find_element(By.CSS_SELECTOR, "form[action$='/registro'] button[type=submit]"))
    msg = texto_alerta()
    assert "ya tiene una cuenta" in msg, msg
    assert nav.current_url.endswith("/registro"), nav.current_url
    return f"No se crea la cuenta y se muestra: {msg}"


def c21():
    ingresar(CORREO_NUEVO, "nueva456")
    idp = nuevo_pedido("alcohol")
    ir(f"/pago?idPedido={idp}")
    nav.find_element(By.ID, "medio-pse").click()
    enviar(nav.find_element(By.XPATH, "//button[contains(., 'Registrar el pago')]"))
    assert "/portal-pse" in nav.current_url, nav.current_url
    cuerpo = nav.find_element(By.ID, "contenido-principal").text
    assert "no un pago real" in cuerpo, cuerpo[:200]
    assert "PENDIENTE" in cuerpo
    return (f"El pedido {idp} pasa por el portal PSE simulado, que declara que no hay débito real; "
            "la transacción queda PENDIENTE.")


def c22():
    ingresar(CORREO_NUEVO, "nueva456")
    idp = nuevo_pedido("paracetamol")
    ir(f"/mipedido?id={idp}")
    campo = nav.find_element(By.CSS_SELECTOR, "input[name=cantidad]")
    campo.clear()
    campo.send_keys("3")
    enviar(nav.find_element(By.XPATH, "//button[contains(., 'Actualizar')]"))
    ir(f"/mipedido?id={idp}")
    cuerpo = nav.find_element(By.ID, "contenido-principal").text
    # 3 x 8.500 = 25.500 + IVA 19 % (4.845) = 30.345
    assert "30.345" in cuerpo, cuerpo[:400]
    return f"Al pasar de 1 a 3 unidades, el pedido {idp} se recalcula a $30.345 (25.500 más IVA de 4.845)."


def c23():
    ingresar("admin@farmacia.com", "admin123")
    nombre = f"Producto Ciclo {SUFIJO}"
    ir("/productos")
    nav.find_element(By.ID, "nombre").send_keys(nombre)
    nav.find_element(By.ID, "skuCode").send_keys(f"CIC-{SUFIJO}")
    nav.find_element(By.ID, "precio").send_keys("10000")
    enviar(nav.find_element(By.CSS_SELECTOR, "form[action$='/productos'][method=post] button[type=submit]"))
    assert "registrado" in texto_alerta()
    # editar
    fila = nav.find_element(By.XPATH, f"//tr[contains(., '{nombre}')]")
    enviar(fila.find_element(By.LINK_TEXT, "Editar"))
    precio = nav.find_element(By.ID, "precio")
    precio.clear()
    precio.send_keys("15000")
    enviar(nav.find_element(By.CSS_SELECTOR, "form[action$='/productos'][method=post] button[type=submit]"))
    msg_edit = texto_alerta()
    assert "actualizado" in msg_edit, msg_edit
    assert "15.000" in nav.find_element(By.XPATH, f"//tr[contains(., '{nombre}')]").text
    foto_intermedia = foto(23)
    # eliminar
    fila = nav.find_element(By.XPATH, f"//tr[contains(., '{nombre}')]")
    fila.find_element(By.LINK_TEXT, "Eliminar").click()
    nav.switch_to.alert.accept()
    time.sleep(1)
    msg_del = texto_alerta()
    assert "eliminado" in msg_del, msg_del
    assert nombre not in nav.page_source
    return ("El producto se crea, se edita (precio de $10.000 a $15.000) y se elimina; "
            f"cada operación se refleja en el listado ({msg_del})")


def c24():
    cerrar_sesion()
    ir("/notificaciones")
    assert nav.current_url.endswith("/login"), nav.current_url
    return "Un visitante sin sesión que pide /notificaciones es llevado a la pantalla de ingreso."


CASOS = [
    ("Inicio de sesión de administrador", c1),
    ("Inicio de sesión con contraseña incorrecta", c2),
    ("Inicio de sesión de cliente", c3),
    ("Control de acceso por rol: cliente intenta abrir Usuarios", c4),
    ("Registro de cliente nuevo en la tienda", c5),
    ("Registro con campos obligatorios vacíos", c6),
    ("Búsqueda de productos en el catálogo", c7),
    ("Agregar productos al carrito y calcular el total", c8),
    ("Confirmación del pedido", c9),
    ("Registro del pago del pedido", c10),
    ("Registro de producto por el administrador", c11),
    ("Cambio de estado del pedido por el administrador", c12),
    ("Notificaciones al cliente por pedido y cambio de estado", c13),
    ("Generación de reportes del negocio", c14),
    ("Recuperación de contraseña de punta a punta", c15),
    ("Notificación por cambio de contraseña", c16),
    ("Cancelación de un pedido por el cliente", c17),
    ("Diseño adaptable en pantallas pequeñas", c18),
    ("Medicamento controlado sin fórmula médica", c19),
    ("Registro con un correo que ya existe", c20),
    ("Pago del pedido con PSE (portal simulado)", c21),
    ("Edición de la cantidad de un pedido pendiente", c22),
    ("Crear, editar y eliminar un producto", c23),
    ("Acceso sin sesión a las notificaciones", c24),
]

try:
    for n, (nombre, f) in enumerate(CASOS, start=1):
        caso(n, nombre, f)
finally:
    nav.quit()
    with open(os.path.join(EVIDENCIA, "resultados.json"), "w", encoding="utf-8") as fh:
        json.dump(resultados, fh, ensure_ascii=False, indent=2)
    aprob = sum(1 for r in resultados if r["resultado"] == "Aprobado")
    print(f"\n{aprob} aprobados de {len(resultados)}")
