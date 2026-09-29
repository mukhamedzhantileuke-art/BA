package logistics.implementor;

import logistics.exception.TelemetryDeliveryException;
import logistics.model.TelemetryPacket;

public interface TelemetrySender {
    void sendTelemetry(TelemetryPacket packet) throws TelemetryDeliveryException;
    String getChannelName();
}
