package com.fitness.fitnessaicoach.repository;

import com.fitness.fitnessaicoach.domain.AIRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
<<<<<<< HEAD
import java.util.Optional;
=======
>>>>>>> main
import java.util.UUID;

public interface AIRecommendationRepository extends JpaRepository<AIRecommendation, UUID> {

    List<AIRecommendation> findByDailyLogId(UUID dailyLogId);

<<<<<<< HEAD
    Optional<AIRecommendation> findFirstByDailyLogIdOrderByCreatedAtDescIdDesc(UUID dailyLogId);

    void deleteByDailyLogId(UUID dailyLogId);

=======
>>>>>>> main
    @Query("""
            select aiRecommendation
            from AIRecommendation aiRecommendation, DailyLog dailyLog
            where aiRecommendation.dailyLogId = dailyLog.id
              and dailyLog.user.id = :userId
            order by aiRecommendation.createdAt desc, aiRecommendation.id desc
            """)
    List<AIRecommendation> findByUserId(UUID userId);
}
