package logistics.abstraction;

import logistics.implementor.TelemetrySender;
import logistics.model.TelemetryPacket;

public abstract class CargoMonitor {
    protected TelemetrySender sender;

    public CargoMonitor(TelemetrySender sender) {
        if (sender == null) {
            throw new IllegalArgumentException("TelemetrySender cannot be null");
        }
        this.sender = sender;
    }

    public void setSender(TelemetrySender sender) {
        if (sender == null) {
            throw new IllegalArgumentException("TelemetrySender cannot be null");
        }
        this.sender = sender;
    }

    public TelemetrySender getSender() {
        return sender;
    }

    public abstract void processSensorData(String shipmentId, String location, double temperature, double humidity);

    protected void dispatch(TelemetryPacket packet) {
        System.out.println("[CargoMonitor Bridge] Forwarding telemetry via: " + sender.getChannelName());
        sender.sendTelemetry(packet);
    }
}
