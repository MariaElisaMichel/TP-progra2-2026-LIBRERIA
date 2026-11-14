package Gestores;

import Database.UsuarioDAO;
import Enums.Permiso;
import Exceptions.AccesoDenegadoException;
import Exceptions.DatosInvalidosException;
import Exceptions.EntidadNoEncontradaException;
import Exceptions.UsuarioDuplicadoException;
import Interfaces.IRepositorio;
import Models.Cliente;
import Models.Usuario;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GestorUsuario implements IRepositorio<Usuario> {

    private final UsuarioDAO usuarioDAO;
    private List<Usuario> usuarios;

    public GestorUsuario(UsuarioDAO usuarioDAO) throws SQLException {
        this.usuarioDAO = usuarioDAO;
        this.usuarios = usuarioDAO.listar();
    }

    @Override
    public Usuario agregar(Usuario actor, Usuario elemento)
            throws AccesoDenegadoException, DatosInvalidosException,
            UsuarioDuplicadoException, SQLException {
        verificarPermiso(actor, Permiso.GESTIONAR_USUARIOS);
        guardarNuevo(elemento);
        return elemento;
    }

    @Override
    public List<Usuario> mostrar(Usuario actor) throws AccesoDenegadoException {
        verificarPermiso(actor, Permiso.GESTIONAR_USUARIOS);
        return new ArrayList<>(usuarios);
    }

    @Override
    public Usuario eliminar(Usuario actor, String dni)
            throws AccesoDenegadoException, DatosInvalidosException,
            EntidadNoEncontradaException, SQLException {
        verificarPermiso(actor, Permiso.GESTIONAR_USUARIOS);
        Usuario usuario = consultar(dni);
        if (usuario.equals(actor)) {
            throw new AccesoDenegadoException("No podés dar de baja tu propio usuario.");
        }
        if (!usuario.estaActivo()) {
            throw new DatosInvalidosException(
                    "El usuario con DNI " + dni + " ya estaba dado de baja.");
        }
        usuario.desactivar();
        guardarCambio(usuario);
        return usuario;
    }

    public Usuario autenticar(String email, String clave) throws AccesoDenegadoException {
        for (Usuario u : usuarios) {
            if (u.getEmail().equalsIgnoreCase(email == null ? "" : email.trim())
                    && u.getClave().equals(clave)) {
                if (!u.estaActivo()) {
                    throw new AccesoDenegadoException("Tu usuario está dado de baja.");
                }
                return u;
            }
        }
        throw new AccesoDenegadoException("El email o la clave son incorrectos.");
    }

    public Cliente registrarCliente(String dni, String nombre, String email,
                                    String clave, String domicilio)
            throws DatosInvalidosException, UsuarioDuplicadoException, SQLException {
        Cliente cliente = new Cliente(dni, nombre, email, clave, domicilio, true);
        guardarNuevo(cliente);
        return cliente;
    }

    public Usuario consultar(String dni) throws EntidadNoEncontradaException {
        for (Usuario u : usuarios) {
            if (u.getDni().equals(dni)) {
                return u;
            }
        }
        throw new EntidadNoEncontradaException("No existe un usuario con DNI " + dni + ".");
    }

    public List<Usuario> buscar(Usuario actor, String texto) throws AccesoDenegadoException {
        verificarPermiso(actor, Permiso.GESTIONAR_USUARIOS);
        List<Usuario> resultado = new ArrayList<>();
        String buscado = texto == null ? "" : texto.trim().toLowerCase();
        for (Usuario u : usuarios) {
            if (u.getNombre().toLowerCase().contains(buscado)
                    || u.getEmail().toLowerCase().contains(buscado)
                    || u.getDni().contains(buscado)) {
                resultado.add(u);
            }
        }
        return resultado;
    }

    public Usuario modificarDatos(Usuario actor, String dni, String nombre, String email)
            throws AccesoDenegadoException, EntidadNoEncontradaException,
            DatosInvalidosException, UsuarioDuplicadoException, SQLException {
        verificarPermiso(actor, Permiso.GESTIONAR_USUARIOS);
        Usuario usuario = consultar(dni);
        verificarNoRepetido(null, email == null ? null : email.trim(), usuario);
        usuario.cambiarDatos(nombre, email);
        guardarCambio(usuario);
        return usuario;
    }

    private void verificarPermiso(Usuario actor, Permiso permiso)
            throws AccesoDenegadoException {
        if (actor == null) {
            throw new AccesoDenegadoException("Tenés que iniciar sesión para hacer esto.");
        }
        if (!actor.estaActivo()) {
            throw new AccesoDenegadoException("Tu usuario está dado de baja.");
        }
        if (!actor.permite(permiso)) {
            throw new AccesoDenegadoException("No tenés permiso para realizar esta operación.");
        }
    }

    private void guardarNuevo(Usuario nuevo)
            throws DatosInvalidosException, UsuarioDuplicadoException, SQLException {
        if (nuevo == null) {
            throw new DatosInvalidosException("Falta el usuario que se quiere registrar.");
        }
        if (!nuevo.estaActivo()) {
            throw new DatosInvalidosException("Un usuario nuevo tiene que estar activo.");
        }
        verificarNoRepetido(nuevo.getDni(), nuevo.getEmail(), null);
        usuarioDAO.guardar(nuevo);
        usuarios.add(nuevo);
    }

    private void verificarNoRepetido(String dni, String email, Usuario excepto)
            throws UsuarioDuplicadoException {
        for (Usuario u : usuarios) {
            if (u == excepto) {
                continue;
            }
            if (dni != null && u.getDni().equals(dni)) {
                throw new UsuarioDuplicadoException(
                        "Ya existe un usuario con el DNI " + dni + ".");
            }
            if (email != null && u.getEmail().equalsIgnoreCase(email)) {
                throw new UsuarioDuplicadoException(
                        "Ya existe un usuario con el email " + email + ".");
            }
        }
    }

    private void guardarCambio(Usuario usuario) throws SQLException {
        try {
            usuarioDAO.actualizar(usuario);
        } catch (SQLException e) {
            usuarios = usuarioDAO.listar();
            throw e;
        }
    }
}