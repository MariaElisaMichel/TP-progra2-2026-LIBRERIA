import Enums.Genero;
import Enums.MedioPago;
import Enums.ModalidadEntrega;
import Enums.Permiso;
import Exceptions.CarritoVacioException;
import Exceptions.EntidadNoEncontradaException;
import Gestores.GestorCatalogo;
import Gestores.GestorUsuario;
import Gestores.GestorVentas;
import Models.Administrador;
import Models.Autor;
import Models.Cliente;
import Models.DetalleVenta;
import Models.Libro;
import Models.Usuario;
import Models.Vendedor;
import Models.Venta;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class MenuConsola {

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final Scanner entrada = new Scanner(System.in);
    private final GestorUsuario usuarios;
    private final GestorCatalogo catalogo;
    private final GestorVentas ventas;
    private Usuario usuarioActual;
    private final Map<String, Integer> cantidades = new HashMap<>();

    public MenuConsola(GestorUsuario usuarios, GestorCatalogo catalogo, GestorVentas ventas) {
        this.usuarios = usuarios;
        this.catalogo = catalogo;
        this.ventas = ventas;
    }

    public void ejecutar() {
        System.out.println("Bienvenidos - Libreria 'Las Superpoderosas' ");
        boolean salir = false;
        while (!salir) {
            try {
                if (usuarioActual == null) {
                    mostrarMenuPublico();
                    int opcion = leerOpcion();
                    if (opcion == 0) {
                        salir = true;
                    } else {
                        opcionPublica(opcion);
                    }
                } else {
                    mostrarMenuSesion();
                    opcionSesion(leerOpcion());
                }
            } catch (Exception e) {
                System.out.println("Error: " + formatearError(e));
            }
        }
        System.out.println("¡Hasta luego!");
    }

    private void mostrarMenuPublico() {
        System.out.println();
        System.out.println("1) Ver catálogo");
        System.out.println("2) Buscar libros");
        System.out.println("3) Registrarme como cliente");
        System.out.println("4) Iniciar sesión");
        System.out.println("0) Salir");
    }

    private void mostrarMenuSesion() {
        System.out.println();
        System.out.println("Sesión: " + usuarioActual.getNombre() + " ("
                + usuarioActual.getClass().getSimpleName() + ")");
        if (usuarioActual.permite(Permiso.VER_CATALOGO)) {
            System.out.println("1) Ver catálogo");
            System.out.println("2) Buscar libros");
        }
        if (usuarioActual.permite(Permiso.COMPRAR)) {
            System.out.println("3) Agregar libro al carrito");
            System.out.println("4) Ver carrito");
            System.out.println("5) Confirmar compra");
        }
        if (usuarioActual.permite(Permiso.VER_MIS_COMPRAS)) {
            System.out.println("6) Mis compras");
        }
        if (usuarioActual.permite(Permiso.GESTIONAR_LIBROS)) {
            System.out.println("7) Cargar libro");
            System.out.println("8) Reponer stock");
            System.out.println("9) Modificar libro");
            System.out.println("10) Dar de baja un libro");
        }
        if (usuarioActual.permite(Permiso.GESTIONAR_VENTAS)) {
            System.out.println("11) Ver todas las ventas");
            System.out.println("12) Anular una venta");
        }
        if (usuarioActual.permite(Permiso.GESTIONAR_USUARIOS)) {
            System.out.println("13) Ver usuarios");
            System.out.println("14) Dar de alta un usuario");
            System.out.println("15) Dar de baja un usuario");
        }
        System.out.println("0) Cerrar sesión");
    }

    private void opcionPublica(int opcion) throws Exception {
        switch (opcion) {
            case 1:
                verCatalogo();
                break;
            case 2:
                buscarLibros();
                break;
            case 3:
                registrarse();
                break;
            case 4:
                iniciarSesion();
                break;
            default:
                System.out.println("Esa opción no existe.");
        }
    }

    private void opcionSesion(int opcion) throws Exception {
        switch (opcion) {
            case 0:
                cerrarSesion();
                break;
            case 1:
                verCatalogo();
                break;
            case 2:
                buscarLibros();
                break;
            case 3:
                agregarAlCarrito();
                break;
            case 4:
                verCarrito();
                break;
            case 5:
                confirmarCompra();
                break;
            case 6:
                misCompras();
                break;
            case 7:
                cargarLibro();
                break;
            case 8:
                reponerStock();
                break;
            case 9:
                modificarLibro();
                break;
            case 10:
                bajaLibro();
                break;
            case 11:
                verVentas();
                break;
            case 12:
                anularVenta();
                break;
            case 13:
                verUsuarios();
                break;
            case 14:
                altaUsuario();
                break;
            case 15:
                bajaUsuario();
                break;
            default:
                System.out.println("Esa opción no existe.");
        }
    }

    private void iniciarSesion() throws Exception {
        String email = leerTexto("Email");
        String clave = leerTexto("Clave");
        usuarioActual = usuarios.autenticar(email, clave);
        cantidades.clear();
        System.out.println("Bienvenido/a, " + usuarioActual.getNombre() + ".");
    }

    private void cerrarSesion() {
        usuarioActual = null;
        cantidades.clear();
        System.out.println("Sesión cerrada.");
    }

    private void registrarse() throws Exception {
        String dni = leerTexto("DNI (7 u 8 dígitos)");
        String nombre = leerTexto("Nombre");
        String email = leerTexto("Email");
        String clave = leerTexto("Clave");
        String domicilio = leerTexto("Domicilio");
        usuarios.registrarCliente(dni, nombre, email, clave, domicilio);
        System.out.println("Registro exitoso. Ya podés iniciar sesión.");
    }

    private void verCatalogo() throws Exception {
        List<Libro> lista;
        if (usuarioActual == null) {
            lista = catalogo.buscar("", null, "");
        } else {
            lista = catalogo.mostrar(usuarioActual);
        }
        imprimirLibros(lista);
    }

    private void buscarLibros() {
        String titulo = leerTexto("Título (Enter para omitir)");
        String autor = leerTexto("Autor (Enter para omitir)");
        Genero genero = null;
        String filtrar = leerTexto("¿Filtrar por género? Presione S para confirmar, N para denegar");
        if (filtrar.equalsIgnoreCase("s")) {
            Genero[] generos = Genero.values();
            genero = generos[elegirNumero("Género", generos)];
        }
        imprimirLibros(catalogo.buscar(titulo, genero, autor));
    }

    private void agregarAlCarrito() throws Exception {
        String isbn = leerTexto("ISBN del libro");
        Libro libro = catalogo.consultar(isbn);
        if (!libro.estaActivo()) {
            System.out.println("Ese libro ya no está disponible.");
            return;
        }
        int cantidad = leerEnteroPositivo("Cantidad");
        int enCarrito = 0;
        if (cantidades.containsKey(isbn)) {
            enCarrito = cantidades.get(isbn);
        }
        if (!libro.tieneStock(enCarrito + cantidad)) {
            System.out.println("No hay stock suficiente: quedan " + libro.getStock()
                    + " y ya tenés " + enCarrito + " en el carrito.");
            return;
        }
        cantidades.put(isbn, enCarrito + cantidad);
        System.out.println("Agregado al carrito: " + libro.getTitulo() + " x" + (enCarrito + cantidad));
    }

    private void verCarrito() throws Exception {
        if (cantidades.isEmpty()) {
            System.out.println("El carrito está vacío.");
            return;
        }
        double total = 0;
        for (Map.Entry<String, Integer> item : cantidades.entrySet()) {
            Libro libro = catalogo.consultar(item.getKey());
            double subtotal = libro.getPrecio() * item.getValue();
            total += subtotal;
            System.out.println(libro.getTitulo() + " x" + item.getValue()
                    + " = $" + String.format("%.2f", subtotal));
        }
        System.out.println("Total: $" + String.format("%.2f", total));
    }

    private void confirmarCompra() throws Exception {
        if (cantidades.isEmpty()) {
            throw new CarritoVacioException(
                    "No hay libros en el carrito: agregá al menos uno antes de confirmar.");
        }
        MedioPago[] medios = MedioPago.values();
        MedioPago medio = medios[elegirNumero("Medio de pago", medios)];
        ModalidadEntrega[] entregas = ModalidadEntrega.values();
        ModalidadEntrega entrega = entregas[elegirNumero("Modalidad de entrega", entregas)];
        String domicilio = null;
        if (entrega == ModalidadEntrega.ENVIO) {
            domicilio = leerTexto("Domicilio de entrega (Enter para usar el registrado)");
            if (domicilio.isEmpty() && usuarioActual instanceof Cliente) {
                domicilio = ((Cliente) usuarioActual).getDomicilio();
            }
        }
        Venta venta = ventas.comprar(usuarioActual, new HashMap<>(cantidades),
                medio, entrega, domicilio);
        cantidades.clear();
        System.out.println("¡Compra realizada!");
        System.out.println(formatearVenta(venta));
    }

    private void misCompras() throws Exception {
        List<Venta> propias = ventas.listarPropias(usuarioActual);
        if (propias.isEmpty()) {
            System.out.println("Todavía no hiciste compras.");
            return;
        }
        for (Venta v : propias) {
            System.out.println(formatearVenta(v));
            System.out.println();
        }
    }

    private void cargarLibro() throws Exception {
        String isbn = leerTexto("ISBN");
        Libro existente = null;
        try {
            existente = catalogo.consultar(isbn);
        } catch (EntidadNoEncontradaException e) {
            existente = null;
        }
        if (existente != null) {
            int cantidad = leerEnteroPositivo("Ese libro ya existe (" + existente.getTitulo()
                    + "). Cantidad a sumar al stock");
            int stock = catalogo.reponer(usuarioActual, isbn, cantidad);
            System.out.println("Se sumó stock. Stock actual: " + stock);
            return;
        }
        String titulo = leerTexto("Título");
        Genero[] generos = Genero.values();
        Genero genero = generos[elegirNumero("Género", generos)];
        double precio = leerPrecio("Precio");
        int stock = leerEnteroPositivo("Stock inicial");
        List<Autor> autores = leerAutores();
        catalogo.agregar(usuarioActual, new Libro(isbn, titulo, genero, precio, stock, autores, true));
        System.out.println("Libro cargado.");
    }

    private void reponerStock() throws Exception {
        String isbn = leerTexto("ISBN");
        int cantidad = leerEnteroPositivo("Cantidad a sumar");
        System.out.println("Stock actual: " + catalogo.reponer(usuarioActual, isbn, cantidad));
    }

    private void modificarLibro() throws Exception {
        String isbn = leerTexto("ISBN del libro a modificar");
        System.out.println("Datos actuales: " + formatearLibro(catalogo.consultar(isbn)));
        String titulo = leerTexto("Nuevo título");
        Genero[] generos = Genero.values();
        Genero genero = generos[elegirNumero("Nuevo género", generos)];
        double precio = leerPrecio("Nuevo precio");
        List<Autor> autores = leerAutores();
        catalogo.modificarDatos(usuarioActual, isbn, titulo, genero, precio, autores);
        System.out.println("Libro modificado.");
    }

    private void bajaLibro() throws Exception {
        String isbn = leerTexto("ISBN del libro a dar de baja");
        catalogo.eliminar(usuarioActual, isbn);
        System.out.println("Libro dado de baja.");
    }

    private void verVentas() throws Exception {
        List<Venta> todas = ventas.mostrar(usuarioActual);
        if (todas.isEmpty()) {
            System.out.println("Todavía no hay ventas.");
            return;
        }
        for (Venta v : todas) {
            System.out.println(formatearVenta(v));
            System.out.println();
        }
    }

    private void anularVenta() throws Exception {
        String id = leerTexto("Id de la venta (por ejemplo V-0001)");
        ventas.eliminar(usuarioActual, id);
        System.out.println("Venta anulada y stock repuesto.");
    }

    private void verUsuarios() throws Exception {
        for (Usuario u : usuarios.mostrar(usuarioActual)) {
            System.out.println(u.getDni() + " - " + u.getNombre() + " ("
                    + u.getClass().getSimpleName() + ", " + u.getEmail() + ") - "
                    + informarEstado(u.estaActivo()));
        }
    }

    private void altaUsuario() throws Exception {
        String[] roles = {"Cliente", "Vendedor", "Administrador"};
        int rol = elegirNumero("Tipo de usuario", roles);
        String dni = leerTexto("DNI (7 u 8 dígitos)");
        String nombre = leerTexto("Nombre");
        String email = leerTexto("Email");
        String clave = leerTexto("Clave");
        Usuario nuevo;
        if (rol == 0) {
            String domicilio = leerTexto("Domicilio");
            nuevo = new Cliente(dni, nombre, email, clave, domicilio, true);
        } else if (rol == 1) {
            nuevo = new Vendedor(dni, nombre, email, clave, true);
        } else {
            nuevo = new Administrador(dni, nombre, email, clave, true);
        }
        usuarios.agregar(usuarioActual, nuevo);
        System.out.println("Usuario creado con exito.");
    }

    private void bajaUsuario() throws Exception {
        String dni = leerTexto("DNI del usuario a dar de baja");
        usuarios.eliminar(usuarioActual, dni);
        System.out.println("Usuario dado de baja.");
    }

    private List<Autor> leerAutores() throws Exception {
        List<Autor> autores = new ArrayList<>();
        boolean otro = true;
        while (otro) {
            String nombre = leerTexto("Nombre del autor");
            String apellido = leerTexto("Apellido del autor");
            Autor autor = catalogo.guardarAutor(usuarioActual, new Autor(nombre, apellido));
            if (!autores.contains(autor)) {
                autores.add(autor);
            }
            otro = leerTexto("¿Agregar otro autor? Presione S en caso afirmativo, N en caso contrario").equalsIgnoreCase("s");
        }
        return autores;
    }

    private void imprimirLibros(List<Libro> lista) {
        if (lista.isEmpty()) {
            System.out.println("No se encontraron libros.");
            return;
        }
        for (Libro l : lista) {
            System.out.println(formatearLibro(l));
        }
    }

    private String leerTexto(String etiqueta) {
        System.out.print(etiqueta + ": ");
        return entrada.nextLine().trim();
    }

    private int leerOpcion() {
        String texto = leerTexto("Elegí una opción");
        try {
            return Integer.parseInt(texto);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private int leerEnteroPositivo(String etiqueta) {
        while (true) {
            String texto = leerTexto(etiqueta);
            try {
                int numero = Integer.parseInt(texto);
                if (numero > 0) {
                    return numero;
                }
            } catch (NumberFormatException e) {
                // sigue al mensaje de abajo
            }
            System.out.println("Ingresá un número entero mayor que cero.");
        }
    }

    private double leerPrecio(String etiqueta) {
        while (true) {
            String texto = leerTexto(etiqueta);
            try {
                double precio = Double.parseDouble(texto.replace(',', '.'));
                if (precio > 0) {
                    return precio;
                }
            } catch (NumberFormatException e) {
                // sigue al mensaje de abajo
            }
            System.out.println("Ingresá un precio mayor que cero (por ejemplo 12500 o 12500.50).");
        }
    }

    private int elegirNumero(String titulo, Object[] opciones) {
        System.out.println(titulo + ":");
        for (int i = 0; i < opciones.length; i++) {
            System.out.println((i + 1) + ") " + legible(opciones[i].toString()));
        }
        while (true) {
            int opcion = leerOpcion();
            if (opcion >= 1 && opcion <= opciones.length) {
                return opcion - 1;
            }
            System.out.println("Elegí un número de la lista.");
        }
    }

    private String informarEstado(boolean activo) {
        return activo ? "Activo" : "Dado de baja";
    }

    private String informarStock(int stock) {
        return stock > 0 ? stock + " disponibles" : "Sin stock";
    }

    private String formatearLibro(Libro libro) {
        return libro.getTitulo() + " | Autores: " + libro.getAutores()
                + " | " + legible(libro.getGenero().name())
                + " | $" + String.format("%.2f", libro.getPrecio())
                + " | " + informarStock(libro.getStock())
                + " | ISBN " + libro.getIsbn();
    }

    private String formatearVenta(Venta venta) {
        StringBuilder texto = new StringBuilder();
        texto.append("Venta ").append(venta.getId())
                .append(" - ").append(venta.getFecha().format(FORMATO_FECHA)).append("\n");
        texto.append("Cliente: ").append(venta.getCliente().getNombre()).append("\n");
        for (DetalleVenta d : venta.getDetalles()) {
            texto.append("  - ").append(d.getLibro().getTitulo())
                    .append(" x").append(d.getCantidad())
                    .append(" a $").append(String.format("%.2f", d.getPrecioUnitario()))
                    .append("\n");
        }
        texto.append("Total: $").append(String.format("%.2f", venta.calcularTotal())).append("\n");
        texto.append("Pago: ").append(legible(venta.getMedio().name())).append("\n");
        texto.append("Entrega: ").append(legible(venta.getEntrega().name()));
        if (venta.getDomicilioEntrega() != null) {
            texto.append(" en ").append(venta.getDomicilioEntrega());
        }
        texto.append("\nEstado: ").append(legible(venta.getEstado().name()));
        if (venta.estaAnulada()) {
            texto.append(" el ").append(venta.getFechaAnulacion().format(FORMATO_FECHA))
                    .append(" por el DNI ").append(venta.getAnuladoPorDni());
        }
        return texto.toString();
    }

    private String formatearError(Exception error) {
        if (error instanceof SQLException) {
            return "Ocurrió un problema con la base de datos. Intentá de nuevo más tarde.";
        }
        if (error.getMessage() == null || error.getMessage().isBlank()) {
            return "Ocurrió un error inesperado.";
        }
        return error.getMessage();
    }

    private String legible(String nombreEnum) {
        String texto = nombreEnum.toLowerCase().replace('_', ' ');
        return Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
    }
}