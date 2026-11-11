import Enums.Permiso;
import Exceptions.DatosInvalidosException;
import Models.Administrador;
import Models.Cliente;
import Models.Usuario;
import Models.Vendedor;

void main() {
    try {
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
}