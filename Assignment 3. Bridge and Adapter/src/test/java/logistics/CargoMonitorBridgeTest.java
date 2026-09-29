package logistics;

import logistics.abstraction.CargoMonitor;
import logistics.abstraction.PerishableFoodMonitor;
import logistics.abstraction.VaccineColdChainMonitor;
import logistics.implementor.TelemetrySender;
import logistics.model.TelemetryPacket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CargoMonitorBridgeTest {

    // Test Stub to capture dispatched telemetry packets
    private static class CapturingTelemetrySenderStub implements TelemetrySender {
        final List<TelemetryPacket> capturedPackets = new ArrayList<>();

        @Override
        public void sendTelemetry(TelemetryPacket packet) {
            capturedPackets.add(packet);
        }

        @Override
        public String getChannelName() {
            return "Test Stub Sender";
        }
    }

    private CapturingTelemetrySenderStub stubSender;

    @BeforeEach
    void setUp() {
        stubSender = new CapturingTelemetrySenderStub();
    }

    @Test
    @DisplayName("Verify VaccineColdChainMonitor normal delegation within safe thermal window")
    void testVaccineMonitorNormalDelegation() {
        CargoMonitor monitor = new VaccineColdChainMonitor(stubSender, -75.0, -65.0);

        monitor.processSensorData("VAX-001", "Basel Depot", -70.5, 40.0);

        assertEquals(1, stubSender.capturedPackets.size());
        TelemetryPacket packet = stubSender.capturedPackets.get(0);
        assertEquals("VAX-001", packet.getShipmentId());
        assertEquals("Basel Depot", packet.getLocation());
        assertEquals(-70.5, packet.getTemperature(), 0.001);
        assertEquals(1, packet.getAlertLevel());
        assertTrue(packet.getPayloadData().contains("NOMINAL"));
    }

    @Test
    @DisplayName("Verify VaccineColdChainMonitor triggers critical alert on thermal breach")
    void testVaccineMonitorThermalBreach() {
        CargoMonitor monitor = new VaccineColdChainMonitor(stubSender, -75.0, -65.0);

        monitor.processSensorData("VAX-002", "Munich Hub", -55.0, 42.0);

        assertEquals(1, stubSender.capturedPackets.size());
        TelemetryPacket packet = stubSender.capturedPackets.get(0);
        assertEquals(3, packet.getAlertLevel());
        assertTrue(packet.getPayloadData().contains("CRITICAL"));
    }

    @Test
    @DisplayName("Verify PerishableFoodMonitor normal delegation and warning thresholds")
    void testPerishableFoodMonitorDelegation() {
        CargoMonitor foodMonitor = new PerishableFoodMonitor(stubSender, 4.0, 85.0);

        // Safe temperature and humidity
        foodMonitor.processSensorData("BERRIES-101", "Verona Cold-Store", 2.5, 90.0);

        // Degraded humidity
        foodMonitor.processSensorData("BERRIES-102", "Alps Transit", 3.0, 70.0);

        assertEquals(2, stubSender.capturedPackets.size());
        assertEquals(1, stubSender.capturedPackets.get(0).getAlertLevel());
        assertEquals(2, stubSender.capturedPackets.get(1).getAlertLevel());
    }

    @Test
    @DisplayName("Verify Bridge allows dynamic implementor hot-swapping")
    void testBridgeDynamicImplementorHotSwap() {
        CapturingTelemetrySenderStub secondStub = new CapturingTelemetrySenderStub();
        CargoMonitor monitor = new VaccineColdChainMonitor(stubSender, -75.0, -65.0);

        monitor.processSensorData("VAX-003", "Origin Port", -72.0, 40.0);
        assertEquals(1, stubSender.capturedPackets.size());
        assertEquals(0, secondStub.capturedPackets.size());

        // Hot swap implementor across the Bridge
        monitor.setSender(secondStub);
        monitor.processSensorData("VAX-003", "At Sea", -71.5, 41.0);

        assertEquals(1, stubSender.capturedPackets.size());
        assertEquals(1, secondStub.capturedPackets.size());
    }
}
