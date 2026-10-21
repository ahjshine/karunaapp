package com.medsentinel.config;

import com.medsentinel.auth.User;
import com.medsentinel.auth.UserRepository;
import com.medsentinel.patient.*;
import com.medsentinel.rbac.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Initializes default roles, permissions, wards, baselines, users, and patients
 * if the database is currently empty (e.g. fresh MySQL or in-memory H2 dev profile).
 */
@Component
@Order(1)
public class DatabaseBootstrap implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseBootstrap.class);
    private static final String DEFAULT_PW_HASH = "$2y$10$5iOvNK7pwmTprByYlNVZZ.uljMdYNGOWiqRuLeLa8OM55e4gL0sxe"; // password123

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final PermissionRepository permRepo;
    private final WardRepository wardRepo;
    private final RoleBaselineRepository baselineRepo;
    private final PatientRepository patientRepo;
    private final RecordRepository recordRepo;
    private final CareAssignmentRepository careRepo;

    public DatabaseBootstrap(UserRepository userRepo,
                             RoleRepository roleRepo,
                             PermissionRepository permRepo,
                             WardRepository wardRepo,
                             RoleBaselineRepository baselineRepo,
                             PatientRepository patientRepo,
                             RecordRepository recordRepo,
                             CareAssignmentRepository careRepo) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.permRepo = permRepo;
        this.wardRepo = wardRepo;
        this.baselineRepo = baselineRepo;
        this.patientRepo = patientRepo;
        this.recordRepo = recordRepo;
        this.careRepo = careRepo;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepo.count() > 0) {
            log.info("Database already contains users. Skipping bootstrap initialization.");
            return;
        }

        log.info("Initializing default database state (roles, permissions, wards, baselines, users, patients)...");

        // 1. Permissions
        Permission pDemog = permRepo.save(new Permission("VIEW_DEMOGRAPHICS"));
        Permission pClin = permRepo.save(new Permission("VIEW_CLINICAL"));
        Permission pBill = permRepo.save(new Permission("VIEW_BILLING"));
        Permission pExport = permRepo.save(new Permission("EXPORT"));
        Permission pAlerts = permRepo.save(new Permission("VIEW_ALERTS"));
        Permission pLogs = permRepo.save(new Permission("VIEW_LOGS"));
        Permission pAdmin = permRepo.save(new Permission("ADMIN"));

        // 2. Roles
        Role rDoc = new Role("DOCTOR");
        rDoc.getPermissions().addAll(List.of(pDemog, pClin, pExport));
        rDoc = roleRepo.save(rDoc);

        Role rNurse = new Role("NURSE");
        rNurse.getPermissions().addAll(List.of(pDemog, pClin));
        rNurse = roleRepo.save(rNurse);

        Role rClerk = new Role("BILLING");
        rClerk.getPermissions().addAll(List.of(pDemog, pBill));
        rClerk = roleRepo.save(rClerk);

        Role rRec = new Role("RECEPTIONIST");
        rRec.getPermissions().add(pDemog);
        rRec = roleRepo.save(rRec);

        Role rAdmin = new Role("ADMIN");
        rAdmin.getPermissions().addAll(List.of(pDemog, pAdmin));
        rAdmin = roleRepo.save(rAdmin);

        Role rReviewer = new Role("REVIEWER");
        rReviewer.getPermissions().addAll(List.of(pAlerts, pLogs));
        rReviewer = roleRepo.save(rReviewer);

        // 3. Wards
        Ward wCardio = wardRepo.save(new Ward("Cardiology"));
        Ward wNeuro = wardRepo.save(new Ward("Neurology"));
        Ward wOrtho = wardRepo.save(new Ward("Orthopedics"));
        Ward wPeds = wardRepo.save(new Ward("Pediatrics"));
        Ward wGen = wardRepo.save(new Ward("General Medicine"));

        List<Ward> allWards = List.of(wCardio, wNeuro, wOrtho, wPeds, wGen);

        // 4. Role Baselines
        saveBaseline(rNurse.getRoleId(), RoleBaseline.BaselineScope.ON_WARD, 50, BigDecimal.valueOf(1.5));
        saveBaseline(rNurse.getRoleId(), RoleBaseline.BaselineScope.OFF_WARD, 5, BigDecimal.valueOf(1.5));
        saveBaseline(rDoc.getRoleId(), RoleBaseline.BaselineScope.ANY, 30, BigDecimal.valueOf(1.5));
        saveBaseline(rClerk.getRoleId(), RoleBaseline.BaselineScope.ANY, 15, BigDecimal.valueOf(1.5));
        saveBaseline(rRec.getRoleId(), RoleBaseline.BaselineScope.ANY, 20, BigDecimal.valueOf(1.5));

        // 5. Default Users
        User doc = createUser("dr.asha", rDoc, wCardio.getWardId(), LocalTime.of(8, 0), LocalTime.of(20, 0));
        User nurse = createUser("nurse.priya", rNurse, wCardio.getWardId(), LocalTime.of(8, 0), LocalTime.of(20, 0));
        User clerk = createUser("clerk.ravi", rClerk, null, LocalTime.of(9, 0), LocalTime.of(18, 0));
        User rec = createUser("rec.neha", rRec, null, LocalTime.of(9, 0), LocalTime.of(18, 0));
        User admin = createUser("admin", rAdmin, null, LocalTime.of(0, 0), LocalTime.of(23, 59));
        User rev = createUser("rev.meera", rReviewer, null, LocalTime.of(0, 0), LocalTime.of(23, 59));

        // 6. Patients across wards
        String[] firstNames = {"Arjun", "Priya", "Rahul", "Ananya", "Rohan", "Sneha", "Vikram", "Deepa", "Siddharth", "Kavita",
                               "Aditya", "Pooja", "Varun", "Meera", "Karan", "Tanvi", "Nikhil", "Divya", "Gaurav", "Isha",
                               "Abhishek", "Shweta", "Manish", "Sunita", "Rajesh", "Anita", "Suresh", "Geeta", "Amit", "Rekha"};
        String[] lastNames = {"Sharma", "Verma", "Patel", "Mehta", "Rao", "Nair", "Iyer", "Reddy", "Gupta", "Malhotra"};

        List<Patient> createdPatients = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            Patient p = new Patient();
            p.setName(firstNames[i % firstNames.length] + " " + lastNames[i % lastNames.length]);
            p.setDob(LocalDate.of(1960 + (i % 45), 1 + (i % 12), 1 + (i % 28)));
            p.setWardId(allWards.get(i % allWards.size()).getWardId());
            createdPatients.add(patientRepo.save(p));
        }

        // 7. Care assignments & Sample records for first 15 patients
        for (int i = 0; i < createdPatients.size(); i++) {
            Patient p = createdPatients.get(i);

            // Clinical Record
            PatientRecord clin = new PatientRecord();
            clin.setPatientId(p.getPatientId());
            clin.setType(PatientRecord.RecordType.CLINICAL);
            clin.setContent(String.format("Patient %s: BP %d/%d mmHg, HR %d bpm. Assessment: Condition stable.",
                p.getName(), 115 + (i % 25), 75 + (i % 15), 68 + (i % 20)));
            recordRepo.save(clin);

            // Billing Record
            PatientRecord bill = new PatientRecord();
            bill.setPatientId(p.getPatientId());
            bill.setType(PatientRecord.RecordType.BILLING);
            bill.setContent(String.format("Invoice #INV-%05d: Inpatient stay ward charge INR %d. Status: PAID.",
                i + 1, 1500 * ((i % 5) + 1)));
            recordRepo.save(bill);

            // Assign doctor & nurse to Cardiology patients
            if (p.getWardId().equals(wCardio.getWardId())) {
                careRepo.save(new CareAssignment(doc.getUserId(), p.getPatientId()));
                careRepo.save(new CareAssignment(nurse.getUserId(), p.getPatientId()));
            }
        }

        log.info("Database bootstrap completed successfully: 6 users, 5 wards, 50 patients initialized.");
    }

    private User createUser(String username, Role role, Long wardId, LocalTime start, LocalTime end) {
        User u = new User();
        u.setUsername(username);
        u.setPwHash(DEFAULT_PW_HASH);
        u.setRole(role);
        u.setWardId(wardId);
        u.setShiftStart(start);
        u.setShiftEnd(end);
        return userRepo.save(u);
    }

    private void saveBaseline(Long roleId, RoleBaseline.BaselineScope scope, int maxRecords, BigDecimal multiplier) {
        RoleBaseline b = new RoleBaseline();
        b.setRoleId(roleId);
        b.setScope(scope);
        b.setMaxRecordsPer10Min(maxRecords);
        b.setMultiplier(multiplier);
        baselineRepo.save(b);
    }
}
