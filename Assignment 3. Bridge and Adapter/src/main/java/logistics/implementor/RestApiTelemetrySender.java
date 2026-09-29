package logistics.implementor;

import logistics.exception.TelemetryDeliveryException;
import logistics.model.TelemetryPacket;

public class RestApiTelemetrySender implements TelemetrySender {
    private final String endpointUrl;
    private final String apiKey;

    public RestApiTelemetrySender(String endpointUrl, String apiKey) {
        this.endpointUrl = endpointUrl;
        this.apiKey = apiKey;
    }

    @Override
    public void sendTelemetry(TelemetryPacket packet) throws TelemetryDeliveryException {
        if (packet == null) {
            throw new TelemetryDeliveryException("Cannot send null telemetry packet via REST");
        }
        System.out.println("[REST Sender] POST " + endpointUrl + " [Auth: Bearer " + apiKey.substring(0, Math.min(4, apiKey.length())) + "...] -> " + packet);
    }

    @Override
    public String getChannelName() {
        return "REST Cloud API";
    }
}
