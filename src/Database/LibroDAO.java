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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class LibroDAO {
    
    private static final String CREAR_AUTORES =
            "CREATE TABLE IF NOT EXISTS autores ("
                    + "nombre VARCHAR(60) NOT NULL, "
                    + "apellido VARCHAR(60) NOT NULL, "
                    + "PRIMARY KEY (nombre, apellido))";
    
    private static final String CREAR_LIBROS =
            "CREATE TABLE IF NOT EXISTS libros ("
                    + "isbn VARCHAR(13) PRIMARY KEY, "
                    + "titulo VARCHAR(150) NOT NULL, "
                    + "genero VARCHAR(20) NOT NULL, "
                    + "precio DECIMAL(10,2) NOT NULL CHECK (precio > 0), "
                    + "stock INT NOT NULL CHECK (stock >= 0), "
                    + "activo BOOLEAN NOT NULL DEFAULT TRUE)";
    
    private static final String CREAR_LIBROS_AUTORES =
            "CREATE TABLE IF NOT EXISTS libros_autores ("
                    + "isbn VARCHAR(13) NOT NULL, "
                    + "autor_nombre VARCHAR(60) NOT NULL, "
                    + "autor_apellido VARCHAR(60) NOT NULL, "
                    + "PRIMARY KEY (isbn, autor_nombre, autor_apellido), "
                    + "FOREIGN KEY (isbn) REFERENCES libros(isbn), "
                    + "FOREIGN KEY (autor_nombre, autor_apellido) REFERENCES autores(nombre, apellido))";
    
    private static final String LISTAR_LIBROS =
            "SELECT isbn, titulo, genero, precio, stock, activo FROM libros ORDER BY titulo";
    
    private static final String LISTAR_AUTORES =
            "SELECT nombre, apellido FROM autores ORDER BY apellido, nombre";
    
    private static final String AUTORES_DE_UN_LIBRO =
            "SELECT a.nombre, a.apellido FROM libros_autores la "
                    + "JOIN autores a ON a.nombre = la.autor_nombre AND a.apellido = la.autor_apellido "
                    + "WHERE la.isbn = ?";
    
    private static final String INSERTAR_AUTOR =
            "INSERT INTO autores (nombre, apellido) VALUES (?, ?) "
                    + "ON DUPLICATE KEY UPDATE nombre = nombre";
    
    private static final String INSERTAR_LIBRO =
            "INSERT INTO libros (isbn, titulo, genero, precio, stock, activo) "
                    + "VALUES (?, ?, ?, ?, ?, ?)";
    
    private static final String INSERTAR_LIBRO_AUTOR =
            "INSERT INTO libros_autores (isbn, autor_nombre, autor_apellido) VALUES (?, ?, ?)";
    
    private static final String ACTUALIZAR_LIBRO =
            "UPDATE libros SET titulo = ?, genero = ?, precio = ?, stock = ?, activo = ? "
                    + "WHERE isbn = ?";
    
    private static final String BORRAR_AUTORES_DE_LIBRO =
            "DELETE FROM libros_autores WHERE isbn = ?";
    
    public void crearTablas() throws SQLException {
        try (Connection con = Conexion.obtener();
             Statement st = con.createStatement()) {
            st.executeUpdate(CREAR_AUTORES);
            st.executeUpdate(CREAR_LIBROS);
            st.executeUpdate(CREAR_LIBROS_AUTORES);
        }
    }
    
    public List<Libro> listar() throws SQLException {
        List<Libro> libros = new ArrayList<>();
        try (Connection con = Conexion.obtener();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(LISTAR_LIBROS)) {
            while (rs.next()) {
                String isbn = rs.getString("isbn");
                try {
                    Genero genero = Genero.valueOf(rs.getString("genero"));
                    libros.add(new Libro(isbn, rs.getString("titulo"), genero,
                                         rs.getDouble("precio"), rs.getInt("stock"),
                                         autoresDe(con, isbn), rs.getBoolean("activo")));
                } catch (DatosInvalidosException e) {
                    throw new SQLException("El libro " + isbn
                                                   + " guardado en la base no es válido: " + e.getMessage());
                } catch (IllegalArgumentException e) {
                    throw new SQLException("El libro " + isbn
                                                   + " tiene un género desconocido en la base de datos.");
                }
            }
        }
        return libros;
    }
    
    public List<Autor> listarAutores() throws SQLException {
        List<Autor> autores = new ArrayList<>();
        try (Connection con = Conexion.obtener();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(LISTAR_AUTORES)) {
            while (rs.next()) {
                autores.add(armarAutor(rs));
            }
        }
        return autores;
    }
    
    public Libro guardar(Libro libro) throws SQLException {
        try (Connection con = Conexion.obtener()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(INSERTAR_LIBRO)) {
                    ps.setString(1, libro.getIsbn());
                    ps.setString(2, libro.getTitulo());
                    ps.setString(3, libro.getGenero().name());
                    ps.setDouble(4, libro.getPrecio());
                    ps.setInt(5, libro.getStock());
                    ps.setBoolean(6, libro.estaActivo());
                    ps.executeUpdate();
                }
                vincularAutores(con, libro);
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
        return libro;
    }
    
    public Autor guardarAutor(Autor autor) throws SQLException {
        try (Connection con = Conexion.obtener()) {
            guardarAutor(con, autor);
        }
        return autor;
    }
    
    public Libro actualizar(Libro libro) throws SQLException {
        try (Connection con = Conexion.obtener()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(ACTUALIZAR_LIBRO)) {
                    ps.setString(1, libro.getTitulo());
                    ps.setString(2, libro.getGenero().name());
                    ps.setDouble(3, libro.getPrecio());
                    ps.setInt(4, libro.getStock());
                    ps.setBoolean(5, libro.estaActivo());
                    ps.setString(6, libro.getIsbn());
                    if (ps.executeUpdate() == 0) {
                        throw new SQLException("No existe el libro con ISBN " + libro.getIsbn());
                    }
                }
                try (PreparedStatement ps = con.prepareStatement(BORRAR_AUTORES_DE_LIBRO)) {
                    ps.setString(1, libro.getIsbn());
                    ps.executeUpdate();
                }
                vincularAutores(con, libro);
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
        return libro;
    }
    
    private void guardarAutor(Connection con, Autor autor) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(INSERTAR_AUTOR)) {
            ps.setString(1, autor.getNombre());
            ps.setString(2, autor.getApellido());
            ps.executeUpdate();
        }
    }
    
    private void vincularAutores(Connection con, Libro libro) throws SQLException {
        for (Autor autor : libro.getAutores()) {
            guardarAutor(con, autor);
            try (PreparedStatement ps = con.prepareStatement(INSERTAR_LIBRO_AUTOR)) {
                ps.setString(1, libro.getIsbn());
                ps.setString(2, autor.getNombre());
                ps.setString(3, autor.getApellido());
                ps.executeUpdate();
            }
        }
    }
    
    private Set<Autor> autoresDe(Connection con, String isbn) throws SQLException {
        Set<Autor> autores = new LinkedHashSet<>();
        try (PreparedStatement ps = con.prepareStatement(AUTORES_DE_UN_LIBRO)) {
            ps.setString(1, isbn);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    autores.add(armarAutor(rs));
                }
            }
        }
        return autores;
    }
    
    private Autor armarAutor(ResultSet rs) throws SQLException {
        try {
            return new Autor(rs.getString("nombre"), rs.getString("apellido"));
        } catch (DatosInvalidosException e) {
            throw new SQLException("Hay un autor inválido guardado en la base de datos: "
                                           + e.getMessage());
        }
    }
}