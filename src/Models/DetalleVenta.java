package Models;

import Exceptions.DatosInvalidosException;

/**
 * Línea de una venta: un libro, la cantidad y el precio unitario al
 * momento de la compra. No cambia si después cambia el precio del libro.
 */
public class DetalleVenta {

    private final Libro libro;
    private final int cantidad;
    private final double precioUnitario;

    public DetalleVenta(Libro libro, int cantidad, double precioUnitario)
            throws DatosInvalidosException {
        if (libro == null) {
            throw new DatosInvalidosException("El detalle debe tener un libro.");
        }
        if (cantidad <= 0) {
            throw new DatosInvalidosException(
                    "La cantidad debe ser mayor que cero.");
        }
        if (precioUnitario <= 0) {
            throw new DatosInvalidosException(
                    "El precio unitario debe ser mayor que cero.");
        }
        this.libro = libro;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
    }

    public double calcularSubtotal() {
        return precioUnitario * cantidad;
    }

    public Libro getLibro() {
        return libro;
    }

    public int getCantidad() {
        return cantidad;
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    @Override
    public String toString() {
        return "DetalleVenta [isbn=" + libro.getIsbn() + ", cantidad="
                + cantidad + ", precioUnitario=" + precioUnitario + "]";
    }
}