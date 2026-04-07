package com.fitness.fitnessaicoach.service;

import com.fitness.fitnessaicoach.domain.*;
import com.fitness.fitnessaicoach.dto.*;
import com.fitness.fitnessaicoach.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class AIIntentService {

    private static final Pattern STEPS = Pattern.compile("(\\d{3,6})\\s*(?:pasos|steps)");
    private static final Pattern WEIGHT = Pattern.compile("(?:peso|weight)?\\s*(?:es|is)?\\s*(\\d{2,3}(?:[.,]\\d{1,2})?)\\s*kg");
    private static final Pattern BURNED = Pattern.compile("(\\d{2,4}(?:[.,]\\d+)?)\\s*(?:calorias|calories)\\s*(?:quemadas|burned)?");
    private static final Pattern SETS_REPS = Pattern.compile("(\\d{1,2})\\s*(?:x|series\\s+de)\\s*(\\d{1,3})");
    private static final Pattern SETS_REPS_WORDS = Pattern.compile("(\\d{1,2})\\s*series?\\s*(?:de\\s*)?(\\d{1,3})\\s*repeticiones?");
    private static final Pattern MINUTES = Pattern.compile("(\\d{1,3})\\s*(?:minutos|min|minutes?)");
    private static final Pattern EXERCISE_COUNT = Pattern.compile("(\\d{1,2})\\s*(?:ejercicios|exercises)");
    private static final Pattern QTY_UNIT = Pattern.compile("^(\\d+(?:[.,]\\d+)?)\\s*(kg|kilos?|gr|g|gramos?|latas?|tiras?|rebanadas?|slices?|piezas?|pieces?|porciones?)\\s+(.+)$");
    private static final Pattern QTY = Pattern.compile("^(\\d+(?:[.,]\\d+)?)\\s+(.+)$");
    private static final Pattern QUESTION = Pattern.compile("\\?|\\b(?:que deberia|voy bien|am i|how|what|should|como voy)\\b");
    private static final List<String> MEAL_KEYS = List.of("desayun", "almorc", "cena", "cene", "cené", "snack", "merienda");
    private static final List<String> WORKOUT_KEYS = List.of("fui al gym", "fui al gimnasio", "hice", "did", "trained", "lifted", "entrene", "entrené", "workout", "gym", "pull", "push", "legs", "leg day", "cardio", "ran", "run", "bench press");
    private static final List<String> FOOD_HINTS = List.of("egg", "eggs", "huevo", "huevos", "rice", "arroz", "chicken", "pollo", "bread", "pan", "tuna", "atun", "atún", "bacon", "carne", "ensalada", "toast", "avocado", "aguacate");
    private static final List<String> STOP_KEYS = List.of("pasos", "peso", "reloj", "calorias", "calories", "desayun", "almorc", "cena", "merienda", "snack");

    private final UserRepository userRepository;
    private final GoalRepository goalRepository;
    private final DailyLogRepository dailyLogRepository;
    private final BodyMetricsRepository bodyMetricsRepository;
    private final MealItemRepository mealItemRepository;
    private final AIRecommendationRepository aiRecommendationRepository;
    private final ExerciseRepository exerciseRepository;
    private final DailyLogService dailyLogService;
    private final MealService mealService;
    private final MealItemService mealItemService;
    private final WorkoutSessionService workoutSessionService;
    private final BodyMetricsService bodyMetricsService;
    private final ExerciseService exerciseService;
    private final GoalService goalService;
    private final NutritionMath nutritionMath;
    private final WorkoutSessionRepository workoutSessionRepository;

    @Transactional
    public ChatIntentResult processMessage(User user, String messageContent) {
        String normalized = normalize(messageContent);
        LocalDate date = LocalDate.now();
        Extracted e = extract(normalized);
        List<String> summary = new ArrayList<>();
        if (e.goalType != null) summary.add(logGoal(user, e.goalType));
        if (e.weightKg != null) summary.add(logWeight(user, date, e.weightKg));
        for (ExtractedMeal meal : e.meals) summary.add(logMeal(user, date, meal));
        if (!e.workouts.isEmpty()) summary.addAll(logWorkouts(user, date, e.workouts, e.workoutCaloriesExplicit));
        if (e.steps != null || e.dailyCaloriesBurned != null) summary.add(logDailyMetrics(user, date, e.steps, e.dailyCaloriesBurned));
        return new ChatIntentResult(e.question, summary, summary.isEmpty() ? null : String.join(" ", summary), date);
    }

    public PromptBuilder.ChatPromptContext buildPromptContext(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow();
        Goal goal = goalRepository.findTopByUserIdOrderByCreatedAtDescIdDesc(userId).orElse(null);
        DailyLog log = dailyLogRepository.findTopByUserIdOrderByLogDateDescIdDesc(userId).orElse(null);
        BodyMetrics metrics = bodyMetricsRepository.findTopByUserIdOrderByDateDescIdDesc(userId).orElse(null);
        double protein = log == null
                ? 0.0
                : mealItemRepository.findAllByDailyLogId(log.getId()).stream()
                .mapToDouble(nutritionMath::proteinFor)
                .sum();
        Object latestWeight = metrics != null && metrics.getWeight() != null ? metrics.getWeight() : user.getWeightKg() != null ? user.getWeightKg() : "unknown";
        double caloriesBurned = log == null
                ? 0.0
                : Math.max(
                        log.getCaloriesBurned() != null ? log.getCaloriesBurned() : 0.0,
                        workoutSessionRepository.sumCaloriesBurnedByDailyLogId(log.getId()) != null
                                ? workoutSessionRepository.sumCaloriesBurnedByDailyLogId(log.getId())
                                : 0.0
                );
        return new PromptBuilder.ChatPromptContext(
                user.getAge() != null ? user.getAge() : "unknown",
                user.getSex() != null ? user.getSex().name() : "unknown",
                user.getHeightCm() != null ? user.getHeightCm() : "unknown",
                user.getActivityLevel() != null ? user.getActivityLevel().name() : "unknown",
                user.getDietType() != null ? user.getDietType().name() : "STANDARD",
                goal != null && goal.getGoalType() != null ? goal.getGoalType().name() : "UNKNOWN",
                goal != null && goal.getTargetWeight() != null ? goal.getTargetWeight() : "unknown",
                goal != null && goal.getTargetCalories() != null ? goal.getTargetCalories() : "unknown",
                goal != null && goal.getTargetProtein() != null ? goal.getTargetProtein() : "unknown",
                goal != null && goal.getTargetCarbs() != null ? goal.getTargetCarbs() : "unknown",
                goal != null && goal.getTargetFat() != null ? goal.getTargetFat() : "unknown",
                log != null && log.getCaloriesConsumed() != null ? log.getCaloriesConsumed() : 0.0,
                caloriesBurned,
                calculateBalance(log),
                log != null && log.getSteps() != null ? log.getSteps() : 0,
                protein,
                latestWeight
        );
    }

    private Extracted extract(String normalized) {
        Double burned = parseDouble(BURNED, normalized);
        List<ExtractedWorkout> workouts = parseWorkouts(normalized, null);
        return new Extracted(
                parseInteger(STEPS, normalized),
                parseDouble(WEIGHT, normalized),
                isDailyBurnMetric(normalized) || workouts.isEmpty() ? burned : null,
                !workouts.isEmpty() && !isDailyBurnMetric(normalized) ? burned : null,
                parseGoal(normalized),
                parseMeals(normalized),
                workouts.isEmpty() ? workouts : parseWorkouts(normalized, !isDailyBurnMetric(normalized) ? burned : null),
                QUESTION.matcher(normalized).find()
        );
    }

    private List<ExtractedMeal> parseMeals(String text) {
        List<ExtractedMeal> meals = new ArrayList<>();
        addMealIfPresent(meals, text, "desayun", MealType.BREAKFAST);
        addMealIfPresent(meals, text, "almorc", MealType.LUNCH);
        addMealIfPresent(meals, text, "cena", MealType.DINNER);
        addMealIfPresent(meals, text, "cene", MealType.DINNER);
        addMealIfPresent(meals, text, "merienda", MealType.SNACK);
        addMealIfPresent(meals, text, "snack", MealType.SNACK);
        if (meals.isEmpty() && looksLikeGenericFoodLog(text)) {
            List<FoodEntry> items = parseFoodItems(text);
            if (!items.isEmpty()) {
                meals.add(new ExtractedMeal(MealType.SNACK, items));
            }
        }
        return meals;
    }

    private boolean looksLikeGenericFoodLog(String text) {
        if (text.contains("pasos") || text.contains("peso") || text.contains("calorias") || text.contains("calories")) return false;
        if (findWorkoutStart(text) >= 0) return false;
        boolean hasFoodHint = FOOD_HINTS.stream().anyMatch(text::contains);
        return hasFoodHint && (text.matches(".*\\d+.*") || text.contains("ate") || text.contains("comi") || text.contains("comio"));
    }

    private void addMealIfPresent(List<ExtractedMeal> meals, String text, String marker, MealType type) {
        int start = text.indexOf(marker);
        if (start < 0) return;
        int from = start + marker.length();
        int end = nextStop(text, from, true);
        List<FoodEntry> items = parseFoodItems(text.substring(from, Math.max(from, end)));
        if (!items.isEmpty()) meals.add(new ExtractedMeal(type, items));
    }

    private List<FoodEntry> parseFoodItems(String raw) {
        String prepared = raw.replaceAll("\\b(?:a las|alas|por la manana|por la tarde|por la noche|dice que|que comio|hoy|lleva|comio|comi|comio)\\b", " ")
                .replaceAll("(?<=[a-z])\\s+(?=\\d+(?:[.,]\\d+)?\\s*(?:kg|kilos?|gr|g|gramos?|latas?|tiras?|rebanadas?|piezas?|porciones?))", ", ")
                .replaceAll("\\s+", " ").trim();
        if (prepared.isBlank()) return List.of();
        List<FoodEntry> items = new ArrayList<>();
        for (String part : prepared.split("\\s*(?:,|\\by\\b|\\band\\b|\\bcon\\b)\\s*")) {
            String cleaned = part.trim().replaceAll("^(?:de|del|la|el)\\s+", "").replaceAll("\\band\\b$", "").trim();
            if (cleaned.isBlank() || cleaned.startsWith("fui al gym") || cleaned.startsWith("hice ")) continue;
            FoodEntry entry = parseFoodEntry(cleaned);
            if (entry != null && !entry.foodName.isBlank()) items.add(entry);
        }
        return items;
    }

    private FoodEntry parseFoodEntry(String value) {
        String cleanedValue = value.replaceFirst("^(?:i ate|ate|i had|had|i drank|drank|today)\\s+", "").trim();
        Matcher unit = QTY_UNIT.matcher(cleanedValue);
        if (unit.find()) return new FoodEntry(normalizeFood(unit.group(3)), convertQty(Double.parseDouble(unit.group(1).replace(',', '.')), unit.group(2)));
        Matcher qty = QTY.matcher(cleanedValue);
        if (qty.find()) return new FoodEntry(normalizeFood(qty.group(2)), Double.parseDouble(qty.group(1).replace(',', '.')));
        return new FoodEntry(normalizeFood(cleanedValue), 1.0);
    }

    private List<ExtractedWorkout> parseWorkouts(String text, Double explicitCalories) {
        int start = findWorkoutStart(text);
        if (start < 0) return List.of();
        int end = nextStop(text, start + 1, false);
        String segment = text.substring(start, end).trim();
        if (segment.isBlank()) return List.of();
        Scheme scheme = parseScheme(segment);
        if (segment.contains("bench press")) {
            Matcher matcher = Pattern.compile("^(\\d{1,2})x(\\d{1,3})").matcher(segment);
            if (matcher.find()) {
                scheme = new Scheme(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)));
            }
        }
        int duration = parseInteger(MINUTES, segment) != null ? parseInteger(MINUTES, segment) : 30;
        int count = parseInteger(EXERCISE_COUNT, segment) != null ? parseInteger(EXERCISE_COUNT, segment) : 1;
        List<String> names;
        if (segment.contains("bench press")) names = List.of("Bench Press");
        else if (segment.contains("cardio")) names = List.of("Cardio");
        else if (segment.contains("ran") || segment.matches(".*\\brun\\b.*")) names = List.of("Running");
        else names = parseExerciseNames(segment);
        if (names.size() == 1 && names.get(0).toLowerCase(Locale.ROOT).matches(".*(day|exercise|exercises|utes).*")) {
            names = List.of();
        }
        if (names.isEmpty()) {
            String base = resolveWorkoutName(segment);
            if (count <= 1) names = List.of(base);
            else {
                List<String> generated = new ArrayList<>();
                for (int i = 1; i <= count; i++) generated.add(base + " Exercise " + i);
                names = generated;
            }
        }
        double totalCalories = explicitCalories != null ? explicitCalories : estimateCalories(segment, names.size(), scheme.sets, scheme.reps, duration);
        double perWorkout = totalCalories / Math.max(1, names.size());
        int perDuration = Math.max(1, duration / Math.max(1, names.size()));
        List<ExtractedWorkout> workouts = new ArrayList<>();
        for (String name : names) workouts.add(new ExtractedWorkout(name, scheme.sets, scheme.reps, perDuration, perWorkout));
        return workouts;
    }

    private int findWorkoutStart(String text) {
        boolean metricsOnlyPhrase = (text.contains("pasos") || text.contains("calorias") || text.contains("calories"))
                && !text.contains("gym")
                && !text.contains("gimnasio")
                && !text.contains("press")
                && !text.contains("sentadilla")
                && !text.contains("pecho")
                && !text.contains("espalda")
                && !text.contains("hombro")
                && !text.contains("pierna")
                && !text.contains("cardio")
                && !text.contains("run")
                && !text.contains("correr")
                && !SETS_REPS.matcher(text).find()
                && !SETS_REPS_WORDS.matcher(text).find();

        int start = Integer.MAX_VALUE;
        for (String key : WORKOUT_KEYS) {
            if (metricsOnlyPhrase && "hice".equals(key)) {
                continue;
            }
            int idx = text.indexOf(key);
            if (idx >= 0 && idx < start) start = idx;
        }
        if (SETS_REPS.matcher(text).find() || SETS_REPS_WORDS.matcher(text).find()) {
            start = Math.min(start, 0);
        }
        if (start != Integer.MAX_VALUE) return start;
        return -1;
    }

    private List<String> parseExerciseNames(String segment) {
        String cleaned = segment.replaceAll("\\b(?:fui al gym|fui al gimnasio|hice|entrene|entrené|workout|training|pecho|espalda|hombros|piernas|push|pull|legs|gym)\\b", " ")
                .replaceAll("\\b(?:did|trained|lifted|ran|running|walked|walking)\\b", " ")
                .replaceAll("\\d+\\s*(?:x|series\\s+de)\\s*\\d+", " ")
                .replaceAll("\\d+\\s*series?\\s*(?:de\\s*)?\\d+\\s*repeticiones?", " ")
                .replaceAll("\\d+\\s*(?:minutos|min|minutes?)", " ")
                .replaceAll("\\d+\\s*(?:calorias|calories)\\s*(?:quemadas|burned)?", " ")
                .replaceAll("\\s+", " ").trim();
        if (cleaned.isBlank()) return List.of();
        LinkedHashSet<String> names = new LinkedHashSet<>();
        for (String part : cleaned.split("\\s*(?:,|\\by\\b)\\s*")) {
            String value = part.trim();
            if (value.isBlank() || value.matches("\\d+")) continue;
            names.add(titleCase(value));
        }
        return new ArrayList<>(names);
    }

    private String logDailyMetrics(User user, LocalDate date, Integer steps, Double caloriesBurned) {
        DailyLog log = resolveLog(user, date);
        DailyLogRequest request = new DailyLogRequest();
        request.setUserId(user.getId());
        request.setLogDate(log.getLogDate());
        request.setSteps(steps != null ? steps : log.getSteps());
        request.setCaloriesConsumed(log.getCaloriesConsumed());
        request.setCaloriesBurned(caloriesBurned != null ? caloriesBurned : log.getCaloriesBurned());
        dailyLogService.createDailyLog(request);
        invalidateCoaching(log.getId());
        List<String> parts = new ArrayList<>();
        if (steps != null) parts.add("Se guardaron " + steps + " pasos.");
        if (caloriesBurned != null) parts.add("Se registraron " + trim(caloriesBurned) + " calorias quemadas.");
        return parts.isEmpty() ? "Se actualizo el registro diario." : String.join(" ", parts);
    }

    private String logWeight(User user, LocalDate date, Double weightKg) {
        BodyMetrics existing = bodyMetricsRepository.findAllByUserIdOrderByDateDescIdDesc(user.getId()).stream().filter(m -> date.equals(m.getDate())).findFirst().orElse(null);
        if (existing != null) {
            existing.setWeight(weightKg);
            bodyMetricsRepository.save(existing);
        } else {
            BodyMetricsRequest request = new BodyMetricsRequest();
            request.setWeight(weightKg);
            request.setDate(date);
            bodyMetricsService.createBodyMetrics(user.getEmail(), request);
        }
        user.setWeightKg(weightKg);
        userRepository.save(user);
        return "Se guardo tu peso en " + trim(weightKg) + " kg.";
    }

    private List<String> logWorkouts(User user, LocalDate date, List<ExtractedWorkout> workouts, Double explicitCalories) {
        DailyLog log = resolveLog(user, date);
        List<String> out = new ArrayList<>();
        double totalCalories = 0.0;
        for (ExtractedWorkout workout : workouts) {
            WorkoutSessionRequest request = new WorkoutSessionRequest();
            request.setDailyLogId(log.getId());
            request.setExerciseId(resolveOrCreateExercise(workout.name).getId());
            request.setSets(workout.sets);
            request.setReps(workout.reps);
            request.setDuration(workout.durationMinutes);
            request.setCaloriesBurned(workout.caloriesBurned);
            workoutSessionService.createWorkoutSession(request);
            totalCalories += workout.caloriesBurned;
            out.add("Entreno guardado: " + workout.name + " " + workout.sets + "x" + workout.reps + ".");
        }
        if (explicitCalories == null && totalCalories > 0) {
            DailyLogRequest update = new DailyLogRequest();
            update.setUserId(user.getId());
            update.setLogDate(log.getLogDate());
            update.setSteps(log.getSteps());
            update.setCaloriesConsumed(log.getCaloriesConsumed());
            update.setCaloriesBurned(totalCalories);
            dailyLogService.createDailyLog(update);
        }
        invalidateCoaching(log.getId());
        return out;
    }

    private String logMeal(User user, LocalDate date, ExtractedMeal meal) {
        DailyLog log = resolveLog(user, date);
        MealRequest mealRequest = new MealRequest();
        mealRequest.setDailyLogId(log.getId());
        mealRequest.setMealType(meal.mealType);
        UUID mealId = mealService.createMeal(mealRequest).getId();
        for (FoodEntry item : meal.items) {
            MealItemRequest request = new MealItemRequest();
            request.setMealId(mealId);
            request.setFoodName(item.foodName);
            request.setQuantity(item.quantity);
            mealItemService.createMealItem(request);
        }
        invalidateCoaching(log.getId());
        return "Comida guardada en " + meal.mealType.name().toLowerCase(Locale.ROOT) + ": " + meal.items.size() + " item(s).";
    }

    private String logGoal(User user, UserGoalType goalType) {
        GoalRequest request = new GoalRequest();
        request.setGoalType(goalType);
        request.setTargetWeight(user.getWeightKg());
        goalService.createGoal(user.getEmail(), request);
        return "Objetivo actualizado a " + switch (goalType) { case LOSE_WEIGHT -> "perder peso"; case BUILD_MUSCLE -> "ganar musculo"; case MAINTAIN -> "mantener"; } + ".";
    }

    private Exercise resolveOrCreateExercise(String name) {
        return exerciseRepository.findFirstByNameIgnoreCase(name).orElseGet(() -> {
            ExerciseRequest request = new ExerciseRequest();
            request.setName(name);
            request.setMuscleGroup(inferMuscleGroup(name));
            request.setEquipment("Unknown");
            request.setDescription("Created from conversational workout logging.");
            UUID id = exerciseService.createExercise(request).getId();
            return exerciseRepository.findById(id).orElseThrow();
        });
    }

    private DailyLog resolveLog(User user, LocalDate date) {
        UUID id = dailyLogService.getOrCreateLogForDate(user.getId(), date).getId();
        return dailyLogRepository.findById(id).orElseThrow();
    }

    private UserGoalType parseGoal(String text) {
        if (text.contains("perder peso") || text.contains("bajar de peso") || text.contains("lose weight") || text.contains("lose fat")) return UserGoalType.LOSE_WEIGHT;
        if (text.contains("ganar musculo") || text.contains("ganar musculo") || text.contains("build muscle") || text.contains("gain muscle")) return UserGoalType.BUILD_MUSCLE;
        if (text.contains("mantener") || text.contains("maintain")) return UserGoalType.MAINTAIN;
        return null;
    }

    private boolean isDailyBurnMetric(String text) {
        return text.contains("reloj") || text.contains("watch");
    }

    private int nextStop(String text, int from, boolean stopOnWorkout) {
        int end = text.length();
        for (String key : STOP_KEYS) {
            int idx = text.indexOf(key, from);
            if (idx >= from && idx < end) end = idx;
        }
        if (stopOnWorkout) for (String key : WORKOUT_KEYS) {
            int idx = text.indexOf(key, from);
            if (idx >= from && idx < end) end = idx;
        }
        return end;
    }

    private Scheme parseScheme(String segment) {
        Integer sets = parseInteger(SETS_REPS, segment);
        Integer reps = null;
        Matcher compact = SETS_REPS.matcher(segment);
        if (compact.find()) reps = Integer.parseInt(compact.group(2));
        if (sets != null && reps != null) return new Scheme(sets, reps);
        Matcher words = SETS_REPS_WORDS.matcher(segment);
        if (words.find()) return new Scheme(Integer.parseInt(words.group(1)), Integer.parseInt(words.group(2)));
        return new Scheme(4, 8);
    }

    private String resolveWorkoutName(String segment) {
        if (segment.contains("pull")) return "Pull";
        if (segment.contains("push")) return "Push";
        if (segment.contains("pierna") || segment.contains("legs") || segment.contains("leg")) return "Leg Day";
        if (segment.contains("pecho") || segment.contains("chest")) return "Chest";
        if (segment.contains("espalda") || segment.contains("back")) return "Back";
        if (segment.contains("hombro") || segment.contains("shoulder")) return "Shoulders";
        if (segment.contains("cardio")) return "Cardio";
        if (segment.contains("run") || segment.contains("correr")) return "Running";
        return "Workout";
    }

    private double estimateCalories(String segment, int count, int sets, int reps, int duration) {
        double base = Math.max(120.0, Math.max(1, count) * sets * reps * 0.75);
        if (segment.contains("cardio") || segment.contains("run") || segment.contains("correr")) return Math.max(base, duration * 8.0);
        return base;
    }

    private Integer parseInteger(Pattern pattern, String text) {
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : null;
    }

    private Double parseDouble(Pattern pattern, String text) {
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? Double.parseDouble(matcher.group(1).replace(',', '.')) : null;
    }

    private double calculateBalance(DailyLog log) {
        if (log == null) return 0.0;
        double caloriesBurned = Math.max(
                log.getCaloriesBurned() != null ? log.getCaloriesBurned() : 0.0,
                workoutSessionRepository.sumCaloriesBurnedByDailyLogId(log.getId()) != null
                        ? workoutSessionRepository.sumCaloriesBurnedByDailyLogId(log.getId())
                        : 0.0
        );
        return (log.getCaloriesConsumed() != null ? log.getCaloriesConsumed() : 0.0) - caloriesBurned;
    }

    private void invalidateCoaching(UUID dailyLogId) {
        aiRecommendationRepository.deleteByDailyLogId(dailyLogId);
    }

    private String normalize(String value) {
        if (value == null) return "";
        return Normalizer.normalize(value.toLowerCase(Locale.ROOT), Normalizer.Form.NFD).replaceAll("\\p{M}", "").replaceAll("\\s+", " ").trim();
    }

    private String normalizeFood(String value) {
        return value.replaceAll("\\b(?:i ate|ate|i had|had|i drank|drank|my meal was|today|de|del|la|el|una|un|que|dice|lleva|por|las|am|pm)\\b", " ").replaceAll("\\s+", " ").trim();
    }

    private double convertQty(double quantity, String unit) {
        return switch (unit) { case "kg", "kilo", "kilos" -> quantity * 1000.0; case "g", "gr", "gramo", "gramos" -> quantity; default -> quantity; };
    }

    private String titleCase(String value) {
        return Arrays.stream(value.split("\\s+")).filter(part -> !part.isBlank()).map(part -> Character.toUpperCase(part.charAt(0)) + part.substring(1)).reduce((a, b) -> a + " " + b).orElse("Workout");
    }

    private String inferMuscleGroup(String workoutName) {
        String normalized = workoutName.toLowerCase(Locale.ROOT);
        if (normalized.contains("pull") || normalized.contains("espalda") || normalized.contains("back")) return "Back";
        if (normalized.contains("push") || normalized.contains("pecho") || normalized.contains("chest")) return "Chest";
        if (normalized.contains("leg") || normalized.contains("pierna")) return "Legs";
        if (normalized.contains("shoulder") || normalized.contains("hombro")) return "Shoulders";
        return "Full Body";
    }

    private String trim(double value) {
        return value == (long) value ? String.valueOf((long) value) : String.format(Locale.US, "%.1f", value);
    }

    public record ChatIntentResult(boolean question, List<String> loggedSummary, String structuredAction, LocalDate targetDate) {}
    private record Extracted(Integer steps, Double weightKg, Double dailyCaloriesBurned, Double workoutCaloriesExplicit, UserGoalType goalType, List<ExtractedMeal> meals, List<ExtractedWorkout> workouts, boolean question) {}
    private record ExtractedMeal(MealType mealType, List<FoodEntry> items) {}
    private record FoodEntry(String foodName, double quantity) {}
    private record ExtractedWorkout(String name, int sets, int reps, int durationMinutes, double caloriesBurned) {}
    private record Scheme(int sets, int reps) {}
}
