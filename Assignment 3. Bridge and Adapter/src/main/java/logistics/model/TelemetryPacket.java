package logistics.model;

public class TelemetryPacket {
    private final String shipmentId;
    private final String location;
    private final double temperature;
    private final double humidity;
    private final int alertLevel;
    private final String payloadData;
    private final long timestamp;

    public TelemetryPacket(String shipmentId, String location, double temperature, double humidity, int alertLevel, String payloadData, long timestamp) {
        this.shipmentId = shipmentId;
        this.location = location;
        this.temperature = temperature;
        this.humidity = humidity;
        this.alertLevel = alertLevel;
        this.payloadData = payloadData;
        this.timestamp = timestamp;
    }

    public String getShipmentId() {
        return shipmentId;
    }

    public String getLocation() {
        return location;
    }

    public double getTemperature() {
        return temperature;
    }

    public double getHumidity() {
        return humidity;
    }

    public int getAlertLevel() {
        return alertLevel;
    }

    public String getPayloadData() {
        return payloadData;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "TelemetryPacket{" +
                "id='" + shipmentId + '\'' +
                ", loc='" + location + '\'' +
                ", temp=" + temperature + "C" +
                ", hum=" + humidity + "%" +
                ", alert=" + alertLevel +
                ", data='" + payloadData + '\'' +
                '}';
    }
}
