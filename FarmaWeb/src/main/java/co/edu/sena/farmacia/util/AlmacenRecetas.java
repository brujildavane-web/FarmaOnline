package co.edu.sena.farmacia.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import co.edu.sena.farmacia.logica.ExcepcionNegocio;
import jakarta.servlet.http.Part;

/**
 * Guarda en disco los archivos de las formulas medicas que adjuntan los
 * clientes.
 *
 * Los archivos no se guardan dentro de la aplicacion web: esa carpeta se
 * borra en cada despliegue y los documentos se perderian. Se usa una carpeta
 * externa, configurable con el parametro carpetaRecetas de web.xml.
 *
 * En la base de datos solo queda el nombre del archivo, en la columna
 * For_Archivo de TblFormulaMedica.
 */
public final class AlmacenRecetas {

    /** Formatos aceptados, segun el requisito del sistema. */
    public static final List<String> FORMATOS_PERMITIDOS = Arrays.asList("pdf", "jpg", "jpeg", "png");

    /** Tamano maximo admitido para el archivo de la receta. */
    public static final long TAMANO_MAXIMO_BYTES = 5L * 1024 * 1024;

    /** Carpeta que se usa cuando web.xml no indica ninguna. */
    private static final String CARPETA_POR_DEFECTO = "farmaweb-recetas";

    private AlmacenRecetas() {
    }

    /**
     * Resuelve la carpeta donde se guardan las recetas.
     *
     * @param carpetaConfigurada valor del parametro carpetaRecetas de web.xml
     * @return ruta de la carpeta, creada si no existia
     */
    public static Path resolverCarpeta(String carpetaConfigurada) {
        Path carpeta;

        if (ConversorDatos.estaVacio(carpetaConfigurada)) {
            carpeta = Paths.get(System.getProperty("user.home"), CARPETA_POR_DEFECTO);
        } else {
            carpeta = Paths.get(carpetaConfigurada.trim());
        }

        try {
            Files.createDirectories(carpeta);
        } catch (IOException error) {
            throw new ExcepcionNegocio(
                    "No fue posible preparar la carpeta de formulas medicas: " + carpeta);
        }
        return carpeta;
    }

    /**
     * Valida y guarda el archivo que adjunto el cliente.
     *
     * El nombre se genera a partir del producto y la fecha, para no depender
     * del nombre original y para que quepa en la columna For_Archivo, que
     * admite 50 caracteres.
     *
     * @param parte archivo recibido en la peticion multipart
     * @param idProducto producto al que corresponde la prescripcion
     * @param carpetaConfigurada valor del parametro carpetaRecetas de web.xml
     * @return nombre con que quedo guardado el archivo
     */
    public static String guardar(Part parte, int idProducto, String carpetaConfigurada) {
        validar(parte);

        String extension = obtenerExtension(parte.getSubmittedFileName());
        String nombreArchivo = String.format("receta-p%d-%s-%d.%s",
                idProducto, LocalDate.now(), System.currentTimeMillis() % 100000, extension);

        Path destino = resolverCarpeta(carpetaConfigurada).resolve(nombreArchivo);

        try (InputStream flujo = parte.getInputStream()) {
            Files.copy(flujo, destino, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException error) {
            throw new ExcepcionNegocio("No fue posible guardar el archivo de la formula medica.");
        }

        return nombreArchivo;
    }

    /**
     * Comprueba que el archivo exista, no este vacio, no exceda el tamano
     * maximo y tenga un formato permitido.
     *
     * @param parte archivo recibido en la peticion
     */
    private static void validar(Part parte) {
        if (parte == null || parte.getSize() == 0
                || ConversorDatos.estaVacio(parte.getSubmittedFileName())) {
            throw new ExcepcionNegocio("Debe seleccionar el archivo de la formula medica.");
        }

        if (parte.getSize() > TAMANO_MAXIMO_BYTES) {
            throw new ExcepcionNegocio("El archivo supera el tamano maximo de 5 MB.");
        }

        String extension = obtenerExtension(parte.getSubmittedFileName());
        if (!FORMATOS_PERMITIDOS.contains(extension)) {
            throw new ExcepcionNegocio(
                    "La formula medica debe estar en formato PDF, JPG o PNG.");
        }
    }

    /**
     * Extrae la extension del nombre original del archivo.
     *
     * @param nombreOriginal nombre con que el cliente subio el archivo
     * @return extension en minusculas, sin el punto
     */
    private static String obtenerExtension(String nombreOriginal) {
        if (ConversorDatos.estaVacio(nombreOriginal)) {
            return "";
        }

        int posicionPunto = nombreOriginal.lastIndexOf('.');
        if (posicionPunto < 0 || posicionPunto == nombreOriginal.length() - 1) {
            return "";
        }
        return nombreOriginal.substring(posicionPunto + 1).toLowerCase();
    }
}
