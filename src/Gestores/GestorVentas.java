package Gestores;

import Database.VentaDAO;
import Enums.EstadoVenta;
import Enums.MedioPago;
import Enums.ModalidadEntrega;
import Enums.Permiso;
import Exceptions.AccesoDenegadoException;
import Exceptions.CarritoVacioException;
import Exceptions.DatosInvalidosException;
import Exceptions.EntidadNoEncontradaException;
import Exceptions.StockInsuficienteException;
import Interfaces.IRepositorio;
import Models.Cliente;
import Models.DetalleVenta;
import Models.Libro;
import Models.Usuario;
import Models.Venta;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GestorVentas implements IRepositorio<Venta> {
    
    private final VentaDAO ventaDAO;
    private final GestorCatalogo catalogo;
    private final List<Venta> ventas;
    
    public GestorVentas(VentaDAO ventaDAO, GestorCatalogo catalogo, GestorUsuario usuarios)
            throws SQLException {
        this.ventaDAO = ventaDAO;
        this.catalogo = catalogo;
        this.ventas = ventaDAO.listar(usuarios.todos(), catalogo.todos());
    }
    
    @Override
    public Venta agregar(Usuario actor, Venta elemento)
            throws AccesoDenegadoException, DatosInvalidosException,
            EntidadNoEncontradaException, StockInsuficienteException, SQLException {
        verificarPermiso(actor, Permiso.COMPRAR);
        if (elemento == null) {
            throw new DatosInvalidosException("Falta la venta que se quiere registrar.");
        }
        if (!elemento.getCliente().equals(actor)) {
            throw new AccesoDenegadoException("Solo podés registrar compras a tu propio nombre.");
        }
        if (elemento.estaAnulada()) {
            throw new DatosInvalidosException("Una compra nueva no puede estar anulada.");
        }
        for (Venta v : ventas) {
            if (v.getId().equals(elemento.getId())) {
                throw new DatosInvalidosException(
                        "Ya existe una venta con el id " + elemento.getId() + ".");
            }
        }
        for (DetalleVenta detalle : elemento.getDetalles()) {
            Libro libro = catalogo.consultar(detalle.getLibro().getIsbn());
            if (!libro.estaActivo()) {
                throw new DatosInvalidosException(
                        "El libro " + libro.getTitulo() + " ya no está disponible.");
            }
            if (!libro.tieneStock(detalle.getCantidad())) {
                throw new StockInsuficienteException("No hay stock suficiente de "
                                                             + libro.getTitulo() + ": quedan " + libro.getStock()
                                                             + " y pediste " + detalle.getCantidad() + ".");
            }
        }
        ventaDAO.registrarVenta(elemento);
        for (DetalleVenta detalle : elemento.getDetalles()) {
            catalogo.consultar(detalle.getLibro().getIsbn()).descontar(detalle.getCantidad());
        }
        ventas.add(elemento);
        return elemento;
    }
    
    @Override
    public List<Venta> mostrar(Usuario actor) throws AccesoDenegadoException {
        verificarPermiso(actor, Permiso.GESTIONAR_VENTAS);
        return new ArrayList<>(ventas);
    }
    
    @Override
    public Venta eliminar(Usuario actor, String id)
            throws AccesoDenegadoException, DatosInvalidosException,
            EntidadNoEncontradaException, SQLException {
        verificarPermiso(actor, Permiso.GESTIONAR_VENTAS);
        Venta venta = consultar(id);
        if (venta.estaAnulada()) {
            throw new DatosInvalidosException("La venta " + id + " ya estaba anulada.");
        }
        LocalDateTime ahora = LocalDateTime.now();
        if (!ventaDAO.anularVenta(id, actor.getDni(), ahora)) {
            throw new DatosInvalidosException(
                    "La venta " + id + " no se pudo anular porque ya estaba anulada.");
        }
        for (DetalleVenta detalle : venta.getDetalles()) {
            catalogo.consultar(detalle.getLibro().getIsbn()).reponer(detalle.getCantidad());
        }
        Venta anulada = new Venta(venta.getId(), venta.getFecha(), venta.getCliente(),
                                  venta.getDetalles(), venta.getMedio(), venta.getEntrega(),
                                  venta.getDomicilioEntrega(), EstadoVenta.ANULADA, ahora, actor.getDni());
        ventas.set(ventas.indexOf(venta), anulada);
        return anulada;
    }
    
    public Venta comprar(Usuario actor, Map<String, Integer> items, MedioPago medio,
                         ModalidadEntrega entrega, String domicilio)
            throws AccesoDenegadoException, CarritoVacioException, DatosInvalidosException,
            EntidadNoEncontradaException, StockInsuficienteException, SQLException {
        verificarPermiso(actor, Permiso.COMPRAR);
        if (!(actor instanceof Cliente)) {
            throw new AccesoDenegadoException("Solo un cliente puede realizar compras.");
        }
        if (items == null || items.isEmpty()) {
            throw new CarritoVacioException(
                    "No hay libros en el carrito: agregá al menos uno para comprar.");
        }
        List<DetalleVenta> detalles = new ArrayList<>();
        for (Map.Entry<String, Integer> item : items.entrySet()) {
            Libro libro = catalogo.consultar(item.getKey());
            detalles.add(new DetalleVenta(libro, item.getValue(), libro.getPrecio()));
        }
        Venta venta = new Venta(ventaDAO.siguienteId(), LocalDateTime.now(), (Cliente) actor,
                                detalles, medio, entrega, domicilio, EstadoVenta.CONFIRMADA, null, null);
        return agregar(actor, venta);
    }
    
    public List<Venta> listarPropias(Usuario actor) throws AccesoDenegadoException {
        verificarPermiso(actor, Permiso.VER_MIS_COMPRAS);
        List<Venta> propias = new ArrayList<>();
        for (Venta v : ventas) {
            if (v.getCliente().equals(actor)) {
                propias.add(v);
            }
        }
        return propias;
    }
    
    private Venta consultar(String id) throws EntidadNoEncontradaException {
        for (Venta v : ventas) {
            if (v.getId().equals(id)) {
                return v;
            }
        }
        throw new EntidadNoEncontradaException("No existe una venta con id " + id + ".");
    }
    
    private void verificarPermiso(Usuario actor, Permiso permiso)
            throws AccesoDenegadoException {
        if (actor == null) {
            throw new AccesoDenegadoException("Tenés que iniciar sesión para hacer esto.");
        }
        if (!actor.estaActivo()) {
            throw new AccesoDenegadoException("Tu usuario está dado de baja.");
        }
        if (!actor.permite(permiso)) {
            throw new AccesoDenegadoException("No tenés permiso para realizar esta operación.");
        }
    }
}