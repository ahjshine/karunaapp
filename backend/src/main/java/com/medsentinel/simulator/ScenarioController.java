package com.medsentinel.simulator;

import java.util.Map;

public class ScenarioController {

    private final ScenarioRunner scenarioRunner;
    private final DataSeeder dataSeeder;

    public ScenarioController(ScenarioRunner scenarioRunner, DataSeeder dataSeeder) {
        this.scenarioRunner = scenarioRunner;
        this.dataSeeder = dataSeeder;
    }

    public Map<String, String> normalNurse() {
        return Map.of("result", scenarioRunner.runNormalNurseShift());
    }

    public Map<String, String> busyClinician() {
        return Map.of("result", scenarioRunner.runBusyClinician());
    }

    public Map<String, String> bruteForce() {
        return Map.of("result", scenarioRunner.runBruteForceLogin());
    }

    public Map<String, String> billingBulk() {
        return Map.of("result", scenarioRunner.runBillingClerkBulkPull());
    }

    public Map<String, String> receptionistClinical() {
        return Map.of("result", scenarioRunner.runReceptionistReadsClinical());
    }

    public Map<String, String> breakGlass() {
        return Map.of("result", scenarioRunner.runBreakGlass());
    }

    public Map<String, String> tamperLog() {
        return Map.of("result", scenarioRunner.tamperLogRow());
    }

    public Map<String, String> seedLogs(int count) {
        int seeded = dataSeeder.generateSeedLogs(count);
        return Map.of("result", String.format("Seeded %d realistic, hash-chained log rows in MongoDB across 7 days.", seeded));
    }
}
