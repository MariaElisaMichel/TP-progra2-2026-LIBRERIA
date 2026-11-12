package Models;

import Enums.Genero;
import Exceptions.DatosInvalidosException;
import Exceptions.StockInsuficienteException;
import java.util.ArrayList;
import java.util.List;

/**
 * Libro del catálogo. Representa una edición identificada por ISBN,
 * no un ejemplar físico: el stock cuenta los ejemplares disponibles.
 */
public class Libro {

    private final String isbn;
    private String titulo;
    private Genero genero;
    private double precio;
    private int stock;
    private boolean activo;
    private final List<Autor> autores;

    public Libro(String isbn, String titulo, Genero genero, double precio,
                 int stock, List<Autor> autores) throws DatosInvalidosException {
        validarIsbn(isbn);
        if (titulo == null || titulo.isBlank()) {
            throw new DatosInvalidosException("El título no puede estar vacío.");
        }
        if (genero == null) {
            throw new DatosInvalidosException("El género es obligatorio.");
        }
        validarPrecio(precio);
        if (stock < 0) {
            throw new DatosInvalidosException("El stock no puede ser negativo.");
        }
        if (autores == null || autores.isEmpty()) {
            throw new DatosInvalidosException(
                    "El libro debe tener al menos un autor.");
        }
        this.isbn = isbn;
        this.titulo = titulo.trim();
        this.genero = genero;
        this.precio = precio;
        this.stock = stock;
        this.autores = new ArrayList<>(autores);
        this.activo = true;
    }

    /** Valida primero y recién después modifica el precio. */
    public double cambiarPrecio(double precio) throws DatosInvalidosException {
        validarPrecio(precio);
        this.precio = precio;
        return this.precio;
    }

    /** Suma ejemplares al stock. Devuelve el stock resultante. */
    public int reponer(int cantidad) throws DatosInvalidosException {
        validarCantidad(cantidad);
        stock += cantidad;
        return stock;
    }

    /** Resta ejemplares del stock. Devuelve el stock resultante. */
    public int descontar(int cantidad)
            throws DatosInvalidosException, StockInsuficienteException {
        validarCantidad(cantidad);
        if (!tieneStock(cantidad)) {
            throw new StockInsuficienteException("Stock insuficiente: hay "
                    + stock + " y se pidieron " + cantidad + ".");
        }
        stock -= cantidad;
        return stock;
    }

    /** Baja lógica. Devuelve el estado activo resultante (false). */
    public boolean desactivar() {
        activo = false;
        return activo;
    }

    public boolean tieneStock(int cantidad) {
        return cantidad > 0 && stock >= cantidad;
    }

    public String getIsbn() {
        return isbn;
    }

    public String getTitulo() {
        return titulo;
    }

    public Genero getGenero() {
        return genero;
    }

    public double getPrecio() {
        return precio;
    }

    public int getStock() {
        return stock;
    }

    public boolean estaActivo() {
        return activo;
    }

    public List<Autor> getAutores() {
        return new ArrayList<>(autores);
    }

    /** El ISBN debe tener 10 o 13 caracteres y todos deben ser números. */
    private static void validarIsbn(String isbn) throws DatosInvalidosException {
        if (isbn == null || (isbn.length() != 10 && isbn.length() != 13)) {
            throw new DatosInvalidosException(
                    "El ISBN debe tener 10 o 13 números, sin guiones ni espacios.");
        }
        for (int i = 0; i < isbn.length(); i++) {
            char c = isbn.charAt(i);
            if (c < '0' || c > '9') {
                throw new DatosInvalidosException(
                        "El ISBN debe tener 10 o 13 números, sin guiones ni espacios.");
            }
        }
    }

    private static void validarPrecio(double precio)
            throws DatosInvalidosException {
        if (precio <= 0) {
            throw new DatosInvalidosException(
                    "El precio debe ser mayor que cero.");
        }
    }

    private static void validarCantidad(int cantidad)
            throws DatosInvalidosException {
        if (cantidad <= 0) {
            throw new DatosInvalidosException(
                    "La cantidad debe ser mayor que cero.");
        }
    }

    /** Dos libros son iguales si tienen el mismo ISBN. */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Libro)) {
            return false;
        }
        Libro otro = (Libro) o;
        return isbn.equals(otro.isbn);
    }

    @Override
    public int hashCode() {
        return isbn.hashCode();
    }

    @Override
    public String toString() {
        return "Libro [isbn=" + isbn + ", titulo=" + titulo
                + ", genero=" + genero + ", precio=" + precio
                + ", stock=" + stock + ", activo=" + activo + "]";
    }
}