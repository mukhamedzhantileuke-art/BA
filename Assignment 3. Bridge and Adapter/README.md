# Assignment 3: Bridge and Adapter Patterns in Cold-Chain Logistics

- **Course:** Software Design Patterns (AITU)
- **Group:** SE-2531
- **Student:** Mukhamedzhan Tileuke
- **Theme:** ❄️ **Cold-Chain Cargo Telemetry & Environmental Monitoring System**

---

## 1. Project Overview & Architecture
This project demonstrates an architectural integration of the **Bridge Pattern** and the **Adapter Pattern** to monitor sensitive pharmaceutical (vaccine) and perishable food shipments.

- **Bridge Abstraction:** `CargoMonitor` (`VaccineColdChainMonitor`, `PerishableFoodMonitor`)
- **Bridge Implementor:** `TelemetrySender` (`MqttTelemetrySender`, `RestApiTelemetrySender`)
- **Incompatible Legacy Class:** `LegacyOrbcommTransponder` (Proprietary satellite burst transceiver SDK)
- **Adapter:** `SatelliteTransponderAdapter` (Bridges the legacy transponder into `TelemetrySender`)
- **Required Complexity Module:** `DynamicTelemetryRouter` (Dynamic runtime implementor selection based on route/network zone)

---

## 2. Directory Structure

```text
Assignment 3. Bridge and Adapter/
├── pom.xml
├── README.md
├── DESIGN_RATIONALE.md
├── diagram.puml
├── src/
│   ├── main/
│   │   └── java/
│   │       └── logistics/
│   │           ├── abstraction/
│   │           │   ├── CargoMonitor.java
│   │           │   ├── VaccineColdChainMonitor.java
│   │           │   └── PerishableFoodMonitor.java
│   │           ├── implementor/
│   │           │   ├── TelemetrySender.java
│   │           │   ├── MqttTelemetrySender.java
│   │           │   └── RestApiTelemetrySender.java
│   │           ├── legacy/
│   │           │   ├── LegacyOrbcommTransponder.java
│   │           │   └── OrbcommTransmissionResult.java
│   │           ├── adapter/
│   │           │   └── SatelliteTransponderAdapter.java
│   │           ├── model/
│   │           │   └── TelemetryPacket.java
│   │           ├── exception/
│   │           │   └── TelemetryDeliveryException.java
│   │           ├── router/
│   │           │   ├── NetworkZone.java
│   │           │   └── DynamicTelemetryRouter.java
│   │           └── Main.java
│   │
│   └── test/
│       └── java/
│           └── logistics/
│               ├── CargoMonitorBridgeTest.java
│               ├── SatelliteAdapterFailureTranslationTest.java
│               └── DynamicRouterTest.java
```

---

## 3. How to Run

### Command Line:
```bash
# 1. Compile main application
javac -d bin -sourcepath src/main/java $(Get-ChildItem -Recurse -Filter *.java src/main/java).FullName

# 2. Run Main demonstration
java -cp bin logistics.Main
```

### In IntelliJ IDEA:
1. Open this directory (`Assignment 3. Bridge and Adapter`) in IntelliJ IDEA.
2. Run `logistics.Main` by clicking the green Run icon.
3. Run all tests under `src/test/java` via JUnit 5.

---

## 4. Defense Cheat Sheet (50% of Grade)

### Q1: Why would Bridge alone or Adapter alone not be enough?
* **Bridge alone is not enough** because the satellite transceiver is a closed-source legacy SDK (`LegacyOrbcommTransponder`) that does not implement our `TelemetrySender` interface.
* **Adapter alone is not enough** because we also have an independent hierarchy of cargo monitoring logic (`VaccineColdChainMonitor`, `PerishableFoodMonitor`) that must vary independently from communication protocols to prevent combinatorial class explosion.

### Q2: How is your wrapped class genuinely incompatible?
1. **Method signature:** `transmitSatelliteBurst(...)` instead of `sendTelemetry(TelemetryPacket)`.
2. **Parameters:** Primitive `long`, `byte[]`, and `boolean` instead of rich domain object `TelemetryPacket`.
3. **Failure mechanism:** Returns integer error codes (`-1`, `-2`, `-99`) inside `OrbcommTransmissionResult` instead of throwing `TelemetryDeliveryException`. The adapter translates these status codes without leaking legacy classes into the abstraction.

### Q3: How is Open/Closed Principle (OCP) satisfied on both axes?
* **Abstraction axis:** Adding a new monitor (e.g. `CryogenicOrganMonitor`) requires extending `CargoMonitor`. No implementors are touched.
* **Implementor axis:** Adding a new protocol (e.g. `LoraWanTelemetrySender`) requires implementing `TelemetrySender`. No monitors are modified.

### Q4: What is your chosen complexity module?
* **Dynamic Implementor Selection:** `DynamicTelemetryRouter` selects the active implementor at runtime based on the carrier's network zone (`CELLULAR_BROADBAND`, `CELLULAR_IOT`, `REMOTE_OCEAN_POLAR`).
