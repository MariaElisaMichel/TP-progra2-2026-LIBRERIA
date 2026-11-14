package Interfaces;

import Exceptions.AccesoDenegadoException;
import Exceptions.DatosInvalidosException;
import Exceptions.EntidadNoEncontradaException;
import Exceptions.StockInsuficienteException;
import Exceptions.UsuarioDuplicadoException;
import Models.Usuario;

import java.sql.SQLException;
import java.util.List;

public interface IRepositorio<T> {

    T agregar(Usuario actor, T elemento)
            throws AccesoDenegadoException, DatosInvalidosException,
            UsuarioDuplicadoException, EntidadNoEncontradaException,
            StockInsuficienteException, SQLException;

    List<T> mostrar(Usuario actor) throws AccesoDenegadoException;

    T eliminar(Usuario actor, String id)
            throws AccesoDenegadoException, DatosInvalidosException,
            EntidadNoEncontradaException, SQLException;
}