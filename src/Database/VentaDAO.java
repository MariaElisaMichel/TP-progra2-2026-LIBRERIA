package Database;

import Enums.EstadoVenta;
import Enums.MedioPago;
import Enums.ModalidadEntrega;
import Exceptions.DatosInvalidosException;
import Models.Cliente;
import Models.DetalleVenta;
import Models.Libro;
import Models.Usuario;
import Models.Venta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class VentaDAO {

    private static final String CREAR_VENTAS =
            "CREATE TABLE IF NOT EXISTS ventas ("
                    + "id VARCHAR(10) PRIMARY KEY, "
                    + "fecha DATETIME NOT NULL, "
                    + "cliente_dni VARCHAR(8) NOT NULL, "
                    + "medio VARCHAR(20) NOT NULL, "
                    + "entrega VARCHAR(20) NOT NULL, "
                    + "domicilio_entrega VARCHAR(150), "
                    + "estado VARCHAR(20) NOT NULL, "
                    + "fecha_anulacion DATETIME, "
                    + "anulado_por_dni VARCHAR(8), "
                    + "FOREIGN KEY (cliente_dni) REFERENCES clientes(dni))";

    private static final String CREAR_DETALLES =
            "CREATE TABLE IF NOT EXISTS detalles_venta ("
                    + "venta_id VARCHAR(10) NOT NULL, "
                    + "isbn VARCHAR(13) NOT NULL, "
                    + "cantidad INT NOT NULL CHECK (cantidad > 0), "
                    + "precio_unitario DECIMAL(10,2) NOT NULL CHECK (precio_unitario > 0), "
                    + "PRIMARY KEY (venta_id, isbn), "
                    + "FOREIGN KEY (venta_id) REFERENCES ventas(id), "
                    + "FOREIGN KEY (isbn) REFERENCES libros(isbn))";

    private static final String SIGUIENTE_ID =
            "SELECT COALESCE(MAX(CAST(SUBSTRING(id, 3) AS UNSIGNED)), 0) + 1 FROM ventas";

    private static final String LISTAR_VENTAS =
            "SELECT id, fecha, cliente_dni, medio, entrega, domicilio_entrega, "
                    + "estado, fecha_anulacion, anulado_por_dni FROM ventas ORDER BY id";

    private static final String DETALLES_DE_UNA_VENTA =
            "SELECT isbn, cantidad, precio_unitario FROM detalles_venta WHERE venta_id = ?";

    private static final String INSERTAR_VENTA =
            "INSERT INTO ventas (id, fecha, cliente_dni, medio, entrega, domicilio_entrega, "
                    + "estado, fecha_anulacion, anulado_por_dni) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String INSERTAR_DETALLE =
            "INSERT INTO detalles_venta (venta_id, isbn, cantidad, precio_unitario) "
                    + "VALUES (?, ?, ?, ?)";

    private static final String DESCONTAR_STOCK =
            "UPDATE libros SET stock = stock - ? WHERE isbn = ? AND stock >= ?";

    private static final String ANULAR_VENTA =
            "UPDATE ventas SET estado = ?, fecha_anulacion = ?, anulado_por_dni = ? "
                    + "WHERE id = ? AND estado = ?";

    private static final String REPONER_STOCK =
            "UPDATE libros l JOIN detalles_venta d ON d.isbn = l.isbn "
                    + "SET l.stock = l.stock + d.cantidad WHERE d.venta_id = ?";

    public void crearTablas() throws SQLException {
        try (Connection con = Conexion.obtener();
             Statement st = con.createStatement()) {
            st.executeUpdate(CREAR_VENTAS);
            st.executeUpdate(CREAR_DETALLES);
        }
    }

    public String siguienteId() throws SQLException {
        try (Connection con = Conexion.obtener();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(SIGUIENTE_ID)) {
            rs.next();
            return String.format("V-%04d", rs.getInt(1));
        }
    }

    public List<Venta> listar(List<Usuario> usuarios, List<Libro> libros) throws SQLException {
        List<Venta> ventas = new ArrayList<>();
        try (Connection con = Conexion.obtener();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(LISTAR_VENTAS)) {
            while (rs.next()) {
                String id = rs.getString("id");
                try {
                    Cliente cliente = buscarCliente(usuarios, rs.getString("cliente_dni"));
                    List<DetalleVenta> detalles = detallesDe(con, id, libros);
                    ventas.add(new Venta(id,
                            rs.getTimestamp("fecha").toLocalDateTime(),
                            cliente, detalles,
                            MedioPago.valueOf(rs.getString("medio")),
                            ModalidadEntrega.valueOf(rs.getString("entrega")),
                            rs.getString("domicilio_entrega"),
                            EstadoVenta.valueOf(rs.getString("estado")),
                            aFecha(rs.getTimestamp("fecha_anulacion")),
                            rs.getString("anulado_por_dni")));
                } catch (DatosInvalidosException e) {
                    throw new SQLException("La venta " + id
                            + " guardada en la base no es válida: " + e.getMessage());
                } catch (IllegalArgumentException e) {
                    throw new SQLException("La venta " + id
                            + " tiene un valor desconocido en la base de datos.");
                }
            }
        }
        return ventas;
    }

    public Venta registrarVenta(Venta venta) throws SQLException {
        try (Connection con = Conexion.obtener()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(INSERTAR_VENTA)) {
                    ps.setString(1, venta.getId());
                    ps.setTimestamp(2, Timestamp.valueOf(venta.getFecha()));
                    ps.setString(3, venta.getCliente().getDni());
                    ps.setString(4, venta.getMedio().name());
                    ps.setString(5, venta.getEntrega().name());
                    ps.setString(6, venta.getDomicilioEntrega());
                    ps.setString(7, venta.getEstado().name());
                    ps.setTimestamp(8, aTimestamp(venta.getFechaAnulacion()));
                    ps.setString(9, venta.getAnuladoPorDni());
                    ps.executeUpdate();
                }
                for (DetalleVenta detalle : venta.getDetalles()) {
                    String isbn = detalle.getLibro().getIsbn();
                    try (PreparedStatement ps = con.prepareStatement(INSERTAR_DETALLE)) {
                        ps.setString(1, venta.getId());
                        ps.setString(2, isbn);
                        ps.setInt(3, detalle.getCantidad());
                        ps.setDouble(4, detalle.getPrecioUnitario());
                        ps.executeUpdate();
                    }
                    try (PreparedStatement ps = con.prepareStatement(DESCONTAR_STOCK)) {
                        ps.setInt(1, detalle.getCantidad());
                        ps.setString(2, isbn);
                        ps.setInt(3, detalle.getCantidad());
                        if (ps.executeUpdate() == 0) {
                            throw new SQLException("No hay stock suficiente en la base "
                                    + "para el libro con ISBN " + isbn);
                        }
                    }
                }
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
        return venta;
    }

    public boolean anularVenta(String id, String actorDni, LocalDateTime fecha)
            throws SQLException {
        try (Connection con = Conexion.obtener()) {
            con.setAutoCommit(false);
            try {
                int filas;
                try (PreparedStatement ps = con.prepareStatement(ANULAR_VENTA)) {
                    ps.setString(1, EstadoVenta.ANULADA.name());
                    ps.setTimestamp(2, Timestamp.valueOf(fecha));
                    ps.setString(3, actorDni);
                    ps.setString(4, id);
                    ps.setString(5, EstadoVenta.CONFIRMADA.name());
                    filas = ps.executeUpdate();
                }
                if (filas == 0) {
                    con.rollback();
                    return false;
                }
                try (PreparedStatement ps = con.prepareStatement(REPONER_STOCK)) {
                    ps.setString(1, id);
                    ps.executeUpdate();
                }
                con.commit();
                return true;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    private List<DetalleVenta> detallesDe(Connection con, String ventaId, List<Libro> libros)
            throws SQLException, DatosInvalidosException {
        List<DetalleVenta> detalles = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(DETALLES_DE_UNA_VENTA)) {
            ps.setString(1, ventaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String isbn = rs.getString("isbn");
                    detalles.add(new DetalleVenta(buscarLibro(libros, isbn),
                            rs.getInt("cantidad"), rs.getDouble("precio_unitario")));
                }
            }
        }
        return detalles;
    }

    private Cliente buscarCliente(List<Usuario> usuarios, String dni) throws SQLException {
        for (Usuario u : usuarios) {
            if (u instanceof Cliente && u.getDni().equals(dni)) {
                return (Cliente) u;
            }
        }
        throw new SQLException("No se encontró el cliente " + dni + " de una venta guardada.");
    }

    private Libro buscarLibro(List<Libro> libros, String isbn) throws SQLException {
        for (Libro l : libros) {
            if (l.getIsbn().equals(isbn)) {
                return l;
            }
        }
        throw new SQLException("No se encontró el libro " + isbn + " de una venta guardada.");
    }

    private static Timestamp aTimestamp(LocalDateTime fecha) {
        return fecha == null ? null : Timestamp.valueOf(fecha);
    }

    private static LocalDateTime aFecha(Timestamp t) {
        return t == null ? null : t.toLocalDateTime();
    }
}