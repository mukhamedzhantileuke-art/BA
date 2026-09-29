package logistics.legacy;

public class LegacyOrbcommTransponder {
    private final String constellationName;
    private boolean simulateFailure = false;
    private int forcedErrorCode = OrbcommTransmissionResult.STATUS_OK;

    public LegacyOrbcommTransponder(String constellationName) {
        this.constellationName = constellationName;
    }

    public void setSimulateFailure(boolean simulateFailure, int errorCode) {
        this.simulateFailure = simulateFailure;
        this.forcedErrorCode = errorCode;
    }

    public OrbcommTransmissionResult transmitSatelliteBurst(long transponderHex, byte[] payloadBytes, boolean emergencyBurst) {
        if (simulateFailure) {
            return new OrbcommTransmissionResult(forcedErrorCode, "Uplink drop on constellation " + constellationName, 0L);
        }

        if (payloadBytes == null || payloadBytes.length == 0) {
            return new OrbcommTransmissionResult(OrbcommTransmissionResult.ERR_BUFFER_OVERFLOW, "Empty binary burst buffer", 0L);
        }

        long ackId = System.currentTimeMillis();
        System.out.println("[Legacy Orbcomm] Burst sent to " + constellationName + " | Transponder: 0x" + Long.toHexString(transponderHex).toUpperCase() +
                " | Bytes: " + payloadBytes.length + " | Emergency: " + emergencyBurst + " | ACK: " + ackId);

        return new OrbcommTransmissionResult(OrbcommTransmissionResult.STATUS_OK, "Burst acknowledged by satellite", ackId);
    }
}
