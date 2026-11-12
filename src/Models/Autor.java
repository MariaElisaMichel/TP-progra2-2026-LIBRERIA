package Models;

import Exceptions.DatosInvalidosException;

/**
 * Autor de uno o más libros. Se identifica por su DNI.
 */
public class Autor extends Persona {

    public Autor(String dni, String nombre) throws DatosInvalidosException {
        super(dni, nombre);
    }

    /** Dos autores son iguales si tienen el mismo DNI. */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Autor)) {
            return false;
        }
        Autor otro = (Autor) o;
        return getDni().equals(otro.getDni());
    }

    @Override
    public int hashCode() {
        return getDni().hashCode();
    }

    @Override
    public String toString() {
        return "Autor [dni=" + getDni() + ", nombre=" + getNombre() + "]";
    }
}