package com.medsentinel.simulator;

import com.medsentinel.auth.User;
import com.medsentinel.auth.UserRepository;
import com.medsentinel.logging.AccessLog;
import com.medsentinel.logging.AccessLogRepository;
import com.medsentinel.logging.LogChainService;
import com.medsentinel.patient.Patient;
import com.medsentinel.patient.PatientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

/**
 * Generates realistic background log traffic across 7 days.
 * Ensures the database contains >= 5,000 properly hash-chained log rows
 * when seeded for demo / presentation purposes.
 */
@Component
@org.springframework.core.annotation.Order(2)
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final AccessLogRepository logRepo;
    private final LogChainService chainService;
    private final UserRepository userRepo;
    private final PatientRepository patientRepo;
    private final boolean seedOnStartup;

    public DataSeeder(AccessLogRepository logRepo,
                      LogChainService chainService,
                      UserRepository userRepo,
                      PatientRepository patientRepo,
                      @Value("${medsentinel.seed.auto:false}") boolean seedOnStartup) {
        this.logRepo = logRepo;
        this.chainService = chainService;
        this.userRepo = userRepo;
        this.patientRepo = patientRepo;
        this.seedOnStartup = seedOnStartup;
    }

    @Override
    public void run(String... args) {
        if (seedOnStartup && logRepo.count() < 100) {
            log.info("Auto-seeding access log with 5,000 realistic entries...");
            generateSeedLogs(5000);
        }
    }

    /**
     * Generates N realistic, hash-chained access log rows spread across 7 days.
     */
    @Transactional
    public int generateSeedLogs(int totalRows) {
        List<User> users = userRepo.findAll();
        List<Patient> patients = patientRepo.findAll();

        if (users.isEmpty() || patients.isEmpty()) {
            log.warn("Cannot seed logs: users or patients table is empty.");
            return 0;
        }

        Random rand = new Random(42); // deterministic seed for reproducibility
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = now.minusDays(7);
        long totalSeconds = 7 * 24 * 3600;
        long stepSeconds = Math.max(1, totalSeconds / totalRows);

        User doctor = users.stream().filter(u -> "dr.asha".equals(u.getUsername())).findFirst().orElse(users.get(0));
        User nurse = users.stream().filter(u -> "nurse.priya".equals(u.getUsername())).findFirst().orElse(users.get(0));
        User clerk = users.stream().filter(u -> "clerk.ravi".equals(u.getUsername())).findFirst().orElse(users.get(0));
        User receptionist = users.stream().filter(u -> "rec.neha".equals(u.getUsername())).findFirst().orElse(users.get(0));

        AccessLog lastLog = logRepo.findTopByOrderByLogIdDesc();
        String currentHash = lastLog != null ? lastLog.getRowHash() : null;

        int created = 0;
        for (int i = 0; i < totalRows; i++) {
            LocalDateTime ts = startTime.plusSeconds(i * stepSeconds + rand.nextInt(Math.max(1, (int) stepSeconds)));

            // 70% normal ward views, 15% billing views, 10% searches, 4% logins, 1% occasional denied
            int roll = rand.nextInt(100);
            AccessLog entry = new AccessLog();
            entry.setTs(ts);
            entry.setIp("10.0." + (rand.nextInt(10) + 1) + "." + (rand.nextInt(250) + 1));

            if (roll < 4) {
                // Login event
                User u = rand.nextBoolean() ? doctor : nurse;
                entry.setUserId(u.getUserId());
                entry.setUsernameAttempted(u.getUsername());
                entry.setAction("LOGIN");
                entry.setOutcome(AccessLog.Outcome.ALLOWED);
                entry.setReason("Authentication successful");
            } else if (roll < 55) {
                // Clinical view by Nurse Priya on cardiology ward
                Patient p = patients.get(rand.nextInt(Math.min(10, patients.size())));
                entry.setUserId(nurse.getUserId());
                entry.setUsernameAttempted(nurse.getUsername());
                entry.setAction("VIEW");
                entry.setResourceType("CLINICAL");
                entry.setPatientId(p.getPatientId());
                entry.setOutcome(AccessLog.Outcome.ALLOWED);
                entry.setReason("Ward chart check");
            } else if (roll < 75) {
                // Clinical view by Dr. Asha
                Patient p = patients.get(rand.nextInt(Math.min(15, patients.size())));
                entry.setUserId(doctor.getUserId());
                entry.setUsernameAttempted(doctor.getUsername());
                entry.setAction("VIEW");
                entry.setResourceType("CLINICAL");
                entry.setPatientId(p.getPatientId());
                entry.setOutcome(AccessLog.Outcome.ALLOWED);
                entry.setReason("Physician round review");
            } else if (roll < 90) {
                // Billing view by Ravi
                Patient p = patients.get(rand.nextInt(patients.size()));
                entry.setUserId(clerk.getUserId());
                entry.setUsernameAttempted(clerk.getUsername());
                entry.setAction("VIEW");
                entry.setResourceType("BILLING");
                entry.setPatientId(p.getPatientId());
                entry.setOutcome(AccessLog.Outcome.ALLOWED);
                entry.setReason("Billing invoice processing");
            } else if (roll < 98) {
                // Demographic search by Neha
                Patient p = patients.get(rand.nextInt(patients.size()));
                entry.setUserId(receptionist.getUserId());
                entry.setUsernameAttempted(receptionist.getUsername());
                entry.setAction("SEARCH");
                entry.setResourceType("DEMOGRAPHIC");
                entry.setPatientId(p.getPatientId());
                entry.setOutcome(AccessLog.Outcome.ALLOWED);
                entry.setReason("Patient check-in verification");
            } else {
                // Sporadic failed login typo
                entry.setUsernameAttempted(rand.nextBoolean() ? "dr.asha" : "nurse.priya");
                entry.setAction("LOGIN");
                entry.setOutcome(AccessLog.Outcome.FAILED);
                entry.setReason("Bad credentials (typo)");
            }

            AccessLog saved = logRepo.save(entry);
            chainService.chainHash(saved, currentHash);
            currentHash = saved.getRowHash();
            created++;
        }

        log.info("Successfully generated {} hash-chained audit log rows.", created);
        return created;
    }
}
