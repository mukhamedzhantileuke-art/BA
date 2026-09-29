package logistics.implementor;

import logistics.exception.TelemetryDeliveryException;
import logistics.model.TelemetryPacket;

public class MqttTelemetrySender implements TelemetrySender {
    private final String brokerUrl;
    private final String topic;

    public MqttTelemetrySender(String brokerUrl, String topic) {
        this.brokerUrl = brokerUrl;
        this.topic = topic;
    }

    @Override
    public void sendTelemetry(TelemetryPacket packet) throws TelemetryDeliveryException {
        if (packet == null) {
            throw new TelemetryDeliveryException("Cannot send null telemetry packet via MQTT");
        }
        System.out.println("[MQTT Sender] Publishing to " + brokerUrl + "/" + topic + " -> " + packet);
    }

    @Override
    public String getChannelName() {
        return "MQTT Cellular IoT";
    }
}
