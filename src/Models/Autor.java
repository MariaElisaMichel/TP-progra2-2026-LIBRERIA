package Models;

import Exceptions.DatosInvalidosException;
import Interfaces.IValidarNombre;

public final class Autor implements IValidarNombre {
    
    private static final int LARGO_MAXIMO = 60;
    
    private final String nombre;
    private final String apellido;
    
    public Autor(String nombre, String apellido) throws DatosInvalidosException {
        validarNombre(nombre);
        validarTexto(apellido, "El apellido");
        this.nombre = nombre.trim();
        this.apellido = apellido.trim();
    }
    
    @Override
    public void validarNombre(String nombre) throws DatosInvalidosException {
        validarTexto(nombre, "El nombre");
    }
    
    @Override
    public Autor cambiarNombre(String nuevoNombre) throws DatosInvalidosException {
        return new Autor(nuevoNombre, apellido);
    }
    
    @Override
    public String getNombre() {
        return nombre;
    }
    
    public String getApellido() {
        return apellido;
    }
    
    private static void validarTexto(String valor, String campo) throws DatosInvalidosException {
        if (valor == null || valor.trim().isEmpty()) {
            throw new DatosInvalidosException(campo + " del autor no puede estar vacío.");
        }
        if (valor.trim().length() > LARGO_MAXIMO) {
            throw new DatosInvalidosException(
                    campo + " del autor no puede superar los " + LARGO_MAXIMO + " caracteres.");
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Autor)) return false;
        Autor otro = (Autor) o;
        return nombre.equalsIgnoreCase(otro.nombre) && apellido.equalsIgnoreCase(otro.apellido);
    }
    
    @Override
    public int hashCode() {
        return nombre.toLowerCase().hashCode() * 31 + apellido.toLowerCase().hashCode();
    }
    
    @Override
    public String toString() {
        return nombre + " " + apellido;
    }
}