package Models;

import Enums.Permiso;
import Exceptions.DatosInvalidosException;

/**
 * Administrador: gestiona libros, ventas y usuarios. No compra.
 */
public class Administrador extends Usuario {
    
    public Administrador(String dni, String nombre, String email, String clave,
                         boolean activo) throws DatosInvalidosException {
        super(dni, nombre, email, clave, activo);
    }
    
    @Override
    public boolean permite(Permiso p) {
        return p == Permiso.VER_CATALOGO
                || p == Permiso.GESTIONAR_LIBROS
                || p == Permiso.GESTIONAR_VENTAS
                || p == Permiso.GESTIONAR_USUARIOS;
    }
}