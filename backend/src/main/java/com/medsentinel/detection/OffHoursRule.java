package com.medsentinel.detection;

import com.medsentinel.auth.User;
import com.medsentinel.auth.UserRepository;
import com.medsentinel.logging.AccessLog;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

/**
 * R4: Off-hours access — access outside assigned shift hours.
 */
@Component
public class OffHoursRule implements DetectionRule {

    private final UserRepository userRepo;

    public OffHoursRule(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public String getRuleCode() { return "R4"; }

    @Override
    public String getRuleName() { return "Off-Hours Access"; }

    @Override
    public int getPoints() { return 20; }

    @Override
    public List<RuleHit> evaluate(AccessLog triggeringEntry) {
        if (triggeringEntry.getUserId() == null) return Collections.emptyList();
        if (triggeringEntry.getOutcome() != AccessLog.Outcome.ALLOWED) return Collections.emptyList();

        User user = userRepo.findById(triggeringEntry.getUserId()).orElse(null);
        if (user == null || user.getShiftStart() == null || user.getShiftEnd() == null) {
            return Collections.emptyList();
        }

        LocalTime now = triggeringEntry.getTs().toLocalTime();
        LocalTime shiftStart = user.getShiftStart();
        LocalTime shiftEnd = user.getShiftEnd();

        boolean withinShift;
        if (shiftStart.isBefore(shiftEnd)) {
            withinShift = !now.isBefore(shiftStart) && !now.isAfter(shiftEnd);
        } else {
            // Overnight shift (e.g., 22:00 to 06:00)
            withinShift = !now.isBefore(shiftStart) || !now.isAfter(shiftEnd);
        }

        if (withinShift) return Collections.emptyList();

        return List.of(new RuleHit(
            "R4", user.getUserId(), user.getUsername(), 20,
            String.format("%s accessed the system at %s, outside assigned shift (%s - %s)",
                user.getUsername(), now, shiftStart, shiftEnd),
            List.of(triggeringEntry.getLogId())
        ));
    }
}
