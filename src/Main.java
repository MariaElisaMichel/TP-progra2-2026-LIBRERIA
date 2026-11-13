import Enums.Permiso;
import Exceptions.DatosInvalidosException;
import Models.Administrador;
import Models.Cliente;
import Models.Usuario;
import Models.Vendedor;
import Enums.Genero;
import Exceptions.StockInsuficienteException;
import Models.Autor;
import Models.Libro;
import java.util.List;
import Enums.EstadoVenta;
import Enums.MedioPago;
import Enums.ModalidadEntrega;
import Models.DetalleVenta;
import Models.Venta;
import java.time.LocalDateTime;
import Database.Conexion;
import java.sql.Connection;
import java.sql.SQLException;
import Database.UsuarioDAO;


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
    }*/
        
    /*    try{
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
*/
    
    
    try{
UsuarioDAO dao = new UsuarioDAO();
        dao.guardar(new Vendedor("23456789", "Sofía", "sofia@mail.com", "5678", true));
        dao.guardar(new Administrador("1234567", "Luis", "luis@mail.com", "abcd", true));
List<Usuario> usuarios = dao.listar();
            System.out.

println("Usuarios guardados: "+usuarios.size());
        for(
Usuario u :usuarios){
        System.out.

println(u);
            }
                    }catch(
SQLException e){
        System.out.

println("Error de base de datos: "+e.getMessage());
        }
        }