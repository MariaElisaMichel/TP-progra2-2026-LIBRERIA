package Database;

import Models.Cliente;
import Models.Usuario;
import Models.Vendedor;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Acceso a la base de datos para los usuarios (clientes, vendedores
 * y administradores, cada uno en su propia tabla). */

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
    
    private static final String INSERTAR_CLIENTE =
            "INSERT INTO clientes (dni, nombre, email, clave, activo, domicilio) "
                    + "VALUES (?, ?, ?, ?, ?, ?)";
    
    private static final String INSERTAR_VENDEDOR =
            "INSERT INTO vendedores (dni, nombre, email, clave, activo) "
                    + "VALUES (?, ?, ?, ?, ?)";
    
    private static final String INSERTAR_ADMINISTRADOR =
            "INSERT INTO administradores (dni, nombre, email, clave, activo) "
                    + "VALUES (?, ?, ?, ?, ?)";
    
    /** Crea las tablas de usuarios si todavía no existen. */
    public void crearTablas() throws SQLException {
        try (Connection con = Conexion.obtener();
             Statement st = con.createStatement()) {
            st.executeUpdate(CLIENTES);
            st.executeUpdate(VENDEDORES);
            st.executeUpdate(ADMINISTRADORES);
        }
    }
    
    /** Guarda un usuario nuevo en la tabla que corresponde a su rol. */
    public Usuario guardar(Usuario usuario) throws SQLException {
        String sql;
        if (usuario instanceof Cliente) {
            sql = INSERTAR_CLIENTE;
        } else if (usuario instanceof Vendedor) {
            sql = INSERTAR_VENDEDOR;
        } else {
            sql = INSERTAR_ADMINISTRADOR; // el único rol que queda
        }
        
        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, usuario.getDni());
            ps.setString(2, usuario.getNombre());
            ps.setString(3, usuario.getEmail());
            ps.setString(4, usuario.getClave());
            ps.setBoolean(5, usuario.estaActivo());
            if (usuario instanceof Cliente) {
                ps.setString(6, ((Cliente) usuario).getDomicilio());
            }
            ps.executeUpdate();
        }
        return usuario;
    }
}