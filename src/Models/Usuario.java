package Models;

import Enums.Permiso;
import Exceptions.DatosInvalidosException;


/**
 * Usuario del sistema. Es abstracta: cada rol decide sus permisos.
 */
public abstract class Usuario extends Persona {

    private String email;
    private final String clave;
    private boolean activo;

    public Usuario(String dni, String nombre, String email, String clave)
            throws DatosInvalidosException {
        super(dni, nombre);
        validarEmail(email);
        exigirTexto(clave, "clave");
        this.email = email.trim();
        this.clave = clave;
        this.activo = true;
    }

    /** Cada rol define qué permisos tiene. */
    public abstract boolean permite(Permiso p);

    /** Baja lógica. Devuelve el estado activo resultante (false). */
    public boolean desactivar() {
        activo = false;
        return activo;
    }

    /** Valida primero y recién después modifica el estado. */
    public Usuario cambiarDatos(String nombre, String email)
            throws DatosInvalidosException {
        validarEmail(email);
        cambiarNombre(nombre);
        this.email = email.trim();
        return this;
    }

    public boolean estaActivo() {
        return activo;
    }

    public String getEmail() {
        return email;
    }
    
    /*UsuarioDAO necesita obtener la clave para la base de datos*/
    public String getClave() {
        return clave;
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
        return getDni().equals(otro.getDni());
    }

    @Override
    public int hashCode() {
        return getDni().hashCode();
    }

    /** Resumen del usuario; no incluye la clave. */
    @Override
    public String toString() {
        return getClass().getSimpleName() + " [dni=" + getDni()
                + ", nombre=" + getNombre() + ", email=" + email
                + ", activo=" + activo + "]";
    }

    private static void validarEmail(String email)
            throws DatosInvalidosException {
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