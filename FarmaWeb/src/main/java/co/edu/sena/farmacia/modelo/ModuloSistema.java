package co.edu.sena.farmacia.modelo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Describe una pestana del sistema y los roles que la tienen habilitada.
 *
 * No corresponde a una tabla de la base de datos: representa el mapa de
 * navegacion del proyecto para que el administrador pueda consultar, desde
 * el modulo de Roles, que ve cada perfil cuando entra con su cuenta.
 */
public class ModuloSistema {

    private final String ruta;
    private final String etiqueta;
    private final String zona;
    private final String proposito;
    private final boolean exigeCuenta;
    private final List<String> rolesConAcceso;

    /**
     * Crea la descripcion de un modulo del sistema.
     *
     * @param ruta direccion con la que se abre, por ejemplo /productos
     * @param etiqueta texto que se muestra en la pestana del menu
     * @param zona area a la que pertenece: gestion o tienda
     * @param proposito para que sirve la pestana, en lenguaje del usuario
     * @param exigeCuenta true cuando se necesita iniciar sesion para verla
     * @param rolesConAcceso nombres de los roles que la tienen habilitada
     */
    public ModuloSistema(String ruta, String etiqueta, String zona, String proposito,
            boolean exigeCuenta, List<String> rolesConAcceso) {
        this.ruta = ruta;
        this.etiqueta = etiqueta;
        this.zona = zona;
        this.proposito = proposito;
        this.exigeCuenta = exigeCuenta;
        this.rolesConAcceso = Collections.unmodifiableList(new ArrayList<>(rolesConAcceso));
    }

    /**
     * Indica si el rol recibido tiene habilitada esta pestana.
     *
     * La comparacion no distingue mayusculas de minusculas para que coincida
     * con el nombre guardado en la tabla TblRol.
     *
     * @param nombreRol nombre del rol que se quiere verificar
     * @return true cuando el rol puede abrir el modulo
     */
    public boolean tieneAcceso(String nombreRol) {
        if (nombreRol == null) {
            return false;
        }
        for (String permitido : rolesConAcceso) {
            if (permitido.equalsIgnoreCase(nombreRol)) {
                return true;
            }
        }
        return false;
    }

    public String getRuta() {
        return ruta;
    }

    /**
     * Entrega la ruta sin la barra inicial, que es la palabra con la que
     * cada pagina JSP marca la pestana activa del menu.
     *
     * @return clave del modulo, por ejemplo productos
     */
    public String getClave() {
        return ruta.startsWith("/") ? ruta.substring(1) : ruta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public String getZona() {
        return zona;
    }

    public String getProposito() {
        return proposito;
    }

    public boolean getExigeCuenta() {
        return exigeCuenta;
    }

    @Override
    public String toString() {
        return etiqueta;
    }
}
