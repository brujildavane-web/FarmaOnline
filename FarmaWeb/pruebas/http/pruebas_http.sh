#!/bin/bash
# Pruebas funcionales por HTTP (curl) de la recuperacion de contrasena, las
# notificaciones y el control de acceso. Requiere la aplicacion en marcha y la
# base de datos recien cargada con bd_farmacia_completa.sql.
#   bash pruebas_http.sh
B=${1:-http://localhost:8080/FarmaOnline}
J=$(mktemp); A=$(mktemp); D=$(mktemp)
ok() { if [ "$2" = "1" ]; then echo "[APROBADO]  $1"; else echo "[RECHAZADO] $1"; fi; }
sen() { echo "$1" | grep -c "$2" | awk '{print ($1>0)?1:0}'; }

R=$(curl -s -X POST $B/recuperar -d "email=")
ok "CP-18 Recuperar con el correo vacio -> 'Debe digitar su correo electronico.'" "$(sen "$R" "Debe digitar su correo")"

R=$(curl -s -X POST $B/recuperar -d "email=nadie@correo.com")
G=$(sen "$R" "Si el correo")
S=$(echo "$R" | grep -c "enlace-recuperacion")
ok "CP-19 Correo no registrado -> respuesta generica y sin enlace" "$([ "$G" = 1 ] && [ "$S" = 0 ] && echo 1 || echo 0)"

R=$(curl -s -X POST $B/recuperar -d "email=laura.gomez@correo.com")
COD=$(echo "$R" | grep -o 'codigo=[0-9a-f]*' | head -1 | cut -d= -f2)
ok "CP-20 Solicitar enlace con laura.gomez@correo.com -> enlace de 64 caracteres" "$([ ${#COD} = 64 ] && echo 1 || echo 0)"

R=$(curl -s -X POST $B/restablecer -d "codigo=$COD&password=nueva123&password2=otra999")
ok "CP-21 Contrasenas distintas -> 'Las dos contrasenas no coinciden.'" "$(sen "$R" "Las dos contrasenas no coinciden")"

R=$(curl -s -X POST $B/restablecer -d "codigo=$COD&password=abc&password2=abc")
ok "CP-22 Contrasena de 3 caracteres -> 'al menos 6 caracteres'" "$(sen "$R" "al menos 6 caracteres")"

L=$(curl -s -i -X POST $B/restablecer -d "codigo=$COD&password=nueva123&password2=nueva123" | grep -i "^Location" | tr -d '\r')
ok "CP-23 Contrasena valida -> redirige a /login ($L)" "$(sen "$L" "/login")"

R=$(curl -s "$B/restablecer?codigo=$COD")
ok "CP-24 Reutilizar el enlace -> 'Este enlace no es valido'" "$(sen "$R" "Este enlace no es")"

V=$(curl -s -c $J -b $J -X POST $B/login -d "email=laura.gomez@correo.com&password=cliente123")
N=$(curl -s -c $J -b $J -i -X POST $B/login -d "email=laura.gomez@correo.com&password=nueva123" | grep -i "^Location" | tr -d '\r')
ok "CP-25 Contrasena anterior rechazada y la nueva entra ($N)" "$([ "$(sen "$V" "credenciales digitadas no son correctas")" = 1 ] && [ "$(sen "$N" "/micuenta")" = 1 ] && echo 1 || echo 0)"

curl -s -c $A -b $A -o /dev/null -X POST $B/login -d "email=admin@farmacia.com&password=admin123"
curl -s -c $A -b $A -o /dev/null -X POST $B/pedidos -d "accion=cambiarEstado&idPedido=2&estado=CONFIRMADO"
curl -s -c $D -b $D -o /dev/null -X POST $B/login -d "email=diego.martinez@correo.com&password=cliente123"
CONT=$(curl -s -b $D $B/micuenta | grep -o 'contador-notificaciones">[0-9]*' | grep -o '[0-9]*$')
AV=$(curl -s -b $D $B/notificaciones | grep -o "Su pedido 2 cambio de estado a CONFIRMADO")
ok "CP-26 Cambio de estado del pedido 2 -> el cliente ve el contador ($CONT) y el aviso" "$([ -n "$AV" ] && [ -n "$CONT" ] && echo 1 || echo 0)"

P=$(curl -s -b $J $B/notificaciones | grep -c "contrasena fue restablecida")
ok "CP-27 Aviso de contrasena restablecida en las notificaciones de Laura" "$([ "$P" -gt 0 ] && echo 1 || echo 0)"

rm -f $J $A $D
