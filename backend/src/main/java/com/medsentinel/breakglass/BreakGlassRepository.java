package com.medsentinel.breakglass;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BreakGlassRepository extends JpaRepository<BreakGlassEvent, Long> {
    List<BreakGlassEvent> findByReviewedFalseOrderByTsDesc();
    List<BreakGlassEvent> findByUserIdOrderByTsDesc(Long userId);
}
