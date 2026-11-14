
import Database.LibroDAO;
import Database.UsuarioDAO;
import Database.VentaDAO;
import Exceptions.DatosInvalidosException;
import Gestores.GestorCatalogo;
import Gestores.GestorUsuario;
import Gestores.GestorVentas;
import Models.Administrador;
import Models.Usuario;

import java.sql.SQLException;

void main() {
    try {
        UsuarioDAO usuarioDAO = new UsuarioDAO();
        LibroDAO libroDAO = new LibroDAO();
        VentaDAO ventaDAO = new VentaDAO();
        usuarioDAO.crearTablas();
        libroDAO.crearTablas();
        ventaDAO.crearTablas();

        boolean hayAdministrador = false;
        for (Usuario u : usuarioDAO.listar()) {
            if (u instanceof Administrador) {
                hayAdministrador = true;
                break;
            }
        }
        if (!hayAdministrador) {
            usuarioDAO.guardar(new Administrador("10000000", "Administrador",
                    "admin@libreria.com", "admin1234", true));
            System.out.println("Se creó el administrador inicial: admin@libreria.com / admin1234");
        }

        GestorUsuario usuarios = new GestorUsuario(usuarioDAO);
        GestorCatalogo catalogo = new GestorCatalogo(libroDAO);
        GestorVentas ventas = new GestorVentas(ventaDAO, catalogo, usuarios);
        new MenuConsola(usuarios, catalogo, ventas).ejecutar();
    } catch (SQLException e) {
        System.out.println("No se pudo iniciar la librería: no hay conexión con la base de datos.");
    } catch (DatosInvalidosException e) {
        System.out.println("No se pudo crear el administrador inicial: " + e.getMessage());
    }
}