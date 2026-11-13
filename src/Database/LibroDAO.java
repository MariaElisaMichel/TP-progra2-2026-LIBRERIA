package Database;

import Enums.Genero;
import Exceptions.DatosInvalidosException;
import Models.Autor;
import Models.Libro;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Acceso a la base de datos para los libros y sus autores
 * (tablas autores, libros y libros_autores).
 */
public class LibroDAO {
    
    private static final String AUTORES =
            "CREATE TABLE IF NOT EXISTS autores ("
                    + "dni VARCHAR(8) NOT NULL, "
                    + "nombre VARCHAR(100) NOT NULL, "
                    + "PRIMARY KEY (dni), "
                    + "CHECK (dni REGEXP '^[0-9]{7,8}$'))";
    
    private static final String LIBROS =
            "CREATE TABLE IF NOT EXISTS libros ("
                    + "isbn VARCHAR(13) NOT NULL, "
                    + "titulo VARCHAR(150) NOT NULL, "
                    + "genero VARCHAR(20) NOT NULL, "
                    + "precio DECIMAL(10,2) NOT NULL, "
                    + "stock INT NOT NULL, "
                    + "activo BOOLEAN NOT NULL DEFAULT TRUE, "
                    + "PRIMARY KEY (isbn), "
                    + "CHECK (isbn REGEXP '^[0-9]{10}([0-9]{3})?$'), "
                    + "CHECK (genero IN ('NOVELA', 'CUENTO', 'POESIA', "
                    + "'ENSAYO', 'INFANTIL', 'OTRO')), "
                    + "CHECK (precio > 0), "
                    + "CHECK (stock >= 0))";
    
    private static final String LIBROS_AUTORES =
            "CREATE TABLE IF NOT EXISTS libros_autores ("
                    + "isbn VARCHAR(13) NOT NULL, "
                    + "autor_dni VARCHAR(8) NOT NULL, "
                    + "PRIMARY KEY (isbn, autor_dni), "
                    + "FOREIGN KEY (isbn) REFERENCES libros (isbn), "
                    + "FOREIGN KEY (autor_dni) REFERENCES autores (dni))";
    
    private static final String LISTAR_AUTORES =
            "SELECT dni, nombre FROM autores";
    
    private static final String LISTAR_LIBROS =
            "SELECT isbn, titulo, genero, precio, stock, activo FROM libros";
    
    private static final String LISTAR_AUTORES_DE_LIBROS =
            "SELECT la.isbn, a.dni, a.nombre FROM libros_autores la "
                    + "JOIN autores a ON la.autor_dni = a.dni";
    
    private static final String INSERTAR_AUTOR =
            "INSERT INTO autores (dni, nombre) VALUES (?, ?)";
    
    private static final String INSERTAR_LIBRO =
            "INSERT INTO libros (isbn, titulo, genero, precio, stock, activo) "
                    + "VALUES (?, ?, ?, ?, ?, ?)";
    
    private static final String INSERTAR_LIBRO_AUTOR =
            "INSERT INTO libros_autores (isbn, autor_dni) VALUES (?, ?)";
    
    private static final String ACTUALIZAR_LIBRO =
            "UPDATE libros SET titulo = ?, genero = ?, precio = ?, stock = ?, "
                    + "activo = ? WHERE isbn = ?";
    
    private static final String BORRAR_AUTORES_DE_LIBRO =
            "DELETE FROM libros_autores WHERE isbn = ?";
    
    /** Crea las tablas de autores y libros si todavía no existen. */
    public void crearTablas() throws SQLException {
        try (Connection con = Conexion.obtener();
             Statement st = con.createStatement()) {
            st.executeUpdate(AUTORES);
            st.executeUpdate(LIBROS);
            st.executeUpdate(LIBROS_AUTORES);
        }
    }
    
