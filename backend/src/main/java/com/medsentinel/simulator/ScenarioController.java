package com.medsentinel.simulator;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/simulator")
public class ScenarioController {

    private final ScenarioRunner scenarioRunner;
    private final DataSeeder dataSeeder;

    public ScenarioController(ScenarioRunner scenarioRunner, DataSeeder dataSeeder) {
        this.scenarioRunner = scenarioRunner;
        this.dataSeeder = dataSeeder;
    }

    @PostMapping("/normal-nurse")
    public ResponseEntity<Map<String, String>> normalNurse() {
        String result = scenarioRunner.runNormalNurseShift();
        return ResponseEntity.ok(Map.of("result", result));
    }

    @PostMapping("/busy-clinician")
    public ResponseEntity<Map<String, String>> busyClinician() {
        String result = scenarioRunner.runBusyClinician();
        return ResponseEntity.ok(Map.of("result", result));
    }

    @PostMapping("/brute-force")
    public ResponseEntity<Map<String, String>> bruteForce() {
        String result = scenarioRunner.runBruteForceLogin();
        return ResponseEntity.ok(Map.of("result", result));
    }

    @PostMapping("/billing-bulk")
    public ResponseEntity<Map<String, String>> billingBulk() {
        String result = scenarioRunner.runBillingClerkBulkPull();
        return ResponseEntity.ok(Map.of("result", result));
    }

    @PostMapping("/receptionist-clinical")
    public ResponseEntity<Map<String, String>> receptionistClinical() {
        String result = scenarioRunner.runReceptionistReadsClinical();
        return ResponseEntity.ok(Map.of("result", result));
    }

    @PostMapping("/break-glass")
    public ResponseEntity<Map<String, String>> breakGlass() {
        String result = scenarioRunner.runBreakGlass();
        return ResponseEntity.ok(Map.of("result", result));
    }

    @PostMapping("/tamper-log")
    public ResponseEntity<Map<String, String>> tamperLog() {
        String result = scenarioRunner.tamperLogRow();
        return ResponseEntity.ok(Map.of("result", result));
    }

    @PostMapping("/seed")
    public ResponseEntity<Map<String, String>> seedLogs(@RequestParam(defaultValue = "5000") int count) {
        int seeded = dataSeeder.generateSeedLogs(count);
        return ResponseEntity.ok(Map.of("result", String.format("Seeded %d realistic, hash-chained log rows across 7 days.", seeded)));
    }
}
