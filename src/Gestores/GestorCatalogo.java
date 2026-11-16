package Gestores;

import Database.LibroDAO;
import Enums.Genero;
import Enums.Permiso;
import Exceptions.AccesoDenegadoException;
import Exceptions.DatosInvalidosException;
import Exceptions.EntidadNoEncontradaException;
import Interfaces.IRepositorio;
import Models.Autor;
import Models.Libro;
import Models.Usuario;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class GestorCatalogo implements IRepositorio<Libro> {
    
    private final LibroDAO libroDAO;
    private List<Libro> libros;
    private final Set<Autor> autores;
    
    public GestorCatalogo(LibroDAO libroDAO) throws SQLException {
        this.libroDAO = libroDAO;
        this.libros = libroDAO.listar();
        this.autores = new LinkedHashSet<>(libroDAO.listarAutores());
    }
    
    @Override
    public Libro agregar(Usuario actor, Libro elemento)
            throws AccesoDenegadoException, DatosInvalidosException,
            EntidadNoEncontradaException, SQLException {
        verificarPermiso(actor, Permiso.GESTIONAR_LIBROS);
        if (elemento == null) {
            throw new DatosInvalidosException("Falta el libro que se quiere cargar.");
        }
        for (Libro existente : libros) {
            if (existente.getIsbn().equals(elemento.getIsbn())) {
                reponer(actor, elemento.getIsbn(), elemento.getStock());
                return existente;
            }
        }
        for (Autor autor : elemento.getAutores()) {
            if (!autores.contains(autor)) {
                throw new EntidadNoEncontradaException("No existe el autor " + autor
                                                               + ". Guardalo primero con guardarAutor.");
            }
        }
        libroDAO.guardar(elemento);
        libros.add(elemento);
        return elemento;
    }
    
    @Override
    public List<Libro> mostrar(Usuario actor) throws AccesoDenegadoException {
        verificarPermiso(actor, Permiso.VER_CATALOGO);
        List<Libro> activos = new ArrayList<>();
        for (Libro l : libros) {
            if (l.estaActivo()) {
                activos.add(l);
            }
        }
        return activos;
    }
    
    @Override
    public Libro eliminar(Usuario actor, String isbn)
            throws AccesoDenegadoException, DatosInvalidosException,
            EntidadNoEncontradaException, SQLException {
        verificarPermiso(actor, Permiso.GESTIONAR_LIBROS);
        Libro libro = consultar(isbn);
        if (!libro.estaActivo()) {
            throw new DatosInvalidosException(
                    "El libro con ISBN " + isbn + " ya estaba dado de baja.");
        }
        libro.desactivar();
        guardarCambio(libro);
        return libro;
    }
    
    public List<Libro> buscar(String titulo, Genero genero, String autor) {
        String buscadoTitulo = titulo == null ? "" : titulo.trim().toLowerCase();
        String buscadoAutor = autor == null ? "" : autor.trim().toLowerCase();
        List<Libro> resultado = new ArrayList<>();
        for (Libro l : libros) {
            if (!l.estaActivo()) {
                continue;
            }
            if (!l.getTitulo().toLowerCase().contains(buscadoTitulo)) {
                continue;
            }
            if (genero != null && l.getGenero() != genero) {
                continue;
            }
            if (!buscadoAutor.isEmpty() && !tieneAutor(l, buscadoAutor)) {
                continue;
            }
            resultado.add(l);
        }
        return resultado;
    }
    
    public Libro consultar(String isbn) throws EntidadNoEncontradaException {
        for (Libro l : libros) {
            if (l.getIsbn().equals(isbn)) {
                return l;
            }
        }
        throw new EntidadNoEncontradaException("No existe un libro con ISBN " + isbn + ".");
    }
    
    public Autor guardarAutor(Usuario actor, Autor autor)
            throws AccesoDenegadoException, DatosInvalidosException, SQLException {
        verificarPermiso(actor, Permiso.GESTIONAR_LIBROS);
        if (autor == null) {
            throw new DatosInvalidosException("Falta el autor que se quiere guardar.");
        }
        if (autores.contains(autor)) {
            return autor;
        }
        libroDAO.guardarAutor(autor);
        autores.add(autor);
        return autor;
    }
    
    public int reponer(Usuario actor, String isbn, int cantidad)
            throws AccesoDenegadoException, DatosInvalidosException,
            EntidadNoEncontradaException, SQLException {
        verificarPermiso(actor, Permiso.GESTIONAR_LIBROS);
        Libro libro = consultar(isbn);
        if (!libro.estaActivo()) {
            throw new DatosInvalidosException("El libro con ISBN " + isbn
                                                      + " está dado de baja y no se le puede reponer stock.");
        }
        libro.reponer(cantidad);
        guardarCambio(libro);
        return libro.getStock();
    }
    
    public Libro modificarDatos(Usuario actor, String isbn, String titulo, Genero genero,
                                double precio, Set<Autor> nuevosAutores)
            throws AccesoDenegadoException, DatosInvalidosException,
            EntidadNoEncontradaException, SQLException {
        verificarPermiso(actor, Permiso.GESTIONAR_LIBROS);
        Libro actual = consultar(isbn);
        if (!actual.estaActivo()) {
            throw new DatosInvalidosException("El libro con ISBN " + isbn
                                                      + " está dado de baja y no se puede modificar.");
        }
        if (nuevosAutores == null) {
            throw new DatosInvalidosException("El libro necesita al menos un autor.");
        }
        for (Autor autor : nuevosAutores) {
            if (!autores.contains(autor)) {
                throw new EntidadNoEncontradaException("No existe el autor " + autor
                                                               + ". Guardalo primero con guardarAutor.");
            }
        }
        Libro nuevo = new Libro(isbn, titulo, genero, precio, actual.getStock(),
                                nuevosAutores, true);
        libroDAO.actualizar(nuevo);
        libros.set(libros.indexOf(actual), nuevo);
        return nuevo;
    }
    List<Libro> todos() {
        return libros;
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
    
    private boolean tieneAutor(Libro libro, String texto) {
        for (Autor a : libro.getAutores()) {
            if (a.toString().toLowerCase().contains(texto)) {
                return true;
            }
        }
        return false;
    }
    
    private void guardarCambio(Libro libro) throws SQLException {
        try {
            libroDAO.actualizar(libro);
        } catch (SQLException e) {
            libros = libroDAO.listar();
            throw e;
        }
    }
}