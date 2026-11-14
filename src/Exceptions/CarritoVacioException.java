package Exceptions;

public class CarritoVacioException extends Exception {
    public CarritoVacioException(String mensaje) {
        super(mensaje);
    }
}