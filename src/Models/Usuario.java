package Models;

import Exceptions.DatosInvalidosException;
import Interfaces.Autorizacion;
import Enums.Permiso;

/**
 * Usuario del sistema. Es abstracta: cada rol decide sus permisos.
 */
public abstract class Usuario implements Autorizacion {

    private final String dni;
    private String nombre;
    private String email;
    private final String clave;
    private boolean activo;

    public Usuario(String dni, String nombre, String email, String clave)
            throws DatosInvalidosException {
        validarDni(dni);
        validarNombreYEmail(nombre, email);
        exigirTexto(clave, "clave");
        this.dni = dni;
        this.nombre = nombre.trim();
        this.email = email.trim();
        this.clave = clave;
        this.activo = true;
    }

    /** Cada rol define qué permisos tiene. */
    @Override
    public abstract boolean permite(Permiso p);

    /** Baja lógica. Devuelve el estado activo resultante (false). */
    public boolean desactivar() {
        activo = false;
        return activo;
    }

    /** Valida primero y recién después modifica el estado. */
    public Usuario cambiarDatos(String nombre, String email)
            throws DatosInvalidosException {
        validarNombreYEmail(nombre, email);
        this.nombre = nombre.trim();
        this.email = email.trim();
        return this;
    }

    public boolean estaActivo() {
        return activo;
    }

    public String getDni() {
        return dni;
    }

    public String getNombre() {
        return nombre;
    }

    public String getEmail() {
        return email;
    }

    /** Dos usuarios son iguales si tienen el mismo DNI. */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Usuario)) {
            return false;
        }
        Usuario otro = (Usuario) o;
        return dni.equals(otro.dni);
    }

    @Override
    public int hashCode() {
        return dni.hashCode();
    }

    /** Resumen del usuario; no incluye la clave. */
    @Override
    public String toString() {
        return getClass().getSimpleName() + " [dni=" + dni
                + ", nombre=" + nombre + ", email=" + email
                + ", activo=" + activo + "]";
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

    private static void validarNombreYEmail(String nombre, String email)
            throws DatosInvalidosException {
        exigirTexto(nombre, "nombre");
        exigirTexto(email, "email");
        if (!email.contains("@")) {
            throw new DatosInvalidosException("El email debe contener '@'.");
        }
    }

    /** Valida que un texto no sea nulo ni esté vacío. */
    protected static void exigirTexto(String valor, String campo)
            throws DatosInvalidosException {
        if (valor == null || valor.isBlank()) {
            throw new DatosInvalidosException(
                    "El " + campo + " no puede estar vacío.");
        }
    }
}