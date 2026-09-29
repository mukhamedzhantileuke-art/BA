package logistics.adapter;

import logistics.exception.TelemetryDeliveryException;
import logistics.implementor.TelemetrySender;
import logistics.legacy.LegacyOrbcommTransponder;
import logistics.legacy.OrbcommTransmissionResult;
import logistics.model.TelemetryPacket;

import java.nio.charset.StandardCharsets;

public class SatelliteTransponderAdapter implements TelemetrySender {
    private final LegacyOrbcommTransponder transponder;

    public SatelliteTransponderAdapter(LegacyOrbcommTransponder transponder) {
        if (transponder == null) {
            throw new IllegalArgumentException("LegacyOrbcommTransponder cannot be null");
        }
        this.transponder = transponder;
    }

    @Override
    public void sendTelemetry(TelemetryPacket packet) throws TelemetryDeliveryException {
        if (packet == null) {
            throw new TelemetryDeliveryException("Cannot transmit null telemetry packet over satellite");
        }

        long transponderHex = parseTransponderId(packet.getShipmentId());
        byte[] payloadBytes = formatBinaryPayload(packet);
        boolean emergencyBurst = packet.getAlertLevel() >= 3;

        OrbcommTransmissionResult result = transponder.transmitSatelliteBurst(transponderHex, payloadBytes, emergencyBurst);

        if (result.getStatusCode() != OrbcommTransmissionResult.STATUS_OK) {
            throw new TelemetryDeliveryException(translateSatelliteError(result.getStatusCode(), result.getDiagnosticMessage()));
        }
    }

    @Override
    public String getChannelName() {
        return "Orbcomm Satellite Transponder (Adapted)";
    }

    private long parseTransponderId(String shipmentId) {
        if (shipmentId == null || shipmentId.isEmpty()) {
            return 0xDEADBEEFL;
        }
        return Math.abs((long) shipmentId.hashCode());
    }

    private byte[] formatBinaryPayload(TelemetryPacket packet) {
        String compactRecord = packet.getShipmentId() + "|" + packet.getLocation() + "|" +
                packet.getTemperature() + "|" + packet.getHumidity() + "|" +
                packet.getAlertLevel() + "|" + packet.getPayloadData();
        return compactRecord.getBytes(StandardCharsets.UTF_8);
    }

    private String translateSatelliteError(int statusCode, String diagnostic) {
        switch (statusCode) {
            case OrbcommTransmissionResult.ERR_NO_SATELLITE_LINK:
                return "Satellite delivery failed: No line-of-sight uplink to orbital constellation (" + diagnostic + ")";
            case OrbcommTransmissionResult.ERR_BUFFER_OVERFLOW:
                return "Satellite delivery failed: Hardware packet buffer overflow (" + diagnostic + ")";
            case OrbcommTransmissionResult.ERR_HARDWARE_MALFUNCTION:
                return "Satellite delivery failed: Transponder transceiver hardware fault (" + diagnostic + ")";
            default:
                return "Satellite delivery failed with legacy status code [" + statusCode + "]: " + diagnostic;
        }
    }
}
