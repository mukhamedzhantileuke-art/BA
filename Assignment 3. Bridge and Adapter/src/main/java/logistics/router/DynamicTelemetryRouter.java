package logistics.router;

import logistics.implementor.TelemetrySender;

import java.util.EnumMap;
import java.util.Map;

public class DynamicTelemetryRouter {
    private final Map<NetworkZone, TelemetrySender> registry = new EnumMap<>(NetworkZone.class);

    public void registerSender(NetworkZone zone, TelemetrySender sender) {
        if (zone == null || sender == null) {
            throw new IllegalArgumentException("Zone and Sender must not be null");
        }
        registry.put(zone, sender);
    }

    public TelemetrySender resolveSender(NetworkZone zone) {
        TelemetrySender sender = registry.get(zone);
        if (sender == null) {
            throw new IllegalStateException("No telemetry implementor registered for zone: " + zone);
        }
        return sender;
    }

    public TelemetrySender resolveSenderByRoute(String routeUri) {
        if (routeUri == null) {
            throw new IllegalArgumentException("Route URI cannot be null");
        }
        if (routeUri.startsWith("sat://") || routeUri.contains("ocean") || routeUri.contains("polar")) {
            return resolveSender(NetworkZone.REMOTE_OCEAN_POLAR);
        } else if (routeUri.startsWith("mqtt://") || routeUri.contains("highway")) {
            return resolveSender(NetworkZone.CELLULAR_IOT);
        } else if (routeUri.startsWith("https://") || routeUri.contains("port") || routeUri.contains("city")) {
            return resolveSender(NetworkZone.CELLULAR_BROADBAND);
        }
        return resolveSender(NetworkZone.CELLULAR_IOT);
    }
}
