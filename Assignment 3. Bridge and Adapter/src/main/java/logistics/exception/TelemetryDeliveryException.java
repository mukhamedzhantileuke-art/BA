package logistics.exception;

public class TelemetryDeliveryException extends RuntimeException {
    public TelemetryDeliveryException(String message) {
        super(message);
    }

    public TelemetryDeliveryException(String message, Throwable cause) {
        super(message, cause);
    }
}
