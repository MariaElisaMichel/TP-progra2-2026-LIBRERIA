package Database;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Abre la conexión con MySQL usando los datos del archivo db.properties.
 */
public class Conexion {

    private static final String ARCHIVO = "db.properties";

    /** Devuelve una conexión nueva. Quien la pide debe cerrarla. */
    public static Connection obtener() throws SQLException {
        Properties datos = new Properties();
        try (FileInputStream archivo = new FileInputStream(ARCHIVO)) {
            datos.load(archivo);
        } catch (IOException e) {
            throw new SQLException("No se pudo leer el archivo " + ARCHIVO, e);
        }
        String url = "jdbc:mysql://" + datos.getProperty("host") + ":"
                + datos.getProperty("puerto") + "/" + datos.getProperty("base");
        return DriverManager.getConnection(url,
                datos.getProperty("usuario"), datos.getProperty("clave"));
    }
}