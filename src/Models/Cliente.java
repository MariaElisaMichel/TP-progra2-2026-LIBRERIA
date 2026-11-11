package Models;

import Enums.Permiso;
import Exceptions.DatosInvalidosException;

/**
 * Cliente: ve el catálogo, compra y consulta sus propias ventas.
 */
public class Cliente extends Usuario {

    private String domicilio;

    public Cliente(String dni, String nombre, String email, String clave,
                   String domicilio) throws DatosInvalidosException {
        super(dni, nombre, email, clave);
        exigirTexto(domicilio, "domicilio");
        this.domicilio = domicilio.trim();
    }

    @Override
    public boolean permite(Permiso p) {
        return p == Permiso.VER_CATALOGO
                || p == Permiso.COMPRAR
                || p == Permiso.VER_MIS_COMPRAS;
    }

    /** Valida primero y recién después modifica el domicilio. */
    public Cliente cambiarDomicilio(String domicilio)
            throws DatosInvalidosException {
        exigirTexto(domicilio, "domicilio");
        this.domicilio = domicilio.trim();
        return this;
    }

    public String getDomicilio() {
        return domicilio;
    }

}