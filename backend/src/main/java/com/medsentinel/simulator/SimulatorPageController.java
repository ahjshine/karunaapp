package com.medsentinel.simulator;

import java.util.List;

public class SimulatorPageController {

    public List<Scenario> getScenarios() {
        return List.of(
            new Scenario("Normal nurse shift", "Nurse views own-ward records. Expected: no alert.", "/api/simulator/normal-nurse"),
            new Scenario("Busy clinician burst", "Nurse opens 40 own-ward charts. Expected: no alert.", "/api/simulator/busy-clinician"),
            new Scenario("Brute-force login", "Six failed logins followed by success. Expected: R1 and R1b.", "/api/simulator/brute-force"),
            new Scenario("Billing clerk bulk pull", "Clerk tries clinical records then retrieves 38 billing records. Expected: high alert.", "/api/simulator/billing-bulk"),
            new Scenario("Receptionist reads clinical", "Receptionist attempts a clinical record. Expected: R3 alert.", "/api/simulator/receptionist-clinical"),
            new Scenario("Break-glass use", "Doctor accesses an off-ward patient for an emergency. Expected: medium review alert.", "/api/simulator/break-glass"),
            new Scenario("Tamper log row", "Intentionally alter one log entry in MongoDB and then verify chain integrity.", "/api/simulator/tamper-log"),
            new Scenario("Generate 5,000 Seed Logs", "Populate 5,000 realistic, hash-chained logs spread across 7 days.", "/api/simulator/seed")
        );
    }

    public record Scenario(String name, String description, String endpoint) {}
}
