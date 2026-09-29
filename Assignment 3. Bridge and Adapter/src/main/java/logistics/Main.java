package logistics;

import logistics.abstraction.CargoMonitor;
import logistics.abstraction.PerishableFoodMonitor;
import logistics.abstraction.VaccineColdChainMonitor;
import logistics.adapter.SatelliteTransponderAdapter;
import logistics.exception.TelemetryDeliveryException;
import logistics.implementor.MqttTelemetrySender;
import logistics.implementor.RestApiTelemetrySender;
import logistics.implementor.TelemetrySender;
import logistics.legacy.LegacyOrbcommTransponder;
import logistics.legacy.OrbcommTransmissionResult;
import logistics.router.DynamicTelemetryRouter;
import logistics.router.NetworkZone;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Assignment 3: Bridge and Adapter Patterns in Cold-Chain Logistics ===");

        // 1. Setup Implementors (Two native, one legacy adapted)
        TelemetrySender mqttSender = new MqttTelemetrySender("tcp://broker.coldchain-iot.net:1883", "telemetry/cargo");
        TelemetrySender restSender = new RestApiTelemetrySender("https://api.coldchain-logistics.com/v2/ingest", "sec_live_key_9941");

        LegacyOrbcommTransponder legacyTransponder = new LegacyOrbcommTransponder("ORBCOMM-OG2-SAT4");
        TelemetrySender satelliteAdapter = new SatelliteTransponderAdapter(legacyTransponder);

        // 2. Setup Required Complexity Module: Dynamic Implementor Selection
        DynamicTelemetryRouter router = new DynamicTelemetryRouter();
        router.registerSender(NetworkZone.CELLULAR_BROADBAND, restSender);
        router.registerSender(NetworkZone.CELLULAR_IOT, mqttSender);
        router.registerSender(NetworkZone.REMOTE_OCEAN_POLAR, satelliteAdapter);

        // 3. Setup Refined Abstractions
        CargoMonitor vaccineMonitor = new VaccineColdChainMonitor(
                router.resolveSenderByRoute("https://rotterdam-port-terminal.eu/gate3"),
                -75.0,
                -65.0
        );

        CargoMonitor foodMonitor = new PerishableFoodMonitor(
                router.resolveSenderByRoute("mqtt://inland-corridor-frankfurt"),
                4.0,
                85.0
        );

        // 4. Scenario A: Port Departure (REST API)
        System.out.println("\n--- Scenario A: Vaccine Shipment at Rotterdam Seaport (Broadband) ---");
        vaccineMonitor.processSensorData("VAX-EU-9012", "Port of Rotterdam, Berth 4", -72.4, 45.0);

        // 5. Scenario B: High-Seas Atlantic Crossing (Dynamic Switch to Satellite Transponder Adapter)
        System.out.println("\n--- Scenario B: Mid-Atlantic Ocean Crossing (Dynamic switch to Satellite) ---");
        vaccineMonitor.setSender(router.resolveSenderByRoute("sat://atlantic-ocean-coordinates"));
        vaccineMonitor.processSensorData("VAX-EU-9012", "Mid-Atlantic Lat 32.1N Lon 44.2W", -59.8, 48.0);

        // 6. Scenario C: Food Shipment along Inland Highway (MQTT)
        System.out.println("\n--- Scenario C: Dairy Cargo on Highway Transit (Cellular IoT / MQTT) ---");
        foodMonitor.processSensorData("FOOD-DAIRY-331", "A3 Autobahn km 142", 2.8, 91.0);

        // 7. Scenario D: Failure Translation Demonstration
        System.out.println("\n--- Scenario D: Legacy Satellite Failure Translation ---");
        System.out.println("Simulating solar storm / loss of orbital line-of-sight on legacy transponder...");
        legacyTransponder.setSimulateFailure(true, OrbcommTransmissionResult.ERR_NO_SATELLITE_LINK);

        try {
            vaccineMonitor.processSensorData("VAX-EU-9012", "Mid-Atlantic Lat 34.5N Lon 46.1W", -71.0, 47.0);
        } catch (TelemetryDeliveryException ex) {
            System.out.println("Caught cleanly in Client layer via TelemetryDeliveryException:");
            System.out.println("Error details: " + ex.getMessage());
        }

        System.out.println("\n=== Demonstration Completed Successfully ===");
    }
}
