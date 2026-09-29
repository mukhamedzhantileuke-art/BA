package logistics;

import logistics.adapter.SatelliteTransponderAdapter;
import logistics.implementor.MqttTelemetrySender;
import logistics.implementor.RestApiTelemetrySender;
import logistics.implementor.TelemetrySender;
import logistics.legacy.LegacyOrbcommTransponder;
import logistics.router.DynamicTelemetryRouter;
import logistics.router.NetworkZone;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DynamicRouterTest {

    private DynamicTelemetryRouter router;
    private TelemetrySender mqttSender;
    private TelemetrySender restSender;
    private TelemetrySender satAdapter;

    @BeforeEach
    void setUp() {
        router = new DynamicTelemetryRouter();
        mqttSender = new MqttTelemetrySender("tcp://test.broker:1883", "topic");
        restSender = new RestApiTelemetrySender("https://test.api/v1", "key123");
        satAdapter = new SatelliteTransponderAdapter(new LegacyOrbcommTransponder("SAT-TEST"));

        router.registerSender(NetworkZone.CELLULAR_IOT, mqttSender);
        router.registerSender(NetworkZone.CELLULAR_BROADBAND, restSender);
        router.registerSender(NetworkZone.REMOTE_OCEAN_POLAR, satAdapter);
    }

    @Test
    @DisplayName("Verify router resolves correct implementor by NetworkZone")
    void testResolveByZone() {
        assertSame(mqttSender, router.resolveSender(NetworkZone.CELLULAR_IOT));
        assertSame(restSender, router.resolveSender(NetworkZone.CELLULAR_BROADBAND));
        assertSame(satAdapter, router.resolveSender(NetworkZone.REMOTE_OCEAN_POLAR));
    }

    @Test
    @DisplayName("Verify router dynamically resolves implementor from route URI string")
    void testResolveByRouteUri() {
        assertSame(satAdapter, router.resolveSenderByRoute("sat://bering-strait-vessel"));
        assertSame(mqttSender, router.resolveSenderByRoute("mqtt://truck-route-66"));
        assertSame(restSender, router.resolveSenderByRoute("https://singapore-port-gateway"));
    }
}
