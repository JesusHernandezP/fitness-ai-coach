package com.fitness.fitnessaicoach.service;

import com.fitness.fitnessaicoach.ai.provider.AITextGenerationClient;
import com.fitness.fitnessaicoach.domain.BodyMetrics;
import com.fitness.fitnessaicoach.domain.ChatMessage;
import com.fitness.fitnessaicoach.domain.ChatRole;
import com.fitness.fitnessaicoach.domain.ChatSession;
import com.fitness.fitnessaicoach.domain.DailyLog;
import com.fitness.fitnessaicoach.domain.Exercise;
import com.fitness.fitnessaicoach.domain.Goal;
import com.fitness.fitnessaicoach.domain.MealType;
import com.fitness.fitnessaicoach.domain.User;
import com.fitness.fitnessaicoach.dto.BodyMetricsRequest;
import com.fitness.fitnessaicoach.dto.DailyLogRequest;
import com.fitness.fitnessaicoach.dto.ExerciseRequest;
import com.fitness.fitnessaicoach.dto.MealItemRequest;
import com.fitness.fitnessaicoach.dto.MealRequest;
import com.fitness.fitnessaicoach.dto.WorkoutSessionRequest;
import com.fitness.fitnessaicoach.dto.ai.AIChatMessageResponse;
import com.fitness.fitnessaicoach.dto.ai.AICoachingResponse;
import com.fitness.fitnessaicoach.exception.UserNotFoundException;
import com.fitness.fitnessaicoach.repository.BodyMetricsRepository;
import com.fitness.fitnessaicoach.repository.AIRecommendationRepository;
import com.fitness.fitnessaicoach.repository.ChatMessageRepository;
import com.fitness.fitnessaicoach.repository.ChatSessionRepository;
import com.fitness.fitnessaicoach.repository.DailyLogRepository;
import com.fitness.fitnessaicoach.repository.ExerciseRepository;
import com.fitness.fitnessaicoach.repository.GoalRepository;
import com.fitness.fitnessaicoach.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class ChatService {

    private static final int MAX_STORED_MESSAGES = 20;
    private static final Pattern STEPS_PATTERN = Pattern.compile("(\\d{3,6})\\s*steps?");
    private static final Pattern WEIGHT_PATTERN = Pattern.compile("(?:weight\\s*)?(\\d{2,3}(?:[.,]\\d{1,2})?)\\s*kg");
    private static final Pattern SETS_REPS_PATTERN = Pattern.compile("(\\d{1,2})\\s*x\\s*(\\d{1,3})");
    private static final Pattern EXERCISE_COUNT_PATTERN = Pattern.compile("(\\d{1,2})\\s+exercises?");
    private static final Pattern FOOD_ITEM_SPLIT_PATTERN = Pattern.compile("\\s*(?:,| and )\\s*");

    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final GoalRepository goalRepository;
    private final DailyLogRepository dailyLogRepository;
    private final BodyMetricsRepository bodyMetricsRepository;
    private final AIRecommendationRepository aiRecommendationRepository;
    private final ExerciseRepository exerciseRepository;
    private final AITextGenerationClient aiTextGenerationClient;
    private final DailyLogService dailyLogService;
    private final MealService mealService;
    private final MealItemService mealItemService;
    private final WorkoutSessionService workoutSessionService;
    private final BodyMetricsService bodyMetricsService;
    private final ExerciseService exerciseService;
    private final AICoachingService aiCoachingService;

    @Transactional
    public AIChatMessageResponse sendMessage(String email, String messageContent) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found."));

        ChatSession session = getOrCreateActiveSession(user);
        saveMessage(session, ChatRole.USER, messageContent);

        String reply = processMessage(user, session, messageContent).trim();

        saveMessage(session, ChatRole.AI, reply);
        session.setLastActivityAt(LocalDateTime.now());
        chatSessionRepository.save(session);
        trimStoredMessages(session.getId());

        return new AIChatMessageResponse(reply);
    }

    private String processMessage(User user, ChatSession session, String messageContent) {
        String normalized = normalize(messageContent);

        if (isProgressQuestion(normalized)) {
            return generateCoachingReply(user);
        }

        ParsedSteps parsedSteps = parseSteps(normalized);
        if (parsedSteps != null) {
            return logSteps(user, parsedSteps.steps());
        }

        ParsedWeight parsedWeight = parseWeight(normalized);
        if (parsedWeight != null) {
            return logWeight(user, parsedWeight.weightKg());
        }

        ParsedWorkout parsedWorkout = parseWorkout(normalized);
        if (parsedWorkout != null) {
            return logWorkout(user, parsedWorkout);
        }

        ParsedFood parsedFood = parseFood(normalized);
        if (parsedFood != null) {
            return logFood(user, parsedFood);
        }

        String prompt = buildPrompt(user.getId(), session, messageContent);
        return aiTextGenerationClient.generateText(prompt).trim();
    }

    private ChatSession getOrCreateActiveSession(User user) {
        return chatSessionRepository.findTopByUserIdOrderByLastActivityAtDescIdDesc(user.getId())
                .map(existing -> {
                    existing.setLastActivityAt(LocalDateTime.now());
                    return chatSessionRepository.save(existing);
                })
                .orElseGet(() -> chatSessionRepository.save(ChatSession.builder()
                        .user(user)
                        .lastActivityAt(LocalDateTime.now())
                        .build()));
    }

    private void saveMessage(ChatSession session, ChatRole role, String content) {
        chatMessageRepository.save(ChatMessage.builder()
                .session(session)
                .role(role)
                .content(content)
                .build());
    }

    private String generateCoachingReply(User user) {
        DailyLog todaysLog = resolveTodayDailyLog(user);
        AICoachingResponse coaching = aiCoachingService.getCoaching(todaysLog.getId());
        return coaching.getAdvice();
    }

    private String logSteps(User user, int steps) {
        DailyLog todayLog = resolveTodayDailyLog(user);

        DailyLogRequest request = new DailyLogRequest();
        request.setUserId(user.getId());
        request.setLogDate(todayLog.getLogDate());
        request.setSteps(steps);
        request.setCaloriesConsumed(todayLog.getCaloriesConsumed());
        request.setCaloriesBurned(todayLog.getCaloriesBurned());

        dailyLogService.createDailyLog(request);
        invalidateCoaching(todayLog.getId());
        return "Logged " + steps + " steps for today.";
    }

    private String logWeight(User user, double weightKg) {
        LocalDate today = LocalDate.now();
        BodyMetrics existingMetrics = bodyMetricsRepository.findAllByUserIdOrderByDateDescIdDesc(user.getId()).stream()
                .filter(metric -> today.equals(metric.getDate()))
                .findFirst()
                .orElse(null);

        if (existingMetrics != null) {
            existingMetrics.setWeight(weightKg);
            bodyMetricsRepository.save(existingMetrics);
        } else {
            BodyMetricsRequest request = new BodyMetricsRequest();
            request.setWeight(weightKg);
            request.setDate(today);
            bodyMetricsService.createBodyMetrics(user.getEmail(), request);
        }

        user.setWeightKg(weightKg);
        userRepository.save(user);
        return "Logged your weight at " + weightKg + " kg for today.";
    }

    private String logWorkout(User user, ParsedWorkout parsedWorkout) {
        DailyLog todayLog = resolveTodayDailyLog(user);
        Exercise exercise = resolveOrCreateExercise(parsedWorkout.name());

        WorkoutSessionRequest request = new WorkoutSessionRequest();
        request.setDailyLogId(todayLog.getId());
        request.setExerciseId(exercise.getId());
        request.setSets(parsedWorkout.sets());
        request.setReps(parsedWorkout.reps());
        request.setDuration(parsedWorkout.durationMinutes());
        request.setCaloriesBurned(parsedWorkout.caloriesBurned());

        workoutSessionService.createWorkoutSession(request);
        invalidateCoaching(todayLog.getId());
        return "Logged workout \"" + exercise.getName() + "\" with " + parsedWorkout.sets() + "x" + parsedWorkout.reps() + ".";
    }

    private String logFood(User user, ParsedFood parsedFood) {
        DailyLog todayLog = resolveTodayDailyLog(user);

        MealRequest mealRequest = new MealRequest();
        mealRequest.setDailyLogId(todayLog.getId());
        mealRequest.setMealType(parsedFood.mealType());
        UUID mealId = mealService.createMeal(mealRequest).getId();

        double totalQuantity = 0.0;
        for (FoodEntry item : parsedFood.items()) {
            MealItemRequest request = new MealItemRequest();
            request.setMealId(mealId);
            request.setFoodName(item.foodName());
            request.setQuantity(item.quantity());
            mealItemService.createMealItem(request);
            totalQuantity += item.quantity();
        }

        invalidateCoaching(todayLog.getId());
        return "Logged " + parsedFood.items().size() + " food item(s) for " + parsedFood.mealType().name().toLowerCase(Locale.ROOT)
                + ". Quantity total: " + trimNumber(totalQuantity) + ".";
    }

    private Exercise resolveOrCreateExercise(String workoutName) {
        return exerciseRepository.findFirstByNameIgnoreCase(workoutName)
                .orElseGet(() -> {
                    ExerciseRequest request = new ExerciseRequest();
                    request.setName(workoutName);
                    request.setMuscleGroup(inferMuscleGroup(workoutName));
                    request.setEquipment("Unknown");
                    request.setDescription("Created from conversational workout logging.");
                    UUID exerciseId = exerciseService.createExercise(request).getId();
                    return exerciseRepository.findById(exerciseId).orElseThrow();
                });
    }

    private DailyLog resolveTodayDailyLog(User user) {
        UUID dailyLogId = dailyLogService.getOrCreateTodayLog(user.getId()).getId();
        return dailyLogRepository.findById(dailyLogId).orElseThrow();
    }

    private ParsedSteps parseSteps(String normalized) {
        Matcher matcher = STEPS_PATTERN.matcher(normalized);
        if (!matcher.find()) {
            return null;
        }
        return new ParsedSteps(Integer.parseInt(matcher.group(1)));
    }

    private ParsedWeight parseWeight(String normalized) {
        Matcher matcher = WEIGHT_PATTERN.matcher(normalized);
        if (!matcher.find() || normalized.contains("question")) {
            return null;
        }
        return new ParsedWeight(Double.parseDouble(matcher.group(1).replace(',', '.')));
    }

    private ParsedWorkout parseWorkout(String normalized) {
        if (!normalized.contains("workout")) {
            return null;
        }

        Matcher setsRepsMatcher = SETS_REPS_PATTERN.matcher(normalized);
        int sets = 3;
        int reps = 10;
        if (setsRepsMatcher.find()) {
            sets = Integer.parseInt(setsRepsMatcher.group(1));
            reps = Integer.parseInt(setsRepsMatcher.group(2));
        }

        Matcher exerciseCountMatcher = EXERCISE_COUNT_PATTERN.matcher(normalized);
        int exerciseCount = exerciseCountMatcher.find() ? Integer.parseInt(exerciseCountMatcher.group(1)) : 1;
        int duration = Math.max(20, exerciseCount * 12);
        double caloriesBurned = Math.max(120.0, exerciseCount * sets * reps * 0.75);

        String name = normalized.replace("today", "")
                .replace("heavy", "")
                .replaceAll("\\d+\\s*exercises?", "")
                .replaceAll("\\d+\\s*x\\s*\\d+", "")
                .replace("workout", "")
                .trim();
        if (name.isBlank()) {
            name = "Workout";
        }

        return new ParsedWorkout(titleCase(name), sets, reps, duration, caloriesBurned);
    }

    private ParsedFood parseFood(String normalized) {
        if (normalized.contains("?") || normalized.startsWith("am i") || normalized.contains("deficit")) {
            return null;
        }
        if (normalized.contains("steps") || normalized.contains("workout") || normalized.contains("kg") || normalized.startsWith("weight")) {
            return null;
        }
        boolean looksLikeFoodLog = normalized.contains(" and ")
                || normalized.contains(",")
                || normalized.contains("breakfast")
                || normalized.contains("lunch")
                || normalized.contains("dinner")
                || normalized.contains("snack")
                || Character.isDigit(normalized.charAt(0));
        if (!looksLikeFoodLog) {
            return null;
        }

        String[] parts = FOOD_ITEM_SPLIT_PATTERN.split(normalized);
        if (parts.length == 0) {
            return null;
        }

        List<FoodEntry> items = java.util.Arrays.stream(parts)
                .map(String::trim)
                .filter(part -> !part.isBlank())
                .map(this::parseFoodEntry)
                .toList();

        if (items.isEmpty()) {
            return null;
        }

        return new ParsedFood(resolveMealType(normalized), items);
    }

    private FoodEntry parseFoodEntry(String value) {
        Matcher matcher = Pattern.compile("^(\\d+(?:[.,]\\d+)?)\\s+(.+)$").matcher(value);
        if (matcher.find()) {
            return new FoodEntry(normalizeFoodName(matcher.group(2)), Double.parseDouble(matcher.group(1).replace(',', '.')));
        }
        return new FoodEntry(normalizeFoodName(value), 1.0);
    }

    private MealType resolveMealType(String normalized) {
        if (normalized.contains("breakfast")) {
            return MealType.BREAKFAST;
        }
        if (normalized.contains("lunch")) {
            return MealType.LUNCH;
        }
        if (normalized.contains("dinner")) {
            return MealType.DINNER;
        }
        return MealType.SNACK;
    }

    private String normalizeFoodName(String value) {
        return value.replaceAll("\\b(today|for breakfast|for lunch|for dinner|snack)\\b", "")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private boolean isProgressQuestion(String normalized) {
        return normalized.contains("?")
                || normalized.contains("deficit")
                || normalized.contains("progress")
                || normalized.contains("how did i do")
                || normalized.contains("calorie balance");
    }

    private String buildPrompt(UUID userId, ChatSession session, String latestUserMessage) {
        Goal latestGoal = goalRepository.findTopByUserIdOrderByCreatedAtDescIdDesc(userId).orElse(null);
        DailyLog latestDailyLog = dailyLogRepository.findTopByUserIdOrderByLogDateDescIdDesc(userId).orElse(null);
        BodyMetrics latestBodyMetrics = bodyMetricsRepository.findTopByUserIdOrderByDateDescIdDesc(userId).orElse(null);

        List<ChatMessage> recentMessages = chatMessageRepository.findTop20BySessionIdOrderByCreatedAtDescIdDesc(session.getId());
        Collections.reverse(recentMessages);

        return """
                You are a continuous AI fitness coach in an ongoing conversation.

                Current user context:
                - goalType: %s
                - targetCalories: %s
                - targetProtein: %s
                - targetCarbs: %s
                - targetFat: %s
                - caloriesConsumed: %s
                - caloriesBurned: %s
                - calorieBalance: %s
                - steps: %s
                - latestWeight: %s

                Conversation rules:
                - Be supportive, clear, and actionable.
                - Keep replies short: at most 4 sentences.
                - Avoid repetition.
                - Use the recent conversation and the latest fitness context.
                - If data is missing, say so briefly and still help.

                Recent conversation:
                %s

                Latest user message:
                %s
                """.formatted(
                latestGoal != null && latestGoal.getGoalType() != null ? latestGoal.getGoalType().name() : "UNKNOWN",
                latestGoal != null && latestGoal.getTargetCalories() != null ? latestGoal.getTargetCalories() : "unknown",
                latestGoal != null && latestGoal.getTargetProtein() != null ? latestGoal.getTargetProtein() : "unknown",
                latestGoal != null && latestGoal.getTargetCarbs() != null ? latestGoal.getTargetCarbs() : "unknown",
                latestGoal != null && latestGoal.getTargetFat() != null ? latestGoal.getTargetFat() : "unknown",
                latestDailyLog != null && latestDailyLog.getCaloriesConsumed() != null ? latestDailyLog.getCaloriesConsumed() : 0.0,
                latestDailyLog != null && latestDailyLog.getCaloriesBurned() != null ? latestDailyLog.getCaloriesBurned() : 0.0,
                calculateBalance(latestDailyLog),
                latestDailyLog != null && latestDailyLog.getSteps() != null ? latestDailyLog.getSteps() : 0,
                latestBodyMetrics != null && latestBodyMetrics.getWeight() != null ? latestBodyMetrics.getWeight() : "unknown",
                formatConversation(recentMessages),
                latestUserMessage
        );
    }

    private double calculateBalance(DailyLog latestDailyLog) {
        if (latestDailyLog == null) {
            return 0.0;
        }

        double consumed = latestDailyLog.getCaloriesConsumed() != null ? latestDailyLog.getCaloriesConsumed() : 0.0;
        double burned = latestDailyLog.getCaloriesBurned() != null ? latestDailyLog.getCaloriesBurned() : 0.0;
        return consumed - burned;
    }

    private String formatConversation(List<ChatMessage> recentMessages) {
        if (recentMessages.isEmpty()) {
            return "No previous messages.";
        }

        return recentMessages.stream()
                .sorted(Comparator.comparing(ChatMessage::getCreatedAt).thenComparing(ChatMessage::getId))
                .map(message -> message.getRole().name() + ": " + message.getContent())
                .reduce((left, right) -> left + System.lineSeparator() + right)
                .orElse("No previous messages.");
    }

    private void trimStoredMessages(UUID sessionId) {
        List<ChatMessage> allMessages = chatMessageRepository.findBySessionIdOrderByCreatedAtAscIdAsc(sessionId);
        int messagesToDelete = allMessages.size() - MAX_STORED_MESSAGES;
        if (messagesToDelete <= 0) {
            return;
        }

        chatMessageRepository.deleteAll(allMessages.subList(0, messagesToDelete));
    }

    private void invalidateCoaching(UUID dailyLogId) {
        aiRecommendationRepository.deleteByDailyLogId(dailyLogId);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
    }

    private String titleCase(String value) {
        return java.util.Arrays.stream(value.split("\\s+"))
                .filter(part -> !part.isBlank())
                .map(part -> Character.toUpperCase(part.charAt(0)) + part.substring(1))
                .reduce((left, right) -> left + " " + right)
                .orElse("Workout");
    }

    private String inferMuscleGroup(String workoutName) {
        String normalized = workoutName.toLowerCase(Locale.ROOT);
        if (normalized.contains("pull") || normalized.contains("back")) {
            return "Back";
        }
        if (normalized.contains("push") || normalized.contains("chest")) {
            return "Chest";
        }
        if (normalized.contains("leg")) {
            return "Legs";
        }
        return "Full Body";
    }

    private String trimNumber(double value) {
        if (value == (long) value) {
            return String.valueOf((long) value);
        }
        return String.valueOf(value);
    }

    private record ParsedSteps(int steps) {
    }

    private record ParsedWeight(double weightKg) {
    }

    private record ParsedWorkout(String name, int sets, int reps, int durationMinutes, double caloriesBurned) {
    }

    private record FoodEntry(String foodName, double quantity) {
    }

    private record ParsedFood(MealType mealType, List<FoodEntry> items) {
    }
}
