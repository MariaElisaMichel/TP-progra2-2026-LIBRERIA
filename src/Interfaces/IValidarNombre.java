package Interfaces;

import Exceptions.DatosInvalidosException;

public interface IValidarNombre {
    
    void validarNombre(String nombre) throws DatosInvalidosException;
    
    IValidarNombre cambiarNombre(String nuevoNombre) throws DatosInvalidosException;
    
    String getNombre();
}
