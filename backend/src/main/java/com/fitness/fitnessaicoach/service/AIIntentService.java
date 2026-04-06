package com.fitness.fitnessaicoach.service;

import com.fitness.fitnessaicoach.domain.BodyMetrics;
import com.fitness.fitnessaicoach.domain.DailyLog;
import com.fitness.fitnessaicoach.domain.Exercise;
import com.fitness.fitnessaicoach.domain.Goal;
import com.fitness.fitnessaicoach.domain.MealType;
import com.fitness.fitnessaicoach.domain.User;
import com.fitness.fitnessaicoach.domain.UserGoalType;
import com.fitness.fitnessaicoach.dto.BodyMetricsRequest;
import com.fitness.fitnessaicoach.dto.DailyLogRequest;
import com.fitness.fitnessaicoach.dto.ExerciseRequest;
import com.fitness.fitnessaicoach.dto.GoalRequest;
import com.fitness.fitnessaicoach.dto.MealItemRequest;
import com.fitness.fitnessaicoach.dto.MealRequest;
import com.fitness.fitnessaicoach.dto.WorkoutSessionRequest;
import com.fitness.fitnessaicoach.dto.ai.AICoachingResponse;
import com.fitness.fitnessaicoach.repository.AIRecommendationRepository;
import com.fitness.fitnessaicoach.repository.BodyMetricsRepository;
import com.fitness.fitnessaicoach.repository.DailyLogRepository;
import com.fitness.fitnessaicoach.repository.ExerciseRepository;
import com.fitness.fitnessaicoach.repository.GoalRepository;
import com.fitness.fitnessaicoach.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class AIIntentService {

    private static final Pattern STEPS_PATTERN = Pattern.compile("(\\d{3,6})\\s*steps?");
    private static final Pattern WALK_STEPS_PATTERN = Pattern.compile("(?:walk(?:ed)?|did|got|made)\\s+(\\d{3,6})\\s*steps?");
    private static final Pattern WEIGHT_PATTERN = Pattern.compile("(?:my\\s+weight\\s+is\\s+|weight\\s*(?:is\\s+)?)?(\\d{2,3}(?:[.,]\\d{1,2})?)\\s*kg");
    private static final Pattern SETS_REPS_PATTERN = Pattern.compile("(\\d{1,2})\\s*x\\s*(\\d{1,3})");
    private static final Pattern SETS_REPS_WORDS_PATTERN = Pattern.compile("(\\d{1,2})\\s*sets?\\s*(\\d{1,3})\\s*reps?");
    private static final Pattern EXERCISE_COUNT_PATTERN = Pattern.compile("(\\d{1,2})\\s+exercises?");
    private static final Pattern CALORIES_BURNED_PATTERN = Pattern.compile("(?:burn(?:ed|t)?|burning)\\s+(\\d{2,4}(?:[.,]\\d{1,2})?)\\s*calories?");
    private static final Pattern DURATION_PATTERN = Pattern.compile("(\\d{1,3})\\s*(?:minutes?|mins?)");
    private static final Pattern FOOD_ITEM_SPLIT_PATTERN = Pattern.compile("\\s*(?:,| and )\\s*");
    private static final Pattern FOOD_ENTRY_PATTERN = Pattern.compile("^(\\d+(?:[.,]\\d+)?)\\s+(.+)$");
    private static final Pattern GOAL_LOSE_FAT_PATTERN = Pattern.compile("\\b(?:want|need|trying|plan)\\s+to\\s+(?:lose\\s+(?:fat|weight)|cut)\\b");
    private static final Pattern GOAL_GAIN_MUSCLE_PATTERN = Pattern.compile("\\b(?:want|need|trying|plan)\\s+to\\s+(?:gain\\s+muscle|bulk|build\\s+muscle)\\b");
    private static final Pattern GOAL_MAINTAIN_PATTERN = Pattern.compile("\\b(?:want|need|trying|plan)\\s+to\\s+(?:maintain|stay\\s+the\\s+same)\\b");
    private static final Pattern WORKOUT_KEYWORDS_PATTERN = Pattern.compile("\\b(workout|training|trained|lifted|did\\s+(?:a\\s+)?.*?(push|pull|legs|leg|run|running|walk|walking|cardio|chest|back|shoulders|arms|full body))\\b");
    private static final Pattern EXERCISE_NAME_PATTERN = Pattern.compile("(bench press|squat|deadlift|shoulder press|overhead press|lat pulldown|pull up|push up|row|lunges?|leg press|cardio|running|run|walking|walk)");

    private final UserRepository userRepository;
    private final GoalRepository goalRepository;
    private final DailyLogRepository dailyLogRepository;
    private final BodyMetricsRepository bodyMetricsRepository;
    private final AIRecommendationRepository aiRecommendationRepository;
    private final ExerciseRepository exerciseRepository;
    private final DailyLogService dailyLogService;
    private final MealService mealService;
    private final MealItemService mealItemService;
    private final WorkoutSessionService workoutSessionService;
    private final BodyMetricsService bodyMetricsService;
    private final ExerciseService exerciseService;
    private final GoalService goalService;
    private final AICoachingService aiCoachingService;

    public Optional<String> handleIntent(User user, String messageContent) {
        String normalized = normalize(messageContent);

        if (isProgressQuestion(normalized)) {
            return Optional.of(generateCoachingReply(user));
        }

        ParsedSteps parsedSteps = parseSteps(normalized);
        if (parsedSteps != null) {
            return Optional.of(logSteps(user, parsedSteps.steps()));
        }

        ParsedCaloriesBurned parsedCaloriesBurned = parseCaloriesBurned(normalized);
        if (parsedCaloriesBurned != null) {
            return Optional.of(logCaloriesBurned(user, parsedCaloriesBurned.caloriesBurned()));
        }

        ParsedWeight parsedWeight = parseWeight(normalized);
        if (parsedWeight != null) {
            return Optional.of(logWeight(user, parsedWeight.weightKg()));
        }

        ParsedWorkout parsedWorkout = parseWorkout(normalized);
        if (parsedWorkout != null) {
            return Optional.of(logWorkout(user, parsedWorkout));
        }

        ParsedFood parsedFood = parseFood(normalized);
        if (parsedFood != null) {
            return Optional.of(logFood(user, parsedFood));
        }

        ParsedGoal parsedGoal = parseGoal(normalized);
        if (parsedGoal != null) {
            return Optional.of(logGoal(user, parsedGoal.goalType()));
        }

        return Optional.empty();
    }

    public PromptBuilder.ChatPromptContext buildPromptContext(UUID userId) {
        Goal latestGoal = goalRepository.findTopByUserIdOrderByCreatedAtDescIdDesc(userId).orElse(null);
        DailyLog latestDailyLog = dailyLogRepository.findTopByUserIdOrderByLogDateDescIdDesc(userId).orElse(null);
        BodyMetrics latestBodyMetrics = bodyMetricsRepository.findTopByUserIdOrderByDateDescIdDesc(userId).orElse(null);

        return new PromptBuilder.ChatPromptContext(
                latestGoal != null && latestGoal.getGoalType() != null ? latestGoal.getGoalType().name() : "UNKNOWN",
                latestGoal != null && latestGoal.getTargetCalories() != null ? latestGoal.getTargetCalories() : "unknown",
                latestGoal != null && latestGoal.getTargetProtein() != null ? latestGoal.getTargetProtein() : "unknown",
                latestGoal != null && latestGoal.getTargetCarbs() != null ? latestGoal.getTargetCarbs() : "unknown",
                latestGoal != null && latestGoal.getTargetFat() != null ? latestGoal.getTargetFat() : "unknown",
                latestDailyLog != null && latestDailyLog.getCaloriesConsumed() != null ? latestDailyLog.getCaloriesConsumed() : 0.0,
                latestDailyLog != null && latestDailyLog.getCaloriesBurned() != null ? latestDailyLog.getCaloriesBurned() : 0.0,
                calculateBalance(latestDailyLog),
                latestDailyLog != null && latestDailyLog.getSteps() != null ? latestDailyLog.getSteps() : 0,
                latestBodyMetrics != null && latestBodyMetrics.getWeight() != null ? latestBodyMetrics.getWeight() : "unknown"
        );
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

    private String logCaloriesBurned(User user, double caloriesBurned) {
        DailyLog todayLog = resolveTodayDailyLog(user);

        DailyLogRequest request = new DailyLogRequest();
        request.setUserId(user.getId());
        request.setLogDate(todayLog.getLogDate());
        request.setSteps(todayLog.getSteps());
        request.setCaloriesConsumed(todayLog.getCaloriesConsumed());
        request.setCaloriesBurned(caloriesBurned);

        dailyLogService.createDailyLog(request);
        invalidateCoaching(todayLog.getId());
        return "Logged " + trimNumber(caloriesBurned) + " calories burned for today.";
    }

    private String logWorkout(User user, ParsedWorkout parsedWorkout) {
        DailyLog todayLog = resolveTodayDailyLog(user);
        List<Exercise> exercises = resolveExercises(parsedWorkout);
        double caloriesPerExercise = parsedWorkout.caloriesBurned() / exercises.size();
        int durationPerExercise = Math.max(1, parsedWorkout.durationMinutes() / exercises.size());

        for (Exercise exercise : exercises) {
            WorkoutSessionRequest request = new WorkoutSessionRequest();
            request.setDailyLogId(todayLog.getId());
            request.setExerciseId(exercise.getId());
            request.setSets(parsedWorkout.sets());
            request.setReps(parsedWorkout.reps());
            request.setDuration(durationPerExercise);
            request.setCaloriesBurned(caloriesPerExercise);
            workoutSessionService.createWorkoutSession(request);
        }

        invalidateCoaching(todayLog.getId());
        if (exercises.size() == 1) {
            return "Logged workout \"" + exercises.get(0).getName() + "\" with " + parsedWorkout.sets() + "x" + parsedWorkout.reps() + ".";
        }

        return "Logged " + exercises.size() + " workout exercise(s) for \"" + parsedWorkout.name() + "\" with "
                + parsedWorkout.sets() + "x" + parsedWorkout.reps() + ".";
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
        return "Logged " + parsedFood.items().size() + " food item(s) for "
                + parsedFood.mealType().name().toLowerCase(Locale.ROOT)
                + ". Quantity total: " + trimNumber(totalQuantity) + ".";
    }

    private String logGoal(User user, UserGoalType goalType) {
        GoalRequest request = new GoalRequest();
        request.setGoalType(goalType);
        request.setTargetWeight(user.getWeightKg());
        goalService.createGoal(user.getEmail(), request);
        return "Set your goal to " + formatGoalType(goalType) + ".";
    }

    private List<Exercise> resolveExercises(ParsedWorkout parsedWorkout) {
        if (parsedWorkout.exerciseCount() <= 1) {
            return List.of(resolveOrCreateExercise(parsedWorkout.name()));
        }

        return java.util.stream.IntStream.rangeClosed(1, parsedWorkout.exerciseCount())
                .mapToObj(index -> resolveOrCreateExercise(parsedWorkout.name() + " Exercise " + index))
                .toList();
    }

    private Exercise resolveOrCreateExercise(String workoutName) {
        return exerciseRepository.findFirstByNameIgnoreCase(workoutName)
                .orElseGet(() -> createExercise(workoutName));
    }

    private Exercise createExercise(String workoutName) {
        ExerciseRequest request = new ExerciseRequest();
        request.setName(workoutName);
        request.setMuscleGroup(inferMuscleGroup(workoutName));
        request.setEquipment("Unknown");
        request.setDescription("Created from conversational workout logging.");
        UUID exerciseId = exerciseService.createExercise(request).getId();
        return exerciseRepository.findById(exerciseId).orElseThrow();
    }

    private DailyLog resolveTodayDailyLog(User user) {
        UUID dailyLogId = dailyLogService.getOrCreateTodayLog(user.getId()).getId();
        return dailyLogRepository.findById(dailyLogId).orElseThrow();
    }

    private ParsedSteps parseSteps(String normalized) {
        Matcher matcher = STEPS_PATTERN.matcher(normalized);
        if (matcher.find()) {
            return new ParsedSteps(Integer.parseInt(matcher.group(1)));
        }

        Matcher walkedMatcher = WALK_STEPS_PATTERN.matcher(normalized);
        if (walkedMatcher.find()) {
            return new ParsedSteps(Integer.parseInt(walkedMatcher.group(1)));
        }

        return null;
    }

    private ParsedCaloriesBurned parseCaloriesBurned(String normalized) {
        if (normalized.contains("workout")) {
            return null;
        }

        Matcher matcher = CALORIES_BURNED_PATTERN.matcher(normalized);
        if (!matcher.find()) {
            return null;
        }

        return new ParsedCaloriesBurned(Double.parseDouble(matcher.group(1).replace(',', '.')));
    }

    private ParsedWeight parseWeight(String normalized) {
        Matcher matcher = WEIGHT_PATTERN.matcher(normalized);
        if (!matcher.find() || normalized.contains("question") || normalized.contains("target weight")) {
            return null;
        }
        return new ParsedWeight(Double.parseDouble(matcher.group(1).replace(',', '.')));
    }

    private ParsedWorkout parseWorkout(String normalized) {
        if (!looksLikeWorkout(normalized)) {
            return null;
        }

        RepetitionScheme repetitionScheme = resolveRepetitionScheme(normalized);
        int sets = repetitionScheme.sets();
        int reps = repetitionScheme.reps();

        Matcher exerciseCountMatcher = EXERCISE_COUNT_PATTERN.matcher(normalized);
        int exerciseCount = exerciseCountMatcher.find() ? Integer.parseInt(exerciseCountMatcher.group(1)) : 1;
        int duration = resolveWorkoutDuration(normalized, exerciseCount);
        double caloriesBurned = parseExplicitWorkoutCalories(normalized);
        if (caloriesBurned <= 0.0) {
            caloriesBurned = estimateWorkoutCalories(normalized, exerciseCount, sets, reps, duration);
        }

        String name = resolveWorkoutName(normalized);

        return new ParsedWorkout(name, sets, reps, duration, caloriesBurned, exerciseCount);
    }

    private ParsedFood parseFood(String normalized) {
        if (normalized.isBlank()) {
            return null;
        }
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

        List<FoodEntry> items = java.util.Arrays.stream(FOOD_ITEM_SPLIT_PATTERN.split(normalized))
                .map(String::trim)
                .filter(part -> !part.isBlank())
                .map(this::stripFoodPrefix)
                .map(this::parseFoodEntry)
                .filter(entry -> !entry.foodName().isBlank())
                .toList();

        if (items.isEmpty()) {
            return null;
        }

        return new ParsedFood(resolveMealType(normalized), items);
    }

    private FoodEntry parseFoodEntry(String value) {
        Matcher matcher = FOOD_ENTRY_PATTERN.matcher(value);
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
        return value.replaceAll("\\b(today|for breakfast|for lunch|for dinner|snack|i ate|ate|had|for|my meal was)\\b", "")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String stripFoodPrefix(String value) {
        return value.replaceFirst("^(?:i\\s+ate|i\\s+had|ate|had|my\\s+meal\\s+was)\\s+", "");
    }

    private ParsedGoal parseGoal(String normalized) {
        if (GOAL_LOSE_FAT_PATTERN.matcher(normalized).find()) {
            return new ParsedGoal(UserGoalType.LOSE_WEIGHT);
        }
        if (GOAL_GAIN_MUSCLE_PATTERN.matcher(normalized).find()) {
            return new ParsedGoal(UserGoalType.BUILD_MUSCLE);
        }
        if (GOAL_MAINTAIN_PATTERN.matcher(normalized).find()) {
            return new ParsedGoal(UserGoalType.MAINTAIN);
        }
        return null;
    }

    private boolean isProgressQuestion(String normalized) {
        return normalized.contains("?")
                || normalized.contains("deficit")
                || normalized.contains("progress")
                || normalized.contains("how did i do")
                || normalized.contains("calorie balance");
    }

    private boolean looksLikeWorkout(String normalized) {
        return normalized.contains("workout")
                || normalized.contains("training")
                || normalized.contains("trained")
                || normalized.contains("lifted")
                || normalized.contains("day")
                || normalized.contains("cardio")
                || normalized.contains("ran")
                || normalized.contains("run ")
                || SETS_REPS_PATTERN.matcher(normalized).find()
                || SETS_REPS_WORDS_PATTERN.matcher(normalized).find()
                || EXERCISE_NAME_PATTERN.matcher(normalized).find()
                || WORKOUT_KEYWORDS_PATTERN.matcher(normalized).find();
    }

    private double parseExplicitWorkoutCalories(String normalized) {
        Matcher matcher = CALORIES_BURNED_PATTERN.matcher(normalized);
        if (!matcher.find()) {
            return 0.0;
        }
        return Double.parseDouble(matcher.group(1).replace(',', '.'));
    }

    private RepetitionScheme resolveRepetitionScheme(String normalized) {
        Matcher setsRepsMatcher = SETS_REPS_PATTERN.matcher(normalized);
        if (setsRepsMatcher.find()) {
            return new RepetitionScheme(
                    Integer.parseInt(setsRepsMatcher.group(1)),
                    Integer.parseInt(setsRepsMatcher.group(2))
            );
        }

        Matcher setsRepsWordsMatcher = SETS_REPS_WORDS_PATTERN.matcher(normalized);
        if (setsRepsWordsMatcher.find()) {
            return new RepetitionScheme(
                    Integer.parseInt(setsRepsWordsMatcher.group(1)),
                    Integer.parseInt(setsRepsWordsMatcher.group(2))
            );
        }

        return new RepetitionScheme(3, 10);
    }

    private int resolveWorkoutDuration(String normalized, int exerciseCount) {
        Matcher durationMatcher = DURATION_PATTERN.matcher(normalized);
        if (durationMatcher.find()) {
            return Integer.parseInt(durationMatcher.group(1));
        }
        if (normalized.contains("cardio") || normalized.contains("ran") || normalized.contains("running")) {
            return 30;
        }
        return Math.max(20, exerciseCount * 12);
    }

    private double estimateWorkoutCalories(String normalized, int exerciseCount, int sets, int reps, int duration) {
        double baseCalories = Math.max(120.0, exerciseCount * sets * reps * 0.75);
        if (normalized.contains("cardio") || normalized.contains("ran") || normalized.contains("running")) {
            baseCalories = Math.max(baseCalories, duration * 9.0);
        }
        if (normalized.contains("intense")) {
            baseCalories *= 1.25;
        }
        if (normalized.contains("light")) {
            baseCalories *= 0.8;
        }
        return baseCalories;
    }

    private String resolveWorkoutName(String normalized) {
        if (normalized.contains("push")) {
            return "Push";
        }
        if (normalized.contains("pull")) {
            return "Pull";
        }
        if (normalized.contains("leg day") || normalized.contains("legs") || normalized.contains("leg ")) {
            return "Leg Day";
        }
        if (normalized.contains("trained chest") || normalized.contains("chest")) {
            return "Chest";
        }
        if (normalized.contains("trained back") || normalized.contains("back")) {
            return "Back";
        }
        if (normalized.contains("trained shoulders") || normalized.contains("shoulders")) {
            return "Shoulders";
        }
        if (normalized.contains("cardio")) {
            return "Cardio";
        }
        if (normalized.contains("ran") || normalized.contains("running") || normalized.matches(".*\\brun\\b.*")) {
            return "Running";
        }

        Matcher namedExerciseMatcher = EXERCISE_NAME_PATTERN.matcher(normalized);
        if (namedExerciseMatcher.find()) {
            return titleCase(namedExerciseMatcher.group(1));
        }

        String fallback = normalized.replace("today", "")
                .replace("heavy", "")
                .replace("light", "")
                .replace("intense", "")
                .replace("did", "")
                .replace("completed", "")
                .replace("trained", "")
                .replace("day", "")
                .replaceAll("\\d+\\s*exercises?", "")
                .replaceAll("\\d+\\s*x\\s*\\d+", "")
                .replaceAll("\\d+\\s*sets?\\s*\\d+\\s*reps?", "")
                .replaceAll("\\d+\\s*(?:minutes?|mins?)", "")
                .replaceAll("(?:burn(?:ed|t)?|burning)\\s+\\d+(?:[.,]\\d+)?\\s*calories?", "")
                .replace("workout", "")
                .replace("training", "")
                .trim();
        if (fallback.isBlank()) {
            return "Unknown";
        }
        return titleCase(fallback);
    }

    private double calculateBalance(DailyLog latestDailyLog) {
        if (latestDailyLog == null) {
            return 0.0;
        }

        double consumed = latestDailyLog.getCaloriesConsumed() != null ? latestDailyLog.getCaloriesConsumed() : 0.0;
        double burned = latestDailyLog.getCaloriesBurned() != null ? latestDailyLog.getCaloriesBurned() : 0.0;
        return consumed - burned;
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

    private String formatGoalType(UserGoalType goalType) {
        return switch (goalType) {
            case LOSE_WEIGHT -> "lose weight";
            case BUILD_MUSCLE -> "build muscle";
            case MAINTAIN -> "maintain";
        };
    }

    private record ParsedSteps(int steps) {
    }

    private record ParsedWeight(double weightKg) {
    }

    private record ParsedCaloriesBurned(double caloriesBurned) {
    }

    private record RepetitionScheme(int sets, int reps) {
    }

    private record ParsedWorkout(String name, int sets, int reps, int durationMinutes, double caloriesBurned, int exerciseCount) {
    }

    private record FoodEntry(String foodName, double quantity) {
    }

    private record ParsedFood(MealType mealType, List<FoodEntry> items) {
    }

    private record ParsedGoal(UserGoalType goalType) {
    }
}
