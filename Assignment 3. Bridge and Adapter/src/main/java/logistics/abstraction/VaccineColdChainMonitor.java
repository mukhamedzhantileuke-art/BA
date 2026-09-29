package logistics.abstraction;

import logistics.implementor.TelemetrySender;
import logistics.model.TelemetryPacket;

public class VaccineColdChainMonitor extends CargoMonitor {
    private final double minSafeTemp;
    private final double maxSafeTemp;

    public VaccineColdChainMonitor(TelemetrySender sender, double minSafeTemp, double maxSafeTemp) {
        super(sender);
        this.minSafeTemp = minSafeTemp;
        this.maxSafeTemp = maxSafeTemp;
    }

    @Override
    public void processSensorData(String shipmentId, String location, double temperature, double humidity) {
        int alertLevel;
        String statusDescription;

        if (temperature < minSafeTemp || temperature > maxSafeTemp) {
            alertLevel = 3;
            statusDescription = "CRITICAL: Vaccine thermal breach! Temp: " + temperature + "C (Safe window: " + minSafeTemp + "C to " + maxSafeTemp + "C)";
        } else {
            alertLevel = 1;
            statusDescription = "NOMINAL: Vaccine thermal compliance verified. Temp: " + temperature + "C";
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
