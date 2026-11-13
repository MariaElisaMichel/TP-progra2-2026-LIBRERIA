package Database;

import Exceptions.DatosInvalidosException;
import Models.Administrador;
import Models.Cliente;
import Models.Usuario;
import Models.Vendedor;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

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
    
    private static final String INSERTAR_CLIENTE =
            "INSERT INTO clientes (dni, nombre, email, clave, activo, domicilio) "
                    + "VALUES (?, ?, ?, ?, ?, ?)";
    
    private static final String INSERTAR_VENDEDOR =
            "INSERT INTO vendedores (dni, nombre, email, clave, activo) "
                    + "VALUES (?, ?, ?, ?, ?)";
    
    private static final String INSERTAR_ADMINISTRADOR =
            "INSERT INTO administradores (dni, nombre, email, clave, activo) "
                    + "VALUES (?, ?, ?, ?, ?)";
    
    private static final String LISTAR_CLIENTES =
            "SELECT dni, nombre, email, clave, activo, domicilio FROM clientes";
    
    private static final String LISTAR_VENDEDORES =
            "SELECT dni, nombre, email, clave, activo FROM vendedores";
    
    private static final String LISTAR_ADMINISTRADORES =
            "SELECT dni, nombre, email, clave, activo FROM administradores";
    
    private static final String ACTUALIZAR_CLIENTE =
            "UPDATE clientes SET nombre = ?, email = ?, activo = ?, domicilio = ? "
                    + "WHERE dni = ?";
    
    private static final String ACTUALIZAR_VENDEDOR =
            "UPDATE vendedores SET nombre = ?, email = ?, activo = ? "
                    + "WHERE dni = ?";
    
    private static final String ACTUALIZAR_ADMINISTRADOR =
            "UPDATE administradores SET nombre = ?, email = ?, activo = ? "
                    + "WHERE dni = ?";
    
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
    
    /** Lee todos los usuarios de las tres tablas. */
    public List<Usuario> listar() throws SQLException {
        List<Usuario> usuarios = new ArrayList<>();
        try (Connection con = Conexion.obtener();
             Statement st = con.createStatement()) {
            
            try (ResultSet rs = st.executeQuery(LISTAR_CLIENTES)) {
                while (rs.next()) {
                    usuarios.add(new Cliente(rs.getString("dni"),
                                             rs.getString("nombre"), rs.getString("email"),
                                             rs.getString("clave"), rs.getString("domicilio"),
                                             rs.getBoolean("activo")));
                }
            }
            try (ResultSet rs = st.executeQuery(LISTAR_VENDEDORES)) {
                while (rs.next()) {
                    usuarios.add(new Vendedor(rs.getString("dni"),
                                              rs.getString("nombre"), rs.getString("email"),
                                              rs.getString("clave"), rs.getBoolean("activo")));
                }
            }
            try (ResultSet rs = st.executeQuery(LISTAR_ADMINISTRADORES)) {
                while (rs.next()) {
                    usuarios.add(new Administrador(rs.getString("dni"),
                                                   rs.getString("nombre"), rs.getString("email"),
                                                   rs.getString("clave"), rs.getBoolean("activo")));
                }
            }
        } catch (DatosInvalidosException e) {
            throw new SQLException(
                    "Hay datos inválidos guardados en la base: " + e.getMessage(), e);
        }
        return usuarios;
    }
    
    /** Guarda los cambios de un usuario que ya existe (se busca por DNI). */
    public Usuario actualizar(Usuario usuario) throws SQLException {
        String sql;
        if (usuario instanceof Cliente) {
            sql = ACTUALIZAR_CLIENTE;
        } else if (usuario instanceof Vendedor) {
            sql = ACTUALIZAR_VENDEDOR;
        } else {
            sql = ACTUALIZAR_ADMINISTRADOR; // el único rol que queda
        }
        
        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, usuario.getNombre());
            ps.setString(2, usuario.getEmail());
            ps.setBoolean(3, usuario.estaActivo());
            if (usuario instanceof Cliente) {
                ps.setString(4, ((Cliente) usuario).getDomicilio());
                ps.setString(5, usuario.getDni());
            } else {
                ps.setString(4, usuario.getDni());
            }
            int filas = ps.executeUpdate();
            if (filas == 0) {
                throw new SQLException("No existe en la base un usuario con DNI "
                                               + usuario.getDni() + ".");
            }
        }
        return usuario;
    }
}