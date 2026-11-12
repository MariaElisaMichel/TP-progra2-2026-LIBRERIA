package Models;

import Enums.EstadoVenta;
import Enums.MedioPago;
import Enums.ModalidadEntrega;
import Exceptions.DatosInvalidosException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Venta con uno o más detalles. No tiene setters: el estado y los datos
 * de anulación se fijan al crear el objeto.
 */
public class Venta {

    private final String id;
    private final LocalDateTime fecha;
    private final Cliente cliente;
    private final List<DetalleVenta> detalles;
    private final MedioPago medio;
    private final ModalidadEntrega entrega;
    private final String domicilioEntrega;      // solo si la entrega es ENVIO
    private final EstadoVenta estado;
    private final LocalDateTime fechaAnulacion; // solo si está ANULADA
    private final String anuladoPorDni;         // solo si está ANULADA

    public Venta(String id, LocalDateTime fecha, Cliente cliente,
                 List<DetalleVenta> detalles, MedioPago medio,
                 ModalidadEntrega entrega, String domicilioEntrega,
                 EstadoVenta estado, LocalDateTime fechaAnulacion,
                 String anuladoPorDni) throws DatosInvalidosException {
        if (id == null || id.isBlank()) {
            throw new DatosInvalidosException(
                    "El id de la venta no puede estar vacío.");
        }
        if (fecha == null) {
            throw new DatosInvalidosException("La fecha es obligatoria.");
        }
        if (cliente == null) {
            throw new DatosInvalidosException("La venta debe tener un cliente.");
        }
        if (detalles == null || detalles.isEmpty()) {
            throw new DatosInvalidosException(
                    "La venta debe tener al menos un detalle.");
        }
        if (medio == null) {
            throw new DatosInvalidosException(
                    "El medio de pago es obligatorio.");
        }
        if (entrega == null) {
            throw new DatosInvalidosException(
                    "La modalidad de entrega es obligatoria.");
        }
        if (entrega == ModalidadEntrega.ENVIO
                && (domicilioEntrega == null || domicilioEntrega.isBlank())) {
            throw new DatosInvalidosException(
                    "El envío requiere domicilio de entrega.");
        }
        if (estado == null) {
            throw new DatosInvalidosException("El estado es obligatorio.");
        }
        validarAnulacion(estado, fechaAnulacion, anuladoPorDni);

        this.id = id.trim();
        this.fecha = fecha;
        this.cliente = cliente;
        this.detalles = new ArrayList<>(detalles);
        this.medio = medio;
        this.entrega = entrega;
        if (entrega == ModalidadEntrega.ENVIO) {
            this.domicilioEntrega = domicilioEntrega.trim();
        } else {
            this.domicilioEntrega = null;
        }
        this.estado = estado;
        this.fechaAnulacion = fechaAnulacion;
        this.anuladoPorDni = anuladoPorDni;
    }

    public double calcularTotal() {
        double total = 0;
        for (DetalleVenta detalle : detalles) {
            total += detalle.calcularSubtotal();
        }
        return total;
    }

    public boolean estaAnulada() {
        return estado == EstadoVenta.ANULADA;
    }

    public String getId() {
        return id;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public List<DetalleVenta> getDetalles() {
        return new ArrayList<>(detalles);
    }

    public MedioPago getMedio() {
        return medio;
    }

    public ModalidadEntrega getEntrega() {
        return entrega;
    }

    public String getDomicilioEntrega() {
        return domicilioEntrega;
    }

    public EstadoVenta getEstado() {
        return estado;
    }

    public LocalDateTime getFechaAnulacion() {
        return fechaAnulacion;
    }

    public String getAnuladoPorDni() {
        return anuladoPorDni;
    }

    /** Una venta anulada debe tener fecha y quién la anuló; una confirmada, ninguno. */
    private static void validarAnulacion(EstadoVenta estado,
                                         LocalDateTime fechaAnulacion, String anuladoPorDni)
            throws DatosInvalidosException {
        boolean conDatos = fechaAnulacion != null
                && anuladoPorDni != null && !anuladoPorDni.isBlank();
        boolean sinDatos = fechaAnulacion == null && anuladoPorDni == null;
        boolean anulada = estado == EstadoVenta.ANULADA;
        if ((anulada && !conDatos) || (!anulada && !sinDatos)) {
            throw new DatosInvalidosException(
                    "Los datos de anulación no coinciden con el estado de la venta.");
        }
    }

    /** Dos ventas son iguales si tienen el mismo id. */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Venta)) {
            return false;
        }
        Venta otra = (Venta) o;
        return id.equals(otra.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Venta [id=" + id + ", cliente=" + cliente.getDni()
                + ", total=" + calcularTotal() + ", estado=" + estado + "]";
    }
}