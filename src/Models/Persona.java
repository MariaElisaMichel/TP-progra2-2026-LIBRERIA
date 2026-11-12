package Models;

import Exceptions.DatosInvalidosException;

/**
 * Persona identificada por DNI. Es la base de Usuario y de Autor.
 */
public abstract class Persona {

    private final String dni;
    private String nombre;

    public Persona(String dni, String nombre) throws DatosInvalidosException {
        validarDni(dni);
        validarNombre(nombre);
        this.dni = dni;
        this.nombre = nombre.trim();
    }

    /** Valida primero y recién después modifica el nombre. */
    public Persona cambiarNombre(String nombre) throws DatosInvalidosException {
        validarNombre(nombre);
        this.nombre = nombre.trim();
        return this;
    }

    public String getDni() {
        return dni;
    }

    public String getNombre() {
        return nombre;
    }

    /** El DNI debe tener 7 u 8 caracteres y todos deben ser números. */
    private static void validarDni(String dni) throws DatosInvalidosException {
        if (dni == null || dni.length() < 7 || dni.length() > 8) {
            throw new DatosInvalidosException(
                    "El DNI debe tener 7 u 8 números, sin puntos ni espacios.");
        }
        for (int i = 0; i < dni.length(); i++) {
            char c = dni.charAt(i);
            if (c < '0' || c > '9') {
                throw new DatosInvalidosException(
                        "El DNI debe tener 7 u 8 números, sin puntos ni espacios.");
            }
        }
    }

    private static void validarNombre(String nombre)
            throws DatosInvalidosException {
        if (nombre == null || nombre.isBlank()) {
            throw new DatosInvalidosException("El nombre no puede estar vacío.");
        }
    }
}