    /** Lee todos los libros, cada uno con sus autores. */
    public List<Libro> listar() throws SQLException {
        Map<String, List<Autor>> autoresPorLibro = new HashMap<>();
        List<Libro> libros = new ArrayList<>();
        try (Connection con = Conexion.obtener();
             Statement st = con.createStatement()) {
            
            try (ResultSet rs = st.executeQuery(LISTAR_AUTORES_DE_LIBROS)) {
                while (rs.next()) {
                    String isbn = rs.getString("isbn");
                    Autor autor = new Autor(rs.getString("dni"),
                                            rs.getString("nombre"));
                    List<Autor> lista = autoresPorLibro.get(isbn);
                    if (lista == null) {
                        lista = new ArrayList<>();
                        autoresPorLibro.put(isbn, lista);
                    }
                    lista.add(autor);
                }
            }
            try (ResultSet rs = st.executeQuery(LISTAR_LIBROS)) {
                while (rs.next()) {
                    String isbn = rs.getString("isbn");
                    libros.add(new Libro(isbn, rs.getString("titulo"),
                                         Genero.valueOf(rs.getString("genero")),
                                         rs.getDouble("precio"), rs.getInt("stock"),
                                         autoresPorLibro.get(isbn),
                                         rs.getBoolean("activo")));
                }
            }
        } catch (DatosInvalidosException e) {
            throw new SQLException(
                    "Hay datos inválidos guardados en la base: " + e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            throw new SQLException(
                    "Hay un género inválido guardado en la base.", e);
        }
        return libros;
    }
    
    /** Lee todos los autores. */
    public List<Autor> listarAutores() throws SQLException {
        List<Autor> autores = new ArrayList<>();
        try (Connection con = Conexion.obtener();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(LISTAR_AUTORES)) {
            while (rs.next()) {
                autores.add(new Autor(rs.getString("dni"),
                                      rs.getString("nombre")));
            }
        } catch (DatosInvalidosException e) {
            throw new SQLException(
                    "Hay datos inválidos guardados en la base: " + e.getMessage(), e);
        }
        return autores;
    }
    
    /** Guarda un autor nuevo. */
    public Autor guardarAutor(Autor autor) throws SQLException {
        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(INSERTAR_AUTOR)) {
            ps.setString(1, autor.getDni());
            ps.setString(2, autor.getNombre());
            ps.executeUpdate();
        }
        return autor;
    }
    
    /**
     * Guarda un libro nuevo junto con sus autores (que ya deben existir).
     * Se hace en una transacción: o se guarda todo, o no se guarda nada.
     */
    public Libro guardar(Libro libro) throws SQLException {
        try (Connection con = Conexion.obtener()) {
            con.setAutoCommit(false);
            try (PreparedStatement psLibro = con.prepareStatement(INSERTAR_LIBRO);
                 PreparedStatement psAutor =
                         con.prepareStatement(INSERTAR_LIBRO_AUTOR)) {
                psLibro.setString(1, libro.getIsbn());
                psLibro.setString(2, libro.getTitulo());
                psLibro.setString(3, libro.getGenero().name());
                psLibro.setDouble(4, libro.getPrecio());
                psLibro.setInt(5, libro.getStock());
                psLibro.setBoolean(6, libro.estaActivo());
                psLibro.executeUpdate();
                
                for (Autor autor : libro.getAutores()) {
                    psAutor.setString(1, libro.getIsbn());
                    psAutor.setString(2, autor.getDni());
                    psAutor.executeUpdate();
                }
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
        return libro;
    }
    
    /**
     * Guarda los cambios de un libro que ya existe (se busca por ISBN):
     * datos, stock, estado y autores. También en una transacción.
     */
    public Libro actualizar(Libro libro) throws SQLException {
        try (Connection con = Conexion.obtener()) {
            con.setAutoCommit(false);
            try (PreparedStatement psLibro = con.prepareStatement(ACTUALIZAR_LIBRO);
                 PreparedStatement psBorrar =
                         con.prepareStatement(BORRAR_AUTORES_DE_LIBRO);
                 PreparedStatement psAutor =
                         con.prepareStatement(INSERTAR_LIBRO_AUTOR)) {
                psLibro.setString(1, libro.getTitulo());
                psLibro.setString(2, libro.getGenero().name());
                psLibro.setDouble(3, libro.getPrecio());
                psLibro.setInt(4, libro.getStock());
                psLibro.setBoolean(5, libro.estaActivo());
                psLibro.setString(6, libro.getIsbn());
                if (psLibro.executeUpdate() == 0) {
                    throw new SQLException("No existe en la base un libro con ISBN "
                                                   + libro.getIsbn() + ".");
                }
                
                psBorrar.setString(1, libro.getIsbn());
                psBorrar.executeUpdate();
                for (Autor autor : libro.getAutores()) {
                    psAutor.setString(1, libro.getIsbn());
                    psAutor.setString(2, autor.getDni());
                    psAutor.executeUpdate();
                }
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
        return libro;
    }
}