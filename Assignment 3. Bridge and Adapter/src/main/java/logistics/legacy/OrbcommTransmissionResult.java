package logistics.legacy;

public class OrbcommTransmissionResult {
    public static final int STATUS_OK = 0;
    public static final int ERR_NO_SATELLITE_LINK = -1;
    public static final int ERR_BUFFER_OVERFLOW = -2;
    public static final int ERR_HARDWARE_MALFUNCTION = -99;

    private final int statusCode;
    private final String diagnosticMessage;
    private final long satelliteAckId;

    public OrbcommTransmissionResult(int statusCode, String diagnosticMessage, long satelliteAckId) {
        this.statusCode = statusCode;
        this.diagnosticMessage = diagnosticMessage;
        this.satelliteAckId = satelliteAckId;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getDiagnosticMessage() {
        return diagnosticMessage;
    }

    public long getSatelliteAckId() {
        return satelliteAckId;
    }
}
