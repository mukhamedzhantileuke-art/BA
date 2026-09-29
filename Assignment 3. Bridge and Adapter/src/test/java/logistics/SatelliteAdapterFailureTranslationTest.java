package logistics;

import logistics.adapter.SatelliteTransponderAdapter;
import logistics.exception.TelemetryDeliveryException;
import logistics.implementor.TelemetrySender;
import logistics.legacy.LegacyOrbcommTransponder;
import logistics.legacy.OrbcommTransmissionResult;
import logistics.model.TelemetryPacket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SatelliteAdapterFailureTranslationTest {

    private LegacyOrbcommTransponder transponder;
    private TelemetrySender adapter;

    @BeforeEach
    void setUp() {
        transponder = new LegacyOrbcommTransponder("TEST-CONSTELLATION-1");
        adapter = new SatelliteTransponderAdapter(transponder);
    }

    @Test
    @DisplayName("Verify successful translation and transmission through adapter")
    void testSuccessfulSatelliteTransmission() {
        TelemetryPacket packet = new TelemetryPacket(
                "SHIP-ATLANTIC-77",
                "Mid-Atlantic",
                -71.2,
                40.0,
                1,
                "NOMINAL",
                System.currentTimeMillis()
        );

        assertDoesNotThrow(() -> adapter.sendTelemetry(packet));
    }

    @Test
    @DisplayName("Verify legacy ERR_NO_SATELLITE_LINK (-1) is translated into TelemetryDeliveryException")
    void testSignalLossFailureTranslation() {
        transponder.setSimulateFailure(true, OrbcommTransmissionResult.ERR_NO_SATELLITE_LINK);

        TelemetryPacket packet = new TelemetryPacket("SHIP-1", "Ocean", -70.0, 50.0, 3, "CRITICAL", System.currentTimeMillis());

        TelemetryDeliveryException thrown = assertThrows(
                TelemetryDeliveryException.class,
                () -> adapter.sendTelemetry(packet)
        );

        assertTrue(thrown.getMessage().contains("No line-of-sight uplink"));
    }

    @Test
    @DisplayName("Verify legacy ERR_BUFFER_OVERFLOW (-2) is translated into TelemetryDeliveryException")
    void testBufferOverflowFailureTranslation() {
        transponder.setSimulateFailure(true, OrbcommTransmissionResult.ERR_BUFFER_OVERFLOW);

        TelemetryPacket packet = new TelemetryPacket("SHIP-2", "Ocean", -70.0, 50.0, 1, "NOMINAL", System.currentTimeMillis());

        TelemetryDeliveryException thrown = assertThrows(
                TelemetryDeliveryException.class,
                () -> adapter.sendTelemetry(packet)
        );

        assertTrue(thrown.getMessage().contains("buffer overflow"));
    }

    @Test
    @DisplayName("Verify legacy ERR_HARDWARE_MALFUNCTION (-99) is translated into TelemetryDeliveryException")
    void testHardwareFaultFailureTranslation() {
        transponder.setSimulateFailure(true, OrbcommTransmissionResult.ERR_HARDWARE_MALFUNCTION);

        TelemetryPacket packet = new TelemetryPacket("SHIP-3", "Ocean", -70.0, 50.0, 1, "NOMINAL", System.currentTimeMillis());

        TelemetryDeliveryException thrown = assertThrows(
                TelemetryDeliveryException.class,
                () -> adapter.sendTelemetry(packet)
        );

        assertTrue(thrown.getMessage().contains("hardware fault"));
    }

    @Test
    @DisplayName("Verify null packet throws TelemetryDeliveryException")
    void testNullPacketHandling() {
        assertThrows(TelemetryDeliveryException.class, () -> adapter.sendTelemetry(null));
    }
}
