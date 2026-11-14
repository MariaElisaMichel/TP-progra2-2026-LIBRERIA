package Models;

import Enums.Permiso;
import Exceptions.DatosInvalidosException;
import Interfaces.IValidarNombre;

import java.util.Objects;

public abstract class Usuario implements IValidarNombre {
    
    private final String dni;
    private String nombre;
    private String email;
    private final String clave;
    private boolean activo;
    
    public Usuario(String dni, String nombre, String email, String clave, boolean activo)
            throws DatosInvalidosException {
        validarDni(dni);
        validarNombre(nombre);
        validarEmail(email);
        exigirTexto(clave, "La clave");
        this.dni = dni;
        this.nombre = nombre.trim();
        this.email = email.trim();
        this.clave = clave;
        this.activo = activo;
    }
    
    public abstract boolean permite(Permiso p);
    
    @Override
    public void validarNombre(String nombre) throws DatosInvalidosException {
        exigirTexto(nombre, "El nombre");
    }
    
    @Override
    public Usuario cambiarNombre(String nuevoNombre) throws DatosInvalidosException {
        validarNombre(nuevoNombre);
        this.nombre = nuevoNombre.trim();
        return this;
    }
    
    public Usuario cambiarDatos(String nombre, String email) throws DatosInvalidosException {
        validarNombre(nombre);
        validarEmail(email);
        this.nombre = nombre.trim();
        this.email = email.trim();
        return this;
    }
    
    public boolean desactivar() {
        boolean estabaActivo = activo;
        activo = false;
        return estabaActivo;
    }
    
    public boolean estaActivo() {
        return activo;
    }
    
    @Override
    public String getNombre() {
        return nombre;
    }
    
    public String getDni() {
        return dni;
    }
    
    public String getEmail() {
        return email;
    }
    
    public String getClave() {
        return clave;
    }
    
    private static void validarDni(String dni) throws DatosInvalidosException {
        if (dni == null || !dni.matches("\\d{7,8}")) {
            throw new DatosInvalidosException(
                    "El DNI debe tener 7 u 8 dígitos, sin puntos ni espacios.");
        }
    }
    
    private static void validarEmail(String email) throws DatosInvalidosException {
        if (email == null || !email.trim().matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            throw new DatosInvalidosException("El email no tiene un formato válido.");
        }
    }
    
    protected static String exigirTexto(String valor, String campo) throws DatosInvalidosException {
        if (valor == null || valor.trim().isEmpty()) {
            throw new DatosInvalidosException(campo + " no puede estar vacío.");
        }
        return valor.trim();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Usuario)) return false;
        Usuario otro = (Usuario) o;
        return dni.equals(otro.dni);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(dni);
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + " " + dni + " - " + nombre + " <" + email + ">"
                + (activo ? "" : " [inactivo]");
    }
}