import Database.Conexion;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Acceso a la base de datos para los usuarios (clientes, vendedores
 * y administradores, cada uno en su propia tabla).
 */
public class UsuarioDAO {

    private static final String CLIENTES =
            "CREATE TABLE IF NOT EXISTS clientes ("
                    + "dni VARCHAR(8) NOT NULL, "
                    + "nombre VARCHAR(100) NOT NULL, "
                    + "email VARCHAR(100) NOT NULL, "
                    + "clave VARCHAR(100) NOT NULL, "
                    + "activo BOOLEAN NOT NULL DEFAULT TRUE, "
                    + "domicilio VARCHAR(150) NOT NULL, "
                    + "PRIMARY KEY (dni), "
                    + "UNIQUE (email), "
                    + "CHECK (dni REGEXP '^[0-9]{7,8}$'), "
                    + "CHECK (email LIKE '%@%'))";

    private static final String VENDEDORES =
            "CREATE TABLE IF NOT EXISTS vendedores ("
                    + "dni VARCHAR(8) NOT NULL, "
                    + "nombre VARCHAR(100) NOT NULL, "
                    + "email VARCHAR(100) NOT NULL, "
                    + "clave VARCHAR(100) NOT NULL, "
                    + "activo BOOLEAN NOT NULL DEFAULT TRUE, "
                    + "PRIMARY KEY (dni), "
                    + "UNIQUE (email), "
                    + "CHECK (dni REGEXP '^[0-9]{7,8}$'), "
                    + "CHECK (email LIKE '%@%'))";

    private static final String ADMINISTRADORES =
            "CREATE TABLE IF NOT EXISTS administradores ("
                    + "dni VARCHAR(8) NOT NULL, "
                    + "nombre VARCHAR(100) NOT NULL, "
                    + "email VARCHAR(100) NOT NULL, "
                    + "clave VARCHAR(100) NOT NULL, "
                    + "activo BOOLEAN NOT NULL DEFAULT TRUE, "
                    + "PRIMARY KEY (dni), "
                    + "UNIQUE (email), "
                    + "CHECK (dni REGEXP '^[0-9]{7,8}$'), "
                    + "CHECK (email LIKE '%@%'))";

    /** Crea las tablas de usuarios si todavía no existen. */
    public void crearTablas() throws SQLException {
        try (Connection con = Conexion.obtener();
             Statement st = con.createStatement()) {
            st.executeUpdate(CLIENTES);
            st.executeUpdate(VENDEDORES);
            st.executeUpdate(ADMINISTRADORES);
        }
    }
}