package logistics.abstraction;

import logistics.implementor.TelemetrySender;
import logistics.model.TelemetryPacket;

public class PerishableFoodMonitor extends CargoMonitor {
    private final double maxTemp;
    private final double minHumidity;

    public PerishableFoodMonitor(TelemetrySender sender, double maxTemp, double minHumidity) {
        super(sender);
        this.maxTemp = maxTemp;
        this.minHumidity = minHumidity;
    }

    @Override
    public void processSensorData(String shipmentId, String location, double temperature, double humidity) {
        int alertLevel;
        String statusDescription;

        boolean tempBreach = temperature > maxTemp;
        boolean humidityDrop = humidity < minHumidity;

        if (tempBreach && humidityDrop) {
            alertLevel = 3;
            statusDescription = "CRITICAL: Severe food spoilage hazard! High temp (" + temperature + "C) & Low humidity (" + humidity + "%)";
        } else if (tempBreach || humidityDrop) {
            alertLevel = 2;
            statusDescription = "WARNING: Food condition degraded. Temp: " + temperature + "C, Humidity: " + humidity + "%";
        } else {
            alertLevel = 1;
            statusDescription = "NOMINAL: Perishable cargo within freshness tolerances";
        }

        TelemetryPacket packet = new TelemetryPacket(
                shipmentId,
                location,
                temperature,
                humidity,
                alertLevel,
                statusDescription,
                System.currentTimeMillis()
        );

        dispatch(packet);
    }
}
