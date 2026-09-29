# Design Rationale Document: Bridge & Adapter in Cold-Chain Logistics

**Course:** Software Design Patterns (AITU)  
**Student:** Mukhamedzhan Tileuke  
**Group:** SE-2531  
**Project:** Cold-Chain Cargo Telemetry & Environmental Monitoring System  

---

## 1. Problem Domain Selection
In pharmaceutical and perishable freight logistics, cargo such as vaccines (mRNA requiring strictly -70°C to -20°C) and fresh dairy/produce must be continuously tracked across international routes. As transport vessels move from port terminals to highways and across deep-ocean routes, communication channels change dramatically (from terrestrial Wi-Fi/Broadband, to Cellular IoT, to orbital Satellite). Concurrently, the business rules governing monitoring also differ by cargo type.

---

## 2. Why Bridge Alone Is Not Enough
The **Bridge pattern** decouples the monitoring business abstractions (`VaccineColdChainMonitor`, `PerishableFoodMonitor`) from the transmission implementations (`MqttTelemetrySender`, `RestApiTelemetrySender`). This prevents a combinatorial explosion of $M \times N$ subclasses (e.g. `VaccineMqttMonitor`, `VaccineRestMonitor`, `VaccineSatelliteMonitor`, `FoodMqttMonitor`, etc.). 

However, Bridge assumes all concrete implementors naturally conform to the common `TelemetrySender` interface. In our domain, deep-ocean connectivity requires communicating with the ship's onboard proprietary satellite transceiver (`LegacyOrbcommTransponder`). Because this legacy library is external and closed-source, it cannot be modified to implement `TelemetrySender`. Bridge alone cannot incorporate this incompatible component.

---

## 3. Why Adapter Alone Is Not Enough
The **Adapter pattern** solves the incompatibility of `LegacyOrbcommTransponder` by wrapping it inside `SatelliteTransponderAdapter` to implement `TelemetrySender`. 

However, Adapter only addresses one-to-one interface mismatch. It provides no mechanism to vary the cargo evaluation logic independently from the communication infrastructure, nor does it prevent class explosion as new cargo types are introduced. Without Bridge, adapting the satellite transponder would still force monolithic, tightly-coupled client classes.

**Conclusion:** Bridge provides the two-dimensional architectural framework; Adapter provides the plug-in mechanism to incorporate legacy hardware into that framework.

---

## 4. Genuine Incompatibility of the Adapted Class
`LegacyOrbcommTransponder` satisfies all criteria for non-trivial incompatibility:
1. **Method Name & Signature:** Calls `transmitSatelliteBurst(...)` instead of `sendTelemetry(TelemetryPacket packet)`.
2. **Parameter Order and Types:** It accepts primitive `long transponderHex`, raw `byte[] payloadBytes`, and `boolean emergencyBurst`, whereas `TelemetrySender` accepts a rich domain object `TelemetryPacket`. The Adapter handles cryptographic/hash ID extraction and binary serialization.
3. **Different Failure Mechanism:** The native implementors signal failures via `TelemetryDeliveryException`. The legacy satellite SDK never throws delivery exceptions; instead, it returns an `OrbcommTransmissionResult` object containing negative status codes (`-1` for uplink drop, `-2` for buffer overflow, `-99` for hardware fault). The Adapter intercepts these error codes and maps them to unified, meaningful `TelemetryDeliveryException` instances without leaking vendor constants or classes to the abstraction layer.

---

## 5. Required Complexity Module: Dynamic Implementor Selection
We implemented **Dynamic Implementor Selection** via `DynamicTelemetryRouter`.
* Cargo carriers dynamically transition across network environments (`CELLULAR_BROADBAND` at ports, `CELLULAR_IOT` on roadways, and `REMOTE_OCEAN_POLAR` on maritime routes).
* The client passes route context (or network zone identifiers) to the router, which resolves the active `TelemetrySender` (including the adapted satellite transceiver) at runtime.
* The Abstraction's `setSender(TelemetrySender)` method allows transparent, runtime hot-swapping of communication channels while the container is en route. The client never hard-codes concrete implementor classes.

---

## 6. Open/Closed Principle (OCP) Compliance on Both Axes
* **Abstraction Axis:** Adding a new cargo type (e.g., `LiveOrganTransplantMonitor`) requires only a new subclass extending `CargoMonitor`. No existing implementors or adapters are touched.
* **Implementor Axis:** Adding a new transmission technology (e.g., `LoraWanTelemetrySender`) requires only a new class implementing `TelemetrySender`. No existing monitors or domain abstractions are modified.

---

## 7. Limitation of the Final Design
The primary architectural trade-off is the **serialization overhead and information flattening** inside the Adapter. Because the satellite interface requires compact `byte[]` arrays to conserve orbital bandwidth, complex structured data inside `TelemetryPacket` is flattened into a delimited binary payload. If a future abstraction introduces rich binary attachments (e.g., camera images of damaged cargo seals), the adapter's single-packet burst model would require multi-packet chunking or protocol renegotiation, which the current `TelemetrySender` interface does not expose.
