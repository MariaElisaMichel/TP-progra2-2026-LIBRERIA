
import Database.*;
import Enums.Genero;
import Exceptions.*;
import Gestores.GestorCatalogo;
import Gestores.GestorUsuario;
import Models.Autor;
import Models.Usuario;
import Models.Libro;

import java.sql.SQLException;


void main(){
   /* try {
        Usuario cliente = new Cliente("12345678", "Ana", "ana@mail.com", "1234", "Calle 1");
        Usuario vendedor = new Vendedor("23456789", "Sofía", "sofia@mail.com", "5678");
        Usuario admin = new Administrador("1234567", "Luis", "luis@mail.com", "abcd");

        System.out.println(cliente.getNombre() + " puede comprar: "
                + cliente.permite(Permiso.COMPRAR));
        System.out.println(vendedor.getNombre() + " gestiona libros: "
                + vendedor.permite(Permiso.GESTIONAR_LIBROS));
        System.out.println(admin.getNombre() + " gestiona usuarios: "
                + admin.permite(Permiso.GESTIONAR_USUARIOS));
        System.out.println(vendedor.getNombre() + " gestiona usuarios: "
                + vendedor.permite(Permiso.GESTIONAR_USUARIOS));

        Usuario otraAna = new Cliente("12345678", "Ana M.", "otra@mail.com", "9999", "Calle 5");
        System.out.println("Mismo usuario: " + cliente.equals(otraAna));
        System.out.println(cliente);

        new Cliente("123", "Pedro", "pedro@mail.com", "1234", "Calle 2");
    } catch (DatosInvalidosException e) {
        System.out.println("Error: " + e.getMessage());
    }
    try {
        Autor autor = new Autor("20123456", "Julio Cortázar");
        Libro libro = new Libro("9789500429634", "Rayuela", Genero.NOVELA,
                15000, 5, List.of(autor));
        System.out.println(libro);
        System.out.println("Stock tras reponer 3: " + libro.reponer(3));
        System.out.println("Stock tras descontar 2: " + libro.descontar(2));
        System.out.println("¿Hay 10?: " + libro.tieneStock(10));
        libro.descontar(10);
    } catch (DatosInvalidosException e) {
        System.out.println("Datos inválidos: " + e.getMessage());
    } catch (StockInsuficienteException e) {
        System.out.println("Error: " + e.getMessage());
    }

    try {
        Cliente comprador = new Cliente("12345678", "Ana", "ana@mail.com", "1234", "Calle 1");
        Autor autorVenta = new Autor("20123456", "Julio Cortázar");
        Libro rayuela = new Libro("9789500429634", "Rayuela", Genero.NOVELA,
                15000, 5, List.of(autorVenta));
        DetalleVenta detalle = new DetalleVenta(rayuela, 2, rayuela.getPrecio());

        Venta venta = new Venta("V-0001", LocalDateTime.now(), comprador,
                List.of(detalle), MedioPago.TRANSFERENCIA, ModalidadEntrega.ENVIO,
                "Calle 1", EstadoVenta.CONFIRMADA, null, null);
        System.out.println(venta);
        System.out.println("Total: " + venta.calcularTotal());
        System.out.println("¿Anulada?: " + venta.estaAnulada());

        Venta anulada = new Venta("V-0003", LocalDateTime.now(), comprador,
                List.of(detalle), MedioPago.EFECTIVO, ModalidadEntrega.RETIRO,
                null, EstadoVenta.ANULADA, LocalDateTime.now(), "20123456");
        System.out.println("V-0003 anulada: " + anulada.estaAnulada());

        new Venta("V-0002", LocalDateTime.now(), comprador, List.of(detalle),
                MedioPago.EFECTIVO, ModalidadEntrega.ENVIO, "",
                EstadoVenta.CONFIRMADA, null, null);
    } catch (DatosInvalidosException e) {
        System.out.println("Error: " + e.getMessage());
    }

    try (Connection con = Conexion.obtener()) {
        System.out.println("Conexión exitosa a la base: " + con.getCatalog());
    } catch (SQLException e) {
        System.out.println("Error de conexión: " + e.getMessage());
<<<<<<< Updated upstream
    }*/
        
    /*    try{
=======


    try {
>>>>>>> Stashed changes
        new UsuarioDAO().crearTablas();
        System.out.println("Tablas de usuarios listas.");
        }catch(SQLException e){
        System.out.println("Error al crear las tablas: "+e.getMessage());
        }
        
        try{
        UsuarioDAO dao=new UsuarioDAO();
        dao.crearTablas();
        Cliente ana=new Cliente("12345678","Ana","ana@mail.com","1234","Calle 1");
        dao.guardar(ana);
        System.out.println("Cliente guardado: "+ana);
        }catch(DatosInvalidosException e){
        System.out.println("Datos inválidos: "+e.getMessage());
        }catch(SQLException e){
        System.out.println("Error de base de datos: "+e.getMessage());
        }



    try {
        UsuarioDAO dao = new UsuarioDAO();
        dao.guardar(new Vendedor("23456789", "Sofía", "sofia@mail.com", "5678", true));
        dao.guardar(new Administrador("1234567", "Luis", "luis@mail.com", "abcd", true));
        List<Usuario> usuarios = dao.listar();
        System.out.println("Usuarios guardados: " + usuarios.size());
        for (Usuario u : usuarios) {
            System.out.println(u);
        }
    } catch (SQLException e) {
        System.out.println("Error de base de datos: " + e.getMessage());
    } catch (DatosInvalidosException e) {
        throw new RuntimeException(e);
    }


        LibroDAO dao = new LibroDAO();
        try {
            dao.crearTablas();
            System.out.println("Tablas de libros listas.");

            Autor cortazar = new Autor("Julio", "Cortázar");
            Libro rayuela = new Libro("9789500429634", "Rayuela", Genero.NOVELA,
                    15000, 5, List.of(cortazar), true);

            try {
                dao.guardar(rayuela);
                System.out.println("Libro guardado.");
            } catch (SQLException e) {
                System.out.println("No se guardó (¿ya existía?): " + e.getMessage());
            }

            System.out.println("--- Libros en la base ---");
            for (Libro l : dao.listar()) {
                System.out.println(l);
            }

            System.out.println("--- Autores en la base ---");
            for (Autor a : dao.listarAutores()) {
                System.out.println(a);
            }

            rayuela.reponer(3);
            dao.actualizar(rayuela);
            System.out.println("Stock actualizado a: " + rayuela.getStock());

            System.out.println("--- Libros después de actualizar ---");
            for (Libro l : dao.listar()) {
                System.out.println(l);
            }
        } catch (SQLException e) {
            System.out.println("Error de base de datos: " + e.getMessage());
        } catch (DatosInvalidosException e) {
            System.out.println("Datos inválidos: " + e.getMessage());
        }

    try {
        UsuarioDAO usuarioDAO = new UsuarioDAO();
        LibroDAO libroDAO = new LibroDAO();
        VentaDAO ventaDAO = new VentaDAO();
        usuarioDAO.crearTablas();
        libroDAO.crearTablas();
        ventaDAO.crearTablas();
        System.out.println("Tablas listas.");

        List<Usuario> usuarios = usuarioDAO.listar();
        Cliente cliente = null;
        for (Usuario u : usuarios) {
            if (u instanceof Cliente) {
                cliente = (Cliente) u;
                break;
            }
        }
        List<Libro> libros = libroDAO.listar();
        if (cliente == null || libros.isEmpty()) {
            System.out.println("Hace falta al menos un cliente y un libro guardados.");
            return;
        }
        Libro libro = libros.get(0);
        System.out.println("Stock inicial de " + libro.getTitulo() + ": " + libro.getStock());

        String id = ventaDAO.siguienteId();
        DetalleVenta detalle = new DetalleVenta(libro, 2, libro.getPrecio());
        Venta venta = new Venta(id, LocalDateTime.now(), cliente, List.of(detalle),
                MedioPago.EFECTIVO, ModalidadEntrega.RETIRO, null,
                EstadoVenta.CONFIRMADA, null, null);
        ventaDAO.registrarVenta(venta);
        System.out.println("Venta registrada: " + venta);

        libros = libroDAO.listar();
        System.out.println("Stock tras la venta: " + libros.get(0).getStock());

        System.out.println("--- Ventas en la base ---");
        for (Venta v : ventaDAO.listar(usuarios, libros)) {
            System.out.println(v + " anulada=" + v.estaAnulada());
        }

        System.out.println("Primera anulación: "
                + ventaDAO.anularVenta(id, "23456789", LocalDateTime.now()));
        System.out.println("Segunda anulación: "
                + ventaDAO.anularVenta(id, "23456789", LocalDateTime.now()));

        libros = libroDAO.listar();
        System.out.println("Stock tras anular: " + libros.get(0).getStock());
    } catch (SQLException e) {
        System.out.println("Error de base de datos: " + e.getMessage());
    } catch (DatosInvalidosException e) {
        System.out.println("Datos inválidos: " + e.getMessage());
    }
    try {
        GestorUsuario gestor = new GestorUsuario(new UsuarioDAO());

        Usuario admin = gestor.autenticar("luis@mail.com", "abcd");
        System.out.println("Sesión iniciada: " + admin);

        try {
            gestor.autenticar("luis@mail.com", "mala");
        } catch (AccesoDenegadoException e) {
            System.out.println("Esperado - login mal: " + e.getMessage());
        }

        gestor.registrarCliente("30123456", "Marta", "marta@mail.com", "1111", "Calle 9");
        System.out.println("Cliente registrado.");

        System.out.println("--- Todos los usuarios ---");
        for (Usuario u : gestor.mostrar(admin)) {
            System.out.println(u);
        }

        System.out.println("--- Búsqueda 'mar' ---");
        for (Usuario u : gestor.buscar(admin, "mar")) {
            System.out.println(u);
        }

        System.out.println("Modificado: "
                + gestor.modificarDatos(admin, "30123456", "Marta Gómez", "marta@mail.com"));

        Usuario marta = gestor.autenticar("marta@mail.com", "1111");
        try {
            gestor.mostrar(marta);
        } catch (AccesoDenegadoException e) {
            System.out.println("Esperado - cliente sin permiso: " + e.getMessage());
        }

        System.out.println("Baja: " + gestor.eliminar(admin, "30123456"));
        try {
            gestor.eliminar(admin, "30123456");
        } catch (DatosInvalidosException e) {
            System.out.println("Esperado - ya estaba de baja: " + e.getMessage());
        }
        try {
            gestor.autenticar("marta@mail.com", "1111");
        } catch (AccesoDenegadoException e) {
            System.out.println("Esperado - login de baja: " + e.getMessage());
        }
    } catch (AccesoDenegadoException | DatosInvalidosException
             | UsuarioDuplicadoException | EntidadNoEncontradaException e) {
        System.out.println("Error de negocio: " + e.getMessage());
    } catch (SQLException e) {
        System.out.println("Error de base de datos: " + e.getMessage());
    }*/

    String isbn = "9789871138043";
    try {
        GestorUsuario usuarios = new GestorUsuario(new UsuarioDAO());
        GestorCatalogo catalogo = new GestorCatalogo(new LibroDAO());
        Usuario admin = usuarios.autenticar("luis@mail.com", "abcd");

        Autor borges = catalogo.guardarAutor(admin, new Autor("Jorge Luis", "Borges"));
        System.out.println("Autor listo: " + borges);

        Libro ficciones = new Libro(isbn, "Ficciones", Genero.CUENTO, 12000, 4,
                List.of(borges), true);
        catalogo.agregar(admin, ficciones);
        System.out.println("Cargado, stock: " + catalogo.consultar(isbn).getStock());

        catalogo.agregar(admin, new Libro(isbn, "Ficciones", Genero.CUENTO, 12000, 3,
                List.of(borges), true));
        System.out.println("Cargado de nuevo (suma), stock: " + catalogo.consultar(isbn).getStock());

        try {
            catalogo.agregar(admin, new Libro("9780000000002", "Libro con autor fantasma",
                    Genero.NOVELA, 5000, 1, List.of(new Autor("Nadie", "Conocido")), true));
        } catch (EntidadNoEncontradaException e) {
            System.out.println("Esperado - autor inexistente: " + e.getMessage());
        }

        try {
            catalogo.agregar(null, ficciones);
        } catch (AccesoDenegadoException e) {
            System.out.println("Esperado - sin sesión: " + e.getMessage());
        }

        System.out.println("--- Catálogo ---");
        for (Libro l : catalogo.mostrar(admin)) {
            System.out.println(l + " autores=" + l.getAutores());
        }

        System.out.println("--- Búsqueda fic / borges ---");
        System.out.println(catalogo.buscar("fic", null, "borges"));

        System.out.println("Stock tras reponer 5: " + catalogo.reponer(admin, isbn, 5));

        catalogo.modificarDatos(admin, isbn, "Ficciones (edición nueva)",
                Genero.CUENTO, 13500, List.of(borges));
        GestorCatalogo recargado = new GestorCatalogo(new LibroDAO());
        System.out.println("Tras reiniciar: " + recargado.consultar(isbn));

        System.out.println("Baja: " + catalogo.eliminar(admin, isbn));
        try {
            catalogo.reponer(admin, isbn, 1);
        } catch (DatosInvalidosException e) {
            System.out.println("Esperado - libro de baja: " + e.getMessage());
        }
    } catch (AccesoDenegadoException | DatosInvalidosException
             | EntidadNoEncontradaException e) {
        System.out.println("Error de negocio: " + e.getMessage());
    } catch (SQLException e) {
        System.out.println("Error de base de datos: " + e.getMessage());
    }


    }





